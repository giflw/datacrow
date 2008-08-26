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

import java.util.Collection;

import net.datacrow.core.objects.DcObject;

public class DataTask extends Thread {

    protected DcObject[] objects;
    protected int[] uiRows;

    private boolean isRunning = false;
    private boolean keepOnRunning = true;

    public DataTask(Collection<? extends DcObject> objects) {
        this.objects = objects.toArray(new DcObject[0]);
    }

    public DataTask() {
        setPriority(Thread.NORM_PRIORITY);
    }
    
    public DataTask(int[] rows) {
        this.uiRows = rows;
    }
    
    public void cancel() {
    	keepOnRunning = false;
    }

    public void stopRunning() {
        keepOnRunning = false;
    	isRunning = false;
        objects = null;
        uiRows = null;
    }

    public void startRunning() {
        isRunning = true;
        keepOnRunning = true;
    }

    public boolean isRunning() {
    	return isRunning;
    }

    public boolean keepOnRunning() {
    	return keepOnRunning;
    }
}
