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

package net.datacrow.console.windows.security;

import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.objects.helpers.Permission;
import net.datacrow.core.objects.helpers.User;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.CloseWindowRequest;
import net.datacrow.core.wf.requests.CreateUserRequest;
import net.datacrow.core.wf.requests.UpdateUserRequest;
import net.datacrow.util.DcSwingUtilities;

public class UserForm extends ItemForm {
    
    private PluginPermissionPanel pluginPermissionPanel;
    private ModulePermissionPanel modulePermissionPanel;
    
    public UserForm(DcObject dco, boolean update, boolean template) {
        super(false, update, dco, template);
        
        // needed for saving the permissions
        if (dco.getID() == null)
            dco.setIDs();
        
        addPluginPermissionPanel();
        addModulePermissionPanel();
    }
    
    private void addModulePermissionPanel() {
    	modulePermissionPanel = new ModulePermissionPanel(dco, update);
    	modulePermissionPanel.setEnabled(!((User) dco).isAdmin());
        tabbedPane.addTab(DcResources.getText("lblModulePermission"),  
                          IconLibrary._icoSettings16, modulePermissionPanel);
    }
    
    private void addPluginPermissionPanel() {
        pluginPermissionPanel = new PluginPermissionPanel(dco, update);
        pluginPermissionPanel.setEnabled(!((User) dco).isAdmin());
        tabbedPane.addTab(DcResources.getText("lblPluginPermissions"),  
                          IconLibrary._icoSettings16, pluginPermissionPanel);
    }
    
    @Override
    protected boolean isChanged() {
        boolean changed = super.isChanged();
        changed = changed ? changed : modulePermissionPanel.isChanged();
        changed = changed ? changed : pluginPermissionPanel.isChanged();
        return changed;
    }

    @Override
    public void close(boolean afterSave) {
        super.close(afterSave);
        
        modulePermissionPanel.clear();
        modulePermissionPanel = null;
        pluginPermissionPanel.clear();
        pluginPermissionPanel = null;
    }

    @Override
    protected void addChildrenPanel() {}
    
    @Override
    protected void saveValues() {
        apply();
        
        dco.addRequest(new CloseWindowRequest(this));
        for (Permission permission : modulePermissionPanel.getPermissions())
            dco.addChild(permission);
        
        for (Permission permission : pluginPermissionPanel.getPermissions())
	        dco.addChild(permission);
        
        try {
            if (!update) {
                dco.addRequest(new CreateUserRequest((User) dco));
                dco.saveNew(true);
            } else if (isChanged()) {
                dco.addRequest(new UpdateUserRequest((User) dco));
                dco.saveUpdate(true);
            } else {
                DcSwingUtilities.displayWarningMessage("msgNoChangesToSave");
            }
        } catch (ValidationException vExp) {
            DcSwingUtilities.displayWarningMessage(vExp.getMessage());
        }
    }
}
