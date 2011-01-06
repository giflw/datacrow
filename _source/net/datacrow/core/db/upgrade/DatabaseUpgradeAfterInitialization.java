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

import java.sql.ResultSet;
import java.sql.SQLException;

import net.datacrow.console.windows.log.LogForm;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.Version;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.util.DcSwingUtilities;

import org.apache.log4j.Logger;

/**
 * If possible, perform upgrades / changes in the DataManagerConversion class!
 * 
 * Converts the current database before the actual module tables are created / updated.
 * This means that the code here defies workflow logic and is strictly to be used for
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
                DcSwingUtilities.displayMessage("Data Crow will perform a non critical upgrade. This process will take a couple of minutes.");
            	upgraded = fillUIPersistFields();
            }

            if (upgraded) {
                lf.close();
                DcSwingUtilities.displayMessage("The upgrade was successful. Data Crow will now continue.");
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
    
    private boolean fillUIPersistFields() {
        boolean upgraded = false;
        
        ResultSet rs;
        ResultSet rs2;
        
        DcField fldPersist;
//        DcObject reference;
        String referenceID;
        
        String ID;
        String sql;

        for (DcModule module : DcModules.getAllModules()) {

            if (    module.isAbstract() || 
                    module.getType() == DcModule._TYPE_TEMPLATE_MODULE)
                continue;
            
            DcObject dco = module.getItem();
            for (DcField fld : module.getFields()) {
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
//                                reference = DataManager.getItem(fld.getReferenceIdx(), referenceID);
                                
                                dco.setValueLowLevel(DcObject._ID, ID);
                                fldPersist = module.getPersistentField(fld.getIndex());
                                dco.setValue(fldPersist.getIndex(), referenceID);
                                dco.setUpdateGUI(false);
                                try {
                                    logger.info("Module: " + module + "/" + module.getTableName() + ": setting value '" + referenceID + "' for field " + fldPersist);
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
