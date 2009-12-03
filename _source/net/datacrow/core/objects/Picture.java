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

import java.io.File;

import net.datacrow.core.DataCrow;
import net.datacrow.core.modules.DcModules;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

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
    
    private boolean isNew = false;
    private boolean isUpdated = false;
    private boolean isDeleted = false;
    
    private DcImageIcon scaled;
    
    /**
     * Creates a new instance
     */
    public Picture() {
        super(DcModules._PICTURE);
    }
    
    public boolean hasImage() {
        return !Utilities.isEmpty(getValue(_C_FILENAME));
    }
    
    public void loadImage() {
        String filename = (String) getValue(_C_FILENAME);
        DcImageIcon image = (DcImageIcon) getValue(Picture._D_IMAGE);

        if (filename != null && image == null) {
            try {
            	filename = new File(filename).exists() ? filename : DataCrow.imageDir + filename;
                image = new DcImageIcon(filename);
                
            } catch (Exception e) {
                logger.error("Could not load image " + DataCrow.imageDir + filename, e);
            }
        } else {
            // make sure the image is loaded.
            // as a precaution; ignore the image if it has bytes assigned to it
            if (image.getCurrentBytes() == null)
                image = new DcImageIcon(image.getImage());
        }
        
        setValue(Picture._D_IMAGE, image);
        
        markAsUnchanged();
    }
    
    public byte[] getBytes() {
    	DcImageIcon image = (DcImageIcon) getValue(_D_IMAGE);
        return image != null ? image.getBytes() : null;
    }
    
    @Override
    public void release() {
        unload();
        scaled = null;
        super.release();
    }
    
    public void unload() {
    	if (getValues() != null && !isNew() && !isUpdated) {
    	    
	    	DcImageIcon image = ((DcImageIcon) getValue(_D_IMAGE));
	    	
	    	if (image != null) image.flush();
	    	if (scaled != null) scaled.flush();
	    	
	        setValueLowLevel(_D_IMAGE, null);
	        setChanged(_D_IMAGE, false);
    	}
    }
    
    @Override
    public void markAsUnchanged() {
        super.markAsUnchanged();

        isNew = false;
        isUpdated = false;
        isDeleted = false;
    }    
    
    public DcImageIcon getScaledPicture() {
        if (scaled == null) {
            String filename = getScaledFilename();
            if (filename != null) {
                if (!new File(DataCrow.imageDir + filename).exists()) {
                    loadImage();
                    DcImageIcon icon = (DcImageIcon) getValue(_D_IMAGE);
                    
                    if (icon != null) {
                        logger.info("Scaled image is missing, creating new");
                        try {
                            Utilities.writeScaledImageToFile(icon, DataCrow.imageDir + filename);
                        } catch (Exception e) {
                            logger.error("Could not create new scaled image!", e);
                        }
                    }
                }
                scaled = new DcImageIcon(DataCrow.imageDir + filename);
            }
        } else {
            scaled = new DcImageIcon(scaled.getImage());
        }
        
        return scaled;
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
                
                if (isNew()) {
                    return null;
                } else {
                    return scaledFilename;
                }
                
            } catch (Exception e) {
                logger.debug("Unable to determine scaled image filename for " + filename + ". Is this a new item?", e);
            }
        }
        return null;
    }
    
    public void isUpdated(boolean b) {
        isUpdated = b;
        if (b) {
            isNew = false;
            isDeleted = false;
        }
    }
    
    public void isNew(boolean b) {
        isNew = b;

        if (b) {
            isUpdated = false;
            isDeleted = false;
        }
    }
    
    public void isDeleted(boolean b) {
        isDeleted = b;
        
        if (b) {
            isUpdated = false;
            isNew = false;
        }
    }
    
    public boolean isLoaded() {
        return getValue(Picture._D_IMAGE) != null;
    }
    
    public boolean isUpdated() {
        return isUpdated;
    }
    
    @Override
    public boolean isNew() {
        return isNew;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    @Override
    public String toString() {
        return getValue(_C_FILENAME) != null ? (String) getValue(_C_FILENAME) : "";
    }
    
    @Override
    public int hashCode() {
        return (getValue(Picture._B_FIELD) != null ? getValue(Picture._B_FIELD).hashCode() : 0) +
               (getValue(Picture._A_OBJECTID) != null ? getValue(Picture._A_OBJECTID).hashCode() : 0); 
    }
    
    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        
        if (o instanceof Picture) {
            Picture picture = (Picture) o;
            
            String fieldname1 = (String) picture.getValue(Picture._B_FIELD);
            String fieldname2 = (String) getValue(Picture._B_FIELD);

            String objectID1 = (String) picture.getValue(Picture._A_OBJECTID);
            String objectID2 = (String) getValue(Picture._A_OBJECTID);
            
            equals = fieldname1.equals(fieldname2) && objectID1.equals(objectID2);
        } else {
            equals = super.equals(o);
        }
        
        return equals;
   }    
}