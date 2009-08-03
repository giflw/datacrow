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

package net.datacrow.fileimporters;

import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcProperty;
import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.plugin.IServer;

/**
 * This client can be updated on events and results form a file import process.
 * 
 * @see FileImporter
 * @author Robert Jan van der Waals
 */
public interface IFileImportClient {
    
    /**
     * Adds a messages.
     * @param message
     */
    public void addMessage(String message);
    
    /**
     * Adds an error.
     * @param e
     */
    public void addError(Throwable e);
    
    /**
     * Sets the expected result count.
     * @param max
     */
    public void initProgressBar(int max);
    
    /**
     * Updates the progress bar to the specified value.
     * @param value
     */
    public void updateProgressBar(int value);
    
    /**
     * Indicates the process has been canceled.
     */
    public boolean cancelled();

    /**
     * Indicates if online services should be used.
     */
    public boolean useOnlineServices();

    /**
     * Indicate the process has finished.
     */
    public void finish();
    
    /**
     * The used search mode.
     * @return The search mode or null.
     */
    public SearchMode getSearchMode();
    
    /**
     * The used server.
     */
    public IServer getServer();

    /**
     * The used region.
     * @return The region or null.
     */
    public Region getRegion();
    
    /**
     * The container to which the resulted items are added.
     * @return A container or null.
     */
    public DcObject getDcContainer();
    
    /**
     * The storage medium to apply on the resulted items.
     * @return A storage medium or null.
     */
    public DcProperty getStorageMedium();
    
    /**
     * The directory usage implementation (free form).
     */
    public int getDirectoryUsage();
    
    public DcModule getModule();
}
