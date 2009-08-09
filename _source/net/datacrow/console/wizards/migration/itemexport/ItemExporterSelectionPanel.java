package net.datacrow.console.wizards.migration.itemexport;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.migration.itemexport.ItemExporter;
import net.datacrow.core.migration.itemexport.ItemExporters;

public class ItemExporterSelectionPanel extends ItemExporterWizardPanel implements MouseListener {

    private ButtonGroup bg;
    private Collection<ItemExporter> exporters = new ArrayList<ItemExporter>();
    
    public ItemExporterSelectionPanel(ItemExporterWizard wizard) {
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
        bg  = null;
        if (exporters != null) exporters.clear();
        exporters = null;
    }  
    
    private void build() {
        setLayout(Layout.getGBL());
        
        int y = 0;
        int x = 0;
        
        for (ItemExporter exporter : ItemExporters.getInstance().getExporters(wizard.getModuleIdx())) {
            exporters.add(exporter);
            JRadioButton rb = ComponentFactory.getRadioButton(exporter.getName(), exporter.getIcon(), exporter.getKey());
            rb.addMouseListener(this);
            bg.add(rb);
            add(rb, Layout.getGBC( x, y++, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets( 0, 5, 5, 5), 0, 0));
        }
    }
    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {
        String command = bg.getSelection().getActionCommand();
        for (ItemExporter exporter : exporters) {
            if (exporter.getKey().equals(command)) {
                wizard.getDefinition().setExporter(exporter);
                try {
                    wizard.next();
                } catch (WizardException we) {
                    new MessageBox(we.getMessage(), MessageBox._WARNING);
                }
            }
        }
    }      
}
