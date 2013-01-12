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
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.QuickViewFieldDefinition;
import net.datacrow.settings.definitions.WebFieldDefinition;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.Directory;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * If possible, perform upgrades / changes in the DataManagerConversion class!
 * 
 * Converts the current database after the actual module tables are created / updated.
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
            
            if (v.isOlder(DataCrow.getVersion())) {
                Directory dir = new Directory(DataCrow.installationDir + "webapp", true, null);
                File file;
                int idx;
                File targetDir;
                File webDir = new File(DataCrow.userDir, "wwwroot/datacrow");
                for (String s : dir.read()) {
                    file = new File(s);
                    idx = s.indexOf("webapp/datacrow/") > -1 ? s.indexOf("webapp/datacrow/") : s.indexOf("webapp\\datacrow\\");
                    
                    if (idx == -1) continue;
                    
                    targetDir = (new File(webDir, s.substring(idx + "webapp/datacrow/".length())).getParentFile());
                    targetDir.mkdirs();
                    Utilities.copy(file, new File(targetDir, file.getName()));
                }
            }
            
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
                		"- The sort index for persons will be recalculated.");
                upgraded = cleanupReferences();
            }
            
//            if (v.isOlder(new Version(3, 9, 9, 0))) {
//                lf = new LogForm();
//                DcSwingUtilities.displayMessage(
//                        "- Pictures of previously deleted items will now be removed. This is a non crucial system task which can take a few minutes.");
//                upgraded = cleanupPictures();                
//            }
            
            if (v.isOlder(new Version(3, 9, 12, 0))) {
                DcModules.get(DcModules._BOOK).getSettings().set(DcRepository.ModuleSettings.stFileImportFileTypes, "txt,chm,doc,docx,pdf,prc,pdb,kml,html,htm,prc,lit,epub,odt");          
            }
            
            if (v.equals(new Version(3, 9, 9, 0)) || v.equals(new Version(3, 9, 8, 0))) {
                lf = new LogForm();
                DcSwingUtilities.displayMessage(
                        "The names of authors will be corrected. The format of the names can be changed afterwards using " +
                        "the Name Rewriter Tool located in the Tools menu.\n");
                upgraded = correctAssociateNames();
                upgraded = fillUIPersistFieldsPersons();
            }   
            
            if (v.isOlder(new Version(3, 9, 15, 0))) {
                // silent upgrade script
                cleanupOfPermission();
            }  
            
            settingsCorrections();
            
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
    
    private void cleanupOfPermission() {
        try {
            DatabaseManager.executeAsAdmin("DELETE FROM Permission WHERE field is null and plugin is null");
            DatabaseManager.executeAsAdmin("DELETE FROM Permission WHERE user is null");
        } catch (SQLException se) {
            logger.error("Error while cleaning up the permissions", se);
        }            
    }
    
    private void settingsCorrections() {
        for (DcModule module : DcModules.getAllModules()) {
            Collection<DcFieldDefinition> definitions = new ArrayList<DcFieldDefinition>();
            if (module.getFieldDefinitions() != null) {
                definitions.addAll(module.getFieldDefinitions().getDefinitions());
                for (DcFieldDefinition def : definitions) {
                    if (module.getField(def.getIndex()) == null)
                        module.getFieldDefinitions().getDefinitions().remove(def);
                }
            }

            int[] tableOrder = (int[]) module.getSetting(DcRepository.ModuleSettings.stTableColumnOrder);
            if (tableOrder != null) {
                int counter = 0;
                for (int field : tableOrder) {
                    if (module.getField(field) != null)
                       counter++;
                }
                
                int[] newOrder = new int[counter];
                counter = 0;
                for (int field : tableOrder) {
                    if (module.getField(field) != null)
                        newOrder[counter++] = field;
                }
                
                module.setSetting(DcRepository.ModuleSettings.stTableColumnOrder, newOrder);
            }
            
            int[] cardOrder = (int[]) module.getSetting(DcRepository.ModuleSettings.stCardViewPictureOrder);
            if (cardOrder != null) {
                int counter = 0;
                for (int field : cardOrder) {
                    if (module.getField(field) != null)
                       counter++;
                }
                
                int[] newOrder = new int[counter];
                counter = 0;
                for (int field : cardOrder) {
                    if (module.getField(field) != null)
                        newOrder[counter++] = field;
                }
                
                module.setSetting(DcRepository.ModuleSettings.stCardViewPictureOrder, newOrder);
            }
            
            Collection<QuickViewFieldDefinition> qvDefinitions = new ArrayList<QuickViewFieldDefinition>();
            if (module.getQuickViewFieldDefinitions() != null) {
                qvDefinitions.addAll(module.getQuickViewFieldDefinitions().getDefinitions());
                for (QuickViewFieldDefinition def : qvDefinitions) {
                    if (module.getField(def.getField()) == null)
                        module.getQuickViewFieldDefinitions().getDefinitions().remove(def);
                }
            }
            
            Collection<WebFieldDefinition> wfDefinitions = new ArrayList<WebFieldDefinition>();
            if (module.getWebFieldDefinitions() != null) {
                wfDefinitions.addAll(module.getWebFieldDefinitions().getDefinitions());
                for (WebFieldDefinition def : wfDefinitions) {
                    if (module.getField(def.getField()) == null)
                        module.getWebFieldDefinitions().getDefinitions().remove(def);
                }
            }
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

    private boolean correctAssociateNames() {

        boolean upgraded = false;
        
        String fieldLn;
        String fieldFn;
        String fieldN;
        String sql;
        
        String firstname;
        String lastname;
        String name;
        String id;
        
        Connection conn = DatabaseManager.getAdminConnection();
        PreparedStatement ps = null;
        String tmp;
        for (DcModule module : DcModules.getAllModules()) {
            if (module.getType() == DcModule._TYPE_ASSOCIATE_MODULE) {
                try {
                    fieldFn = module.getField(DcAssociate._E_FIRSTNAME).getDatabaseFieldName();
                    fieldLn = module.getField(DcAssociate._F_LASTTNAME).getDatabaseFieldName();
                    fieldN  = module.getField(DcAssociate._A_NAME).getDatabaseFieldName();
                    
                    sql = "select " + fieldFn + ", " + fieldLn + ", ID from " + module.getTableName() + 
                          " where firstname is not null and lastname is not null"; 
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
                        
                        if (firstname.length() == 0 && lastname.length() == 0) {
                            continue;
                        }
                        
                        name = (firstname + " " + lastname).trim();
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
