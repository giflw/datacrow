package net.datacrow.console.wizards.itemexport;

import java.util.ArrayList;
import java.util.List;

import net.datacrow.console.wizards.IWizardPanel;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class ItemExporterWizard extends Wizard {

    private ItemExporterDefinition definition;
    private List<String> items;
    
	public ItemExporterWizard(int module, List<String> items) {
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
    
    public List<String> getItems() {
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
    	panels.add(new ItemExporterSelectFieldsPanel(this));
    	panels.add(new ItemExporterTaskPanel(this));
    	return panels;
    }

    @Override
    protected void initialize() {}

    @Override
    protected void saveSettings() {
        DcSettings.set(DcRepository.Settings.stItemExporterWizardFormSize, getSize());
    }
}
