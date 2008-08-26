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

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.util.StringUtils;

public abstract class SearchTask extends Thread {
    
    private boolean isCancelled = false;
    
    protected IOnlineSearchClient listener;
    
    private int maximum = 20;
    
    private String query;
    
    private String address;
    private IServer server;
    private SearchMode mode;
    private Region region;
    
    public SearchTask(IOnlineSearchClient listener, 
                      IServer server,
                      Region region,
                      SearchMode mode,
                      String query) {
        
        this.listener = listener;
        this.region = region;
        this.mode = mode;
        this.server = server;
        this.address = region != null ? region.getUrl() : server.getUrl();
        this.query = StringUtils.normalize(query);        
    }

//    public SearchTask(IOnlineSearchClient listener, String searchString, int max) {
//        this.listener = listener;
//        this.query = StringUtils.normalize(query);
//        this.maximum = max;
//    }

    @Override
    public abstract void run();
    
    public abstract String getWhiteSpaceSubst();
    
    public abstract DcObject query(DcObject dco) throws Exception;
    
    protected void setServiceInfo(DcObject dco) {
        String service =  server.getName() + " / " + 
                         (region != null ? region.getCode() : "none") + " / " +
                         (mode != null ? mode.getDisplayName() : "none") + " / " + 
                          "value=[" + query + "]";
        dco.setValue(DcObject._SYS_SERVICE, service);
    }
    
    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }
    
    public void cancelSearch() {
        isCancelled = true;
    }

    protected void isCancelled(boolean b) {
        this.isCancelled = b;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public SearchMode getMode() {
        return mode;
    }

    public void setMode(SearchMode mode) {
        this.mode = mode;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getQuery() {
        String s = StringUtils.normalize(query); 
            
        s = query.replaceAll(" ", getWhiteSpaceSubst());
        s = s.replaceAll("\n", "");
        s = s.replaceAll("\r", "");
        
        // replace the & character
        int idx = s.indexOf('&');
        while (idx > -1) {
            s = s.substring(0, s.indexOf('&')) + "%26" + s.substring(s.indexOf('&') + 1, s.length());
            idx = s.indexOf('&');
        }
            
        return s;
    }

//    public String getSearchString() {
//        return searchString;
//    }
//    
//    public void setSearchString(String searchString) {
//        this.searchString = searchString;
//    }
//
    public IServer getServer() {
        return server;
    }

    public int getMaximum() {
        return maximum;
    }
}
