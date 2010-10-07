package net.datacrow.console.wizards.itemexport;

import javax.swing.JPanel;

import net.datacrow.console.wizards.IWizardPanel;

public abstract class ItemExporterWizardPanel extends JPanel implements IWizardPanel {
    
    protected ItemExporterWizard wizard;
    protected ItemExporterDefinition definition;
    
    public ItemExporterWizardPanel(ItemExporterWizard wizard) {
        this.wizard = wizard;
        this.definition = wizard.getDefinition();
    }

    @Override
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
    
    @Override
    public void onDeactivation() {}
    
    @Override
    public void onActivation() {}    
}
