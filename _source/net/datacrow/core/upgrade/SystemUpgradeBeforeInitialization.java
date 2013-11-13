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

package net.datacrow.core.upgrade;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.Version;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.helpers.ExternalReference;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.Directory;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * Upgrade steps for the various versions.
 * 
 * Converts the current database before the actual module tables are created / updated.
 * This means that the code here defies workflow logic and is strictly to be used for
 * table conversions and migration out of the scope of the normal module upgrade code.
 * 
 * The automatic database correction script runs after this manual upgrade.
 * 
 * @author Robert Jan van der Waals
 */
public class SystemUpgradeBeforeInitialization {
    
    private static Logger logger = Logger.getLogger(SystemUpgradeBeforeInitialization.class.getName());
    
    public void start() {
        try {
            boolean upgraded = false;
            Version v = DatabaseManager.getVersion();
            
            if (v.isOlder(DataCrow.getVersion())) {
                checkSystemFiles();
            }
            
            if (v.isOlder(new Version(3, 9, 22, 0))) {
                DataCrow.showSplashScreen(false);
            	cleanupReferences();
            	DcSettings.set(DcRepository.Settings.stGarbageCollectionIntervalMs, Long.valueOf(0));
            	upgraded |= createIndexes();
            }
            
            if (v.isOlder(new Version(3, 9, 11, 0))) {
                DataCrow.showSplashScreen(false);
                removeCompanyFields();
            }
            
            if (upgraded) {
                DataCrow.showSplashScreen(false);
                DcSwingUtilities.displayMessage("The upgrade was successful. Data Crow will now continue.");
            }
            
            DataCrow.showSplashScreen(true);
            
        } catch (Exception e) {
            String msg = e.toString() + ". Data conversion failed. " +
                "Please restore your latest Backup and retry. Contact the developer " +
                "if the error persists";
            
            DcSwingUtilities.displayErrorMessage(msg);
            logger.error(msg, e);
        }            
    }
    
    /** 
     * Check whether the installed version has newer versions of the resources files and other
     * system related files. If yes, the user will be asked whether the user files can be overwritten.
     */
    private void checkSystemFiles() throws Exception {
        checkResources();
        checkReports();
    }
    
    private void checkResources() throws Exception {
        Directory dir = new Directory(new File(DataCrow.installationDir, "resources").toString(), false, null);
        Map<File, File> files = new HashMap<File, File>();
        
        File src;
        File tgt;
        for (String s : dir.read()) {
            src = new File(s);
            tgt = new File(DataCrow.resourcesDir, src.getName());
            
            if (!Utilities.isSameFile(src, tgt))
                files.put(src, tgt);
        }
        
        if (files.size() > 0) {
            DataCrow.showSplashScreen(false);
            
            boolean copy = DcSwingUtilities.displayQuestion(
                    "The installation folder has new language files available. Do you want to install these to your user folder?");
            if (copy) {
                for (File source : files.keySet()) {
                    Utilities.copy(source, files.get(source), true);
                }
            }
            
            DataCrow.showSplashScreen(true);
        }
    }
    
    private void checkReports() throws Exception {
        Directory dir = new Directory(new File(DataCrow.installationDir, "reports").toString(), true, new String[] {"xsl", "xslt"});
        Map<File, File> files = new HashMap<File, File>();
        
        File src;
        File tgt;
        int idx;
        for (String s : dir.read()) {
            src = new File(s);
            
            if (src.isDirectory()) continue;
            
            if (src.getParent().endsWith("reports")) {
                tgt = new File(DataCrow.reportDir, src.getName());
            } else {
                idx = s.indexOf("/reports/") > -1 ? s.indexOf("/reports/") : s.indexOf("\\reports\\");
                tgt = new File(new File(DataCrow.reportDir, s.substring(idx + 9)).getParentFile(), src.getName());
            }
            
            if (!Utilities.isSameFile(src, tgt))
                files.put(src, tgt);
        }
        
        if (files.size() > 0) {
            DataCrow.showSplashScreen(false);
            
            boolean copy = DcSwingUtilities.displayQuestion(
                    "The installation folder has new report files available. Do you want to install these to your user folder?");
            if (copy) {
                File targetDir;
                for (File source : files.keySet()) {
                    targetDir = files.get(source).getParentFile();
                    targetDir.mkdirs();
                    Utilities.copy(source, files.get(source), true);
                }
            }
            
            DataCrow.showSplashScreen(true);
        }    
    }     

    private void removeCompanyFields() {
        String sql;
        for (DcModule module : DcModules.getAllModules()) {
            
            if (module.getType() == DcModule._TYPE_ASSOCIATE_MODULE) {
                try {
                   sql = "ALTER TABLE " + module.getTableName() + " DROP COLUMN COMPANY";
                   DatabaseManager.execute(sql);
                } catch (SQLException e) {
                    logger.debug(e, e);
                }
            }
        }
    }
    
    @SuppressWarnings("resource")
    private boolean isExistingConstraint(Statement stmt, String contraint) {
        boolean exists = false;
        ResultSet rs = null;
        
        try {
            rs  = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.SYSTEM_INDEXINFO WHERE upper(index_name) = '" + contraint.toUpperCase() + "'");
            
            while (rs.next()) {
                exists = true;
            }
        } catch (Exception e) {
            logger.error(e, e);
        }
        
        try { 
            if (rs != null) rs.close(); 
        } catch (Exception e) {
            logger.debug("Could not release database resources", e);
        }
        
        return exists;
    }
    
    @SuppressWarnings("resource")
    private boolean createIndexes() throws Exception {
        Connection conn = DatabaseManager.getAdminConnection();
        Statement stmt = null;

        try {
            stmt = conn.createStatement();
        } catch (SQLException se) {
            logger.error(se, se);
        }

        try {
            for (DcModule module : DcModules.getAllModules()) {
                if (module.getTableName() == null)
                    continue;
                
                // test if table exists
                try {
                    stmt.execute("select TOP 1 * from " + module.getTableName());
                } catch (Exception e) {
                    // no privileges and/or the table does not yet exist
                    logger.info("Skipping constraint creation for table " + module.getTableName() + " as it does not yet exist");
                    continue;
                }
                
                // check if the constraint already exists
                if (isExistingConstraint(stmt, module.getTableName() + "_IDX")) 
                    continue;
                
                if (module.getIndex() == DcModules._PICTURE) {
                    try { 
                        
                        stmt.execute("delete from picture where filename is null or objectid is null or field is null");
                        
                    	String sql = "select distinct filename from picture where filename in (select filename from picture group by filename having count(ObjectID) > 1)";
                    	ResultSet rs = stmt.executeQuery(sql);
                    	ResultSet rs2;
                    	
                    	String filename;
                    	String objectID;
                    	Long width;
                    	Long height;
                    	String field;
                    	String externalFilename;
                    	
                    	while (rs.next()) {
                    	    
                    	    sql = "select distinct filename, objectid, width, field, height, external_filename from Picture where filename = '" + rs.getString("filename") + "'";
                    	    rs2 = stmt.executeQuery(sql);
                    	    while (rs2.next()) {
                    	        
                    	        sql = "delete from Picture where filename = '" + rs2.getString("filename") + "'";
                    	        
                    	        filename = rs2.getString("filename");
                                objectID = rs2.getString("objectid");
                                width = rs2.getLong("width");
                                height = rs2.getLong("height");
                                field = rs2.getString("field");
                                externalFilename = rs2.getString("external_filename");
                                externalFilename = Utilities.isEmpty(externalFilename) ? "null" : "'" + externalFilename + "'";
                                
                                logger.info("found duplicate records in the picture table - removing duplicate: " + filename);
                                
                                stmt.execute(sql);
                                sql = "insert into Picture (filename, objectid, width, field, height, external_filename) " +
                                        "values ('" + filename + "','" + objectID + "'," + width + ",'," + field + "'," + height + "," + externalFilename + ")";
                                
                                stmt.execute(sql);
                                
                                break;
                    	    }
    
                    	    rs2.close();
                    	}
                    	
                    	rs.close();
                    	
                    	logger.info("Creating unique index on " + module.getTableName());
                    	
                        stmt.execute("CREATE unique index " + module.getTableName() + "_IDX ON " + module.getTableName() + " (" +
                                module.getField(Picture._A_OBJECTID).getDatabaseFieldName() + ", " +
                                module.getField(Picture._B_FIELD).getDatabaseFieldName() + ")");
                    
                    } catch (Exception se) {
                        // throw new Exception("Unable to create unique index on " + module.getTableName(), se);
                        // there is no solution for this... yet.
                        logger.debug(se, se);
                    }
                } else if (module.getType() == DcModule._TYPE_MAPPING_MODULE) {
                    try { 
                    	
                        stmt.execute("delete from " +  module.getTableName() + " where objectid is null or referencedid is null");
                        
                        String sql = "select objectid, objectid + referencedid from " + module.getTableName() + " group by objectid, objectid + referencedid having count(*) > 1";
                    	ResultSet rs = stmt.executeQuery(sql);
                    	
                    	ResultSet rs2;
                        String objectID;
                        String referencedID;
                    	while (rs.next()) {
                    	    
                            sql = "select distinct objectID, referencedID from " + module.getTableName() + " where objectID = '" + rs.getString("objectID") + "'";
                            rs2 = stmt.executeQuery(sql);
    
                            while (rs2.next()) {
                                
                                referencedID = rs2.getString("referencedid");
                                objectID = rs2.getString("objectid");
                                
                                logger.info("Cleaning duplicates for " + module.getTableName() + ": " + objectID + ", reference ID: " + referencedID);
                                
                                sql = "delete from " + module.getTableName() + " where ObjectID = '" + objectID + "' AND referencedID = '" + referencedID + "'";
                                stmt.execute(sql);
                                
                                sql = "insert into " + module.getTableName() + " (objectid, referencedid) values ('" + objectID + "','" + referencedID + "')";
                                stmt.execute(sql);
                            }
    
                            rs2.close();
                    	}
                    	
                    	rs.close();
                    	
                    	logger.info("Creating unique index on " + module.getTableName());
                    	stmt.execute("CREATE unique index " + module.getTableName() + "_IDX ON " + module.getTableName() + " (" +
                                module.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() + ", " +
                                module.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + ")");
                    } catch (Exception se) {
                        throw new Exception("Unable to create unique index on " + module.getTableName(), se);
                    }
                } else if (module.getType() == DcModule._TYPE_EXTERNALREFERENCE_MODULE) {
                    try { 
                        
                        stmt.execute("delete from " +  module.getTableName() + " where externalid is null or externalidtype is null");
                        
                        String sql = "select externalid, externalidtype from " + module.getTableName() + " group by externalid, externalidtype having count(*) > 1";
                        ResultSet rs = stmt.executeQuery(sql);
                        
                        ResultSet rs2;
                        String externalidtype;
                        String externalid;
                        String name;
                        String ID;
                        while (rs.next()) {
                            
                            sql = "select top 1 id, externalid, externalidtype, name from " + module.getTableName() + 
                                    " where externalid = '" + rs.getString("externalid") + "' and externalidtype = '" + rs.getString("externalidtype") + "'";
                            rs2 = stmt.executeQuery(sql);
    
                            while (rs2.next()) {
                                
                                externalid = rs2.getString("externalid");
                                externalidtype = rs2.getString("externalidtype");
                                name = rs2.getString("name");
                                ID = rs2.getString("id");
                                
                                logger.info("Cleaning duplicates for " + module.getTableName() + ": " + externalid + ", reference type: " + externalidtype);
                                
                                sql = "delete from " + module.getTableName() + " where externalid = '" + externalid + "' AND externalidtype = '" + externalidtype + "'";
                                stmt.execute(sql);
                                
                                sql = "insert into " + module.getTableName() + " (id, externalid, externalidtype, name) values ('" + ID + "','" + externalid + "','" + externalidtype + "','" + name + "')";
                                stmt.execute(sql);
                            }
    
                            rs2.close();
                        }
                        
                        rs.close();
                        
                        logger.info("Creating unique index on " + module.getTableName());
                        stmt.execute("CREATE UNIQUE INDEX " + module.getTableName() + "_IDX ON " + module.getTableName() + " (" +
                                module.getField(ExternalReference._EXTERNAL_ID).getDatabaseFieldName() + ", " +
                                module.getField(ExternalReference._EXTERNAL_ID_TYPE).getDatabaseFieldName() + ")");
                    } catch (Exception se) {
                        throw new Exception("Unable to create unique index on " + module.getTableName(), se);
                    }
                }
            }
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se) {
                logger.debug("Failed to close database reources", se);
            }
        }
        
        return true;
    }
    
    @SuppressWarnings("resource")
    private void cleanupReferences() {
        Connection conn = DatabaseManager.getAdminConnection();
        Statement stmt = null;
        String sql;
        for (DcModule module : DcModules.getAllModules()) {
            
            if (module.getType() == DcModule._TYPE_MAPPING_MODULE) {
                
                // test if table exists
                try {
                	stmt = conn.createStatement();
                	
                    try {
                        stmt.execute("select TOP 1 * from " + module.getTableName());
                    } catch (Exception e) {
                        // no privileges and/or the table does not yet exist
                        logger.info("Skipping constraint creation for table " + module.getTableName() + " as it does not yet exist");
                        continue;
                    }
                	
                	sql = "DELETE FROM " + module.getTableName() + 
                            " WHERE " + module.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() +
                            " NOT IN (SELECT ID FROM " + 
                            DcModules.get(module.getField(DcMapping._A_PARENT_ID).getSourceModuleIdx()).getTableName() + ") " +
                            " OR " + module.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + 
                            " NOT IN (SELECT ID FROM " + 
                            DcModules.get(module.getField(DcMapping._B_REFERENCED_ID).getSourceModuleIdx()).getTableName() + ")";
                    stmt.execute(sql); 
                
                } catch (Exception se) {
                    logger.error("Could not remove references", se);
                }
            }
        }
        
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException se) {
            logger.debug("Failed to close database resources", se);
        }
   }
}
