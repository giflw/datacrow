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

package net.datacrow.core.migration.itemexport;

import java.io.File;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import net.datacrow.core.DataCrow;
import net.datacrow.core.objects.Picture;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

public class ItemExporterUtilities {

    private static Logger logger = Logger.getLogger(ItemExporterUtilities.class.getName());
    
    private ItemExporterSettings settings;
    private String exportName;
    private String exportDir;
    
    public ItemExporterUtilities(String exportFilename, ItemExporterSettings settings) {
        File file = new File(exportFilename);
        
        this.settings = settings;
        this.exportName = file.getName();
        this.exportDir = file.getParent();
        this.exportName = exportName.lastIndexOf(".") > -1 ? exportName.substring(0, exportName.lastIndexOf(".")) : exportName;
        
        if (settings.getBoolean(ItemExporterSettings._COPY_IMAGES))
            new File(getImageDir()).mkdirs();
    }
    
    private String getImageDir() {
        return new File(exportDir, exportName +  "_images/").toString();
    }
    
    public String getImageURL(Picture p) {
        String url = "";
        String imageFilename = (String) p.getValue(Picture._C_FILENAME); 
        if (!Utilities.isEmpty(imageFilename)) {
            if (settings.getBoolean(ItemExporterSettings._COPY_IMAGES)) {
                copyImage(p,  new File(getImageDir(), imageFilename).toString());
                
                if (settings.getBoolean(ItemExporterSettings._ALLOWRELATIVEIMAGEPATHS))
                    url = "./" + exportName + "_images/" + imageFilename;
                else 
                    url = "file:///" +  new File(getImageDir(), imageFilename);
            } else {
                url = "file:///" +  new File(DataCrow.imageDir, (String) p.getValue(Picture._C_FILENAME));
            }
        }
        return url;
    }
    
    private void copyImage(Picture picture, String target) {
        try {
            picture.loadImage();
            ImageIcon icon = (ImageIcon) picture.getValue(Picture._D_IMAGE);

            if (icon != null) {
                if (settings.getBoolean(ItemExporterSettings._SCALE_IMAGES)) {
                    int width = settings.getInt(ItemExporterSettings._IMAGE_WIDTH);
                    int height = settings.getInt(ItemExporterSettings._IMAGE_HEIGHT);
                    Utilities.writeScaledImageToFile(icon, target, DcImageIcon._TYPE_PNG, width, height);
                } else {
                    Utilities.writeToFile(icon, target);
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred while copying image to " + target, e);
        }
    }    
}
