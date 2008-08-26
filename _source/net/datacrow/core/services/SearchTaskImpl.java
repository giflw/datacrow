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
import java.util.Collection;

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.plugin.IServer;

import org.apache.log4j.Logger;

public abstract class SearchTaskImpl extends SearchTask {
    
    private static Logger logger = Logger.getLogger(SearchTaskImpl.class.getName());
    
    public SearchTaskImpl(IOnlineSearchClient listener, 
                          IServer server, 
                          Region region, 
                          SearchMode mode,
                          String query) {
        
        super(listener, server, region, mode, query);
    }

    @Override
    public String getWhiteSpaceSubst() {
        return "+";
    }

    @Override
    public DcObject query(DcObject dco) throws Exception {
        String link = (String) dco.getValue(DcObject._SYS_SERVICEURL); 
        if (link != null && link.length() > 0)
            return getDcObject(new URL(link));

        return null;
    }

    /**
     * Query for the item using the web ID. 
     */
    protected abstract DcObject getDcObject(String id) throws Exception;
    
    /**
     * Query for the item via the URL 
     */
    protected abstract DcObject getDcObject(URL url) throws Exception;
    
    /**
     * Get every web ID from the page. With these IDs it should be possible to 
     * get to the detailed item information. 
     */
    protected abstract Collection<String> getIDs();
    
    
    @Override
    public void run() {
        Collection<String> ids = getIDs();
        listener.processingTotal(ids.size());

        if (ids.size() == 0) {
            listener.addWarning(DcResources.getText("msgNoResultsForKeywords", getQuery()));
            listener.stopped();
            return;
        }

        listener.addMessage(DcResources.getText("msgFoundXResults", String.valueOf(ids.size())));
        listener.addMessage(DcResources.getText("msgStartParsingXResults", String.valueOf(ids.size())));
        int counter = 0;
        
        for (String id : ids) {
            
            if (isCancelled() || counter == getMaximum()) break;
            
            try {
                DcObject dco = getDcObject(id);
                if (dco != null) {
                    setServiceInfo(dco);
                    listener.addMessage(DcResources.getText("msgParsingSuccessfull", dco.toString()));
                    listener.addObject(dco);
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
