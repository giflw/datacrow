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

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.wf.requests.CloseWindowRequest;
import net.datacrow.core.wf.requests.RefreshChildView;
import net.datacrow.util.DcSwingUtilities;

public class ChildItemForm extends ItemForm {

    private ChildForm childForm;
    
    public ChildItemForm(boolean update, DcObject o, ChildForm childForm) {
        super(false, update, o, true);
        this.childForm = childForm;
    }
    
    @Override
    protected void saveValues() {
        apply();
        dco.addRequest(new CloseWindowRequest(this));
        
        if (!update)  {
            try {
                dco.addRequest(new RefreshChildView(childForm));
                dco.setSilent(true);
                dco.saveNew(true);
            } catch (ValidationException vExp) {
                DcSwingUtilities.displayWarningMessage(vExp.getMessage());
            }
        } else if (isChanged()) {
            try {
                dco.addRequest(new RefreshChildView(childForm));
                dco.setSilent(true);
                dco.saveUpdate(true);
            } catch (ValidationException vExp) {
                DcSwingUtilities.displayWarningMessage(vExp.getMessage());
            }
        } else if (! isChanged()) {
            DcSwingUtilities.displayWarningMessage("msgNoChangesToSave");
        }
    }  
    
    @Override
    public void close(boolean aftersave) { 
        super.close(aftersave);
        childForm = null;
    }
    
    /**
     * Deletes this item from the database
     */
    @Override
    protected void deleteItem() {
        if (DcSwingUtilities.displayQuestion("msgDeleteQuestion")) {
            String id = dco.getID();
            dco.clearValues();
            dco.setValue(DcObject._ID, id);
            dco.setSilent(true);

            dco.addRequest(new CloseWindowRequest(this));
            dco.addRequest(new RefreshChildView(childForm));
            
            try {
                dco.delete(false);
            } catch (ValidationException e) {}
        }
    }
}
