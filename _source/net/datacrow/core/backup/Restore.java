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

import javax.swing.SwingUtilities;

import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.windows.messageboxes.QuestionBox;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.ModuleUpgrade;
import net.datacrow.core.Version;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcLanguageResource;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.enhancers.ValueEnhancers;
import net.datacrow.filerenamer.FilePatterns;
import net.datacrow.settings.DcSettings;

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
    
    private boolean isImage(String filename) {
        return filename.toLowerCase().endsWith(".jpg");
    }
    
    private boolean isResource(String filename) {
        return filename.toLowerCase().endsWith(DcLanguageResource.suffix);
    }
    
    private boolean isVersion(String filename) {
        return filename.toLowerCase().equals("version.txt");
    }
    
    private boolean isReport(String filename) {
        return filename.toLowerCase().endsWith(".xsl") || filename.toLowerCase().endsWith(".xslt") ||
               filename.toLowerCase().indexOf("/reports") > -1;
    }
    
    private boolean isModule(String filename) {
        return filename.toLowerCase().indexOf("/modules") > -1;
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
    
    private static class InterfaceUpdater implements Runnable {
        private IBackupRestoreListener listener;
        
        public InterfaceUpdater(IBackupRestoreListener listener) {
            this.listener = listener;
        }
        
        public void run() {
            DataCrow.mainFrame.applySettings();
            DataCrow.mainFrame.updateLAF(DcSettings.getLookAndFeel(DcRepository.Settings.stLookAndFeel));
            listener.notifyProcessed();
            listener = null;
        }
    }
    
    private static class ItemLoader implements Runnable {
        private IBackupRestoreListener listener;
        
        public ItemLoader(IBackupRestoreListener listener) {
            this.listener = listener;
        }
        
        public void run() {
            listener.sendMessage("Loading items");
            DcModules.loadData();
            listener.notifyProcessed();
            
            DataCrow.mainFrame.changeModule(DcSettings.getInt(DcRepository.Settings.stModule));
            DataCrow.mainFrame.setViews();
            
            for (DcModule module : DcModules.getAllModules()) {
                if (module.getSearchView() != null)
                    module.getSearchView().clear();    
            }
            
            DataManager.bindData(DcModules.getCurrent().getSearchView(), 
                                 DataManager.get(DcSettings.getInt(DcRepository.Settings.stModule), null));
            
            listener = null;
        }
    }    
    
    private void restartApplication() {

        try {
            listener.notifyProcessingCount(3);
            
            listener.sendMessage(DcResources.getText("msgRestartingDb"));
            
            DataManager.unload();
            DataManager.setUseCache(false);
            
            SecurityCentre.getInstance().initialize();
            
            if (version == null || version.isOlder(DataCrow.getVersion()) || !SecurityCentre.getInstance().unsecureLogin()) {
                new MessageBox(DcResources.getText("msgRestoreFinishedRestarting"), MessageBox._WARNING);
                
                // do not save settings as the restored settings are used for upgrading purposes.
                DataManager.clearCache();
                DataCrow.mainFrame.finish(false, false);
                System.exit(0);
            }
            
            new ModuleUpgrade().upgrade();
            DcModules.load();
            
            ValueEnhancers.initialize();
            DatabaseManager.initialize();

            listener.notifyProcessed();
            listener.sendMessage(DcResources.getText("msgReapplyingSettings"));
            
            new DcSettings();
            new DcResources();
            
            DcModules.applySettings();
            DataFilters.load();
            FilePatterns.load();
            
            SwingUtilities.invokeLater(new InterfaceUpdater(listener));
            SwingUtilities.invokeLater(new ItemLoader(listener));        

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
            for (int n; (n = bis.read(b)) != -1;)
                sb.append(new String(b, 0, n));

            version = new Version(sb.toString());
            
            bis.close();
            is.close();
        }
        
        if (version == null || version.isUndetermined()) {
            QuestionBox qb = new QuestionBox(DcResources.getText("msgCouldNotDetermineVersion"));
            if (!qb.isAffirmative())
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
                    
                    boolean isImage = isImage(filename);
                    boolean isReport = isReport(filename);
                    boolean isModule = isModule(filename);
                    boolean isResource = isResource(filename);
                    boolean isData = !isImage && !isReport && !isModule && !isResource; 
                    
                    boolean restore = true;
                    
                    if (isImage && restoreDatabase) {
                        filename = DataCrow.imageDir + filename.substring(filename.lastIndexOf("/") + 1, filename.length());
                    } else if (isResource && restoreDatabase) {
                        filename = DataCrow.resourcesDir + filename.substring(filename.lastIndexOf("/") + 1, filename.length());
                    } else if (isReport && restoreReports) {
                        filename = filename.substring(filename.lastIndexOf("/reports") + 1, filename.length());
                        filename = DataCrow.baseDir + filename;
                    } else if (isModule && restoreModules) {
                        filename = filename.substring(filename.lastIndexOf("/modules") + 1, filename.length());
                        filename = DataCrow.baseDir + filename;
                    } else if (isData && restoreDatabase) {
                        filename = DataCrow.baseDir + "data/" + 
                                   filename.substring(filename.lastIndexOf("/") + 1, filename.length());
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("Skipping " + filename);

                        restore = false;
                    }
                    
                    if (restore) {
                        try {
                            new File(filename.substring(0, filename.lastIndexOf("/"))).mkdirs();
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
