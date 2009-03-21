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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;

import org.apache.log4j.Logger;

/**
 * Manages table conversions when
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

    public void execute() {
        if (getOldFieldType() == ComponentFactory._REFERENCEFIELD &&
            getNewFieldType() == ComponentFactory._REFERENCESFIELD) {
            
            logger.info("Starting to convert reference field [" + columnName + "] to a multi references field");

            // load data from the database!
            DataManager.setUseCache(false);
            String sql = "SELECT ID, " + getColumnName() + " FROM " + DcModules.get(getModuleIdx()).getTableName() + " " +
                         "WHERE " + getColumnName() + " IS NOT NULL";
            try {
                ResultSet rs = DatabaseManager.executeSQL(sql, true);
                DcModule mappingMod = DcModules.get(DcModules.getMappingModIdx(moduleIdx, getReferencingModuleIdx()));
            
                DcObject mapping = mappingMod.getDcObject();
                while (rs.next()) {
                    String ID = rs.getString(1);
                    String referenceID = rs.getString(2);
                    
                    mapping.setValue(DcMapping._A_PARENT_ID, ID);
                    mapping.setValue(DcMapping._B_REFERENCED_ID, referenceID);
                    
                    DatabaseManager.executeQuery(new Query(Query._INSERT, mapping, null, null), true);
                }
            } catch (Exception e) {
                logger.error("Failed to create reference. Conversion has failed.", e);
            }
        }
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
