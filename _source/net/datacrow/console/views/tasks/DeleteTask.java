package net.datacrow.console.views.tasks;

import java.awt.Cursor;
import java.util.Collection;

import javax.swing.SwingUtilities;

import net.datacrow.console.views.View;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DataTask;
import net.datacrow.util.DcSwingUtilities;

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
                if (!DcSwingUtilities.displayQuestion("msgDeleteQuestion"))
                    stopRunning();
            } catch (Exception e) {
                logger.error(e, e);
            }
                
            if (isRunning()) {
                int counter = 1;
                
                for (DcObject dco : objects) {
                    try {
                        dco.delete(true);
                    } catch (ValidationException e) {
                        DcSwingUtilities.displayWarningMessage(e.getMessage());
                    }
                    
                    try {
                        sleep(10);
                    } catch (Exception ignore) {}
                    
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
