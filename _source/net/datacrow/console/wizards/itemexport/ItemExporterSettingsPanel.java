package net.datacrow.console.wizards.itemexport;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFileField;
import net.datacrow.console.windows.reporting.ReportSettingsPanel;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.Utilities;
import net.datacrow.util.filefilters.DcFileFilter;

public class ItemExporterSettingsPanel extends ItemExporterWizardPanel {

    private DcFileField target = ComponentFactory.getFileField(true, false);
    private ReportSettingsPanel settingsPanel = new ReportSettingsPanel();
    
    public ItemExporterSettingsPanel(ItemExporterWizard wizard) {
        super(wizard);
        build();
    }
    
    @Override
    public Object apply() throws WizardException {

        if (target.getFile() == null || Utilities.isEmpty(target.getFilename()))
            throw new WizardException(DcResources.getText("msgNoFileSelected"));
        
        String filename = target.getFilename();
        filename = filename.endsWith(definition.getExporter().getFileType()) ? filename : filename + "." + definition.getExporter().getFileType();
        
        target.getFile().delete();
        File file = new File(filename);
        
        try {
            file.createNewFile();
        
            definition.setFile(file);
            settingsPanel.saveSettings(definition.getSettings(), false);
        } catch (IOException ie) {
            throw new WizardException(DcResources.getText("msgFileCannotBeUsed"));
        }
            
        return definition;
    }

    @Override
    public void onActivation() {
        if (definition != null && definition.getExporter() != null) {
            settingsPanel.applySettings(definition.getSettings());
            target.setFileFilter(new DcFileFilter(definition.getExporter().getFileType()));
            target.setFile(definition.getFile());
        }
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgExportSettings");
    }

    private void build() {
        setLayout(Layout.getGBL());
        add(target,         Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
            new Insets( 0, 5, 5, 5), 0, 0));
        add(settingsPanel,  Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
    }
}
