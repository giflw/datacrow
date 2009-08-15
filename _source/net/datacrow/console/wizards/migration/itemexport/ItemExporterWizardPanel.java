package net.datacrow.console.wizards.migration.itemexport;

import javax.swing.JPanel;

import net.datacrow.console.wizards.IWizardPanel;

public abstract class ItemExporterWizardPanel extends JPanel implements IWizardPanel {
    
    protected ItemExporterWizard wizard;
    protected ItemExporterDefinition definition;
    
    public ItemExporterWizardPanel(ItemExporterWizard wizard) {
        this.wizard = wizard;
        this.definition = wizard.getDefinition();
    }

    public void destroy() {
        wizard = null;
        definition = null;
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) onActivation();
        else onDeactivation();
    }
    
    public void onDeactivation() {}
    
    public void onActivation() {}    
}