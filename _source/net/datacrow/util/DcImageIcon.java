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

package net.datacrow.util;

import java.awt.Image;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

public class DcImageIcon extends ImageIcon {

	private static final long serialVersionUID = 5371441182570021920L;

    private static Logger logger = Logger.getLogger(DcImageIcon.class.getName());

    public static int _TYPE_JPEG = 0;
    public static int _TYPE_PNG = 1;
    
	private byte[] bytes;
	private String filename;

	private static long counter = 0;
	
    public DcImageIcon() {
        super();
    }
    
    public DcImageIcon(File file) {
        this(file.toString());
    }    
    
    public DcImageIcon(String filename) {
        super(filename, "image" + counter++);
        this.filename = filename;
    }  
    
    public DcImageIcon(byte[] bytes, boolean storeBytes) {
        super(bytes, "image" + counter++);
        if (storeBytes)
            this.bytes = bytes;
    }
    
    public DcImageIcon(byte[] bytes) {
        super(bytes, "image" + counter++);
        this.bytes = bytes;
    }

    public DcImageIcon(Image image) {
        super(image, "image" + counter++);
    }

    public DcImageIcon(URL location) {
        super(location, "image" + counter++);
    }
    
    public void flush() {
    	bytes = null;
    	filename = null;
    	getImage().flush();
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
    	this.filename = filename;
    }
    
    public byte[] getCurrentBytes() {
        return bytes;
    }
    
    public byte[] getBytes() {
    	if (filename != null && bytes == null) 
            logger.debug("Retrieving bytes from " + filename);
    		
    	try {
    		this.bytes = bytes != null ? bytes :  
    		             filename != null ? Utilities.readFile(new File(filename)) : Utilities.getBytes(this);
    	} catch (Exception ie) {
    		logger.error("Could not retrieve bytes from " + filename, ie);
    	}
        
    	return bytes;
    }
    
    @Override
    protected void finalize() throws Throwable {
        flush();
        try {
            Image image = getImage();
            tracker.removeImage(image);
            setImage(null);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        
        super.finalize();
        super.setDescription(null);
        super.setImageObserver(null);
    }
}
