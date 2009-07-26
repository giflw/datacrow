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

package net.datacrow.synchronizers;

import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.plugin.IServer;

public interface ISynchronizerClient {
    
    public boolean isCancelled();
    
    public void initProgressBar(int max);

    public void updateProgressBar();
    
    public void addMessage(String message);
    
    public boolean isReparseFiles();

    public boolean useOnlineService();
    
    public void enableActions(boolean b);

    public void initialize();
    
    public IServer getServer();

    public Region getRegion();

    public SearchMode getSearchMode();
    
    public int getItemPickMode();
}
