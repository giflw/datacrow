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
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.xml.XmlField;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.enhancers.IValueEnhancer;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.DcFieldDefinitions;

public class DcField implements Serializable{

    private static final long serialVersionUID = -3426157253979375896L;
    
    private String systemName;
    private String label;
    private boolean enabled;
    private boolean required;
    private boolean searchable;
    private boolean readonly;
    private boolean techinfo;
    private boolean uiOnly;
    private int maximumLength;
    private int fieldType;
    private int valueType;
    private int index;
    private int module;
    private int sourceModuleIdx;
    private String databaseFieldName;
    
    private Collection<IValueEnhancer> enhancers = new ArrayList<IValueEnhancer>();

    public DcField(XmlField field, int module) {
        this(field.getIndex(), module, field.getName(), field.isUiOnly(), field.isEnabled(),
             field.isReadonly(), field.isSearchable(), field.isTechinfo(), field.getMaximumLength(),
             field.getFieldType(), field.getModuleReference(), field.getValueType(), field.getColumn());
    }
    
    public DcField( int index,
                    int module,
                    String label,
                    boolean uiOnly,
                    boolean enabled,
                    boolean readonly,
                    boolean searchable,
                    boolean techinfo,
                    int maximumLength,
                    int fieldType,
                    int modRef,
                    int valueType,
                    String databaseFieldName) {

        setEnabled(enabled);
        setIndex(index);
        setModule(module);
        setLabel(label);
        setSystemName(label);
        setReadOnly(readonly);
        setSearchable(searchable);
        setTechinfo(techinfo);
        setMaximumLength(maximumLength);
        setFieldType(fieldType);
        setValueType(valueType);
        setDatabaseFieldName(databaseFieldName);
        setUiOnly(uiOnly);
        setSourceModuleIdx(modRef);
    }
    
    public void setSourceModuleIdx(int modRef) {
        this.sourceModuleIdx = modRef;
    }

    public int getSourceModuleIdx() {
        return sourceModuleIdx;
    }

    public int getReferenceIdx() {
        return DcModules.getReferencedModule(this).getIndex();
    }
    
    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public int getModule() {
        return module;
    }

    public void setTechinfo(boolean techinfo) {
        this.techinfo = techinfo;
    }

    public void setUiOnly(boolean uiOnly) {
        this.uiOnly = uiOnly;
    }

    public boolean isUiOnly() {
        return uiOnly;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        if (    SecurityCentre.getInstance().getUser() == null ||
                SecurityCentre.getInstance().getUser().isAuthorized(this)) {
            
            if (DcModules.get(getModule()) == null)
                return enabled;

            if (DcModules.get(getModule()).getSettings() == null)
                return enabled;
            
            DcFieldDefinitions definitions = (DcFieldDefinitions) 
                DcModules.get(getModule()).getSettings().getDefinitions(DcRepository.ModuleSettings.stFieldDefinitions);
            
            for (DcFieldDefinition definition : definitions.getDefinitions()) {
                if (definition.getIndex() == getIndex()) 
                    return definition.isEnabled();
                
            }
            
            return enabled; 
        } else { 
            return false;
        }
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public String getDatabaseFieldName() {
        return databaseFieldName;
    }

    public int getFieldType() {
        return fieldType;
    }

    public int getValueType() {
        return valueType;
    }

    public String getLabel() {
        return label;
    }

    public String getSystemName() {
        return systemName;
    }

    public boolean isTechnicalInfo() {
        return techinfo;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isReadOnly() {
        if (     SecurityCentre.getInstance().getUser() != null &&
                !SecurityCentre.getInstance().getUser().isEditingAllowed(this))
            return true;
        else 
            return readonly;
    }

    public boolean isSearchable() {
        return searchable;
    }

    /**
     * Returns the maximum field length (characters positions).
     * In case the field is of type long text field the maximum value will be
     * the maximum integer (Integer.MAX_VALUE) value (maximum field setting is thus overruled).
     * @return
     */
    public int getMaximumLength() {
        return getFieldType() == ComponentFactory._LONGTEXTFIELD ? 
               Integer.MAX_VALUE : 
               maximumLength;
    }

    public void setLabel(String s) {
        label = s;
    }

    private void setSystemName(String s) {
        systemName = s;
    }

    public void setReadOnly(boolean b) {
        readonly = b;
    }

    public void setRequired(boolean b) {
        required = b;
    }

    public void setMaximumLength(int i) {
        maximumLength = i;
    }

    public void setFieldType(int index) {
        fieldType = index;
    }

    public void setValueType(int index) {
        valueType = index;
    }

    public void setDatabaseFieldName(String s) {
        databaseFieldName = s;
    }

    @Override
    public String toString() {
        return label;
    }
    
    public void removeEnhancers() {
        enhancers.clear();
    }
    
    public void addValueEnhancer(IValueEnhancer enhancer) {
        enhancers.add(enhancer);
    }
    
    public IValueEnhancer[] getValueEnhancers() {
        return enhancers.toArray(new IValueEnhancer[0]);
    }

    public String getDataBaseFieldType() {
        String s = "";
        if (getValueType() == DcRepository.ValueTypes._STRING) {
            if (getFieldType() == ComponentFactory._LONGTEXTFIELD)
                s = DcRepository.Database._FIELDOBJECT;
            else
                s = DcRepository.Database._FIELDSTRING + "(" + getMaximumLength() + ")";

        } else if (getValueType() == DcRepository.ValueTypes._DOUBLE) {
            s = DcRepository.Database._FIELDNUMERIC + "(10, 2)";
            
        } else if (getValueType() == DcRepository.ValueTypes._BIGINTEGER ||
                   getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE ||
                   getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                   getValueType() == DcRepository.ValueTypes._DCPARENTREFERENCE ||
                   getValueType() == DcRepository.ValueTypes._LONG) {
            s = DcRepository.Database._FIELDBIGINT;
            
        } else if (getValueType() == DcRepository.ValueTypes._ICON ||
                   getValueType() == DcRepository.ValueTypes._PICTURE ||
                   getValueType() == DcRepository.ValueTypes._BLOB) {
            
            s = DcRepository.Database._FIELDOBJECT;
        } else if (getValueType() == DcRepository.ValueTypes._BOOLEAN) {
            
            s = DcRepository.Database._FIELDBOOLEAN;
        } else if (getValueType() == DcRepository.ValueTypes._DATE) {
            
            s = DcRepository.Database._FIELDDATE;
        }

        return s;
    }
}
