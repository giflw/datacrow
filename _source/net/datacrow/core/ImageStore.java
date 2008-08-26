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

package net.datacrow.core;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

/**
 * Images use a lot of resources. To make sure images are flushed and its resources are freed
 * they can be added to this image store. The {@link FreeResourcesTask} will call the clean() method
 * every now and then. 
 * 
 * @author rj.vanderwaals
 */
public class ImageStore {

    private static Logger logger = Logger.getLogger(ImageStore.class.getName());
    
    private static final Collection<WeakReference<Image>> references = new ArrayList<WeakReference<Image>>();
    
    public synchronized static void addImage(Image image) {
        if (!exists(image) && image != null)
            references.add(new WeakReference<Image>(image));
    }
    
    private synchronized static boolean exists(Image image) {
        for (WeakReference<Image> reference : references) {
            if (reference.get() == image) return true;
        }
        return false;
    }
    
    public synchronized static void clean() {
        Collection<WeakReference<Image>> remove = new ArrayList<WeakReference<Image>>();
        int collected = 0;
        for (WeakReference<Image> reference : references) {
            Image image = reference.get();
            if (image == null) {
                remove.add(reference);
                collected++;
            } else {

                try {
                    image.getGraphics().dispose();
                } catch (Exception e) {
                    // this will fail for non-offscreen images (see API)
                    if (logger.isDebugEnabled())
                        logger.debug("Could not dispose of the graphics of image of type [" + image.getClass() + "] in store [" + image.hashCode() + "]. Not an offscreen image.");
                }
                   
                image.flush();
                
                // try to enqueue. As we are only interested in freeing the resources used
                // by the images and we already disposed of its graphics and flushed its resources its time
                // to queue the reference for garbage collection. 
                if (!reference.enqueue()) {
                    if (logger.isDebugEnabled()) {
                        boolean isBuffered = image instanceof BufferedImage;
                        logger.debug("Failed to enqueue image [" + image.hashCode() + "] of type [" + image.getClass() + "]" +
                                     "Source [" + (image.getSource() != null ? image.getSource().getClass() : "N/A") + "]. " +  
                                     (isBuffered ? "The image is buffered and contains " + 
                                             (((BufferedImage) image).getSources() != null ? 
                                              ((BufferedImage) image).getSources().size() : 0) + " sources.": ""));
                    }
                }
            }
        }
        
        logger.info(DcResources.getText("msgImagesInStore", new String[] {"" + references.size(), "" + collected}));
        references.removeAll(remove);
        remove.clear();
    }
}
