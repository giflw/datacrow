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

package net.datacrow.console.components;

import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.data.DataManager;
import net.datacrow.core.objects.DcObject;

public class DcObjectComboBox extends DcComboBox implements IComponent {

    private final int module;
    
    public DcObjectComboBox(int module) {
        super();
        this.module = module;
    }
    
    @Override
    public void setValue(Object value) {
        setSelectedItem(value);
        
        if (value != null) {
            boolean exists = false;
            for (int i = 0; i < dataModel.getSize(); i++)
                if (value.equals(dataModel.getElementAt(i)))
                    exists = true;
            
            if (!exists) {
                addItem(value);
                setSelectedItem(value);
            }
        }
    }    

    @Override
    public void refresh() {
        Object o = getSelectedItem();
  
        Collection<DcObject> newValues = new ArrayList<DcObject>();
        for (int i = 0; i < dataModel.getSize(); i++) {
            Object value = dataModel.getElementAt(i);
            if (value instanceof DcObject && ((DcObject) value).isNew())
                newValues.add((DcObject) value);
        }
        
        removeAllItems();
        addItem(" ");
        
        for (DcObject dco : newValues)
            addItem(dco);

        for (DcObject dco :  DataManager.get(module, null))
            addItem(dco);

        if (o != null)
            setSelectedItem(o);
        else
            setSelectedIndex(0);
        
        revalidate();
    }
}
