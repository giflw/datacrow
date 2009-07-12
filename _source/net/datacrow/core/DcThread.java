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

public class DcThread extends Thread {
    
    private boolean canceled = false;

    public DcThread(ThreadGroup group, String name) {
        super(group, name);
    }

    public void cancel() {
        canceled = true;
    }
    
    public boolean isCanceled() {
        return canceled;
    }

    public void cancelOthers() {
        DcThread[] threads = new DcThread[getThreadGroup().activeCount() * 2];
        getThreadGroup().enumerate(threads, false);
        for (DcThread thread : threads) {
            if (thread != this && thread != null)
                thread.cancel();
        }
        
        while (getThreadGroup().activeCount() > 1) {
            try {
                sleep(100);
            } catch (Exception e) {}
        }
    }
}
