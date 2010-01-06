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

package net.datacrow.console.wizards.tool;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import net.datacrow.console.wizards.IWizardPanel;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.DcRepository;
import net.datacrow.core.plugin.Plugin;
import net.datacrow.core.plugin.Plugins;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class ToolSelectWizard extends Wizard {

    public ToolSelectWizard() {
        super();
        
        setHelpIndex("dc.tools.wizard");
        setSize(DcSettings.getDimension(DcRepository.Settings.stItemWizardFormSize));
        setCenteredLocation();
    }
    
    @Override
    protected List<IWizardPanel> getWizardPanels() {
        List<IWizardPanel> panels = new ArrayList<IWizardPanel>();
        panels.add(new ModuleSelectPanel(this));
        panels.add(new ToolSelectPanel(this));
        return panels;
    }

    @Override
    protected void saveSettings() {
        DcSettings.set(DcRepository.Settings.stToolSelectWizard, getSize());
    }

    @Override
    public void finish() throws WizardException {
        Tool tool = (Tool) getCurrent().apply();
        try {
            Plugin plugin = Plugins.getInstance().get(tool.getPlugin());
            plugin.actionPerformed(new ActionEvent(this, 0, ""));
        } catch (Exception e) {
            throw new WizardException(e.getMessage());
        }
    }

    @Override
    public void next() throws WizardException {
        ToolSelectBasePanel panel = (ToolSelectBasePanel) getCurrent();
        Tool tool = panel.getTool();
        tool = tool == null ? new Tool() : tool;
        
        super.next();
        
        panel = (ToolSelectBasePanel) getCurrent();
        panel.onActivation();
        panel.setTool(tool);
    }

    @Override
    protected String getWizardName() {
        return DcResources.getText("lblToolSelectWizard");
    }

    @Override
    protected void initialize() {}

    @Override
    protected boolean isRestartSupported() {
        return false;
    }
}
