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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;

import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;

import org.apache.log4j.Logger;

public class InsertQuery extends Query {
    
    private final static Logger logger = Logger.getLogger(InsertQuery.class.getName());
    
    private DcObject dco;
    
    public InsertQuery(DcObject dco) throws SQLException {
        super(dco.getModule().getIndex(), dco.getRequests());
        this.dco = dco;
        this.dco.setIDs();
    }
    
    @Override
    protected void clear() {
        super.clear();
        dco = null;
    }
    
    @SuppressWarnings("resource")
    @Override
    public List<DcObject> run() {
        boolean success = false;
        
        Collection<Object> values = new ArrayList<Object>();
        StringBuffer columns = new StringBuffer();

        // create non existing references
        createReferences(dco);
	       
        Collection<DcMapping> references = new ArrayList<DcMapping>();
        Collection<Picture> pictures = new ArrayList<Picture>();

        Connection conn = null;
        Statement stmt = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseManager.getAdminConnection();
            stmt = conn.createStatement();
        
            Picture picture;
            ImageIcon image;
            for (DcField field : dco.getFields()) {
                if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                    picture = (Picture) dco.getValue(field.getIndex());
                    image = picture != null ? (ImageIcon) picture.getValue(Picture._D_IMAGE) : null; 
                    if (image != null) {
                        if (image.getIconHeight() == 0 || image.getIconWidth() == 0) {
                            logger.warn("Image " + dco.getID() + "_" + field.getDatabaseFieldName() + ".jpg" + " is invalid and will not be saved");
                        } else {
                            picture.setValue(Picture._A_OBJECTID, dco.getID());
                            picture.setValue(Picture._B_FIELD, field.getDatabaseFieldName());
                            picture.setValue(Picture._C_FILENAME, dco.getID() + "_" + field.getDatabaseFieldName() + ".jpg");
                            picture.setValue(Picture._E_HEIGHT, image.getIconHeight());
                            picture.setValue(Picture._F_WIDTH, image.getIconWidth());
                            picture.isEdited(true);
                            pictures.add(picture);
                        }
                    }
                } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    Collection<DcMapping> c = (Collection<DcMapping>) dco.getValue(field.getIndex());
                    if (c != null) references.addAll(c);                
                    
                } else if (!field.isUiOnly()) {
                    if (columns.length() > 0)
                        columns.append(", ");
    
                    values.add(getQueryValue(dco, field.getIndex()));
                    columns.append(field.getDatabaseFieldName());
                }
            }
            
            String sqlPart = "";
            for (int i = 0; i < values.size(); i++)
                sqlPart += (sqlPart.length() > 0 ? ", ?" : "?"); 
            
            String sql = "INSERT INTO " + dco.getTableName() + " (" + columns + ") \r\n" + "VALUES (" + sqlPart + ");";
            
            ps = conn.prepareStatement(sql);
            setValues(ps, values);
            ps.execute();
            
            saveReferences(references, dco.getID());
            
            for (Picture p : pictures) {
                try {
                    new InsertQuery(p).run();
                    saveImage(p);
                } catch (SQLException e) {
                    logger.error("An error occured while inserting the following picture: " + p, e);
                }   
            }

            if (dco.getCurrentChildren() != null) {
                for (DcObject child : dco.getCurrentChildren()) {
                    try {
                        child.setValue(child.getParentReferenceFieldIndex(), dco.getID());
                        new InsertQuery(child).run();
                    } catch (SQLException e) {
                        logger.error("An error occured while inserting the following child object: " + child, e);
                    }                         
                }
            }
            
            success = true;
            
        } catch (SQLException e) {
            logger.error("An error occured while running the query", e);
        }
        
        try {
            if (ps != null) ps.close();
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            logger.error("Error while closing connection", e);
        }
        
        handleRequest(success);
        return null;
    }
    
    @Override
    protected void finalize() throws Throwable {
        clear();
        super.finalize();
    }
}
