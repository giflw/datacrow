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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.util.Utilities;

public class FieldNodeElement extends NodeElement {
    
    private int field;
    
    public FieldNodeElement(int module, int field, Object key, String displayValue, ImageIcon icon, String clause) {
        super(module, key, displayValue, icon, clause);
        this.field = field;
        setCount(1);
    }
    
    @Override
    public List<Long> getItems(List<NodeElement> parents) {
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

        List<Long> keys = new ArrayList<Long>();
        try {
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
                keys.add(rs.getLong(1));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setCount(keys.size());
        return keys;
    }
    
    public int getField() {
        return field;
    }
}
