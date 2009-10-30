package net.datacrow.console.wizards.itemimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.components.tables.DcTableModel;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.DcRepository;
import net.datacrow.core.migration.itemimport.ItemImporter;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

import org.apache.log4j.Logger;

public class ItemImporterMappingPanel extends ItemImporterWizardPanel {

	private static Logger logger = Logger.getLogger(ItemImporterMappingPanel.class.getName());
	
    private ItemImporterWizard wizard;
    private DcTable table;
    
    public ItemImporterMappingPanel(ItemImporterWizard wizard) {
        this.wizard = wizard;
        
        build();
    }
    
	public Object apply() throws WizardException {
        ItemImporter importer = wizard.getDefinition().getImporter();
        
        for (int i = 0; i < table.getRowCount(); i++) {
        	String source = (String) table.getValueAt(i, 1, true);
        	DcField target = (DcField) table.getValueAt(i, 2, true);
        	if (target != null)
        		importer.addMapping(source,  target);
        }
        return wizard.getDefinition();
    }

    public void destroy() {
    	wizard = null;
    }

    public String getHelpText() {
        return DcResources.getText("msgImportFieldMapping");
    }
    
    @Override
    public void onActivation() {
    	if (wizard.getDefinition() != null && 
    	    wizard.getDefinition().getFile() != null) {
    	    
            table.clear();
            
            try {
                ItemImporter reader = wizard.getDefinition().getImporter();
                int veld = 1;
                for (String source : reader.getSourceFields())
                    table.addRow(new Object[] {Integer.valueOf(veld++), source, reader.getTargetField(source)});

            } catch (Exception exp) {
                DcSwingUtilities.displayErrorMessage(DcResources.getText("msgFileCannotBeUsed") + ": " + exp.getMessage());
                logger.error("Error while reading from file", exp);
            }        
    	}
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        //**********************************************************
        //Create Import Panel
        //**********************************************************
        table = ComponentFactory.getDCTable(false, false);

        DcTableModel model = (DcTableModel) table.getModel();
        model.setColumnCount(3);

        TableColumn columnNr = table.getColumnModel().getColumn(0);
        columnNr.setCellEditor(new DefaultCellEditor(ComponentFactory.getTextFieldDisabled()));
        columnNr.setHeaderValue(DcResources.getText("lblField"));
        
        TableColumn columnName = table.getColumnModel().getColumn(1);
        columnName.setCellEditor(new DefaultCellEditor(ComponentFactory.getTextFieldDisabled()));
        columnName.setHeaderValue(DcResources.getText("lblSourceName"));

        TableColumn columnField = table.getColumnModel().getColumn(2);
        JComboBox comboFields = ComponentFactory.getComboBox();
        columnField.setHeaderValue(DcResources.getText("lblTargetName"));

        for (DcField field : wizard.getModule().getFields()) {
            if (    (!field.isUiOnly() || 
                      field.getValueType() == DcRepository.ValueTypes._PICTURE || 
                      field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) && 
                     field.getIndex() != DcObject._ID)
                
                comboFields.addItem(field);
        }
        
        columnField.setCellEditor(new DefaultCellEditor(comboFields));

        table.applyHeaders();

        JScrollPane scroller = new JScrollPane(table);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel panelImportDef = new JPanel();
        panelImportDef.setLayout(Layout.getGBL());

        add(scroller,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 5, 5), 0, 0));
    }
}
