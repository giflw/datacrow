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

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.swing.ImageIcon;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.windows.LogForm;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.windows.messageboxes.QuestionBox;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.Version;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcAssociate;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.helpers.Container;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.Directory;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * Converts the current database before the actual module tables are created / updated.
 * This means that the code here defies workflow logic and is strictly to be used for
 * table conversions and migration out of the scope of the normal module upgrade code.
 * 
 * The automatic database correction script runs after this manual upgrade.
 * 
 * @author rj.vanderwaals
 */
public class DatabaseUpgrade {
    
    private static Logger logger = Logger.getLogger(DatabaseUpgrade.class.getName());
    
    public void start() {

        try {
            Version version = DatabaseManager.getVersion();
            
            if (!isNewDatabase() && version.isOlder(DataCrow.getVersion())) {

                if (version.isUndetermined()) {
                    QuestionBox qb = new QuestionBox(DcResources.getText("msgCouldNotDetermineVersion"));
                    
                    if (!qb.isAffirmative())
                        System.exit(0);
                }
                
                convertAssociateNames();
                convertAssociateNames2();
                moveImages();
                convertFileSize();
                convertNumberColumns(version);
                repairCrossTables(version);
                convertRatings(version);
                convertLocations();

                LogForm.getInstance().close();
                
                DataManager.setUseCache(false);
            }

            removeInvalidImages();
            convertDates();
            
        } catch (DatabaseUpgradeException due) {
            String msg = due.getMessage() + ". Upgrade failed. " +
                "Please restore your latest Backup and retry. Contact the developer " +
                "if the error persists";
            new MessageBox(msg, MessageBox._ERROR);
            logger.error(msg, due);
        }
    }
    
    
    /************************************************
     * Invalid images removal
     ************************************************/

    private void convertDates() throws DatabaseUpgradeException {
    	
    	if (isNewDatabase()) return;

    	Connection conn = null;
    	Statement stmt = null;

    	boolean needsConversion = false;
    	try {
    		conn = DatabaseManager.getAdminConnection();
            stmt = getSqlStatement(conn);
            try {
                // If the location column exists the database needs to be upgraded.
                ResultSet rs = conn.getMetaData().getColumns(null, null, "SOFTWARE", "CREATED");
                rs.next();
                
                String type = rs.getString("TYPE_NAME");
                needsConversion = type.equals("VARCHAR"); 
                
                rs.close();
                
            } catch (Exception e) {}
    		
            if (!needsConversion) {
            	closeDbConnection(conn, stmt);
            	return;
            }

            QuestionBox qb = new QuestionBox("Upgrade for version < 3.4.0: Created and Modified columns will be converted to date columns");
            
            if (!qb.isAffirmative()) return;
            
            DataManager.setUseCache(false);
            DataCrow.showSplashScreen(false);
            LogForm.getInstance().setVisible(true);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            for (DcModule module : DcModules.getAllModules()) {

            	DcField field1 = module.getField(DcObject._SYS_CREATED);
            	DcField field2 = module.getField(DcObject._SYS_MODIFIED);

            	if (field1 == null || field2 == null ||
            	    module.isAbstract() || Utilities.isEmpty(module.getTableName())) continue;
            	
                try {
                    stmt.executeQuery("SELECT TOP 1 * FROM " + module.getTableName());
                } catch (Exception e) {
                    continue;
                }
            	
            	logger.info("Converting dates for " + module.getTableName());

            	stmt.execute("ALTER TABLE " + module.getTableName() + " ALTER COLUMN " + field1.getDatabaseFieldName() + " RENAME TO OLD1");
            	stmt.execute("ALTER TABLE " + module.getTableName() + " ALTER COLUMN " + field2.getDatabaseFieldName() + " RENAME TO OLD2");
            	
            	stmt.execute("ALTER TABLE " + module.getTableName() + " ADD COLUMN " + field1.getDatabaseFieldName() + " date");
            	stmt.execute("ALTER TABLE " + module.getTableName() + " ADD COLUMN " + field2.getDatabaseFieldName() + " date");

            	ResultSet rs = stmt.executeQuery("SELECT ID, OLD1, OLD2 from " + module.getTableName() + " WHERE OLD1 IS NOT NULL OR OLD2 IS NOT NULL");
            	PreparedStatement ps = conn.prepareStatement("UPDATE " + module.getTableName() +  
            			                                     " SET " + field1.getDatabaseFieldName() + " = ?, " + 
            	                                             field2.getDatabaseFieldName() + " = ? WHERE ID = ?");
            	try {
	            	while (rs.next()) {
	            		String s1 = rs.getString("OLD1");
	            		String s2 = rs.getString("OLD2");
	            		String ID = rs.getString("ID");
	            		
	            		if (s1 != null) {
		        			try {
		                        ps.setDate(1, new java.sql.Date(sdf.parse(s1).getTime()));
		                    } catch (ParseException e) {
		                        ps.setNull(1, Types.NULL);
		                    }
	            		} else {
	            			ps.setNull(1, Types.NULL);
	            		}
	
	            		if (s2 != null) {
		        			try {
		                        ps.setDate(2, new java.sql.Date(sdf.parse(s2).getTime()));
		                    } catch (ParseException e) {
		                        ps.setNull(2, Types.NULL);
		                    }
	            		} else {
	            			ps.setNull(2, Types.NULL);
	            		}
	
	            		ps.setString(3, ID);
	            		ps.execute();
	            	}
            	} catch (Exception e) {
            		logger.error(e, e);
            	}

            	rs.close();
            	ps.close();
            	
            	stmt.execute("ALTER TABLE " + module.getTableName() + " DROP COLUMN OLD1");
            	stmt.execute("ALTER TABLE " + module.getTableName() + " DROP COLUMN OLD2");
            }
    	} catch (SQLException se) {
    		throw new DatabaseUpgradeException("Date conversion failed", se);
    	}
    	
    	closeDbConnection(conn, stmt);
    }
    
    /************************************************
     * Invalid images removal
     ************************************************/

    private void removeInvalidImages() {
    	Connection conn = null;
    	Statement stmt = null;
    	try {
            conn = DatabaseManager.getAdminConnection();
            stmt = getSqlStatement(conn);
    		
    		// If the location column exists the database needs to be upgraded.
            ResultSet rs = stmt.executeQuery("SELECT OBJECTID, FIELD FROM PICTURE WHERE " +
            		                         "(HEIGHT IS NULL OR HEIGHT < 1) OR " +
            		                         "(WIDTH IS NULL OR WIDTH < 1)");
            
            PreparedStatement ps = conn.prepareStatement("DELETE FROM PICTURE WHERE OBJECTID = ? AND FIELD = ?");
            while (rs.next()) {
            	long objectID = rs.getLong("OBJECTID");
            	String field = rs.getString("FIELD");
            	
            	ps.setLong(1, objectID);
            	ps.setString(2, field);
            	
            	ps.execute();
            	
            	// reload from the database!
            	DataManager.setUseCache(false);
            }
            
        } catch (Exception e) {
        	logger.error("Could not removed incorrect picture references from the database", e);
        }
    	
        try {
        	closeDbConnection(conn, stmt);
        } catch (Exception e) {
        	logger.error("Error while closing the database connection", e);
        }
    }

    /************************************************
     * Name conversions
     ************************************************/

    private void convertLocations() throws DatabaseUpgradeException {
        Connection conn = DatabaseManager.getAdminConnection();
        Statement stmt = getSqlStatement(conn);
        try {
            // If the location column exists the database needs to be upgraded.
            stmt.execute("SELECT TOP 1 LOCATION FROM SOFTWARE");
        } catch (Exception e) {
            closeDbConnection(conn, stmt);
            return;
        }
        
        DataCrow.showSplashScreen(false);
        
        DcModule cm = DcModules.get(DcModules._CONTAINER);
        try {
            // Check if the container table exists
            stmt.execute("SELECT TOP 1 NAME FROM " + cm.getTableName());
        } catch (Exception e) {
            try {
                Query query = new Query(Query._CREATE, cm.getDcObject(), null, null);
                PreparedStatement ps = query.getQuery();
                ps.execute();
            } catch (SQLException se) {
                throw new DatabaseUpgradeException("Could not create the container table. Upgrade failed.", se);
            }
        }
        
        QuestionBox qb = new QuestionBox("Upgrade for version < 3.4.0: Locations will be converted " +
        		                         "to the new container structure. Continue?");
        
        if (!qb.isAffirmative()) return; 
        
        LogForm.getInstance().setVisible(true);

        Collection<DcObject> containers = new ArrayList<DcObject>();
        for (DcModule module : DcModules.getAllModules()) {
                            
            if (!module.isContainerManaged() || module.isAbstract())
                continue;
            
            DcField field = module.getField(DcObject._SYS_CONTAINER);
                 
            try {
                stmt.execute("ALTER TABLE " + module.getTableName() + " ADD COLUMN " + 
                             field.getDatabaseFieldName() + " " + field.getDataBaseFieldType());
                
            } catch (SQLException se) {}
            
            try {
                ResultSet rs = stmt.executeQuery("SELECT ID, LOCATION FROM " + module.getTableName());

                PreparedStatement psIns = conn.prepareStatement(
                        "INSERT INTO " + cm.getTableName() + " (ID, NAME) VALUES (?, ?)");
                
                PreparedStatement psUpd = conn.prepareStatement(
                        "UPDATE " + module.getTableName() + " SET " + field.getDatabaseFieldName() + " = ? " +
                        "WHERE ID = ?");
                
                while (rs.next()) {
                    try {
                        String name = rs.getString("LOCATION");
                        String ID = rs.getString("ID");
                        
                        if (Utilities.isEmpty(name))
                            continue;
                        
                        name = name.trim();
                        DcObject container = null;
                        for (DcObject c : containers) {
                            if (name.equals(c.getValue(Container._A_NAME)))
                                container = c;
                        }
                        
                        if (container == null) {
                            container = cm.getDcObject();
                            container.setIDs();
                            container.setValue(Container._A_NAME, name);
                            
                            psIns.setString(1, container.getID());
                            psIns.setString(2, name);
                            psIns.execute();
                            
                            containers.add(container);
                        }
                        
                        psUpd.setLong(1, Long.valueOf(container.getID()));
                        psUpd.setLong(2, Long.valueOf(ID));
                        psUpd.execute();
                        
                    } catch (SQLException se) {
                        logger.error("Error while creating container for " + module.getName(), se);
                    }
                }
                
                try {
                    stmt.execute("ALTER TABLE " + module.getTableName() + " DROP COLUMN LOCATION");
                    
                } catch (SQLException se) {}
                
                rs.close();
                psIns.close();
                psUpd.close();
            } catch (SQLException se) {
                String msg = "An error occurred while creating the containers.";
                logger.error(msg, se);
                new DatabaseUpgradeException(msg, se);
            }                
        }
    }
    
    
    /************************************************
     * Name conversions
     ************************************************/
    
    private void convertAssociateNames() throws DatabaseUpgradeException {

        Connection conn = DatabaseManager.getAdminConnection();
        Statement stmt = getSqlStatement(conn);
        
        // perform always
        DcAssociate base = new DcAssociate(DcModules._ACTOR);
        for (DcModule module : DcModules.getAllModules()) {
            if (module.getDcObject() instanceof DcAssociate) {

                try {
                    stmt.executeQuery("SELECT LASTNAME FROM " + module.getTableName());
                } catch (Exception e) {
                    // new columns have not yet been created.
                    try {
                        stmt.executeQuery("ALTER TABLE " + module.getTableName() + " ADD COLUMN " + 
                                          module.getField(DcAssociate._F_LASTTNAME).getDatabaseFieldName() + " " + 
                                          module.getField(DcAssociate._F_LASTTNAME).getDataBaseFieldType());
                    } catch (SQLException se) {
                        throw new DatabaseUpgradeException("Could not create the column LASTNAME for " + module.getLabel(), se);
                    }

                }
                
                try {
                    stmt.executeQuery("SELECT FIRSTNAME FROM " + module.getTableName());
                } catch (Exception e) {
                    // new columns have not yet been created.
                    try {
                        stmt.executeQuery("ALTER TABLE " + module.getTableName() + " ADD COLUMN " + 
                                          module.getField(DcAssociate._E_FIRSTNAME).getDatabaseFieldName() + " " + 
                                          module.getField(DcAssociate._E_FIRSTNAME).getDataBaseFieldType());
                    } catch (SQLException se) {
                        throw new DatabaseUpgradeException("Could not create the column FIRSTNAME for " + module.getLabel(), se);
                    }
                }
                
                try {
                    ResultSet rs = stmt.executeQuery("SELECT ID, NAME FROM " + module.getTableName() + 
                                                     " WHERE ((LASTNAME IS NULL OR LASTNAME = '') OR " +
                                                     "(FIRSTNAME IS NULL OR FIRSTNAME = '')) AND NAME IS NOT NULL");
                    
                    PreparedStatement ps = conn.prepareStatement("UPDATE " + module.getTableName() +  
                                                                " SET FIRSTNAME = ?, LASTNAME = ? WHERE ID = ?");
                    
                    while (rs.next()) {
                        try {
                            base.clearValues();
                            String id = rs.getString("ID");
                            String name = rs.getString("NAME");
                            base.setValue(DcAssociate._A_NAME, name);
                            base.setName();
                            
                            ps.setString(1, (String) base.getValue(DcAssociate._E_FIRSTNAME));
                            ps.setString(2, (String) base.getValue(DcAssociate._F_LASTTNAME));
                            ps.setString(3, id);
                            
                            ps.execute();
                        } catch (SQLException se) {
                            logger.error("Error while setting the first and lastname for " + module.getName(), se);
                        }
                    }
                    
                    rs.close();
                    ps.close();
                } catch (SQLException se) {
                    String msg = "An error occured while splitting the names into first and last name for " + module.getLabel() + ".";
                    logger.error(msg, se);
                    new DatabaseUpgradeException(msg, se);
                }
            }
        }
        
        closeDbConnection(conn, stmt);
    }
    
    private void convertAssociateNames2() throws DatabaseUpgradeException {

        Connection conn = DatabaseManager.getAdminConnection();
        Statement stmt = getSqlStatement(conn);
        
        if (DatabaseManager.getVersion().equals(new Version(3, 4, 0, 0)) ||  
            DatabaseManager.getVersion().equals(new Version(3, 4, 1, 0))) {

            QuestionBox qb = new QuestionBox("Upgrade for version < 3.4.2. Names of persons will be converted. " +
            		                         "Firstname is currently set as lastname and vice versa. Continue? It is safe to skip this upgrade!");
            if (!qb.isAffirmative())
            	return;
        } else {
            return;
        }
        
    	DcAssociate base = new DcAssociate(DcModules._ACTOR);
        for (DcModule module : DcModules.getAllModules()) {
            if (module.getDcObject() instanceof DcAssociate) {
                try {
                	ResultSet rs = stmt.executeQuery("SELECT ID, FIRSTNAME, LASTNAME  FROM " + module.getTableName() + 
                		                             " WHERE LASTNAME IS NOT NULL AND FIRSTNAME IS NOT NULL");

                    PreparedStatement ps = conn.prepareStatement("UPDATE " + module.getTableName() + " SET FIRSTNAME = ?, LASTNAME = ? WHERE ID = ?");
                	while (rs.next()) {
                        try {
                            base.clearValues();
                            String id = rs.getString("ID");
                            String lastname = rs.getString("LASTNAME");
                            String firstname = rs.getString("FIRSTNAME");
                            
                            ps.setString(1, lastname);
                            ps.setString(2, firstname);
                            ps.setString(3, id);
                            
                            ps.execute();
                        } catch (SQLException se) {
                            logger.error("Error while setting the first and lastname for " + module.getName(), se);
                        }
                    }
                    
                    rs.close();
                    ps.close();
                } catch (SQLException se) {
                    String msg = "An error occured while splitting the names into first and last name for " + module.getLabel() + ".";
                    logger.error(msg, se);
                    new DatabaseUpgradeException(msg, se);
                }
            }
        }
        
        closeDbConnection(conn, stmt);
    }
    
    
    /************************************************
     * Move images to the new location
     ************************************************/
    
    private void moveImages() throws DatabaseUpgradeException {
        
        if (new File(DataCrow.baseDir + "data/images").exists()) {
            QuestionBox qb = new QuestionBox("The image location has moved and scaled images " +
            		                         "have to be rewritten. It is not recommended to skip this " +
            		                         "conversion. Would you likt to continue with this conversion?");
            if (!qb.isAffirmative())
                return;
        } else {
            return;
        }
        
        DataCrow.showSplashScreen(false);
        
        LogForm.getInstance().setVisible(true);
        
        for (String file : Directory.read(DataCrow.baseDir + "data/images", false, false, new String[] {"jpg"})) {
             File base = new File(file);
             if (file.indexOf("_small") == -1) {
                 
                 logger.info("Moving " + file);
                 File f = new File(DataCrow.imageDir, base.getName());
                 
                 try {
                     Utilities.rename(base, f);
                 } catch (Exception e) {
                     QuestionBox qb = new QuestionBox("Could not move " + file + ". Do you want to continue and skip this file? " +
                                          "(You can also copy the images manually from data/images to webapp/datacrow/mediaimages)");
                     if (!qb.isAffirmative()) return;
                 }
                 
                 String scaledFilename = DataCrow.imageDir + new File(new Picture().getScaledFilename(file)).getName();
                 try {
                     Utilities.writeScaledImageToFile(new ImageIcon(f.toString()), scaledFilename);
                 } catch (Exception e) {
                     throw new DatabaseUpgradeException("Could not created scaled image " + scaledFilename + ".", e);
                 }
             }

             base.delete();
         }
         new File(DataCrow.baseDir + "data/images").delete();
    }
    
    
    /************************************************
     * Convert ratings
     ************************************************/
    
    private void convertRatings(Version v) throws DatabaseUpgradeException {
        // run always!
        Connection conn = DatabaseManager.getAdminConnection();
        Statement stmt = getSqlStatement(conn);

        for (DcModule module : DcModules.getAllModules()) {
            for (DcField field : module.getFields()) {
                
                if (    module.getTableName() != null && module.getTableName().trim().length() > 0 && 
                        field.getFieldType() == ComponentFactory._RATINGCOMBOBOX) {

                    String sql = "update " + module.getTableName() + " set " + field.getDatabaseFieldName() + 
                    " = " + field.getDatabaseFieldName() + " / 2 where " + field.getDatabaseFieldName() + " > 10";
                    
                    try {
                        stmt.execute(sql);
                    } catch (SQLException se) {
                        logger.error(se, se);
                    }
                }
            }
        }
        
        closeDbConnection(conn, stmt);
        
        Boolean b = getLegacySetting("ratings_converted");
        
        if (b != null && b.booleanValue())
            return;
        
        if (!v.isUndetermined())
        	return;
        
        DataCrow.showSplashScreen(false);
        
        QuestionBox qb = new QuestionBox("Upgrade for version < 3.1.30. The 5-based ratings will be converted to the " +
        		                         "new 10-based rating system. Do you want to perform this upgrade.");
        
        if (qb.isAffirmative()) {
            
            conn = DatabaseManager.getAdminConnection();
            stmt = getSqlStatement(conn);
            
            LogForm.getInstance().setVisible(true);
            
            for (DcModule module : DcModules.getAllModules()) {
                for (DcField field : module.getFields()) {
                    
                    if (    module.getTableName() != null && module.getTableName().trim().length() > 0 && 
                            field.getFieldType() == ComponentFactory._RATINGCOMBOBOX) {
                        
                        try {
                            String sql = "SELECT ID, " + field.getDatabaseFieldName() + " FROM " + module.getTableName() + 
                                         " WHERE " + field.getDatabaseFieldName() + " IS NOT NULL AND " + 
                                         field.getDatabaseFieldName() + " > 0";
                            
                            PreparedStatement ps = conn.prepareStatement("UPDATE " + module.getTableName() + " SET " + 
                                                                         field.getDatabaseFieldName() + " = ? " + " WHERE ID = ?");
                            ResultSet rs = stmt.executeQuery(sql);
                            while (rs.next()) {
                                int rating = rs.getInt(field.getDatabaseFieldName()) * 2;
                                long id = rs.getLong("ID"); 
                                ps.setInt(1, rating);
                                ps.setLong(2, id);
                                ps.execute();
                            }
                            
                            rs.close();
                        } catch (SQLException se) {
                            throw new DatabaseUpgradeException("Could not convert rating for module " + module.getLabel(), se);
                        }
                    }
                }
                
                logger.info("Converted ratings for " + module.getLabel());
            }
        }
    }
    
    
    /************************************************
     * Cross table fixes
     ************************************************/

    private void repairCrossTables(Version v) throws DatabaseUpgradeException {
        
        Boolean b = getLegacySetting("cross_tables_fixed");
        
        if (b != null && b.booleanValue())
            return;
        
        if (!v.isUndetermined())
        	return;

        DataCrow.showSplashScreen(false);
        QuestionBox qb = new QuestionBox("Upgrade for version < 3.1.25. Crosstables for Music Artists and Music Genres need to be fixed. " +
        		                         "Do you want to perform this upgrade?");
        
        if (qb.isAffirmative()) {
            LogForm.getInstance().setVisible(true);
            
            Collection<Table> tables = new ArrayList<Table>();
            tables.add(new Table("X_AUDIOCD_MUSICGENRE", "AUDIOCD"));
            tables.add(new Table("X_AUDIOTRACK_MUSICGENRE", "AUDIOTRACK"));
            tables.add(new Table("X_MUSICALBUM_MUSICGENRE", "MUSICALBUM"));
            tables.add(new Table("X_MUSICTRACK_MUSICGENRE", "MUSICTRACK"));
            
            for (Table table : tables)
                repairCrossTables(tables, table);
            
            tables.clear();
            tables.add(new Table("X_AUDIOCD_MUSICARTIST", "AUDIOCD"));
            tables.add(new Table("X_AUDIOTRACK_MUSICARTIST", "AUDIOTRACK"));
            tables.add(new Table("X_MUSICALBUM_MUSICARTIST", "MUSICALBUM"));
            tables.add(new Table("X_MUSICTRACK_MUSICARTIST", "MUSICTRACK"));

            for (Table table : tables)
                repairCrossTables(tables, table);
        }
    }
    
    private void repairCrossTables(Collection<Table> tables, Table table) throws DatabaseUpgradeException {
  
        Connection conn = DatabaseManager.getAdminConnection();
        Statement stmt = getSqlStatement(conn);
        
        for (Table t : tables) {
            
            if (t.equals(table)) continue;
            
            String sql = "select top 1 OBJECTID from " + table.getCrosstable() + " " +
                         "where OBJECTID not in (select ID from " + table.getTable() + ")";
    
            try {
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    
                    rs.close();
                    
                    try {
                        stmt.execute("CREATE TABLE " + t.getCrosstable() + " " + 
                                    "(OBJECTID bigint, REFERENCEDID bigint)");
                    } catch (Exception e) {
                        logger.debug(e, e);
                    }
                    
                    PreparedStatement ps = conn.prepareStatement("INSERT INTO " + t.getCrosstable()+ " (OBJECTID, REFERENCEDID) VALUES (?, ?)");
        
                    sql = "select OBJECTID, REFERENCEDID from " + table.getCrosstable() + " " + 
                          "where OBJECTID in (select ID from " + t.getTable() + ")";
                    
                    try {
                        rs = stmt.executeQuery(sql);
                        while (rs.next()) {
                            ps.setLong(1, rs.getLong("OBJECTID"));
                            ps.setLong(2, rs.getLong("REFERENCEDID"));
                            ps.execute();
                        }
                    } catch (SQLException se) {
                        logger.error(se, se);
                    }
                }
                rs.close();
            } catch (Exception e) {
                logger.error(e, e);
            }
        }

        try {
            stmt.execute("delete from " + table.getCrosstable() + " where OBJECTID not in " +
                        "(select ID from " + table.getTable() + ")");
        } catch (SQLException e) {
            logger.error(e, e);
        }   
        
        closeDbConnection(conn, stmt);
    }

    
    /************************************************
     * File size conversion
     ************************************************/

    private void convertFileSize() throws DatabaseUpgradeException {

        Connection conn = DatabaseManager.getAdminConnection();
        Statement stmt = getSqlStatement(conn);
        try {
            // If the file hash column exists the database does not need to be upgraded.
            stmt.execute("SELECT TOP 1 FILEHASH FROM SOFTWARE");
            closeDbConnection(conn, stmt);
            return;
        } catch (Exception e) {
            closeDbConnection(conn, stmt);
        }
        
        DataCrow.showSplashScreen(false);
        
        QuestionBox qb = new QuestionBox("Upgrade for version < 3.1.25: Filesizes need to be adjusted to the new format. " +
                                         "Do you want to perform this upgrade?");
        if (qb.isAffirmative()) {
            LogForm.getInstance().setVisible(true);
            
            convertFileSize("software");
            convertFileSize("movie");
            convertFileSize("musictrack");
            convertFileSize("book");
            convertFileSize("image");
        }
    }
    
    private void convertFileSize(String table) throws DatabaseUpgradeException { 
        
        logger.info("Starting conversion of filesizes for table " + table);

        Connection conn = DatabaseManager.getAdminConnection();
        Statement stmt = getSqlStatement(conn);

        try {
            String sql = "SELECT ID, FILESIZE FROM " + table + " WHERE FILESIZE IS NOT NULL AND FILESIZE != 0";
            ResultSet rs = stmt.executeQuery(sql);
            
            PreparedStatement ps = conn.prepareStatement("UPDATE SOFTWARE SET FILESIZE = ? WHERE ID = ?");
            while (rs.next()) {
                Long ID = rs.getLong("ID");
                Long filesize = rs.getLong("FILESIZE");
                filesize = (long) Math.round(filesize * 1024F);
                
                ps.setLong(1, filesize);
                ps.setLong(2, ID);
                
                ps.execute();
            }
            
            logger.info("Conversion of table " + table + " was successful");
            
        } catch (Exception e) {
            new DatabaseUpgradeException("Could not convert file size for " + table, e);
        }
        
        closeDbConnection(conn, stmt);
    }
    
    private static class Table {

        private String crosstable;
        private String table;
        
        public Table(String crosstable, String table) {
            super();
            this.crosstable = crosstable;
            this.table = table;
        }
        
        public String getCrosstable() {
            return crosstable;
        }
        
        public String getTable() {
            return table;
        }
    
        @Override
        public int hashCode() {
            return table.hashCode() + crosstable.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            return o instanceof Table && ((Table) o).getTable().equals(table) &&
                                         ((Table) o).getCrosstable().equals(crosstable);
        }
    }   
    
    
    /************************************************
     * Convert File Size Columns
     ************************************************/
    private void convertNumberColumns(Version v) throws DatabaseUpgradeException {

        if (v.isOlder(new Version(3, 4, 2, 0))) {
            Connection conn = DatabaseManager.getAdminConnection();
            Statement stmt = getSqlStatement(conn);
            
            for (DcModule module : DcModules.getAllModules()) {
                
                if (module.getTableName() != null && module.getTableName().trim().length() > 0) {
                    for (DcField field : module.getFields()) {
                        if (field.getValueType() == DcRepository.ValueTypes._LONG && !field.isUiOnly()) {
                            try {
                                String sql = "ALTER TABLE " + module.getTableName() + " ALTER COLUMN " + 
                                             field.getDatabaseFieldName() + " " +
                                             field.getDataBaseFieldType();
                                stmt.execute(sql);
                            } catch (Exception e) {
                                logger.error("Could not convert the column type for the filesize for module " + module.getTableName(), e);
                            }
                        }
                    }
                }
            }
        }
    }

    
    /************************************************
     * Helper methods
     ************************************************/
    
    private Boolean getLegacySetting(String key) {
        String filename = DataCrow.baseDir + "data" + File.separator + "data_crow.properties";
        try {
            Properties properties = new Properties();
            FileInputStream fis = new FileInputStream(filename);
            properties.load(fis);
            fis.close();
            
            String value = (String) properties.get(key);
            
            return value == null || value.trim().equals("") ? null : 
                   Boolean.valueOf(value);
            
        } catch (Exception ignore) {
            logger.debug("Could not load legacy settings from " + filename);
        } 
        
        return null;
    }
    
    private Statement getSqlStatement(Connection conn) throws DatabaseUpgradeException {
        try {
            return conn.createStatement();
        } catch (SQLException se) {
            throw new DatabaseUpgradeException("Could not connect to the database.", se);
        }
    }
    
    private void closeDbConnection(Connection conn, Statement stmt) throws DatabaseUpgradeException {
        try {
        	if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException se) {
            logger.error(se, se);
        }
    }
    
    private boolean isNewDatabase() throws DatabaseUpgradeException {
        Connection conn = DatabaseManager.getAdminConnection();
        Statement stmt = getSqlStatement(conn);
        
        boolean isNew = false;
        try {
            // Has the database been created?
            // For new databases the following statement will always fail while for
            // new databases this check will always be correct.
            stmt.executeQuery("SELECT TOP 1 * FROM SOFTWARE");
        } catch (Exception e) {
            isNew = true;
        }
        closeDbConnection(conn, stmt);
        return isNew;
    }
        
}
