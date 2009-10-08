/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
 *                                                                            *
 *                       This file is part of Data Crow.                      *
 *       Data Crow is free software; you can redistribute it and/or           *
 *        modify it under the terms of the GNU General Public                 *
 *       License as published by the Free Software Foundation; either         *
 *              version 3 of the License, or any later version.               *
 *                                                                            *
 *        Data Crow is distributed in the hope that it will be useful,        *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *           MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.             *
 *           See the GNU General Public License for more details.             *
 *                                                                            *
 *        You should have received a copy of the GNU General Public           *
 *  License along with this program. If not, see http://www.gnu.org/licenses  *
 *                                                                            *
 ******************************************************************************/

package net.datacrow.console.wizards.module.exchange;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.settings.SettingsPanel;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.DcRepository;
import net.datacrow.core.settings.Setting;
import net.datacrow.core.settings.SettingsGroup;

public class PanelExportConfiguration extends ModuleExportWizardPanel {

	private static final String _EXPORT_RELATED_MODULES = "export_related_modules";
	private static final String _EXPORT_DATA_RELATED_MODULES = "export_data_related_modules";
	private static final String _EXPORT_DATA_MAIN_MODULE = "export_data_related_modules";
	private static final String _PATH = "export_path";
	
    public PanelExportConfiguration(Wizard wizard, boolean exists) {
        build();
    }
    
    public Object apply() throws WizardException {
        
        return null;
    }

    @Override
    public String getHelpText() {
    	// TODO: Add text here.
        return "";//DcResources.getText("msgBasicModuleInfo");
    }
    
    public void destroy() {}    
    
    private void build() {
        setLayout(Layout.getGBL());
        
        SettingsGroup group = new SettingsGroup("", "");
        
        group.add(
        		new Setting(DcRepository.ValueTypes._BOOLEAN,
        		PanelExportConfiguration._EXPORT_DATA_MAIN_MODULE,
                Boolean.FALSE,
                ComponentFactory._CHECKBOX,
                "",
                "lblRetrieveFeatureListing",
                false,
                false));
        group.add(
        		new Setting(DcRepository.ValueTypes._BOOLEAN,
        		PanelExportConfiguration._EXPORT_RELATED_MODULES,
                Boolean.FALSE,
                ComponentFactory._CHECKBOX,
                "",
                "lblRetrieveFeatureListing",
                false,
                false));
        group.add(
        		new Setting(DcRepository.ValueTypes._BOOLEAN,
        		PanelExportConfiguration._EXPORT_DATA_RELATED_MODULES,
                Boolean.FALSE,
                ComponentFactory._CHECKBOX,
                "",
                "lblRetrieveFeatureListing",
                false,
                false));     
        group.add(
        		new Setting(DcRepository.ValueTypes._STRING,
        		PanelExportConfiguration._PATH,
                Boolean.FALSE,
                ComponentFactory._FILEFIELD,
                "",
                "lblRetrieveFeatureListing",
                false,
                false));         
        
        SettingsPanel settingsPanel = new SettingsPanel(group, true);
        settingsPanel.initializeSettings();
        
        add(settingsPanel, Layout.getGBC(1, 4, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets( 5, 5, 5, 5), 0, 0));
    }
}
