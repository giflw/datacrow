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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;


public class UpdateQuery extends Query {
    
    private final static Logger logger = Logger.getLogger(UpdateQuery.class.getName());
    
    private DcObject dco;

    public UpdateQuery(DcObject dco) {
        super(dco.getModule().getIndex(), dco.getRequests());
        this.dco = dco;
    }
    
    @Override
    protected void clear() {
        super.clear();
        dco = null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<DcObject> run() {
        boolean success = false;
        
        Collection<Picture> pictures = new ArrayList<Picture>();
        Collection<Collection<DcMapping>> references = new ArrayList<Collection<DcMapping>>();
        Collection<Object> values = new ArrayList<Object>();
        
        // create non existing references
        createReferences(dco);

        PreparedStatement ps = null;
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();

            StringBuffer sbValues = new StringBuffer();
            for (DcField field : dco.getFields()) {
                // Make sure only changed fields are updated
                if (!dco.isChanged(field.getIndex()))
                    continue;
                
                if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                    Picture picture = (Picture) dco.getValue(field.getIndex());
                    if (picture != null && (picture.isNew() || picture.isEdited() || picture.isDeleted())) {
                        picture.setValue(Picture._A_OBJECTID, dco.getID());
                        picture.setValue(Picture._B_FIELD, field.getDatabaseFieldName());
                        picture.setValue(Picture._C_FILENAME, dco.getID() + "_" + field.getDatabaseFieldName() + ".jpg");
                        
                        ImageIcon icon = (ImageIcon) picture.getValue(Picture._D_IMAGE);
                        if (icon != null) {
                            picture.setValue(Picture._E_HEIGHT, Long.valueOf(icon.getIconHeight()));
                            picture.setValue(Picture._F_WIDTH, Long.valueOf(icon.getIconWidth()));
                            pictures.add(picture);
                        }
                    }
                } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    Collection<DcMapping> c = (Collection<DcMapping>) dco.getValue(field.getIndex());
                    
                    if (c != null) references.add(c);
                    
                    if (dco.isChanged(field.getIndex())) {
                        DcModule mappingMod = DcModules.get(DcModules.getMappingModIdx(field.getModule(), field.getReferenceIdx(), field.getIndex()));
                        String sql = "DELETE FROM " + mappingMod.getTableName() + " WHERE " +  
                                     mappingMod.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() + " = '" + dco.getID() + "'";
                        stmt.execute(sql);
                    }
                } else if (dco.isChanged(field.getIndex()) && !field.isUiOnly()) {
                    if (sbValues.length() > 0)
                        sbValues.append(", ");
    
                    sbValues.append(field.getDatabaseFieldName());
                    sbValues.append(" = ?");
                    values.add(getQueryValue(dco, field.getIndex()));
                }
            }
    
            String s = sbValues.toString();
            if (dco.getModule().getIndex() != DcModules._PICTURE && !Utilities.isEmpty(values)) {
                ps = conn.prepareStatement("UPDATE " + dco.getTableName() + " SET " + s + "\r\n WHERE ID = '" + dco.getID() + "'");
                setValues(ps, values);
                ps.execute();
                ps.close();
            } else if (!Utilities.isEmpty(values)) {
            	
                ps = conn.prepareStatement("UPDATE " + dco.getTableName() + " SET " + s + "\r\n WHERE " +
                        dco.getDatabaseFieldName(Picture._A_OBJECTID) + " = '" + dco.getValue(Picture._A_OBJECTID) + "' AND " +
                        dco.getDatabaseFieldName(Picture._B_FIELD) + " = '" + dco.getValue(Picture._B_FIELD) + "'");
                setValues(ps, values);
                ps.execute();
                ps.close();
            }
    
            for (Collection<DcMapping> c : references) {
                for (DcMapping mapping : c) {
                    stmt.execute("INSERT INTO " + mapping.getTableName() + 
                                 " (" + mapping.getDatabaseFieldName(DcMapping._A_PARENT_ID) + ", " +
                                 mapping.getDatabaseFieldName(DcMapping._B_REFERENCED_ID) + 
                                 ") \r\n VALUES ('" + dco.getID() + "', '" + mapping.getReferencedID() + "');");
                }
            }
            
            for (Picture picture : pictures) {
                if (picture.isNew()) {
                    new InsertQuery(picture).run();
                    saveImage(picture);
                } else if (picture.isEdited()) {
                    new UpdateQuery(picture).run();
                    saveImage(picture);
                } else if (picture.isDeleted()) {
                    stmt.execute("DELETE FROM " + picture.getTableName() + " WHERE " +
                            picture.getField(Picture._A_OBJECTID).getDatabaseFieldName() + " = '" + dco.getID() + "' AND " +
                            picture.getField(Picture._B_FIELD).getDatabaseFieldName() + " = '" +  picture.getValue(Picture._B_FIELD) + "'");
                    deleteImage(picture);    
                }
            }
            
            for (DcObject child : dco.getCurrentChildren()) {
                if (child.isChanged()) {
                    boolean exists = false;
                    if (child.getID() != null) {
                        ResultSet rs = DatabaseManager.executeSQL("select count(*) from "
                                        + child.getModule().getTableName() + " where ID = '" + child.getID() + "'");
                        rs.next();
                        exists = rs.getInt(1) > 0;
                        rs.close();
                    }
                    Query query = exists ? new UpdateQuery(child) : new InsertQuery(child);
                    query.run();
                }
            }
            
            success = true;
            pictures.clear();
        } catch (SQLException e) {
            logger.error("An error occured while running the query", e);
        }
        
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.error("Error while closing connection", e);
        }

        handleRequest(null, success);
        
        if (dco.isUpdateGUI() && success && DataCrow.isInitialized()) 
        	updateUI(dco);
        
        return null;
    }
    
    private void updateUI(DcObject dco) {
        if (dco.getModule().getSearchView() != null) 
            dco.getModule().getSearchView().update(dco);
        
    	for (DcModule module : DcModules.getReferencingModules(dco.getModule().getIndex())) {
    		if (module.isSearchViewInitialized())
    			module.getSearchView().refreshQuickView();
    	}

    	for (DcModule module : DcModules.getAbstractModules(dco.getModule())) {
    		if (module.isSearchViewInitialized())
    			module.getSearchView().update(dco);
    	}
    }
}
