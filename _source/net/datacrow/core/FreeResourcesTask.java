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

package net.datacrow.core;

import java.util.Date;

import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.settings.DcSettings;

import org.apache.log4j.Logger;

/**
 * Responsible for reclaiming used resources.
 * @author Robert Jan van der Waals.
 */
public class FreeResourcesTask {

    private static Logger logger = Logger.getLogger(FreeResourcesTask.class.getName());
    private static boolean done = false;
    private static Task instance;

    /**
     * Initializes this class and starts the monitoring processes.
     */
    public FreeResourcesTask() {
        int interval = DcSettings.getInt(DcRepository.Settings.stGarbageCollectionIntervalMs);
        scheduleRegularGC(interval);
    }    

    /**
     * Tries to reclaim memory every x milliseconds.
     * @param intervalMilliSecs
     */
    private void scheduleRegularGC(long intervalMilliSecs) {
        if (!done){ 
            if (intervalMilliSecs > 0) {
                Task task = getInstance();
                java.util.Timer scheduler = new java.util.Timer();
                scheduler.scheduleAtFixedRate(task, 10, intervalMilliSecs);
            }
            
            done = true;
        }
    }
    
    private static void suggestGCNow() {
    	long start = logger.isDebugEnabled() ? new Date().getTime() : 0;
        ImageCleaner.clean();
        debug(start, "Clearing images in store");

        start = logger.isDebugEnabled() ? new Date().getTime() : 0;
        for (DcObject dco : DataManager.get(DcModules._PICTURE, null))
            ((Picture) dco).unload();
        debug(start, "Unloading images");
        
        start = logger.isDebugEnabled() ? new Date().getTime() : 0;
        System.runFinalization();
        debug(start, "Running finalizers");
        
        start = logger.isDebugEnabled() ? new Date().getTime() : 0;
        System.gc();
        debug(start, "Calling System.gc()");
    }
    
    private static void debug(long start, String msg) {
        if (logger.isDebugEnabled()) {
            long end = new Date().getTime();
            logger.debug(msg + " took " + (end - start) + "ms");
        }
    }

    private static class Task extends java.util.TimerTask {
        @Override
        public void run() {
            suggestGCNow();
        }
    }

    private synchronized Task getInstance() {
        if (instance == null)
            instance = new Task();

        return instance;
    }
}
