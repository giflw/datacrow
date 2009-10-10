package net.datacrow.core.migration;

public interface IModuleWizardClient {

    public void notifyNewTask();
    
	public void notifyStarted(int count);
	
	public void notifyFinished();
	
	public void notifyProcessed();
	
	public void notifyError(Exception e);
	
	public void notifyMessage(String msg);
	
	/**
	 * Indicates a sub process has been started. This allows the client to 
	 * either update its secondary progress bar or temporarily use the main 
	 * progress bar to indicate the status of the sub process. 

	 * @param count Size of the task
	 */
	public void notifyStartedSubProcess(int count);
	
	public void notifySubProcessed();
	
	/**
	 * Indicates that the sub process has finished.
	 * @see #notifyStartedSubProcess(int)
	 */
	public void notifyFinishedSubProcess();
}
