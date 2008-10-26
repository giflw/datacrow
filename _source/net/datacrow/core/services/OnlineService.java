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

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.console.windows.onlinesearch.OnlineSearchForm;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcMediaObject;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.services.plugin.IServer;

import org.apache.log4j.Logger;

public class OnlineService {

    private static Logger logger = Logger.getLogger(OnlineService.class.getName());
    
    private static final int _SERVICE_NAME = 0;
    private static final int _REGION_CODE = 1;
    private static final int _MODE_NAME = 2;
    private static final int _QUERY = 3;    
    
    private Collection<IServer> servers = new ArrayList<IServer>();
    
    private final int module;
    
    public OnlineService(int module) {
        this.module = module;
    }
    
    public Collection<IServer> getServers() {
        return servers;
    }
    
    public void addServer(IServer server) {
        servers.add(server);
    }
    
    public IServer getServer(DcObject dco) {
        return getServer(getService(dco, _SERVICE_NAME));
    }
    
    public Region getRegion(DcObject dco) {
        return getRegion(getService(dco, _REGION_CODE));
    }

    public SearchMode getMode(DcObject dco) {
        return getMode(getService(dco, _MODE_NAME));
    }
    
    public String getQuery(DcObject dco) {
        return getService(dco, _QUERY);
    }    
    
    public Region getDefaultRegion() {
        String code = DcModules.get(module).getSettings().getString(DcRepository.ModuleSettings.stOnlineSearchDefaultRegion);
        return getRegion(code);
    }
    
    public IServer getDefaultServer() {
        String name = DcModules.get(module).getSettings().getString(DcRepository.ModuleSettings.stOnlineSearchDefaultServer);
        return getServer(name);
    }
    
    public SearchMode getDefaultSearchMode() {
        String name = DcModules.get(module).getSettings().getString(DcRepository.ModuleSettings.stOnlineSearchDefaultMode);
        return getMode(name);       
    }
    
    public IServer getServer(String name) {
        for (IServer server : getServers()) {
            if (server.getName().equals(name) && server.getModule() == module)
                return server;
        }
        
        return null;
    }
    
    private Region getRegion(String code) {
        IServer server = getDefaultServer();
        
        if (server != null && server.getRegions() != null) {
            for (Region region : server.getRegions()) {
                if (region.getCode().equals(code))
                    return region;
            }
        }
        return null;
    }    

    private SearchMode getMode(String diplayName) {
        IServer server = getDefaultServer();
        if (server != null && server.getSearchModes() != null) {
            for (SearchMode mode : server.getSearchModes()) {
                if (mode.getDisplayName().equals(diplayName))
                    return mode;
            }
        }
        return null;        
    }
    
    private String getService(DcObject dco, int level) {
        String s = (String) dco.getValue(DcMediaObject._SYS_SERVICE);
        
        if (s == null) return null;
        
        StringTokenizer st = new StringTokenizer(s, "/");
        String[] values = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreElements())
            values[i++] = ((String) st.nextElement()).trim();
        
        String value = null;
        try {
            value = values[level];
            if (level == _QUERY && value.length() > 0) 
                value = value.substring(value.indexOf("[") + 1, value.lastIndexOf("]"));
        } catch (Exception e) {
            logger.error("Error while trying to get online service information from " + dco, e);
        }
        
        return value;
    }  
    
    public DcObject query(DcObject dco) throws Exception {
        IServer server = getServer(dco);
        Region region = getRegion(dco);
        
        if (server != null) {
            SearchTask task = server.getSearchTask(null, null, region, null);
            return task.query(dco);
        }
        
        return null;
    }    
    
    public OnlineSearchForm getUI(DcObject dco, ItemForm itemForm, boolean advanced) {
        return new OnlineSearchForm(this, dco, itemForm, advanced);
    }
}
