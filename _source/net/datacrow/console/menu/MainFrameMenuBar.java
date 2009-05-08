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

package net.datacrow.console.menu;

import java.util.Collection;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcMenu;
import net.datacrow.console.views.MasterView;
import net.datacrow.core.DataCrow;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcTemplate;
import net.datacrow.core.objects.template.Templates;
import net.datacrow.core.plugin.Plugin;
import net.datacrow.core.plugin.PluginHelper;
import net.datacrow.core.plugin.Plugins;
import net.datacrow.core.resources.DcResources;

public class MainFrameMenuBar extends net.datacrow.console.components.DcMenuBar {

	public static final int _SEARCHPANEL = 0;
    public static final int _INSERTPANEL = 1;
    public static final int _NOTEPANEL = 2;
    
    private final DcModule module;

    public MainFrameMenuBar(DcModule module) {
        this.module = module;
    	build();
    }
    
    private void build() {
        DcMenu menuAdministration = new AdministrationMenu(module);
        DcMenu menuTools = ComponentFactory.getMenu(DcResources.getText("lblTools"));
        DcMenu menuModules = ComponentFactory.getMenu(DcResources.getText("lblModules"));
        DcMenu menuSettings = ComponentFactory.getMenu(DcResources.getText("lblSettings"));
        DcMenu menuFile = ComponentFactory.getMenu(DcResources.getText("lblFile"));
        DcMenu menuFilter = ComponentFactory.getMenu(DcResources.getText("lblFilter"));
        DcMenu menuPlugins = ComponentFactory.getMenu(DcResources.getText("lblPlugins"));
        DcMenu menuUser = ComponentFactory.getMenu(DcResources.getText("lblUser"));
        DcMenu menuXp = ComponentFactory.getMenu(DcResources.getText("lblXpMode"));
        DcMenu menuWebServer = ComponentFactory.getMenu(DcResources.getText("lblWebServer"));
        DcMenu menuInformation = ComponentFactory.getMenu(DcResources.getText("lblHelp"));
        
        // webserver menu
        if (DataCrow.isWebModuleInstalled())
            PluginHelper.add(menuWebServer, "WebServer");
        
        // xp menu
        PluginHelper.add(menuXp, "BeginnerMode");
        PluginHelper.add(menuXp, "ExpertMode");
        
        // view menu
        DcMenu menuView = ComponentFactory.getMenu(DcResources.getText("lblView"));
        if (module.getSearchView().get(MasterView._TABLE_VIEW) != null)
            PluginHelper.add(menuView, "ChangeView", null, null, null, MasterView._TABLE_VIEW, -1);
        
        if (module.getSearchView().get(MasterView._LIST_VIEW) != null)
            PluginHelper.add(menuView, "ChangeView", null, null, null, MasterView._LIST_VIEW, -1);
        
        menuView.addSeparator();
        PluginHelper.add(menuView, "ToggleQuickFilterBar");
        PluginHelper.add(menuView, "ToggleModuleList");
        PluginHelper.add(menuView, "ToggleQuickView");
        PluginHelper.add(menuView, "ToggleGroupingPane");
        PluginHelper.add(menuView, "ToggleToolbarLabels");
        
        // modules menu
        DcMenu subMenuModule = ComponentFactory.getMenu(DcResources.getText("lblActiveModule"));
        for (DcModule module : DcModules.getModules()) {
            if (module.isSelectableInUI())
                PluginHelper.add(subMenuModule, "OpenModule", module.getIndex());
        }
        
        PluginHelper.add(menuModules, "CreateModuleWizard");
        PluginHelper.add(menuModules, "AlterModuleWizard");
        PluginHelper.add(menuModules, "RelateModuleWizard");
        PluginHelper.add(menuModules, "DeleteModuleWizard");    
        
        menuModules.addSeparator();
        menuModules.add(subMenuModule);
        
        // information menu
        PluginHelper.add(menuInformation, "Help");
        PluginHelper.add(menuInformation, "TipOfTheDay");
        menuInformation.addSeparator();
        PluginHelper.add(menuInformation, "About");
        PluginHelper.add(menuInformation, "Donate");
        
        // filter menu
        PluginHelper.add(menuFilter, "Filter");
        PluginHelper.add(menuFilter, "ApplyFilter");
        menuFilter.addSeparator();
        PluginHelper.add(menuFilter, "UndoFilter");
        
        // tools menu
        
        if (module.getImporterClass() != null)
            PluginHelper.add(menuTools, "FileImport");
        
        if (module.deliversOnlineService()) {
            menuTools.addSeparator();

            if (module.getIndex() == DcModules._AUDIOCD)
            	PluginHelper.add(menuTools, "RetrieveAudioCDInformation");

            PluginHelper.add(menuTools, "OnlineSearch");
            menuTools.addSeparator();
        }
        
        if (module.getSynchronizer() != null) {
            PluginHelper.add(menuTools, "MassUpdate");
            menuTools.addSeparator();
        }   
        
        // file menu
        DcMenu menuCreateNew = ComponentFactory.getMenu(IconLibrary._icoAdd, 
                                DcResources.getText("lblNewItem", module.getObjectName()));
        boolean templatesPresent = false;
        if (module.getTemplateModule() != null) {
            Templates.refresh();
            for (DcTemplate template : Templates.getTemplates(module.getTemplateModule().getIndex())) {
                templatesPresent = true;
                PluginHelper.add(menuCreateNew, "CreateNew", null, null, template, -1, module.getIndex());
            }
        }        
        
        PluginHelper.add(menuFile, "NewItemWizard");
        menuFile.addSeparator();
        PluginHelper.add(menuFile, "FileLauncher");
        menuFile.addSeparator();

        if (templatesPresent)
            menuFile.add(menuCreateNew);
        
        PluginHelper.add(menuFile, "CreateNew", module.getIndex());
        PluginHelper.add(menuFile, "OpenItem");
        PluginHelper.add(menuFile, "SaveAll");
        PluginHelper.add(menuFile, "Delete", module.getIndex());
        menuFile.addSeparator();
        PluginHelper.add(menuFile, "Log");
        PluginHelper.add(menuFile, "Exit");

        // settings menu
        PluginHelper.add(menuSettings, "Settings");
        PluginHelper.add(menuSettings, "FieldSettings");
        menuSettings.addSeparator();
        
        if (!module.isAbstract())
            PluginHelper.add(menuSettings, "QuickViewSettings");
        
        PluginHelper.add(menuSettings, "ViewSettings");
        
        if (!module.isAbstract()) {
            if (DataCrow.isWebModuleInstalled())
                PluginHelper.add(menuSettings, "WebFieldSettings");
        }
        
        PluginHelper.add(menuSettings, "ItemFormSettings");
        menuSettings.addSeparator();
        PluginHelper.add(menuSettings, "ChangeLookAndFeel");
        menuSettings.addSeparator();
        PluginHelper.add(menuSettings, "ResourceEditor");

        // tools menu
        PluginHelper.add(menuTools, "Report");
        PluginHelper.add(menuTools, "Migration");
        
        if (!module.isAbstract()) {
            menuTools.addSeparator();
            PluginHelper.add(menuTools, "AutoIncrementer");
            PluginHelper.add(menuTools, "TitleRewriter");
        }
        
        Collection<Plugin> plugins = Plugins.getInstance().getUserPlugins(null, -1, module.getIndex());
        for (Plugin plugin : plugins) {
            if (plugin.isShowInMenu())
                menuPlugins.add(ComponentFactory.getMenuItem(plugin));
        }
        
        menuTools.addSeparator();
        PluginHelper.add(menuTools, "BackupAndRestore");
        menuTools.addSeparator();
        PluginHelper.add(menuTools, "ExpertUser");
        PluginHelper.add(menuTools, "DriveManager");
        
        if (module.isFileBacked())
            PluginHelper.add(menuTools, "FileRenamer", module.getIndex());
        else if (module.getChild() != null && module.getChild().isFileBacked())
            PluginHelper.add(menuTools, "FileRenamer",module.getChild().getIndex());
        
        // item information menu
        menuAdministration.addSeparator();
        PluginHelper.add(menuAdministration, "LoanInformation");
        
        // user menu
        PluginHelper.add(menuUser, "ChangePassword");
        
        menuWebServer.setEnabled(menuWebServer.getItemCount() > 0);
        menuFile.setEnabled(menuFile.getItemCount() > 0);
        menuModules.setEnabled(menuModules.getItemCount() > 0);
        menuView.setEnabled(menuView.getItemCount() > 0);
        menuFilter.setEnabled(menuFilter.getItemCount() > 0);
        menuSettings.setEnabled(menuSettings.getItemCount() > 0);
        menuTools.setEnabled(menuTools.getItemCount() > 0);
        menuInformation.setEnabled(menuInformation.getItemCount() > 0);
        
        if (menuFile.isEnabled())
            this.add(menuFile);
        
        if (menuAdministration.isEnabled())
            this.add(menuAdministration);
        
        this.add(menuXp);
        
        if (menuModules.isEnabled())
            this.add(menuModules);
        
        if (menuView.isEnabled())
            this.add(menuView);
        
        if (menuFilter.isEnabled())
            this.add(menuFilter);

        if (menuSettings.isEnabled())
            this.add(menuSettings);
        
        if (menuUser.isEnabled())
            this.add(menuUser);
        
        if (menuTools.isEnabled())
            this.add(menuTools);
        
        if (plugins.size() > 0)
            this.add(menuPlugins);
        
        if (menuWebServer.isEnabled())
            this.add(menuWebServer);
        
        this.add(menuInformation);
    }
}
