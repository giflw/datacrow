package net.datacrow.util;

import javax.swing.SwingUtilities;

import net.datacrow.console.windows.onlinesearch.ProgressDialog;

public class PollerTask extends Thread {
        
    private Thread thread;
    
    private boolean finished = false;
    private String title;
    
    public PollerTask(Thread thread, String title) {
        setPriority(Thread.MIN_PRIORITY);
        this.thread = thread;
        this.title = title;
    }
    
    public void finished(boolean b) {
        finished = b;
    }
    
    @Override
    public void run() {
        final ProgressDialog dlg = new ProgressDialog(title);
        while (!finished && thread.isAlive()) {
            SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        public void run() {
                            dlg.update();                                
                        }
                    }));
            
            try { sleep(10); } catch (Exception ignore) {}
        }
        
        SwingUtilities.invokeLater(
            new Thread(new Runnable() { 
                public void run() {
                    dlg.close();
                }
            }));
        
        thread = null;
    }
}
