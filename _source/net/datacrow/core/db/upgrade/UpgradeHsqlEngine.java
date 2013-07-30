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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.Utilities;

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
            File file = new File(DataCrow.databaseDir, DcSettings.getString(DcRepository.Settings.stConnectionString) + ".properties");
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
                    "The database version of HSQL will be upgraded." +
                    "\nIMPORTANT: after the upgrade your password has to be entered in CAPITALS!");
            
            String address = "jdbc:hsqldb:file:" + DataCrow.databaseDir + DcSettings.getString(DcRepository.Settings.stConnectionString);
            
            String path = DataCrow.installationDir;
            String[] command  = new String[] {
                    "java", 
                    "-jar",
                    path + "upgradeHSQL/upgradeHSQL.jar",
                    "address"};
            DcSwingUtilities.displayMessage(
                    "NOTE: if the upgrade fails this process will be started again on your next startup. " +
                    "If this continues, run the following command from the command line / terminal: \n" +
                    "java -jar " + path + "upgradeHSQL/upgradeHSQL.jar " + address);
            
            try {
                Process p = new ProcessBuilder(command).start();
                InputStream stderr = p.getErrorStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ( (line = br.readLine()) != null)
                    logger.error(line);
                
                p.waitFor();
                br.close();
                
                DcSwingUtilities.displayMessage("The upgrade has been completed. Data Crow will now exit.");
                
                System.exit(0);
                
            } catch (Exception exp) {
                logger.debug("Could not launch the command [" + 
                        "java -jar " + path + "upgradeHSQL/upgradeHSQL.jar " + address + "]", exp);
            }
        } else if (v != null && v.equals("1.8.1") && format != null && format.equals("0")) {
            // conversion failure or not yet converted from the old version to the new version
            File file = new File(DataCrow.databaseDir, DcSettings.getString(DcRepository.Settings.stConnectionString) + ".script");
            
            FileInputStream in = null;
            BufferedReader br = null;
            try {
                in = new FileInputStream(file);
                br = new BufferedReader(new InputStreamReader(in));
                String strLine;

                FileWriter outFile = new FileWriter(new File(DataCrow.databaseDir, DcSettings.getString(DcRepository.Settings.stConnectionString) + ".dcnew"));
                PrintWriter out = new PrintWriter(outFile);
                
                // All unique indexes will be skipped. The uniqueness will be fixed with the upgrade to version 3.9.22
                while((strLine = br.readLine()) != null) {
                    strLine = strLine.replaceAll("_KEEP", "KEEP_ME");
                    if (!strLine.startsWith("CREATE UNIQUE INDEX ") && !strLine.equals("\n") && !strLine.equals("\r") && !strLine.equals("\n\r") && !strLine.equals("")) {
                        out.write(strLine);
                        out.write("\r\n");
                    }
                }
                
                out.close();
                in.close();
                br.close();

                new File(DataCrow.databaseDir, DcSettings.getString(DcRepository.Settings.stConnectionString) + ".script").delete();
                Utilities.rename(
                        new File(DataCrow.databaseDir, DcSettings.getString(DcRepository.Settings.stConnectionString) + ".dcnew"), 
                        new File(DataCrow.databaseDir, DcSettings.getString(DcRepository.Settings.stConnectionString) + ".script"));
                
            } catch (Exception e) {
                logger.error(e, e);
                try { in.close(); } catch (IOException exp) {};
                try { br.close(); } catch (IOException exp) {};
            }
        }
    }
}