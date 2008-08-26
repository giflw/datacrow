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

import net.datacrow.console.windows.itemforms.DcMinimalisticItemView;
import net.datacrow.core.objects.DcObject;

public class RefreshPropertyItemViewRequest implements IUpdateUIRequest {

    private static final long serialVersionUID = 2482330071457469709L;
    private boolean executeOnFail = false;
    private DcMinimalisticItemView view;
    
    public RefreshPropertyItemViewRequest(DcMinimalisticItemView view) {
        this.view = view;
    }
    
    public void execute(Collection<DcObject> c) {
        SwingUtilities.invokeLater(new Updater(view));
        end();
    }
    
    public void end() {
        view = null;
    }

    public boolean getExecuteOnFail() {
        return executeOnFail;
    }
    
    public void setExecuteOnFail(boolean b) {
        executeOnFail = b;
    }
    
    private static class Updater implements Runnable {
        
        private DcMinimalisticItemView view;
        
        public Updater(DcMinimalisticItemView view) {
            this.view = view;
        }
        
        public void run() {
            view.addObjects();
            view = null;
        }
    }
}
