/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
 *                                                                            *
 *                       This file is part of Data Crow.                      *
 *       Data Crow is free software; you can redistribute it and/or           *
 *        modify it under the terms of the GNU General Public                 *
 *       License as published by the Free Software Foundation; either         *
 *              version 3 of the License, or any later version.               *
 *                                                                            *
 *        Data Crow is distributed in the hope that it will be useful,        *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *           MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.             *
 *           See the GNU General Public License for more details.             *
 *                                                                            *
 *        You should have received a copy of the GNU General Public           *
 *  License along with this program. If not, see http://www.gnu.org/licenses  *
 *                                                                            *
 ******************************************************************************/

package net.datacrow.util;

import java.io.File;

import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.definitions.ProgramDefinitions;
import edu.stanford.ejalbert.BrowserLauncher;

public class FileLauncher {

    private final String filename;
    
    public FileLauncher(String filename) {
        this.filename = filename;
    }
    
    @SuppressWarnings("deprecation")
    public void launchFile() {
        if (filename == null || filename.trim().length() == 0) {
            new MessageBox(DcResources.getText("msgNoFilename"), MessageBox._WARNING);
            return;
        }
        
        File file = new File(filename);
        if (!file.exists()) {
            new MessageBox(DcResources.getText("msgFileDoesNotExist", filename), MessageBox._WARNING);
            return;
        }
            
        String extension = Utilities.getExtension(file);
        if (extension == null || extension.length() == 0) {
            new MessageBox(DcResources.getText("msgInvalidExtension", filename), MessageBox._WARNING);
            return;
        }
        

        ProgramDefinitions definitions = (ProgramDefinitions) DcSettings.getDefinitions(DcRepository.Settings.stProgramDefinitions);
        String program = null;
        if (definitions != null) 
            program = definitions.getProgramForExtension(extension);

        if (program == null || program.trim().length() == 0) {
            try {
                try {
                    // use the browser launcer (v2)
                    BrowserLauncher.openURL("file://" + filename);
                } catch (Exception exp) {
                    // last change: just start it on the command line
                    runCmd(getLaunchableName());
                }
            } catch (Exception exp) {
                new MessageBox(DcResources.getText("msgNoProgramDefinedForExtension", extension), MessageBox._WARNING);  
            }
        } else {
            String cmd = program + " " + getLaunchableName();
            try {
                runCmd(cmd);
            } catch (Exception exp) {
                new MessageBox(DcResources.getText("msgErrorWhileExecuting", cmd), MessageBox._WARNING);
            } 
        }
    }   
    
    private String getLaunchableName() {
    	String name = "";    
    	
        if (!DataCrow.getPlatform().isWin()) {
        	for (int i = 0; i < filename.length(); i++) {
        		char c = filename.charAt(i);
        		name += (c == '*' || c == '?' || c == '\\') ? "\\" + c : "" + c; 
        	}
        	name = "'" + filename + "'";
        } else {
        	name = '"' + filename + '"';
        }
    	
    	return name;
    }
    
    private void runCmd(String cmd) throws Exception {
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
    }
}
