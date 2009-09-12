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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.Book;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Hash;
import net.datacrow.util.StringUtils;
import net.datacrow.util.Utilities;
import net.datacrow.util.isbn.ISBN;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/**
 * E-Book (Electronical Book) file imporerter.
 * 
 * @author Robert Jan van der Waals
 */
public class EbookImport extends FileImporter {

    private static Logger logger = Logger.getLogger(DataManager.class.getName());
    
    public EbookImport() {
        super(DcModules._BOOK);
    }

    @Override
    public String[] getDefaultSupportedFileTypes() {
        return new String[] {"txt","chm", "doc", "pdf", "prc", "pdb", "kml", "html", "htm", "pdf", "prc", "lit"};
    }
    
    @Override
    public boolean allowReparsing() {
        return true;
    }    
    
    @Override
    public DcObject parse(String filename, int directoryUsage) {
        Book book = new Book();
        
        try {
            book.setValue(Book._A_TITLE, getName(filename, directoryUsage));
            book.setValue(Book._SYS_FILENAME, filename);
            
            // check if the filename contains an ISBN
            String isbn = String.valueOf(StringUtils.getContainedNumber(filename));
            boolean isIsbn10 = ISBN.isISBN10(isbn);
            boolean isIsbn13 = ISBN.isISBN13(isbn);
            
            // this can be used later on by the online search
            if (isIsbn10 || isIsbn13) {
                String isbn10 = isIsbn10 ? isbn : ISBN.getISBN10(isbn);
                String isbn13 = isIsbn13 ? isbn : ISBN.getISBN13(isbn);
                book.setValue(Book._J_ISBN10, isbn10);
                book.setValue(Book._N_ISBN13, isbn13);
            }
            
            if (filename.toLowerCase().endsWith("pdf")) {
                RandomAccessFile raf = null;
                PDFFile pdffile;
                try {
                    File file = new File(filename);
                    raf = new RandomAccessFile(file, "r");
                    FileChannel channel = raf.getChannel();
                    ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                    pdffile = new PDFFile(buf);
                    pdffile.stop(1);
    
                    try {
                        book.setValue(Book._T_NROFPAGES, Long.valueOf(pdffile.getNumPages()));
                        Iterator<String> it = pdffile.getMetadataKeys();
                        while (it.hasNext()) {
                            String key = it.next();
                            String value = pdffile.getStringMetadata(key);
                            
                            if (!Utilities.isEmpty(value)) {
                                if (key.equalsIgnoreCase("Author"))
                                    DataManager.createReference(book, Book._G_AUTHOR, value);
                                if (key.equalsIgnoreCase("Title") && !value.trim().equalsIgnoreCase("untitled"))
                                    book.setValue(Book._A_TITLE, value);
                            }
                        }
                    } catch (IOException ioe) {
                        getClient().addMessage(DcResources.getText("msgCouldNotReadInfoFrom", filename));
                    }
    
                    // draw the first page to an image
                    PDFPage page = pdffile.getPage(0);
                    if (page != null) {
                        Rectangle rect = new Rectangle(0,0, (int)page.getBBox().getWidth(), (int)page.getBBox().getHeight());
                        Image front = page.getImage(rect.width, rect.height, rect, null, true, true);
                        book.setValue(Book._K_PICTUREFRONT, new DcImageIcon(Utilities.getBytes(new ImageIcon(front))));
                    }
                } finally {
                    if (raf != null)
                        raf.close();
                    
                }
            }
            
            Hash.getInstance().calculateHash(book);
        } catch (OutOfMemoryError err) {
            logger.error(err, err);
            getClient().addMessage(DcResources.getText("msgOutOfMemory"));
        } catch (Exception exp) {
            logger.error(exp, exp);
            getClient().addMessage(DcResources.getText("msgCouldNotReadInfoFrom", filename));
        } catch (Error err) {
            logger.error(err, err);
            getClient().addMessage(DcResources.getText("msgCouldNotReadInfoFrom", filename));
        }
        return book;
    }    
}
