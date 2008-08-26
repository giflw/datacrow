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

package net.datacrow.drivemanager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.datacrow.core.DataCrow;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.Hash;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * The Drive Manager of Data Crow. Schedules tasks to scan drives for files,
 * allows current file locations to be retrieved and checks the system for new
 * mounted / inserted discs. 
 */
public class DriveManager {

    public static final int _PRECISION_LOWEST = 0;
    public static final int _PRECISION_MEDIUM = 1;
    public static final int _PRECISION_HIGHEST = 2;
    
    private static Logger logger = Logger.getLogger(DriveManager.class);

    private static DriveManager instance = new DriveManager();
    
    private FileSynchronizer fs;
    private DrivePoller dp;

    private Collection<File> drives;
    private Collection<String> excludedDirs = new ArrayList<String>();
    private Map<File, DriveScanner> scanners = new HashMap<File, DriveScanner>();
    
    private Collection<IDriveManagerListener> pollerListeners = new ArrayList<IDriveManagerListener>();
    private Collection<IDriveManagerListener> scannerListeners = new ArrayList<IDriveManagerListener>();
    private Collection<IDriveManagerListener> synchronizerListeners = new ArrayList<IDriveManagerListener>();
    
    public static DriveManager getInstance() {
        return instance;
    }
    
    private DriveManager()  {
        clean();
    }
    
    public void startDrivePoller() throws JobAlreadyRunningException {
        if (dp != null && dp.isRunning()) {
            throw new JobAlreadyRunningException();
        } else {
            dp = dp == null ? new DrivePoller() : dp;
            dp.start();
        }
    }
    
    public void startFileSynchronizer(int precision) throws JobAlreadyRunningException {
        if (fs != null && fs.isRunning()) {
            throw new JobAlreadyRunningException();
        } else {
            fs = fs == null ? new FileSynchronizer() : fs;
            fs.start(precision);
        }
    }    
    
    public void sendMessage(Collection<IDriveManagerListener> listeners, String msg) {
        for (IDriveManagerListener listener : listeners)
            listener.notify(msg);
    }

    public void notifyJobStopped(Collection<IDriveManagerListener> listeners) {
        for (IDriveManagerListener listener : listeners)
            listener.notifyJobStopped();
    }

    public void notifyJobStarted(Collection<IDriveManagerListener> listeners) {
        for (IDriveManagerListener listener : listeners)
            listener.notifyJobStarted();
    }
    
    public void restartScan(File drive) {
        if (getDrives().contains(drive)) {
            notifyJobStarted(getScannerListeners());
            DriveScanner scanner = scanners.get(drive);
            scanner.restart();
        }
    }
    
    public void startScanners() throws JobAlreadyRunningException {
        if (isScanActive()) 
            throw new JobAlreadyRunningException();
        
        initializeScanners();
        notifyJobStarted(getScannerListeners());
        
        for (File drive : getDrives()) {
            if (Utilities.isDriveTraversable(drive) && drive.canRead()) {
                scanners.get(drive).cancel();
                scanners.get(drive).start();
            } else { 
                sendMessage(getScannerListeners(), DcResources.getText("msgSkippingUnreadbleDrive", drive.toString()));
            }
        }
    }    
    
    public void stopScanners() {
        for (DriveScanner scanner : scanners.values()) 
            scanner.cancel();
        
        notifyJobStopped(getScannerListeners());
    }

    public void stopDrivePoller() {
        if (dp != null) dp.cancel();
    }

    public void stopFileSynchronizer() {
        if (fs != null) fs.cancel();
    }    
    
    public void setDrives(Collection<File> drives) throws JobAlreadyRunningException {
        if (isScanActive()) 
            throw new JobAlreadyRunningException();
            
        this.drives = drives;
    }
    
    public Collection<String> getExcludedDirs() {
        return excludedDirs;
    }    
    
    protected String getTempFileSuffix() {
        return "_drive_report.properties";
    }
    
    protected String getTempDir() {
        return DataCrow.baseDir + "data/temp/";
    }    
    
    private void initializeScanners() {
        for (File drive : getDrives()) {
            try {
                if (scanners.get(drive) == null) 
                    scanners.put(drive, new DriveScanner(this, drive));    
            } catch (Exception e) {
                logger.error(e, e);
            }      
        }
    }

    public Collection<IDriveManagerListener> getPollerListeners() {
        return pollerListeners;
    }

    public Collection<IDriveManagerListener> getScannerListeners() {
        return scannerListeners;
    }

    public Collection<IDriveManagerListener> getSynchronizerListeners() {
        return synchronizerListeners;
    } 
    
    private void clean() {
        File file = new File(getTempDir());
        for (String filename : file.list()) {
            if (filename.endsWith(getTempFileSuffix())) {
                File tempFile = new File(getTempDir() + filename);
                if (tempFile.isFile())
                    tempFile.delete();
            }
        }
    }    
    
    public synchronized Collection<File> getDrives() {
        if (drives == null || drives.size() == 0) {
            drives = new ArrayList<File>();
            File[] roots = File.listRoots();
            for (File root : roots) {
                if (!Utilities.isFloppyDrive(root) && Utilities.isDriveTraversable(root))
                    drives.add(root);
            }
        }
        return drives;
    }

    public boolean isScanActive() {
        boolean running = false;
        for (DriveScanner scanner : scanners.values())
            running |= scanner.isRunning();
        
        return running;
    }
    
    protected void notifyScanComplete(DriveScanner scanner) {
        sendMessage(getScannerListeners(), 
                    DcResources.getText("msgScanHasCompletedForX", "" + scanner.getDrive()));

        scanner.cancel();
        if (!isScanActive())
            notifyJobStopped(getScannerListeners());
    }
    
    public void setExcludedDirectories(Collection<String> directories) throws JobAlreadyRunningException {
        if (isScanActive()) 
            throw new JobAlreadyRunningException();
        
        this.excludedDirs = directories;
    }
    
    protected boolean isDirExcluded(File directory) {
        return excludedDirs.contains(directory.toString());
    }
    
    public void exclude(String directory) {
        String dir = directory.toLowerCase();
        dir = dir.replaceAll("'\'", File.separator);
        dir = dir.replaceAll("'/'", File.separator);
        excludedDirs.add(dir);
    }    
    
    private FileInfo getFileInfo(File file, String hash, Long size) {
        hash = hash == null ? Hash.getInstance().calculateHash(file.toString()) : hash;
        size = size == null ? Utilities.getSize(file) : size;
        return new FileInfo(hash, file.toString(), size);        
    }

    public void addPollerListener(IDriveManagerListener listener) {
        pollerListeners.add(listener);
    }

    public void addScannerListener(IDriveManagerListener listener) {
        scannerListeners.add(listener);
    }

    public void addSynchronizerListener(IDriveManagerListener listener) {
        synchronizerListeners.add(listener);
    }
    
    /**
     * Retrieves the actual location / file for the given filename.
     * The size and the hash of the original file are used to determine its new
     * location.
     */
    public FileInfo find(FileInfo fi, int precision) {
        String filename = fi.getFilename();
        File file = new File(fi.getFilename());
        
        if (file.exists())
            return getFileInfo(file, fi.getHash(), fi.getSize());
        
        // The precision determines on which a file is allowed to be matched.
        // skip this file if there is not enough information for the given precision level.
        if (!fi.valid(precision))
            return null;
            
        try {
	        String name =  filename.indexOf("/") > 0 ?  filename.substring(filename.lastIndexOf("/")) : 
	                       filename.indexOf("\\") > 0 ? filename.substring(filename.lastIndexOf("\\")) :
	                       filename;
	        
	        FileInfo result = null;
	        for (String propertyFile : new File(getTempDir()).list()) {
	            if (!propertyFile.endsWith(getTempFileSuffix())) continue;
	            
	            File tmpFile = new File(getTempDir() + propertyFile);
	            if (!tmpFile.canRead()) continue;
	            
                RandomAccessFile raf = new RandomAccessFile(tmpFile, "r");
	            long length = raf.length();
	            
	            while (raf.getFilePointer() < length) {
	                try {
	                    String line = raf.readLine();
	                    int idx = line.indexOf("=");
	                    
	                    Long fodSize = Long.valueOf(line.substring(0, idx));
	                    String fodName = line.substring(idx + 1);
	                    File fod = new File(fodName);
                        
                        // make sure the file exists; might be dealing with info from an
                        // unmounted drive!
                        if (fod.exists()) {
    	                    if (fi.getSize() == null) {
    	                        if (name.equals(fodName)) {
    	                            result = getFileInfo(fod, null, null);
    	                            break;
    	                        }
    	                        
    	                    } else if (fi.getSize().equals(fodSize)) {
                                String newHash = Hash.getInstance().calculateHash(fod.toString());
    	                        if (fi.getHash() == null) {
    	                            if (fod.getName().equalsIgnoreCase(name)) { 
    	                                result = getFileInfo(fod, newHash, Utilities.getSize(fod));
    	                                break;
    	                            }
    	                        } else if (newHash.equals(fi.getHash())) {
                                    result = getFileInfo(fod, newHash, Utilities.getSize(fod));
                                    break;
    	                        }
    	                    }
                        }
		            } catch (IOException ioe) {
		                logger.error(ioe, ioe);
		            } 
	            }
                
                try {
                    raf.close();
                } catch (IOException ioe) {
                    logger.error("Could not close RAF", ioe);
                }
                
                if (result != null) 
                    return result;
	        }
        } catch (Exception e) {
        	logger.error(e, e);
        }
        
        return null;
    }
}
