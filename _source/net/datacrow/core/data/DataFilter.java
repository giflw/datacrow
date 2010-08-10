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
import java.util.Date;
import java.util.List;

import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.util.StringUtils;
import net.datacrow.util.Utilities;
import net.datacrow.util.comparators.DcObjectComparator;

/**
 * Used when searching for items. 
 * A filter is created out of filter entries (see {@link DataFilterEntry}).
 * Filters can be saved to a file.
 *  
 * @author Robert Jan van der Waals
 */
public class DataFilter {

    private int module;

    private String name;
    private int sortOrder = DcObjectComparator._SORTORDER_ASCENDING;
    
    private DcField[] order;
    private Collection<DataFilterEntry> entries = new ArrayList<DataFilterEntry>();
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
   
    /**
     * Creates a filter based on the supplied item.
     * @param dco
     */
    public DataFilter(DcObject dco) {
        this.module = dco.getModule().getIndex();
        setEntries(dco);
    }

    /**
     * Creates a filter based on an xml definition.
     * @param xml
     * @throws Exception
     */
    public DataFilter(String xml) throws Exception {
        parse(xml);
    }
    
    /**
     * Creates an empty filter for a specific module.
     * @param module
     */
    public DataFilter(int module) {
        this.module = module;
    }    

    /**
     * Creates a filter using the supplied entries.
     * @param module
     * @param entries
     */
    public DataFilter(int module, Collection<DataFilterEntry> entries) {
        this(module);
        this.entries = entries;
    }    

    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * Sets the order. Results retrieved will be sorted based on this order.
     * @param s Array of field names (column names).
     */
    public void setOrder(String[] s) {
        order = new DcField[s.length];
        DcModule m = DcModules.get(module);
        for (int i = 0; i < s.length; i++)
            order[i] = m.getField(s[i]);
    }
    
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    /**
     * Sets the order. Results retrieved will be sorted based on this order.
     * @param order Array of fields.
     */
    public void setOrder(DcField[] order) {
        this.order = order;
    }
    
    /**
     * Adds a single entry to this filter.
     * @param entry
     */
    public void addEntry(DataFilterEntry entry) {
        entries.add(entry);
    }
    
    /**
     * Sets the entries for this filter.
     * Existing entries will be overwritten.
     * @param entries
     */
    public void setEntries(Collection<DataFilterEntry> entries) {
        this.entries = entries;
    }

    /**
     * Returns all entries belonging to this filter.
     * @return
     */
    public Collection<DataFilterEntry> getEntries() {
        return entries;
    }
    
    /**
     * Sets the entries based on the supplied item.
     * Existing entries will be overridden.
     * @param dco
     */
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
    
    /**
     * Returns the order information.
     * @return
     */
    public DcField[] getOrder() {
        return order;
    }
    
    /**
     * Returns the name of this filter.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this filter.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns the module for which this filter has been created.
     */
    public int getModule() {
        return module;
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
    
    /**
     * Parses the XML filter definition.
     * @param xml Filter definition
     * @throws Exception
     */
    private void parse(String xml) throws Exception {
        module = Integer.parseInt(StringUtils.getValueBetween("<MODULE>", "</MODULE>", xml));
        name = StringUtils.getValueBetween("<NAME>", "</NAME>", xml);
        
        if (xml.contains("<SORTORDER>"))
            sortOrder = Integer.parseInt(StringUtils.getValueBetween("<SORTORDER>", "</SORTORDER>", xml));
        
        String sEntries = StringUtils.getValueBetween("<ENTRIES>", "</ENTRIES>", xml);
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
            
            Object value = null;
            if (sValue.length() > 0) {
                DcField field = DcModules.get(iModule).getField(iField);
                int valueType = field.getValueType();
                if (valueType == DcRepository.ValueTypes._BOOLEAN) {
                    value = Boolean.valueOf(sValue);
                } else if (valueType == DcRepository.ValueTypes._DATE) {
                    value = sdf.parse(sValue);
                } else if (valueType == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                    DataFilter df = new DataFilter(field.getReferenceIdx());
                    df.addEntry(new DataFilterEntry(DataFilterEntry._AND,
                                                    field.getReferenceIdx(), 
                                                    DcObject._ID, 
                                                    Operator.EQUAL_TO, 
                                                    sValue));
                    List<DcObject> items = DataManager.get(df);
                    value = items != null && items.size() == 1 ? items.get(0) : sValue;
                } else if (valueType == DcRepository.ValueTypes._LONG) {
                    value = Long.valueOf(sValue);
                } else {
                    value = sValue;
                }
            }

            addEntry(new DataFilterEntry(sAndOr, iModule, iField, operator, value));
            
            sEntries = sEntries.substring(sEntries.indexOf("</ENTRY>") + 8, sEntries.length());
            idx = sEntries.indexOf("<ENTRY>");
        }
        
        Collection<DcField> fields = new ArrayList<DcField>();
        String sOrder = StringUtils.getValueBetween("<ORDER>", "</ORDER>", xml);
        idx = sOrder.indexOf("<FIELD>");
        while (idx != -1) {
            int iField = Integer.parseInt(StringUtils.getValueBetween("<FIELD>", "</FIELD>", sOrder));
            fields.add(DcModules.get(module).getField(iField));
            sOrder = sOrder.substring(sOrder.indexOf("</FIELD>") + 8, sOrder.length());
            idx = sOrder.indexOf("<FIELD>");
        }

        order = fields.toArray(new DcField[0]);
    }
    
    /**
     * Creates a xml definition for this filter.
     */
    public String toStorageString() {
        String storage = "<FILTER>\n";
        
        storage += "<NAME>" + getName() + "</NAME>\n";
        storage += "<MODULE>" + getModule() + "</MODULE>\n";
        storage += "<SORTORDER>" + getSortOrder() + "</SORTORDER>\n";
        
        storage += "<ENTRIES>\n";
        
        for (DataFilterEntry entry : entries) {
            
            if (entry == null) continue;

            storage += "<ENTRY>\n";
            
            storage += "<ANDOR>" + entry.getAndOr() + "</ANDOR>\n";
            storage += "<OPERATOR>" + entry.getOperator().getIndex() + "</OPERATOR>\n";
            storage += "<MODULE>" + entry.getModule() + "</MODULE>\n";
            storage += "<FIELD>" + entry.getField() + "</FIELD>\n";
            
            Object value;
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
    
    @SuppressWarnings("unchecked")
    public String toSQL(int[] fields) {
        DcModule module = DcModules.get(getModule());
        String columns = "";
        DcField field;
        if (fields != null && fields.length > 0) {
            for (int idx : fields) {
                field = module.getField(idx);
                
                if (!field.isUiOnly()) {
                    if (columns.length() > 0) columns += ", ";
                    columns += field.getDatabaseFieldName();
                }
            }
        } else {
            columns = "*";
        }
        
        StringBuffer sql = new StringBuffer("SELECT " + columns + " FROM " + module.getTableName());
        
        Object value;
        int operator;
        int counter2;
        int counter = 0;
        String queryValue = null;
        for (DataFilterEntry entry : getEntries()) {
            module = DcModules.get(entry.getModule());
            field = module.getField(entry.getField());
            operator = entry.getOperator().getIndex();
            value = entry.getValue() != null ? Utilities.getQueryValue(entry.getValue(), field) : null;
            
            if (value != null) {
                queryValue = String.valueOf(value);
                if (field.getValueType() == DcRepository.ValueTypes._DATE ||
                    field.getValueType() == DcRepository.ValueTypes._STRING) {
                    queryValue = queryValue.replaceAll("\'", "''");
                }
            }
            
            if (counter > 0) sql.append(entry.isAnd() ? " AND " : " OR ");
            
            if (counter == 0) sql.append(" WHERE ");
            
            if (field.getValueType() == DcRepository.ValueTypes._STRING)
                sql.append("UPPER(" + field.getDatabaseFieldName() + ")");
            else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION)
                sql.append("ID");
            else
                sql.append(field.getDatabaseFieldName());
            
            if (    operator == Operator.CONTAINS.getIndex() || 
                    operator == Operator.DOES_NOT_CONTAIN.getIndex() ||
                   (operator == Operator.EQUAL_TO.getIndex() && field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) ||
                   (operator == Operator.NOT_EQUAL_TO.getIndex() && field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION)) {

                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    if (operator == Operator.DOES_NOT_CONTAIN.getIndex() ||
                        operator == Operator.NOT_EQUAL_TO.getIndex()) 
                        sql.append(" NOT");

                    DcModule mapping = DcModules.get(DcModules.getMappingModIdx(field.getModule(), field.getReferenceIdx(), field.getIndex()));

                    sql.append(" IN (");
                    sql.append("SELECT ");
                    sql.append(mapping.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName());
                    sql.append(" FROM ");
                    sql.append(mapping.getTableName());
                    sql.append(" WHERE ");
                    sql.append(mapping.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName());
                    sql.append(" IN (");
                    
                    counter2 = 0;
                    for (DcObject dco : (Collection<DcObject>) value) {
                        if (counter > 0)  sql.append(",");
                        sql.append(dco.getID());
                        counter2++;
                    }
                    sql.append("))");
                } else {
                    if (operator == Operator.DOES_NOT_CONTAIN.getIndex()) sql.append(" NOT");
                    sql.append(" LIKE UPPER(");
                    sql.append("'%" + queryValue + "%')");
                }
                
            } else if (operator == Operator.ENDS_WITH.getIndex()) {
                sql.append(" LIKE UPPER(");
                sql.append("'%" + queryValue + "')");
            } else if (operator == Operator.EQUAL_TO.getIndex()) {
                if (value instanceof String)
                    sql.append(" = UPPER('"+ queryValue +"')");
                else 
                    sql.append(" = " + queryValue);
            } else if (operator == Operator.BEFORE.getIndex() ||
                       operator == Operator.LESS_THEN.getIndex()) {
                sql.append(" < ");
                sql.append(queryValue);
            } else if (operator == Operator.AFTER.getIndex() ||
                       operator == Operator.GREATER_THEN.getIndex()) {
                sql.append(" > ");
                sql.append(queryValue);
            } else if (operator == Operator.IS_EMPTY.getIndex()) {
                sql.append(" IS NULL");
            } else if (operator == Operator.IS_FILLED.getIndex()) {
                sql.append(" IS NOT NULL");
            } else if (operator == Operator.NOT_EQUAL_TO.getIndex()) {
                sql.append(" <> ");
                if (value instanceof String)
                    sql.append(" UPPER('"+ queryValue +"')");
                else 
                    sql.append(queryValue);
            } else if (operator == Operator.STARTS_WITH.getIndex()) {
                sql.append(" LIKE UPPER(");
                if (value instanceof String)
                    sql.append("'"+ queryValue +"%')");
                else 
                    sql.append(queryValue);
            }
            counter++;
        }
        
        counter = 0; 
        
        module = DcModules.get(getModule());
        field = module.getField(module.getDefaultSortFieldIdx());
        if (order != null && order.length > 0) {
            for (DcField orderOn : order) {
                sql.append(counter == 0 ? " ORDER BY " : ",");
                sql.append(orderOn.getDatabaseFieldName());
                counter++;
            }
        } else if (field != null && !field.isUiOnly()) {
            sql.append(" ORDER BY ");
            sql.append(module.getField(module.getDefaultSortFieldIdx()).getDatabaseFieldName());
        }
        
        return sql.toString();
    }
    
//    private void addAvailabilityCondition(DcObject dco, StringBuffer conditions) {
//        if (dco.getModule().canBeLend()) {
//            ContactPerson loanedBy = (ContactPerson) dco.getValue(DcObject._SYS_LENDBY);
//            Integer duration = (Integer) dco.getValue(DcObject._SYS_LOANDURATION);
//            String s = (String) dco.getValue(DcObject._SYS_AVAILABLE);
//
//            if (s != null || loanedBy != null || duration != null) {
//                boolean available = s == null || duration != null ? false : Boolean.valueOf(s);
//                loanedBy = available ? null : loanedBy;
//
//                if (conditions.length() > 0)
//                    conditions.append(" AND");
//
//                boolean hasChildren = dco.getChildren().size() > 0;
//                String column = hasChildren ? " " + dco.getTableShortName() + ".ID" : " ID";
//                String tablename = hasChildren ? dco.getTableShortName() : dco.getTableName();
//
//                String current = formatter.format(new Date());
//                String daysCondition = duration != null ? " AND DATEDIFF('dd', startDate , '" + current + "') >= " + duration.intValue() : "";
//                String personCondition = loanedBy != null ? " AND PersonID = " + loanedBy.getID() : "";
//
//                if (available)
//                    conditions.append(column + " NOT in (select objectID from Loans where objectID = " +
//                                      tablename + ".ID AND enddate IS NULL AND startDate <= '" + current +  "')");
//                else
//                    conditions.append(column + " in (select objectID from Loans where objectID = " +
//                                      tablename + ".ID " + daysCondition + " AND enddate IS NULL AND startDate <= '" + current +  "'" +
//                                      personCondition + ")");
//            }
//        }
//    }   

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : super.hashCode();
    }
    
    public boolean equals(DataFilter df) {
        return name != null && df.getName() != null ? name.equals(df.getName()) : df.getName() != null || name != null;
    }
}
