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
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.datacrow.util.Base64;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * A property represents a simple item such as a category or a storage medium.
 * Properties are widely used within Data Crow to allow users to select from 
 * a predefined set of options.
 * 
 * @author Robert Jan van der Waals
 */
public class DcProperty extends DcObject {

    private static final long serialVersionUID = -936831554081323250L;

    private static Logger logger = Logger.getLogger(DcProperty.class.getName());
    
    public static final int _A_NAME = 150;
    public static final int _B_ICON = 151;
    
    /**
     * Creates a new instance.
     * @param module
     */
    public DcProperty(int module) {
        super(module);
    }
    
    /**
     * Retrieve the icon to represent the item.
     * @return The value of the {@link #_B_ICON} field.
     */
    @Override
    public ImageIcon getIcon() {
        String value = (String) getValue(_B_ICON);
        ImageIcon icon = null;
        if (value != null && value.length() > 1) 
            icon = Utilities.base64ToImage(value);
        
        return icon;
    } 
    
    /**
     * The filename on which this item is based.
     * @return Returns always null.
     */
    @Override
    public String getFilename() {
        return null;
    }    
    
    @Override
    public String toString() {
        return getValue(_A_NAME) != null ? getValue(_A_NAME).toString() : "";
    }  

    @Override
    public String getName() {
        return toString();
    }
    
    /**
     * Before saving a property its icon is scaled.
     */
    @Override
    protected void beforeSave() {
        super.beforeSave();
        
        String value = (String) getValue(_B_ICON);
        
        if (value != null && value.length() > 0) {
            byte[] bytes = Base64.decode(value.toCharArray());
            
            ImageIcon current = new ImageIcon(bytes);
            
            if (current.getIconHeight() > 16 || current.getIconWidth() > 16) {
                Image img = Utilities.getScaledImage(bytes, 16, 16);
                BufferedImage buffImg = Utilities.toBufferedImage(new ImageIcon(img));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
                try {
                    ImageIO.write(buffImg, "jpg", baos );
                    baos.flush();
                    bytes = baos.toByteArray();
                    baos.close();
                    setValue(_B_ICON, new String(Base64.encode(bytes)));
                    
                    img = null;
                    buffImg = null;
                } catch (Exception e) {
                    logger.error("Could not save scaled image for object with ID " + getID(), e);
                }
            }
        }
    }   
    
    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (o != null && o instanceof DcProperty) {
            equals = ((DcProperty) o).getID().equals(getID());
        } else if (o instanceof String) {
            equals = o.toString().equals(getValue(_A_NAME));
        }
        return equals;
    }
}
