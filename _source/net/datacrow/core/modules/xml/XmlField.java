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

import net.datacrow.core.modules.InvalidValueException;
import net.datacrow.util.XMLParser;

import org.w3c.dom.Element;

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
    
    public XmlField() {}
    
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

    public String getColumn() {
        return column;
    }


    public boolean isEnabled() {
        return enabled;
    }


    public int getFieldType() {
        return fieldType;
    }


    public int getIndex() {
        return index;
    }


    public int getMaximumLength() {
        return maximumLength;
    }


    public int getModuleReference() {
        return moduleReference;
    }


    public String getName() {
        return name;
    }


    public boolean isReadonly() {
        return readonly;
    }


    public boolean isSearchable() {
        return searchable;
    }


    public boolean isTechinfo() {
        return techinfo;
    }


    public boolean isUiOnly() {
        return uiOnly;
    }


    public int getValueType() {
        return valueType;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setMaximumLength(int maximumLength) {
        this.maximumLength = maximumLength;
    }

    public void setModuleReference(int moduleReference) {
        this.moduleReference = moduleReference;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public void setTechinfo(boolean techinfo) {
        this.techinfo = techinfo;
    }

    public void setUiOnly(boolean uiOnly) {
        this.uiOnly = uiOnly;
    }

    public void setValueType(int valueType) {
        this.valueType = valueType;
    }
    
    @Override
    public String toString() {
        return getName();
    }

    public boolean isOverwritable() {
        return overwritable;
    }

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
