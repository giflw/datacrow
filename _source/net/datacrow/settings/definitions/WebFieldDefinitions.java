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

public class WebFieldDefinitions implements IDefinitions {

    private Collection<WebFieldDefinition> definitions = new ArrayList<WebFieldDefinition>();

    public void add(Collection<Definition> c) {
        for (Definition definition : c)
            add(definition);
    }
    
    public void add(Definition definition) {
        definitions.add((WebFieldDefinition) definition);
    }

    public void clear() {
        definitions.clear();
    }

    public Collection<WebFieldDefinition> getWebDefinitions() {
        return definitions;
    }
    
    public Collection<WebFieldDefinition> getDefinitions() {
        return definitions;
    }

    public int getSize() {
        return definitions.size();
    }         

    public WebFieldDefinition get(int field) {
        for (WebFieldDefinition d : definitions) {
            if (d.getField() == field)
                return d;
        }
        return null;
    }
    
    private void removeDefinition(int field) {
        WebFieldDefinition definition = null;
        for (WebFieldDefinition d : definitions) {
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
        int width = Integer.parseInt((String) st.nextElement());
        int maxText = Integer.parseInt((String) st.nextElement());
        
        boolean overview = Boolean.valueOf((String) st.nextElement()).booleanValue();
        boolean link = Boolean.valueOf((String) st.nextElement()).booleanValue();
        
        boolean quickSearch = false;
        if (st.hasMoreElements())
            quickSearch = Boolean.valueOf((String) st.nextElement()).booleanValue();
        
        removeDefinition(field);
        add(new WebFieldDefinition(field, width, maxText, overview, link, quickSearch));
    }
}
