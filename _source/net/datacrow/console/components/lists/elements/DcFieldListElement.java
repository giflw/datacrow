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

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.objects.DcField;

public class DcFieldListElement extends DcListElement {
    
    private static final Dimension dim = new Dimension(360, 30);
    
    private DcField field;
    
    public DcFieldListElement(DcField field) {
        this.field = field;
        
        setPreferredSize(dim);
        setMinimumSize(dim);
        
        build();
    }

    public DcField getField() {
        return field;
    }
    
    @Override
    protected void build() {
        
        if (field == null)
            return;
        
        setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel labelField = ComponentFactory.getLabel(field.getLabel());
        labelField.setPreferredSize(new Dimension(360, 30));
        add(labelField);
    }
    
    @Override
    public void clear() {
        super.clear();
    }
}
