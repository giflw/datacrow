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
import java.util.Collection;

import net.datacrow.core.db.Query;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.wf.UIUpdater;

/**
 * Requests can be added to queries and are executed after the query has been 
 * performed. A request is in most cases a UI related task which needs to be executed
 * without locking the GUI. 
 * 
 * @see IUpdateUIRequest
 * @see UIUpdater
 * @see Query
 * 
 * @author Robert Jan van der Waals
 */
public interface IRequest extends Serializable {

    /**
     * Executes this request. 
     * @param items The items retrieved by the query (if any).
     */
    public void execute(Collection<DcObject> items);

    /**
     * Indicates if the request is allowed to be executed even when the query 
     * has failed.
     */
    public boolean getExecuteOnFail();

    /**
     * Indicate if the request is allowed to be executed even when the query 
     * has failed.
     */
    public void setExecuteOnFail(boolean b);
    
    /**
     * Free resources.
     */
    public void end();
}
