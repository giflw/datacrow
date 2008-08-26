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

package net.datacrow.core.objects;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.db.Query;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.template.Templates;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.WorkFlow;
import net.datacrow.core.wf.requests.IRequest;
import net.datacrow.core.wf.requests.Requests;
import net.datacrow.core.wf.requests.SynchronizeWithManagerRequest;
import net.datacrow.enhancers.IValueEnhancer;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.DcFieldDefinitions;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Hash;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class DcObject implements Comparable<DcObject>, Serializable {
    
    private static final long serialVersionUID = -6969856564828155152L;

    private static Logger logger = Logger.getLogger(DcObject.class.getName());

    private final int module;
    
    private Map<Integer, DcValue> values = new HashMap<Integer, DcValue>();
    private Requests requests = new Requests();
    
    protected Collection<DcObject> children = new ArrayList<DcObject>();

    private boolean validate = true;
    private boolean silent = false;
    private boolean partOfBatch = false;
    private boolean endOfBatch = false;
    
    //indicates if this item should be managed by the DataManager
    protected boolean synchronizeWithDM = true;
    
    public static final int _ID = 0;
    public static final int _SYS_MODULE = 201;
    public static final int _SYS_AVAILABLE = 202;
    public static final int _SYS_LOANEDBY = 203;
    public static final int _SYS_LOANDURATION = 204;
    public static final int _SYS_CREATED = 205;
    public static final int _SYS_MODIFIED = 206;
    public static final int _SYS_SERVICE = 207;
    public static final int _SYS_SERVICEURL = 208;
    
    public static final int _SYS_FILEHASH = 209;
    public static final int _SYS_FILESIZE = 210;
    public static final int _SYS_FILENAME = 211;
    public static final int _SYS_FILEHASHTYPE = 212;

    public static final int _SYS_CONTAINER = 213;
    public static final int _SYS_DISPLAYVALUE = 214;
    
    public DcObject(int module) {
        this.module = module;

        // initialize the values map
        int[] fields = getModule().getFieldIndices();
        for (int i = 0; i < fields.length; i++) {
            values.put(fields[i], new DcValue());
        }

        // initialize system values
        values.put(DcObject._SYS_MODULE, new DcValue());
        setValue(DcObject._SYS_MODULE, getModule());

        // apply settings (such field visibility)
        applySettings(DcModules.get(module).getFieldDefinitions());
        
        markAsUnchanged();
    }    
    
    /**
     * Educated guess..
     */
    public int getDisplayFieldIdx() {
        for (DcFieldDefinition definition :  DcModules.get(module).getFieldDefinitions().getDefinitions()) {
            if (definition.isDescriptive())
                return definition.getIndex();
        }

        return getModule().getDefaultSortFieldIdx();
    }
    
    public int getDefaultSortFieldIdx() {
        return DcObject._ID;
    } 
    
    /**
     * Returns the name of this object based on the field settings. If the field settings do
     * no specify any descriptive fields the default name field index is used as defined in the
     * module definition.
     */
    public String getName() {
        String name = "";
        for (DcFieldDefinition definition :  DcModules.get(module).getFieldDefinitions().getDefinitions()) {
            if (definition.isDescriptive()) {
                int idx = definition.getIndex();
                String disp = getDisplayString(idx);
                if (disp.length() > 0)
                    name += (name.length() > 0 ? ", " + disp : disp);
            }
        }
        
        if (name.length() == 0)
            name = getDisplayString(getModule().getNameFieldIdx());    
        
        return name;
    }
    
    /**
     * Is this object capable of storing IDs? In most cases the answer should be yes.
     */
    public boolean hasPrimaryKey() {
        return getField(_ID) != null;
    }
    
    /**
     * Clears the requests.
     */
    public void removeRequests() {
        if (requests != null)
            requests.clear();
    }

    public Map<Integer, DcValue> getValues() {
        return values;
    }
    
    /**
     * Set the value using the database field name as key.
     * @param column
     * @param value
     */
    public void setValueForColumn(String column, Object value) {
        for (DcField field : getFields()) {
            if (field.getDatabaseFieldName().equalsIgnoreCase(column)) {
                setValue(field.getIndex(), value);
                break;
            }
        }
    }
    
    /**
     * Set the value using the system name of the field.
     * @param sysName
     * @param value
     */
    public void setValueForName(String sysName, Object value) {
        for (DcField field : getFields()) {
            if (field.getSystemName().equalsIgnoreCase(sysName)) {
                setValue(field.getIndex(), value);
                break;
            }
        }
    }    

    /**
     * Indicate whether this object should be synchronized with the Data Manager.
     * The default value is true. Should only be used for testing purposes or non standard
     * operations.
     * @param b
     */
    public void setSynchronizeWithDM(boolean b) {
    	synchronizeWithDM = b;
    }
    
    /**
     * Retrieves all the requests. These requests will be executed after a save or a delete.
     * @return
     */
    public Requests getRequests() {
    	return requests;
    }

    public String getTableName() {
    	return getModule().getTableName();
    }

    public String getTableShortName() {
        return getModule().getTableShortName();
    }

    public String getTableJoin() {
        return getModule().getTableJoin();
    }

    /**
     * Retrieves the database column count. This count will differ from the field count as
     * some fields are calculated and not stored in the database.
     * @return
     */
    public int getDatabaseFieldCount() {
        int count = 0;
        for (DcField field : getFields())
            count += field.isUiOnly() ? 0 : 1;

        return count;
    }

    public String getLabel(int index) {
        return getField(index).getLabel();
    }

    /**
     * Loads the actual image / picture information. Changes are overwritten. Useful 
     * when reloading an object.
     */
    public void initializeImages() {
        for (Picture picture : DataManager.getPictures(getID())) {
            picture.setValue(Picture._D_IMAGE, null);
            setValueForColumn((String) picture.getValue(Picture._B_FIELD), picture);
        }
    }
    
    /**
     * Loads the actual reference information. Uses the Data Manager to retrieve the 
     * references and stores them in this object.
     */
    public void initializeReferences() {
        int[] fields = getFieldIndices();
        for (int i = 0; i < fields.length; i++) {
            DcField field = getField(fields[i]);
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                int mappingModIdx = DcModules.getMappingModIdx(getModule().getIndex(), field.getReferenceIdx());
                Collection<DcMapping> mo = DataManager.getReferences(mappingModIdx, getID());
                setValue(fields[i], mo);
            }
        }
        
        for (int i = 0; i < fields.length; i++) {
            DcField field = getField(fields[i]);
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE &&
                getValue(field.getIndex()) != null) {
                Object o = getValue(field.getIndex());
                if (o instanceof String)
                    setValue(fields[i], DataManager.getObject(field.getReferenceIdx(), (String) o));    
            }
        }
    }
    
    /**
     * Add a request. This request will be executed after saving or deleting the object.
     * @param request
     */
    public void addRequest(IRequest request) {
		requests.add(request);
    }

    /**
     * Indicates this item is the last in line of a batch operation (save/update/delete).
     * @param b
     */
    public void setEndOfBatch(boolean b) {
    	endOfBatch = b;
    }

    /**
     * Indicates if this item is used in a batch process (save/update/delete).
     * @param b
     */
    public void setPartOfBatch(boolean b) {
    	partOfBatch = b;
    }

    /**
     * Indicates if this item is used in a batch process (save/update/delete).
     */
    public boolean isPartOfBatch() {
        return partOfBatch;
    }

    /**
     * Indicates this item is the last in line of a batch operation (save/update/delete).
     */
    public boolean isEndOfBatch() {
    	return endOfBatch;
    }

    /**
     * Does this field contains a value?
     * @param index
     */
    public boolean isFilled(int index) {
        return !Utilities.isEmpty(getValue(index));
    }

    /**
     * Remove all children from this object
     */
    public void removeChildren() {
        if (children != null)
            children.clear();
    }

    public boolean isNew() {
        return getID() == null || DataManager.getObject(getModule().getIndex(), getID()) == null;
    }
    
    /**
     * Load all children. Children will only be loaded when no child information
     * is present yet. Will not overwrite existing values.
     */
    public void loadChildren() {
        if (getModule().getChild() == null || isNew()) 
            return;
        
        children.clear();
        int childIdx = getModule().getChild().getIndex();
        for (DcObject dco : DataManager.getChildren(getID(), childIdx)) {
            children.add(dco);
        }
    }  

    public void setChildren(Collection<DcObject> children) {
        if (this.children == null) {
            this.children = new ArrayList<DcObject>(children);
        } else {
            this.children.clear();
            this.children.addAll(children);
        }
        
        for (DcObject child : children)
            child.setValue(child.getParentReferenceFieldIndex(), getID());
    }

    public void addChild(DcObject child) {
        child.setPartOfBatch(isPartOfBatch());
        String parentID = child.getDisplayString(child.getParentReferenceFieldIndex());
        if (!getID().equals(parentID))
            child.setValue(child.getParentReferenceFieldIndex(), getID());

        this.children = children == null ? new ArrayList<DcObject>() : children;
        
        if (endOfBatch) {
        	setEndOfBatch(false);
        	for (DcObject dco : getChildren())
        	    dco.setEndOfBatch(false);

        	child.setEndOfBatch(true);
        }

        children.add(child);
    }

    public Collection<DcObject> getChildren() {
        loadChildren();
        return children != null ? new ArrayList<DcObject>(children) : null;
    }
    
    public String getParentID() {
        Object o = getValue(getParentReferenceFieldIndex());
        
        if (o instanceof DcObject)
            return ((DcObject) o).getID();
        else 
            return (String) o;
    }

    public int getParentReferenceFieldIndex() {
        return getModule().getParentReferenceFieldIndex();
    }

    /**
     * The icon used to represent this item.
     */
    public ImageIcon getIcon() {
        return null;
    }

    public DcField getFileField() {
        if (getModule().isFileBacked()) {
            return getField(DcObject._SYS_FILENAME);
        } else {
            for (DcField field : getFields()) {
                if (field.getFieldType() == ComponentFactory._FILELAUNCHFIELD)
                    return field;
            }
            return null;
        }
    }
    
    public String getFilename() {
        DcField field = getFileField();
        return field != null ? (String) getValue(field.getIndex()) : null;
    }

    public DcModule getModule() {
        return DcModules.get(module);
    }

    public Collection<DcField> getFields() {
        return getModule().getFields();
    }

    public void markAsUnchanged() {
        for (DcValue value : values.values())
            value.setChanged(false);
        
        // 22032008: Removed "markAsUnchanged" on child objects!
        // this broke saving the permissions (and possibly other item save's).
    }
    
    public void reload() {
        unloadImages();
        
        String query = "SELECT * FROM " + getTableName() + " WHERE ID = " + getID();
        Collection<DcObject> objects = DatabaseManager.executeQuery(query, Query._SELECT, false);
        for (DcObject dco : objects) {
            int[] fields = getModule().getFieldIndices();
            for (int i = 0; i < fields.length; i++) {
                //if (!dco.getField(fields[i]).isUiOnly())
                    setValue(fields[i], dco.getValue(fields[i]));
            }
            break;
        }
        
        for (DcObject dco : objects) 
            dco.unload();
        
        if (getModule().canBeLended()) {
            Loan loan = DataManager.getCurrentLoan(getID());
            setValue(DcObject._SYS_AVAILABLE, loan.isAvailable(getID()));
            setValue(DcObject._SYS_LOANEDBY, loan.getPersonDescription());
            setValue(DcObject._SYS_LOANDURATION, loan.getDaysLoaned());
        }
        
        initializeImages();
        markAsUnchanged();
    }
    
    protected void beforeSave() {
        if (getModule().isFileBacked())
            Hash.getInstance().calculateHash(this);
    }
    
    /**
     * Frees the resources hold by this items pictures.
     */
    public void freeResources() {
        for (DcField field : getFields()) {
            if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                Picture picture = (Picture) getValue(field.getIndex());
                if (picture != null)
                    picture.unload();
            }
        }
    }
    
    /**
     * Resets the pictures. All pictures are unloaded and set to null. 
     */
    private void unloadImages() {
        for (DcField field : getFields()) {
            if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                Picture picture = (Picture) getValue(field.getIndex());
                if (picture != null)
                    picture.unload();
                
                setValueLowLevel(field.getIndex(), null);
            }
        }
    }
    
    public void setValue(int index, Object o) {
        DcValue value = getValueDef(index);
        if (value != null)
            value.setValue(o, getModule().getField(index));
    }    
    
    /**
     * Applies the value directly on this item. All checks are bypasses.
     */
    public void setValueLowLevel(int index, Object o) {
        DcValue value = getValueDef(index);
        value.setValueLowLevel(o, getModule().getField(index));
    }

    public void markAsChanged() {
        for (DcValue value : values.values())
            value.setChanged(true);

        if (children != null)
            for (DcObject child : children) child.markAsChanged();
    }

    public boolean isChanged() {
        for (DcField field : getFields()) {
            boolean changed = getValueDef(field.getIndex()).isChanged();
            if ((!field.isUiOnly() || field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) && changed)
                return true;
        }
        return false;
    }

    public boolean isChanged(int index) {
        return getValueDef(index).isChanged();
    }

    public void setChanged(int index, boolean b) {
    	getValueDef(index).setChanged(b);
    }

    public boolean isEnabled(int index) {
        return getModule().getField(index).isEnabled();
    }

    public boolean isRequired(int index) {
        return getModule().getField(index).isRequired();
    }

    public boolean isSearchable(int index) {
        return getModule().getField(index).isSearchable();
    }

    public String getID() {
        return hasPrimaryKey() ? getValueDef(_ID).getValueAsString() : null;
    }

    public Collection<DcField> getNotSearchableFields() {
        Collection<DcField>  notSearchable = new ArrayList<DcField>();
        for (DcField field : getFields()) {
            if (!field.isSearchable())
                notSearchable.add(field);
        }
        return notSearchable;
    }

    /**
     * Unloads this items. Its resources are freed and its pictures are unloaded.
     * The item is unusable after this operation (!).
     */
    public void unload() {
    	
    	freeResources();
    	
        for (DcField field : getFields()) {
            if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                Picture picture = (Picture) getValue(field.getIndex());
                if (picture != null) picture.unload(); 
                setValueLowLevel(field.getIndex(), null);
            }
        }
        
        clearValues();
        
        values.clear();
        values = null;
        
        requests.clear();
        requests = null;
        
        if (children != null)
            children.clear();
        
        children = null;
    }
    
    /**
     * Resets this item. All values are set to empty.
     */
    public void clearValues() {
        for (Integer key : values.keySet()) {
            if (key.intValue() != _ID) {
                DcValue value = values.get(key);
                value.clear();
            }
        }
    }

    public int getMaxFieldLength(int index) {
        return getField(index).getMaximumLength();
    }

    public Object getValue(int index) {
        Object value = null;
        if (getValueDef(index) != null) {
            if (index == _SYS_DISPLAYVALUE)
                value = toString();
            else
                value = getValueDef(index).getValue();
        }
        return value;
    }

    public String getDisplayString(int index) {
        if (index == _SYS_DISPLAYVALUE)
            return toString();
        
        return  getValueDef(index) != null ? getValueDef(index).getDisplayString() : "";
    }

    public int getFieldType(int index) {
        return getField(index).getFieldType();
    }

    public String getDatabaseFieldName(int index) {
        return getField(index).getDatabaseFieldName();
    }

    public void applyEnhancers(boolean update) {
        for (DcField field : getFields()) {
            Object value = getValue(field.getIndex());
            for (IValueEnhancer enhancer : field.getValueEnhancers()) {
                if (enhancer.isEnabled() && 
                    (update && enhancer.isRunOnUpdating() || !update && enhancer.isRunOnInsert())) {
                    
                    Object newVal = enhancer.apply(field, value);
                    Object oldVal = getValue(field.getIndex());
                    
                    if (newVal != null && (oldVal == null || !newVal.equals(oldVal)))
                        setValue(field.getIndex(), newVal);
                }
            }
        }
    }    
    
    private String getCurrentDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(new Date());
    }
    
    public void saveNew(boolean queued) throws ValidationException {
        try {
            markAsChanged();
            applyEnhancers(false);
            checkIntegrity(false);
            
            beforeSave();
            setValue(_SYS_CREATED, getCurrentDate());
            
            if (queued) {
            	if (synchronizeWithDM)
            		addRequest(new SynchronizeWithManagerRequest(SynchronizeWithManagerRequest._ADD, this));
            	
            	WorkFlow.insertValues(this);
            } else {
                Query query = new Query(Query._INSERT, this, null, null);
            	DatabaseManager.executeQuery(query, false);

            	if (synchronizeWithDM)
            		new SynchronizeWithManagerRequest(SynchronizeWithManagerRequest._ADD, this).execute(null);
            }
        } catch (ValidationException ve) {
            executeRequests(false);
            throw ve;
        } catch (Exception e) {
            logger.error("An error (" + e + ") occurred while inserting " + this, e);
        }
    }    
    
    public void saveUpdate(boolean queued) throws ValidationException {
        saveUpdate(queued, true);
    }
    
    public void saveUpdate(boolean queued, boolean validate) throws ValidationException {
        try {
            applyEnhancers(true);
            checkIntegrity(validate);

            beforeSave();
            setValue(_SYS_MODIFIED, getCurrentDate());
            
            if (queued) {
            	if (synchronizeWithDM)
            		addRequest(new SynchronizeWithManagerRequest(SynchronizeWithManagerRequest._UPDATE, this));
            	
            	WorkFlow.updateValues(this);
            } else {
                Query query = new Query(Query._UPDATE, this, null, null);
                DatabaseManager.executeQuery(query, false);
            	if (synchronizeWithDM)
            		new SynchronizeWithManagerRequest(SynchronizeWithManagerRequest._UPDATE, this).execute(null);
            }
        } catch (ValidationException exp) {
            executeRequests(false);
            throw exp;
        } catch (Exception e) {
            logger.error("An error (" + e + ") occurred while updating " + this, e);
        }
    }

    public void delete() {
    	if (synchronizeWithDM)
    		addRequest(new SynchronizeWithManagerRequest(SynchronizeWithManagerRequest._DELETE, this));
    	
        WorkFlow.deleteValues(this);
    }

    public void setSilent(boolean b) {
        this.silent = b;
        
        if (getChildren() != null) {
            for (DcObject child : getChildren())
                child.setSilent(silent);
        }
    }

    /**
     * Allows messages to be displayed on screen after the execution of an operation. 
     */
    public boolean isSilent() {
        return this.silent;
    }

    /**
     * Indicates if validation should take place when the item is saved.
     */
    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public DcField getField(int index) {
        return getModule().getField(index);
    }

    public void checkIntegrity(boolean update) throws ValidationException {
        if (DcSettings.getBoolean(DcRepository.Settings.stCheckRequiredFields))
            validateRequiredFields(update);

        if (DcSettings.getBoolean(DcRepository.Settings.stCheckUniqueness))
            isUnique(this, update);
    }

    public void isUnique(DcObject o, boolean bUpdateQuery) throws ValidationException {
        boolean bUnique = WorkFlow.checkUniqueness(o, bUpdateQuery);
        if (!bUnique && validate)
        	throw new ValidationException(DcResources.getText("msgItemNotUnique", toString()));
    }

    public int[] getFieldIndices() {
        return getModule().getFieldIndices();
    }

    public void applySettings(DcFieldDefinitions definitions) {
        for (DcFieldDefinition definition :  definitions.getDefinitions()) {
            DcField field = getField(definition.getIndex());

            field.setRequired(definition.isRequired());
            field.setEnabled(definition.isEnabled());

            String label = definition.getLabel();
            if (label != null && label.trim().length() > 0)
                field.setLabel(label);
            else
                field.setLabel(field.getSystemName());
        }
    }

    public void setIDs() {
        if (hasPrimaryKey()) {
            String id = Utilities.getUniqueID();
            
            while (id == null || id.length() < 15)
                id = Utilities.getUniqueID();
            
            id = id.substring(0, 15);
            setValue(DcObject._ID, id);
            
            if (children != null) {
                for (DcObject child : children) {
                    String parentID = child.getDisplayString(child.getParentReferenceFieldIndex());
                    if (!id.equals(parentID)) {
                        child.setIDs();
                        child.setValue(child.getParentReferenceFieldIndex(), id);
                    }
                }
            }
        }
    }

    protected DcValue getValueDef(int index) {
        return values.get(index);
    }

    protected void executeRequests(boolean saveSuccessful) {
        if (requests != null) {
            IRequest[] requestArray = requests.get();
            for (int i = 0; i < requestArray.length; i++) {
                IRequest request = requestArray[i];
                if (saveSuccessful || request.getExecuteOnFail())
                    request.execute(new ArrayList<DcObject>());
                else 
                    request.end();
            }
        }
    }
    
    protected void validateRequiredFields(boolean update) throws ValidationException {
        String s = "";
        for (DcField field : getFields()) {
            if (field.isRequired()) {
                Object value = getValue(field.getIndex());
                if (value == null || value.toString().equals("")) {
                    if (s.length() > 0) s += ",";
                    s += getLabel(field.getIndex());
                }
            }
        }
        
        if (s.trim().length() > 1)
            throw new ValidationException(DcResources.getText("msgMissingRequiredValues", 
                                          new String[] {s, toString()}));
    }

    /**
     * Copies all values from the specified Data Crow object
     * @param dco source object
     */
    public void copy(DcObject dco, boolean overwrite) {
        int[] fields = dco.getFieldIndices();
        for (int i = 0; i < fields.length; i++) {
            int field = dco.getFieldIndices()[i];

            if (!overwrite && !Utilities.isEmpty(dco.getValue(field)))
                continue;
            
            if (field != _ID && dco.getValue(field) != null) {
            	Object o = dco.getValue(field);
            	if (o instanceof DcImageIcon) {
            		DcImageIcon oldIcon = (DcImageIcon) o;
            		
            		DcImageIcon icon;
            		if (oldIcon.getCurrentBytes() != null)
            			icon = new DcImageIcon(oldIcon.getCurrentBytes());
            		else
            			icon = new DcImageIcon(oldIcon.getImage());
            		
            		icon.setFilename(oldIcon.getFilename());
            	
            		setValue(field, icon);
            	} else if (getField(field).getValueType() == DcRepository.ValueTypes._PICTURE) {
                    Picture curPic = (Picture) dco.getValue(field);
                    
                    Picture newPic = new Picture();
                    newPic.copy(curPic, overwrite);
                    
                    newPic.isNew(curPic.isNew());
                    newPic.isDeleted(curPic.isDeleted());
                    newPic.isUpdated(curPic.isUpdated());
                    
                    setValue(field, newPic);
                } else {
                    setValue(field, o);    
                }
            }
        }
    }

    public void applyTemplate() {
        if (getModule().getTemplateModule() != null) {
            DcTemplate template = Templates.getDefault(getModule().getTemplateModule().getIndex());
            if (template != null)
                applyTemplate(template);
        }
    }
    
    public void applyTemplate(DcTemplate template) {
        if (template == null) {
            applyTemplate();
            return;
        } 
        
        int[] fields = getFieldIndices();
        for (int i = 0; i < fields.length; i++) {
            int idx = fields[i];

            DcField field = getField(idx);
            Object templateVal = template.getValue(idx); 
            
            if (    idx != _ID && 
                    field.getValueType() != DcRepository.ValueTypes._PICTURE && 
                    templateVal != null) {
                
                setValue(idx, template.getValue(idx));    
            } else if ( field.getValueType() == DcRepository.ValueTypes._PICTURE && 
                        template.getValue(idx) != null) {
                
                Picture templatePic = (Picture) template.getValue(idx);
                
                Picture pic = new Picture();
                templatePic.loadImage();
                pic.setValue(Picture._D_IMAGE, templatePic.getValue(Picture._D_IMAGE));
                pic.setValue(Picture._E_HEIGHT, templatePic.getValue(Picture._E_HEIGHT));
                pic.setValue(Picture._F_WIDTH, templatePic.getValue(Picture._F_WIDTH));
                pic.isNew(true);
                
                setValue(idx, pic);                
            }
        }
    }    
    
    /**
     * Copy an existing picture and set it on this item. This is the safest way to copy
     * an picture of another item to this item. 
     */
    public void copyImage(Picture picture, int field) {
        if (picture != null) {
            DcImageIcon icon = (DcImageIcon) picture.getValue(Picture._D_IMAGE);
            if (icon != null && icon.getCurrentBytes() != null)
                setValue(field, new DcImageIcon(icon.getCurrentBytes()));
        }
    }
    
    /**
     * Clones this objects. All values are copies as well as its children.
     * The clone operates on copies of the pictures and the children and can be used entirely
     * in parallel with its clone(s).
     */
    @Override
    public DcObject clone() {
        DcObject dco = getModule().getDcObject();
        
        dco.copy(this, true);
        dco.setValue(DcObject._ID, getID());
        dco.markAsUnchanged();
        
        if (children != null) {
            for (DcObject child : children) {
                DcObject childCopy = child.getModule().getDcObject();
                childCopy.copy(child, true);
                dco.addChild(child);
            }
        }
        
        int[] indices = getFieldIndices();
        for (int i = 0; i < indices.length; i++) {
            dco.setChanged(indices[i], isChanged(indices[i]));
        }
        
        return dco;
    }
    
    @Override
    public int hashCode() {
        return getID() != null ? getID().hashCode() : super.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
    
    @Override
    protected void finalize() throws Throwable {
        unload();
        super.finalize();
    }

    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        
        if (o instanceof String) {
            equals = getID() != null ? getID().equals(o) : false;
        } else if (o instanceof DcObject) {
            String id1 = ((DcObject) o).getDisplayString(DcObject._ID);
            String id2 = getDisplayString(DcObject._ID);
            if (id1.length() == 0 && id2.length() == 0) 
                return o == this;
            else 
                return id1.equals(id2);
        } else {
            equals = super.equals(o);
        }
        
        return equals;
    }
    
    public int compareTo(DcObject o) {
        return toString().compareTo(o.toString());
    }
}
