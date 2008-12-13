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
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.helpers.Media;
import net.datacrow.core.wf.requests.Requests;

import org.apache.log4j.Logger;

/**
 * Separation between the UI and the Data layer; allows the GUI to communicate
 * with the DatabaseManager. 
 * 
 * @author Robert Jan van der Waals
 */
public class WorkFlow {

    private static Logger logger = Logger.getLogger(WorkFlow.class.getName());
    
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

    /**
     * Converts the result set to a collection of items.
     * @param rs An unclosed SQL result set.
     * @return Collection of items.
     */
    public List<DcObject> convert(ResultSet rs) {
        List<DcObject> objects = new ArrayList<DcObject>();

        try {
        	rs.isLast();
        } catch (Exception exp) {
            return objects;
        }

        try {
            int columnCount = rs.getMetaData().getColumnCount();
            String[] columns = new String[columnCount];

            int k = 0;
            for (int i = 1; i < columnCount + 1; i++) {
                columns[k] = rs.getMetaData().getColumnName(i);
                k++;
            }

            String sTableName = rs.getMetaData().getTableName(1);
            
            while (rs.next()) {
                DcObject dco = DcModules.getObjectForTable(sTableName);

                for (int i = 0; i < columns.length; i++) {
                    int type = rs.getMetaData().getColumnType(i + 1);
                    String column = columns[i];
                    Object value = null;

                    if (type == Types.BOOLEAN) {
                        value = rs.getBoolean(column);
                    } else if (type == Types.DATE) {
                        value = rs.getDate(column);
                    } else {
                        value = rs.getString(column);
                    }
                    dco.setValueForColumn(column, value);
                }

                if (dco.getModule().canBeLend())
                    dco.setLoanInformation();

                if (!(dco instanceof Picture))
                    dco.initializeImages();

                dco.initializeReferences();

                dco.setValue(Media._SYS_MODULE, dco.getModule().getObjectName());
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
}
