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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.Version;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.settings.DcSettings;

import org.apache.log4j.Logger;

public class DcDatabase {

    private static Logger logger = Logger.getLogger(DcDatabase.class.getName());
    private QueryQueue queue;
    
    private Version originalVersion;

    public DcDatabase() {}
    
    public Version getOriginalVersion() {
        return originalVersion;
    }

    public Version getVersion(Connection connection) {
        int major = 0;
        int minor = 0;
        int build = 0;
        int patch = 0;

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM VERSION");
            
            while (rs.next()) {
                major = rs.getInt("major");
                minor = rs.getInt("minor");
                build = rs.getInt("build");
                patch = rs.getInt("patch");
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException se) {}
        
        return new Version(major, minor, build, patch);
    }
    
    private void updateVersion(Connection connection) throws SQLException {
        Version v = DataCrow.getVersion();
        Statement stmt = connection.createStatement();
        stmt.execute("DELETE FROM VERSION");
        stmt.execute("INSERT INTO VERSION(MAJOR, MINOR, BUILD, PATCH) VALUES (" + 
                     v.getMajor() + "," + v.getMinor() + "," + v.getBuild() + "," + v.getPatch() + ")");
    }
    
    public void initiliaze(Connection connection) throws Exception {
        
        new DatabaseUpgrade().start();
        
        startQueryQueue();
        initialize(connection);
        setDbProperies(connection);
        originalVersion = getVersion(connection);
        
        updateVersion(connection);

        // Set the database privileges for the current user. This avoids errors for upgraded modules and such. 
        DatabaseManager.setPriviliges(SecurityCentre.getInstance().getUser().getUser());
        
        try {
            connection.close();
        } catch (Exception e) {
            logger.error(e, e);
        }
    }

    public int getQueueSize() {
    	return queue.getQueueSize();
    }
    
    private void startQueryQueue() {
        queue = new QueryQueue();
        Thread queryQueue = new Thread(queue, "queryQueue");
        queryQueue.setPriority(Thread.NORM_PRIORITY);
        queryQueue.setDaemon(true);
        queryQueue.start();
    }

    public String getName() {
        return DcSettings.getString(DcRepository.Settings.stConnectionString);
    }

    public void addQuery(Query query) {
        queue.addQuery(query);
    }

    public void setDbProperies(Connection connection) {
        try {
            
            Statement stmt = connection.createStatement();
            stmt.execute("SET SCRIPTFORMAT COMPRESSED");
            int cacheScale = DcSettings.getInt(DcRepository.Settings.stHsqlCacheScale);
            int cacheSizeScale = DcSettings.getInt(DcRepository.Settings.stHsqlCacheSizeScale);

            stmt.execute("SET PROPERTY \"hsqldb.cache_scale\" " + cacheScale);
            stmt.execute("SET PROPERTY \"hsqldb.cache_size_scale\" " + cacheSizeScale);
            stmt.execute("SET LOGSIZE 200");
            
            stmt.close();
        } catch (Exception e) {
            logger.error(DcResources.getText("msgUnableToChangeDbSettings"), e);
        }
    }

    private void initialize(Connection connection) throws Exception {
        Statement stmt = connection.createStatement();

        initializeSystemTable(stmt);
        
        for (DcModule module : DcModules.getAllModules()) {
            DcObject dco = module.getDcObject();
            if (!dco.getTableName().equals("")) {
                String testQuery = "select * from " + dco.getTableName();
                try {
                    stmt.setMaxRows(1);
                    ResultSet result = stmt.executeQuery(testQuery);
                    initializeColumns(connection, result.getMetaData(), dco);
                    logger.info(DcResources.getText("msgTableFound", dco.getTableName()));
                    result.close();
                } catch (SQLException e) {
                    logger.info((DcResources.getText("msgTableNotFound", dco.getTableName())));
                    createTable(dco);
                }
            }
        }
        stmt.close();
    }
    
    private void initializeSystemTable(Statement stmt) {
        try {
            stmt.execute("SELECT * FROM VERSION");
        } catch (Exception e) {
            try {
                stmt.execute("CREATE TABLE VERSION (Major " + DcRepository.Database._FIELDBIGINT + "," +
                                                   "Minor " + DcRepository.Database._FIELDBIGINT + "," +
                                                   "Build " + DcRepository.Database._FIELDBIGINT + "," +
                                                   "Patch " + DcRepository.Database._FIELDBIGINT + ")");
            } catch (SQLException se) {
                logger.error("Could not create the version table!", se);
            }
        }
    }

    private void initializeColumns(Connection connection, ResultSetMetaData metaData, DcObject dco) throws SQLException {
        String tableName = dco.getTableName();
        
        for (DcField field : dco.getFields()) {
            String column = field.getDatabaseFieldName();
            String type = field.getDataBaseFieldType();
            
            boolean found = false;
            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                if (metaData.getColumnName(i).toUpperCase().equals(column.toUpperCase())) {
                    found = true;
                    int dbSize = metaData.getColumnDisplaySize(i);
                    if (    dbSize < field.getMaximumLength() && 
                            field.getValueType() == DcRepository.ValueTypes._STRING) {
                        logger.info(DcResources.getText("msgTableUpgradeIncorrectColumn", new String[] {tableName, field.getLabel()}));
                        executeQuery(connection, "alter table " + tableName + " alter column " + column + " " + type);
                    }
                }
            }
            
            if (!field.isUiOnly() && !found) {
                logger.info(DcResources.getText("msgTableUpgradeMissingColumn", new String[] {tableName, field.getLabel()}));
                executeQuery(connection, "alter table " + tableName + " add column " + column + " " + type);
                //DatabaseManager.setPriviliges(user)
                DataManager.setUseCache(false);
            }
        }
    }

    private void executeQuery(Connection connection, String sql) {
        try {
            executeQuery(connection.prepareStatement(sql));
        } catch (Exception e) {
            logger.error("Error while executing query " + sql, e);
        }
    }
    
    private void executeQuery(PreparedStatement ps) {
        try {
            ps.execute();
            logger.info(ps);
            ps.close();
        } catch (Exception e) {
            logger.error("Error while executing query " + ps, e);
        }
    }

    private void createTable(DcObject dco) {
        try {
        	DataManager.setUseCache(false);
        	
            Query query = new Query(Query._CREATE, dco, null, null);
            executeQuery(query.getQuery());
            
            DcObject[] objects = dco.getModule().getDefaultData();
            if (objects != null) {
                for (int i = 0; i < objects.length; i++) {
                    objects[i].setIDs();
                    objects[i].setSynchronizeWithDM(false);
                    objects[i].setSilent(true);
                    objects[i].saveNew(false);
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred while inserting demo data", e);
        }
    }
}
