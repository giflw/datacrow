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

package net.datacrow.core.db.upgrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcSwingUtilities;

import org.apache.log4j.Logger;

/**
 * This class upgrades the old HSQL database, version 1.8.1 to the latest version 2.2.9.
 * It opens the database in the old engine, stores it as a script after which it can be upgraded
 * to the latest version of HSQL.
 *  
 * @author Robert Jan van der Waals
 */
public class UpgradeHsqlEngine {
    
    private static Logger logger = Logger.getLogger(UpgradeHsqlEngine.class.getName());
    
    public void run() {
        String v = "";
        String format = "";
        
        try {
            File file = new File(DataCrow.dataDir, DcSettings.getString(DcRepository.Settings.stConnectionString) + ".properties");
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                Properties p = new Properties();
                p.load(fis);
                v = (String) p.get("version");
                format = (String) p.get("hsqldb.script_format");
                fis.close();
            }
            
        } catch (Exception e) {
            logger.debug(e, e);
        }

        if (v != null && v.equals("1.8.1") && format != null && format.equals("3")) {
            DcSwingUtilities.displayMessage(
                    "The database version of HSQL will be upgraded. You will be asked for your username and password " +
                    "after which the upgrade will commence.");
            
            String address = "jdbc:hsqldb:file:" + DataCrow.dataDir + DcSettings.getString(DcRepository.Settings.stConnectionString);
            String cmd = "java -jar \"" + DataCrow.installationDir.substring(1) + "upgradeHSQL/upgradeHSQL.jar\" " + address;
            
            Runtime rt = Runtime.getRuntime();
            try {
                Process p = rt.exec(cmd);
                InputStream stderr = p.getErrorStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ( (line = br.readLine()) != null)
                    logger.error(line);
                
                p.waitFor();

                DcSwingUtilities.displayMessage("The upgrade has been completed. Data Crow will now exit.");
                
                System.exit(0);
                
            } catch (Exception exp) {
                logger.debug("Could not launch the command [" + cmd + "]", exp);
            }
        }
    }
}