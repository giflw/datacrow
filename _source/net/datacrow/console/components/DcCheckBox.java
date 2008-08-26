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

import java.awt.Graphics;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JToolTip;

import net.datacrow.util.DcSwingUtilities;

public class DcCheckBox extends JCheckBox implements IComponent {
    
    public DcCheckBox(String lbl) {
        super(lbl);
        setBackground(new JLabel().getBackground());
    }
    
    public void clear() {
        setSelected(false);
    }

    public Object getValue() {
        return Boolean.valueOf(isSelected());
    }

    public void setEditable(boolean b) {
        setEnabled(b);
    }

    public void setValue(Object value) {
        if (value != null) {
            Boolean b = value instanceof Boolean ? (Boolean) value : Boolean.valueOf((String) value);
            setSelected(b);
        }
    }

    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }    

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(DcSwingUtilities.setRenderingHint(g));
    }  
}
