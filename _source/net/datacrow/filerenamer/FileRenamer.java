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

package net.datacrow.filerenamer;

import java.io.File;
import java.io.IOException;

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.Utilities;

public class FileRenamer {

    private Task task;
    private static FileRenamer instance;
    
    public static FileRenamer getInstance() {
        instance = instance == null ? new FileRenamer() : instance;
        return instance;
    }
    
    private FileRenamer() {}
    
    public void start(IFileRenamerListener listener, File baseDir, FilePattern pattern, DcObject[] objects) {
        if (task == null || !task.isAlive()) {
            task = new Task(listener, baseDir, pattern, objects);
            task.start();
        }
    }
    
    public boolean isRunning() {
        return task != null && task.isAlive();
    }
    
    public void cancel() {
        if (task != null) task.cancel();
    }    
    
    private static class Task extends Thread {
        
        private boolean keepOnRunning = true;

        private IFileRenamerListener listener;
        
        private File baseDir;
        private FilePattern pattern;
        private DcObject[] objects;
        
        public Task(IFileRenamerListener listener, File baseDir, FilePattern pattern, DcObject[] objects) {
            this.listener = listener;
            this.pattern = pattern;
            this.objects = objects;
            this.baseDir = baseDir;
            
            this.setPriority(Thread.NORM_PRIORITY);
        }

        public void cancel() {
            keepOnRunning = false;
        }
        
        @Override
        public void run() {
            
            listener.notifyJobStarted();
            listener.notifyTaskSize(objects.length);
            
            for (DcObject dco : objects) {
                if (!keepOnRunning) break;
                
                String filename = dco.getFilename();
                File currentFile = new File(filename);
                if (!currentFile.exists()) {
                    listener.notify(DcResources.getText("msgFileDoesNotExist", currentFile.toString()));
                } else if (!currentFile.canWrite()) {
                    listener.notify(DcResources.getText("msgFileNotWritable", currentFile.toString()));                    
                } else {
                    filename = pattern.getFilename(dco, currentFile, baseDir);
                    
                    File newFile = new File(filename);
                    try {
                        Utilities.rename(currentFile, newFile);
                        
                        dco.setValue(DcObject._SYS_FILENAME, newFile.toString());
                        try {
                            dco.saveUpdate(false, false);
                        } catch (ValidationException ve) {
                            // validation exceptions can not occur as we save without validation..
                        }
                        
                        listener.notify(DcResources.getText("msgRenamedFileFromTo", 
                                        new String[] {currentFile.toString(), newFile.toString()}));
                    } catch (IOException ioe) {
                        listener.notify(ioe);
                    }
                }
                    
                listener.notifyProcessed();
            }
            
            listener.notifyJobStopped();
            
            listener = null;
            pattern = null;
            objects = null;
        }
    }
}
