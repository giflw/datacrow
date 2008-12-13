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

package net.datacrow.core.wf.requests;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;

import javax.swing.ImageIcon;

import net.datacrow.core.DataCrow;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * Stores or deletes the images (physical disk operation).
 * 
 * @author Robert Jan van der Waals
 */
public class ImageRequest implements IRequest {
    
    private static final long serialVersionUID = 6822123799687000065L;

    private static Logger logger = Logger.getLogger(ImageRequest.class.getName());
    
    public static final int _SAVE = 1;
    public static final int _DELETE = 2;
    
    private boolean executeOnFail = false;

    private String filename;
    private DcImageIcon icon;
    private String ID;
    private int modus;
    
    /**
     * Creates a new instance.
     * @param pic
     * @param modus Either {@link #_SAVE} or {@link #_DELETE}.
     */
    public ImageRequest(Picture pic, int modus) {
        this.icon = (DcImageIcon) pic.getValue(Picture._D_IMAGE);
        this.filename = pic.getFilename();
        this.modus = modus;
    }

    /**
     * Creates a new instance.
     * @param ID The Object ID.
     * @param modus Either {@link #_SAVE} or {@link #_DELETE}.
     */
    public ImageRequest(String ID, int modus) {
        this.ID = ID;
        this.modus = modus;
    }    
    
    /**
     * Stores or deletes the images.
     * @param items Not used for this particular request.
     */
    public void execute(Collection<DcObject> items) {
        if (ID != null && modus == ImageRequest._DELETE) {
            File file = new File(DataCrow.imageDir);
            String[] files = file.list(new Filter(ID));
            
            if (files == null) return;
            
            for (int i = 0; i < files.length; i++) {
                String fullPath = DataCrow.imageDir + files[i];
                File f = new File(fullPath);
                if (f.exists()) {
                    f.deleteOnExit();
                    f.delete();
                }
            }
        } else {
            if (modus == ImageRequest._SAVE) 
                saveImage();
            else if (modus == ImageRequest._DELETE)
                deleteImage();
        }
    }
    
    private void deleteImage() {
        String filename1 = DataCrow.imageDir + filename;
        File file1 = new File(filename1);
        if (file1.exists()) {
            file1.deleteOnExit();
            file1.delete();
        }
        
        String filename2 = new Picture().getScaledFilename(DataCrow.imageDir + filename);
        
        File file2 = new File(filename2);
        if (file2.exists()) {
            file2.deleteOnExit();
            file2.delete();
        }
        
        icon.flush();
        ID = null;
    }

    private void saveImage() {
        if (filename == null)
            return;
        
        String imageFile = DataCrow.imageDir + filename;
        try {
            File file = new File(imageFile);
            if (file.exists())
                file.delete();
            
            byte[] image = icon.getBytes();
            if (image != null && image.length > 10) {
                Utilities.writeToFile(image, imageFile);
                Utilities.writeScaledImageToFile(new ImageIcon(image), new Picture().getScaledFilename(imageFile));
            }
            
        } catch (Exception e) {
            logger.error("Could not save [" + imageFile + "]", e);
        }
        
        end();
    }
    
    public void end() {
        if (icon != null)
            icon.flush();
        
        icon = null;
        ID = null;
        filename = null;
    }    
    
    public boolean getExecuteOnFail() {
        return executeOnFail;
    }

    public void setExecuteOnFail(boolean b) {
        executeOnFail = b;
    }
    
    private static class Filter implements FilenameFilter {

        private final String id; 
        
        public Filter(String id) {
            this.id = id;
        }
        
        public boolean accept(File file, String filename) {
            return filename.startsWith(id);
        }
    }    
}
