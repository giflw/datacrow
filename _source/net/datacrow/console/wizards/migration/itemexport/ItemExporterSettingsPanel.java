package net.datacrow.console.wizards.migration.itemexport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import net.datacrow.console.Layout;
import net.datacrow.console.windows.reporting.ReportSettingsPanel;
import net.datacrow.console.wizards.WizardException;

public class ItemExporterSettingsPanel extends ItemExporterWizardPanel {

    private ReportSettingsPanel settingsPanel = new ReportSettingsPanel();
    
    public ItemExporterSettingsPanel(ItemExporterWizard wizard) {
        super(wizard);
        build();
    }
    
    public Object apply() throws WizardException {
        if (definition != null && definition.getSettings() != null)
            settingsPanel.saveSettings(definition.getSettings(), false);
        
        return definition;
    }

    @Override
    public void onActivation() {
        if (definition != null && definition.getSettings() != null)
            settingsPanel.applySettings(definition.getSettings());
    }

    public String getHelpText() {
        return null;
    }

    private void build() {
        setLayout(Layout.getGBL());
        add(settingsPanel,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                      ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                       new Insets( 5, 5, 5, 5), 0, 0));
    }
}
