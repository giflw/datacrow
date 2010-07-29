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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.Version;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.MappingModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.User;
import net.datacrow.core.security.SecuredUser;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.util.DcSwingUtilities;

import org.apache.log4j.Logger;

/**
 * The database manager is responsible for all databases.
 * This class is the only service providing access to the databases.
 * 
 * @author Robert Jan van der Waals
 */
public class DatabaseManager {

    private static Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    
    private static DcDatabase db = new DcDatabase();
    private static boolean isServerClientMode = false;
    
    public static boolean initialized = false;
   
    /**
     * Initializes the database. A connection with the HSQL database engine is established
     * and the if needed the databases are upgraded.
     */
    public static void initialize() {
        
        try {
            long start = logger.isDebugEnabled() ? new Date().getTime() : 0;

            db.initiliaze();
            
            if (logger.isDebugEnabled()) {
                long end = new Date().getTime();
                logger.debug("Initialization of the database (DcDatabase) took " + (end - start) + "ms");
            }  
            
            start = logger.isDebugEnabled() ? new Date().getTime() : 0;
            
            db.getConversions().load();
            db.getConversions().execute();

            if (logger.isDebugEnabled()) {
                long end = new Date().getTime();
                logger.debug("Execution of the database conversion scripts took " + (end - start) + "ms");
            }  

            start = logger.isDebugEnabled() ? new Date().getTime() : 0;
            
            db.cleanup();
            
            if (logger.isDebugEnabled()) {
                long end = new Date().getTime();
                logger.debug("Database cleanup took " + (end - start) + "ms");
            }  
            
            initialized = true;

        } catch (Exception e) {
            logger.error("Could not find and connect to the database!", e);
            DcSwingUtilities.displayErrorMessage("Could not find or connect to the database!");
        }
    }

    /**
     * Retrieves the original database version (the version before the database was upgraded). 
     * @return
     */
    public static Version getOriginalVersion() {
        return db.getOriginalVersion();
    }
    
    /**
     * Retrieves the current database version.
     * @return The current version. If version information could not be found an undetermined
     * version is returned.
     */
    public static Version getVersion() {
        Connection connection = getAdminConnection();
        if (connection != null)
            return db.getVersion(connection);
        else 
            return new Version(0,0,0,0);
    }
    
    /**
     * Retrieves the count of currently queued queries. 
     */
    public static int getQueueSize() {
    	return db.getQueueSize();
    }
    
    /**
     * Apply settings on the databases. 
     */
    public static void applySettings() {
        db.setDbProperies(getConnection());
    }
    
    /**
     * Checks whether the database is available. It could be the database is locked.
     */
    public static boolean isLocked() {
        return getAdminConnection() == null;
    }
    
    /**
     * Closes the database connections.
     * @param compact Indicates if the database should be compacted.
     */
    public static void closeDatabases(boolean compact) {
        try {
            if (db != null) {
                
                // calculates the conversions based on the alter module wizard
                Conversions conversions = db.getConversions();
                conversions.calculate();
                conversions.save();
                
                Connection connection = getAdminConnection();
                Statement stmt = connection.createStatement();
                
                if (!isServerClientMode) {
                    if (compact)
                        stmt.execute("SHUTDOWN COMPACT");
                    else 
                        stmt.execute("SHUTDOWN");
                }
                
                stmt.close();
                connection.close();
                
                // just to make sure the database is really released..
                org.hsqldb.DatabaseManager.closeDatabases(0);
            }
        } catch (Exception exp) {
            logger.error("Error while closing the database (compact = " + compact + ")", exp);
        }
    }

    /**
     * Returns a new connection to the database based on the logged on user.
     */
    public static Connection getConnection() {
        SecuredUser su = SecurityCentre.getInstance().getUser();
        if (su == null)
            return getConnection("sa", "");
        else {
            return getConnection(su.getUsername(), su.getPassword());
        }
    }
    
    /**
     * Returns a connection for the given user credentials.
     * @param username
     * @param password
     * @return
     */
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
                address = "jdbc:hsqldb:" + DataCrow.dataDir + name;
            }
            
            Connection connection = DriverManager.getConnection(address, username, password);
            connection.setAutoCommit(true);
            return connection;
            
        } catch (Exception e) {
            logger.debug(e, e);
            
            if (!username.equals("dc_admin"))
                logger.debug("User '" + username + "' with password '" + password + "' cannot login");
        }
        
        return null;
    }
    
    
    
//    /**
//     * Executes a query.
//     * @param sql The SQL statement.
//     * @param type The query type ({@link Query}).
//     * @param log Indicates if information on the query should be logged.
//     * @return The retrieved items or null when an error occurs.
//     */
//    public static void runSQL(String sql) {
//        try {
//            Connection conn = DatabaseManager.getConnection();
//            conn.createStatement().execute(sql);
//            conn.close();
//        } catch (SQLException e) {
//            logger.error("There were errors for query " + sql, e);
//        }
//    }     
    
//    /**
//     * Executes a query. 
//     * @param ps The prepared SQL statement.
//     * @param type The query type ({@link Query}.
//     * @param log Indicates if information on the query should be logged.
//     * @return The retrieved items or null when an error occurs.
//     * 
//     * @deprecated
//     */
//    public static List<DcObject> retrieveItems(PreparedStatement ps, int type) {
//        List<DcObject> data = null;
//        
//        try {
//            if (    type != Query._SELECT && type != Query._SELECTOR && 
//                    type != Query._SELECTPRECISE && type != Query._SELECTPRECISEOR &&
//                    type != Query._UNDEFINED) {
//                
//                ps.execute();
//            } else {
//                ResultSet result = ps.executeQuery();
//                data = WorkFlow.getInstance().convert(result, null);
//                result.close();
//            }
//        } catch (SQLException e) {
//            if (!e.getMessage().equals("No ResultSet was produced"))
//                logger.error("Error while executing query " + ps, e);
//        }
//        
//        try {
//            ps.close();
//        } catch (SQLException e) {
//            logger.error(e, e);
//        }
//
//        return data;
//    }    
    
    public static List<Long> getKeys(DataFilter filter) {
        List<Long> data = new ArrayList<Long>();

        try {
            ResultSet rs = DatabaseManager.executeSQL(filter.toSQL(new int[] {DcObject._ID}));
            
            while (rs.next())
                data.add(rs.getLong(1));
            
            rs.close();
        } catch (SQLException e) {
            if (!e.getMessage().equals("No ResultSet was produced"))
                logger.error("Error while executing query", e);
        }
        return data;
    }

    /**
     * Executes a query. 
     * @param sql SQL statement.
     * @param log Indicates if information on the query should be logged.
     * @return The result set.
     */
    public static ResultSet executeSQL(String sql) throws SQLException {
        Connection connection = getConnection();
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
    
    /**
     * Update the item in the database with the values from the specified item.
     * @param dco
     */
    public static void update(DcObject dco) {
        if (dco.isChanged()) 
            db.queue(new UpdateQuery(dco));
    }

    /**
     * Stores the item in the database.
     * @param dco
     */
    public static void insert(DcObject dco) {
        try {
            Query query = new InsertQuery(dco);
            db.queue(query);
        } catch (SQLException e) {
            logger.error(e, e);
        }            
    }

    public static  void delete(DcObject dco) {
        try {
            db.queue(new DeleteQuery(dco));
        } catch (SQLException e) {
            logger.error(e, e);
        }
    }

    /**
     * Checks the database to see if the item already exists.
     * @param o The item to check.
     * @param isExisting Indicates if the check is performed for a new or an existing item.
     */
    public static boolean isUnique(DcObject o, boolean isExisting) {
        if (o.hasPrimaryKey() && !o.getModule().isChildModule()) {
            boolean hasUniqueFields = false;
            DcObject dco = o.getModule().getItem();

            for (DcFieldDefinition def : o.getModule().getFieldDefinitions().getDefinitions()) {
                if (def.isUnique()) {
                    dco.setValue(def.getIndex(), o.getValue(def.getIndex()));
                    hasUniqueFields = true;
                }
            }
                
            if (hasUniqueFields) {
                DataFilter df = new DataFilter(dco);
                List<DcObject> items = DataManager.get(df);
                
                int count = 0;
                for (DcObject item : items)
                	count = !isExisting || !item.getID().equals(o.getID()) ? count + 1 : count;

                if (count > 0) return false;
            }
            
            dco.release();
        }
        return true;
    }
    
    /*****************************************************************************************
     * Security methods
     *****************************************************************************************/

    /**
     * Creates an admin connection to the database.
     */
    public static Connection getAdminConnection() {
    	return DatabaseManager.getConnection("dc_admin", "UK*soccer*96");
    }
    
    /**
     * Change the password for the given user.
     * @param user
     * @param password
     */
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
     * @param user
     */
    public static void setPriviliges(User user) {
        
        long start = logger.isDebugEnabled() ? new Date().getTime() : 0;
        
        for (DcModule module : DcModules.getAllModules())
            setPriviliges(module, user);
        
        if (logger.isDebugEnabled()) {
            long end = new Date().getTime();
            logger.debug("Setting the correct database privileges " + (end - start) + "ms");
        }  
    }
    
    /**
     * Updates the privileges of an existing user. 
     * @param user
     * @param admin Indicates if the user is an administrator.
     */
    public static void setPriviliges(String user, boolean admin) {
        for (DcModule module : DcModules.getAllModules())
            setPriviliges(module, user, admin);
    }
    
    protected static void setPriviliges(DcModule module, User user) {
        if (user == null)
            return;
        
        setPriviliges(module, (String) user.getValue(User._A_LOGINNAME), user.isAdmin());
    } 
        
    /**
     * Applies the users privileges on the database tables and columns.
     * @param module
     * @param user
     * @param admin
     */
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
                
                if (admin || module instanceof MappingModule) {
                    sql = "GRANT DELETE ON " + tablename + " TO " + user;
                    stmt.execute(sql);
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

