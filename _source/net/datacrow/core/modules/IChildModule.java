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

package net.datacrow.core.modules;

import net.datacrow.console.windows.itemforms.DcMinimalisticItemView;
import net.datacrow.core.objects.DcObject;

public interface IChildModule {

    /**
     * Creates a (if needed) simple item view.
     * @param parent The parent item.
     * @param module
     * @param readonly Indicates if editing is allowed.
     */
    public DcMinimalisticItemView getItemView(DcObject parent, int module, boolean readonly);
    
    /**
     * Retrieves the index of the module (unique!)
     */    
    public int getIndex();
}
