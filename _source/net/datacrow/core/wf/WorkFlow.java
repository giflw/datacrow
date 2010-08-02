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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

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
    public static void handleRequests(Collection<DcObject> objects, Requests requests, boolean qryWasSuccess) {
        SwingUtilities.invokeLater(new UIUpdater(objects, requests, qryWasSuccess));
    }

    /**
     * Checks the database to see if the item already exists.
     * @param o The item to check.
     * @param isExisting Indicates if the check is performed for a new or an existing item.
     */
    public static boolean checkUniqueness(DcObject o, boolean isExisting) {
        return DatabaseManager.isUnique(o, isExisting);
    }

    
    public static void setValues(ResultSet rs, DcObject item, int[] fields) {
        try {
            Object value;
            String column;
            DcField field;
            for (int i = 0; i < fields.length; i++) {
                field = item.getField(fields[i]);
                column = field.getDatabaseFieldName();

                if (field.isUiOnly()) continue;
                
                value = rs.getObject(column);
                value = Utilities.isEmpty(value) ? null : value;
                item.setValue(fields[i], value);
            }
            
            item.setValue(Media._SYS_MODULE, item.getModule().getObjectName());

        } catch (Exception e) {
            logger.error("An error occurred while converting result set to items", e);
        }
    }
    
    /**
     * Converts the result set to a collection of items.
     * @param rs An unclosed SQL result set.
     * @return Collection of items.
     */
    public List<DcObject> convert(ResultSet rs, boolean minimal) {
        List<DcObject> objects = new ArrayList<DcObject>();

        try {
        	rs.isLast();
        } catch (Exception exp) {
            return objects;
        }

        try {
            String table = rs.getMetaData().getTableName(1);
            DcModule module = DcModules.getModuleForTable(table);
            
            int[] fields =  new int[rs.getMetaData().getColumnCount()];
            for (int i = 1; i < fields.length + 1; i++)
                fields[i-1] = module.getField(rs.getMetaData().getColumnName(i)).getIndex();
            
            DcObject dco;
            while (rs.next()) {
                dco = module.getItem();
                setValues(rs, dco, fields);

                if (DatabaseManager.initialized && !minimal) {
                    if (fields.length > 1) {
                        if (dco.getModule().canBeLend())
                            dco.setLoanInformation();
            
                        if (dco.getModule().getIndex() != DcModules._PICTURE)
                            dco.initializeImages();

                        dco.initializeReferences();
                    }
                }

                dco.setNew(false);
                dco.markAsUnchanged();
                objects.add(dco);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("An error occurred while converting result set to items", e);
        }
        
        try {
            rs.close();
        } catch (Exception e) {
            logger.warn("Failed to close the resultset", e);
        }
        
        return objects;
    }
}
