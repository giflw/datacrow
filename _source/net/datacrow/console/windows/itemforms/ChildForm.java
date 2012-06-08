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

package net.datacrow.console.windows.itemforms;

import net.datacrow.core.data.DataManager;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.wf.requests.RefreshChildView;
import net.datacrow.core.wf.requests.Requests;

public class ChildForm extends DcMinimalisticItemView {

    private String parentID;
    private final int parentModuleIdx;
    
    public ChildForm(DcObject parent, int module, boolean readonly) {
        super(module, readonly);
        
        this.parentID = parent.getID();
        this.parentModuleIdx = parent.getModule().getIndex();
        
        list.setEnabled(!readonly);
    }
    
    @Override
    public void clear() {
        super.clear();
        parentID = null;
    }

    @Override
    public void load() {
        list.clear();
        list.add(DataManager.getChildrenKeys(parentID, getModuleIdx()));
    }
    
    @Override
    public void open() {
        DcObject dco = list.getSelectedItem();
        if (dco != null) {
            dco.markAsUnchanged();
            ChildItemForm childItemForm = new ChildItemForm(true, dco, this);
            childItemForm.setVisible(true);            
        }
    }
    
    @Override
    public void createNew() {
        DcObject dco = getModule().getItem();
        dco.setValue(dco.getParentReferenceFieldIndex(), parentID);
        
        ChildItemForm itemForm = new ChildItemForm(false, dco, this);
        itemForm.setVisible(true);
    }

    @Override
    public Requests getAfterDeleteRequests() {
        Requests requests = new Requests();
        requests.add(new RefreshChildView(this));
        return requests;
    }

    public int getParentModuleIdx() {
        return parentModuleIdx;
    }   
}
