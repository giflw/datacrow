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

package net.datacrow.core.wf;

import java.util.Collection;

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.wf.requests.IRequest;
import net.datacrow.core.wf.requests.IUpdateUIRequest;
import net.datacrow.core.wf.requests.Requests;

public class UIUpdater implements Runnable {

    private Collection<DcObject> objects;
    private Requests requests;
    private boolean qryWasSuccess;

    public UIUpdater(Collection<DcObject> objects,
                     Requests requestors,
                     boolean qryWasSuccess) {
        
        this.qryWasSuccess = qryWasSuccess;
        this.objects = objects;
        this.requests = requestors;
    }

    public void close() {
        objects = null;

        if (requests != null)
            requests.clear();
        
        requests = null;
    }

    public void run() {
        if (requests != null) {
            IRequest[] requestArray = requests.get();

            for (int i = 0; i < requestArray.length; i++) {
                IRequest request = requestArray[i];
                
                if (request instanceof IUpdateUIRequest) { 
                    if (qryWasSuccess || request.getExecuteOnFail())
                        request.execute(objects);
                    else 
                        request.end();
                }
            }            
        }
        close();
    }
}
