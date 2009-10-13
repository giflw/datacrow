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
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;

import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcMediaModule;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.MappingModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.helpers.ContactPerson;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.IRequest;
import net.datacrow.core.wf.requests.ImageRequest;
import net.datacrow.core.wf.requests.Requests;
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
public class Query {
    
    private static Logger logger = Logger.getLogger(Query.class.getName());

    private final static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public static final int _UNDEFINED = 0;
    public static final int _UPDATE = 1;
    public static final int _CREATE = 2;
    public static final int _INSERT = 3;
    public static final int _SELECT = 4;
    public static final int _DELETE = 5;

    protected static final int _SELECTPRECISE = 6;
    protected static final int _SELECTOR = 7;
    protected static final int _SELECTPRECISEOR = 8;

    private final int module;
    private int queryType;

    private boolean bPreciseSelect = false;
    private boolean bComplyToAllConditions = true;

    private List<PreparedStatement> queries;
    private String objectID;

    private Requests requests;
    
    private String[] ordering;
    private boolean bSilence = false;
    private boolean isBatch = false;
    private boolean endOfBatch = false;    
    
    /**
     * Constructs a new Query object. 
     * @param queryType type of query
     * @param dco template
     * @param options query options
     * @param requests actions / requests to be executed
     * @throws SQLException
     */
    public Query(int queryType,
                 DcObject dco,
                 QueryOptions options,
                 Requests requests) throws SQLException {

        long start = logger.isDebugEnabled() ? new Date().getTime() : 0;
        
        module = dco.getModule().getIndex();

        setSilence(dco.isSilent());

        if (options != null) {
            bPreciseSelect = options.getPreciseSelect();
            bComplyToAllConditions = options.getComplyAllConditions();
            ordering = options.getOrdering();
        }

        this.requests = requests == null ? new Requests() : requests;
        this.queryType = queryType;
        this.objectID = dco.getID();

        if (this.queryType == _SELECT) {
            if (bPreciseSelect && !bComplyToAllConditions) {
                this.queryType = _SELECTPRECISEOR;
            } else if (bPreciseSelect) {
                this.queryType = _SELECTPRECISE;
            } else if (!bComplyToAllConditions) {
                this.queryType = _SELECTOR;
            }
        }

        queries = getQueries(dco);
        
        if (logger.isDebugEnabled()) {
            long end = new Date().getTime();
            logger.debug("Query was generated in " + (end - start) + "ms");
        }
    } 
    
    public List<PreparedStatement> getQueries() {
        return queries;
    }
    
    public PreparedStatement getQuery() {
        return getQueries().get(0);
    }
    
    private List<PreparedStatement> getQueries(DcObject dco) throws SQLException {
        List<PreparedStatement> queries = new ArrayList<PreparedStatement>();
        switch (queryType) {
            case _UPDATE :
                queries.addAll(getUpdateQueries(dco));
                break;
            case _CREATE :
                queries.addAll(getCreateTableQuery(dco));
                break;
            case _INSERT :
                queries.addAll(getInsertQueries(dco));
                break;
            case _DELETE :
                queries.addAll(getDeleteQueries(dco));
                break;
            case _UNDEFINED :
            case _SELECT :
            case _SELECTPRECISE :
            case _SELECTOR :
            case _SELECTPRECISEOR :
                queries.addAll(getSelectQueries(dco));
                break;
        }
        return queries;
    }
    
    private boolean isEmpty(Object o) {
        String emptyValue = DcResources.getText("lblEmptySearchValue");
        return o == null || o.equals(emptyValue) || o.equals("0") || o.equals("NULL");
    }

    @SuppressWarnings({"unchecked"})
    private List<PreparedStatement> getInsertQueries(DcObject dco) throws SQLException {
        
        dco.setIDs();
        
        Collection<Object> values = new ArrayList<Object>();
        StringBuffer columns = new StringBuffer();

        // create non existing references
        createReferences(dco);
        
        Collection<DcMapping> references = new ArrayList<DcMapping>();
        Collection<Picture> pictures = new ArrayList<Picture>();

        for (DcField field : dco.getFields()) {
            if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                Picture picture = (Picture) dco.getValue(field.getIndex());
                ImageIcon image = picture != null ? (ImageIcon) picture.getValue(Picture._D_IMAGE) : null; 
                if (image != null) {
                    if (image.getIconHeight() == 0 || image.getIconWidth() == 0) {
                    	logger.warn("Image " + dco.getID() + "_" + field.getDatabaseFieldName() + ".jpg" + " is invalid and will not be saved");
                    } else {
	                    picture.setValue(Picture._A_OBJECTID, dco.getID());
	                    picture.setValue(Picture._B_FIELD, field.getDatabaseFieldName());
	                    picture.setValue(Picture._C_FILENAME, dco.getID() + "_" + field.getDatabaseFieldName() + ".jpg");
	                    picture.setValue(Picture._E_HEIGHT, image.getIconHeight());
	                    picture.setValue(Picture._F_WIDTH, image.getIconWidth());
	                    picture.isNew(true);
	                    pictures.add(picture);
                    }
                }
            } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                Collection<DcMapping> c = (Collection<DcMapping>) dco.getValue(field.getIndex());
                if (c != null) references.addAll(c);                
                
            } else if (!field.isUiOnly()) {
                if (columns.length() > 0)
                    columns.append(", ");

                if (!(dco.getValue(field.getIndex()) instanceof Picture)) {
                    values.add(getQueryValue(dco, field.getIndex()));
                    columns.append(field.getDatabaseFieldName());
                }
            }
        }
        
        List<PreparedStatement> queries = new ArrayList<PreparedStatement>();
        if (references.size() > 0) {
            for (DcMapping mapping : references) {
                queries.add(getPreparedStatement("INSERT INTO " + mapping.getTableName() + 
                                    " (" + mapping.getDatabaseFieldName(DcMapping._A_PARENT_ID) + ", " +
                                           mapping.getDatabaseFieldName(DcMapping._B_REFERENCED_ID) + 
                                     ") \r\n VALUES (" + dco.getID() + ", " + mapping.getReferencedId() + ")"));
            }
        }        
        
        String sqlPart = "";
        for (int i = 0; i < values.size(); i++)
            sqlPart += (sqlPart.length() > 0 ? ", ?" : "?"); 
        
        String sql = "INSERT INTO " + dco.getTableName() + " (" + columns + ") \r\n" + "VALUES (" + sqlPart + ");";
        PreparedStatement ps = getPreparedStatement(sql);
        setValues(ps, values);
        queries.add(ps);
        
        for (Picture picture : pictures) {
            queries.addAll(getInsertQueries(picture));
            requests.add(new ImageRequest(picture, ImageRequest._SAVE));
        }
        
        pictures.clear();
        return queries;
    }

    private List<PreparedStatement> getCreateTableQuery(DcObject dco) throws SQLException {
        String columns = "";

        for (DcField field : dco.getFields()) {
            if (!field.isUiOnly()) {
                if (columns.length() > 0)
                    columns += ", ";

                columns += field.getDatabaseFieldName() + " " + field.getDataBaseFieldType();
                if (field.getIndex() == DcObject._ID) {
                    columns += " PRIMARY KEY";
                }
            }
        }
        
        String sql = "CREATE MEMORY TABLE " + dco.getTableName() + "\r\n(" + columns + ");";
        List<PreparedStatement> queries = new ArrayList<PreparedStatement>();
        queries.add(getPreparedStatementAdmin(sql));
        return queries;
    }

    @SuppressWarnings("unchecked")
    private List<PreparedStatement> getUpdateQueries(DcObject dco) throws SQLException {
        Collection<Picture> pictures = new ArrayList<Picture>();
        Collection<Collection<DcMapping>> references = new ArrayList<Collection<DcMapping>>();
        Collection<Object> values = new ArrayList<Object>();
        
        // create non existing references
        createReferences(dco);
        
        List<PreparedStatement> queries = new ArrayList<PreparedStatement>();
        StringBuffer sbValues = new StringBuffer();
        for (DcField field : dco.getFields()) {
            // Make sure only changed fields are updated
            if (!dco.isChanged(field.getIndex()))
                continue;
            
            if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                Picture picture = (Picture) dco.getValue(field.getIndex());
                if (picture != null && (picture.isNew() || picture.isUpdated() || picture.isDeleted())) {
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
                                 mappingMod.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() + " = " + dco.getID();
                    queries.add(getPreparedStatement(sql));
                }
            } else if (dco.isChanged(field.getIndex()) && !field.isUiOnly()) {
                if (sbValues.length() > 0)
                    sbValues.append(", ");

                sbValues.append(field.getDatabaseFieldName());
                sbValues.append(" = ?");
                values.add(getQueryValue(dco, field.getIndex()));
            }
        }

        if (!(dco instanceof Picture)) {
            String sql = "UPDATE " + dco.getTableName() + " SET " + sbValues + "\r\n WHERE ID = " + dco.getID();
            PreparedStatement ps = getPreparedStatement(sql);
            setValues(ps, values);
            queries.add(ps);
        } else {
            String sql = "UPDATE " + dco.getTableName() + " SET " + sbValues + "\r\n WHERE " +
                         dco.getDatabaseFieldName(Picture._A_OBJECTID) + " = " + dco.getValue(Picture._A_OBJECTID) + " AND " +
                         dco.getDatabaseFieldName(Picture._B_FIELD) + " = ?";
            
            values.add(dco.getValue(Picture._B_FIELD));
            PreparedStatement ps = getPreparedStatement(sql);
            setValues(ps, values);
            queries.add(ps);
        }

        if (references.size() > 0) {
            for (Collection<DcMapping> c : references) {
                for (DcMapping mapping : c) {
                    String sql = "INSERT INTO " + mapping.getTableName() + 
                                 " (" + mapping.getDatabaseFieldName(DcMapping._A_PARENT_ID) + ", " +
                                 mapping.getDatabaseFieldName(DcMapping._B_REFERENCED_ID) + 
                                 ") \r\n VALUES (" + dco.getID() + ", " + mapping.getReferencedId() + ");";
                    queries.add(getPreparedStatement(sql)); 
                }
            }
        }
        
        for (Picture picture : pictures) {
            Collection<Picture> currentPics = DataManager.getPictures(dco.getID());
            
            boolean isReallyNew = picture.isNew() && (currentPics == null || !currentPics.contains(picture));
            if (!isReallyNew && picture.isNew())
                picture.isUpdated(!isReallyNew);
            
            // no images = create this image instead of updating it (3.4.19)
            if (picture.isUpdated() && (currentPics == null || currentPics.isEmpty()))
                picture.isNew(true);
            
            if (picture.isUpdated()) {
                queries.addAll(getUpdateQueries(picture));
                requests.add(new ImageRequest(picture, ImageRequest._SAVE));                
            } else if (picture.isDeleted()) {
                String sql = "DELETE FROM " + picture.getTableName() + " WHERE " +
                             picture.getField(Picture._A_OBJECTID).getDatabaseFieldName() + " = " + dco.getID() + " AND " +
                             picture.getField(Picture._B_FIELD).getDatabaseFieldName() + " = ?";
                
                PreparedStatement ps = getPreparedStatement(sql);
                ps.setString(1, (String) picture.getValue(Picture._B_FIELD));
                
                queries.add(ps);
                requests.add(new ImageRequest(picture, ImageRequest._DELETE));                
            } else if (picture.isNew()) {
                queries.addAll(getInsertQueries(picture));
                requests.add(new ImageRequest(picture, ImageRequest._SAVE));
            }
        }
        
        pictures.clear();
        return queries;
    }
    
    @SuppressWarnings("unchecked")
    private void createReferences(DcObject dco) {
        for (DcField field : dco.getFields()) {        
            Object value = dco.getValue(field.getIndex());
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                
                DcObject reference = value instanceof DcObject ? (DcObject) value : null;
                
                if (reference == null)
                    continue;
                
                try { 
                    DcObject existing = DataManager.getObject(reference.getModule().getIndex(), reference.getID());
                    existing = existing == null ? DataManager.getObjectForString(reference.getModule().getIndex(), reference.toString()) : existing;
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
                    DcObject reference = mapping.getReferencedObject();
                    try { 
                        if (reference == null) continue;
                        
                        DcObject existing = DataManager.getObject(reference.getModule().getIndex(), reference.getID());
                        existing = existing == null ? DataManager.getObjectForString(reference.getModule().getIndex(), reference.toString()) : existing;

                        if (existing == null) {
                            reference.saveNew(false);
                        } else {
                            mapping.setReferencedObject(existing);
                            mapping.setValue(DcMapping._B_REFERENCED_ID, existing.getID());
                        }
                    } catch (Exception e) {
                        logger.error("Error (" + e + ") while creating a new reference item; " + reference, e);
                    }
                }
            }
        }
    }
    
    private PreparedStatement getPreparedStatement(String sql) throws SQLException {
        return DatabaseManager.getConnection().prepareStatement(sql);
    }

    private PreparedStatement getPreparedStatementAdmin(String sql) throws SQLException {
        Connection connection = DatabaseManager.getAdminConnection();
        connection = connection == null ? DatabaseManager.getConnection() : connection;
        return connection.prepareStatement(sql);
    }
    
    private void setValues(PreparedStatement ps, Collection<Object> values) throws SQLException {
        int pos = 1;
        
        try {
            for (Object value : values) {
                if (value instanceof String)
                    ps.setString(pos, (String) value);
                else if (value instanceof Long)
                    ps.setLong(pos, (Long) value);
                else if (value instanceof Double)
                    ps.setDouble(pos, (Double) value);
                else if (value instanceof Integer)
                    ps.setInt(pos, (Integer) value);
                else if (value instanceof Boolean)
                    ps.setBoolean(pos, (Boolean) value);
                else if (value instanceof Date)
                    ps.setDate(pos, new java.sql.Date(((Date) value).getTime()));
                else
                    ps.setNull(pos, Types.NULL);
                
                pos++;
            }
        
        } catch (Exception e) {
            logger.error("Could not set values [" + values + "] for " + ps, e);
        }        
    }
    
    private List<PreparedStatement> getDeleteQueries(DcObject dco) throws SQLException {
        List<PreparedStatement> queries = new ArrayList<PreparedStatement>();

        Loan loan = new Loan();
        Picture picture = new Picture();
        if (dco.hasPrimaryKey())
            queries.add(getPreparedStatement("DELETE FROM " + dco.getTableName() + " WHERE ID = " + dco.getID()));

        if (dco.getModule().canBeLend())
            queries.add(getPreparedStatement("DELETE FROM " + loan.getTableName() + " WHERE " +
                            loan.getField(Loan._D_OBJECTID).getDatabaseFieldName() + " = " + dco.getID()));

        // Delete children. Ignore any abstract module (parent and/or children)
        if (	dco.getModule().getChild() != null && 
        	   !dco.getModule().isAbstract() && 
        	   !dco.getModule().getChild().isAbstract()) {
        	
            DcModule childModule = dco.getModule().getChild(); 
            queries.add(getPreparedStatement("DELETE FROM " + childModule.getTableName() + " WHERE " + 
                        childModule.getField(childModule.getDcObject().getParentReferenceFieldIndex()).getDatabaseFieldName() + " = " + dco.getID()));
        }
        
        // Remove any references to the to be deleted item.
        if (dco.getModule().hasDependingModules()) {
            for (DcModule m : DcModules.getReferencingModules(dco.getModule().getIndex())) {
            	
            	if (m.isAbstract()) continue;
            	
                if (m instanceof MappingModule) {
                    DcObject mapping = m.getDcObject();
                    queries.add(getPreparedStatement(
                            "DELETE FROM " + m.getTableName() + " WHERE " + mapping.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + " = " + dco.getID()));
                } else {
                    for (DcField field : m.getFields()) {
                        if (!field.isUiOnly() && field.getReferenceIdx() == dco.getModule().getIndex()) {
                            queries.add(getPreparedStatement(
                                "UPDATE " + m.getTableName() + " SET " +  field.getDatabaseFieldName() + " = NULL WHERE " + 
                                field.getDatabaseFieldName() + " = " + dco.getID()));
                        }
                    }
                }
            }
        }
        
        requests.add(new ImageRequest(dco.getID(), ImageRequest._DELETE));
        
        queries.add(getPreparedStatement("DELETE FROM " + picture.getTableName() + " WHERE " +
                    picture.getField(Picture._A_OBJECTID).getDatabaseFieldName() + " = " + dco.getID()));

        return queries;
    }

    private Object getQueryValue(DcObject dco, int index) {
        Object value = dco.getValue(index);
        if (Utilities.isEmpty(value))
            value = null;
        else if (value instanceof DcObject)
            value = ((DcObject) value).getID();

        return value;
    }    

    private List<PreparedStatement> getSelectQueries(DcObject dco) throws SQLException {
        List<PreparedStatement> queries = new ArrayList<PreparedStatement>();
        Collection<String> tables = new ArrayList<String>();
        Collection<DcObject> objects = new ArrayList<DcObject>();
        
        if (dco.getModule().isAbstract()) {
            for (DcModule module : DcModules.getModules()) {
                if ((module instanceof DcMediaModule || dco.getModule().getIndex() != DcModules._MEDIA) && 
                    !module.isAbstract() && !module.isChildModule()) {
                    tables.add(module.getTableName());
                }
            }
        } else {
            objects.add(dco);
            objects.addAll(dco.getChildren());
            
            String table = dco.getTableName();
            tables.add(table);
        }

        for (String table: tables) {
            StringBuffer columns = new StringBuffer();
            String query = "";

            if (dco.getModule().isAbstract()) {
                objects.clear();
                DcObject o = DcModules.getObjectForTable(table);
                o.copy(dco, true);
                objects.add(o);
            }

            int counter = 0;
            StringBuffer conditions = new StringBuffer();
            Collection<Object> values = new ArrayList<Object>();
            for (DcObject object : objects) {
                for (DcField field : object.getFields()) {
                    if (!field.isUiOnly() && field.getValueType() != DcRepository.ValueTypes._PICTURE)  {
                        String column = field.getDatabaseFieldName();
                        if (objects.size() > 1 && !Utilities.isEmpty(object.getTableShortName()))
                            column = object.getTableShortName() + "." + column;

                        if (counter == 0) {
                            if (columns.length() > 0)
                                columns.append(", ");

                            // only select fields for the current module, child fields are added to the condition only
                            columns.append(column);
                        }

                        if (object.isChanged(field.getIndex())) {
                            Object value = getQueryValue(object, field.getIndex());

                            String appender = queryType == _SELECTOR || queryType == _SELECTPRECISEOR ? " OR " : " AND ";
                            if (conditions.length() > 0)
                                conditions.append(appender);
                            
                            if (isEmpty(value)) {
                                conditions.append(getEmptyCondition(column, field));
                            } else {
                                String condition;

                            	String s = value instanceof String ? ((String) value).toUpperCase() : "";
                            	s = s.replaceAll("\'", "''");

                                if (queryType == _SELECT || queryType == _SELECTOR) {
                                    if (value instanceof String) {
                                        condition = "UPPER (" + column + ") LIKE '%" + s + "%'";
                                    } else { 
                                        condition = column + " = ?";
                                        values.add(value);
                                    }
                                } else {
                                    if (value instanceof String) {
                                        condition = "UPPER (" + column + ") = " + s;
                                    } else {
                                        condition = column + " = " + s;
                                    }
                                }
                                conditions.append(condition);
                            }
                        }
                    }
                }
            }

            query += "SELECT DISTINCT " + columns + " FROM " + table;

            DcObject objectClone = DcModules.getObjectForTable(table);
            if (objectClone  != null)
                objectClone.copy(dco, true);

            addAvailabilityCondition(objectClone == null ? dco : objectClone, conditions);

            if (conditions.length() > 0)
                query +=  "\r\n WHERE " + conditions;

            if (ordering != null) {
                for (int i = 0; i < ordering.length; i++) {
                    if (i == 0)
                        query += " ORDER BY ";
                    else
                        query += ", ";

                    query += ordering[i];
                }
            } else if (dco.hasPrimaryKey()) {
                query += " ORDER BY " + dco.getField(DcObject._ID).getDatabaseFieldName();
            }

            PreparedStatement ps = getPreparedStatement(query);
            setValues(ps, values);

            queries.add(ps);
        }

        return queries;
    }
    
    private boolean isInteger(DcField field) {
        return  field.getValueType() == DcRepository.ValueTypes._LONG ||
                field.getValueType() == DcRepository.ValueTypes._BIGINTEGER || 
                field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE ||
                field.getValueType() == DcRepository.ValueTypes._DCPARENTREFERENCE;
    }
    
    public void unload() {
        if (requests != null) {
        	for (IRequest request : requests.get())
        		request.end();
            requests.clear();
        }
        
        if (queries != null)
            queries.clear();
        
        requests = null;
        queries = null;
        objectID = null;
        ordering = null;
    }
    
    public int getModule() {
        return module;
    }

    public String[] getOrdering() {
        return ordering;
    }

    /**
     * Indicates that the query is part of a batch and if it is at the end.
     * @param endOfBatch
     */
    public void setBatch(boolean endOfBatch) {
        this.isBatch = true;
        this.endOfBatch = endOfBatch;
    }

    /**
     * Indicates if the query is part of a batch
     */
    public boolean isBatch() {
        return isBatch;
    }

    /**
     * Indicates if the query is at the end of a batch
     */
    public boolean isEndOfBatch() {
        return isBatch() && endOfBatch;
    }

    public String getObjectID() {
        return objectID;
    }

    /**
     * The query type.
     */
    public int getType() {
        return queryType;
    }

    /**
     * Indicates if information should be communicated to the user.
     */
    public boolean getSilence() {
        return bSilence;
    }

    /**
     * Indicates if information should be communicated to the user.
     */
    public void setSilence(boolean silence) {
        this.bSilence = silence;
    }

    /**
     * Gets the requests waiting to be executed.
     */
    public Requests getRequests() {
        return requests;
    }
    
    private String getEmptyCondition(String column, DcField field) {
        String emptyCondition;
        if (isInteger(field)) 
            emptyCondition = "(" + column + " = 0 OR " + column + " IS NULL) ";
        else
            emptyCondition = "(" + column + " IS NULL OR " + column + " = '') ";

        return emptyCondition;
    } 
    
    private void addAvailabilityCondition(DcObject dco, StringBuffer conditions) {
        if (dco.getModule().canBeLend()) {
            ContactPerson loanedBy = (ContactPerson) dco.getValue(DcObject._SYS_LENDBY);
            Integer duration = (Integer) dco.getValue(DcObject._SYS_LOANDURATION);
            String s = (String) dco.getValue(DcObject._SYS_AVAILABLE);

            if (s != null || loanedBy != null || duration != null) {
                boolean available = s == null || duration != null ? false : Boolean.valueOf(s);
                loanedBy = available ? null : loanedBy;

                if (conditions.length() > 0)
                    conditions.append(" AND");

                boolean hasChildren = dco.getChildren().size() > 0;
                String column = hasChildren ? " " + dco.getTableShortName() + ".ID" : " ID";
                String tablename = hasChildren ? dco.getTableShortName() : dco.getTableName();

                String current = formatter.format(new Date());
                String daysCondition = duration != null ? " AND DATEDIFF('dd', startDate , '" + current + "') >= " + duration.intValue() : "";
                String personCondition = loanedBy != null ? " AND PersonID = " + loanedBy.getID() : "";

                if (available)
                    conditions.append(column + " NOT in (select objectID from Loans where objectID = " +
                                      tablename + ".ID AND enddate IS NULL AND startDate <= '" + current +  "')");
                else
                    conditions.append(column + " in (select objectID from Loans where objectID = " +
                                      tablename + ".ID " + daysCondition + " AND enddate IS NULL AND startDate <= '" + current +  "'" +
                                      personCondition + ")");
            }
        }
    }   
    
    @Override
    public String toString() {
        String sql = "";
        
        for (PreparedStatement ps : queries) {
            sql += (sql.length() > 0 ? "\r\n" : "");
            sql += ps.toString();
        }
        return sql;
    }
}
