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
import net.datacrow.console.components.DcPopupMenu;
import net.datacrow.console.views.View;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.DcPropertyModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.plugin.Plugin;
import net.datacrow.core.plugin.PluginHelper;
import net.datacrow.core.plugin.Plugins;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.fileimporters.FileImporter;
import net.datacrow.settings.definitions.DcFieldDefinition;

public class ViewPopupMenu extends DcPopupMenu {

    public ViewPopupMenu(DcObject dco, int viewIdx) {
        DcModule current = DcModules.getCurrent();
        
        DcModule module = dco.getModule();
        if (viewIdx == View._TYPE_SEARCH && 
            !module.isChildModule() &&
            !(current.getIndex() == DcModules._CONTAINER && 
              dco.getModule().getIndex() != DcModules._CONTAINER)) {

            if (module.isAbstract())
                PluginHelper.add(this, "SaveSelected");
            
            PluginHelper.add(this, "OpenItem");
            PluginHelper.add(this, "EditAsNew", null, dco, null, -1, module.getIndex());
        }
        
        if (viewIdx == View._TYPE_SEARCH) { 
            if (dco.getModule().getParent() != null) {
                // in case a child is selected, make sure its the child which is going to be deleted
                // and not the parent (via the DcModules.getCurrent(), which returns the parent).
                PluginHelper.add(this, "Delete", module.getIndex());
            } else if ((current.getIndex() == DcModules._CONTAINER && dco.getModule().getIndex() != DcModules._CONTAINER)) {
                PluginHelper.add(this, "Delete", DcModules._ITEM);                
            } else {
                // make sure the actual SELECTED module is used for deleting the item. otherwise, if
                // the media module is selected, the item from the, for example, software module view
                // is deleted.
                PluginHelper.add(this, "Delete", DcModules.getCurrent().getIndex());
            }
        } else {
            PluginHelper.add(this, "RemoveRow", DcModules.getCurrent().getIndex());
            PluginHelper.add(this, "AddRow", DcModules.getCurrent().getIndex());
        }   
        
        if (viewIdx == View._TYPE_SEARCH && 
            module.getIndex() == DcModules._USER &&
            SecurityCentre.getInstance().getUser().isAuthorized("SetPassword")) {

            addSeparator();
            PluginHelper.add(this, "SetPassword", "", dco, null, viewIdx, DcModules.getCurrent().getIndex());
        }
        
        addSeparator();
        PluginHelper.add(this, "Sort");
        
        if (	viewIdx == View._TYPE_SEARCH && 
        		module.canBeLended() &&
        		SecurityCentre.getInstance().getUser().isAuthorized("Loan")) {
        	
            addSeparator();
            PluginHelper.add(this, "Loan");
        }

        addSeparator();
        
        for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
            DcField field = module.getField(definition.getIndex());
            DcPropertyModule pm = DcModules.getPropertyModule(field);
            if (pm != null) PluginHelper.add(this, "ManageItem", pm.getIndex());
        }
        
        addSeparator();
        PluginHelper.add(this, "ViewSettings");
        
        if (viewIdx == View._TYPE_SEARCH) {
            FileImporter importer = module.getImporter();
            if (importer != null && importer.allowReparsing() && module.getDcObject().getFileField() != null) { 
                addSeparator();
                PluginHelper.add(this, "AttachFileInfo");
            }
        }
        
        addSeparator();
        
        if (current.getIndex() != DcModules._ITEM)
            PluginHelper.add(this, "UpdateAll", module.getIndex());
        
        PluginHelper.add(this, "FileLauncher", module.getIndex());
        
        addSeparator();
        PluginHelper.add(this, "Report");
        
        Collection<Plugin> plugins = Plugins.getInstance().getUserPlugins(dco, viewIdx, module.getIndex());
        for (Plugin plugin : plugins) {
            if (plugin.isShowInPopupMenu()) {
                addSeparator();
                add(ComponentFactory.getMenuItem(plugin));
            }
        }
    }
}
