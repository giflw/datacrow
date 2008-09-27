/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                       (c) 2003 The Data Crow team                          *
 *                              info@datacrow.net                             *
 *                                                                            *
 *                                                                            *
 *       This library is free software; you can redistribute it and/or        *
 *        modify it under the terms of the GNU Lesser General Public          *
 *       License as published by the Free Software Foundation; either         *
 *     version 2.1 of the License, or (at your option) any later version.     *
 *                                                                            *
 *      This library is distributed in the hope that it will be useful,       *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU       *
 *           Lesser General Public License for more details.                  *
 *                                                                            *
 *     You should have received a copy of the GNU Lesser General Public       *
 *    License along with this library; if not, write to the Free Software     *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA   *
 *                                                                            *
 ******************************************************************************/

package net.datacrow.util.freedb;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import net.datacrow.core.DataCrow;


public class DiscIDUtil {
	
	private static Logger logger = Logger.getLogger(DiscIDUtil.class.getName());
    
    private boolean success = false;
    
    /**
     * Indicates whether the disc ID was read successfully
     * @return success indicator
     */
    public boolean isSuccess() {
    	return success;
    }
    
    /**
     * Read the disc ID. The disc ID is read in a separate process as this is
     * done platform dependent. 
     * @return valid disc id (with track offsets)
     * @throws IOException
     */
    public String readDiscID() throws IOException {
        String exec = getScript();
        setExecutionAllowed(exec);
        
        Process p = Runtime.getRuntime().exec(exec);
        try { 
            p.waitFor();
        } catch (Exception exp) {}
        
        int exitvalue = p.exitValue();
        InputStream is = null;
        if (exitvalue == 1) {
            is = p.getErrorStream();
        } else {
            is = p.getInputStream();
            success = true;
        }

        byte[] b = new byte[is.available()];
        is.read(b);
        String s = new String(b);
        if (s.equals("")) {
            success = false;
        }
        is.close();
        
        return s;
    }    
    
    private String getScript() {
        String exec;
     
        if (DataCrow.getPlatform().isMac()) {
            exec = DataCrow.pluginsDir + "discid/mac/discid";
        } else if (DataCrow.getPlatform().isWin()) {
            exec = DataCrow.pluginsDir + "discid/win32/discid.exe";
            if (exec.startsWith("/")) exec = exec.substring(1, exec.length());
        } else {
            exec = DataCrow.pluginsDir + "discid/linux/discid";
        }           
        
        if (exec.indexOf(" ") > -1) 
        	exec = '"' + exec + '"';
        
        return exec;
    }
    
    /**
     * Change the permissions on the script file (non Windows users)
     * for the current user (allow execution).
     */
    private void setExecutionAllowed(String scriptFile) {
        try {
            if (!DataCrow.getPlatform().isWin()) {
                String cmd = "chmod u=rwx " + scriptFile;
                Process proc = Runtime.getRuntime().exec(cmd);
                proc.waitFor();
            }
        } catch (Exception e) {
            logger.error(e, e);
        }        
    }    
}
