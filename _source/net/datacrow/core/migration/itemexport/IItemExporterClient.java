package net.datacrow.core.migration.itemexport;

public interface IItemExporterClient {

    public void notifyProcessed();
    
    public void notifyStarted(int count);
    
    public void notifyStopped();

    public void notifyMessage(String message);
}
