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

import java.awt.image.BufferedImage;
import java.io.Serializable;
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
import net.datacrow.core.db.QueryQueue;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.helpers.ExternalReference;
import net.datacrow.core.objects.helpers.Movie;
import net.datacrow.core.objects.helpers.Software;
import net.datacrow.core.objects.template.Templates;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.WorkFlow;
import net.datacrow.core.wf.requests.IRequest;
import net.datacrow.core.wf.requests.Requests;
import net.datacrow.core.wf.requests.SynchronizeWithManagerRequest;
import net.datacrow.enhancers.IValueEnhancer;
import net.datacrow.enhancers.ValueEnhancers;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.DcFieldDefinitions;
import net.datacrow.util.Base64;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Hash;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * This class is what it is all about. Each DcObject represents an item 
 * within Data Crow. DcObjects are very generic by nature. There are no direct
 * getters and setters for their values. Instead the values are stored in a Map object
 * and the values are retrieved by using the field indices. 
 * <br>
 * It's recommended before starting new development of plugins to create so called
 * helper classes for your new module. Examples of helper classes are {@link Software}
 * and {@link Movie}.
 * <br>
 * DcObjects are managed and maintained by the Data Manager class ({@link DataManager}.
 * Each DcObject belongs to a (@link {@link DcModule}).
 * 
 * @author Robert Jan van der Waals
 */
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
    public static final int _SYS_LENDBY = 203;
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
    
    public static final int _SYS_LOANDUEDATE = 215;
    public static final int _SYS_LOANDAYSTILLOVERDUE = 216;
    
    public static final int _VALUE = 217;
    
    public static final int _SYS_EXTERNAL_REFERENCES = 218;
    
    /**
     * Creates a new instance.
     * @param module
     */
    public DcObject(int module) {
        this.module = module;

        // initialize the values map
        int[] fields = getModule().getFieldIndices();
        for (int i = 0; i < fields.length; i++) {
            values.put(fields[i], new DcValue());
        }
        markAsUnchanged();
    }    
    
    public int getSystemDisplayFieldIdx() {
        return getModule().getSystemDisplayFieldIdx();
    }

    public boolean isDestroyed() {
        return values == null;
    }
    
    /**
     * Educated guess..
     */
    public int getDisplayFieldIdx() {
        return getModule().getDisplayFieldIdx();
    }
    
    /**
     * The default sort field index. In case the user has not specified the field to sort on 
     * this value will be used. 
     */
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

    /**
     * Retrieves the value objects.
     */
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
    
    @SuppressWarnings("unchecked")
    public String getExternalReference(String type) {
        Collection<DcObject> references = (Collection<DcObject>) getValue(_SYS_EXTERNAL_REFERENCES);
        references = references == null ? new ArrayList<DcObject>() : references;
        for (DcObject mapping : references) {
            DcObject reference = ((DcMapping) mapping).getReferencedObject();

            if (    reference != null &&
                    reference.getValue(ExternalReference._EXTERNAL_ID_TYPE) != null &&
                    reference.getValue(ExternalReference._EXTERNAL_ID_TYPE).equals(type)) {
                
                return (String) reference.getValue(ExternalReference._EXTERNAL_ID);
            }
        }
        return null;
    }
    
    /**
     * Adds or updates the existing external key of the specified type.
     * @param type The type of the key
     * @param key The external key / ID
     */
    @SuppressWarnings("unchecked")
    public void addExternalReference(String type, String key) {
        Collection<DcObject> references = (Collection<DcObject>) getValue(_SYS_EXTERNAL_REFERENCES);
        references = references == null ? new ArrayList<DcObject>() : references;
            
        boolean set = false;
        for (DcObject mapping : references) {
            DcObject reference = ((DcMapping) mapping).getReferencedObject();
            if (reference.getDisplayString(ExternalReference._EXTERNAL_ID_TYPE).equals(type)) {
                reference.setValue(ExternalReference._EXTERNAL_ID, key);
                set = true;
            }
        }
        
        if (!set && !Utilities.isEmpty(type)) {
            DcObject er = DcModules.get(module + DcModules._EXTERNALREFERENCE).getItem();
            er.setValue(ExternalReference._EXTERNAL_ID, key);
            er.setValue(ExternalReference._EXTERNAL_ID_TYPE, type);
            er.setIDs();
            DataManager.createReference(this, DcObject._SYS_EXTERNAL_REFERENCES, er);
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

    /**
     * The database table name.
     */
    public String getTableName() {
    	return getModule().getTableName();
    }

    public String getTableShortName() {
        return getModule().getTableShortName();
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
                int mappingModIdx = DcModules.getMappingModIdx(getModule().getIndex(), field.getReferenceIdx(), field.getIndex());
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
        return !DataManager.exists(this);
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

    /**
     * Retrieves the child objects belonging to this item.
     * @return The children or null of none.
     */
    public Collection<DcObject> getChildren() {
        loadChildren();
        return getCurrentChildren();
    }
    
    /**
	 * Gets the children as they have been currently set (without reloading them).
	 */
    public Collection<DcObject> getCurrentChildren() {
        return children != null ? new ArrayList<DcObject>(children) : new ArrayList<DcObject>();
    }
    
    /**
     * Retrieves the ID of the parent of this object. 
     * @return The parent ID or null.
     */
    public String getParentID() {
        Object o = getValue(getParentReferenceFieldIndex());
        
        if (o instanceof DcObject)
            return ((DcObject) o).getID();
        else 
            return (String) o;
    }

    /**
     * Retrieves the index of the field which is used to hold the link to the parent. 
     */
    public int getParentReferenceFieldIndex() {
        return getModule().getParentReferenceFieldIndex();
    }

    /**
     * The icon used to represent this item.
     */
    public ImageIcon getIcon() {
        for (DcField field : getFields()) {
            if (field.getValueType() == DcRepository.ValueTypes._ICON) {
                String value = (String) getValue(field.getIndex());
                ImageIcon icon = null;
                if (value != null && value.length() > 1) 
                    icon = Utilities.base64ToImage(value);
                return icon;
            }
        }
        return null;
    }

    public DcField getFileField() {
        return getModule().getFileField();
    }

    /**
     * Retrieves the filename value. This will only generate a result if the object
     * has a file field.
     * @return The file name or null.
     */
    public String getFilename() {
        DcField field = getFileField();
        return field != null ? Utilities.getMappedFilename((String) getValue(field.getIndex())) : null; 
    }

    /**
     * Retrieves the module to which this object belongs.
     */
    public DcModule getModule() {
        return DcModules.get(module);
    }

    /**
     * Retrieves all fields belonging to this object .
     */
    public Collection<DcField> getFields() {
        return getModule().getFields();
    }

    /**
     * Mark all fields as unchanged. This does not reset the values to their
     * original values! (use {@link #reload()})
     */
    public void markAsUnchanged() {
        for (DcValue value : values.values())
            value.setChanged(false);
        
        // 22032008: Removed "markAsUnchanged" on child objects!
        // this broke saving the permissions (and possibly other item save's).
    }
    
    /**
     * Reloads the item directly from the database (!).
     */
    public void reload() {
        // do not unload: dangerous because of the newly added destroy or release method 
        // unloadImages();
        
        String query = "SELECT * FROM " + getTableName() + " WHERE ID = " + getID();
        Collection<DcObject> objects = DatabaseManager.executeQuery(query, Query._SELECT);
        for (DcObject dco : objects) {
            int[] fields = getModule().getFieldIndices();
            for (int i = 0; i < fields.length; i++)
                setValue(fields[i], dco.getValue(fields[i]));

            break;
        }
        
        for (DcObject dco : objects) 
            dco.release();
        
        setLoanInformation();
        
        initializeImages();
        markAsUnchanged();
    }
    
    /**
     * Update the loan information.
     */
    public void setLoanInformation() {
        if (getModule().canBeLend()) {
            Loan loan = DataManager.getCurrentLoan(getID());
            setLoanInformation(loan);
        }
    }
    
    /**
     * Update the loan information based on the supplied loan object.
     */
    public void setLoanInformation(Loan loan) {
        if (getModule().canBeLend()) {
            setValue(DcObject._SYS_AVAILABLE, loan.isAvailable(getID()));
            setValue(DcObject._SYS_LENDBY, loan.getPerson());
            setValue(DcObject._SYS_LOANDUEDATE, loan.getDueDate());
            setValue(DcObject._SYS_LOANDURATION, loan.getDaysLoaned());
            setValue(DcObject._SYS_LOANDAYSTILLOVERDUE, loan.getDaysTillOverdue());
        }
    }

    /**
     * Actions to be performed before saving the object.
     */
    protected void beforeSave() throws ValidationException {
        if (getModule().isFileBacked())
            Hash.getInstance().calculateHash(this);
        
        if ( getField(_SYS_EXTERNAL_REFERENCES) != null &&
             getExternalReference(DcRepository.ExternalReferences._PDCR) == null &&
            !Utilities.isEmpty(getDisplayString(getSystemDisplayFieldIdx()))) {

            // Only create this ONCE. It is supposed to remain the same for ever
            addExternalReference(DcRepository.ExternalReferences._PDCR, 
                                 getDisplayString(getSystemDisplayFieldIdx()));
        }
        
        saveIcon();
    }
    
    private void saveIcon() {
        for (DcField field : getFields()) {
        
            if (field.getValueType() != DcRepository.ValueTypes._ICON) continue;
            if (!isChanged(field.getIndex())) continue;
            
            String value = (String) getValue(field.getIndex());
            
            if (value != null && value.length() > 0) {
                byte[] bytes = Base64.decode(value.toCharArray());
                ImageIcon current = new ImageIcon(bytes);
                
                if (current.getIconHeight() > 16 || current.getIconWidth() > 16) {
                    BufferedImage img = Utilities.toBufferedImage(current, 16, 16);
                    try {
                        bytes = Utilities.getBytes(new DcImageIcon(img), DcImageIcon._TYPE_PNG);
                        setValue(field.getIndex(), new String(Base64.encode(bytes)));
                    } catch (Exception e) {
                        logger.error("Could not save scaled image for object with ID " + getID(), e);
                    }
                }
            }
        }
    }
    
    /**
     * Frees the resources hold by this items pictures.
     */
    public void freeResources() {
        for (DcField field : getFields()) {
            if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                Picture picture = (Picture) getValue(field.getIndex());
                if (picture != null)
                    picture.unload(false);
            }
        }
    }
    
    /**
     * Frees the resources hold by this items pictures.
     */
    public void flushImages() {
        for (DcField field : getFields()) {
            if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                Picture picture = (Picture) getValue(field.getIndex());
                if (picture != null) {
                    DcImageIcon icon = (DcImageIcon) picture.getValue(Picture._D_IMAGE);
                    if (icon != null) icon.flush();
                }
            }
        }
    }    
    
    /**
     * Sets a value on this object.
     * @param index The field index.
     * @param o The value to be set.
     */
    @SuppressWarnings("unchecked")
    public void setValue(int index, Object o) {
        
        if (isDestroyed()) {
            logger.warn("System tried to set a value while the object was already destroyed");
            return;
        }
        
        DcValue value = getValueDef(index);
        if (value != null) {
            if (index == _SYS_EXTERNAL_REFERENCES && getValue(index) != null && o != null ) {
                mergeReferences((Collection<DcMapping>) o);
            } else {
                value.setValue(o, getModule().getField(index));
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void mergeReferences(Collection<DcMapping> mappings) {
        Collection<DcMapping> currentMappings = (Collection<DcMapping>) getValue(DcObject._SYS_EXTERNAL_REFERENCES);
            
        // external references are always merged!
        for (DcMapping mapping : mappings) {
            DcObject reference = mapping.getReferencedObject();
            boolean exists = false;
            
            if (currentMappings != null) {
                for (DcMapping mappingCurrent : currentMappings) {
                    DcObject referenceCurrent = mappingCurrent.getReferencedObject();
                    exists = referenceCurrent.getValue(ExternalReference._EXTERNAL_ID_TYPE).equals(reference.getValue(ExternalReference._EXTERNAL_ID_TYPE));
                    if (exists) {
                        mappingCurrent.setValue(DcMapping._B_REFERENCED_ID, mapping.getReferencedObject().getID());
                        mappingCurrent.setReferencedObject(mapping.getReferencedObject());
                        getValueDef(DcObject._SYS_EXTERNAL_REFERENCES).setChanged(true);
                        break;
                    }
                }
            }
            
            if (!exists) {
                DcMapping newMapping = (DcMapping) DcModules.get(DcModules.getMappingModIdx(
                        getModule().getIndex(), mapping.getReferencedObject().getModule().getIndex(), DcObject._SYS_EXTERNAL_REFERENCES)).getItem();
                newMapping.setValue(DcMapping._A_PARENT_ID, getID());
                newMapping.setValue(DcMapping._B_REFERENCED_ID, mapping.getReferencedObject().getID());
                newMapping.setReferencedObject(mapping.getReferencedObject());
                currentMappings.add(newMapping);
                getValueDef(DcObject._SYS_EXTERNAL_REFERENCES).setChanged(true);
            }
        }
    }

    /**
     * Applies the value directly on this item. All checks are bypasses.
     * @param index The field index.
     * @param o The value to be set.
     */
    public void setValueLowLevel(int index, Object o) {
        DcValue value = getValueDef(index);
        if (value != null)
            value.setValueLowLevel(o, getModule().getField(index));
    }

    /**
     * Marks the object as changed.  
     */
    public void markAsChanged() {
        for (DcValue value : values.values())
            value.setChanged(true);

        if (children != null)
            for (DcObject child : children) child.markAsChanged();
    }

    /**
     * Checks whether the object holds unchanged values.
     * @see DcValue#isChanged()
     */
    public boolean isChanged() {
        
        if (isDestroyed()) {
            logger.warn("System tried to check if a value was changed while the object was already destroyed");
            return false;
        }
        
        try {
            for (DcField field : getFields()) {
                boolean changed = getValueDef(field.getIndex()).isChanged();
                if ((!field.isUiOnly() || field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) && changed)
                    return true;
            }
        } catch (Exception e) {
            logger.debug("Item was probably already destroyed!", e);
        }
        return false;
    }

    /**
     * Checks whether the specified field holds a changed value.
     * @see DcValue#isChanged()
     * @param index The field index
     */
    public boolean isChanged(int index) {
        if (isDestroyed()) {
            logger.warn("System tried to check if the value was changed while the object was already destroyed");
            return false;
        }

        return getValueDef(index).isChanged();
    }

    /**
     * Manually mark a field as changed
     * @see DcValue#isChanged()
     * @param index The field index
     * @param b Changed true / false
     */
    public void setChanged(int index, boolean b) {
        if (isDestroyed()) {
            logger.warn("System tried to mark a field as changed while the object was already destroyed");
        } else {
            getValueDef(index).setChanged(b);
        }
    }

    /**
     * Indicates whether the field is enabled.
     * This depends on the field settings which can be altered by the user.
     * @see DcFieldDefinitions
     * @see DcField#isEnabled()
     * @param index
     */
    public boolean isEnabled(int index) {
        return getModule().getField(index).isEnabled();
    }

    /**
     * Checks whether the field is marked as required.
     * This depends on the field settings which can be altered by the user.
     * @see DcFieldDefinitions
     * @param index The field index
     */
    public boolean isRequired(int index) {
        return getModule().getField(index).isRequired();
    }

    /**
     * Indicates whether the specified field can be searched on.
     * @param index The field index
     */
    public boolean isSearchable(int index) {
        return getModule().getField(index).isSearchable();
    }

    /**
     * The internal ID
     */
    public String getID() {
        return hasPrimaryKey() ? getValueDef(_ID).getValueAsString() : null;
    }

    /**
     * Retrieves all fields on which cannot be searched.
     */
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
    public void release() {
        
        if (isDestroyed()) return;

        clearValues(true);
        setValueLowLevel(DcObject._ID, null);
        
        if (requests != null)
            requests.clear();
        
        if (children != null)
            children.clear();

        getModule().release(this);
    }
    
    public void destroy() {
        clearValues(true);
        values.clear();
        values = null;
        requests.clear();
        requests = null;
        children = null;
    }
    
    
    /**
     * Resets this item. All values are set to empty.
     * @param nochecks Just do it, do not check whether we are dealing with an edited item
     */
    public void clearValues(boolean nochecks) {
        if (isDestroyed()) {
            logger.warn("System tried to clear all values while the object was already destroyed");
        } else {
            for (Integer key : values.keySet()) {
                if (key.intValue() != _ID) {
                    DcValue value = values.get(key);
                    value.clear(nochecks);
                }
            }
        }
    }

    /**
     * Retrieves the maximum field / value length.
     * @param index The field index
     */
    public int getMaxFieldLength(int index) {
        return getField(index).getMaximumLength();
    }

    /**
     * Retrieves the value for the specified field.
     * @param index The field index.
     */
    public Object getValue(int index) {
        
        Object value = null;
        if (isDestroyed()) {
            logger.warn("System tried to retrieve a value while the object was already destroyed");
        } else if (getField(index) == null) {
            logger.warn("Field with index " + index + " does not exist for module " + getModule());
        } else {
        	// recalculate loan information
        	if (getModule().canBeLend() && index != DcObject._SYS_AVAILABLE) {
        		Boolean available = (Boolean) getValue(DcObject._SYS_AVAILABLE);
            	if (available != null && !available.booleanValue())
            		setLoanInformation();
        	}
        	
            if (index == _SYS_DISPLAYVALUE) {
                value = toString();
            } else if (index == _SYS_MODULE) {
                value = getModule();
            } else {
                value = getValueDef(index).getValue();
            }
        }
        
        return value;
    }

    /**
     * Gets the display value for the specified field.
     * @see DcObject#_SYS_DISPLAYVALUE 
     * @param index The field index
     */
    public String getDisplayString(int index) {
        if (index == _SYS_DISPLAYVALUE)
            return toString();
        else if (index == _SYS_MODULE)
            return getModule().getObjectNamePlural();
        
        // as file mappings can be changed at any time recalculate the display value
        if (getField(index) != null &&
            (getField(index).getFieldType() == ComponentFactory._FILEFIELD ||
             getField(index).getFieldType() == ComponentFactory._FILELAUNCHFIELD) &&
             getValueDef(index) != null) {

            getValueDef(index).createDisplayString(getField(index));
        }
        
        return getValueDef(index) != null ? getValueDef(index).getDisplayString() : "";
    }

    /**
     * Retrieves the field type.
     * @see ComponentFactory
     * @param index The field index.
     */
    public int getFieldType(int index) {
        return getField(index).getFieldType();
    }

    /**
     * Retrieves the database column name.
     * @param index The field index.
     * @return The database field name or null for UI only fields.
     */
    public String getDatabaseFieldName(int index) {
        return getField(index).getDatabaseFieldName();
    }

    /**
     * Applies the enhancers on this item.
     * @see ValueEnhancers 
     * @param update Indicates if the item is new or existing.
     */
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
    
    private Date getCurrentDate() {
        return new Date();
    }

    /**
     * Inserts the item into the database.
     * @param queued Indicates if the item should be saved using the query queue.
     * @see Query
     * @see DatabaseManager
     * @see QueryQueue
     * @throws ValidationException
     */
    public void saveNew(boolean queued) throws ValidationException {
        try {
            markAsChanged();
            applyEnhancers(false);
            checkIntegrity();
            
            beforeSave();
            setValue(_SYS_CREATED, getCurrentDate());
            
            setIDs();
            
            if (queued) {
            	if (synchronizeWithDM)
            		addRequest(new SynchronizeWithManagerRequest(SynchronizeWithManagerRequest._ADD, this));
            	
            	WorkFlow.insert(this);
            } else {
                Query query = new Query(Query._INSERT, this, null, null);
            	DatabaseManager.executeQuery(query);

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
    
    /**
     * Save the changed item to the database.
     * @param queued Indicates if the item should be saved using the query queue.
     * @see Query
     * @see DatabaseManager
     * @see QueryQueue
     * @throws ValidationException
     */
    public void saveUpdate(boolean queued) throws ValidationException {
        saveUpdate(queued, true);
    }
    
    /**
     * Save the changed item to the database.
     * @param queued Indicates if the item should be saved using the query queue.
     * @param validate Indicates if the item should be validated before saving.
     * @see Query
     * @see DatabaseManager
     * @see QueryQueue
     * @throws ValidationException
     */
    public void saveUpdate(boolean queued, boolean validate) throws ValidationException {
        try {
            applyEnhancers(true);
            
            if (validate)
                checkIntegrity();

            beforeSave();
            setValue(_SYS_MODIFIED, getCurrentDate());
            
            if (queued) {
            	if (synchronizeWithDM)
            		addRequest(new SynchronizeWithManagerRequest(SynchronizeWithManagerRequest._UPDATE, this));
            	
            	WorkFlow.update(this);
            } else {
                Query query = new Query(Query._UPDATE, this, null, null);
                DatabaseManager.executeQuery(query);
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

    /**
     * Permanently deletes the item.
     */
    public void delete(boolean validate) throws ValidationException {
        
        if (validate)
            beforeDelete();
        
    	if (synchronizeWithDM)
    		addRequest(new SynchronizeWithManagerRequest(SynchronizeWithManagerRequest._DELETE, this));
    	
        WorkFlow.delete(this);
    }
    
    protected void beforeDelete() throws ValidationException {
        Collection<DcObject> items = DataManager.getReferencingItems(this);
        if (items.size() > 0) 
            throw new ValidationException(DcResources.getText("msgCannotDeleteDueToReferences", items.toString()));
    }

    /**
     * Indicates if messages should be displayed to the user.
     * @param b
     */
    public void setSilent(boolean b) {
        this.silent = b;
        
        if (getCurrentChildren() != null) {
            for (DcObject child : getCurrentChildren())
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

    /**
     * Retrieves the field
     * @param index Field index
     */
    public DcField getField(int index) {
        return getModule().getField(index);
    }

    /**
     * Checks the integrity of the item. 
     * @param update Indicates if the item is new or not.
     * @throws ValidationException
     */
    public void checkIntegrity() throws ValidationException {
        if (DcSettings.getBoolean(DcRepository.Settings.stCheckRequiredFields))
            validateRequiredFields();

        if (DcSettings.getBoolean(DcRepository.Settings.stCheckUniqueness))
            isUnique();
    }

    /**
     * Checks if the item is unique.
     * @param o The item to be checked.
     * @param update Indicates if the item is new or not.
     * @throws ValidationException
     */
    public void isUnique() throws ValidationException {
        boolean bUnique = WorkFlow.checkUniqueness(this, !isNew());
        if (!bUnique && validate) {
            
            String fields = "";
            for (DcFieldDefinition definition : getModule().getFieldDefinitions().getDefinitions()) {
                if (definition.isUnique()) {
                    fields += fields.length() > 0 ? ", " : "";
                    fields += definition.getLabel();
                }
            }
                
        	throw new ValidationException(DcResources.getText("msgItemNotUnique", new String[] {toString(), fields}));
        }
    }

    /**
     * Retrieves all field indices.
     */
    public int[] getFieldIndices() {
        return getModule().getFieldIndices();
    }

    public void setIDs() {
        if (hasPrimaryKey()) {
            String id = getID(); //Utilities.getUniqueID();
            
            while (id == null || id.length() < 15)
                id = Utilities.getUniqueID();
            
            id = id.substring(0, 15);
            setValue(DcObject._ID, id);
            
            if (children != null) {
                for (DcObject child : children) {
                    if (child.hasPrimaryKey()) {
                        child.setValue(DcObject._ID, null);
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
    
    protected void validateRequiredFields() throws ValidationException {
        
        if (!validate) return;
        
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
     * Merges the values of this and the source item.
     * Only empty values are updated with the values of the source item.  
     * @param dco The source item.
     */
    public void merge(DcObject dco) {
        copy(dco, false, false);
    }
    
    /**
     * Copies all values from the specified Data Crow object.
     * @param overwrite Indicates whether existing values should be overwritten.
     * @param allowDeletes Allows existing values to be cleared.
     * @param dco Source item.
     */
    @SuppressWarnings("unchecked")
    public void copy(DcObject dco, boolean overwrite, boolean allowDeletes) {
        int[] fields = dco.getFieldIndices();
        for (int i = 0; i < fields.length; i++) {
            int field = fields[i];

            // Do not overwrite when:
            // - the to be copied value is empty and deletes are not allowed
            // - overwriting is not allowed and the current value is not empty
            if (!dco.isFilled(field) && !allowDeletes)
                continue;
            else if (!overwrite && isFilled(field))
                continue;
            
            if (field != _ID) {
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
            	} else if (o != null && getField(field).getValueType() == DcRepository.ValueTypes._PICTURE) {
                    Picture curPic = (Picture) dco.getValue(field);
                    
                    Picture newPic = (Picture) DcModules.get(DcModules._PICTURE).getItem();
                    newPic.copy(curPic, overwrite, allowDeletes);
                    
                    newPic.isDeleted(curPic.isDeleted());
                    newPic.isEdited(curPic.isEdited());
                    
                    setValue(field, newPic);
            	} else if (o != null && getField(field).getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
            	    Collection<DcMapping> newMappings = new ArrayList<DcMapping>();
            	    
            	    for (DcObject mapping : (Collection<DcObject>) o)
            	        newMappings.add((DcMapping) mapping.clone());
            	    
            	    setValue(field, newMappings);
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
                
                Picture pic = (Picture) DcModules.get(DcModules._PICTURE).getItem();
                templatePic.loadImage();
                pic.setValue(Picture._D_IMAGE, templatePic.getValue(Picture._D_IMAGE));
                pic.setValue(Picture._E_HEIGHT, templatePic.getValue(Picture._E_HEIGHT));
                pic.setValue(Picture._F_WIDTH, templatePic.getValue(Picture._F_WIDTH));
                pic.isEdited(true);
                
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
     * 
     * Note; this is not a shallow copy and costs just as much resources as its original.
     * After using the clone it is best to disgard it by calling the destroy method.
     */
    @Override
    public DcObject clone() {
        DcObject dco = getModule().getItem();
        
        dco.copy(this, true, true);
        dco.setValue(DcObject._ID, getID());
        dco.markAsUnchanged();
        
        if (children != null) {
            for (DcObject child : children)
                dco.addChild(child.clone());
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
        release();
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
