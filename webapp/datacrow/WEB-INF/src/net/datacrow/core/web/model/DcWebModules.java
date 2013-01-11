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

package net.datacrow.core.web.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.DataCrow;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.DcPropertyModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.web.DcSecured;

/**
 * Holder of web modules. 
 */
public class DcWebModules extends DcSecured implements Serializable  {
    
    private static final long serialVersionUID = 8998754917651362294L;
    
    private Collection<DcWebModule> modules = new ArrayList<DcWebModule>();
    
    public DcWebModules() {}
    
    public void load() {
        
        modules.clear();
        
        if (!isLoggedIn()) return;
        
        File dir = new File(DataCrow.webDir, "/datacrow/images/modules/");
        dir.mkdirs();
        String[] files = dir.list();
        if (files != null) {
            for (String file : files)
                new File(DataCrow.webDir, "/datacrow/images/modules/" + file).delete();
        }
        
        for (DcModule module : DcModules.getAllModules()) {
            if (    getUser().isAuthorized(module) &&
                    module.isTopModule() && module.isEnabled() && 
                   !module.isAbstract() &&
                  (!module.hasDependingModules() || 
                    module.getIndex() == DcModules._CONTACTPERSON ||
                    module.getIndex() == DcModules._CONTAINER) &&
                    module.getIndex() != DcModules._USER) {
                
                modules.add(new DcWebModule(module.getIndex(), module.getLabel()));
                
                for (DcField field : module.getFields()) {
                    DcModule referencedMod = DcModules.getReferencedModule(field);
                    if (    getUser().isAuthorized(module) &&
                            referencedMod.isEnabled() &&
                            referencedMod.getIndex() != module.getIndex() && 
                          !(referencedMod instanceof DcPropertyModule) &&
                            referencedMod.getIndex() != DcModules._CONTACTPERSON &&
                            referencedMod.getIndex() != DcModules._CONTAINER) {
                        
                        modules.add(new DcWebModule(referencedMod.getIndex(), referencedMod.getLabel()));
                    }
                }
            }
        }
    }
    
    public Collection<DcWebModule> getModules() {
        return modules;
    }
}
