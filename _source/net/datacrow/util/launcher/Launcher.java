package net.datacrow.util.launcher;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public abstract class Launcher {

	private static Logger logger = Logger.getLogger(Launcher.class.getName());
	
	public abstract void launch();
	
    protected Desktop getDesktop() {
        if (Desktop.isDesktopSupported())
            return Desktop.getDesktop();
        
        return null;
    }
    
    protected void runCmd(String[] command) throws Exception {
    	try { 
    	    Process p = new ProcessBuilder(command).start();
    		
    		InputStream is = p.getErrorStream();
    	    InputStreamReader isr = new InputStreamReader(is);
    	    BufferedReader br = new BufferedReader(isr);
    	    String line;
    	    
    	    // log the error messages
    	    while ((line = br.readLine()) != null) {
    	        logger.error(line);
    	    }
    	    
    	    p.waitFor();
    	    br.close();
    		
    	} catch (IOException ie) {
    	    String s = "";
    	    for (String cmd : command)
    	        s+= cmd + " ";
    	    
        	logger.debug("Could not launch command using the runCmd method [" + s.trim() + "]", ie);
        	throw new Exception(ie);
    	}
    }
}
