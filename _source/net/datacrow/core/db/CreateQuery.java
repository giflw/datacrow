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

package net.datacrow.core.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;

import org.apache.log4j.Logger;

public class CreateQuery extends Query {

    private final static Logger logger = Logger.getLogger(CreateQuery.class.getName());
    
    public CreateQuery(int module) throws SQLException {
        super(module, null);
    }

    @Override
    public List<DcObject> run() {
        Connection conn = null;
        Statement stmt = null;

        String columns = "";
        for (DcField field : getModule().getFields()) {
            if (!field.isUiOnly()) {
                if (columns.length() > 0)
                    columns += ", ";

                columns += field.getDatabaseFieldName() + " " + field.getDataBaseFieldType();
                if (field.getIndex() == DcObject._ID) {
                    columns += " PRIMARY KEY";
                }
            }
        }
        
        // TODO: do we really want to use memory tables??
        String sql = "CREATE MEMORY TABLE " + getModule().getTableName() + "\r\n(" + columns + ");";
        
        try { 
            conn = DatabaseManager.getAdminConnection();
            stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException se) {
            logger.error(se, se);
        }
        
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.error("Error while closing connection", e);
        }

        clear();
        return null;
    }
}
