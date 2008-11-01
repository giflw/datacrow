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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.Version;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.User;
import net.datacrow.core.security.SecuredUser;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.core.wf.WorkFlow;
import net.datacrow.core.wf.requests.IRequest;
import net.datacrow.core.wf.requests.Requests;
import net.datacrow.core.wf.requests.SynchronizeWithManagerRequest;
import net.datacrow.settings.DcSettings;

import org.apache.log4j.Logger;

/**
 * @author Robert Jan van der Waals
 */
public class DatabaseManager {

    private static Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    
    private static DcDatabase db = new DcDatabase();
    private static boolean isServerClientMode = false;
    
    public static void initialize() {
        Connection connection = getAdminConnection();

        try {
            db.initiliaze(connection);
        } catch (Exception e) {
            logger.error("Could not find and connect to the database!", e);
            new MessageBox("Could not find or connect to the database!", MessageBox._ERROR);
        }
    }

    public static Version getOriginalVersion() {
        return db.getOriginalVersion();
    }
    
    public static Version getVersion() {
        Connection connection = getAdminConnection();
        if (connection != null)
            return db.getVersion(connection);
        else 
            return new Version(0,0,0,0);
    }
    
    public static int getQueueSize() {
    	return db.getQueueSize();
    }
    
    public static void applySettings() {
        db.setDbProperies(getConnection());
    }
    
    /**
     * Checks whether the database is available. It could be the database is locked.
     */
    public static boolean isLocked() {
        return getAdminConnection() == null;
    }
    
    public static void closeDatabases(boolean compact) {
        try {
            if (db != null) {
                Connection connection = getAdminConnection();
                Statement stmt = connection.createStatement();
                
                if (!isServerClientMode) {
                    if (compact)
                        stmt.executeUpdate("SHUTDOWN COMPACT");
                    else 
                        stmt.executeUpdate("SHUTDOWN");
                }
                
                stmt.close();
                connection.close();
            }
        } catch (Exception exp) {
            logger.error("Error while closing the database (compact = " + compact + ")", exp);
        }
    }

    public static Connection getConnection() {
        SecuredUser su = SecurityCentre.getInstance().getUser();
        if (su == null)
            return getConnection("sa", "");
        else {
            return getConnection(su.getUsername(), su.getPassword());
        }
    }
    
    public static Connection getConnection(String username, String password) {
        String address = null;
        
        try {
            String name = db.getName();
            Class.forName(DcSettings.getString(DcRepository.Settings.stDatabaseDriver));

            if (name.startsWith("//") || name.startsWith("\\\\")) {
                isServerClientMode = true;
                address = "jdbc:hsqldb:hsql:" + name;
            } else {
                isServerClientMode = false;
                address = "jdbc:hsqldb:" + DataCrow.baseDir +  "data/" + name;
            }
            
            Connection connection = DriverManager.getConnection(address, username, password);
            connection.setAutoCommit(true);
            return connection;
            
        } catch (Exception e) {
        	logger.info("User '" + username + "' with password '" + password + "' cannot login");
        }
        
        return null;
    }
    
    public static Collection<DcObject> executeQuery(String sql, int type, boolean log) {
        try {
            return executeQuery(DatabaseManager.getConnection().prepareStatement(sql), type, log);
        } catch (SQLException e) {
            logger.error("There were errors for query " + sql, e);
        }
        return null;
    }     
    
    public static List<DcObject> executeQuery(DcObject dco, boolean log) throws SQLException {
        Query query = new Query(Query._SELECT, dco, null, null);
        return executeQuery(query.getQuery(), Query._SELECT, log);
    }
    
    public static List<DcObject> executeQuery(Query query, boolean logQuery) {
        List<DcObject> data = new ArrayList<DcObject>();
        for (PreparedStatement ps : query.getQueries()) {
            Collection<DcObject> c = executeQuery(ps, query.getType(), logQuery);
            if (c != null) data.addAll(c);
        }
        
        Requests rc = query.getRequestors();
        if (rc != null) {
            IRequest[] requests = rc.get();
            for (int i = 0; i < requests.length; i++) {
                requests[i].execute(data);
            }
        }
        
        return data;
    }      
    
    public static ResultSet executeSQL(String sql, boolean log) throws Exception {
        if (log) logger.info(sql);
        Connection connection = getConnection();
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
    
    public static List<DcObject> executeQuery(PreparedStatement ps, int type, boolean log) {
        List<DcObject> data = null;
        
        try {
            if (    type != Query._SELECT && type != Query._SELECTOR && 
                    type != Query._SELECTPRECISE && type != Query._SELECTPRECISEOR &&
                    type != Query._UNDEFINED) {
                
                ps.execute();
            } else {
                ResultSet result = ps.executeQuery();
                data = new WorkFlow().convertToDCObjects(result);
                result.close();
            }
        } catch (SQLException e) {
            if (!e.getMessage().equals("No ResultSet was produced"))
                logger.error("Error while executing query " + ps, e);
        }

        if (log) {
            String sql = ps.toString();
            int idx = sql.toLowerCase().indexOf("sql=[");
            if (idx > -1)
                sql = sql.substring(idx + 5, (idx + 5 < sql.length() - 2 ? sql.length() - 2 : sql.length()));
            logger.info(sql);
        }

        return data;
    }

    public static void updateValues(DcObject dco) {
        try {
            boolean isChanged = dco.isChanged();
            if (isChanged) {
                Query query = new Query( Query._UPDATE, dco, null, dco.getRequests());
    
                if (dco.isPartOfBatch())
                    query.setBatch(dco.isEndOfBatch());
    
                query.setSilence(dco.isSilent());
                db.addQuery(query);
            }
    
            // save children. do not save children when dealing with an abstract module as
            // this: 1. not needed and 2. causes unwanted side-effects (container > audio cd > audio track)
            Collection<DcObject> children = new ArrayList<DcObject>();
            Collection<DcObject> c = dco.getChildren();
            if (!dco.getModule().isAbstract() && c != null)
                children.addAll(c);
            
            int counter = 1;
            for (DcObject child : children) {
                if (child.isChanged()) {
                    child.addRequest(new SynchronizeWithManagerRequest(
                            SynchronizeWithManagerRequest._UPDATE, child));
                    
                    boolean exists = false;
                    if (child.getID() != null && child.getID().length() > 0) {
                        DcObject childTest = child.getModule().getDcObject();
                        childTest.setValue(DcObject._ID, child.getID());
                        Collection<DcObject> objects = executeQuery(childTest, true);
                        exists = objects.size() > 0;
                    }
    
                    Query query;
                    if (!exists)
                        query = new Query(Query._INSERT, child, null, child.getRequests());
                    else
                        query = new Query(Query._UPDATE, child, null, child.getRequests());
    
                    if (!isChanged) {
                        query.setBatch(counter == children.size());
                        query.setSilence(counter != children.size());
                    } else {
                        query.setSilence(true);
                    }
                    db.addQuery(query);
                }
            }
        } catch (SQLException e) {
            logger.error(e, e);
        }            
    }

    public static void insertValues(DcObject dco) {
        try {
            if (dco.getID() == null || dco.getID().equals(""))
                dco.setIDs();
    
            Query query = new Query(Query._INSERT, dco, null, dco.getRequests());
            if (dco.isPartOfBatch())
                query.setBatch(dco.isEndOfBatch());
    
            query.setSilence(dco.isSilent());
            db.addQuery(query);
    
            Collection<DcObject> children = dco.getChildren();
            
            if (children != null) {
                for (DcObject child : children) {
                    child.addRequest(new SynchronizeWithManagerRequest(
                            SynchronizeWithManagerRequest._ADD, child));
                    
                    child.setValue(child.getParentReferenceFieldIndex(), dco.getID());
                    query = new Query(Query._INSERT, child, null, child.getRequests());
                    query.setSilence(true);
                    db.addQuery(query);
                }
            }
        } catch (SQLException e) {
            logger.error(e, e);
        }            
    }

    public static  void deleteValues(DcObject dco) {
        try {
            Query query = new Query(Query._DELETE, dco, null, dco.getRequests());
            if (dco.isPartOfBatch())
                query.setBatch(dco.isEndOfBatch());
        
            query.setSilence(dco.isSilent());
            db.addQuery(query);
        } catch (SQLException e) {
            logger.error(e, e);
        }
    }

    public static boolean isUnique(DcObject o, boolean isUpdate) {
        if (o.hasPrimaryKey() && !o.getModule().isChildModule()) {
            boolean hasRequiredFields = false;
            DcObject dco = o.getModule().getDcObject();

            int[] fields = o.getFieldIndices();
            for (int i = 0 ; i < fields.length; i++) {
                int field = fields[i];
                if (o.isRequired(field)) {
                    hasRequiredFields = true;
                    dco.setValue(field, o.getValue(field));
                }
            }

            if (hasRequiredFields) {
                DataFilter df = new DataFilter(dco);
                DcObject[] objects = DataManager.get(o.getModule().getIndex(), df);
                
                int count = 0;
                for (int i = 0; i < objects.length; i++)
                	count = !isUpdate || !objects[i].getID().equals(o.getID()) ? count + 1 : count;

                if (count > 0)
                    return false;
            }
        }
        return true;
    }
    
    /*****************************************************************************************
     * Security methods
     *****************************************************************************************/
    
    protected static Connection getAdminConnection() {
    	return DatabaseManager.getConnection("dc_admin", "UK*soccer*96");
    }
    
    public static void changePassword(User user, String password) {
        Connection c = null;
        Statement stmt = null;
        
        try {
            c = getAdminConnection();
            stmt = c.createStatement();

            String sql = "ALTER USER '" + user.getValue(User._A_LOGINNAME) + "' SET PASSWORD '" + password + "'";
            stmt.execute(sql);
            
        } catch (SQLException se) {
            logger.error(se, se);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (c != null) c.close();
            } catch (Exception e) {}
        }
    }
    
    /**
     * Removes a user from the database
     */
    public static void deleteUser(User user) {
        Connection c = null;
        Statement stmt = null;
        
        try {
            c = DatabaseManager.getConnection();
            stmt = c.createStatement();

            String sql = "DROP USER " + user.getValue(User._A_LOGINNAME);
            stmt.execute(sql);
            
            SecurityCentre.getInstance().logoff(user);
            
        } catch (SQLException se) {
            logger.error(se, se);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (c != null) c.close();
            } catch (Exception e) {}
        }
    }
    
    /**
     * Creates a user with all the correct privileges
     */
    public static void createUser(User user, String password) {
        Connection c = null;
        Statement stmt = null;
        
        try {
            c = DatabaseManager.getConnection();
            stmt = c.createStatement();

            String sql = "CREATE USER '" + user.getValue(User._A_LOGINNAME) + "' PASSWORD '" + password + "'";
            if (user.isAdmin()) 
                sql += " ADMIN";
            
            stmt.execute(sql);

            setPriviliges(user);
            
        } catch (SQLException se) {
            logger.error(se, se);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (c != null) c.close();
            } catch (Exception e) {}
        }
    }    
    
    /**
     * Updates the privileges of an existing user.
     */
    public static void setPriviliges(User user) {
        for (DcModule module : DcModules.getAllModules())
            setPriviliges(module, user);
    }
    
    public static void setPriviliges(String user, boolean admin) {
        for (DcModule module : DcModules.getAllModules())
            setPriviliges(module, user, admin);
    }
    
    protected static void setPriviliges(DcModule module, User user) {
        if (user == null)
            return;
        
        setPriviliges(module, (String) user.getValue(User._A_LOGINNAME), user.isAdmin());
    } 
        
   protected static void setPriviliges(DcModule module, String user, boolean admin) {

       Connection c = null;
       Statement stmt = null;
       
       try {
            
            String tablename = module.getTableName();
            
            if (tablename == null || tablename.trim().length() == 0)
                return;
            
            c = DatabaseManager.getAdminConnection();
            stmt = c.createStatement();

            // check if the table exists
            try {
                String sql = "SELECT TOP 1 * from " + tablename;
                stmt.execute(sql);
            } catch (SQLException se) {
                // the table has not been created yet; empty database.
                return;
            }
            
            String sql = "REVOKE ALL ON " + tablename + " FROM " + user;
            stmt.execute(sql);
            
            if (module.isEnabled()) {
                
                if (admin) {
                    sql = "GRANT ALL ON " + tablename + " TO " + user;
                    stmt.execute(sql);

                } else {
                    sql = "GRANT SELECT ON " + tablename + " TO " + user;
                    stmt.execute(sql);
                    
                    if (module.isEditingAllowd()) {
                        sql = "GRANT UPDATE ON " + tablename + " TO " + user;
                        stmt.execute(sql);
                        sql = "GRANT INSERT ON " + tablename + " TO " + user;
                        stmt.execute(sql);
                    }
                    
                    if (admin) {
                        sql = "GRANT DELETE ON " + tablename + " TO " + user;
                        stmt.execute(sql);
                    }
                }
            }
           
        } catch (SQLException se) {
            logger.error(se, se);
            
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (c != null) c.close();
            } catch (Exception e) {}
        }
    }    
}

