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

import javax.swing.BorderFactory;

import net.datacrow.console.components.DcTextPane;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.DcFieldDefinitions;

public class DcCardObjectListElement extends DcObjectListElement {

    public DcCardObjectListElement(DcObject dco) {
        super(dco);
        
        setPreferredSize(new Dimension(150, 200));
        setMaximumSize(new Dimension(150, 200));
        setMinimumSize(new Dimension(150, 200));
        
        setBorder(BorderFactory.createLineBorder(DcSettings.getColor(DcRepository.Settings.stCardViewBackgroundColor)));
    }
    
    @Override
    public Collection<Picture> getPictures() {
        Collection<Picture> pictures = new ArrayList<Picture>();
        
        DcFieldDefinitions definitions = dco.getModule().getFieldDefinitions();
        for (DcFieldDefinition definition : definitions.getDefinitions()) {
            DcField field = dco.getField(definition.getIndex());
            if (field.getValueType() == DcRepository.ValueTypes._PICTURE)
                pictures.add((Picture) dco.getValue(field.getIndex()));
        }
        
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