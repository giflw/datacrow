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
import java.util.Collection;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.web.DcSecured;
import net.datacrow.settings.definitions.WebFieldDefinition;
import net.datacrow.settings.definitions.WebFieldDefinitions;
import net.datacrow.util.StringUtils;

public class DcWebObject extends DcSecured {

    private List<DcWebField> fields = new ArrayList<DcWebField>();
    private List<DcWebField> technicalFields = new ArrayList<DcWebField>();
    private List<DcWebField> pictureFields = new ArrayList<DcWebField>();
    
    private boolean isChild;
    
    private DataModel childrenColumnHeaders;
    private DataModel children;
    
    private int module;
    private int rowIdx;
    
    private boolean isNew = false;
    private String ID;
    private String name;
    
    private int tab = 1;
    
    public DcWebObject() {}
    
    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean isChild) {
        this.isChild = isChild;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getName() {
        return name;
    }
    
    public int getRowIdx() {
        return rowIdx;
    }

    public void setRowIdx(int rowIdx) {
        this.rowIdx = rowIdx;
    }
    
    public boolean isInformationTabVisible() {
        return fields.size() > 0;
    }

    public boolean isTechnicalTabVisible() {
        return technicalFields.size() > 0;
    }

    public boolean isPictureTabVisible() {
        return pictureFields.size() > 0;
    }

    public boolean isChildrenTabVisible() {
        return DcModules.get(getModule()).getChild() != null && 
              !DcModules.get(getModule()).getChild().isAbstract();
    }
    
    public String getChildrenLabel() {
        return DcModules.get(getModule()).getChild() != null ?
               DcModules.get(getModule()).getChild().getObjectNamePlural() : "";
    }
    
    public void initialize(int moduleIdx) {
        reset();
        
        tab = 1;
        this.module = moduleIdx;
        
        
        WebFieldDefinitions definitions = (WebFieldDefinitions) DcModules.get(moduleIdx).getSetting(DcRepository.ModuleSettings.stWebFieldDefinitions);
        
        for (WebFieldDefinition def : definitions.getDefinitions()) {
            DcField field = DcModules.get(moduleIdx).getField(def.getField());

            if (    getUser().isAuthorized(field) && field.isEnabled()) {
                
                DcWebField wf = new DcWebField(field);
                
                wf.setType(wf.isUrl() ? DcWebField._TEXTFIELD : wf.getType());
                if (wf.isImage()) 
                    addPictureField(wf);
                else
                    addField(wf);
            }
        }
        
        if (tab == 3 && !isChildrenTabVisible())
            tab = 0;
        if (tab == 4 && !isPictureTabVisible())
            tab = 0;
    }

    public void loadChildren() {
        DcModule childModule = DcModules.get(getModule()).getChild(); 
        
        List<List<?>> woChildren = new ArrayList<List<?>>();
        List<DcWebField> woFields = new ArrayList<DcWebField>();
        getDcObject().setChildren(new ArrayList<DcObject>());
        getDcObject().loadChildren(null);
        if (getDcObject().getModule().getChild() != null) {
            for (DcObject child : getDcObject().getChildren()) {
                List<Object> values = new ArrayList<Object>();

                for (WebFieldDefinition def : childModule.getWebFieldDefinitions().getDefinitions()) {
                    DcField field = child.getModule().getField(def.getField());
                    if (def.isOverview() && getUser().isAuthorized(field) && field.isEnabled()) {
                        String s = child.getDisplayString(field.getIndex());
                        
                        if (def.getMaxTextLength() != 0 && field.getValueType() != DcRepository.ValueTypes._PICTURE)
                            s = StringUtils.concatUserFriendly(s, def.getMaxTextLength());

                        values.add(s);
                    }
                }
                values.add(child.getID());
                woChildren.add(values);
            }
            
            for (WebFieldDefinition def : DcModules.get(DcModules.get(getModule()).getChild().getIndex()).getWebFieldDefinitions().getDefinitions()) {
                DcField field = childModule.getField(def.getField());
                if (def.isOverview() && getUser().isAuthorized(field) && field.isEnabled()) {
                    DcWebField wf = new DcWebField(field);
                    wf.setWidth(def.getWidth());
                    wf.setLinkToDetails(def.isLink());
                    wf.setMaxTextLength(def.getMaxTextLength());
                    woFields.add(wf);
                }
            }
        }

        childrenColumnHeaders = new ListDataModel(woFields);
        children = new ListDataModel(woChildren);
    }
    
    public DataModel getChildrenColumnHeaders() {
        return childrenColumnHeaders;
    }

    public Object getChildrenColumnValue() {
        Object columnValue = null;
        if (children.isRowAvailable() && childrenColumnHeaders.isRowAvailable())
            columnValue = ((List) children.getRowData()).get(childrenColumnHeaders.getRowIndex());
        return columnValue;
    }
    
    @SuppressWarnings("unchecked")
    public void setChildrenColumnValue(Object value) {
        if (children.isRowAvailable() && childrenColumnHeaders.isRowAvailable())
            ((List) children.getRowData()).set(childrenColumnHeaders.getRowIndex(), value);
    }

    public String getChildrenColumnWidth() {
        if (children.isRowAvailable() && childrenColumnHeaders.isRowAvailable())
            return getChildrenWebField().getWidth();
        return null;
    }
    
    public boolean isLinkToChildDetails() {
        return getChildrenWebField().isLinkToDetails();
    }
    
    private DcWebField getChildrenWebField() {
        return (DcWebField) childrenColumnHeaders.getRowData();
    }
    
    public void load() {
        Collection<DcWebField> all = new ArrayList<DcWebField>();
        all.addAll(fields);
        all.addAll(technicalFields);
        all.addAll(pictureFields);
        
        DcObject dco = getDcObject();
        for (DcWebField wf : all) {
            DcField field = dco.getField(wf.getIndex());
            Object value = dco.getValue(field.getIndex());
            if (value != null) {
                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                    if (!(value instanceof String))
                        value = ((DcObject) value).getID();
                    else
                        value = null;
                } else if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                    value = "/mediaimages/" + ((Picture) value).getScaledFilename();
                } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    value = dco.getDisplayString(field.getIndex());
                } else if (value instanceof Number) {
                    value = String.valueOf(value);
                }
            }
            wf.setValue(value);
        }
        loadChildren();
    }
    
    public DcObject getDcObject() {
        return DataManager.getItem(getModule(), getID());        
    }
    
    private String current() {
        if (isChild)
            return "childdetails";
        else
            return "details";
    }
    
    public String switchToInfoTab() {
        tab = 1;
        return current();
    }

    public String switchToTechTab() {
        tab = 2;
        return current();
    }

    public String switchToChildTab() {
        tab = 3;
        return current();
    }

    public String switchToPicTab() {
        tab = 4;
        return current();
    }
    
    public int getTab() {
        return tab;
    }

    public List<DcWebField> getTechnicalFields() {
        return technicalFields;
    }

    public List<DcWebField> getPictureFields() {
        return pictureFields;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getModule() {
        return module;
    }
    
    public DataModel getChildren() {
        return children;
    }
    
    private void reset() {
        isNew = false;
        rowIdx = 0;
        ID = null;
        name = null;
        fields.clear();
        pictureFields.clear();
        technicalFields.clear();
        if (children != null)
            ((List) children.getWrappedData()).clear();
    }

    public void addField(DcWebField field) {
        fields.add(field);
    }

    public void addTechnicalField(DcWebField field) {
        technicalFields.add(field);
    }
    
    public void addPictureField(DcWebField field) {
        pictureFields.add(field);
    }
    
    public List<DcWebField> getFields() {
        return fields;
    }
    
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}