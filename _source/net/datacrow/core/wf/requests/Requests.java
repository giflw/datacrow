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

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Collection of requests.  Requests can be added to queries and are executed after 
 * the query has been performed. A request is in most cases a UI related task which 
 * needs to be executed without locking the GUI.
 *  
 * @see IRequest
 * @see IUpdateUIRequest
 * 
 * @author Robert Jan van der Waals
 */
public class Requests implements Serializable {

    private static final long serialVersionUID = 6664749483781565179L;
    
    private ArrayList<IRequest> requests = new ArrayList<IRequest>();

    /**
     * Create a new empty instance.
     */
    public Requests() {}

    /**
     * Create a new instance and adds the provided request.
     */
    public Requests(IRequest request) {
        add(request);
    }

    /**
     * Total count of requests part of this instance.
     */
    public int size() {
        return requests.size();
    }
    
    /**
     * Adds a request.
     * @param request
     */
    public void add(IRequest request) {
    	requests = requests == null ? new ArrayList<IRequest>() : requests;
        requests.add(request);
    }

    /**
     * Removes the specified request.
     * @param request
     */
    public void remove(IRequest request) {
        if (requests != null)
            requests.remove(request);
    }

    /**
     * Free resources.
     */
    public void clear() {
    	if (requests != null) {
    		requests.clear();
    		requests = null;
    	}
    }

    /**
     * Gets the requests as an array.
     * @return The array (filled or empty)
     */
    public IRequest[] get() {
        return requests != null ? requests.toArray(new IRequest[0]) : new IRequest[0];
    }
}
