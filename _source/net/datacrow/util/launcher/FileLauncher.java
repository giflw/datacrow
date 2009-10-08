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

package net.datacrow.util.launcher;

import java.awt.Desktop;
import java.io.File;

import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.definitions.ProgramDefinitions;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class FileLauncher extends Launcher {

	private static Logger logger = Logger.getLogger(FileLauncher.class.getName());
	
    private final String filename;
    
    public FileLauncher(File file) {
        this.filename = file != null ? file.toString() : null;
    }
    
    public FileLauncher(String filename) {
        this.filename = filename;
    }
    
    public void launch() {
        if (Utilities.isEmpty(filename)) {
            new MessageBox(DcResources.getText("msgNoFilename"), MessageBox._WARNING);
            return;
        }
        
        File file = new File(filename);
        if (!file.exists()) {
            new MessageBox(DcResources.getText("msgFileDoesNotExist", filename), MessageBox._WARNING);
            return;
        }
            
        ProgramDefinitions definitions = (ProgramDefinitions) DcSettings.getDefinitions(DcRepository.Settings.stProgramDefinitions);
        String program = null;
        String extension = Utilities.getExtension(file);
        if (definitions != null && !Utilities.isEmpty(extension)) 
            program = definitions.getProgramForExtension(extension);

        Desktop desktop = getDesktop();
        if (program == null || program.trim().length() == 0) {
            boolean launched = true;
            if (desktop != null) {
                try {
                    desktop.open(file);
                } catch (Exception exp) {
                	logger.debug("Could not launch file using the Dekstop class [" + file + "]", exp);
                    launched = false;
                }
            }

            if (!launched) {
                try {
                    runCmd(filename);
                } catch (Exception ignore) {
                    try {
                        runCmd(getLaunchableName());
                    } catch (Exception exp) {
                        new MessageBox(DcResources.getText("msgNoProgramDefinedForExtension", extension), MessageBox._WARNING);    
                    }
                }
            }
        } else {
            String cmd = program + " " + getLaunchableName();
            try {
                runCmd(cmd);
            } catch (Exception ignore) {
                try {
                    runCmd("'" + cmd + "' " + getLaunchableName());
                } catch (Exception exp) {
                    new MessageBox(DcResources.getText("msgErrorWhileExecuting", cmd), MessageBox._WARNING);
                }
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
}
