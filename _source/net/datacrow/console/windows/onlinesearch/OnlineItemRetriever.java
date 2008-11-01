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

package net.datacrow.console.windows.onlinesearch;

import javax.swing.SwingUtilities;

import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.services.SearchTask;

import org.apache.log4j.Logger;

/**
 * Call from another thread and wait for this task to finish
 */
public class OnlineItemRetriever extends Thread {

    private static Logger logger = Logger.getLogger(ProgressDialog.class.getName());

    
    private SearchTask task;
    private DcObject dco;

    public OnlineItemRetriever(SearchTask task, DcObject dco) {
        this.task = task;
        this.dco = dco;
        
        setPriority(Thread.MIN_PRIORITY);
    }

    public DcObject getDcObject() {
        return dco;
    }
    
    @Override
    public void run() {
        PollerTask poller = new PollerTask(this);
        poller.start();
        
        try {
            dco = task.query(dco);
        } catch (Exception e) {
            new MessageBox(e.getMessage(), MessageBox._ERROR);
            logger.error(e, e);
        }
    }

    private class PollerTask extends Thread {
        
        private Thread thread;
        
        public PollerTask(Thread thread) {
            this.thread = thread;
            setPriority(Thread.MIN_PRIORITY);
        }
        
        @Override
        public void run() {
            
            final ProgressDialog dlg = new ProgressDialog(dco.getName());
            while (thread.isAlive()) {
                SwingUtilities.invokeLater(
                        new Thread(new Runnable() { 
                            public void run() {
                                dlg.update();                                
                            }
                        }));
                
                try { sleep(10); } catch (Exception ignore) {}
            }
            
            SwingUtilities.invokeLater(
                new Thread(new Runnable() { 
                    public void run() {
                        dlg.close();
                    }
                }));
        }
    }
}
