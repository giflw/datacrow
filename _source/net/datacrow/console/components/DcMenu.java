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

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;

import net.datacrow.console.ComponentFactory;
import net.datacrow.util.DcSwingUtilities;

public class DcMenu extends JMenu {
    
    public DcMenu(String text) {
        super(text);
        setFont(ComponentFactory.getSystemFont());
    }
    
    @Override
    public void addSeparator() {
        JPopupMenu menu = getPopupMenu();
        if (menu.getComponentCount() > 0) {
            Component[] c = menu.getComponents();
            if (c[c.length - 1] instanceof JMenuItem) 
                super.addSeparator();
        }
    }

    @Override
    public JMenuItem add(JMenuItem menuItem) {
        if (menuItem != null) 
            return super.add(menuItem);
        
        return menuItem;
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
