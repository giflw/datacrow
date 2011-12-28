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

package net.datacrow.core.wf;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import net.datacrow.core.DcRepository;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.Media;
import net.datacrow.core.wf.requests.Requests;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * Separation between the UI and the Data layer; allows the GUI to communicate
 * with the DatabaseManager. 
 * 
 * @author Robert Jan van der Waals
 */
public class WorkFlow {

    private static Logger logger = Logger.getLogger(WorkFlow.class.getName());
    
    private static WorkFlow instanze;

    static {
        instanze = new WorkFlow();
    }
    
    private WorkFlow() {}
    
    public static WorkFlow getInstance() {
        return instanze;
    }
    
    /**
     * Saves the item to the database.
     * @param o
     */
    public static void insert(DcObject o) {
        DatabaseManager.insert(o);
    }

    /**
     * Updates the item in the database.
     * @param o
     */
    public static void update(DcObject o) {
        DatabaseManager.update(o);
    }

    /**
     * Deletes the item from the database.
     * @param o
     */
    public static void delete(DcObject o) {
        DatabaseManager.delete(o);
    }

    /**
     * Handles the requests.
     * @param objects
     * @param requests
     * @param qryWasSuccess
     */
    public static void handleRequests(Requests requests, boolean qryWasSuccess) {
        SwingUtilities.invokeLater(new UIUpdater(requests, qryWasSuccess));
    }

    /**
     * Checks the database to see if the item already exists.
     * @param o The item to check.
     * @param isExisting Indicates if the check is performed for a new or an existing item.
     */
    public static boolean checkUniqueness(DcObject o, boolean isExisting) {
        return DatabaseManager.isUnique(o, isExisting);
    }

    /**
     * Converts the result set to a collection of items.
     * @param rs An unclosed SQL result set.
     * @return Collection of items.
     */
    public List<DcObject> convert(ResultSet rs, int[] requestedFields) {
        List<DcObject> objects = new ArrayList<DcObject>();

        try {
            rs.isLast();
        } catch (Exception exp) {
            return objects;
        }

        try {
        	ResultSetMetaData md = rs.getMetaData();
        	
        	int fieldStart = 1;
            int[] fields = null;
            DcObject dco;
            DcModule module = null;
            while (rs.next()) {
            	try {
            		int moduleIdx = rs.getInt("MODULEIDX");
   			 		module = DcModules.get(moduleIdx);
   			 		fieldStart = 2;
            	} catch (Exception e) {
            		module = DcModules.get(md.getTableName(1));
            	}
            	
            	if (fields == null) {
	                fields = new int[md.getColumnCount() - (fieldStart - 1)];
	                int fieldIdx = 0;
	                for (int i = fieldStart; i < fields.length + fieldStart; i++) {
	                	String column = md.getColumnName(i);
	                	
	                	try {
	                	    fields[fieldIdx++] = module.getField(column).getIndex();
	                	} catch (Exception e) {
	                	    logger.error("Could not find field for column " + column, e);
	                	}
	                }
            	}
            	
            	dco = module.getItem();
                setValues(rs, dco, fields, requestedFields);

                objects.add(dco);
            }
        } catch (Exception e) {
            logger.error("An error occurred while converting result set to items", e);
        }
        
        try {
            rs.close();
        } catch (Exception e) {
            logger.warn("Failed to close the resultset", e);
        }
        
        return objects;
    }
    
    public static void setValues(ResultSet rs, DcObject item, int[] fields, int[] requestedFields) {
        try {
            Object value = null;
            String column;
            for (int i = 0; i < fields.length; i++) {
                DcField field = item.getField(fields[i]);
                column = field.getDatabaseFieldName();

                if (field.isUiOnly()) continue;
                
                try {
                    value = rs.getObject(column);
                    value = Utilities.isEmpty(value) ? null : value;
                    item.setValue(fields[i], value);
                } catch (Exception e) {
                    logger.error("Could not retrieve and/or set value for field " + field, e);
                }
            }
            
            item.setValue(Media._SYS_MODULE, item.getModule().getObjectName());
            
            boolean loan = requestedFields == null;
            boolean images = requestedFields == null;
            boolean references = requestedFields == null;
            
            if (DatabaseManager.initialized) {
                if (requestedFields != null) {
                    for (int field : requestedFields) {
                        if (item.getModule().canBeLend() &&
                            (field == DcObject._SYS_AVAILABLE || 
                             field == DcObject._SYS_LOANDAYSTILLOVERDUE ||
                             field == DcObject._SYS_LOANDUEDATE ||
                             field == DcObject._SYS_LOANDURATION))  
                            loan |= true;
                        else if (item.getModule().getIndex() != DcModules._PICTURE &&
                        		item.getField(field).getValueType() == DcRepository.ValueTypes._PICTURE)
                            images |= true;
                        else if (item.getField(field).getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION)
                            item.initializeReferences(field, false);
                    }
                }
                        
                if (loan) 
                    item.setLoanInformation();
                
                if (images && item.getModule().isHasImages()) 
                    item.initializeImages();
                
                if (references && item.getModule().isHasReferences()) 
                    item.initializeReferences();
            }

            item.setNew(false);
            item.markAsUnchanged();

        } catch (Exception e) {
            logger.error("An error occurred while converting result set to items", e);
        }
    }
}
