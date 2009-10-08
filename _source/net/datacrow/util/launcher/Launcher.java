package net.datacrow.util.launcher;

import java.awt.Desktop;
import java.io.IOException;

import org.apache.log4j.Logger;

public abstract class Launcher {

	private static Logger logger = Logger.getLogger(Launcher.class.getName());
	
	public abstract void launch();
	
    protected Desktop getDesktop() {
        if (Desktop.isDesktopSupported())
            return Desktop.getDesktop();
        
        return null;
    }
    
    protected void runCmd(String cmd) throws Exception {
    	try { 
    		Process p = Runtime.getRuntime().exec(cmd);
    		p.waitFor();
    	} catch (IOException ie) {
        	logger.debug("Could not launch command using the runCmd method [" + cmd + "]", ie);
        	throw new Exception(ie);
    	}
    }
}
