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

package net.datacrow.core.web.model;

import java.util.ArrayList;
import java.util.List;


public class DcWebImages {

    private DcWebImage current;
    private List<DcWebImage> images = new ArrayList<DcWebImage>();

    public DcWebImage getCurrent() {
        if (current == null && images.size() > 0)
            current = images.get(0);
        
        return current;
    }
    
    public void clear() {
        current = null;
        images.clear();
    }
    
    public void add(DcWebImage image) {
        images.add(image);
    }
    
    public int getCount() {
        return images.size();
    }
    
    public List<DcWebImage> getImages() {
        return images;
    }
    
    public void setCurrent(int fieldIdx) {
        for (DcWebImage image : images) {
            if (image.getFieldIdx() == fieldIdx)
                current = image;
        }
    }
}
