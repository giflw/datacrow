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

package net.datacrow.settings.definitions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

public class QuickViewFieldDefinitions implements IDefinitions {

    private Collection<QuickViewFieldDefinition> definitions = new ArrayList<QuickViewFieldDefinition>();

    public void add(Collection<Definition> c) {
        for (Definition definition : c)
            add(definition);
    }
    
    public void add(Definition definition) {
        definitions.add((QuickViewFieldDefinition) definition);
    }

    public void clear() {
        definitions.clear();
    }

    public Collection<QuickViewFieldDefinition> getQuickViewDefinitions() {
        return definitions;
    }
    
    public Collection<QuickViewFieldDefinition> getDefinitions() {
        return definitions;
    }

    public int getSize() {
        return definitions.size();
    }         

    private void removeDefinition(int field) {
        QuickViewFieldDefinition definition = null;
        for (QuickViewFieldDefinition d : definitions) {
            if (d.getField() == field) {
                definition = d;
                break;
            }
        }
        
        if (definition != null)
            definitions.remove(definition);
    }    
    
    public void add(String s) {
        StringTokenizer st = new StringTokenizer(s, "/&/");
        int field = Integer.parseInt((String) st.nextElement());
        boolean enabled = Boolean.valueOf((String) st.nextElement()).booleanValue();
        String direction = (String) st.nextElement();
        
        int maxLength = 0;
        if (st.hasMoreElements())
            maxLength = Integer.parseInt((String) st.nextElement());

        removeDefinition(field);
        add(new QuickViewFieldDefinition(field, enabled, direction, maxLength));
    }
}
