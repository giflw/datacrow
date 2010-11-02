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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.objects.Picture;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class DeleteQuery extends Query {
    
    private final static Logger logger = Logger.getLogger(DeleteQuery.class.getName());
    
    private DcObject dco;
    
    public DeleteQuery(DcObject dco) throws SQLException {
        super(dco.getModule().getIndex(), dco.getRequests());
        this.dco = dco;
    }
    
    @Override
    protected void clear() {
        super.clear();
        dco = null;
    }

    @Override
    public List<DcObject> run() {
    	boolean success = false;
        Connection conn = null;
        Statement stmt = null;
        
        String ID = dco.getID();

        try { 
            conn = DatabaseManager.getAdminConnection();
            stmt = conn.createStatement();
            
            if (dco.hasPrimaryKey()) {
                stmt.execute("DELETE FROM " + dco.getTableName() + " WHERE ID = '" + dco.getID() + "'");
            } else {
                String sql = "DELETE FROM " + dco.getTableName() + " WHERE ";
                
                int counter = 0;
                for (DcField field : dco.getFields()) {
                    
                    boolean isString = field.getValueType() == DcRepository.ValueTypes._STRING ||
                                       field.getValueType() == DcRepository.ValueTypes._DATE;
                    if (    dco.isChanged(field.getIndex()) && 
                            !field.isUiOnly() && 
                            !Utilities.isEmpty(dco.getValue(field.getIndex()))) {
                        
                        sql += counter == 0 ? "" : " AND ";
                        sql += field.getDatabaseFieldName() + " = ";
                        sql += isString ? "'" : "";
                        sql += String.valueOf(Utilities.getQueryValue(dco.getValue(field.getIndex()), field)).replaceAll("\'", "''");
                        sql += isString ? "'" : "";
                        counter++;
                    }
                }
                stmt.execute(sql);
                
                try {
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    logger.error("Error while closing connection", e);
                }
                
                // exit: other queries depend on ID!
                return null;
            }

            if (dco.getModule().canBeLend())
                stmt.execute("DELETE FROM " + DcModules.get(DcModules._LOAN).getTableName() + " WHERE " +
                             DcModules.get(DcModules._LOAN).getField(Loan._D_OBJECTID).getDatabaseFieldName() + " = '" + dco.getID() + "'");

            // Delete children. Ignore any abstract module (parent and/or children)
            if (    dco.getModule().getChild() != null && 
                   !dco.getModule().isAbstract() && 
                   !dco.getModule().getChild().isAbstract()) {
                
                DcModule childModule = dco.getModule().getChild(); 
                stmt.execute("DELETE FROM " + childModule.getTableName() + " WHERE " + 
                             childModule.getField(childModule.getParentReferenceFieldIndex()).getDatabaseFieldName() + " = '" + dco.getID() + "'");
            }
            
            // Remove any references to the to be deleted item.
            if (dco.getModule().hasDependingModules()) {
                for (DcModule m : DcModules.getReferencingModules(dco.getModule().getIndex())) {
                    if (m.isAbstract()) continue;
                    
                    if (m.getType() == DcModule._TYPE_MAPPING_MODULE) {
                        stmt.execute("DELETE FROM " + m.getTableName() + " WHERE " + 
                                     m.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + " = '" + dco.getID() + "'");
                    } else {
                        for (DcField field : m.getFields()) {
                            if (!field.isUiOnly() && field.getReferenceIdx() == dco.getModule().getIndex()) {
                                stmt.execute("UPDATE " + m.getTableName() + " SET " +  field.getDatabaseFieldName() + " = NULL WHERE " + 
                                             field.getDatabaseFieldName() + " = '" + dco.getID() + "'");
                            }
                        }
                    }
                }
            }
            
            for (DcField field : dco.getFields()) {
                if (field.getValueType() == DcRepository.ValueTypes._PICTURE && dco.isFilled(field.getIndex()))
                    deleteImage((Picture) dco.getValue(field.getIndex()));
            }
            
            stmt.execute("DELETE FROM " + DcModules.get(DcModules._PICTURE).getTableName() + " WHERE " +
                         DcModules.get(DcModules._PICTURE).getField(Picture._A_OBJECTID).getDatabaseFieldName() + " = '" + dco.getID() + "'");
            
            success = true;
        } catch (SQLException se) {
            logger.error(se, se);
        }
        
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.error("Error while closing connection", e);
        }
        
        handleRequest(null, success);
        
        if (dco.isUpdateGUI() && DataCrow.isInitialized() && success && dco.getModule().getSearchView() != null) {
            dco.getModule().getSearchView().remove(ID);
        	for (DcModule module : DcModules.getReferencingModules(dco.getModule().getIndex())) {
        		if (module.getSearchView() != null)
        			module.getSearchView().refreshQuickView();
        	}
            
        	for (DcModule module : DcModules.getAbstractModules(dco.getModule())) {
        		if (module.isSearchViewInitialized())
        			module.getSearchView().remove(ID);
        	}
        }

        clear();
        return null;
    }
}
