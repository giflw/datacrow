package net.datacrow.console.wizards.migration.itemexport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.datacrow.console.wizards.IWizardPanel;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;

public class ItemExporterWizard extends Wizard {

    private ItemExporterDefinition definition;
    private Collection<DcObject> items;
    
	public ItemExporterWizard(int module, Collection<DcObject> items) {
		super(module);
		this.items = items;
	}

    public ItemExporterDefinition getDefinition() {
        return definition;
    }
    
    public Collection<DcObject> getItems() {
        return items;
    }

    @Override
    public void finish() throws WizardException {
        
        if (definition != null && definition.getExporter() != null)
            definition.getExporter().cancel();
        
        definition = null;
        items = null;
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
        definition = new ItemExporterDefinition();

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
