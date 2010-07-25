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

package net.datacrow.util;

import net.datacrow.console.MainFrame;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class SystemMonitor extends Thread {

    private static Logger logger = Logger.getLogger(MainFrame.class.getName());
    
    private Runtime runtime;
    
    private boolean checkMem = true;
    
    public SystemMonitor() {
        runtime = Runtime.getRuntime();
        setName("System-Monitor-Thread");
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                sleep(60000);
            } catch (Exception e) {
                logger.error(e, e);
            }
            
            if (checkMem)
                checkMemory();
        }
    }
    
    private void checkMemory() {
        long max = Math.round(Math.round(runtime.maxMemory() / 1024) / 1024) + 1;
        long used = Math.round(Math.round(runtime.totalMemory() / 1024) / 1024) + 1;
        
        long available = max - used;

        logger.debug("Memory usage (max " + max + " MB) (used " + used + " MB) (available " + available + " MB)");

        if (max <= 65) {
            DcSwingUtilities.displayWarningMessage(DcResources.getText("msgMemory64MB", String.valueOf(max)));
            checkMem = false;
        }
    }
}
