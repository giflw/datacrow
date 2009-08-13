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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.console.components.DcTextPane;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;

public class DcCardObjectListElement extends DcObjectListElement {

    private final static Dimension size = new Dimension(150, 200);
    
    public DcCardObjectListElement(DcObject dco) {
        super(dco);
        
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
    }
    
    @Override
    public Collection<Picture> getPictures() {
    	Collection<Picture> pictures = new ArrayList<Picture>();
    	for (int field : dco.getModule().getSettings().getIntArray(DcRepository.ModuleSettings.stCardViewPictureOrder))
    		pictures.add((Picture) dco.getValue(field));

		return pictures;
    }
    
    @Override
    public void setBackground(Color color) {
        for (int i = 0; i < getComponents().length; i++) {
            if (getComponents()[i] instanceof DcTextPane)
                getComponents()[i].setBackground(color);
        }
    }    
}