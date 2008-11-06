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

import org.apache.log4j.Logger;

import net.datacrow.console.MainFrame;
import net.datacrow.console.windows.messageboxes.MessageBox;

public class MemoryMonitor extends Thread {

    private static Logger logger = Logger.getLogger(MainFrame.class.getName());
    
    private Runtime runtime;
    
    public MemoryMonitor() {
        runtime = Runtime.getRuntime();
    }
    
    @Override
    public void run() {
        
        while (true) {
            
            try {
                sleep(2000);
            } catch (Exception ignore) {}
            
            long max = Math.round(Math.round(runtime.maxMemory() / 1024) / 1024) + 1;
            long used = Math.round(Math.round(runtime.totalMemory() / 1024) / 1024) + 1;
            
            long available = max - used;

            logger.debug("Memory usage (max " + max + " MB) (used " + used + " MB) (available " + available + " MB)");
            
            if (max <= 65) {
                new MessageBox("Data Crow has too less memory allocated by Java (" + max + " MB). " +
                               "This will cause Data Crow to become very sluggish! " +
                               "You should consider to allow Data Crow to use more memory. " +
                               "This can be done by starting Data Crow in the following way: " +
                               "\r\njava -Xmx256m -jar datacrow.jar", MessageBox._WARNING);
                break;
            }
            
            if (available < 1) {
                new MessageBox("Only " + available + "MB memory is available (maximum is " + max + " MB)! " +
                               "You should consider to allow Data Crow to use more memory. " +
                		       "This can be done by starting Data Crow in the following way: " +
                		       "java -Xmx2562m -jar datacrow.jar", MessageBox._WARNING);
                break;
            }
        }
    }
}
