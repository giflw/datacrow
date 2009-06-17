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

import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.Book;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Hash;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/**
 * E-Book (Electronical Book) file imporerter.
 * 
 * @author Robert Jan van der Waals
 */
public class EbookImport extends FileImporter {

    public EbookImport() {
        super(DcModules._BOOK);
    }

    @Override
    public String[] getDefaultSupportedFileTypes() {
        return new String[] {"txt","chm", "doc", "pdf", "prc", "pdb", "kml", "html", "htm", "pdf", "prc"};
    }
    
    @Override
    public boolean allowReparsing() {
        return true;
    }    
    
    @Override
    public DcObject parse(IFileImportClient listener, String filename, int directoryUsage) {
        Book book = new Book();
        
        try {
            book.setValue(Book._A_TITLE, getName(filename, directoryUsage));
            book.setValue(Book._SYS_FILENAME, filename);
            
            if (filename.toLowerCase().endsWith("pdf")) {
                File file = new File(filename);
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                FileChannel channel = raf.getChannel();
                ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                PDFFile pdffile = new PDFFile(buf);

                // draw the first page to an image
                PDFPage page = pdffile.getPage(0);
                Rectangle rect = new Rectangle(0,0, (int)page.getBBox().getWidth(), (int)page.getBBox().getHeight());
                Image front = page.getImage(rect.width, rect.height, rect, null, true, true);
                book.setValue(Book._K_PICTUREFRONT, new DcImageIcon(front));
            }
            
            Hash.getInstance().calculateHash(book);
        } catch (Exception exp) {
            listener.addMessage(DcResources.getText("msgCouldNotReadInfoFrom", filename));
        }
        
        return book;
    }    
}
