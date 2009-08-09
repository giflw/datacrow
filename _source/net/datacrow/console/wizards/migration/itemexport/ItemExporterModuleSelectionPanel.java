package net.datacrow.console.wizards.migration.itemexport;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.components.tables.DcTableModel;
import net.datacrow.console.wizards.WizardException;

public class ItemExporterModuleSelectionPanel extends ItemExporterWizardPanel implements ActionListener {

    private DcTable table;
    
    public ItemExporterModuleSelectionPanel(ItemExporterWizard wizard) {
        super(wizard);
        build();
    }
    
    public Object apply() throws WizardException {
        
        
        
        return definition;
    }

    public String getHelpText() {
        return null;
    }
    
    @Override
    public void destroy() {
        if (table != null) table.clear();
        table = null;
    }  
    
    private void add() {
        //new ModuleExportDefinition
    }

    private void remove() {
        
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        /*************************************
         * Overview 
         *************************************/
        table = ComponentFactory.getDCTable(false, false);

        DcTableModel model = (DcTableModel) table.getModel();
        model.setColumnCount(3);

        TableColumn columnName = table.getColumnModel().getColumn(0);
        columnName.setHeaderValue("MODULE");

        TableColumn columnField = table.getColumnModel().getColumn(1);
        columnField.setHeaderValue("FILE");

        TableColumn cExporter = table.getColumnModel().getColumn(2);
        cExporter.setHeaderValue("EXPORTER");
        
        table.applyHeaders();

        JScrollPane scroller = new JScrollPane(table);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        /*************************************
         * Actions 
         *************************************/
        JPanel panelActions = new JPanel();

        
        

        add(scroller, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets( 0, 5, 5, 5), 0, 0));
        
        
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("add")) 
            add();
        else if (ae.getActionCommand().equals("remove")) 
            remove();
        
        
    }
}
