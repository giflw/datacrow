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

package net.datacrow.core.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.util.StringUtils;

import org.apache.log4j.Logger;

public class OnlineSearchHelper implements IOnlineSearchClient {
    
    private static Logger logger = Logger.getLogger(OnlineSearchHelper.class.getName());
    
    private final int module;
    
    private SearchTask task;
    
    private int maximum = 2;
    
    private IServer server;
    private Region region;
    private SearchMode mode;
    private int itemMode;
    
    private Collection<DcObject> result = new ArrayList<DcObject>();
    
    public OnlineSearchHelper(int module, int itemMode) {
        this.module = module;
        this.itemMode = itemMode;
    }
    
    public void setServer(IServer server) {
        this.server = server;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void setMode(SearchMode mode) {
        this.mode = mode;
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }
    
    public DcObject query(DcObject item) {
        IServer server = getServer();
        Region region = getRegion(server);
        
        task = server.getSearchTask(this, getSearchMode(server), region, null);
        task.setItemMode(SearchTask._ITEM_MODE_FULL);
        
        try {
            return task.getItem(new URL((String) item.getValue(DcObject._SYS_SERVICEURL)));
        } catch (Exception e) {
            logger.error(e, e);
            return item;
        }
    }
    
    
    public DcObject query(DcObject base, String query, int[] matcherFieldIdx) {
        IServer server = getServer();
        Region region = getRegion(server);
        
        task = server.getSearchTask(this, getSearchMode(server), region, query);
        task.setItemMode(itemMode);
        task.setMaximum(maximum);
        task.run();
        
        return getMatchingItem(base, matcherFieldIdx);
    }
    
    public Collection<DcObject> query(String query) {
        IServer server = getServer();
        Region region = getRegion(server);
        
        task = server.getSearchTask(this, getSearchMode(server), region, query);
        task.setItemMode(itemMode);
        task.setMaximum(maximum);
        task.run();
        
        return new ArrayList<DcObject>(result);
    }    

    private IServer getServer() {
        OnlineService os = DcModules.get(module).getOnlineService();
        IServer defaultSrv = 
            os.getServer(DcModules.get(module).getSettings().getString(DcRepository.ModuleSettings.stOnlineSearchDefaultServer));
        
        IServer server = this.server != null ? this.server : defaultSrv;
        return server == null ? (IServer) os.getServers().toArray()[0] : server;
    }

    private Region getRegion(IServer server) {
        OnlineService os = DcModules.get(module).getOnlineService();
        Region region = this.region != null ? this.region : os.getDefaultRegion();
        if (region != null) {
            boolean partOfServer = false;
            
            for (Region serverRegion : server.getRegions()) {
                if (serverRegion.getCode().equals(region.getCode())) {
                    region = serverRegion;
                    partOfServer = true;
                }
            }
            if (!partOfServer) region = null;
        } 
            
        return region == null && server.getRegions().size() > 0 ? (Region) server.getRegions().toArray()[0] : region;
    }
    
    private SearchMode getSearchMode(IServer server) {
        OnlineService os = DcModules.get(module).getOnlineService();
        SearchMode mode = this.mode == null ? os.getDefaultSearchMode() : this.mode;
        
        if (server.getSearchModes() == null) 
            return null;
        
        if (mode != null) {
            boolean partOfServer = false;
            for (SearchMode serverMode : server.getSearchModes()) {
                if (serverMode.getDisplayName().equals(mode.getDisplayName())) {
                    mode = serverMode;
                    partOfServer = true;
                }
            }
            if (!partOfServer) mode = null;
        } 
        
        if (mode == null && server.getSearchModes() != null) {
            for (SearchMode serverMode : server.getSearchModes())
                mode = serverMode.keywordSearch() ? serverMode : mode;
        }
            
        return mode;
    }
    
    private DcObject getMatchingItem(DcObject base, int[] matcherFieldIdx) {
        for (DcObject dco : result) {
            boolean match = true;
            for (int i = 0; i < matcherFieldIdx.length; i++) {
                Object o1 = base.getValue(matcherFieldIdx[i]);
                Object o2 = dco.getValue(matcherFieldIdx[i]);
                
                String value1 = o1 == null || o1.toString().equals("-1") ? "" : StringUtils.normalize(o1.toString().trim());
                String value2 = o2 == null || o2.toString().equals("-1") ? "" : StringUtils.normalize(o2.toString().trim());
                
                match &= value1.equals(value2);
            }
            
            if (match) return dco;
        }
        
        return null;
    }
    
    public void clear(DcObject except) {
        for (DcObject dco : result)
            if (dco != except) dco.unload();
        
        task = null;
        server = null;
        region = null;
        result.clear();
        result = null;
    }
    
    public void addError(Throwable t) {
        logger.error(t, t);
    }

    public void addError(String message) {
        logger.error(message);
    }

    public void addMessage(String message) {}

    public void addObject(DcObject dco) {
        if (result != null) {
            result.add(dco);
            if (isPerfectMatch(dco)) 
                task.cancelSearch();
        }
    }
    
    private boolean isPerfectMatch(DcObject dco) {
        String string =  StringUtils.normalize(task.getQuery()).toLowerCase();
        String item = StringUtils.normalize(dco.toString()).toLowerCase();
        return string.equals(item);
    }

    public void addWarning(String warning) {}

    public DcObject getDcObject() {
        return null;
    }

    public DcModule getModule() {
        return DcModules.get(module);
    }

    public void processed(int i) {}

    public void processing() {}

    public void processingTotal(int i) {}

    public int resultCount() {
        return result.size();
    }

    public void stopped() {}
}
