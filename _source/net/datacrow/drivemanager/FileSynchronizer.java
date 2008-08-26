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

import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class FileSynchronizer {

    private static Logger logger = Logger.getLogger(FileSynchronizer.class.getName());
    private Task task;
    private Collection<DcModule> modules = new ArrayList<DcModule>();
    
    protected FileSynchronizer() {
        for (DcModule module : DcModules.getModules()) {
            if (module.isFileBacked())
                modules.add(module);
        }
    }
    
    public boolean start(int precision) {
        if (task == null || !task.isAlive()) {
            task = new Task(this, precision);
            task.start();
            return true;
        }
        return false;
    }
    
    public boolean isRunning() {
        return task != null && task.isAlive();
    }
    
    public void cancel() {
        if (task != null) task.cancel();
    }

    protected Collection<DcModule> getModules() {
        return modules;
    }    
    
    private static class Task extends Thread {
        
        private boolean keepOnRunning = true;
        private FileSynchronizer fs;
        private int precision;
        
        public Task(FileSynchronizer fs, int precision) {
            this.fs = fs;
            this.precision = precision;
            
            setPriority(Thread.MIN_PRIORITY);
        }
        
        public void cancel() {
            keepOnRunning = false;
        }

        @Override
        public void run() {
            DriveManager dm = DriveManager.getInstance();

            dm.notifyJobStarted(dm.getSynchronizerListeners());
            
            while (keepOnRunning) {
                for (DcModule module : fs.getModules()) {
                    
                    if (!keepOnRunning) break;
                    
                    DataFilter df = new DataFilter(module.getIndex());
                    df.addEntry(new DataFilterEntry(DataFilterEntry._AND, 
                                                    module.getIndex(), 
                                                    DcObject._SYS_FILENAME, 
                                                    Operator.IS_FILLED, null));
                    
                    if (precision >= DriveManager._PRECISION_MEDIUM)
                        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, 
                                    module.getIndex(), 
                                    DcObject._SYS_FILESIZE, 
                                    Operator.IS_FILLED, null));

                    if (precision == DriveManager._PRECISION_HIGHEST)
                        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, 
                                    module.getIndex(), 
                                    DcObject._SYS_FILEHASH, 
                                    Operator.IS_FILLED, null));
                    
                    for (DcObject dco : DataManager.get(module.getIndex(), df)) {
                        
                        if (!keepOnRunning) break;
                        
                        String filename = (String) dco.getValue(DcObject._SYS_FILENAME);
                        String hash = (String) dco.getValue(DcObject._SYS_FILEHASH);
                        Long size = (Long) dco.getValue(DcObject._SYS_FILESIZE);
                        
                        FileInfo currentFI = new FileInfo(hash, filename, size);
                        FileInfo fi = dm.find(currentFI, precision);
                        if (fi != null && !currentFI.equals(fi)) {
                            dco.setValue(DcObject._SYS_FILENAME, fi.getFilename());
                            dco.setValue(DcObject._SYS_FILESIZE, fi.getSize());
                            dco.setValue(DcObject._SYS_FILEHASH, fi.getHash());
                            
                            try {
                                dco.setSilent(true);
                                dco.saveUpdate(true);
                                
                                String message = DcResources.getText("msgSynchronizedFile", 
                                        new String[] {dco.toString(), fi.getFilename()}); 
                                dm.sendMessage(dm.getSynchronizerListeners(), message);
                            } catch (ValidationException ve) {
                                dm.sendMessage(dm.getSynchronizerListeners(),
                                        DcResources.getText("msgSynchronizerCouldNotSave", dco.toString()));
                            }
                        }
                        
                        currentFI.clear();
                        if (fi != null)
                            fi.clear();
                        
                        try {
                            sleep(2000);
                        } catch (Exception e) {
                            logger.error(e, e);
                        }
                    }
                }
            }
            
            fs = null;
            dm.notifyJobStopped(dm.getSynchronizerListeners());
        }
    }
}
