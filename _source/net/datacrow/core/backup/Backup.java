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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.resources.DcLanguageResource;
import net.datacrow.core.resources.DcResources;
import net.datacrow.reporting.templates.ReportTemplates;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.Directory;

/**
 * Performs a backup of the Data Crow data, settings, modules and reports.
 * 
 * @author Robert Jan van der Waals
 */
public class Backup extends Thread {

    private File directory;
    private IBackupRestoreListener listener;
    private String comment;
 
    /**
     * Creates a new instance.
     * @param listener The listener which will be informed of events and errors.
     * @param directory The directory where the backup will be created.
     */
    public Backup(IBackupRestoreListener listener, File directory, String comment) {
        this.directory = directory;
        this.comment = comment;
        this.listener = listener;
    }
    
    /**
     * Retrieves all the files to be backed up.
     * @return A collection of fully classified filenames.
     */
    private Collection<String> getFiles() {
        File dataDir = new File(DataCrow.dataDir);
        String[] list = dataDir.list();
        Collection<String>  files = new ArrayList<String> ();
        for (int i = 0; i < list.length; i++) {
            String file = list[i];
            File fl = new File(dataDir, file);
            if (!fl.isDirectory() && !file.endsWith(".log") && !file.equals("images"))
                files.add(fl.toString());
        }

        Collection<String> resources = Directory.read(DataCrow.resourcesDir, true, false, null);
        for (String resource : resources) {
            if (resource.toLowerCase().endsWith(DcLanguageResource.suffix))
                files.add(resource);
        }
        
        files.addAll(
            Directory.read(DataCrow.moduleDir, true, false, null));
        
        files.addAll(
            Directory.read(DataCrow.imageDir, true, false, new String[] {"jpg", "jpeg"}));        
        
        for (String reportDir : new ReportTemplates(true).getFolders())
            files.addAll(Directory.read(reportDir, true, false, new String[] {"xsl", "xslt"}));
        
        return files;
    }

    private ZipOutputStream getZipOutputStream(String target) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmm");
        String date = format.format(cal.getTime());

        String filename = "datacrow_backup_" + date + ".bck";
        String zipFile = target.endsWith(File.separator) ? target + filename :
                                                           target + File.separator + filename;
        ZipOutputStream zout = null;
        try {
            FileOutputStream fos = new FileOutputStream(zipFile);
            zout = new ZipOutputStream(fos);
        } catch (Exception e) {
            listener.sendError(e);
        }

        return zout;
    }    
    
    /**
     * Performs the actual back up and informs the listener on the progress.
     */
    @Override
    public void run() {
        listener.notifyStarted();
        
        if (!directory.exists())
            directory.mkdirs();
        
        listener.sendMessage(DcResources.getText("msgStartBackup"));
        DatabaseManager.closeDatabases(true);
        listener.sendMessage(DcResources.getText("msgClosingDb"));

        try {
            Collection<String> files = getFiles();
            listener.notifyProcessingCount(files.size());
            
            byte b[] = new byte[512];
            ZipOutputStream zout = getZipOutputStream(directory.toString());
            
            if (zout != null) {
                ZipEntry versionEntry = new ZipEntry("version.txt");
                zout.putNextEntry(versionEntry);
                String version = String.valueOf(DataCrow.getVersion().toString());
                zout.write(version.getBytes(), 0, version.getBytes().length);
                
                if (comment.length() > 0)
                    zout.write(("\n" + comment).getBytes(), 0, ("\n" + comment).getBytes().length);
                
                zout.closeEntry();
                
                for (String file : files) {
                    if (!file.endsWith(".log")) {
                        InputStream in = new FileInputStream(file);
                        ZipEntry e = new ZipEntry(file.replace(File.separatorChar, '/'));
                        zout.putNextEntry(e);
                        int len = 0;
                        while ((len = in.read(b)) != -1) {
                            zout.write(b, 0, len);
                        }

                        listener.sendMessage(DcResources.getText("msgCreatingBackupOfFile", file));

                        in.close();
                        zout.closeEntry();
                    }

                    listener.notifyProcessed();
                }
                
                zout.close();
                listener.sendMessage(DcResources.getText("msgWritingBackupFile"));
            }
        } catch (Exception e) {
            listener.sendMessage(DcResources.getText("msgBackupError", e.getMessage()));
            listener.sendError(e);
        }

        DcSettings.set(DcRepository.Settings.stBackupLocation, directory.toString());
        
        listener.sendMessage(DcResources.getText("msgRestartingDb"));
        DatabaseManager.initialize();
        listener.sendMessage(DcResources.getText("msgBackupFinished"));
        listener.notifyStopped();
        
        listener = null;
        directory = null;
    }
}
