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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.util.StringUtils;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * Used to filter for items. 
 * A filter is created out of filter entries (see {@link DataFilterEntry}).
 * Filters can be saved to a file for reuse. Filters are used on the web as well as in 
 * the normal GUI.
 *  
 * @author Robert Jan van der Waals
 */
public class DataFilter {

    private static Logger logger = Logger.getLogger(DataFilter.class.getName());
    
    private int module;

    private final static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    
    private String name;
    
    public static final int _SORTORDER_ASCENDING = 0;
    public static final int _SORTORDER_DESCENDING = 1;
    
    private int sortOrder = _SORTORDER_ASCENDING;
    
    private DcField[] order;
    private Collection<DataFilterEntry> entries = new ArrayList<DataFilterEntry>();
    
    private final static Calendar cal = Calendar.getInstance();
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
                } else if (valueType == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    
                    StringTokenizer st = new StringTokenizer(sValue, ",");
                    Collection<DcObject> values = new ArrayList<DcObject>();
                    while (st.hasMoreElements()) {
                        DataFilter df = new DataFilter(field.getReferenceIdx());
                        df.addEntry(new DataFilterEntry(DataFilterEntry._AND,
                                                        field.getReferenceIdx(), 
                                                        DcObject._ID, 
                                                        Operator.EQUAL_TO, 
                                                        (String) st.nextElement()));

                        values.addAll(DataManager.get(df));
                    }
                
                    value = values;

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
            
            if (entry == null || entry.getValue() == null) continue;

            storage += "<ENTRY>\n";
            
            storage += "<ANDOR>" + entry.getAndOr() + "</ANDOR>\n";
            storage += "<OPERATOR>" + entry.getOperator().getIndex() + "</OPERATOR>\n";
            storage += "<MODULE>" + entry.getModule() + "</MODULE>\n";
            storage += "<FIELD>" + entry.getField() + "</FIELD>\n";
            
            Object value = "";
            if (entry.getValue() instanceof Collection) {
                String ids = "";
                for (Object o : ((Collection) entry.getValue())) {
                    if (o != null && o instanceof DcObject) {
                        ids += (ids.length() > 0 ? "," : "") + ((DcObject) o).getID();
                    } else {
                        logger.debug("Expected an instance of DcObject for Collections for DataFilter. Unexpected value encountered " + o);
                    }
                }
                value = ids;
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
    
    /**
     * Creates an entirely flat structure of the data.
     * - Items will be returned duplicated in case of multiple references.
     * - Pictures get their filename returned
     * 
     * @param fields
     * @param order
     * @return
     */
    public String toSQLFlatStructure(int[] fields) {
    	DcModule module = DcModules.get(getModule());
    	int[] queryFields = fields == null || fields.length == 0 ? module.getFieldIndices() : fields;
    	
    	StringBuffer sql = new StringBuffer();
    	StringBuffer joins = new StringBuffer();
    	
    	sql.append("SELECT DISTINCT ");
    	joins.append(" FROM ");
    	joins.append(module.getTableName());
    	joins.append(" MAINTABLE ");
    	
    	DcModule mapping;
    	DcModule reference;
    	String subTable;
    	String mapTable;
    	int tableCounter = 0;
    	int columnCounter = 0;
    	
    	for (int idx : queryFields) {
    		DcField field = module.getField(idx);
    		
    		if (  field.getIndex() == DcObject._SYS_AVAILABLE ||
                  field.getIndex() == DcObject._SYS_LENDBY ||
                  field.getIndex() == DcObject._SYS_LOANSTATUS ||
                  field.getIndex() == DcObject._SYS_LOANSTATUSDAYS ||
                  field.getIndex() == DcObject._SYS_LOANDUEDATE ||
                  field.getIndex() == DcObject._SYS_LOANDURATION) 
    		    continue;
    		
    		if (columnCounter > 0)
    			sql.append(", ");
    		
    		if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
    			mapping = DcModules.get(DcModules.getMappingModIdx(module.getIndex(), field.getReferenceIdx(), field.getIndex()));
    			reference = DcModules.get(field.getReferenceIdx());
    			
    			mapTable = " MAPTABLE" + tableCounter;
    			
    			joins.append(" LEFT OUTER JOIN ");
    			joins.append(mapping.getTableName());
    			joins.append(mapTable);
    			joins.append(" ON ");
    			joins.append(mapTable);
    			joins.append(".");
    			joins.append(mapping.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName());
    			joins.append(" = MAINTABLE.ID");
    			
    			subTable = " SUBTABLE" + tableCounter;
    			joins.append(" LEFT OUTER JOIN ");
    			joins.append(reference.getTableName());
    			joins.append(subTable);
    			joins.append(" ON ");
    			joins.append(subTable);
    			joins.append(".ID");
    			joins.append(" = ");
    			joins.append(mapTable);
    			joins.append(".");
    			joins.append(mapping.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName());
    			
    			sql.append(subTable);
    			sql.append(".");
    			sql.append(reference.getField(reference.getDisplayFieldIdx()).getDatabaseFieldName());
    			tableCounter++;
    			
    		} else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
    			reference = DcModules.get(field.getReferenceIdx());
    			subTable = " SUBTABLE" + tableCounter;
    			joins.append(" LEFT OUTER JOIN ");
    			joins.append(reference.getTableName());
    			joins.append(subTable);
    			joins.append(" ON ");
    			joins.append(subTable);
    			joins.append(".ID = MAINTABLE.");
    			joins.append(field.getDatabaseFieldName());
    			
    			sql.append(subTable);
    			sql.append(".");
    			sql.append(reference.getField(reference.getDisplayFieldIdx()).getDatabaseFieldName());
    			tableCounter++;
    			
    		} else if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
    			reference = DcModules.get(DcModules._PICTURE);
    			subTable = " SUBTABLE" + tableCounter;
    			
    			sql.append("(case when ");
    			sql.append(subTable);
    			sql.append(".OBJECTID IS NULL then '' else ");
    			sql.append("'/mediaimages/'+MAINTABLE.ID+'_");
    			sql.append(field.getDatabaseFieldName());
    			sql.append("_small.jpg' ");
    			sql.append("END) AS ");
    			sql.append(field.getDatabaseFieldName());
    			
    			joins.append(" LEFT OUTER JOIN ");
    			joins.append(reference.getTableName());
    			joins.append(subTable);
    			joins.append(" ON ");
    			joins.append(subTable);
    			joins.append(".OBJECTID = MAINTABLE.ID");
    			joins.append(" AND ");
    			joins.append(subTable);
    			joins.append(".");
    			joins.append(reference.getField(Picture._B_FIELD));
    			joins.append("='");
    			joins.append(field.getDatabaseFieldName());
    			joins.append("'");
    			
    			tableCounter++;

    		} else if (!field.isUiOnly()) {
    			sql.append("MAINTABLE.");
    			sql.append(field.getDatabaseFieldName());
    		} else {
    			sql.append("'N/A' AS ");
    			sql.append("NA");
    			sql.append(columnCounter);
    		}
    		columnCounter++;
    	}
    	
    	sql.append(joins.toString());
    	addEntries(sql, module);
    	return sql.toString();
    }
    
    public String toSQL(int[] fields, boolean order, boolean includeMod) {
        DcField field;
        
        DcModule m = DcModules.get(getModule());
        int[] queryFields = fields == null || fields.length == 0 ? m.getFieldIndices() : fields;
        
        Collection<DcModule> modules = new ArrayList<DcModule>();
        if (m.isAbstract())
        	modules.addAll(DcModules.getPersistentModules(m));
        else 
        	modules.add(m);
        
        StringBuffer sql = new StringBuffer();
        
        int columnCounter = 0;
        int moduleCounter = 0;
        if (m.isAbstract()) {
        	sql.append("SELECT MODULEIDX");
        	for (int idx : queryFields) {
				field = m.getField(idx);
				if (!field.isUiOnly()) {
					sql.append(", ");
					sql.append(field.getDatabaseFieldName());
					columnCounter++;
				}
			}
        	
        	sql.append(" FROM (");
        }
        
        for (DcModule module : modules) {
        	columnCounter = 0;
        	if (moduleCounter > 0)
				sql.append(" UNION ");
			
    		sql.append(" SELECT ");
        		
       		if (m.isAbstract() || includeMod) {
        		sql.append(module.getIndex());
        		sql.append(" AS MODULEIDX ");
        		columnCounter++;
        	}
			
			if (m.isAbstract()) {
				for (DcField abstractField : m.getFields()) {
					if (!abstractField.isUiOnly()) {
						if (columnCounter > 0) sql.append(", ");
						sql.append(abstractField.getDatabaseFieldName());
						columnCounter++;
					}
				}
			} else {
				for (int idx : queryFields) {
					field = m.getField(idx);
					if (!field.isUiOnly()) {
						if (columnCounter > 0) sql.append(", ");
						sql.append(field.getDatabaseFieldName());
						columnCounter++;
					}
				}
			}
			
			sql.append(" FROM ");
			sql.append(module.getTableName());

			if (order) addOrderByClause(sql);
			
	        addEntries(sql, module);

	        moduleCounter++;
        }
        
        if (m.isAbstract()) sql.append(") media ");
	        
        // add a join to the reference table part of the sort
        if (order) addOrderBy(sql);
        return sql.toString();
    }
    
    @SuppressWarnings("unchecked")
	private void addEntries(StringBuffer sql, DcModule module) {
    	boolean hasConditions = false;
        DcModule entryModule; 
        
        List<DataFilterEntry> childEntries = new ArrayList<DataFilterEntry>();

		Object value;
        int operator;
        int counter2;
        int counter = 0;
        String queryValue = null;
        DcField field;
        
        DcModule m = DcModules.get(getModule());
        
        for (DataFilterEntry entry : getEntries()) {
        	
        	if (!m.isAbstract()) {
	        	entryModule = DcModules.get(entry.getModule());
	            if (entry.getModule() != getModule()) {
	                childEntries.add(entry);
	                continue;
	            }
        	} else {
        		entryModule = module;
        	}
            
            field = entryModule.getField(entry.getField());
            
            if (    field.isUiOnly() && 
                    field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION &&
                    field.getValueType() != DcRepository.ValueTypes._PICTURE) 
                continue;
            
            hasConditions = true;
            
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
            
            
            boolean useUpper = field.getValueType() == DcRepository.ValueTypes._STRING &&
                field.getIndex() != DcObject._ID &&
                field.getValueType() != DcRepository.ValueTypes._DCOBJECTREFERENCE &&
                field.getValueType() != DcRepository.ValueTypes._DCPARENTREFERENCE &&
                field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION;
            
            if (field.getValueType() == DcRepository.ValueTypes._STRING) {
                if (useUpper) sql.append("UPPER(");
                sql.append(field.getDatabaseFieldName());
                if (useUpper) sql.append(")");
            } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                       field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                sql.append("ID");
            } else {
                sql.append(field.getDatabaseFieldName());
            }
            
            if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                
                if (operator == Operator.IS_EMPTY.getIndex()) 
                    sql.append(" NOT");
                
                DcModule picModule = DcModules.get(DcModules._PICTURE);
                sql.append(" IN (SELECT OBJECTID FROM " + picModule.getTableName() + 
                           " WHERE " + picModule.getField(Picture._B_FIELD).getDatabaseFieldName() + 
                           " = '" + field.getDatabaseFieldName() + "')");
                
            } else if ( operator == Operator.CONTAINS.getIndex() || 
                        operator == Operator.DOES_NOT_CONTAIN.getIndex() ||
                       (operator == Operator.EQUAL_TO.getIndex() && field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) ||
                       (operator == Operator.NOT_EQUAL_TO.getIndex() && field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION)) {

                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    if (operator == Operator.DOES_NOT_CONTAIN.getIndex() ||
                        operator == Operator.NOT_EQUAL_TO.getIndex()) 
                        sql.append(" NOT");

                    sql.append(" IN (");
                    
                    DcModule mapping = DcModules.get(DcModules.getMappingModIdx(entryModule.getIndex(), field.getReferenceIdx(), field.getIndex()));
                	sql.append("SELECT ");
                    sql.append(mapping.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName());
                    sql.append(" FROM ");
                    sql.append(mapping.getTableName());
                    sql.append(" WHERE ");
                    sql.append(mapping.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName());
	
                    sql.append(" IN (");
                    if (!(value instanceof Collection)) {
                        sql.append("'");
                        sql.append(value);
                        sql.append("'");
                        sql.append(")");
                    } else {
                        counter2 = 0;
                        for (Object o : (Collection<DcObject>) value) {
                            
                            if (counter2 > 0)  sql.append(",");

                            sql.append("'");
                            if (o instanceof DcObject)
                                sql.append(((DcObject) o).getID());
                            else
                                sql.append(o.toString());
                            sql.append("'");
                            
                            counter2++;
                        }
                        sql.append(")");
                    }
                    sql.append(")");
                } else {
                    if (operator == Operator.DOES_NOT_CONTAIN.getIndex()) sql.append(" NOT");
                    sql.append(" LIKE ");
                    
                    if (useUpper) sql.append("UPPER(");
                    sql.append("'%" + queryValue + "%'");
                    if (useUpper) sql.append(")");
                }

            } else if (operator == Operator.ENDS_WITH.getIndex()) {
                sql.append(" LIKE ");
                if (useUpper) sql.append("UPPER(");
                sql.append("'%" + queryValue);
                if (useUpper) sql.append(")");
            } else if (operator == Operator.EQUAL_TO.getIndex()) {
                if (useUpper) {
                    sql.append(" = UPPER('"+ queryValue +"')");
                } else {
                    sql.append(" = ");
                    if (value instanceof String) sql.append("'");
                    sql.append(queryValue);
                    if (value instanceof String) sql.append("'");
                }
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
                if (useUpper) {
                    sql.append(" UPPER('"+ queryValue +"')");
                } else {
                    if (value instanceof String) sql.append("'");
                    sql.append(queryValue);
                    if (value instanceof String) sql.append("'");
                }
            } else if (operator == Operator.STARTS_WITH.getIndex()) {
                
                sql.append(" LIKE ");
                if (useUpper) sql.append("UPPER(");
                sql.append("'%" + queryValue);
                
                if (value instanceof String)
                    sql.append("'"+ queryValue +"%'");
                else 
                    sql.append(queryValue);
                
                if (useUpper) sql.append(")");
            } else if (operator == Operator.TODAY.getIndex()) {
                sql.append(" = TODAY");
            } else if (operator == Operator.DAYS_BEFORE.getIndex()) {
                cal.setTime(new Date());
                Long days = (Long) entry.getValue();
                cal.add(Calendar.DATE, -1 * days.intValue());
                sql.append(" = '" + formatter.format(cal.getTime()) + "'");
            } else if (operator == Operator.DAYS_AFTER.getIndex()) {
                Long days = (Long) entry.getValue();
                cal.add(Calendar.DATE, days.intValue());
                sql.append(" = '" + formatter.format(cal.getTime()) + "'");
            } else if (operator == Operator.MONTHS_AGO.getIndex()) {
                Long days = (Long) entry.getValue();
                cal.add(Calendar.MONTH, -1 * days.intValue());
                cal.set(Calendar.DAY_OF_MONTH, 1);
                sql.append(" BETWEEN '" + formatter.format(cal.getTime()) + "'");
                cal.set(Calendar.DAY_OF_MONTH, cal.getMaximum(Calendar.DAY_OF_MONTH));
                sql.append(" AND '" + formatter.format(cal.getTime()) + "'");
            } else if (operator == Operator.YEARS_AGO.getIndex()) {
                Long days = (Long) entry.getValue();
                cal.add(Calendar.YEAR, -1 * days.intValue());
                cal.set(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                sql.append(" BETWEEN '" + formatter.format(cal.getTime()) + "'");
                cal.set(Calendar.MONTH, 12);
                cal.set(Calendar.DAY_OF_MONTH, 31);
                sql.append(" AND '" + formatter.format(cal.getTime()) + "'");
            }
            
            counter++;
        }
        
        if (childEntries.size() > 0) {
            DcModule childModule = DcModules.get(childEntries.get(0).getModule());
            
            DataFilter df = new DataFilter(childModule.getIndex());
            for (DataFilterEntry entry : childEntries)
                df.addEntry(entry);
            
            String subSelect = df.toSQL(new int[] {childModule.getParentReferenceFieldIndex()}, false, false);
            
            if (hasConditions)
                sql.append(" AND ID IN (");
            else 
                sql.append(" WHERE ID IN (");
            
            sql.append(subSelect);
            sql.append(")");
        }
        
        addLoanConditions(getEntries(), module, sql, hasConditions);
    }
    
    private void addOrderByClause(StringBuffer sql) {
        if (order != null && order.length > 0) {
            for (DcField orderOn : order) {

                // can happen; old configurations
                if (orderOn == null) continue;
            	
                if (orderOn.getFieldType() == ComponentFactory._REFERENCEFIELD ||
                    orderOn.getFieldType() == ComponentFactory._REFERENCESFIELD) {
                    
                    String column = orderOn.getFieldType() == ComponentFactory._REFERENCESFIELD ?
                            DcModules.get(orderOn.getModule()).getPersistentField(orderOn.getIndex()).getDatabaseFieldName() :
                            orderOn.getDatabaseFieldName();
                    
                	String referenceTableName = DcModules.get(orderOn.getReferenceIdx()).getTableName();
                    sql.append(" LEFT OUTER JOIN ");
                    sql.append(referenceTableName);
                    sql.append(" ON ");
                    sql.append(referenceTableName);
                    sql.append(".ID = ");
                    sql.append(column);
                } 
            }
        }
    }
    
    private void addOrderBy(StringBuffer sql) {
    	int counter = 0; 
        DcModule module = DcModules.get(getModule());
        DcModule referenceMod;
        DcField field = module.getField(module.getDefaultSortFieldIdx());
        if (order != null && order.length > 0) {
            for (DcField orderOn : order) {
                if (orderOn != null) {
                	sql.append(counter == 0 ? " ORDER BY " : ", ");
                	if (orderOn.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE ||
                	    orderOn.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {

                	    referenceMod = DcModules.get(orderOn.getReferenceIdx());
                        sql.append(referenceMod.getTableName());
                        sql.append(".");
                        sql.append(referenceMod.getField(referenceMod.getSystemDisplayFieldIdx()).getDatabaseFieldName());
                        sql.append(getSortOrder() == _SORTORDER_ASCENDING ? "" : " DESC");
                	} else if (!orderOn.isUiOnly() && orderOn.getDatabaseFieldName() != null) {
	                    sql.append(orderOn.getDatabaseFieldName());
	                    sql.append(getSortOrder() == _SORTORDER_ASCENDING ? "" : " DESC");
                	}
                	counter++;
                }
            }
            
        } else if (field != null && !field.isUiOnly()) {
            sql.append(" ORDER BY ");
            sql.append(module.getField(module.getDefaultSortFieldIdx()).getDatabaseFieldName());
        }
    }
    
    private void addLoanConditions(Collection<DataFilterEntry> entries, DcModule module, StringBuffer sql, boolean hasConditions) {
        
        Object person = null;
        Object duration = null;
        Object available = null;
        
        Object queryValue;
        
        for (DataFilterEntry entry : entries) {
            queryValue = Utilities.getQueryValue(entry.getValue(), DcModules.get(entry.getModule()).getField(entry.getField()));
            if (entry.getField() == DcObject._SYS_AVAILABLE)
                available = queryValue;
            if (entry.getField() == DcObject._SYS_LENDBY)
                person = queryValue;
            if (entry.getField() == DcObject._SYS_LOANDURATION)
                duration = queryValue;
        }
        
        if (available == null && person == null && duration == null)
            return;
        
        sql.append(hasConditions ? " AND " : " WHERE ");
        
        String maintable = module.getTableName();

        String current = formatter.format(new Date());
        String daysCondition = duration != null ? " AND DATEDIFF('dd', startDate , '" + current + "') >= " + duration : "";
        String personCondition = person != null ? " AND PersonID = '" + person + "'" : "";

        if (available != null && Boolean.valueOf(available.toString()))
            sql.append(" ID NOT IN (select objectID from Loans where objectID = " +  maintable
                       + ".ID AND enddate IS NULL AND startDate <= '" + current +  "')");
        else
            sql.append(" ID IN (select objectID from Loans where objectID = " +  maintable
                       + ".ID "  + daysCondition + " AND enddate IS NULL AND startDate <= '" + current +  "'" + personCondition + ")");
    }   

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : super.hashCode();
    }
    
    public boolean equals(DataFilter df) {
        return name != null && df.getName() != null ? name.equals(df.getName()) : df.getName() != null || name != null;
    }
}
