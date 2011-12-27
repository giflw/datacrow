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

package net.datacrow.console.views.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.datacrow.console.views.View;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.util.DataTask;
import net.datacrow.util.DcSwingUtilities;

public class DeleteTask extends DataTask {
    
    public DeleteTask(View view, List<String> keys) {
        super();
        
        setName("Delete-Items-Task");
        
        Collection<DcObject> items = new ArrayList<DcObject>();
        DcObject dco;
        for (String key : keys) {
            dco = view.getModule().getItem();
            dco.setValueLowLevel(DcObject._ID, key);
            items.add(dco);
        }
        
        super.setItems(items);
        super.setModule(view.getModule());
    }

    @Override
    public void run() {
        try {
            if (!DcSwingUtilities.displayQuestion("msgDeleteQuestion"))
                return;

            startTask();
            
            if (isRunning()) {
                for (DcObject dco : items) {
                    try {
                        dco.delete(true);
                    } catch (ValidationException e) {
                        DcSwingUtilities.displayWarningMessage(e.getMessage());
                    }
                    
                    try {
                        sleep(10);
                    } catch (Exception ignore) {}
                }
            }
        } finally {
            endTask();
        }
    }
}
