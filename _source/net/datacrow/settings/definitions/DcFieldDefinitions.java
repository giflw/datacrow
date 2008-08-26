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

import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModules;


public class DcFieldDefinitions implements IDefinitions {
    
    private java.util.List<DcFieldDefinition> definitions = new ArrayList<DcFieldDefinition>();
    
    public void add(Definition definition) {
        definitions.add((DcFieldDefinition) definition);
    }

    public void add(Collection<Definition> c) {
        for (Definition definition : c)
            add(definition);
    }

    public void clear() {
        definitions.clear();
    }

    public int getSize() {
        return definitions.size();
    }
    
    public Collection<DcFieldDefinition> getDefinitions() {
        return definitions;
    }    
    
    public DcFieldDefinition get(int field) {
        for (DcFieldDefinition definition : definitions) {
            if (definition.getIndex() == field) 
                return definition;
        }
        return null;
    }
    
    private void removeDefinition(int field) {
        DcFieldDefinition def = null;
        for (DcFieldDefinition definition : definitions)
            def = definition.getIndex() == field ? definition : def;
        
    	if (def != null)
    		definitions.remove(def);
    }
    
    /**
     * Other settings depend on the global field settings:
     * - Quick view settings
     * - Table column order
     */
    public void checkDependencies() {
        int[] columnOrder = DcModules.getCurrent().getSettings().getIntArray(DcRepository.ModuleSettings.stTableColumnOrder);
        QuickViewFieldDefinitions qvDefs = (QuickViewFieldDefinitions) DcModules.getCurrent().getSetting(DcRepository.ModuleSettings.stQuickViewFieldDefinitions);
        
        Collection<Integer> c = new ArrayList<Integer>();
        for (int column : columnOrder) {
            for (DcFieldDefinition definition : getDefinitions()) {
                if (definition.getIndex() == column && definition.isEnabled())
                    c.add(Integer.valueOf(column));
            }
        }
        
        columnOrder = new int[c.size()];
        int i = 0;
        for (Integer field : c) 
            columnOrder[i++] = field.intValue();
        
        DcModules.getCurrent().setSetting(DcRepository.ModuleSettings.stTableColumnOrder, columnOrder);
        
        QuickViewFieldDefinitions qvDefsNew = new QuickViewFieldDefinitions();
        for (QuickViewFieldDefinition qvDef : qvDefs.getDefinitions()) {
            for (DcFieldDefinition definition : getDefinitions()) {
                if (qvDef.getField() == definition.getIndex() && definition.isEnabled())
                    qvDefsNew.add(qvDef);
            }
        }
        
        DcModules.getCurrent().setSetting(DcRepository.ModuleSettings.stQuickViewFieldDefinitions, qvDefsNew);
    }
	
    public void add(String s) {
        StringTokenizer st = new StringTokenizer(s, "/&/");
        Collection<String> c = new ArrayList<String>();
        while (st.hasMoreTokens())
            c.add((String) st.nextElement());
        
        String[] values = c.toArray(new String[0]);
        int field = Integer.valueOf(values[0]).intValue();
        String name = values[1] == null || values[1].toLowerCase().equals("null") ? "" : values[1];
        boolean enabled = Boolean.valueOf(values[2]).booleanValue();
        boolean required = Boolean.valueOf(values[3]).booleanValue();
        
        boolean descriptive = values.length > 5 ? Boolean.valueOf(values[5]).booleanValue() : 
                              values.length > 4 ? Boolean.valueOf(values[4]).booleanValue() : false;
        
        removeDefinition(field);
    	add(new DcFieldDefinition(field, name, enabled, required, descriptive));	
    }
}