package net.datacrow.console.wizards.itemimport;

import java.util.ArrayList;
import java.util.List;

import net.datacrow.console.wizards.IWizardPanel;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class ItemImporterWizard extends Wizard {

    public static final int _STEP_MAPPING = 2;
    
	private ItemImporterDefinition definition;
	
	public ItemImporterWizard(int moduleIdx) {
		super(moduleIdx);
		
		setTitle(getWizardName());
		setHelpIndex("dc.migration.wizard.importer");
		
		this.definition = new ItemImporterDefinition();
		setSize(DcSettings.getDimension(DcRepository.Settings.stItemImporterWizardFormSize));
		setCenteredLocation();
	}
	
	protected ItemImporterDefinition getDefinition() {
		return definition;
	}

	@Override
    protected boolean isRestartSupported() {
	    return false;
    }
	
    @Override
    public void finish() throws WizardException {
        if (definition != null && definition.getImporter() != null)
            definition.getImporter().cancel();

        definition = null;
        close();
    }

    @Override
    protected String getWizardName() {
        return DcResources.getText("lblItemImportWizard");
    }
    
    @Override
    public void next() throws WizardException {
        if (getDefinition() != null && getDefinition().getImporter() != null) {
            if (!getDefinition().getImporter().requiresMapping()) {
                if (!skip.contains(Integer.valueOf(_STEP_MAPPING)))
                    skip.add(Integer.valueOf(_STEP_MAPPING));
            } else {
                while (skip.contains(Integer.valueOf(_STEP_MAPPING)))
                    skip.remove(Integer.valueOf(_STEP_MAPPING));
            }
        }
        
        super.next();
    }

    @Override
    protected List<IWizardPanel> getWizardPanels() {
    	List<IWizardPanel> panels = new ArrayList<IWizardPanel>();
    	panels.add(new ItemImporterSelectionPanel(this));
    	panels.add(new ItemImporterDefinitionPanel(this));
    	panels.add(new ItemImporterMappingPanel(this));
    	panels.add(new ItemImporterTaskPanel(this));
    	return panels;
    }

    @Override
    protected void initialize() {}

    @Override
    protected void saveSettings() {
        DcSettings.set(DcRepository.Settings.stItemImporterWizardFormSize, getSize());
    }
}
