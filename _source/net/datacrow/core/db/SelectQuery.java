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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.datacrow.core.data.DataFilter;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.wf.WorkFlow;
import net.datacrow.core.wf.requests.Requests;

import org.apache.log4j.Logger;

public class SelectQuery extends Query {
    
    private final static Logger logger = Logger.getLogger(SelectQuery.class.getName());
    
    private int[] fields;
    private DataFilter df;
    
    /**
     * Constructs a new Query object from a data filter.
     */
    public SelectQuery(DcObject dco, int[] fields) {
        super(dco.getModule().getIndex(), dco.getRequests());
        this.fields = fields;
        this.df = new DataFilter(dco);
    }
    
    /**
     * Constructs a new Query object from a data filter.
     */
    public SelectQuery(DataFilter df, Requests requests, int[] fields) {
        super(df.getModule(), requests);
        this.fields = fields;
        this.df = df;
    }
    
    @SuppressWarnings("resource")
    @Override
    public List<DcObject> run()  {
        boolean success = false;
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<DcObject> items = null;
        String sql = df.toSQL(fields, true, true);
        
        logger.debug(sql);
        
        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            items = WorkFlow.getInstance().convert(rs, fields);
            
            success = true;
            
        } catch (SQLException e) {
            logger.error("Error while executing query: " + sql, e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                logger.error("Error while closing connection", e);
            }
        }
        
        handleRequest(success);
        clear();
        
        return items;
    }

    @Override
    public void clear() {
        super.clear();
        fields = null;
        df = null;
    }
}
