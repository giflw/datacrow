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

import net.datacrow.core.data.DataManager;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.helpers.Media;
import net.datacrow.core.wf.requests.Requests;

import org.apache.log4j.Logger;

/**
 * Separation between the UI and the Data layer; allows the UI to communicate
 * with the DatabaseManager. 
 * 
 * @author rj.vanderwaals
 */
public class WorkFlow {

    private static Logger logger = Logger.getLogger(WorkFlow.class.getName());
    
    public static void insertValues(DcObject o) {
        DatabaseManager.insertValues(o);
    }

    public static void updateValues(DcObject o) {
        DatabaseManager.updateValues(o);
    }

    public static void deleteValues(DcObject o) {
        DatabaseManager.deleteValues(o);
    }

    public static void handleRequests(Collection<DcObject> objects, Requests requests, boolean qryWasSuccess) {
        Runnable runMe = new UIUpdater(objects, requests, qryWasSuccess);
        SwingUtilities.invokeLater(runMe);
    }

    public static boolean checkUniqueness(DcObject o, boolean bUpdate) {
        return DatabaseManager.isUnique(o, bUpdate);
    }

    public List<DcObject> convertToDCObjects(ResultSet result) {
        List<DcObject> objects = new ArrayList<DcObject>();

        try {
        	result.isLast();
        } catch (Exception exp) {
            return objects;
        }

        try {
            int columnCount = result.getMetaData().getColumnCount();
            String[] columns = new String[columnCount];

            int k = 0;
            for (int i = 1; i < columnCount + 1; i++) {
                columns[k] = result.getMetaData().getColumnName(i);
                k++;
            }

            String sTableName = result.getMetaData().getTableName(1);
            
            while (result.next()) {
                DcObject dco = DcModules.getObjectForTable(sTableName);

                for (int i = 0; i < columns.length; i++) {
                    int type = result.getMetaData().getColumnType(i + 1);
                    String column = columns[i];
                    Object value = null;

                    if (type == Types.BOOLEAN) {
                        value = result.getBoolean(column);
                    } else if (type == Types.DATE) {
                        value = result.getDate(column);
                    } else {
                        value = result.getString(column);
                    }
                    dco.setValueForColumn(column, value);
                }

                if (dco.getModule().canBeLended()) {
                    Loan loan = DataManager.getCurrentLoan(dco.getID());
                    dco.setValue(DcObject._SYS_AVAILABLE, loan.isAvailable(dco.getID()));
                    dco.setValue(DcObject._SYS_LOANEDBY, loan.getPersonDescription());
                    dco.setValue(DcObject._SYS_LOANDURATION, loan.getDaysLoaned());
                }

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
            result.close();
        } catch (Exception e) {
            logger.warn("Failed to close the resultset", e);
        }
        
        return objects;
    }
}
