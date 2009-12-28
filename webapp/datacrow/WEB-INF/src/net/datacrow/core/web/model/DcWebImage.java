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

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.Picture;


public class DcWebImage {

    private net.datacrow.core.objects.Picture picture;

    private int fieldIdx;
    private int moduleIdx;

    public net.datacrow.core.objects.Picture getPicture() {
        return picture;
    }

    public String getName() {
        return DcModules.get(moduleIdx).getField(fieldIdx).getLabel();
    }
    
    public void setFieldIdx(int fieldIdx) {
        this.fieldIdx = fieldIdx;
    }

    public void setModuleIdx(int moduleIdx) {
        this.moduleIdx = moduleIdx;
    }

    public void setPicture(net.datacrow.core.objects.Picture picture) {
        this.picture = picture;
    }

    public String getFilename() {
        return picture != null ? "mediaimages/" + picture.getValue(Picture._C_FILENAME) : null;
    }

    public String getFilenameScaled() {
        return picture != null ? "mediaimages/" + picture.getScaledFilename() : null;
    }
    
    public int getFieldIdx() {
        return fieldIdx;
    }
    
    public int getModuleIdx() {
        return moduleIdx;
    }
}
