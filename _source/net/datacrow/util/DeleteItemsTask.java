package net.datacrow.util;

import java.util.Collection;

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.wf.requests.IRequest;
import net.datacrow.core.wf.requests.Requests;

public class DeleteItemsTask extends DataTask {

    private ITaskListener listener;
    
    private Requests requests = new Requests();
    
    public DeleteItemsTask(ITaskListener listener, Collection<DcObject> objects) {
        super(null, objects);   
        
        this.listener = listener;
    }
    
    boolean ignoreWarning = false;
        
    public void setIgnoreWarnings(boolean b) {
        ignoreWarning = b;
    }
    
    public void addRequest(IRequest request) {
        requests.add(request);
    }
        
    @Override
    public void run() {
        try {
            listener.notifyTaskStarted();
            listener.notifyTaskSize(items.size());
            
            int counter = 1;
            for (DcObject dco : items) {
                
                if (listener.isStopped()) break;
                
                listener.notifyProcessed();
                
                dco.markAsUnchanged();
                
                if (counter == items.size()) {
                    for (int j = 0; j < requests.get().length; j++) {
                        dco.addRequest(requests.get()[j]);
                    }
                } 

                try {
                    dco.delete(true);
                } catch (ValidationException e) {
                    if (!ignoreWarning) DcSwingUtilities.displayWarningMessage(e.getMessage());
                }
                
                try {
                    sleep(300);
                } catch (Exception ignore) {}
                
                counter++;
            }

        } finally {
            listener.notifyTaskStopped();
            listener = null;
            items.clear();
        }
    }
}
