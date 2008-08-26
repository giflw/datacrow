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

package net.datacrow.console.components.renderers;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;

import net.datacrow.core.IconLibrary;

public class CheckBoxTableCellRenderer extends DcTableCellRenderer {
    
    private static final CheckBoxTableCellRenderer instance = new CheckBoxTableCellRenderer();
    
    private CheckBoxTableCellRenderer() {}
    
    public static CheckBoxTableCellRenderer getInstance() {
        return instance;
    }
    
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        JComponent c = (JComponent) super.getTableCellRendererComponent(table,  
                                            value,
                                            isSelected,
                                            hasFocus,
                                            row,
                                            column);        

        if (value != null) {
            setText("");
            boolean b = Boolean.valueOf(value.toString());
            if (b) 
                setIcon(IconLibrary._icoChecked);
            else
                setIcon(IconLibrary._icoUnchecked);
        } else {
            setIcon(IconLibrary._icoUnchecked);
        }
        return c;
    }
}
