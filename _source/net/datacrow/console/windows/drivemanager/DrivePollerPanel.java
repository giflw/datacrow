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

package net.datacrow.console.windows.drivemanager;

import javax.swing.ImageIcon;

import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcResources;
import net.datacrow.drivemanager.DriveManager;
import net.datacrow.drivemanager.JobAlreadyRunningException;

public class DrivePollerPanel extends DriveManagerPanel {

    public DrivePollerPanel() {
        super();
        DriveManager.getInstance().addPollerListener(this);
    }
    
    @Override
    protected String getHelpText() {
        return DcResources.getText("msgDrivePollerHelp");
    }

    @Override
    protected ImageIcon getIcon() {
        return IconLibrary._icoDrivePoller;
    }

    @Override
    protected String getTitle() {
        return DcResources.getText("lblDrivePoller");
    }    
    
    @Override
    protected void start() throws JobAlreadyRunningException {
        DriveManager.getInstance().startDrivePoller();
    }
    
    @Override
    protected void stop() {
        DriveManager.getInstance().stopDrivePoller();
    }    
}
