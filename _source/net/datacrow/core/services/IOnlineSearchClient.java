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

package net.datacrow.core.services;

import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcObject;

public interface IOnlineSearchClient {

    public void addObject(DcObject dco);
    
    public void processing();
    
    public void stopped();
    
    public void addMessage(String message);
    
    public void addError(Throwable t);
    
    public void addError(String message);
    
    public void addWarning(String warning);
    
    public int resultCount();
    
    public DcModule getModule();
    
    public void processingTotal(int i);
    
    public void processed(int i);
}
