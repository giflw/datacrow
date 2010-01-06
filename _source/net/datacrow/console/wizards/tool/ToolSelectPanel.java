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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.console.Layout;
import net.datacrow.console.components.DcPluginField;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.plugin.InvalidPluginException;
import net.datacrow.core.plugin.Plugin;
import net.datacrow.core.plugin.Plugins;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.security.SecurityCentre;

import org.apache.log4j.Logger;

public class ToolSelectPanel extends ToolSelectBasePanel {

    private static Logger logger = Logger.getLogger(ToolSelectPanel.class.getName());
    
    public ToolSelectPanel(Wizard wizard) {
        super(wizard);
        build();
    }

    public String getHelpText() {
        return DcResources.getText("msgSelectTheToolOfYourChoice");
    }
    
    @Override
    public void onActivation() {
        super.onActivation();

        removeAll();
        build();
        revalidate();
        repaint();
        
        getWizard().repaint();
    }

    public Object apply() throws WizardException {
        Tool tool = getTool();
        return tool;
    }

    @Override
    public void destroy() {
        super.destroy();
    }  
    
    private void addPlugin(Collection<Plugin> plugins, String key) {
        try {
            plugins.add(Plugins.getInstance().get(key, getTool().getModule()));
        } catch (InvalidPluginException ipe) {
            logger.error(ipe, ipe);
        }
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        Tool tool = getTool();
        DcModule module = DcModules.get(tool.getModule());
        
        if (module == null) return;
        
        int y = 0;
        int x = 0;

        Collection<Plugin> plugins = new ArrayList<Plugin>();
        addPlugin(plugins, "NewItemWizard");
        addPlugin(plugins, "CreateNew");
        
        if (module.getImporterClass() != null)
            addPlugin(plugins, "FileImport");
        
        if (module.deliversOnlineService()) {
            addPlugin(plugins, "OnlineSearch");
            addPlugin(plugins, "MassUpdate");
        }
        
        for (Plugin plugin : plugins) {
            if (SecurityCentre.getInstance().getUser().isAuthorized(plugin)) {
                DcPluginField fld = new DcPluginField(plugin);
                add(fld, Layout.getGBC( x, y++, 1, 1, 1.0, 1.0
                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                    new Insets( 0, 5, 5, 5), 0, 0));
            }
        }
    }
}
