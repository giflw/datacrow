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

package net.datacrow.console.components.panels.tree;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class FieldNodeElement extends NodeElement {
    
    private static Logger logger = Logger.getLogger(FieldNodeElement.class.getName());

    private int field;
    
    public FieldNodeElement(int module, int field, Object key, String displayValue, DcImageIcon icon) {
        super(module, key, displayValue, icon);
        this.field = field;
        setCount(1);
    }
    
    @Override
    public List<String> getItems(List<NodeElement> parents) {
        String sql = "select ID from " + DcModules.get(module).getTableName();
        int counter = 0;
        for (NodeElement e : parents) {
            
            if (!Utilities.isEmpty(e.getWhereClause()) && this != e) {
                sql += counter > 0 ? " and ID in (" : " where ID in (";
                sql += e.getWhereClause() + ")";
                counter++;
            }
        }
        
        if (getWhereClause() != null) {
            sql += counter > 0 ? " and ID in (" : " where ID in (";
            sql += getWhereClause() + ")";
        }

        List<String> keys = new ArrayList<String>();
        try {
            Connection conn =  DatabaseManager.getConnection();
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
            int index = 1;
            for (NodeElement e : parents) {
                if (!Utilities.isEmpty(e.getWhereClause()) && e.getKey() != null) {
                    ps.setObject(index, e.getKey());
                    index++;
                }
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                keys.add(rs.getString(1));
            }
            
            logger.debug(ps);
            
            conn.close();
            rs.close();
            ps.close();
        } catch (Exception e) {
            logger.error(e, e);
        }

        setCount(keys.size());
        return keys;
    }
    
    @Override
    public String getWhereClause() {
        DcModule module = DcModules.get(getModule());
        DcField field = module.getField(getField());
        
        if (field == null)
            return DataFilters.getCurrent(getModule()).toSQL(new int[] {DcObject._ID});
        
        if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
            field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
        
            DcModule reference = DcModules.get(field.getReferenceIdx());
            DcModule main = field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE ? module : 
                   DcModules.get(DcModules.getMappingModIdx(getModule(), reference.getIndex(), field.getIndex()));
                
            if (getKey() != null) {
                return "select distinct " +
                        (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ?
                                " main." + main.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() : " main.ID") +
                        " from " + reference.getTableName() + " ref" +
                        " inner join " + main.getTableName() + " main" + 
                        (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ?
                              " on main." + main.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + " = ref.ID" 
                            : " on main." + field.getDatabaseFieldName() + " = ref.ID") +
                         " and ref.ID = ?";
            } else {
                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                    return "select ID from " + DcModules.get(getModule()).getTableName() + " where " + field.getDatabaseFieldName() + " is null";
                } else {
                    return "select ID from " + DcModules.get(getModule()).getTableName() + " where ID not in (" +
                             "select " + main.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() + " from " +
                             main.getTableName() + ")";
                }
            }
        } else {
            if (getKey() != null) {
                return "select ID from " + DcModules.get(getModule()).getTableName() + " where " + field.getDatabaseFieldName() + " = ?";
            } else {
                return "select ID from " + DcModules.get(getModule()).getTableName() + " where " + field.getDatabaseFieldName() + " is null";
            }
        }
    }
    
    public int getField() {
        return field;
    }
}
