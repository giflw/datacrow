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

package net.datacrow.core.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMediaObject;
import net.datacrow.core.objects.DcObject;
import net.datacrow.util.DcObjectComparator;
import net.datacrow.util.DcObjectCompositeComparator;
import net.datacrow.util.StringUtils;

public class DataFilter {

    private int module;

    private String name;
    
    private DcField[] order;
    private Collection<DataFilterEntry> entries = new ArrayList<DataFilterEntry>();
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    
    public DataFilter(DcObject dco) {
        this.module = dco.getModule().getIndex();
        setEntries(dco);
    }

    public DataFilter(String xml) throws Exception {
        parse(xml);
    }
    
    public DataFilter(int module) {
        this.module = module;
    }    
    
    public DataFilter(int module, Collection<DataFilterEntry> entries) {
        this(module);
        this.entries = entries;
    }    
    
    public void setOrder(String[] s) {
        order = new DcField[s.length];
        DcModule m = DcModules.get(module);
        for (int i = 0; i < s.length; i++)
            order[i] = m.getField(s[i]);
    }
    
    public void setOrder(DcField[] order) {
        this.order = order;
    }
    
    public void addEntry(DataFilterEntry entry) {
        entries.add(entry);
    }
    
    public void setEntries(Collection<DataFilterEntry> entries) {
        this.entries = entries;
    }

    public Collection<DataFilterEntry> getEntries() {
        return entries;
    }
    
    public void setEntries(DcObject dco) {
        entries.clear();
        for (DcField field : dco.getFields()) {
            if (field.isSearchable() && dco.isChanged(field.getIndex())) { 
                entries.add(new DataFilterEntry(DataFilterEntry._AND,
                                                field.getModule(), 
                                                field.getIndex(), 
                                                Operator.EQUAL_TO, 
                                                dco.getValue(field.getIndex())));
            }
        }
    }
    
    public DcField[] getOrder() {
        return order;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public int getModule() {
        return module;
    }
    
    public boolean applies(DcObject dco) {
        boolean filterApplies = true;

        int counter = 0;
        for (DataFilterEntry entry : entries) {
            boolean entryApplies = false;
            
            if (dco.getModule().getIndex() != DcModules._CONTAINER)
                dco.loadChildren();
            
            if (    entry.getModule() == dco.getModule().getIndex() || 
                   (DcModules.get(entry.getModule()).isAbstract() && 
                    dco instanceof DcMediaObject)) {
                entryApplies = entry.applies(dco);
            } else if (dco.getChildren() != null) {
                for (DcObject child : dco.getChildren())
                    entryApplies |= entry.applies(child); 
            }
            
            filterApplies = counter == 0 ? entryApplies :
                            (entry.isAnd() ? filterApplies && entryApplies : filterApplies || entryApplies);
            
            counter++;
        }
        
        return filterApplies;
    }
    
    public void sort(List<DcObject> c) {
        if (order == null || order.length == 0 || c == null || c.size() == 0 || 
            DcModules.get(module).getTableName() == null || 
            DcModules.get(module).getTableName().equals(""))
            return;
        
        
        if (order.length == 1) {
            Collections.sort(c, new DcObjectComparator(order[0].getIndex()));
        } else {
            ArrayList<DcObjectComparator> dcocs = new ArrayList<DcObjectComparator>();
            for (int i = 0; i < order.length; i++) {
                if (order[i] != null) {
                    DcObjectComparator dcoc = new DcObjectComparator(order[i].getIndex());
                    dcocs.add(dcoc);
                }
            }
            
            DcObjectCompositeComparator cc = new DcObjectCompositeComparator(dcocs);
            Collections.sort(c, cc);
        }
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    @Override
    public boolean equals(Object o) {
        String name1 = o instanceof DataFilter ? ((DataFilter) o).getName() : "";
        String name2 = getName();
        
        name1 = name1 == null ? "" : name1;
        name2 = name2 == null ? "" : name2;
        
        return name1.equals(name2);
    }
    
    private void parse(String s) throws Exception {
        module = Integer.parseInt(StringUtils.getValueBetween("<MODULE>", "</MODULE>", s));
        name = StringUtils.getValueBetween("<NAME>", "</NAME>", s);
        
        String sEntries = StringUtils.getValueBetween("<ENTRIES>", "</ENTRIES>", s);
        int idx = sEntries.indexOf("<ENTRY>");
        while (idx != -1) {
            String sEntry = StringUtils.getValueBetween("<ENTRY>", "</ENTRY>", sEntries);
            int op = Integer.valueOf(StringUtils.getValueBetween("<OPERATOR>", "</OPERATOR>", sEntry)).intValue();
            
            Operator operator = null;
            for (Operator o : Operator.values()) {
                if (o.getIndex() == op)
                    operator = o;
            }
            
            int iField = Integer.valueOf(StringUtils.getValueBetween("<FIELD>", "</FIELD>", sEntry)).intValue();
            int iModule = Integer.valueOf(StringUtils.getValueBetween("<MODULE>", "</MODULE>", sEntry)).intValue();
            String sValue = StringUtils.getValueBetween("<VALUE>", "</VALUE>", sEntry);
            String sAndOr = StringUtils.getValueBetween("<ANDOR>", "</ANDOR>", sEntry);
            
            Object oValue = null;
            if (sValue.length() > 0) {
                DcField field = DcModules.get(iModule).getField(iField);
                int valueType = field.getValueType();
                if (valueType == DcRepository.ValueTypes._BOOLEAN) {
                    oValue = Boolean.valueOf(sValue);
                } else if (valueType == DcRepository.ValueTypes._DATE) {
                    oValue = sdf.parse(sValue);
                } else if (valueType == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                    DataFilter df = new DataFilter(field.getReferenceIdx());
                    df.addEntry(new DataFilterEntry(DataFilterEntry._AND,
                                                    field.getReferenceIdx(), 
                                                    DcObject._ID, 
                                                    Operator.EQUAL_TO, 
                                                    sValue));
                    Object[] v = DataManager.get(field.getReferenceIdx(), df);
                    oValue = v != null && v.length == 1 ? v[0] : sValue;
                } else if (valueType == DcRepository.ValueTypes._LONG) {
                    oValue = Long.valueOf(sValue);
                } else {
                    oValue = sValue;
                }
            }

            addEntry(new DataFilterEntry(sAndOr, iModule, iField, operator, oValue));
            
            sEntries = sEntries.substring(sEntries.indexOf("</ENTRY>") + 8, sEntries.length());
            idx = sEntries.indexOf("<ENTRY>");
        }
        
        Collection<DcField> fields = new ArrayList<DcField>();
        String sOrder = StringUtils.getValueBetween("<ORDER>", "</ORDER>", s);
        idx = sOrder.indexOf("<FIELD>");
        while (idx != -1) {
            int iField = Integer.parseInt(StringUtils.getValueBetween("<FIELD>", "</FIELD>", sOrder));
            fields.add(DcModules.get(module).getField(iField));
            sOrder = sOrder.substring(sOrder.indexOf("</FIELD>") + 8, sOrder.length());
            idx = sOrder.indexOf("<FIELD>");
        }

        order = fields.toArray(new DcField[0]);
    }
    
    public String toStorageString() {
        String storage = "<FILTER>\n";
        
        storage += "<NAME>" + getName() + "</NAME>\n";
        storage += "<MODULE>" + getModule() + "</MODULE>\n";
        
        storage += "<ENTRIES>\n";
        
        for (DataFilterEntry entry : entries) {

            storage += "<ENTRY>\n";
            
            storage += "<ANDOR>" + entry.getAndOr() + "</ANDOR>\n";
            storage += "<OPERATOR>" + entry.getOperator().getIndex() + "</OPERATOR>\n";
            storage += "<MODULE>" + entry.getModule() + "</MODULE>\n";
            storage += "<FIELD>" + entry.getField() + "</FIELD>\n";
            
            String value;
            if (entry.getValue() == null) {
                value = "";
            } else if (entry.getValue() instanceof DcObject) {
                value = ((DcObject) entry.getValue()).getID();
            } else if (entry.getValue() instanceof Date) {
                value = sdf.format((Date) entry.getValue());
            } else {
                value = entry.getValue().toString();
            }
            
            storage += "<VALUE>" + value + "</VALUE>\n";
            storage += "</ENTRY>\n";
        }
        
        storage += "</ENTRIES>\n";

        storage += "<ORDER>\n";
        
        for (int i = 0; i < order.length; i++)
            storage += "<FIELD>" + order[i].getIndex() + "</FIELD>\n";
        
        storage += "</ORDER>\n";
        storage += "</FILTER>\n";
        
        return storage;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : super.hashCode();
    }
    
    public boolean equals(DataFilter df) {
        return name != null && df.getName() != null ? name.equals(df.getName()) : df.getName() != null || name != null;
    }
}
