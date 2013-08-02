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

import net.datacrow.console.views.View;
import net.datacrow.console.windows.messageboxes.SaveQuestionBox;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.util.DataTask;

import org.apache.log4j.Logger;

public class SaveTask extends DataTask {

    private static Logger logger = Logger.getLogger(SaveTask.class.getName());
    
    private View view;
    
    public SaveTask(View view, Collection<? extends DcObject> items) {
        super(view.getModule(), items);
        setName("Save-Items-Task");
        this.view = view;
    }

    @Override
    public void run() {
        try {
            startTask();

            boolean ignoreErrors = false;
            int counter = 1;
            
            for (DcObject dco : new ArrayList<DcObject>(items)) {
                
                dco.setLastInLine(counter == items.size());
                
                if (!keepOnRunning()) break;
                
                try {
                    if (view.getType() == View._TYPE_SEARCH) {
                        view.updateProgressBar(counter);
                        dco.saveUpdate(true);
                    } else {
                        dco.saveNew(true);
                    }
                } catch (ValidationException vExp) {
                    if (!ignoreErrors) {
                        SaveQuestionBox question = new SaveQuestionBox(vExp);
                        if (question.getResult() == SaveQuestionBox._CANCEL)
                            endTask();
                        else if (question.getResult() == SaveQuestionBox._IGNORE)
                            ignoreErrors = true;
                    }
                }

                try {
                    
                    while (DatabaseManager.getQueueSize() > 0)
                        sleep(300);
                    
                } catch (Exception e) {
                	logger.warn("Could not wait", e);
                }
                
                counter++;
            }
        } finally {
            endTask();
        }
    }
}
