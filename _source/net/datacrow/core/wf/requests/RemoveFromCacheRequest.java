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

import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

public class RemoveFromCacheRequest implements IRequest {

    private static final long serialVersionUID = -6911519035345853780L;
    private String id;
    private final int module;
    private boolean executeOnFail = false;

    public RemoveFromCacheRequest(int module, String id) {
        this.id = id;
        this.module = module;
    }

    public void execute(Collection<DcObject> objects) {
        DcModule mod = DcModules.get(module);
        mod.getSearchView().removeFromCache(id);
        end();
    }

    public void end() {
        id = null;
    }
    
    public boolean getExecuteOnFail() {
    	return executeOnFail;
    }

    public void setExecuteOnFail(boolean b) {
    	executeOnFail = b;
    }
}
