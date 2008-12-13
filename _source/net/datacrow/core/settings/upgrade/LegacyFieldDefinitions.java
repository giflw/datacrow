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

package net.datacrow.core.settings.upgrade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

/**
 * Used for conversion purposes only.
 * 
 * @deprecated
 * @author Robert Jan van der Waals
 */
@SuppressWarnings("dep-ann")
public class LegacyFieldDefinitions {
    
    private java.util.List<LegacyFieldDefinition> definitions = new ArrayList<LegacyFieldDefinition>();
    
    protected java.util.List<LegacyFieldDefinition> getDefinitions() {
        return definitions;
    }
    
    protected void add(String s) {
        StringTokenizer st = new StringTokenizer(s, "/&/");
        Collection<String> c = new ArrayList<String>();
        while (st.hasMoreTokens())
            c.add((String) st.nextElement());
        
        String[] values = c.toArray(new String[0]);
        int field = Integer.valueOf(values[0]).intValue();
        String name = values[1] == null || values[1].toLowerCase().equals("null") ? "" : values[1];
        boolean enabled = Boolean.valueOf(values[2]).booleanValue();
        boolean required = Boolean.valueOf(values[3]).booleanValue();
        boolean visible = Boolean.valueOf(values[4]).booleanValue();          
        boolean descriptive = values.length > 5 ? Boolean.valueOf(values[5]).booleanValue() : false;
        
        definitions.add(new LegacyFieldDefinition(field, name, enabled, required, descriptive, visible));   
    }
}
