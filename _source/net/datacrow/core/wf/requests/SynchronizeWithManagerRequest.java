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

package net.datacrow.core.wf.requests;

import java.util.Collection;
import java.util.Date;

import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

import org.apache.log4j.Logger;

/**
 * Synchronizes the item cache ({@link DataManager}) as to reflect item updates, removal and
 * creation. This is the most important request type of Data Crow and is part of every item data
 * related query.
 * 
 * @author Robert Jan van der Waals
 */
public class SynchronizeWithManagerRequest implements IRequest {
    
    private static final long serialVersionUID = -1042270209816565755L;

    private static Logger logger = Logger.getLogger(SynchronizeWithManagerRequest.class.getName());
    
    public static final int _UPDATE = 0;
    public static final int _DELETE = 1;
    public static final int _ADD = 2;
    
    private int type; 
    private int module;
    private DcObject dco;
    
    /**
     * Creates a new instance.
     * @param type Either {@link #_UPDATE}, {@link #_DELETE} or {@link #_ADD}.
     * @param dco The item to synchronize.
     */
    public SynchronizeWithManagerRequest(int type, DcObject dco) {
        this.type = type;
        this.dco = dco;
        
        if (dco.getModule().getIndex() == DcModules._MEDIA)
            this.module = DcModules.getCurrent().getIndex();
        else         
            this.module = dco.getModule().getIndex();
    }

    /**
     * Execute the synchronization.
     * @param items The items to synchronize.
     */
    public void execute(Collection<DcObject> items) {
        
        long start = logger.isDebugEnabled() ? new Date().getTime() : 0;
        
        switch (type) {
        case _UPDATE:
            DataManager.update(dco, module);
            break;
        case _ADD:
            DataManager.add(dco, module);
            break;
        case _DELETE:
            DataManager.remove(dco, module);
            break;
        }
        
        end();
        
        if (logger.isDebugEnabled()) {
            long end = new Date().getTime();
            logger.debug("Synchronization request handling took " + (end - start) + "ms");
        }        
    }

    /**
     * Free resources.
     */
    public void end() {
        dco = null;
    }
    
    /**
     * This request should never be executed when the query failed to execute.
     */
    public boolean getExecuteOnFail() {
        return false;
    }

    /**
     * This request should never be executed when the query failed to execute.
     * Ignores any input.
     */
    public void setExecuteOnFail(boolean b) {}
}
