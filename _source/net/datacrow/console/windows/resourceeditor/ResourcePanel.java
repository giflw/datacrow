package net.datacrow.console.windows.resourceeditor;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.resources.DcLanguageResource;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.Utilities;

public class ResourcePanel extends JPanel {
    
    private DcTable table = ComponentFactory.getDCTable(false, false);
    private String filterKey;
    
    public ResourcePanel(String key) {
        this.filterKey = key;
        build();
    }
    
    public void load(DcLanguageResource resources) {
        
        Set<String> keys = resources.getResourcesMap().keySet();
        ArrayList<String> list = new ArrayList<String>(keys);
        Collections.sort(list);

        for (String resourceKey : list) {
            if (resourceKey.startsWith(filterKey)) 
                table.addRow(new Object[] {resourceKey, resources.get(resourceKey)});    
        }
        
        if (filterKey.equals("sys")) {
            for (DcModule module : DcModules.getAllModules()) {
                if (module.isTopModule() || 
                    module.isChildModule() || 
                    module.getType() == DcModule._TYPE_PROPERTY_MODULE || 
                    module.isAbstract()) {
                    
                    if (!Utilities.isEmpty(module.getLabel()) && Utilities.isEmpty(resources.get( module.getModuleResourceKey())))
                        table.addRow(new Object[] {module.getModuleResourceKey(), module.getLabel()});
                    
                    if (!Utilities.isEmpty(module.getObjectName()) && Utilities.isEmpty(resources.get(module.getItemResourceKey())))
                        table.addRow(new Object[] {module.getItemResourceKey(), module.getObjectName()});

                    if (!Utilities.isEmpty(module.getObjectNamePlural()) && Utilities.isEmpty(resources.get(module.getItemPluralResourceKey())))
                        table.addRow(new Object[] {module.getItemPluralResourceKey(), module.getObjectNamePlural()});

                    for (DcField field : module.getFields()) {
                        if (!Utilities.isEmpty(field.getLabel()) && Utilities.isEmpty(resources.get(field.getResourceKey())))
                            table.addRow(new Object[] {field.getResourceKey(),field.getLabel()});
                    }
                }
            }
        }
    }
    
    public void save(DcLanguageResource dlr) {
        String key;
        String value;
        
        for (int i = 0; i < table.getRowCount(); i++) {
            key = (String) table.getValueAt(i, 0, true);
            value = (String) table.getValueAt(i, 1, true);
            dlr.put(key, value);
        }
    }
    
    private void build() {
        JScrollPane sp = new JScrollPane(table);
        table.setColumnCount(2);
    
        TableColumn columnKey = table.getColumnModel().getColumn(0);
        columnKey.setMaxWidth(300);
        columnKey.setPreferredWidth(150);
        columnKey.setCellEditor(new DefaultCellEditor(ComponentFactory.getTextFieldDisabled()));       
        columnKey.setHeaderValue(DcResources.getText("lblKey"));
        
        TableColumn columnValue = table.getColumnModel().getColumn(1);
        columnValue.setCellEditor(new DefaultCellEditor(ComponentFactory.getShortTextField(1000)));
        columnValue.setHeaderValue(DcResources.getText("lblValue"));
        
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);        
        
        table.applyHeaders();
        
        ComponentFactory.setBorder(sp);
        
        setLayout(Layout.getGBL());
        add(sp,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
    }
}
