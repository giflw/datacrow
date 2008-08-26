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

import org.apache.log4j.Logger;

import net.datacrow.console.MainFrame;
import net.datacrow.console.views.View;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.settings.DcSettings;

public class UpdateStatusProgressBarRequest implements IRequest {

    private static final long serialVersionUID = -2194307878562403146L;

    private static Logger logger = Logger.getLogger(UpdateStatusProgressBarRequest.class.getName());
    
    private boolean executeOnFail = true;
    private final int module;
    private final int panel;
    private final int value;
    
    public UpdateStatusProgressBarRequest(int module, int panel, int value) {
        this.module = module;
        this.panel = panel;
        this.value = value;
    }
    
    public void execute(Collection<DcObject> c) {
        DcModule mod = DcModules.get(DcSettings.getInt(DcRepository.Settings.stModule));
        if (!mod.isAbstract())
            mod = DcModules.get(module);

        try {
            View view;
            if (panel == MainFrame._SEARCHTAB) 
                view = mod.getCurrentSearchView();
            else
                view = mod.getCurrentInsertView();
            
            view.updateProgressBar(value);

        } catch (Exception e) {
            logger.error("Could not update the progressbar", e);
        }
        
        end();
    }

    public void end() {
        
    }
    
    public boolean getExecuteOnFail() {
        return executeOnFail;
    }
    
    public void setExecuteOnFail(boolean b) {
        executeOnFail = b;
    }
}