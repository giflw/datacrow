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

import java.io.Serializable;

import net.datacrow.core.modules.DcModules;

/**
 * A lightweight version of DcModule. 
 */
public class DcWebModule implements Serializable {

    private static final long serialVersionUID = -5554918550508922338L;
    
    private int index;
    private String label;
    
    public DcWebModule(int index, String label) {
        super();
        this.index = index;
        this.label = label;
    }

    public String getIcon16() {
        return "/images/modules/" + DcModules.get(index).getName().toLowerCase() + "16.png";
    }

    public String getIcon32() {
        return "/images/modules/" + DcModules.get(index).getName().toLowerCase() + "32.png";
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
