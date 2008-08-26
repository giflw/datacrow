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
import net.datacrow.core.objects.helpers.Book;
import net.datacrow.util.Hash;

public class EbookImport extends FileImporter {

    public EbookImport() {
        super(DcModules._BOOK);
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[] {"txt","chm", "doc", "pdf", "prc", "pdb", "kml", "html", "htm", "pdf", "prc"};
    }
    
    @Override
    public boolean allowReparsing() {
        return true;
    }    
    
    @Override
    public DcObject parse(String filename, int directoryUsage) throws ParseException {
        Book book = new Book();
        
        try {
            book.setValue(Book._A_TITLE, getName(filename, directoryUsage));
            book.setValue(Book._SYS_FILENAME, filename);
            Hash.getInstance().calculateHash(book);
        } catch (Exception exp) {
            throw new ParseException(exp);
        }
        
        return book;
    }    
}
