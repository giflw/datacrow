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

package net.datacrow.console.components.lists.elements;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DcRepository;
import net.datacrow.settings.DcSettings;

public abstract class DcListElement extends JPanel {
    
    protected abstract void build();
    
    public void update() {
        clear();
        build();
        revalidate();
    }  
    
    public void destroy() {
        clear();
    }    
    
    @Override
    public void setFont(Font font) {
        Font fontNormal = DcSettings.getFont(DcRepository.Settings.stSystemFontNormal);
        Font fontSystem = DcSettings.getFont(DcRepository.Settings.stSystemFontNormal);        
        
        super.setFont(fontNormal);
        Component component;
        Component subComponent;
        for (int i = 0; i < getComponents().length; i++) {
            component = getComponents()[i];
            if (component instanceof JPanel) {
                
                for (int j = 0; j < ((JPanel) component).getComponents().length; j++) {
                    subComponent = ((JPanel) component).getComponents()[j];
                    if (subComponent instanceof JLabel) 
                        subComponent.setFont(fontSystem);
                    else
                        subComponent.setFont(fontNormal);
                }
            }
        }
    }  

    public void clear() {
        ComponentFactory.clean(this);
        
        if (getParent() != null)
            getParent().remove(this);
    }
}
