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

package net.datacrow.fileimporters;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.Software;
import net.datacrow.util.Hash;

public class SoftwareImporter extends FileImporter {

    public SoftwareImporter() {
        super(DcModules._SOFTWARE);
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[] {};
    }
    
    @Override
    public boolean allowDirectoryRegistration() {
        return true;
    }

    @Override
    public boolean allowReparsing() {
        return true;
    }    
    
    @Override
    public boolean canImportArt() {
        return true;
    }    
    
    @Override
    public DcObject parse(String filename, int directoryUsage) throws ParseException {
        Software software = new Software();
        
        try {
            software.setValue(Software._A_TITLE, getName(filename, directoryUsage));
            software.setValue(Software._SYS_FILENAME, filename);
        	Hash.getInstance().calculateHash(software);
        } catch (Exception exp) {
            throw new ParseException(exp);
        }
        
        return software;
    }
}
