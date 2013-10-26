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

import net.datacrow.console.windows.itemformsettings.MaintainTabsDialog;

/**
 * Request to close a form.
 * 
 * @author Robert Jan van der Waals
 */
public class UpdateItemFormSettingsWindow implements IUpdateUIRequest {

    private static final long serialVersionUID = -8369080521270253879L;

    private MaintainTabsDialog wdw;
    private boolean executeOnFail = false;
    private boolean tabDeleted;

    public UpdateItemFormSettingsWindow(MaintainTabsDialog wdw, boolean tabDeleted) {
        this.wdw = wdw;
        this.tabDeleted = tabDeleted;
    }

    @Override
    public void execute() {
        wdw.refresh();
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
        wdw = null;
    }
}
