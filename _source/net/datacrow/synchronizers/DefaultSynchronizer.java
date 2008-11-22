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

package net.datacrow.synchronizers;

import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.console.views.View;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.OnlineSearchHelper;

import org.apache.log4j.Logger;

public abstract class DefaultSynchronizer extends Synchronizer {

    private static Logger logger = Logger.getLogger(DefaultSynchronizer.class.getName());
    
    public DefaultSynchronizer(String title, int module) {
        super(title, module);
    }
    
    @Override
    public boolean canParseFiles() {
        return false;
    }

    @Override
    public boolean canUseOnlineServices() {
        return true;
    }
    
    protected boolean exactSearch(DcObject dco) {
        try {
            DcObject dcoNew = dco.getModule().getOnlineServices().query(dco);
            if (dcoNew != null) {
                dco.copy(dcoNew, true);
                dcoNew.unload();
                return true;
            }
        } catch (Exception e) {
            logger.error("Error while retrieving exact match for " + dco, e);
        }        
        return false;
    }    
    
    @Override
    public Thread getTask() {
        return new Task();
    }    
    
    protected void update(DcObject dco, DcObject result, OnlineSearchHelper osh) {
        if (result == null) return;

        DcObject result2 = osh.query(result);
        dco.copy(result2, true);
        result.unload();
        result2.unload();
    }
    
    private class Task extends Thread {
        
        @Override
        public void run() {
            try {
                initialize();
                
                View view = DcModules.getCurrent().getCurrentSearchView();
                
                Collection<DcObject> objects = new ArrayList<DcObject>();
                objects.addAll(dlg.getItemPickMode() == _ALL ? view.getItems() : view.getSelectedItems());                
                initProgressBar(objects.size());
                
                for (DcObject dco : objects) {
                    if (isCancelled()) break;
                    
                    boolean updated = false;
                    
                    addMessage(DcResources.getText("msgSearchingOnlineFor", "" + dco));
                    updated = onlineUpdate(dco, getServer(), getRegion(), getSearchMode());
                    updateProgressBar();
                    
                    try {
                        if (updated) {
                            dco.setSilent(true);
                            dco.saveUpdate(true);
                            
                            while (DatabaseManager.getQueueSize() > 0) {
                                try {
                                    sleep(100);
                                } catch (Exception exp) {}
                            }
                        }
                    } catch (ValidationException ve) {
                        addMessage(ve.getMessage());
                    }
                }
            } finally {
                addMessage(DcResources.getText("msgSynchronizerEnded"));
                enableAction(true);
            }  
        }
    }
}
