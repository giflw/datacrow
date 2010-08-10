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

import java.util.List;

import javax.faces.model.SelectItem;

public class DcReferences {

    private int moduleIdx;
    private int fieldIdx;
    
    private List<SelectItem> listItems;
    private String[] keys;
    
    public DcReferences() {}

    public int getModuleIdx() {
        return moduleIdx;
    }

    public void setModuleIdx(int moduleIdx) {
        this.moduleIdx = moduleIdx;
    }

    public int getFieldIdx() {
        return fieldIdx;
    }

    public void setFieldIdx(int fieldIdx) {
        this.fieldIdx = fieldIdx;
    }
    
    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public List<SelectItem> getListItems() {
        return listItems;
    }

    public void setListItems(List<SelectItem> values) {
        this.listItems = values;
    }
}
