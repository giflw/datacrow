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

package net.datacrow.core.web.model;

import java.util.ArrayList;
import java.util.List;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.web.DcSecured;
import net.datacrow.util.Rating;

/**
 * A light weight version of the DcField. The heavy weight version is also present
 * in memory, on the server. 
 */
public class DcWebField extends DcSecured {

    public static final int _CHECKBOX = 0;
    public static final int _TEXTFIELD = 1;
    public static final int _LONGFIELD = 2;
    public static final int _DROPDOWN = 3;
    public static final int _IMAGE = 4;
    public static final int _URLFIELD = 5;
    public static final int _MULTIRELATE = 6;
    public static final int _DATE = 7;
    public static final int _FILE = 8;
    
    private int index;
    private int module;
    private int type;
    private int maxTextLength;
    
    private String label;
    private String width;
    private Object value;
    
    private boolean isLinkToDetails;
    private boolean readonly;
    private boolean required;

    public DcWebField(DcField field) {
        this.index = field.getIndex();
        this.label = field.getLabel();
        this.module = field.getModule();
        this.readonly =  field.isReadOnly() || !getUser().isEditingAllowed(field);
        this.required = field.isRequired();
        
        if (getDcField().getFieldType() == ComponentFactory._RATINGCOMBOBOX ||
            getDcField().getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
            type = _DROPDOWN;
        } else if (getDcField().getValueType() == DcRepository.ValueTypes._PICTURE) {
            type = _IMAGE;
        } else if (getDcField().getValueType() == DcRepository.ValueTypes._BOOLEAN) {
            type = _CHECKBOX;
        } else if (getDcField().getFieldType() == ComponentFactory._LONGTEXTFIELD) {
            type = _LONGFIELD;
        } else if (getDcField().getFieldType() == ComponentFactory._URLFIELD) {
            type = _URLFIELD;
        } else if (getDcField().getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
            type = _MULTIRELATE;
        } else if (getDcField().getValueType() == DcRepository.ValueTypes._DATE) {
            type = _DATE;
        } else if (getDcField().getFieldType() == ComponentFactory._FILEFIELD ||
                   getDcField().getFieldType() == ComponentFactory._FILELAUNCHFIELD) {
            type = _FILE;
        } else {
            type = _TEXTFIELD;
        } 
    }
    
    public boolean isRequired() {
        return required;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public void setMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
    }

    public int getType() {
        return type;
    }

    public void setWidth(int width) {
        this.width = String.valueOf(width);
    }

    public boolean isLinkToDetails() {
        return isLinkToDetails;
    }

    public void setLinkToDetails(boolean isLinkToDetails) {
        this.isLinkToDetails = isLinkToDetails;
    }

    /**
     * Retrieves the value for this field. This is only used for input forms.
     */
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public DcModule getDcModule() {
        return DcModules.get(module);
    }
    
    public DcField getDcField() {
        return getDcModule().getField(index);
    }
    
    public List<DcReference> getReferences() {
        List<DcReference> references = new ArrayList<DcReference>();
        if (getDcField().getFieldType() == ComponentFactory._RATINGCOMBOBOX) {
            for (int rating = -1; rating < 11; rating++)
                references.add(new DcReference(Rating.getLabel(rating), String.valueOf(rating)));
        } else {
            for (DcObject dco : DataManager.get(DcModules.getReferencedModule(getDcField()).getIndex(), null))
                references.add(new DcReference(dco.toString(), dco.getID()));
        }
        
        return references; 
    }
    
    public boolean isDropDown() {
        return type == _DROPDOWN;
    }
    
    public boolean isImage() {
        return type == _IMAGE;
    }

    public boolean isCheckbox() {
        return type == _CHECKBOX;
    }

    public boolean isUrl() {
        return type == _URLFIELD;
    }
    
    public boolean isFile() {
        return type == _FILE;
    }    
    
    public boolean isTextfield() {
        return type == _TEXTFIELD || type == _FILE;
    }

    public boolean isLongTextfield() {
        return type == _LONGFIELD;
    }

    public boolean isMultiRelate() {
        return type == _MULTIRELATE;
    }

    public boolean isDate() {
        return type == _DATE;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public String getLabel() {
        return label;
    }

    public String getWidth() {
        return width;
    }

    public int getIndex() {
        return index;
    }
}
