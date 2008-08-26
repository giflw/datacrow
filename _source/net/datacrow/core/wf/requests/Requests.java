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

public class Requests implements Serializable {

    private static final long serialVersionUID = 6664749483781565179L;
    
    private ArrayList<IRequest> requests = new ArrayList<IRequest>();

    public Requests() {
    }

    public Requests(IRequest request) {
        add(request);
    }

    public int size() {
        return requests.size();
    }
    
    public void add(IRequest request) {
    	requests = requests == null ? new ArrayList<IRequest>() : requests;
        requests.add(request);
    }

    public void remove(IRequest request) {
        if (requests != null)
            requests.remove(request);
    }

    public void clear() {
    	if (requests != null) {
    		requests.clear();
    		requests = null;
    	}
    }

    public IRequest[] get() {
        return requests != null ? requests.toArray(new IRequest[0]) : new IRequest[0];
    }
}
