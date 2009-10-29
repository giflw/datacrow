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

/**
 * A mapping represents a many to many relationship.
 * Mappings are stored in cross reference database tables.
 * 
 * @author Robert Jan van der Waals
 */
public class DcMapping extends DcObject {
    
    private static final long serialVersionUID = 1314886460279316879L;
    
    public static final int _A_PARENT_ID = 1;
    public static final int _B_REFERENCED_ID = 2;
    public static final int _C_DISPLAY_STRING = 3;
    
    private DcObject referencedObj;
    
    /**
     * Creates a new instance.
     * @param module
     */
    public DcMapping(int module) {
        super(module);
    }

    /**
     * The icon. 
     * @return Always returns null.
     */
    @Override
    public ImageIcon getIcon() {
        return getReferencedObject() != null ? getReferencedObject().getIcon() : null;
    } 
    
    /**
     * The filename to which this module is stored.
     * @return Always returns null.
     */
    @Override
    public String getFilename() {
        return null;
    }
    
    /**
     * Sets the referenced object (the child).
     * @param referencedObj
     */
    public void setReferencedObject(DcObject referencedObj) {
        this.referencedObj = referencedObj;
    }
    
    /**
     * Retrieves the referenced object.
     */
    public DcObject getReferencedObject() {
        return referencedObj == null ? 
               DataManager.getObject(getReferencedModuleIdx(), getReferencedId()) : referencedObj;
    }
    
    /**
     * The parent module index.
     */
    public int getParentModuleIdx() {
        return getField(_A_PARENT_ID).getReferenceIdx();
    }

    /**
     * The referenced module index.
     */
    public int getReferencedModuleIdx() {
        return getField(_B_REFERENCED_ID).getSourceModuleIdx();
    }

    /**
     * The object ID of the parent.
     */
    public String getParentId() {
        return (String) getValue(_A_PARENT_ID);
    }

    /**
     * The object ID of the referenced item.
     */
    public String getReferencedId() {
        return (String) getValue(_B_REFERENCED_ID);
    }
    
    @Override
    public String toString() {
        DcObject dco = null;
        
        try {
            dco = getReferencedObject();
        } catch (Exception e) {}
            
        return dco == null ? "" : dco.toString();
    }
}
