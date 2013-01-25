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

package net.datacrow.core.backup;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.datacrow.core.DataCrow;
import net.datacrow.core.Version;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcLanguageResource;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

import org.apache.log4j.Logger;

/**
 * The restore class is capable of restoring a back up.
 * Based on the settings either the data, the modules, the modules or all
 * information is restored.
 * 
 * @author Robert Jan van der Waals
 */
public class Restore extends Thread {
    
    private static Logger logger = Logger.getLogger(Restore.class.getName());
    
    private Version version;
    private File source;
    private IBackupRestoreListener listener;
    
    private boolean restoreModules = true;
    private boolean restoreDatabase = true;
    private boolean restoreReports = true;
   
    /**
     * Creates a new instance.
     * @param listener The listener will be updated on events and errors.
     * @param source The backup file.
     */
    public Restore(IBackupRestoreListener listener, File source) {
        this.source = source;
        this.listener = listener;
    }
    
    private boolean isVersion(String filename) {
        return filename.toLowerCase().equals("version.txt");
    }
    
    /**
     * Indicate if the database should be restores.
     * @param b
     */
    public void setRestoreDatabase(boolean b) {
        this.restoreDatabase = b;
    }

    /**
     * Indicate if the modules should be restores.
     * @param b
     */
    public void setRestoreModules(boolean b) {
        this.restoreModules = b;
    }

    /**
     * Indicate if the reports should be restores.
     * @param b
     */
    public void setRestoreReports(boolean b) {
        this.restoreReports = b;
    }
    
    private void restartApplication() {
        try {
            DcSwingUtilities.displayWarningMessage("msgRestoreFinishedRestarting");
            DataManager.deleteIcons();
            System.exit(0);
        } catch (Exception e) {
            listener.sendError(e);
        }
    }    
    
    private boolean isSupportedVersion(ZipFile zf) throws IOException {
        ZipEntry ze = zf.getEntry("version.txt");
        
        if (ze != null) {
            InputStream is = zf.getInputStream(ze);
            BufferedInputStream bis = new BufferedInputStream(is);
            
            StringBuffer sb = new StringBuffer();
            byte[] b = new byte[4096];
            for (int n; (n = bis.read(b)) != -1;) {
                String s = new String(b, 0, n);
                sb.append(s);
            }

            String s = sb.toString();
            s = s.indexOf("\n") > -1 ? s.substring(0, s.indexOf("\n")) : s;
            version = new Version(s);
            
            bis.close();
            is.close();
        }
        
        if (version == null || version.isUndetermined()) {
            if (!DcSwingUtilities.displayQuestion("msgCouldNotDetermineVersion"))
                return false;
        } else if (version != null && version.isOlder(new Version(3, 4, 13, 0))) {
            if (!DcSwingUtilities.displayQuestion("msgOldVersion3.4.12"))
                return false;
        } else if (version != null && version.isOlder(new Version(3, 8, 16, 0))) {
            if (!DcSwingUtilities.displayQuestion("msgOldVersion3.8.16"))
                return false;
        }
        
        return true;
    }
    
    private void clear() {
        listener.notifyStopped();
        version = null;
        source = null;
        listener = null;
    }
    
    /**
     * Returns the target file for the provided backup file entry.
     * This method should only be used for backups made with versions older then 3.9.16.
     */
    private String getTargetFile_older_V_3_9_16(String filename) {
        boolean isImage = filename.toLowerCase().endsWith(".jpg");
        boolean isReport = 
                filename.toLowerCase().endsWith(".xsl") || 
                filename.toLowerCase().endsWith(".xslt") ||
                filename.toLowerCase().indexOf("/reports") > -1;
        boolean isModule = filename.toLowerCase().indexOf("/modules") > -1;
        boolean isResource = filename.toLowerCase().endsWith(DcLanguageResource.suffix);
        boolean isData = !isImage && !isReport && !isModule && !isResource; 
        
        if (isImage && restoreDatabase) {
            filename = DataCrow.imageDir + filename.substring(filename.lastIndexOf("/") + 1, filename.length());
        } else if (isResource && restoreDatabase) {
            filename = DataCrow.resourcesDir + filename.substring(filename.lastIndexOf("/") + 1, filename.length());
        } else if (isReport && restoreReports) {
            filename = filename.substring(filename.lastIndexOf("/reports") + 1, filename.length());
            filename = DataCrow.userDir + filename;
        } else if (isModule && restoreModules) {
            filename = filename.substring(filename.lastIndexOf("/modules") + 1, filename.length());
            filename = DataCrow.userDir + filename;
        } else if (isData && restoreDatabase) {
            if (filename.endsWith("data_crow.properties")) {
                filename = new File(DataCrow.applicationSettingsDir, "data_crow.properties").toString();
            } else if (filename.endsWith("data_crow_queries.txt")) {
                filename = new File(DataCrow.applicationSettingsDir, "data_crow_queries.txt").toString();
            } else if (filename.endsWith("filepatterns.xml")) {
                filename = new File(DataCrow.applicationSettingsDir, "filepatterns.xml").toString();
            } else if (filename.endsWith("filters.xml")) {
                filename = new File(DataCrow.applicationSettingsDir, "filters.xml").toString();
            } else if (filename.endsWith("log4j.properties")) {
                filename = new File(DataCrow.applicationSettingsDir, "log4j.properties").toString();
            } else if (filename.endsWith("enhancers_autoincrement.properties")) {
                filename = new File(DataCrow.applicationSettingsDir, "enhancers_autoincrement.properties").toString();
            } else if (filename.endsWith("enhancers_titlerewriters.properties")) {
                filename = new File(DataCrow.applicationSettingsDir, "enhancers_titlerewriters.properties").toString();
            } else if (filename.endsWith("enhancers_associatenamerewriters.properties")) {
                filename = new File(DataCrow.applicationSettingsDir, "enhancers_associatenamerewriters.properties").toString();
            } else {
                boolean found = false;
                for (DcModule module : DcModules.getAllModules()) {
                    if (module.getName().length() > 0 && filename.endsWith(module.getName().toLowerCase() + ".properties")) {
                        filename = new File(DataCrow.moduleSettingsDir, module.getName().toLowerCase() + ".properties").toString();
                        found = true;
                    }
                }
    
                if (!found) {
                    if (filename.endsWith(".script")) 
                        filename = new File(DataCrow.databaseDir, filename.substring(filename.lastIndexOf("/") + 1, filename.length())).toString();
                    else if (filename.endsWith(".lck")) 
                        filename = new File(DataCrow.databaseDir, filename.substring(filename.lastIndexOf("/") + 1, filename.length())).toString();
                    else if (filename.endsWith(".log")) 
                        filename = new File(DataCrow.databaseDir, filename.substring(filename.lastIndexOf("/") + 1, filename.length())).toString();
                    else if (filename.endsWith(".properties")) 
                        filename = new File(DataCrow.databaseDir, filename.substring(filename.lastIndexOf("/") + 1, filename.length())).toString();
                    else if (filename.endsWith(".new")) 
                        filename = new File(DataCrow.databaseDir, filename.substring(filename.lastIndexOf("/") + 1, filename.length())).toString();
                }
            }
        } else {
            if (logger.isDebugEnabled())
                logger.debug("Skipping " + filename);
            
            filename = null;
        }    
        return filename;
    }
    
    /**
     * Returns the target file for the provided backup file entry.
     */
    private String getTargetFile(String filename) {
        boolean restore = true;
        if (    !restoreDatabase && 
               (filename.toLowerCase().startsWith("database/") ||
                filename.toLowerCase().startsWith("database\\")  ||
                filename.toLowerCase().indexOf("/mediaimages/") > -1 ||
                filename.toLowerCase().indexOf("\\mediaimages\\") > -1)) {
            restore = false;
        } else if (
                !restoreModules && 
               (filename.toLowerCase().startsWith("modules/") ||
                filename.toLowerCase().startsWith("modules\\"))) {    
            restore = false;
        } else if (
                !restoreReports && 
               (filename.toLowerCase().startsWith("reports/") ||
                filename.toLowerCase().startsWith("reports\\") ))   {
            restore = false;
        }
   
        return restore ? new File(DataCrow.userDir, filename).toString() : null;
    }  
    
    /**
     * Performs the actual restore. The listener is updated on errors and events.
     */
    @Override
    public void run() {
        
        try {
            ZipFile zf = new ZipFile(source);
            
            if (!isSupportedVersion(zf)) {
                clear();
                return;
            }
            
            Enumeration<? extends ZipEntry> list = zf.entries();

            listener.notifyStarted();
            listener.notifyProcessingCount(zf.size());

            listener.sendMessage(DcResources.getText("msgStartRestore"));
            listener.sendMessage(DcResources.getText("msgClosingDb"));
            DatabaseManager.closeDatabases(false);

            boolean success = true;
            while (list.hasMoreElements()) {
                ZipEntry ze = list.nextElement();
                String filename = ze.getName();
                try {

                    if (isVersion(filename)) 
                        continue;
                    
                    filename = version.isOlder(new Version(3, 9, 16, 0)) ? getTargetFile_older_V_3_9_16(filename) : getTargetFile(filename);

                    if (filename != null) {
                        File file = new File(filename);
                        
                        if (file.exists())
                            file.delete();
                        
                        if (file.exists())
                            listener.sendMessage(DcResources.getText("msgRestoreFileOverwriteIssue", filename.substring(filename.lastIndexOf("/") + 1)));
                        
                        try {
                            file.getParentFile().mkdirs();
                        } catch (Exception e) {
                            logger.warn("Unable to create directories for " + filename, e);
                        }
                        
                        InputStream istr = zf.getInputStream(ze);
                        BufferedInputStream bis = new BufferedInputStream(istr);
                        FileOutputStream fos = new FileOutputStream(filename);

                        int sz = (int)ze.getSize();
                        final int N = 1024;
                        byte buf[] = new byte[N];
                        int ln = 0;
                        while (sz > 0 &&  // workaround for bug
                            (ln = bis.read(buf, 0, Math.min(N, sz))) != -1) {
                                fos.write(buf, 0, ln);
                                sz -= ln;
                        }
                        bis.close();
                        fos.flush();
                        fos.close();
                        istr.close();

                        listener.sendMessage(DcResources.getText("msgRestoringFile", filename.substring(filename.lastIndexOf("/") + 1)));
                    }
                    
                    listener.notifyProcessed();
                
                } catch (Exception exp) {
                    success = false;
                    listener.sendMessage(DcResources.getText("msgRestoreFileError", new String[] {filename, exp.getMessage()}));
                }
            }
            
            if (!success)
                throw new Exception(DcResources.getText("msgIncompleteRestore"));

        } catch (Exception e) {
            listener.sendError(e);
        }

        restartApplication();
        listener.sendMessage(DcResources.getText("msgRestoreFinished"));
        
        clear();
    }
}
