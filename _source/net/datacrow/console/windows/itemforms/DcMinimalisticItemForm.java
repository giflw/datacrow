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

import net.datacrow.console.views.ISimpleItemView;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.windows.messageboxes.QuestionBox;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.CloseWindowRequest;
import net.datacrow.core.wf.requests.RefreshSimpleViewRequest;

public class DcMinimalisticItemForm extends ItemForm {
    
    private ISimpleItemView parent;
    
    public DcMinimalisticItemForm(boolean readonly, boolean update, DcObject o, ISimpleItemView parent) {
        super(readonly, update, o, true);
        this.parent = parent;
    }
    
    @Override
    protected void applySettings() {
        setSize(DcModules.get(moduleIdx).getSettings().getDimension(DcRepository.ModuleSettings.stSimpleItemFormSize));
    }
    
    @Override
    protected void saveSettings() {
        DcModules.get(moduleIdx).setSetting(DcRepository.ModuleSettings.stSimpleItemFormSize, getSize());        
    }
    
    @Override
    protected void saveValues() {
        apply();
        
        if (parent != null)
            dco.addRequest(new RefreshSimpleViewRequest(parent));
        
        dco.addRequest(new CloseWindowRequest(this));
        
        if (!update)  {
            try {
                dco.setSilent(true);
                dco.saveNew(true);
            } catch (ValidationException vExp) {
                new MessageBox(vExp.getMessage(), MessageBox._WARNING);
            }
        } else if (isChanged()) {
            try {
                dco.setSilent(true);
                dco.saveUpdate(true);
            } catch (ValidationException vExp) {
                new MessageBox(vExp.getMessage(), MessageBox._WARNING);
            }
        } else if (! isChanged()) {
            new MessageBox(DcResources.getText("msgNoChangesToSave"), MessageBox._WARNING);
        }
    }  
    
    /**
     * Deletes this item from the database
     */
    @Override
    protected void deleteValues() {
        QuestionBox qb = new QuestionBox(DcResources.getText("msgDeleteQuestion"), this);
        qb.setVisible(true);
        
        if (qb.isAffirmative()) {
            String id = dco.getID();
            dco.clearValues();
            dco.setValue(DcObject._ID, id);
            dco.setSilent(true);

            dco.addRequest(new CloseWindowRequest(this));
            
            if (parent != null)
                dco.addRequest(new RefreshSimpleViewRequest(parent));
            
            dco.delete();
        }
    }
}
