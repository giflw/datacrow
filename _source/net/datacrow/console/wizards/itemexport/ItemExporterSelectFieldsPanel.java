package net.datacrow.console.wizards.itemexport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFieldSelectorField;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;

public class ItemExporterSelectFieldsPanel extends ItemExporterWizardPanel {

    private DcFieldSelectorField fldFields;
    
    public ItemExporterSelectFieldsPanel(ItemExporterWizard wizard) {
        super(wizard);
        build();
    }
    
    @Override
    public Object apply() throws WizardException {
        definition.setFields(fldFields.getSelectedFieldIndices());
        wizard.getModule().getSettings().set(DcRepository.ModuleSettings.stExportFields, definition.getFields());
        return definition;
    }

    @Override
    public void onActivation() {
        if (definition != null && definition.getExporter() != null) {
            if (definition.getFields() != null) {
                fldFields.setSelectedFields(definition.getFields());
            } else {
                int[] fields = wizard.getModule().getSettings().getIntArray(DcRepository.ModuleSettings.stExportFields);
                
                if (fields != null)
                    fldFields.setSelectedFields(fields);
                else 
                    fldFields.setSelectedFields(wizard.getModule().getFieldIndices());
            }
        }
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgExportFieldSelect");
    }

    private void build() {
        fldFields = new DcFieldSelectorField(wizard.getModule().getIndex(), true, true);
        setLayout(Layout.getGBL());
        add(fldFields,  Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
             new Insets( 5, 5, 5, 5), 0, 0));
    }
}
