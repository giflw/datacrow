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

import net.datacrow.console.views.ISimpleItemView;

/**
 * Refreshed a simple item view.
 * 
 * @author Robert Jan van der Waals
 */
public class RefreshSimpleViewRequest implements IUpdateUIRequest {

    private static final long serialVersionUID = 2482330071457469709L;
    private boolean executeOnFail = false;
    private ISimpleItemView view;
    
    public RefreshSimpleViewRequest(ISimpleItemView view) {
        this.view = view;
    }
    
    @Override
    public void execute() {
        SwingUtilities.invokeLater(new Updater(view));
        end();
    }
    
    @Override
    public void end() {
        view = null;
    }

    @Override
    public boolean getExecuteOnFail() {
        return executeOnFail;
    }
    
    @Override
    public void setExecuteOnFail(boolean b) {
        executeOnFail = b;
    }
    
    private static class Updater implements Runnable {
        
        private ISimpleItemView view;
        
        public Updater(ISimpleItemView view) {
            this.view = view;
        }
        
        @Override
        public void run() {
            view.loadItems();
            view = null;
        }
    }
}
