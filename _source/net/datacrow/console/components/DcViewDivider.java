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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.border.Border;

import net.datacrow.settings.DcSettings;

public class DcViewDivider extends JSplitPane implements ComponentListener {
    
    private final String settingKey;
    
    public DcViewDivider(JComponent left, JComponent right, String settingKey) {
        super(JSplitPane.HORIZONTAL_SPLIT, left, right);
        this.settingKey = settingKey;
        left.addComponentListener(this);
                
        Border border = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        setBorder(border);
        
        applyDividerLocation();
    }
    
    public void applyDividerLocation() {
        if (isEnabled()) {
            removeComponentListener(this);
            setDividerLocation(DcSettings.getInt(settingKey));
            addComponentListener(this);
        }
    }
    
    public void listenForResize(boolean b) {
        if (!b) {
            removeComponentListener(this);
        } else {
            removeComponentListener(this);
            addComponentListener(this);
        }
    }

    @Override
    public void componentHidden(ComponentEvent ce) {}
    @Override
    public void componentMoved(ComponentEvent ce) {}
    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentResized(ComponentEvent ce) {
        if (	getLeftComponent() != null && 
        		getLeftComponent().isVisible() && 
        		getRightComponent() != null && 
        		getRightComponent().isVisible())
        	
            DcSettings.set(settingKey, getDividerLocation());
    }
}
