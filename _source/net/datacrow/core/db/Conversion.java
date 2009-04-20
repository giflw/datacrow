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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcProperty;

import org.apache.log4j.Logger;

/**
 * Manages table conversions based on the module definition.
 * 
 * @author Robert Jan van der Waals
 */
public class Conversion {

    private static Logger logger = Logger.getLogger(Conversion.class.getName());
    
    private int moduleIdx;
    
    private String columnName;
    
    private int oldFieldType;
    private int newFieldType;
    private int referencingModuleIdx = -1;
    
    public Conversion(String s) {
        StringTokenizer st = new StringTokenizer(s, "/&/");
        List<String> c = new ArrayList<String>();
        while (st.hasMoreTokens())
            c.add((String) st.nextElement());
        
        int i = 0;
        setModuleIdx(Integer.parseInt(c.get(i++)));
        setColumnName(c.get(i++));
        setOldFieldType(Integer.parseInt(c.get(i++)));
        setNewFieldType(Integer.parseInt(c.get(i++)));
        setReferencingModuleIdx(Integer.parseInt(c.get(i++)));
    }
    
    public Conversion(int module) {
        this.moduleIdx = module;
    }
    
    @Override
    public String toString() {
        return  getModuleIdx() + "/&/" + getColumnName() + "/&/" + 
                getOldFieldType() + "/&/" + getNewFieldType() +  "/&/" +
                getReferencingModuleIdx();
    }

    /**
     * Checks whether the conversion is actually needed. This check is in place to make
     * sure older backups can still be restored. 
     */
    public boolean isNeeded() {
        boolean needed = false;
        
        String sql = "select top 1 * from " + DcModules.get(moduleIdx).getTableName();
        try {
            ResultSet result = DatabaseManager.executeSQL(sql, false);
            ResultSetMetaData meta = result.getMetaData();
            
            if (getNewFieldType() == ComponentFactory._REFERENCESFIELD) {
                boolean exists = false;
                for (int i = 1; i < meta.getColumnCount() + 1; i++)
                    exists |= meta.getColumnName(i).equalsIgnoreCase(columnName);
                
                // column should no longer be there after a successful conversion..
                // else the conversion still needs to (re-) occur.
                needed = exists;
            } else if (getNewFieldType() == ComponentFactory._REFERENCEFIELD) {
                // Check: check if there are items stored in the targeted module and if it exists.

                DcModule reference = DcModules.get(referencingModuleIdx);
                sql = "select top 1 " + columnName + " from " + reference.getTableName();
                
                try {
                    ResultSet rs = DatabaseManager.executeSQL(sql, false);
                    rs.close();
                    
                    // check the column type.. if not BIGINT a conversion is still needed.
                    needed = meta.getColumnType(1) != Types.BIGINT;
                } catch (Exception ignore) {
                    needed = true;
                }
            }
        } catch (Exception e) {
            logger.error(e, e);
        }
        
        return needed;
    }
    
    /**
     * Handles complex conversions. Simple conversions are executed directly on the database.
     * @see DatabaseManager#initialize()
     * @return
     */
    public boolean execute() {
        // Converting a reference field to a multi-reference field
        if (getOldFieldType() == ComponentFactory._REFERENCEFIELD &&
            getNewFieldType() == ComponentFactory._REFERENCESFIELD) {
            
            logger.info("Starting to convert reference field [" + columnName + "] to a multi references field");

            // load data from the database:
            DataManager.setUseCache(false);
            String sql = "SELECT ID, " + getColumnName() + " FROM " + DcModules.get(getModuleIdx()).getTableName() + " " +
                         "WHERE " + getColumnName() + " IS NOT NULL";
            try {
                ResultSet rs = DatabaseManager.executeSQL(sql, true);
                DcModule mappingMod = DcModules.get(DcModules.getMappingModIdx(moduleIdx, DcModules.get(getModuleIdx()).getField(columnName).getReferenceIdx()));
            
                DcObject mapping = mappingMod.getDcObject();
                while (rs.next()) {
                    String ID = rs.getString(1);
                    String referenceID = rs.getString(2);
                    
                    mapping.setValue(DcMapping._A_PARENT_ID, ID);
                    mapping.setValue(DcMapping._B_REFERENCED_ID, referenceID);
                    
                    DatabaseManager.executeQuery(new Query(Query._INSERT, mapping, null, null), true);
                }
            } catch (Exception e) {
                logger.error("Failed to create reference. Conversion has failed. Restart Data Crow to try again.", e);
                return false;
            }
         
        // Converting any kind of field to a reference field
        } else if (getNewFieldType() == ComponentFactory._REFERENCESFIELD ||
                   getNewFieldType() == ComponentFactory._REFERENCEFIELD) {
            
            logger.info("Starting to convert field [" + columnName + "] to a multi references field");
            
            String sql = "select distinct " + columnName + " from " + DcModules.get(getModuleIdx()).getTableName();
            
            try {
                ResultSet rs = DatabaseManager.executeSQL(sql, true);
                
                DcModule refMod = DcModules.get(referencingModuleIdx);
                
                while (rs.next()) {
                    String name = rs.getString(1);
                    DcObject reference = refMod.getDcObject();
                    reference.setValue(DcProperty._A_NAME, name);
                    DatabaseManager.executeQuery(new Query(Query._INSERT, reference, null, null), true);
                    
                    String sql2 = "select item.ID, property.ID from " + refMod.getTableName() + " property " +
                                   "inner join " + DcModules.get(getModuleIdx()).getTableName() + " item " +
                                   "on property." + refMod.getField(DcProperty._A_NAME).getDatabaseFieldName() + "=" +
                                   "item." + columnName;
                    ResultSet rs2 = DatabaseManager.executeSQL(sql2, true);
                    
                    while (rs2.next()) {
                        String itemID = rs2.getString(1);
                        String propertyID = rs2.getString(2);
                        
                        if (getNewFieldType() == ComponentFactory._REFERENCESFIELD) {
                            DcModule mappingMod = DcModules.get(DcModules.getMappingModIdx(moduleIdx, getReferencingModuleIdx()));
                            
                            DcObject mapping = mappingMod.getDcObject();
                            mapping.setValue(DcMapping._A_PARENT_ID, itemID);
                            mapping.setValue(DcMapping._B_REFERENCED_ID, propertyID);
                            
                            DatabaseManager.executeQuery(new Query(Query._INSERT, mapping, null, null), true);
                        } else {
                            String sql3 = "update " + DcModules.get(getModuleIdx()).getTableName() +
                                          " set " + columnName + "=" + propertyID;
                            DatabaseManager.executeSQL(sql3, true);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to create reference. Conversion has failed. Restart Data Crow to try again.", e);
                return false;
            }
        }
        
        try {
            
            if (getNewFieldType() == ComponentFactory._REFERENCEFIELD) {
                DatabaseManager.executeSQL(
                        "alter table " + DcModules.get(getModuleIdx()).getTableName() + 
                        " alter column " + columnName + " " + DcModules.get(getModuleIdx()).getField(columnName).getDataBaseFieldType(), true);
            } 
            
            // note that column removal is performed by the cleanup method of the database
            
        } catch (Exception e) {
            logger.error("Failed to clean up after doing the field type conversion.", e);
        }        
        
        return true;
    }
    
    public int getReferencingModuleIdx() {
        return referencingModuleIdx;
    }

    public void setReferencingModuleIdx(int referencingModuleIdx) {
        this.referencingModuleIdx = referencingModuleIdx;
    }

    public int getModuleIdx() {
        return moduleIdx;
    }

    public void setModuleIdx(int moduleIdx) {
        this.moduleIdx = moduleIdx;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getOldFieldType() {
        return oldFieldType;
    }

    public void setOldFieldType(int oldFieldType) {
        this.oldFieldType = oldFieldType;
    }

    public int getNewFieldType() {
        return newFieldType;
    }

    public void setNewFieldType(int newFieldType) {
        this.newFieldType = newFieldType;
    }
}
