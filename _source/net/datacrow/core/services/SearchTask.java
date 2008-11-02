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

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.util.StringUtils;

import org.apache.log4j.Logger;

public abstract class SearchTask extends Thread {

    private static Logger logger = Logger.getLogger(SearchTask.class.getName());

    public static final int _ITEM_MODE_SIMPLE = 0;
    public static final int _ITEM_MODE_FULL = 1; 
    
    private boolean isCancelled = false;
    
    protected IOnlineSearchClient listener;
    
    private int maximum = 20;
    
    private String query;
    
    private String address;
    private IServer server;
    private SearchMode searchMode;
    private Region region;
    
    private int itemMode = _ITEM_MODE_SIMPLE;
    
    public SearchTask(IOnlineSearchClient listener, IServer server,
                      Region region, SearchMode mode, String query) {

		this.listener = listener;
		this.region = region;
		this.searchMode = mode;
		this.server = server;
		this.address = region != null ? region.getUrl() : server.getUrl();
		this.query = StringUtils.normalize(query);
	}

    protected void setServiceInfo(DcObject dco) {
        String service =  server.getName() + " / " + 
                         (region != null ? region.getCode() : "none") + " / " +
                         (searchMode != null ? searchMode.getDisplayName() : "none") + " / " + 
                          "value=[" + query + "]";
        dco.setValue(DcObject._SYS_SERVICE, service);
    }

    public void setItemMode(int mode) {
        this.itemMode = mode;
    }
    
    public int getItemMode() {
        return itemMode;
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
        return searchMode;
    }

    public void setMode(SearchMode mode) {
        this.searchMode = mode;
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

    public IServer getServer() {
        return server;
    }

    public int getMaximum() {
        return maximum;
    }    

    public String getWhiteSpaceSubst() {
        return "+";
    }

    public DcObject query(DcObject dco) throws Exception {
        String link = (String) dco.getValue(DcObject._SYS_SERVICEURL); 
        if (link != null && link.length() > 0)
            return getItem(new URL(link));

        return null;
    }

    /**
     * Query for the item(s) using the web key. 
     */
    protected Collection<DcObject> getItems(String key, boolean full) throws Exception {
        Collection<DcObject> items = new ArrayList<DcObject>();
        items.add(getItem(key, full));
        return items;
    }

    /**
     * Query for the item using the web key. 
     */
    protected abstract DcObject getItem(String key, boolean full) throws Exception;
    
    /**
     * Query for the item via the URL 
     */
    protected abstract DcObject getItem(URL url) throws Exception;
    
    /**
     * Get every web ID from the page. With these IDs it should be possible to 
     * get to the detailed item information. 
     */
    protected abstract Collection<String> getItemKeys() throws Exception ;
    
    @Override
    public void run() {
        Collection<String> keys = new ArrayList<String>();

        listener.addMessage(DcResources.getText("msgConnectingToServer", getAddress()));

        try {
            keys.addAll(getItemKeys());
        } catch (Exception e) {
            listener.addError(DcResources.getText("msgCouldNotConnectTo", getServer().getName()));
            logger.error(e, e);
        }
        
        listener.processingTotal(keys.size());

        if (keys.size() == 0) {
            listener.addWarning(DcResources.getText("msgNoResultsForKeywords", getQuery()));
            listener.stopped();
            return;
        }

        listener.addMessage(DcResources.getText("msgFoundXResults", String.valueOf(keys.size())));
        listener.addMessage(DcResources.getText("msgStartParsingXResults", String.valueOf(keys.size())));
        int counter = 0;
        
        for (String key : keys) {
            
            if (isCancelled() || counter == getMaximum()) break;
            
            try {
                for (DcObject dco : getItems(key, getItemMode() == _ITEM_MODE_FULL)) {
                    dco.setIDs();
                    setServiceInfo(dco);
                    
                    listener.addMessage(DcResources.getText("msgParsingSuccessfull", dco.toString()));
                    listener.addObject(dco);
                    sleep(100);
                }
                listener.processed(counter);
            } catch (Exception exp) {
                listener.addMessage(DcResources.getText("msgParsingError", "" + exp));
                logger.error(DcResources.getText("msgParsingError", "" + exp), exp);
                listener.processed(counter);
            }
            
            counter++;
        }
        
        listener.processed(counter);
        listener.stopped();        
    }
}
