package net.datacrow.core.migration.itemimport;

import net.datacrow.core.objects.DcObject;

public interface IItemImporterClient {

    public void notifyProcessed(DcObject item);
    
    public void notifyStarted(int count);
    
    public void notifyStopped();

    public void notifyMessage(String message);
}
