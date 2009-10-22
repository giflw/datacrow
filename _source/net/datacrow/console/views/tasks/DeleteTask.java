package net.datacrow.console.views.tasks;

import java.awt.Cursor;
import java.util.Collection;

import javax.swing.SwingUtilities;

import net.datacrow.console.MainFrame;
import net.datacrow.console.views.View;
import net.datacrow.console.windows.messageboxes.QuestionBox;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.IRequest;
import net.datacrow.core.wf.requests.StatusUpdateRequest;
import net.datacrow.core.wf.requests.UpdateStatusProgressBarRequest;
import net.datacrow.util.DataTask;

import org.apache.log4j.Logger;

public class DeleteTask extends DataTask {
    
    private static Logger logger = Logger.getLogger(DeleteTask.class.getName());

    private View view; 
    
    public DeleteTask(View view, Collection<? extends DcObject> objects) {
        super(objects);
        setName("Delete-Items-Task");
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
                        view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        view.setStatus(DcResources.getText("msgDeletingXItems", "" +objects.length));
                        view.setActionsAllowed(false);
                    }
                });
            } catch (Exception e) {
                logger.error(e, e);
            }
            
            startRunning();
            
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        QuestionBox qb = new QuestionBox(DcResources.getText("msgDeleteQuestion")); 
                        qb.setVisible(true);
                        if (!qb.isAffirmative())
                            stopRunning();
                    }
                });
            } catch (Exception e) {
                logger.error(e, e);
            }
                
            if (isRunning()) {
                int counter = 1;
                
                for (DcObject dco : objects) {
                    
                    dco.markAsUnchanged();
                    dco.addRequest(new UpdateStatusProgressBarRequest(
                            dco.getModule().getIndex(), MainFrame._SEARCHTAB, counter));                        
                    
                    if (counter == objects.length) {
                        dco.setSilent(false);
                        dco.setEndOfBatch(true);
                        IRequest request = 
                            new StatusUpdateRequest(dco.getModule().getIndex(), MainFrame._SEARCHTAB, "msgDeleteSuccessfull");
                        request.setExecuteOnFail(true);
                        dco.addRequest(request);
                    } else {
                        dco.setSilent(true);
                        dco.setEndOfBatch(false);
                    }
                    
                    dco.delete(); 
                    
                    try {
                        sleep(300);
                    } catch (Exception ignore) {} // doesn't want to sleep..
                    
                    counter++;
                }
            }
        } finally {
            stopRunning();
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        view.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        view.setActionsAllowed(true);
                    }
                });
            } catch (Exception e) {
                logger.error(e, e);
            }
            view = null;
        }
    }
}
