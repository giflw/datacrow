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

package net.datacrow.util;

import java.util.Collection;

import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

public class DataTask extends Thread {
    
    private DcModule module;

    private boolean isRunning = false;
    private boolean keepOnRunning = true;

    protected Collection<? extends DcObject> items;

    public DataTask() {
        setPriority(Thread.NORM_PRIORITY);
    }
    
    public DataTask(DcModule module, Collection<? extends DcObject> items) {
        this.items = items;
        this.module = module;
    }
    
    public void setItems(Collection<? extends DcObject> items) {
        this.items = items;
    }
    
    public void setModule(DcModule module) {
        this.module = module;
    }

    public void startTask() {
        isRunning = true;
        keepOnRunning = true;
        if (module != null && module.hasSearchView()) {
            module.getSearchView().setBusy(true);
            
            // make sure that quick view is not updated will referenced items are saved
            for (DcModule referencedModule : DcModules.getReferencedModules(module.getIndex())) {
                if (referencedModule.hasSearchView())
                    module.getSearchView().setBusy(true);
            }
        }
    }
    
    public void endTask() {
        if (module != null && module.hasSearchView()) {
            module.getSearchView().setBusy(false);
            
            for (DcModule referencedModule : DcModules.getReferencedModules(module.getIndex())) {
                if (referencedModule.hasSearchView())
                    module.getSearchView().setBusy(false);
            }
        }
        
        keepOnRunning = false;
        isRunning = false;
        
        if (items != null) {
            items.clear();
            items = null;
        }
        
        module = null;
    }

    public void cancel() {
    	keepOnRunning = false;
    }

    public boolean isRunning() {
    	return isRunning;
    }

    public boolean keepOnRunning() {
    	return keepOnRunning;
    }
}
