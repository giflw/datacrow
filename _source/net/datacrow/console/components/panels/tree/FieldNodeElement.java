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

package net.datacrow.console.components.panels.tree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import net.datacrow.core.data.DataManager;

public class FieldNodeElement extends NodeElement {
    
    private int field;        
    
    public FieldNodeElement(int module, int field, Object key, ImageIcon icon) {
        super(module, key, icon);
        this.field = field;
    }
    
    @Override
    public List<Long> getItems() {
        //DataManager.getKeys(getModule(), new DataFilter(dco))
        
        return new ArrayList<Long>();
    }

    @Override
    public int getCount() {
        return DataManager.getCount(module, field, key);
    }
}
