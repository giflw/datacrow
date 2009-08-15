package net.datacrow.console.wizards.migration.itemexport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFileField;
import net.datacrow.console.windows.reporting.ReportSettingsPanel;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcFileFilter;

public class ItemExporterSettingsPanel extends ItemExporterWizardPanel {

    private DcFileField target = ComponentFactory.getFileField(true, false);
    private ReportSettingsPanel settingsPanel = new ReportSettingsPanel();
    
    public ItemExporterSettingsPanel(ItemExporterWizard wizard) {
        super(wizard);
        build();
    }
    
    public Object apply() throws WizardException {

        if (target.getFile() == null)
            throw new WizardException(DcResources.getText("msgNoFileSelected"));
        
        if (!target.getFile().exists() || !target.getFile().canRead())
            throw new WizardException(DcResources.getText("msgFileCannotBeUsed"));
        
        definition.setFile(target.getFile());
        settingsPanel.saveSettings(definition.getSettings(), false);
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

    public String getHelpText() {
        return null;
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
