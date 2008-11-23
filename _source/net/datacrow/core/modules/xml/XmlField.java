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

package net.datacrow.core.modules.xml;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.InvalidValueException;
import net.datacrow.core.objects.DcField;
import net.datacrow.util.XMLParser;

import org.w3c.dom.Element;

/**
 * A XML definition of a {@link DcField}.
 * 
 * @author Robert Jan van der Waals
 */
public class XmlField extends XmlObject {
    
    private int index;
    
    private int valueType;
    private int fieldType;
    private int maximumLength;
    private int moduleReference;
    
    private String name;
    private String column;
    private boolean uiOnly;
    private boolean enabled;
    private boolean readonly;
    private boolean searchable;
    private boolean techinfo;
    private boolean overwritable;
   
    /**
     * Creates an empty instance.
     */
    public XmlField() {}
    
    /**
     * Creates a new instances.
     * @param module The XML module to which the field belongs.
     * @param element The XML element to parse.
     * @throws InvalidValueException
     */
    public XmlField(XmlModule module, Element element) throws InvalidValueException {
        index = XMLParser.getInt(element, "index");
        name = XMLParser.getString(element, "name");
        column = XMLParser.getString(element, "database-column-name");
        uiOnly =  XMLParser.getBoolean(element, "ui-only");
        enabled = XMLParser.getBoolean(element, "enabled");
        readonly = XMLParser.getBoolean(element, "readonly");
        searchable = XMLParser.getBoolean(element, "searchable");
        techinfo = XMLParser.getBoolean(element, "techinfo");
        maximumLength =  XMLParser.getInt(element, "maximum-length");
        fieldType =  XMLParser.getInt(element, "field-type");
        valueType = XMLParser.getInt(element, "value-type");
        overwritable = XMLParser.getBoolean(element, "overwritable");
        
        String reference = XMLParser.getString(element, "module-reference");
        if (module != null)
            moduleReference = reference == null || reference.trim().length() == 0 || reference.equals("{index}") ?
                              module.getIndex() :  XMLParser.getInt(element, "module-reference"); 
    }

    /**
     * The database column name
     */
    public String getColumn() {
        return column;
    }

    /**
     * Indicates if the field is enabled by default.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * The field type.
     * @see ComponentFactory
     */
    public int getFieldType() {
        return fieldType;
    }

    /**
     * The unique field index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * The maximum length for the values for this field.
     */
    public int getMaximumLength() {
        return maximumLength;
    }

    /**
     * The module reference (module index). Can be any module for reference fields. 
     */
    public int getModuleReference() {
        return moduleReference;
    }

    /**
     * The field's system name.
     */
    public String getName() {
        return name;
    }

    /**
     * Indicates whether this field is editable or not.
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * Indicates whether a user can search on this field.
     */
    public boolean isSearchable() {
        return searchable;
    }

    /**
     * Indicates whether the field holds technical information.
     */
    public boolean isTechinfo() {
        return techinfo;
    }

    /**
     * Indicates if the value is actually stored in the database or that the value
     * is calculated.
     */
    public boolean isUiOnly() {
        return uiOnly;
    }

    /**
     * The value type.
     * @see DcRepository.ValueTypes
     */
    public int getValueType() {
        return valueType;
    }

    /**
     * Sets the database column name.
     * @param column
     */
    public void setColumn(String column) {
        this.column = column;
    }
    
    /**
     * Indicates whether this field is enabled by default.
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Sets the field type.
     * @see ComponentFactory
     * @param fieldType
     */
    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * Sets the unique field index.
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Sets the maximum length for the value of this field.
     * @param maximumLength
     */
    public void setMaximumLength(int maximumLength) {
        this.maximumLength = maximumLength;
    }

    /**
     * Sets the module reference.
     * @param moduleReference
     */
    public void setModuleReference(int moduleReference) {
        this.moduleReference = moduleReference;
    }

    /**
     * Sets the system name.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Marks the field as readonly.
     * @param readonly
     */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    /**
     * Indicate if a user can search on this field.
     * @param searchable
     */
    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    /**
     * Indicates if the field holds technical information.
     * @param techinfo
     */
    public void setTechinfo(boolean techinfo) {
        this.techinfo = techinfo;
    }

    /**
     * Indicate if the value of this field is stored in the database or that it is calculated.
     * @param uiOnly
     */
    public void setUiOnly(boolean uiOnly) {
        this.uiOnly = uiOnly;
    }

    /**
     * Sets the value type.
     * @see DcRepository.ValueTypes
     * @param valueType
     */
    public void setValueType(int valueType) {
        this.valueType = valueType;
    }
    
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Indicates if this field can be removed using the module wizard.
     */
    public boolean isOverwritable() {
        return overwritable;
    }

    /**
     * Indicate if this field can be removed using the module wizard.
     */
    public void setOverwritable(boolean overwritable) {
        this.overwritable = overwritable;
    }
    
    @Override
    public int hashCode() {
        return getModuleReference() + getIndex();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof XmlField) {
            XmlField field = (XmlField) o; 
            return field.getModuleReference() == getModuleReference() && field.getIndex() == getIndex();
        } 
        return false;
    }
}
