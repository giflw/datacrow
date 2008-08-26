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

package net.datacrow.console.components.lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import net.datacrow.console.windows.security.ModulePermissionPanel;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.DcPropertyModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.security.SecurityCentre;

import org.apache.log4j.Logger;

public class SecurityModuleList extends DcModuleList {
	
	private static Logger logger = Logger.getLogger(SecurityModuleList.class.getName());
	
	private ModulePermissionPanel modulePermissionPanel;
	
	public SecurityModuleList(ModulePermissionPanel modulePermissionPanel) {
		this.modulePermissionPanel = modulePermissionPanel;
	}
	
	@Override
	public void clear() {
	    super.clear();
		modulePermissionPanel = null;
	}
	
	private void setSelected() {
        listenForChanges = false;
        Object item = getSelectedValue();
        if (item instanceof ModulePanel) {
            int module = ((ModulePanel) item).getModule();
            if (module != currentIndex) {
                currentIndex = module;
                modulePermissionPanel.setSelected(currentIndex);
                setSelectedModule(currentIndex);
            }
        }
        listenForChanges = true;		
	}
	
	@Override
    public void valueChanged(ListSelectionEvent lse) {
        if (listenForChanges) setSelected();
    }
    
    @Override
	public void addModules() {
        if (elements != null) elements.clear();
        
        Collection<DcModule> managedModules = SecurityCentre.getInstance().getManagedModules();
        for (DcModule module : managedModules) {
            try {
            	if (module.isSelectableInUI()) {
            		List<ModulePanel> c = new ArrayList<ModulePanel>();
	                c.add(new ModulePanel(module, ModulePanel._ICON32));
	                
	                for (DcField field : module.getFields()) {
	                    DcModule referencedMod = DcModules.getReferencedModule(field);
                    	if (    managedModules.contains(referencedMod) &&
                    			referencedMod.isEnabled() &&
                        		referencedMod.getIndex() != module.getIndex() && 
                              !(referencedMod instanceof DcPropertyModule) &&
                                referencedMod.getIndex() != DcModules._CONTACTPERSON &&
                                referencedMod.getIndex() != DcModules._CONTAINER) {
	                    	
	                        c.add(new ModulePanel(referencedMod, ModulePanel._ICON16));
                    	}
	                }
	                
	                elements.put(module.getIndex(), c);
            	}
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
        
        for (DcModule module : SecurityCentre.getInstance().getManagedModules()) {
            setModules(module.getIndex());
        	break;
        }
	}
}
