package net.datacrow.console.wizards.itemimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFileField;
import net.datacrow.console.components.IComponent;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.settings.Setting;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.filefilters.DcFileFilter;

public class ItemImporterDefinitionPanel extends ItemImporterWizardPanel {

    private DcFileField source;
    private ItemImporterWizard wizard;
    private Map<String, IComponent> settings = new HashMap<String, IComponent>();
    
    public ItemImporterDefinitionPanel(ItemImporterWizard wizard) {
        this.wizard = wizard;
        build();
    }

    @Override
	public void onActivation() {
    	removeAll();
		build();

		if (	wizard.getDefinition() != null && 
				wizard.getDefinition().getImporter() != null) {
			
			settings.clear();
			
			source.setFileFilter(new DcFileFilter(wizard.getDefinition().getImporter().getSupportedFileTypes()));
			source.setFile(wizard.getDefinition().getFile());
			
			int y = 1;
	        for (String key : wizard.getDefinition().getImporter().getSettingKeys()) {
	        	Setting setting = DcSettings.getSetting(key) != null ? DcSettings.getSetting(key) : 
	        		wizard.getModule().getSettings().getSetting(key);
	        	
	        	JComponent c = setting.getUIComponent();
	        	settings.put(key, (IComponent) c);
	        	
	        	JLabel label = ComponentFactory.getLabel(setting.getLabelText());
	
	        	add(label, 
	        	         Layout.getGBC( 0, y, 1, 1, 1.0, 1.0
	                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
	                     new Insets( 0, 5, 5, 5), 0, 0));
	            add(c,   Layout.getGBC( 1, y, 1, 1, 1.0, 1.0
	                    ,GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL,
	                     new Insets( 0, 5, 5, 5), 0, 0));
            	
	            ((IComponent) c).setValue(setting.getValue());
	             
	        	y++;
	        }
		}
	}

	public Object apply() throws WizardException {
        if (source.getFile() == null)
            throw new WizardException(DcResources.getText("msgNoFileSelected"));
        
        if (!source.getFile().exists() || !source.getFile().canRead())
            throw new WizardException(DcResources.getText("msgFileCannotBeUsed"));
        
        wizard.getDefinition().getImporter().clearMappings();
        
        // store the settings
        // note: I have made sure this works for both module and application settings 
        for (String key : settings.keySet()) {
            Setting setting = DcSettings.getSetting(key) != null ? DcSettings.getSetting(key) : wizard.getModule().getSettings().getSetting(key);
            setting.setValue(settings.get(key).getValue());
        }        
        
        try {
            wizard.getDefinition().getImporter().setFile(source.getFile());
        } catch (Exception e) {
            throw new WizardException(e.getMessage());
        }
            
        wizard.getDefinition().setFile(source.getFile());
        return wizard.getDefinition();
    }

    public void destroy() {
    	source = null;
    	wizard = null;
    	if (settings != null) settings.clear();
    	settings = null;
    }

    public String getHelpText() {
        return DcResources.getText("msgImportSettings");
    }

    private void build() {
        setLayout(Layout.getGBL());

        //**********************************************************
        //Create Import Panel
        //**********************************************************
        source = ComponentFactory.getFileField(false, false);
        add(source, Layout.getGBC( 0, 0, 2, 1, 1.0, 1.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
            new Insets( 0, 5, 5, 5), 0, 0));
    }
}
