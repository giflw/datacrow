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

public class ProgramDefinition extends Definition {

	private String extension = "";
	private String program = "";
	
	public ProgramDefinition(String extension, String program) {
		this.extension = extension;
		this.program = program;
	}
	
	public String getExtension() {
		return extension;
	}

	public String getProgram() {
		return program;
	}

    @Override
    public String toSettingValue() {
        return extension + "/&/" + program;
    }    
    
    @Override
	public String toString() {
		return "[" + extension + "] " + program;
	}
}
