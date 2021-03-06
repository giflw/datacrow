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

package net.datacrow.core.objects;

import java.awt.Image;
import java.io.File;

import net.datacrow.core.DataCrow;
import net.datacrow.core.modules.DcModules;
import net.datacrow.util.DcImageIcon;

import org.apache.log4j.Logger;

/**
 * A picture represents a physical picture file.
 * Every image stored in Data Crow (such as screenshots) is represented by 
 * a picture object.
 * 
 * @author Robert Jan van der Waals
 */
public class Picture extends DcObject {

    private static final long serialVersionUID = 2871103592900195025L;

    private static Logger logger = Logger.getLogger(Picture.class.getName());
    
    public static final int _A_OBJECTID = 1;
    public static final int _B_FIELD = 2;
    public static final int _C_FILENAME = 3;
    public static final int _D_IMAGE = 4;
    public static final int _E_HEIGHT = 5;
    public static final int _F_WIDTH = 6;
    public static final int _G_EXTERNAL_FILENAME = 7;
    
    private boolean edited = false;
    private boolean deleted = false;
    
    private DcImageIcon scaledImage;
    
    /**
     * Creates a new instance
     */
    public Picture() {
        super(DcModules._PICTURE);
    }
    
    @Override
    public void initializeImages() {}
    
    @Override
    public boolean hasPrimaryKey() {
        return false;
    }

    @Override
    public void initializeReferences() {}    
    
    /**
     * Checks whether an image has been defined and, if so, if the image exists.
     */
    public boolean hasImage() {
        return isFilled(_D_IMAGE) || 
              (isFilled(_C_FILENAME) && new File(DataCrow.imageDir, getDisplayString(_C_FILENAME)).exists());
    }

    public void loadImage(boolean external) {
        String filename = (String) getValue(_G_EXTERNAL_FILENAME);
        filename = !external || filename == null || !new File(filename).exists() ? (String) getValue(_C_FILENAME) : filename;
        DcImageIcon image = (DcImageIcon) getValue(Picture._D_IMAGE);

        if (filename != null && image == null) {
            try {
            	filename = new File(filename).exists() ? filename : DataCrow.imageDir + filename;
                image = new DcImageIcon(filename);
                
            } catch (Exception e) {
                logger.error("Could not load image " + DataCrow.imageDir + filename, e);
            }
        } else if (image != null && isEdited()) {
            image.flush();
            image = new DcImageIcon(image.getImage());
            scaledImage = null;
        }
        
        setValue(Picture._D_IMAGE, image);
        markAsUnchanged();
    }
    
    public Image getImage() {
        DcImageIcon image = (DcImageIcon) getValue(_D_IMAGE);
        return image != null ? image.getImage() : null;
    }
    
    @Override
    public void destroy() {
        unload();
        deleted = false;
        edited = false;
        scaledImage = null;
    }
    
    public String getImageFilename() {
        return (String) getValue(Picture._C_FILENAME);
    }
    
    public void unload() {
        
        if (scaledImage != null) {
            scaledImage.flush();
            scaledImage = null;
        }
        
        if (getValues() != null && (!isNew() && !edited)) {
	    	DcImageIcon image = ((DcImageIcon) getValue(_D_IMAGE));

	    	if (image != null) image.flush();
	    	
	        setValueLowLevel(_D_IMAGE, null);
	        setChanged(_D_IMAGE, false);
        }
    }
    
    @Override
    public void markAsUnchanged() {
        super.markAsUnchanged();

        edited = false;
        deleted = false;
    }    
    
    public DcImageIcon getScaledPicture() {
        String filename = getScaledFilename();
        if (filename != null) {
            if (scaledImage != null) scaledImage.flush();
            return new DcImageIcon(new File(DataCrow.imageDir, filename));
        }
        return null;
    }
    
    public String getScaledFilename() {
        return getScaledFilename((String) getValue(Picture._C_FILENAME));
    }

    public String getScaledFilename(String filename) {
        if (filename != null) {
            try {
                int idx = filename.indexOf(".jpg");
                String plain = filename.substring(0, idx);
                String scaledFilename = plain + "_small.jpg";
                return scaledFilename;
            } catch (Exception e) {
                logger.debug("Unable to determine scaled image filename for " + filename + ". Is this a new item?", e);
            }
        }
        return null;
    }
    
    public void isEdited(boolean b) {
        edited = b;
        if (b) deleted = false;
    }
    
    public void isDeleted(boolean b) {
        deleted = b;
        if (b) edited = false;
    }
    
    @Override
    public boolean isLoaded() {
        return getValue(Picture._D_IMAGE) != null;
    }
    
    @Override
    public boolean isNew() {
        return super.isNew() && !isDeleted() && getValue(_D_IMAGE) != null; 
    }
    
    public boolean isEdited() {
        return edited;
    }
    
    public boolean isDeleted() {
        return deleted;
    }
    
    @Override
    public String toString() {
        return getValue(_C_FILENAME) != null ? (String) getValue(_C_FILENAME) : "";
    }
    
    @Override
    public int hashCode() {
        return (getValue(Picture._C_FILENAME) != null ? getValue(Picture._C_FILENAME).hashCode() : 0); 
    }
    
    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        
        if (o instanceof Picture) {
            Picture picture = (Picture) o;
            
            String filename1 = (String) picture.getValue(Picture._C_FILENAME);
            String filename2 = (String) getValue(Picture._C_FILENAME);
            
            equals = filename1 == filename2 || (filename1 != null && filename1.equals(filename2));
        } else {
            equals = super.equals(o);
        }
        
        return equals;
   }

	@Override
	protected void finalize() throws Throwable {
		unload();
		super.finalize();
	}    
}