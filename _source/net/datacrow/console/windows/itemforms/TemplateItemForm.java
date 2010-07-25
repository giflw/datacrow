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
import net.datacrow.core.objects.DcTemplate;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.wf.requests.CloseWindowRequest;
import net.datacrow.core.wf.requests.RefreshSimpleViewRequest;
import net.datacrow.core.wf.requests.UpdateDefaultTemplate;
import net.datacrow.util.DcSwingUtilities;

public class TemplateItemForm extends ItemForm {
    
    private DcMinimalisticItemView parent;
    
    public TemplateItemForm(boolean update, DcObject o, DcMinimalisticItemView parent) {
        super(false, update, o, true);
        this.parent = parent;
    }
    
    @Override
    protected void saveValues() {
        apply();

        if (((DcTemplate) dco).isDefault())
            dco.addRequest(new UpdateDefaultTemplate((String) dco.getValue(50), dco.getModule().getIndex()));

        dco.addRequest(new RefreshSimpleViewRequest(parent));
        dco.addRequest(new CloseWindowRequest(this));
        
        if (!update)  {
            try {
                dco.setSilent(true);
                dco.saveNew(true);
            } catch (ValidationException vExp) {
                DcSwingUtilities.displayWarningMessage(vExp.getMessage());
            }
        } else if (isChanged()) {
            try {
                dco.setSilent(true);
                dco.saveUpdate(true);
            } catch (ValidationException vExp) {
                DcSwingUtilities.displayWarningMessage(vExp.getMessage());
            }
        } else if (! isChanged()) {
            DcSwingUtilities.displayWarningMessage("msgNoChangesToSave");
        }
    }  
    
    /**
     * Deletes this item from the database
     */
    @Override
    protected void deleteItem() {
        if (DcSwingUtilities.displayQuestion("msgDeleteQuestion")) {
            Long id = dco.getID();
            dco.clearValues();
            dco.setValue(DcObject._ID, id);
            dco.setSilent(true);

            dco.addRequest(new CloseWindowRequest(this));
            dco.addRequest(new RefreshSimpleViewRequest(parent));
            try {
                dco.delete(false);
            } catch (ValidationException e) {}
        }
    }
}
