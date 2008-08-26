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

import javax.swing.ImageIcon;

import net.datacrow.core.data.DataManager;
import net.datacrow.settings.definitions.DcFieldDefinitions;

public class DcMapping extends DcObject {
    
    private static final long serialVersionUID = 1314886460279316879L;
    
    public static final int _A_PARENT_ID = 1;
    public static final int _B_REFERENCED_ID = 2;
    public static final int _C_DISPLAY_STRING = 3;
    
    private DcObject referencedObj;
    
    public DcMapping(int module) {
        super(module);
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    } 
    
    @Override
    public String getFilename() {
        return null;
    }
    
    public void setReferencedObject(DcObject referencedObj) {
        this.referencedObj = referencedObj;
    }
    
    public DcObject getReferencedObject() {
        return referencedObj == null ? 
               DataManager.getObject(getReferencedModuleIdx(), getReferencedId()) : referencedObj;
    }
    
    public int getParentModuleIdx() {
        return getField(_A_PARENT_ID).getReferenceIdx();
    }

    public int getReferencedModuleIdx() {
        return getField(_B_REFERENCED_ID).getSourceModuleIdx();
    }
    
    public String getParentId() {
        return (String) getValue(_A_PARENT_ID);
    }

    public String getReferencedId() {
        return (String) getValue(_B_REFERENCED_ID);
    }
    
    @Override
    public String toString() {
        DcObject dco = null;
        
        try {
            dco = getReferencedObject();
        } catch (Exception ignore) {}
            
        return dco == null ? "" : dco.toString();
    }
    
    @Override
    public void applySettings(DcFieldDefinitions definitions) {}

    protected void setModuleIndex() {}
}
