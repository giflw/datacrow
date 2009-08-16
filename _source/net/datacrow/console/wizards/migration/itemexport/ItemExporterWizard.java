package net.datacrow.console.wizards.migration.itemexport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.datacrow.console.wizards.IWizardPanel;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class ItemExporterWizard extends Wizard {

    private ItemExporterDefinition definition;
    private Collection<DcObject> items;
    
	public ItemExporterWizard(int module, Collection<DcObject> items) {
		super(module);
		
		setTitle(getWizardName());
		setHelpIndex("dc.migration.wizard.exporter");
		
		this.items = items;
		setSize(DcSettings.getDimension(DcRepository.Settings.stItemExporterWizardFormSize));
		setCenteredLocation();
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
        return DcResources.getText("lblItemExportWizard");
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
    protected void saveSettings() {
        DcSettings.set(DcRepository.Settings.stItemExporterWizardFormSize, getSize());
    }
}
