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

import javax.swing.SwingUtilities;

import net.datacrow.console.views.MasterView;
import net.datacrow.console.windows.itemforms.DcMinimalisticItemView;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

public class RefreshChildView implements IUpdateUIRequest {

    private static final long serialVersionUID = -781595544545143855L;
    private boolean executeOnFail = false;
    private DcMinimalisticItemView form;
    
    public RefreshChildView(DcMinimalisticItemView form) {
        this.form = form;
    }
    
    public void execute(Collection<DcObject> c) {
        SwingUtilities.invokeLater(new Updater(form));
        end();
    }
    
    public boolean getExecuteOnFail() {
        return executeOnFail;
    }
    
    public void setExecuteOnFail(boolean b) {
        executeOnFail = b;
    }
    
    public void end() {
        form = null;
    }
    
    private static class Updater implements Runnable {
        
        private DcMinimalisticItemView form;
        
        public Updater(DcMinimalisticItemView form) {
            this.form = form;
        }
        
        public void run() {
            if (form != null)
                form.addObjects();
            
            for (DcObject dco : form.getItems()) {
                String parentID = dco.getParentID();
                
                if (dco.getModule().getParent() == null && dco.getModule().isContainerManaged()) {
                    DcObject parent = DataManager.getObject(DcModules._CONTAINER, parentID);
                    parent.getModule().getSearchView().get(MasterView._LIST_VIEW).updateItem(
                            parentID,  
                            parent.getModule().getDcObject(), 
                            false, false, false);                    
                    
                } else {
                    DcObject parent = DataManager.getObject(dco.getModule().getParent().getIndex(), parentID);
                    parent.getModule().getSearchView().get(MasterView._LIST_VIEW).updateItem(
                                                           parentID,  
                                                           parent.getModule().getDcObject(), 
                                                           false, false, false);
                }
                break;
            }
            
            form = null;
        }
    }    
}
