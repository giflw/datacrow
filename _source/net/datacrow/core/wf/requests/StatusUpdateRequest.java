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

import net.datacrow.console.MainFrame;
import net.datacrow.console.windows.itemforms.DcMinimalisticItemView;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.DcPropertyModule;
import net.datacrow.core.modules.TemplateModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;

public class StatusUpdateRequest implements IRequest {

    private static final long serialVersionUID = -3575195683867232898L;
    
    private final int module;
    private final int tab;
    private String msgID;
    private boolean executeOnFail = false;

    public StatusUpdateRequest(int module, int tab, String msgID) {
        this.module = module;
        this.tab = tab;
        this.msgID = msgID;
    }

    public void execute(Collection<DcObject> objects) {
        SwingUtilities.invokeLater(new Updater(module, msgID, tab));
        end();
    }
    
    public void end() {
        msgID = null;
    }

    public boolean getExecuteOnFail() {
        return executeOnFail;
    }

    public void setExecuteOnFail(boolean b) {
        executeOnFail = b;
    }
    
    private static class Updater implements Runnable {
        
        private int module;
        private int tab;
        private String msgID;
        
        public Updater(int module, String msgID, int tab) {
            this.module = module;
            this.tab = tab;
            this.msgID = msgID;
        }
        
        public void run() {
            DcModule mod = DcModules.get(module);

            String msg = DcResources.getText(msgID);
            if (mod.getType() == DcModule._TYPE_PROPERTY_MODULE) {
                DcMinimalisticItemView view = ((DcPropertyModule) mod).getForm();
                view.setStatus(msg);
            } else if (mod.getType() == DcModule._TYPE_TEMPLATE_MODULE) {
                DcMinimalisticItemView form = ((TemplateModule) mod).getForm();
                form.setStatus(msg);             
            } else {
                if (tab == MainFrame._INSERTTAB) {
                    mod.getCurrentInsertView().setStatus(msg);
                } else if (tab == MainFrame._SEARCHTAB) {
                    mod.getCurrentSearchView().setStatus(msg);
                }
            }
            
            msg = null;
        }
    }    
}
