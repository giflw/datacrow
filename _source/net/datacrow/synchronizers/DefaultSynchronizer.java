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
import net.datacrow.core.DcRepository;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcAssociate;
import net.datacrow.core.objects.DcMediaObject;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcProperty;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.OnlineSearchHelper;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.SearchTask;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.util.StringUtils;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public abstract class DefaultSynchronizer extends Synchronizer {

    private static Logger logger = Logger.getLogger(DefaultSynchronizer.class.getName());
    
    protected DcObject dco;
    
    public DefaultSynchronizer(String title, int module) {
        super(title, module);
    }
    
    @Override
    public boolean canParseFiles() {
        return false;
    }
    
    public DcObject getDcObject() {
        return dco;
    }

    protected int getSearchFieldIdx(SearchMode mode) {
        return  mode != null ? mode.getFieldBinding() :
                dco instanceof DcMediaObject ? DcMediaObject._A_TITLE :
                dco instanceof DcProperty ? DcProperty._A_NAME :
                dco instanceof DcAssociate ? DcAssociate._A_NAME :
                dco.getDisplayFieldIdx();
    }
    
    protected String getSearchString(int field, IServer server) {
        return dco.getDisplayString(field);
    }
    
    protected boolean matches(DcObject result, String searchString, int fieldIdx) {
        return StringUtils.equals(searchString, result.getDisplayString(fieldIdx));
    }
    
    @Override
    public boolean onlineUpdate(ISynchronizerClient client, DcObject dco) {
        this.client = client;
        this.dco = dco;

        // use the original service settings
        if (dco.getModule().getSettings().getBoolean(DcRepository.ModuleSettings.stMassUpdateUseOriginalServiceSettings)) {
            return exactSearch(dco);

        } else { 
            boolean updated = false;
            int fieldIdx = getSearchFieldIdx(client.getSearchMode());
            String searchString = getSearchString(fieldIdx, client.getServer());
            if (Utilities.isEmpty(searchString)) return updated;
            
            OnlineSearchHelper osh = new OnlineSearchHelper(dco.getModule().getIndex(), SearchTask._ITEM_MODE_SIMPLE);
            osh.setServer(client.getServer());
            osh.setRegion(client.getRegion());
            osh.setMode(client.getSearchMode());
            osh.setMaximum(2);
            
            Collection<DcObject> results = osh.query(searchString, dco);
            for (DcObject result : results) {
                if (matches(result, searchString, fieldIdx)) {
                    merge(dco, result, osh);
                    updated = true;
                    break;
                }
            }
            return updated;
        }
    }
    
    private boolean exactSearch(DcObject dco) {
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
    public boolean canUseOnlineServices() {
        return true;
    }
    
    @Override
    public Thread getTask() {
        return new Task();
    }    
    
    private class Task extends Thread {
        
        @Override
        public void run() {
            try {
                client.initialize();
                
                View view = DcModules.getCurrent().getCurrentSearchView();
                Collection<DcObject> objects = new ArrayList<DcObject>();
                objects.addAll(client.getItemPickMode() == _ALL ? view.getItems() : view.getSelectedItems());                
                client.initProgressBar(objects.size());
                
                for (DcObject dco : objects) {
                    if (client.isCancelled()) break;
                    
                    boolean updated = false;
                    
                    client.addMessage(DcResources.getText("msgSearchingOnlineFor", "" + dco));
                    
                    updated = parseFiles(dco);
                    updated |= onlineUpdate(client, dco);
                    
                    client.updateProgressBar();
                    
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
                        client.addMessage(ve.getMessage());
                    }
                }
            } finally {
                client.addMessage(DcResources.getText("msgSynchronizerEnded"));
                client.enableActions(true);
            }  
        }
    }
}
