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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collection;

import javax.swing.JPanel;

import net.datacrow.console.components.DcLabel;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.settings.DcSettings;

/**
 * A list element which is capable of displaying a DcObject.
 * 
 * @author Robert Jan van der Waals
 */
public abstract class DcObjectListElement extends DcListElement {

    protected static final int fieldHeight = 21;
    protected DcObject dco;
    
    protected DcObjectListElement() {}
    
    public DcObjectListElement(DcObject dco) {
        this.dco = dco;
    }

    public DcObject getDcObject() {
        return dco;
    }
    
    public abstract Collection<Picture> getPictures();
    
    public void update(DcObject dco) {
        clear();
    }    
    
    @Override
    public void setForeground(Color fg) {
    	for (Component c : getComponents()) {
    		c.setForeground(fg);
    	}
    }

    public JPanel getPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        return panel;
    }
    
    protected DcLabel getLabel(int field, boolean label, int width) {
        DcLabel lbl = new DcLabel();
        if (label) {
            lbl.setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontBold));
            lbl.setText(dco.getLabel(field));
        } else {
            lbl.setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontNormal));
            lbl.setText(dco.getDisplayString(field));
        }
        
        lbl.setPreferredSize(new Dimension(width, fieldHeight));
        
        return lbl;
    }       
    
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        for (int i = 0; i < getComponents().length; i++) {
            getComponents()[i].setBackground(color);
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        dco = null;
    }
    
    @Override
    public void clear() {
        removeAll();
        
        if (dco != null)
            dco.freeResources();
    }
}
