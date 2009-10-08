package net.datacrow.core.migration.moduleimport;

public interface IModuleImporterClient {

	public void notifyStarted(int count);
	
	public void notifyFinished();
	
	public void notifyProcessed();
	
	public void notifyError(Exception e);
	
	public void notifyMessage(String msg);
}