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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.wf.WorkFlow;
import net.datacrow.core.wf.requests.IRequest;
import net.datacrow.core.wf.requests.IUpdateUIRequest;
import net.datacrow.core.wf.requests.Requests;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * The Query class creates SQL statements needed to remove, update, insert and 
 * select items from the database. Queries created by this class ensure the integrity 
 * of the data.
 * 
 * Note that the Query class can actually contain several SQL statements.
 * 
 * @author Robert Jan van der Waals
 */
public abstract class Query {
    
    private final static Logger logger = Logger.getLogger(Query.class.getName());
    private final int module;

    private Requests requests;
    
    private boolean log = true;

    /**
     * Constructs a new Query object. 
     * @param queryType type of query
     * @param dco template
     * @param options query options
     * @param requests actions / requests to be executed
     * @throws SQLException
     */
    public Query(int module, Requests requests) {
        this.module = module;
        this.requests = requests;
    } 

    protected void clear() {
        if (requests != null) requests.clear();
        requests = null;
    }
    
    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public abstract List<DcObject> run();
    
    protected void handleRequest(boolean success) {
        if (requests != null)  {
            Requests uiRequests = new Requests();

            for (IRequest request : requests.get()) {
                requests.remove(request);
                if (request instanceof IUpdateUIRequest)
                    uiRequests.add(request);
                else 
                    request.execute();
            }

            if (uiRequests.size() > 0)
            	WorkFlow.handleRequests(uiRequests, success);
        }
    }
    
    protected void saveReferences(Collection<DcMapping> references, String parentID) {
        for (DcMapping mapping : references) {
            try {
                mapping.setValue(DcMapping._A_PARENT_ID, parentID);
                mapping.saveNew(false);
            } catch (ValidationException ve) {
                logger.error("An error occured while inserting the following reference " + mapping, ve);
            }                    
        }
    }
    
    protected PreparedStatement getPreparedStament(String sql) throws SQLException {
        return DatabaseManager.getAdminConnection().prepareStatement(sql);
    }
    
    protected void setValues(PreparedStatement ps, Collection<Object> values) {
        int pos = 1;
        for (Object value : values) {
            try {
                ps.setObject(pos, value);
                pos++;
            } catch (Exception e) {
                logger.error("Could not set value [" + value + "] on position [" + pos + "] for " + ps, e);
                try {ps.setObject(pos, null); pos++;} catch (Exception e2) {
                    logger.error("Could not set [" + pos + "] to NULL (to correct error with value [" + value + "])", e2);
                }
            }
        }
    }
    
    protected Object getQueryValue(DcObject dco, int index) {
        return Utilities.getQueryValue(dco.getValue(index), dco.getField(index));
    }
    
    public int getModuleIdx() {
        return module;
    }
    
    public DcModule getModule() {
        return DcModules.get(getModuleIdx());
    }

    /**
     * Gets the requests waiting to be executed.
     */
    public Requests getRequests() {
        return requests;
    }
    
    @SuppressWarnings("unchecked")
    protected void createReferences(DcObject dco) {
        Object value;
        DcObject reference;
        DcObject existing;
        for (DcField field : dco.getFields()) {        
            value = dco.getValue(field.getIndex());
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                reference = value instanceof DcObject ? (DcObject) value : null;
                if (reference == null) continue;
                
                // also created references for the sub items of this reference...
                createReferences(reference);
                
                try { 
                    existing = DataManager.getItem(reference.getModule().getIndex(), reference.getID());
                    existing = existing == null ? DataManager.getItemByKeyword(reference.getModule().getIndex(), reference.toString()) : existing;
                    if (existing == null) {
                        // save the value that was set
                        reference.setValidate(false);
                        reference.saveNew(false);
                        reference.setValidate(true);
                    } else {
                        // reuse the existing value
                        dco.setValue(field.getIndex(), existing);
                    }

                } catch (Exception e) {
                    logger.error("Error (" + e + ") while creating a new reference item; " + reference, e);
                }
                
            } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                if (value == null)
                    continue;
                
                for (DcMapping mapping : (Collection<DcMapping>) value) {
                    reference = mapping.getReferencedObject();
                    try { 
                        if (reference == null) continue;
                        
                        // also created references for the sub items of this reference...
                        createReferences(reference);
                        
                        existing = DataManager.getItem(reference.getModule().getIndex(), reference.getID());
                        existing = existing == null ? DataManager.getItemByKeyword(reference.getModule().getIndex(), reference.toString()) : existing;

                        if (existing == null) {
                            reference.setValidate(false);
                            reference.saveNew(false);
                            reference.setValidate(true);
                        } else {
                            mapping.setValue(DcMapping._B_REFERENCED_ID, existing.getID());
                        }
                    } catch (Exception e) {
                        logger.error("Error (" + e + ") while creating a new reference item; " + reference, e);
                    }
                }
            }
        }
    }  
    
    protected void deleteImage(Picture picture) {
        String filename = (String) picture.getValue(Picture._C_FILENAME);

        if (filename == null) return;
        
        String filename1 = DataCrow.imageDir + filename;

        File file1 = new File(filename1);
        if (file1.exists()) file1.delete();
        
        String filename2 = picture.getScaledFilename(DataCrow.imageDir + filename);
        
        File file2 = new File(filename2);
        if (file2.exists()) file2.delete();
    }    
    
    protected void saveImage(Picture picture) {
        String filename = picture.getImageFilename();
        
        if (filename == null)  return;
        
        File file = new File(DataCrow.imageDir, filename);
        String imageFile = file.toString();
        
        try {
            if (file.exists()) 
                file.delete();
            
            DcImageIcon icon = (DcImageIcon) picture.getValue(Picture._D_IMAGE);
            File realImgFile = icon.getFilename() != null ? new File(icon.getFilename()) : null;
            if (realImgFile != null && realImgFile.exists()) {
                FileInputStream fis  = new FileInputStream(realImgFile);
                FileOutputStream fos = new FileOutputStream(file);
                try {
                    byte[] buf = new byte[1024];
                    int i = 0;
                    while ((i = fis.read(buf)) != -1) {
                        fos.write(buf, 0, i);
                    }
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (fis != null) fis.close();
                    if (fos != null) fos.close();
                }
                Utilities.writeScaledImageToFile(icon, new File(picture.getScaledFilename(imageFile)));
            } else {
                ImageIO.write(Utilities.toBufferedImage(icon), "PNG", file);
                Utilities.writeScaledImageToFile(icon, new File(picture.getScaledFilename(imageFile)));
                icon.flush();
            }
        } catch (Exception e) {
            logger.error("Could not save [" + imageFile + "]", e);
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        clear();
        super.finalize();
    }
 }
