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

package net.datacrow.core.security;

import java.util.HashMap;
import java.util.Map;

import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.helpers.Permission;

public final class ModulePermission {

    private int moduleIdx;
    
    private Map<Integer, Permission> fields = new HashMap<Integer, Permission>();
    
    protected ModulePermission(int moduleIdx) {
        this.moduleIdx = moduleIdx;
    }

    public final int getModuleIdx() {
        return moduleIdx;
    }

    public final void setModuleIdx(int moduleIdx) {
        this.moduleIdx = moduleIdx;
    }

    public final Permission getPermision(int fieldIdx) {
        return fields.get(Integer.valueOf(fieldIdx));
    }
    
    public final void addPermission(Permission field) {
        fields.put(Integer.valueOf(field.getFieldIdx()), field);
    }
    
    public final boolean isAuthorized(DcField field) {
        Permission permission = fields.get(Integer.valueOf(field.getIndex()));
        return permission != null ? permission.isViewingAllowed() : true;
    }
    
    public final boolean isAuthorized() {
        for (Permission permission : fields.values()) {
            if (permission.isViewingAllowed())
                return true;
        }
        return false;
    }

    public final boolean isEditingAllowed() {
        for (Permission permission : fields.values()) {
            if (permission.isEditingAllowed())
                return true;
        }
        return false;
    }
}
