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

package net.datacrow.console.wizards.module;

import java.util.ArrayList;
import java.util.List;

import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.wizards.IWizardPanel;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.DcPropertyModule;
import net.datacrow.core.modules.ModuleJar;
import net.datacrow.core.modules.xml.XmlField;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class CreateModuleWizard extends Wizard {

    public CreateModuleWizard() {
        super();
        
        setTitle(DcResources.getText("lblModuleCreateWizard"));
        setHelpIndex("dc.modules.create");

        setSize(DcSettings.getDimension(DcRepository.Settings.stModuleWizardFormSize));
        setCenteredLocation();
    }
    
    @Override
    protected void initialize() {
    }
    
    @Override
    protected List<IWizardPanel> getWizardPanels() {
        List<IWizardPanel> panels = new ArrayList<IWizardPanel>();
        panels.add(new PanelModuleType(this));
        panels.add(new PanelBasicInfo(this, false));
        panels.add(new PanelFields(this, false));
        return panels;
    }

    @Override
    protected void saveSettings() {
        DcSettings.set(DcRepository.Settings.stModuleWizardFormSize, getSize());
    }

    @Override
    public void finish() throws WizardException {
        XmlModule module = (XmlModule) getCurrent().apply();
        module.setIndex(DcModules.getAvailableIdx(module));
        module.setProductVersion(DataCrow.getVersion().getFullString());

        try {
            if (module.getModuleClass().equals(DcPropertyModule.class))
                module.setServingMultipleModules(true);
            
            new ModuleJar(module).save();
            
            for (XmlField field : module.getFields()) {
                if (field.getModuleReference() !=  0 && field.getModuleReference() != module.getIndex()) {
                    DcModule m = DcModules.get(field.getModuleReference()) == null ? 
                                 DcModules.get(field.getModuleReference() + module.getIndex()) : 
                                 DcModules.get(field.getModuleReference());
                    
                    if (m != null && m.getXmlModule() != null)
                        new ModuleJar(m.getXmlModule()).save();
                }
            }
            
            if (module.getModuleClass().equals(DcPropertyModule.class)) {
                DcModules.registerBasePropertyModule((DcPropertyModule) DcModules.convert(module));
            } else {
                DcModules.register(DcModules.convert(module));
                DcModules.registerPropertyModules(DcModules.convert(module));
            }
            
            close();
        } catch (Exception e) {
            throw new WizardException(DcResources.getText("msgCouldNotWriteModuleFile", e.getMessage()));
        }
    }

    @Override
    public void next() {
        try {
            XmlModule module = (XmlModule) getCurrent().apply();

            if (module == null)
                return;
            
            current += 1;
            if (current <= getStepCount()) {
                for (int i = 0; i < getStepCount(); i++) {
                    ModuleWizardPanel panel = (ModuleWizardPanel) getWizardPanel(i);
                    panel.setModule(module);
                    panel.setVisible(i == current);
                }
            } else {
                current -= 1;
            }

            applyPanel();
        } catch (WizardException wzexp) {
            if (wzexp.getMessage().length() > 1)
                new MessageBox(wzexp.getMessage(), MessageBox._WARNING);
        }
    }

    @Override
    public void close() {
        if (!isCancelled() && !isRestarted())
            new RestartDataCrowDialog(this);
        
        super.close();
    }

    @Override
    protected String getWizardName() {
        return DcResources.getText("msgModuleWizard",
                                   new String[] {String.valueOf(current + 1), String.valueOf(getStepCount())});
    }

    @Override
    protected void restart() {
        try {
            finish();
            saveSettings();
            CreateModuleWizard wizard = new CreateModuleWizard();
            wizard.setVisible(true);
        } catch (WizardException exp) {
            new MessageBox(exp.getMessage(), MessageBox._WARNING);
        }
    }
}
