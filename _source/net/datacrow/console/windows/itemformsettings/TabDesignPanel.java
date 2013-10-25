package net.datacrow.console.windows.itemformsettings;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;

import net.datacrow.console.Layout;
import net.datacrow.console.components.panels.FieldSelectionPanel;
import net.datacrow.console.components.panels.IFieldSelectionListener;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Tab;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.DcFieldDefinitions;

public class TabDesignPanel extends JPanel implements IFieldSelectionListener {
    
    private FieldSelectionPanel pnlFields;
    
    private DcObject tab;
    private String tabName;
    private DcModule module;
    
    public TabDesignPanel(DcModule module, DcObject tab) {
        this.module = module;
        this.tab = tab;
        this.tabName = (String) tab.getValue(Tab._A_NAME);
        
        build();
        refresh();
    }

    @Override
    public void fieldSelected(DcField field) {
        field.getDefinition().setTab(tabName);
    }
    
    @Override
    public void fieldDeselected(DcField field) {
        field.getDefinition().setTab(null);
    }
    
    private String getTabName(String tab) {
        return tab != null && tab.startsWith("lbl") ? DcResources.getText(tab) : tab;
    }
    
    private boolean isAllowed(int fieldIdx) {
        DcField field = module.getField(fieldIdx);
        
        return  field.isEnabled() && 
                field.getValueType() != DcRepository.ValueTypes._PICTURE &&
                field.getIndex() != DcObject._SYS_AVAILABLE &&
                field.getIndex() != DcObject._SYS_LENDBY &&
                field.getIndex() != DcObject._SYS_LOANDUEDATE &&
                field.getIndex() != DcObject._SYS_LOANDURATION &&
                field.getIndex() != DcObject._SYS_LOANENDDATE &&
                field.getIndex() != DcObject._SYS_LOANSTARTDATE &&
                field.getIndex() != DcObject._SYS_LOANSTATUS &&
                field.getIndex() != DcObject._SYS_LOANSTATUSDAYS &&
                field.getIndex() != DcObject._SYS_DISPLAYVALUE &&
                field.getIndex() != DcObject._SYS_MODULE;
    }

    protected void save(DcFieldDefinitions definitions) {
        for (DcField field : pnlFields.getSelectedFields()) {
            definitions.add(field.getDefinition());
        }
    }
    
    private List<DcField> getSelectedFields() {
        List<DcField> fields = new ArrayList<DcField>();
        for (DcFieldDefinition def : module.getFieldDefinitions().getDefinitions()) {
           if (isAllowed(def.getIndex()) && tabName.equals(getTabName(def.getTab())))
               fields.add(module.getField(def.getIndex()));
        }
        return fields;
    }
    
    private List<DcField> getAvailableFields() {
        List<DcField> fields = new ArrayList<DcField>();
        for (DcFieldDefinition def : module.getFieldDefinitions().getDefinitions()) {
            if (isAllowed(def.getIndex()) && StringUtils.isEmpty(def.getTab()))
                fields.add(module.getField(def.getIndex()));
        }
        return fields;
    }
    
    protected void refresh() {
        List<DcField> fields = new ArrayList<DcField>();
        List<DcField> selected = getSelectedFields();
        fields.addAll(selected);
        fields.addAll(getAvailableFields());
        
        pnlFields.setFields(fields);
        pnlFields.setSelectedFields(selected);
    }
    
    private void build() {        
        setLayout(Layout.getGBL());
        pnlFields = new FieldSelectionPanel(module, false, true, true);
        pnlFields.setFieldSelectionListener(this);
        
        add(pnlFields,  Layout.getGBC( 0, 0, 1, 1, 40.0, 40.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 5, 5), 0, 0));
    }
}
