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

import java.awt.Cursor;
import java.util.Collection;

import javax.swing.SwingUtilities;

import net.datacrow.console.MainFrame;
import net.datacrow.console.views.View;
import net.datacrow.console.windows.messageboxes.SaveQuestionBox;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.StatusUpdateRequest;
import net.datacrow.util.DataTask;

import org.apache.log4j.Logger;

public class SaveTask extends DataTask {

    private static Logger logger = Logger.getLogger(SaveTask.class.getName());
    
    private View view;
    
    public SaveTask(View view, Collection<? extends DcObject> objects) {
        super(objects);
        setName("Save-Items-Task");
        this.view = view;
    }

    @Override
    public void run() {
        try {
            try {
                if (!SwingUtilities.isEventDispatchThread()) {                
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            view.updateProgressBar(0);
                            view.initProgressBar(objects.length);
                            view.setStatus(DcResources.getText("msgSavingXItems", "" + objects.length));
                            view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                            view.setActionsAllowed(false);
                            view.checkForChanges(false);
                        }
                    });
                } else {
                    view.updateProgressBar(0);
                    view.initProgressBar(objects.length);
                    view.setStatus(DcResources.getText("msgSavingXItems", "" + objects.length));
                    view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    view.setActionsAllowed(false);
                    view.checkForChanges(false);
                }
            } catch (Exception e) {
                logger.error(e, e);
            }

            startRunning();

            boolean ignoreErrors = false;
            int counter = 1;
            
            for (DcObject dco : objects) {
                
                if (!keepOnRunning()) break;
                
                try {
                    if (counter == objects.length) {
                        dco.addRequest(new StatusUpdateRequest(dco.getModule().getIndex(), 
                                       view.getType() == View._TYPE_SEARCH ? MainFrame._SEARCHTAB : MainFrame._INSERTTAB, "msgDataSaved"));
                    }

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
                            stopRunning();
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
            stopRunning();
            
            try {
                if (!SwingUtilities.isEventDispatchThread()) {                
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            view.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                            view.setActionsAllowed(true);
                            view.checkForChanges(true);
                            //view.setDefaultSelection();
                        }
                    });
                } else {
                    view.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    view.setActionsAllowed(true);
                    view.checkForChanges(true);
                    //view.setDefaultSelection();
                }
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }
}
