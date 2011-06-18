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

package net.datacrow.core.db.upgrade;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.console.windows.log.LogForm;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.Version;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.MappingModule;
import net.datacrow.core.objects.DcAssociate;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcProperty;
import net.datacrow.util.DcSwingUtilities;

import org.apache.log4j.Logger;

/**
 * If possible, perform upgrades / changes in the DataManagerConversion class!
 * 
 * Converts the current database before the actual module tables are created / updated.
 * This means that the code here defies work flow logic and is strictly to be used for
 * table conversions and migration out of the scope of the normal module upgrade code.
 * 
 * The automatic database correction script runs after this manual upgrade.
 * 
 * @author Robert Jan van der Waals
 */
public class DatabaseUpgradeAfterInitialization {
    
private static Logger logger = Logger.getLogger(DatabaseUpgradeAfterInitialization.class.getName());
    
    public void start() {
        try {
            boolean upgraded = false;
            Version v = DatabaseManager.getVersion();
            LogForm lf = null;
            if (v.isOlder(new Version(3, 9, 2, 0))) {
                lf = new LogForm();
                DcSwingUtilities.displayMessage(
                        "Data Crow will perform a non critical upgrade. This process will take a couple of minutes.");
            	upgraded = fillUIPersistFields();
            }

            if (v.isOlder(new Version(3, 9, 6, 0))) {
                lf = new LogForm();
                DcSwingUtilities.displayMessage(
                        "Data Crow will perform a non critical upgrade to clear unwanted characters from languages, countries and other items.");
                upgraded = cleanupNames();
            }
            
            if (v.isOlder(new Version(3, 9, 8, 0))) {
                lf = new LogForm();
                DcSwingUtilities.displayMessage(
                		"- Ghost references will be removed. \n " +
                		"- The names of all persons (actors, authors, etc) will be formatted to read \"Lastname, Firstname\".\n" +
                		"- The sort index for persons will be recalculated.");
                upgraded = cleanupReferences();
                upgraded = reverseNames();
                upgraded = fillUIPersistFieldsPersons();
            }
            
            if (v.isOlder(new Version(3, 9, 9, 0))) {
                lf = new LogForm();
                DcSwingUtilities.displayMessage(
                        "- Names of authors will be corrected. \n" +
                        "- Pictures of previously deleted items will now be removed. This is a non crucial system task which can take a few minutes.");
                upgraded = cleanupPictures();
                upgraded = reverseAuthorNames();
            }
            
            if (upgraded) {
                lf.close();
                DcSwingUtilities.displayMessage(
                        "The upgrade was successful. Data Crow will now continue.");
                DataCrow.showSplashScreen(true);
            }
            
        } catch (Exception e) {
            String msg = e.toString() + ". Data conversion failed. " +
                "Please restore your latest Backup and retry. Contact the developer " +
                "if the error persists";
            
            DcSwingUtilities.displayErrorMessage(msg);
            logger.error(msg, e);
        }            
    }
    
    private boolean cleanupPictures() {
        
        String sql = "SELECT DISTINCT OBJECTID FROM PICTURE";
        boolean success = false;
        try {
            ResultSet rs = DatabaseManager.executeSQL(sql);
            Collection<String> ids = new ArrayList<String>();
            
            while (rs.next()) {
                ids.add(rs.getString(1).toLowerCase());
            }
            
            rs.close();
            
            String id;
            File f;
            for (String file : new File(DataCrow.imageDir).list()) {
                
                if (file.indexOf("_") == -1) {
                    logger.info("Skipped the following file " + file);
                    continue;
                }
                    
                id = file.substring(0, file.indexOf("_")).toLowerCase();
                if (!ids.contains(id)) {
                    f = new File(DataCrow.imageDir, file);
                    f.delete();
                    logger.info("Removed file " + f);
                }
            }
            
            success = true;
        } catch (SQLException se) {
            logger.error("Error while cleaning pictures. Not crucial; no loss of data!", se);
        }            
        
        return success;
    }
    
    private boolean cleanupReferences() {
    	MappingModule mm;
    	DcModule pm;
    	DcModule cm;
    	String sql;
    	ResultSet rs;
    	int count;
    	for (DcModule module : DcModules.getAllModules()) {
    		
    		if (module.getType() == DcModule._TYPE_MAPPING_MODULE) {
    			try {
	    			mm = (MappingModule) module;
	    			pm = DcModules.get(mm.getParentModIdx());
	    			cm = DcModules.get(mm.getReferencedModIdx());
	    			
	    			sql = "select count(*) as INVALIDENTRIES from " + mm.getTableName() + " where objectid not in " +
    				"(select id from " + pm.getTableName() + ") or referencedid not in " +
    				"(select id from " + cm.getTableName() + ")";
	    			
	    			rs = DatabaseManager.executeSQL(sql);
	    			rs.next();
	    			count = rs.getInt(1);
	    			rs.close();
	    			
	    			if (count > 0) {
		    			logger.info("Cleaning " + mm.getTableName() + " of " + count + " ghost record(s).");
		    			
		    			sql = "delete from " + mm.getTableName() + " where objectid not in " +
		    				"(select id from " + pm.getTableName() + ") or referencedid not in " +
		    				"(select id from " + cm.getTableName() + ")";
	    			
		    			rs = DatabaseManager.executeSQL(sql);
		    			
		    			if (rs != null) rs.close();
	    			}
    			} catch (SQLException se) {
    				logger.error("Error while cleaning references for module " + module, se);
    			}
    			
    		}
    	}
    	return true;
    }

    private boolean reverseAuthorNames() {

        boolean upgraded = false;

        String sql;
        String firstname;
        String lastname;
        String name;
        String id;
        
        Connection conn = DatabaseManager.getAdminConnection();
        PreparedStatement ps = null;
        
        DcModule module = DcModules.get(DcModules._AUTHOR);

        String fieldFn = module.getField(DcAssociate._E_FIRSTNAME).getDatabaseFieldName();
        String fieldLn = module.getField(DcAssociate._F_LASTTNAME).getDatabaseFieldName();
        String fieldN  = module.getField(DcAssociate._A_NAME).getDatabaseFieldName();
        String fieldC  = module.getField(DcAssociate._G_IS_COMPANY).getDatabaseFieldName();

        try {
            sql = "update " + module.getTableName() + " set " + fieldC + " = false where " + fieldC + " = true";
            DatabaseManager.executeSQL(sql);
        } catch (SQLException se) {
            logger.error("Could not mark " + module.getObjectNamePlural() + " as non-companies for module " + module, se);
        }          
        try {
            sql = "select " + fieldFn + ", " + fieldLn + ", ID from " + module.getTableName() + " where " + fieldC + " is null or " + fieldC + " = false"; 
            ResultSet rs = DatabaseManager.executeSQL(sql);
            while (rs.next()) {
                firstname = rs.getString(1);
                lastname = rs.getString(2);
                id = rs.getString(3);
                
                firstname = firstname == null ? "" : firstname.trim();
                lastname = lastname == null ? "" : lastname.trim();
                if (lastname.startsWith("(") && firstname.indexOf(" ") > -1) {
                    String tmp = lastname;
                    lastname = firstname.substring(firstname.indexOf(" ") + 1);
                    firstname = firstname.substring(0, firstname.indexOf(" ")) + " " + tmp;
                }
                
                name = firstname.length() > 0 && lastname.length() > 0 ? lastname + ", " + firstname :
                       firstname.length() == 0 ? lastname : firstname;
                
                sql = "update " + module.getTableName() + " set " + 
                       fieldN + " = ? , " + 
                       fieldFn + " = ?, " +
                       fieldLn + " = ? " +
                       "where ID = ?";
                
                ps = conn.prepareStatement(sql);
                
                ps.setString(1, name);
                ps.setString(2, firstname);
                ps.setString(3, lastname);
                ps.setString(4, id);
                
                ps.execute();
                ps.close();
            }
            
            rs.close();
            upgraded = true;
        } catch (SQLException se) {
            logger.error("Could not update " + module, se);
        }

        return upgraded;
    }
    
    private boolean reverseNames() {

        boolean upgraded = false;
        
        String fieldLn;
        String fieldFn;
        String fieldN;
        String fieldC;
        String sql;
        
        String firstname;
        String lastname;
        String name;
        String id;
        
        Connection conn = DatabaseManager.getAdminConnection();
        PreparedStatement ps = null;
        
        Collection<DcModule> companyModules = new ArrayList<DcModule>();
        companyModules.add(DcModules.get(DcModules._SOFTWAREPUBLISHER));
        companyModules.add(DcModules.get(DcModules._DEVELOPER));
        companyModules.add(DcModules.get(DcModules._BOOKPUBLISHER));
        companyModules.add(DcModules.get(DcModules._AUTHOR));
        
        for (DcModule module : DcModules.getAllModules()) {
            if (module.getType() == DcModule._TYPE_ASSOCIATE_MODULE) {
                try {
                    sql = "update " + module.getTableName() + " set " + module.getField(DcAssociate._G_IS_COMPANY).getDatabaseFieldName() + " = false";
                    DatabaseManager.executeSQL(sql);
                } catch (SQLException se) {
                    logger.error("Could not mark " + module.getObjectNamePlural() + " as non-companies for module " + module, se);
                }  
            }
        }
        
        for (DcModule module : companyModules) {
            try {
                sql = "update " + module.getTableName() + " set " + module.getField(DcAssociate._G_IS_COMPANY).getDatabaseFieldName() + " = true";
                DatabaseManager.executeSQL(sql);
            } catch (SQLException se) {
                logger.error("Could not mark " + module.getObjectNamePlural() + " as companies for module " + module, se);
            }
        }
        
        String tmp;
        for (DcModule module : DcModules.getAllModules()) {
            if (module.getType() == DcModule._TYPE_ASSOCIATE_MODULE) {
                try {
                    fieldFn = module.getField(DcAssociate._E_FIRSTNAME).getDatabaseFieldName();
                    fieldLn = module.getField(DcAssociate._F_LASTTNAME).getDatabaseFieldName();
                    fieldN  = module.getField(DcAssociate._A_NAME).getDatabaseFieldName();
                    fieldC  = module.getField(DcAssociate._G_IS_COMPANY).getDatabaseFieldName();
                    
                    sql = "select " + fieldFn + ", " + fieldLn + ", ID from " + module.getTableName() + " where " + fieldC + " is null or " + fieldC + " = false"; 
                    ResultSet rs = DatabaseManager.executeSQL(sql);
                    while (rs.next()) {
                        firstname = rs.getString(1);
                        lastname = rs.getString(2);
                        id = rs.getString(3);
                        
                        firstname = firstname == null ? "" : firstname.trim();
                        lastname = lastname == null ? "" : lastname.trim();
                        if (lastname.startsWith("(") && firstname.indexOf(" ") > -1) {
                            tmp = lastname;
                            lastname = firstname.substring(firstname.indexOf(" ") + 1);
                            firstname = firstname.substring(0, firstname.indexOf(" ")) + " " + tmp;
                        }
                        
                        name = firstname.length() > 0 && lastname.length() > 0 ? lastname + ", " + firstname :
                               firstname.length() == 0 ? lastname : firstname;
                        
                        sql = "update " + module.getTableName() + " set " + 
                               fieldN + " = ? , " + 
                               fieldFn + " = ?, " +
                               fieldLn + " = ? " +
                               "where ID = ?";
                        
                        ps = conn.prepareStatement(sql);
                        
                        ps.setString(1, name);
                        ps.setString(2, firstname);
                        ps.setString(3, lastname);
                        ps.setString(4, id);
                        
                        ps.execute();
                        ps.close();
                    }
                    
                    rs.close();
                    upgraded = true;
                } catch (SQLException se) {
                    logger.error("Could not update " + module, se);
                }
            }
        }
        return upgraded;
    }
    
    private boolean cleanupNames() {
        
        boolean upgraded = false;
        String field;
        String sql;
        
        for (DcModule module : DcModules.getAllModules()) {
            if (module.getType() == DcModule._TYPE_PROPERTY_MODULE) {
                field = module.getField(DcProperty._A_NAME).getDatabaseFieldName();
                sql = "UPDATE " + module.getTableName() + " SET " + field + " = LTRIM(RTRIM(" + field + "))";
                
                try {
                    DatabaseManager.executeSQL(sql);
                    upgraded = true;
                } catch (SQLException se) {
                    logger.error("Could not update " + module, se);
                }
            }
        }
        
        return upgraded;
    }
    
    private boolean fillUIPersistFieldsPersons() {
        boolean upgraded = false;
        
        ResultSet rs;
        DcField fldPersist;
        
        String ID;
        String referencedID;
        String sql;

        for (DcModule module : DcModules.getAllModules()) {

            if (module.isAbstract() || module.getType() == DcModule._TYPE_TEMPLATE_MODULE) continue;
            
            DcObject dco = module.getItem();
            for (DcField fld : module.getFields()) {
            	logger.info("Creating persistant field for module: " + module.getTableName() + ": " + fld);
                if (fld.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                	
                	if (DcModules.get(fld.getReferenceIdx()).getType() != DcModule._TYPE_ASSOCIATE_MODULE)
                		continue;
                	
                	fldPersist = module.getPersistentField(fld.getIndex());
                	
                    try {
                        DcModule mm = DcModules.get(DcModules.getMappingModIdx(fld.getModule(), fld.getReferenceIdx(), fld.getIndex()));

                        sql = 
                        	" select objectid, referencedid from " + mm.getTableName() + 
                        	" inner join " + DcModules.get(fld.getReferenceIdx()).getTableName() + 
                        	" on " + DcModules.get(fld.getReferenceIdx()).getTableName() + ".ID = " +  mm.getTableName() + ".referencedID " +
                        	" order by objectid, name";
                        
                        rs = DatabaseManager.executeSQL(sql);
                        
                        String prevID = null;
                        while (rs.next()) {
                            ID = rs.getString(1);
                            referencedID = rs.getString(2);
                            if (!ID.equals(prevID)) {
                            	sql = "update " + dco.getModule().getTableName() + " set " + 
                            		fldPersist.getDatabaseFieldName() + " = '" + referencedID + "' " +
                            		"where ID ='" + ID + "'";
                            	DatabaseManager.executeSQL(sql);
                            }
                            
                            prevID = ID;
                        }
                        rs.close();
                    } catch (SQLException se) {
                        logger.error("Could not remove references", se);
                    }
                    
                    upgraded = true;
                }
            }
        }
        
        return upgraded;
    }
    
    private boolean fillUIPersistFields() {
        boolean upgraded = false;
        
        ResultSet rs;
        ResultSet rs2;
        
        DcField fldPersist;
        String referenceID;
        
        String ID;
        String sql;

        for (DcModule module : DcModules.getAllModules()) {

            if (module.isAbstract() || module.getType() == DcModule._TYPE_TEMPLATE_MODULE) continue;
            
            DcObject dco = module.getItem();
            for (DcField fld : module.getFields()) {
                logger.info("Creating persistant field for module: " + module.getTableName() + ": " + fld);
                if (fld.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    try {
                        DcModule mm = DcModules.get(DcModules.getMappingModIdx(fld.getModule(), fld.getReferenceIdx(), fld.getIndex()));

                        sql = "select distinct objectid from " + mm.getTableName();
                        rs = DatabaseManager.executeSQL(sql);
                        
                        while (rs.next()) {
                            ID = rs.getString(1);
                            sql = "select top 1 referencedid from " + mm.getTableName() + " where objectid = '" + ID + "'";
                            rs2 = DatabaseManager.executeSQL(sql);
                            if (rs2.next()) {
                                dco.clearValues();
                                
                                referenceID = rs2.getString(1);
                                dco.setValueLowLevel(DcObject._ID, ID);
                                fldPersist = module.getPersistentField(fld.getIndex());
                                dco.setValue(fldPersist.getIndex(), referenceID);
                                dco.setUpdateGUI(false);
                                try {
                                    dco.saveUpdate(false, false);
                                } catch (Exception e) {
                                    logger.error(e, e);
                                }
                            }
                            rs2.close();
                        }
                        rs.close();
                    } catch (SQLException se) {
                        logger.error("Could not remove references", se);
                    }
                    
                    upgraded = true;
                }
            }
        }
        
        return upgraded;
    }
}
