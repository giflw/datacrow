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

public class ProgramDefinitions implements IDefinitions {

    Collection<ProgramDefinition> definitions = new ArrayList<ProgramDefinition>();
    
    public void add(Collection<Definition> c) {
        for (Definition definition : c)
            add(definition);
    }

    public void add(Definition definition) {
        definitions.add((ProgramDefinition) definition);
    }

    public void clear() {
        definitions.clear();
    }

    public int getSize() {
        return definitions.size();
    }     
    
	public String getProgramForExtension(String extension) {
	    for (ProgramDefinition definition : definitions) {
			if (    definition.getExtension().equals(extension) || 
			        definition.getExtension().endsWith(extension)) 
				return definition.getProgram();
		}
		return null;
	}
	
	public Collection<ProgramDefinition> getDefinitions() {
	    return definitions;
	}
    
    public void add(String s) {
        int index = s.indexOf("/&/");
        String extension = s.substring(0, index);
        String program = s.substring(index + 3, s.length());

        add(new ProgramDefinition(extension, program));
    }
}
