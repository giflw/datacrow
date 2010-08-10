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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;

import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.web.DcSecured;

public class AdvancedFilter extends DcSecured {

    private DataFilter df;
    private DataFilterEntry entry = new DataFilterEntry();

    private List<DcWebModule> modules = new ArrayList<DcWebModule>();
    
    private List<Operator> operators = new ArrayList<Operator>();
    private List<String> andOr = new ArrayList<String>();
        
    private Map<Integer, List<DcWebField>> fields = new HashMap<Integer, List<DcWebField>>();

    private int selectedFieldIdx;
    private int selectedModuleIdx;
    
    private int mainModuleIdx;
    
    public AdvancedFilter() {
        andOr.add(DcResources.getText("lblAnd"));
        andOr.add(DcResources.getText("lblOr"));
    }
    
    public List<Operator> getOperators() {
        return operators;
    }

    public List<String> getAndOrList() {
        return andOr;
    }
    
    public void addCurrentEntry() {
        df.addEntry(entry);
        entry = new DataFilterEntry();
        setFieldIdx(selectedFieldIdx);
        setModule(selectedModuleIdx);
    }
    
    public void deleteEntry(int id) {
        DataFilterEntry remove = null;
        for (DataFilterEntry entry : getEntries()) {
            if (entry.getID() == id)
                remove = entry;
        }
        
        if (remove != null)
            df.getEntries().remove(remove);
    }
    
    public void editEntry(int id) {
        for (DataFilterEntry entry : getEntries()) {
            if (entry.getID() == id)
                this.entry = entry;
        }
        
        selectedFieldIdx = entry.getField();
        selectedModuleIdx = entry.getModule();
        getEntries().remove(entry);
    }
    
    public DataFilterEntry getEntry() {
        return entry;
    }

    public void selectField(ValueChangeEvent vce) throws AbortProcessingException {
        if (vce.getNewValue() != null) {
            setFieldIdx((Integer) vce.getNewValue(), selectedModuleIdx);
        }
    }

    public void selectModule(ValueChangeEvent vce) throws AbortProcessingException {
        selectedModuleIdx = (Integer) vce.getNewValue();
        setFields(selectedModuleIdx);
    }
    
    public void initialize(int moduleIdx) {
        if (df == null || this.mainModuleIdx != moduleIdx) {
            mainModuleIdx = moduleIdx;
            fields.clear();
            modules.clear();
            
            df = new DataFilter(moduleIdx);
            
            setFields(moduleIdx);
            modules.add(new DcWebModule(moduleIdx, DcModules.get(moduleIdx).getLabel()));
            
            DcModule child = DcModules.get(moduleIdx).getChild();
            if (child != null) {
                modules.add(new DcWebModule(child.getIndex(), DcModules.get(child.getIndex()).getLabel()));
                setFields(child.getIndex());
            }
        }
        
        setFieldIdx(fields.get(moduleIdx).get(0).getIndex(), moduleIdx);
    }
    
    public List<DcWebField> getFields() {
        return fields.get(selectedModuleIdx);
    }
    
    private void setFields(int moduleIdx) {
        List<DcWebField> flds = new ArrayList<DcWebField>();
        for (DcField field : DcModules.get(moduleIdx).getFields()) {
            if (     getUser().isAuthorized(field) && 
                     field.isEnabled() && field.isSearchable()) {
                
                DcWebField wf = new DcWebField(field);

                if (wf.isLongTextfield())
                    wf.setType(DcWebField._TEXTFIELD);
                
                if (wf.isMultiRelate())
                    wf.setType(DcWebField._DROPDOWN);
                
                flds.add(wf);
            }
        }
        fields.put(moduleIdx, flds);
    }
    
    public List<DcWebModule> getModules() {
        return modules;
    }
    
    public void setFieldIdx(int fieldIdx, int moduleIdx) {
        this.selectedModuleIdx = moduleIdx;
        this.selectedFieldIdx = fieldIdx;
        
        setFieldIdx(fieldIdx);
        setModule(moduleIdx);
        
        operators.clear();
        operators.addAll(Operator.get(DcModules.get(moduleIdx).getField(fieldIdx)));
    }
    
    public List<DcReference> getReferences() {
        List<DcReference> references = new ArrayList<DcReference>();
        for (DcObject dco : DataManager.get(DcModules.get(selectedModuleIdx).getField(selectedFieldIdx).getReferenceIdx(), null)) {
            references.add(new DcReference(dco.toString(), dco.getID()));
        }
        return references;
    }

    public DcWebField getField() {
        for (DcWebField field : fields.get(selectedModuleIdx)) {
            if (field.getIndex() == selectedFieldIdx)
                return field;
        }
        return null;
    }
    
    public Collection<DataFilterEntry> getEntries() {
        return df.getEntries();
    }
    
    public DataFilter getFilter() {
        return df;
    }
    
    public void setAndOr(String andOr) {
        entry.setAndOr(andOr);
    }
    
    public void setFieldIdx(int fieldIdx) {
        entry.setField(fieldIdx);
        entry.setValue(null);
    }

    public void setModule(int moduleIdx) {
        entry.setModule(moduleIdx);
    }
    
    public void setOperator(int operatorIdx) {
        for (Operator o : Operator.values()) {
            if (o.getIndex() == operatorIdx)
                entry.setOperator(o);
        }
    }
    
    public void setValue(Object value) {
        DcField field = DcModules.get(selectedModuleIdx).getField(selectedFieldIdx);
        
        Object o = value;
        if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
            o = DataManager.getItem(field.getReferenceIdx(), String.valueOf(value));
        } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
            Collection<DcObject> references = new ArrayList<DcObject>();
            DcObject reference = DataManager.getItem(field.getReferenceIdx(), String.valueOf(value));
            references.add(reference);
            o = references;
        }
        
        entry.setValue(o);
    }
    
    public String getAndOr() {
        return entry.getAndOr();
    }
    
    public int getFieldIdx() {
        return entry.getField();
    }
    
    public int getModule() {
        return entry.getModule();
    }
    
    public int getOperator() {
        return entry.getOperator() != null ? entry.getOperator().getIndex() : 0;
    }
    
    public boolean isNeedsValue() {
        return entry.getOperator() != null ? entry.getOperator().needsValue() : false;
    }
    
    @SuppressWarnings("unchecked")
    public Object getValue() {
        Object value = entry.getValue();
        if (entry.getValue() instanceof List) {
            value =  ((List<DcObject>) entry.getValue()).get(0).getID();
        } else if (entry.getValue() instanceof DcObject) {
            value =  ((DcObject) entry.getValue()).getID();
        }
        return value; 
    }
}
