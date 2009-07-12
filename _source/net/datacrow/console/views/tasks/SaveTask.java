package net.datacrow.console.views.tasks;

import java.awt.Cursor;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import net.datacrow.console.MainFrame;
import net.datacrow.console.views.View;
import net.datacrow.console.windows.messageboxes.SaveQuestionBox;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.RemoveFromCacheRequest;
import net.datacrow.core.wf.requests.StatusUpdateRequest;
import net.datacrow.core.wf.requests.UpdateStatusProgressBarRequest;
import net.datacrow.util.DataTask;

public class SaveTask extends DataTask {

    private static Logger logger = Logger.getLogger(SaveTask.class.getName());
    
    private View view;
    
    public SaveTask(View view, Collection<? extends DcObject> objects) {
        super(objects);
        this.view = view;
    }

    @Override
    public void run() {
        try {
            
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        view.updateProgressBar(0);
                        view.initProgressBar(objects.length);
                        view.setStatus(DcResources.getText("msgSavingXItems", "" + objects.length));
                        view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        view.setActionsAllowed(false);
                        view.checkForChanges(false);
                    }
                });
            } catch (Exception e) {
                logger.error(e, e);
            }

            startRunning();

            boolean ignoreErrors = false;
            int counter = 1;
            
            for (DcObject dco : objects) {
                
                if (!keepOnRunning()) break;
                
                try {
                    dco.setEndOfBatch(counter == objects.length);
                    dco.setSilent(true);

                    if (dco.isEndOfBatch()) {
                        dco.setSilent(false);
                        dco.addRequest(new StatusUpdateRequest(dco.getModule().getIndex(), MainFrame._INSERTTAB, "msgDataSaved"));
                    }

                    if (view.getType() == View._TYPE_SEARCH) {
                        view.updateProgressBar(counter);
                        dco.addRequest(new RemoveFromCacheRequest(view.getModule().getIndex(), dco.getID()));
                        dco.saveUpdate(true);
                    } else {
                        dco.addRequest(new UpdateStatusProgressBarRequest(dco.getModule().getIndex(), MainFrame._INSERTTAB, counter));
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
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        view.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        view.setActionsAllowed(true);
                        view.checkForChanges(true);
                        view.setDefaultSelection();
                    }
                });
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }
}
