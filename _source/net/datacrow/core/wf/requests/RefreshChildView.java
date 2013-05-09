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

import javax.swing.SwingUtilities;

import net.datacrow.console.windows.itemforms.DcMinimalisticItemView;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

/**
 * Refreshed the child view.
 * 
 * @author Robert Jan van der Waals
 */
public class RefreshChildView implements IUpdateUIRequest {

    private static final long serialVersionUID = -781595544545143855L;
    private boolean executeOnFail = false;
    private DcMinimalisticItemView form;
    
    public RefreshChildView(DcMinimalisticItemView form) {
        this.form = form;
    }
    
    @Override
    public void execute() {
        SwingUtilities.invokeLater(new Updater(form));
        end();
    }
    
    @Override
    public boolean getExecuteOnFail() {
        return executeOnFail;
    }
    
    @Override
    public void setExecuteOnFail(boolean b) {
        executeOnFail = b;
    }
    
    @Override
    public void end() {
        form = null;
    }
    
    private static class Updater implements Runnable {
        
        private DcMinimalisticItemView form;
        
        public Updater(DcMinimalisticItemView form) {
            this.form = form;
        }
        
        @Override
        public void run() {
            if (form != null)
                form.load();
            else 
                return;
            
            for (DcObject dco : form.getItems()) {
                if (dco.getModule().getParent() != null) {
                    dco.getModule().getParent().getCurrentSearchView().loadChildren();
                    dco.getModule().getParent().getCurrentSearchView().refreshQuickView();
                } else {
                    DcModules.get(DcModules._CONTAINER).getCurrentSearchView().refreshQuickView();
                }
                break;
            }
            
            form = null;
        }
    }    
}
