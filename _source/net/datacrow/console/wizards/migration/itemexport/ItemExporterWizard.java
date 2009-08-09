package net.datacrow.console.wizards.migration.itemexport;

import java.util.ArrayList;
import java.util.List;

import net.datacrow.console.wizards.IWizardPanel;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.resources.DcResources;

public class ItemExporterWizard extends Wizard {

    private ItemExporterDefinition definition = new ItemExporterDefinition();
    
	public ItemExporterWizard() {
		super();
	}

    public ItemExporterDefinition getDefinition() {
        return definition;
    }

    @Override
    public void finish() throws WizardException {
        close();
    }

    @Override
    protected boolean isRestartSupported() {
        return false;
    }    
    
    @Override
    protected String getWizardName() {
        return DcResources.getText("lblMigrationWizard");
    }
    
    @Override
    protected List<IWizardPanel> getWizardPanels() {
    	List<IWizardPanel> panels = new ArrayList<IWizardPanel>();
    	panels.add(new ItemExporterSelectionPanel(this));
    	panels.add(new ItemExporterSettingsPanel(this));
    	panels.add(new ItemExporterPanel(this));
    	return panels;
    }

    @Override
    protected void initialize() {}

    @Override
    protected void saveSettings() {}
}
