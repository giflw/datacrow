package net.datacrow.console.wizards.itemimport;

import javax.swing.JPanel;

import net.datacrow.console.wizards.IWizardPanel;

public abstract class ItemImporterWizardPanel extends JPanel implements IWizardPanel{

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
