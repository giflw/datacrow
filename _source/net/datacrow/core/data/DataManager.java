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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.db.SelectQuery;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcAssociate;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcProperty;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.Tab;
import net.datacrow.core.objects.helpers.ExternalReference;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.OnlineSearchHelper;
import net.datacrow.core.services.SearchTask;
import net.datacrow.core.wf.WorkFlow;
import net.datacrow.settings.definitions.WebFieldDefinition;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.StringUtils;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * @author Robert Jan van der Waals        
 */ 
public class DataManager {

    private static Logger logger = Logger.getLogger(DataManager.class.getName());
    
    public static int getCount(int module, int field, Object value) {
        int count = 0;
        
        try {
            DcModule m = DcModules.get(module);
            DcField f = field > 0 ? m.getField(field) : null;
            String sql;
            if (f == null) {
                sql = "select count(*) from " + m.getTableName();
            } else if (f.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                sql = "select count(*) from " + m.getTableName() + " where " + m.getField(field).getDatabaseFieldName() + 
                      (value == null ? " IS NULL " : " = ?");
            } else {
                if (value != null) {
                	m = DcModules.get(DcModules.getMappingModIdx(module, f.getReferenceIdx(), field));
                	sql = "select count(*) from " + m.getTableName() + " where " + m.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + " = ?";
                } else { 
                	DcModule mapping = DcModules.get(DcModules.getMappingModIdx(module, f.getReferenceIdx(), field));
                	sql = "select count(*) from " + m.getTableName() + " MAINTABLE where not exists (select " + 
                	      mapping.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() + " from " + mapping.getTableName() + " where " +
                	      mapping.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() + " = MAINTABLE.ID)"; 
                }
            }
                
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
            
            if (f != null && value != null) ps.setObject(1, value instanceof DcObject ? ((DcObject) value).getID() : value);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                count = rs.getInt(1);
            
            rs.close();
        } catch (Exception e) {
            logger.error(e, e);
        }
        return count;
    }

    /**
     * Specifically created for the web interface. 
     * Returns the entire result set as a flat string structure.
     * 
     * @param df
     * @param fields
     * @param definitions
     * @return
     */
    public static List<List<String>> getValues(DataFilter df, int[] fields, List<WebFieldDefinition> definitions) {
    	
    	List<List<String>> result = new ArrayList<List<String>>();
    	
    	ResultSet rs = null;
    	DcField field;
    	
    	try {
    		
	    	String sql = df.toSQLFlatStructure(fields);
	    	rs = DatabaseManager.executeSQL(sql);
	    	List<String> values;
	    	
	    	int maxLength;
	    	String ID;
	    	String value;
	    	String previousID = null;
	    	boolean concat = false;
	    	
	    	DcModule module = DcModules.get(df.getModule());
	    	DcObject template = module.getItem();
	    		
	    	while(rs.next()) {
	    		values = new ArrayList<String>();
	    		ID = rs.getString("ID");

	    		// concatenate previous result set (needed for multiple references)
	    		if (ID.equals(previousID)) {
	    			values = result.get(result.size() - 1);
	    			concat = true;
	    		}
	    		
	    		for (int i = 0; i < fields.length; i++) {
	    			field = module.getField(fields[i]);

    				if (!field.isUiOnly() && 
    					field.getValueType() != DcRepository.ValueTypes._STRING &&
    					field.getValueType() != DcRepository.ValueTypes._DCOBJECTREFERENCE) {

    					template.setValue(field.getIndex(), rs.getObject(i+1));
    					value = template.getDisplayString(field.getIndex());
	    			} else {
    					value = rs.getString(i + 1);
    				}

	    			if (!concat) {
		    			maxLength = fields[i] != DcObject._ID ? definitions.get(i).getMaxTextLength() : 0;
		    			value = value == null ? "" : StringUtils.concatUserFriendly(value, maxLength);
		    			values.add(value);
	    			} else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
	    				value = values.get(i) + ", " + value;
	    				values.set(i, value);
	    			}
	    		}

	    		previousID = ID;
	    		result.add(values);
	    		concat = false;
	    	}
    	} catch (Exception e) {
    	    logger.error("An error occurred while building the String result set", e);
    	}
        
    	if (rs != null) {
    		try {
				rs.close();
			} catch (SQLException e) {
	            logger.error("An error occurred while closing the result set", e);
			}
    	}  
    	
    	return result;
    }
    
    /**
     * Retrieves the children for the specified parent.
     * @param parentId The parent object ID.
     * @param childIdx The child module index.
     * @return The children or an empty collection.
     */
    public static List<DcObject> getChildren(String parentID, int childIdx, int[] fields) {
        DataFilter df = new DataFilter(childIdx);
        DcModule module = DcModules.get(childIdx);
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, childIdx, module.getParentReferenceFieldIndex(), Operator.EQUAL_TO, parentID));
        return new SelectQuery(df, null, fields).run();
    }
    
    /**
     * Retrieves the children for the specified parent.
     * @param parentId The parent object ID.
     * @param childIdx The child module index.
     * @return The children or an empty collection.
     */
    public static Map<String, Integer> getChildrenKeys(String parentID, int childIdx) {
        DataFilter df = new DataFilter(childIdx);
        DcModule module = DcModules.get(childIdx);
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, childIdx, module.getParentReferenceFieldIndex(), Operator.EQUAL_TO, parentID));
        return getKeys(df);
    }
    
    
    /**
     * Creates a reference to the specified object. The provided value can either
     * be a DcObject or a display string. In the latter case the display string will
     * be used to retrieve the DcObject. If no object is found it will be created and
     * saved. The online search is used to retrieve additional information.
     *  
     * @param dco The item to which the reference will be created.
     * @param fieldIdx The field index to set the reference on.
     * @param value The referenced value.
     * @return If an object has been created for the specified value this object will be
     * returned. Else null will be returned.
     */
    public static DcObject createReference(DcObject dco, int fieldIdx, Object value) {
        String name = value != null ? value instanceof String ? (String) value : value.toString() : null;
        
        if (Utilities.isEmpty(name)) return null;
        
        // method 1: item is provided and exists
        int moduleIdx = DcModules.getReferencedModule(dco.getField(fieldIdx)).getIndex();
        DcModule module = DcModules.get(moduleIdx);
        DcObject ref = value instanceof DcObject ? (DcObject) value : null;

        // check if we are dealing with an external reference
        if (ref == null && module.getType() == DcModule._TYPE_EXTERNALREFERENCE_MODULE) {

            ref = getExternalReference(moduleIdx, name); 
        
        } else if (ref == null && module.getType() != DcModule._TYPE_EXTERNALREFERENCE_MODULE) {
            
            // method 2: simple external reference + display value comparison
            ref = DataManager.getObjectForString(moduleIdx, name);
    
            if (ref == null && fieldIdx != DcObject._SYS_EXTERNAL_REFERENCES) {
                ref = module.getItem();
                
                boolean onlinesearch = false;
                if (module.getType() == DcModule._TYPE_ASSOCIATE_MODULE) {
                    ref.setValue(DcAssociate._A_NAME, name);
                    onlinesearch = ref.getModule().deliversOnlineService() &&
                                   dco.getModule().getSettings().getBoolean(DcRepository.ModuleSettings.stOnlineSearchSubItems);  
                } else {
                    ref.setValue(ref.getSystemDisplayFieldIdx(), name);
                }
                
                if (onlinesearch) {
                    OnlineSearchHelper osh = new OnlineSearchHelper(moduleIdx, SearchTask._ITEM_MODE_FULL);
                    DcObject queried = osh.query(ref, name, new int[] {module.getSystemDisplayFieldIdx()});
                    ref = queried != null ? queried : ref;
                    osh.clear();
                }
                
                ref.setIDs();
            }
        }
        
        if (ref != null) {
            if (dco.getField(fieldIdx).getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION)
                DataManager.addMapping(dco, ref, fieldIdx);
            else
                dco.setValue(fieldIdx, ref);
        }
        
        return ref;
    }
    
    public static List<DcObject> getReferencingItems(DcObject item) {
        List<DcObject> items = new ArrayList<DcObject>();
        
        for (DcModule module : DcModules.getActualReferencingModules(item.getModule().getIndex())) {
            if ( module.getIndex() != item.getModule().getIndex() && 
                 module.getType() != DcModule._TYPE_MAPPING_MODULE &&   
                 module.getType() != DcModule._TYPE_TEMPLATE_MODULE) {
                
                for (DcField field : module.getFields()) {
                    if (field.getReferenceIdx() == item.getModule().getIndex()) {
                        DataFilter df = new DataFilter(module.getIndex());
                        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, module.getIndex(), field.getIndex(), Operator.EQUAL_TO, item));
                        
                        for (DcObject dco : DataManager.get(df, module.getMinimalFields(null))) {
                            if (!items.contains(dco))
                                items.add(dco);
                        }
                    }
                }
            }
        }  
        
        return items;
    }    
    
    /**
     * Retrieves the tab. In case it does not yet exists the tab is created and stored
     * to the database.
     * 
     * @param module
     * @param name
     * @param create
     * 
     * @return Existing or newly created tab
     */
    public static boolean checkTab(int module, String name) {
        boolean exists = true;

        DataFilter df = new DataFilter(DcModules._TAB);
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._TAB, Tab._D_MODULE, Operator.EQUAL_TO, Long.valueOf(module)));
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._TAB, Tab._A_NAME, Operator.EQUAL_TO, name));
        
        Collection<String> tabs = getKeyList(df);
        if (tabs.size() == 0) {
            try {
                Tab tab = (Tab) DcModules.get(DcModules._TAB).getItem();
                tab.setIDs();
                tab.setValue(Tab._A_NAME, name);
                tab.setValue(Tab._D_MODULE, Long.valueOf(module));
                
                int order = name.equals(DcResources.getText("lblInformation")) ? 1 : 
                            name.equals(DcResources.getText("lblSummary")) ? 0 : 
                            2;
                
                tab.setValue(Tab._C_ORDER, Long.valueOf(order));
                
                if (name.equalsIgnoreCase(DcResources.getText("lblInformation")) || name.equals(DcResources.getText("lblSummary")))
                    tab.setValue(Tab._B_ICON, new DcImageIcon(DataCrow.installationDir + "icons" + File.separator + "information.png"));
                else if (name.equalsIgnoreCase(DcResources.getText("lblTechnicalInfo")))
                    tab.setValue(Tab._B_ICON, new DcImageIcon(DataCrow.installationDir + "icons" + File.separator + "informationtechnical.png"));
                
                tab.saveNew(false);
            } catch (Exception e) {
                logger.error(e, e);
                exists = false;
            }
        }
        
        return exists; 
    }
    
    public static DcObject getTab(int module, String name) {
        DataFilter df = new DataFilter(DcModules._TAB);
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._TAB, Tab._D_MODULE, Operator.EQUAL_TO, Long.valueOf(module)));
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._TAB, Tab._A_NAME, Operator.EQUAL_TO, name));
        List<DcObject> tabs = get(df);
        return tabs != null && tabs.size() > 0 ? tabs.get(0) : null;
    }
    
    public static List<DcObject> getTabs(int module) {
        DataFilter df = new DataFilter(DcModules._TAB);
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._TAB, Tab._D_MODULE, Operator.EQUAL_TO, Long.valueOf(module)));
        return get(df);
    }
    
    /**
     * Adds a referenced item to the specified parent object.
     * @param parent The item to which the reference will be added.
     * @param child The to be referenced item.
     * @param fieldIdx The field holding the reference.
     */
    @SuppressWarnings("unchecked")
    public static void addMapping(DcObject parent, DcObject child, int fieldIdx) {
        DcMapping mapping = (DcMapping) DcModules.get(DcModules.getMappingModIdx(parent.getModule().getIndex(), child.getModule().getIndex(), fieldIdx)).getItem();
        mapping.setValue(DcMapping._A_PARENT_ID, parent.getID());
        mapping.setValue(DcMapping._B_REFERENCED_ID, child.getID());
        mapping.setReference(child);
        
        Collection<DcMapping> mappings = (Collection<DcMapping>) parent.getValue(fieldIdx);
        mappings = mappings == null ? new ArrayList<DcMapping>() : mappings;
        
        // check if a mapping exists already
        for (DcMapping m : mappings) {
            if (m.getReferencedID().equals(child.getID()) || m.toString().equals(child.toString()))
                return;
        }
        
        mappings.add(mapping);
        parent.setValue(fieldIdx, mappings);
    }    
    
    /**
     * Retrieves all the loans (actual and historic).
     * @param parentID The item ID for which the loans are retrieved.
     * @return A collection holding loans or an empty collection.
     */
    public static Collection<Loan> getLoans(String parentID) {
        DataFilter df = new DataFilter(DcModules._LOAN);
        df.addEntry(new DataFilterEntry(DcModules._LOAN, Loan._D_OBJECTID, Operator.EQUAL_TO, parentID));
        Collection<DcObject> items = get(df, DcModules.get(DcModules._LOAN).getMinimalFields(null));
        
        Collection<Loan> loans = new ArrayList<Loan>();
        for (DcObject item : items)
            loans.add((Loan) item);
        
        return loans;
    }
    
    /**
     * Retrieves the actual loan.
     * @param parentID The item ID for which the loan is retrieved.
     */
    public static Loan getCurrentLoan(String parentID) {
        DataFilter df = new DataFilter(DcModules._LOAN);
        df.addEntry(new DataFilterEntry(DcModules._LOAN, Loan._B_ENDDATE, Operator.IS_EMPTY, null));
        df.addEntry(new DataFilterEntry(DcModules._LOAN, Loan._D_OBJECTID, Operator.EQUAL_TO, parentID));
        List<DcObject> items = get(df);
        return items.size() > 0 ? (Loan) items.get(0) : new Loan();
    }
    
    public static DcObject getExternalReference(int moduleIdx, String type) {
        DataFilter df = new DataFilter(moduleIdx);
        df.addEntry(new DataFilterEntry(moduleIdx, ExternalReference._EXTERNAL_ID, Operator.EQUAL_TO, type));
        List<DcObject> items = get(df, new int[] {DcObject._ID});
        return items.size() > 0 ? items.get(0) : null;    
    }    
    
    public static DcObject getObjectByExternalID(int moduleIdx, String type, String externalID) {
        DcModule module =  DcModules.get(moduleIdx);
       
        if (module.getField(DcObject._SYS_EXTERNAL_REFERENCES) == null) return null;
        
        DcModule extRefModule =  DcModules.get(moduleIdx + DcModules._EXTERNALREFERENCE);
        String sql = "SELECT ID FROM " + extRefModule.getTableName() + " WHERE " +
            "UPPER(" + extRefModule.getField(ExternalReference._EXTERNAL_ID).getDatabaseFieldName() + ") = UPPER(?) AND " +
            "UPPER(" + extRefModule.getField(ExternalReference._EXTERNAL_ID_TYPE).getDatabaseFieldName() + ") = UPPER(?)";
        
        Connection conn = DatabaseManager.getConnection();
        DcObject result = null;
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, externalID);
            ps.setString(2, type);
            
            ResultSet rs = ps.executeQuery();
            String referenceID;
            while (rs.next()) {
                referenceID = rs.getString(1);
                
                int idx = DcModules.getMappingModIdx(extRefModule.getIndex() - DcModules._EXTERNALREFERENCE, extRefModule.getIndex(), DcObject._SYS_EXTERNAL_REFERENCES);
                DcModule mappingMod = DcModules.get(idx);
                sql = "SELECT * FROM " + DcModules.get(moduleIdx) + " WHERE ID IN (" +
                	  "SELECT OBJECTID FROM " + mappingMod.getTableName() + 
                      " WHERE " + mappingMod.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + " = ?)";
    
                PreparedStatement ps2 = conn.prepareStatement(sql);
                ps2.setString(1, referenceID);
                
                List<DcObject> items = WorkFlow.getInstance().convert(ps2.executeQuery(), new int[] {DcObject._ID});
                result = items.size() > 0 ? items.get(0) : null;
            }
        
            ps.close();
            rs.close();
            conn.close();
        } catch (SQLException se) {
            logger.error(se, se);
        }
        return result;
    }
    
    public static DcObject getObjectForString(int module, String reference) {
        DcObject dco = getObjectByExternalID(module, DcRepository.ExternalReferences._PDCR, reference); 
        dco = dco == null ? getObjectForDisplayValue(module, reference) : dco;
        return dco;
    }
    
    /**
     * Retrieves an item based on its display value.
     * @param module
     * @param s The display value.
     * @return Either the item or null. 
     */
    private static DcObject getObjectForDisplayValue(int moduleIdx, String s) {
        DcModule module = DcModules.get(moduleIdx);

        try {
        	String columns = module.getIndex() + " AS MODULEIDX";
        	for (DcField field : module.getFields()) {
        		if (!field.isUiOnly())
        			columns += "," + field.getDatabaseFieldName();
        	}
        	
            String query = "SELECT " + columns + " FROM " + module.getTableName() + " WHERE " + 
                "UPPER(" + module.getField(module.getSystemDisplayFieldIdx()).getDatabaseFieldName() + ") =  UPPER(?)";
            
            if (module.getType() == DcModule._TYPE_PROPERTY_MODULE)
                query += " OR UPPER(" + module.getField(DcProperty._C_ALTERNATIVE_NAMES).getDatabaseFieldName() + ") LIKE ?"; 
            
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(query);

            ps.setString(1, s);
            if (module.getType() == DcModule._TYPE_PROPERTY_MODULE)
                ps.setString(2,  ";%" + s.toUpperCase() + "%;");
            
            List<DcObject> items = WorkFlow.getInstance().convert(ps.executeQuery(), new int[] {DcObject._ID});
            return items.size() > 0 ? items.get(0) : null;
            
        } catch (SQLException e) {
            logger.error(e, e);
        }
        
        return null;
    }    
    
    public static DcObject getItem(int module, String ID) {
        return getItem(module, ID, null);
    }  
    
    /**
     * Retrieve the item based on its ID.
     * @param module
     * @param ID
     * @return null or the item if found.
     */
    public static DcObject getItem(int module, String ID, int[] fields) {
        DataFilter df = new DataFilter(module);
        df.addEntry(new DataFilterEntry(module, DcObject._ID, Operator.EQUAL_TO, ID));
        List<DcObject> items = get(df, fields);
        DcObject item = items != null && items.size() > 0 ? items.get(0) : null;
        if (item != null) item.markAsUnchanged();
        return item;
    }    
  
    /**
     * Retrieve all referenced items for the given parent ID.
     * @param module
     * @param parentId
     */
    public static Collection<DcObject> getReferences(int modIdx, String parentID, boolean full) {
        DataFilter df = new DataFilter(modIdx);
        df.addEntry(new DataFilterEntry(modIdx, DcMapping._A_PARENT_ID, Operator.EQUAL_TO, parentID));
        return get(df, full ? null : DcModules.get(modIdx).getMinimalFields(null));
    }

    /**
     * Retrieves all pictures for the given parent ID. 
     * @param parentId
     * @return Either the pictures or an empty collection.
     */
    public static Collection<DcObject> getPictures(String parentID) {
        DataFilter df = new DataFilter(DcModules._PICTURE);
        df.addEntry(new DataFilterEntry(DcModules._PICTURE, Picture._A_OBJECTID, Operator.EQUAL_TO, parentID));
        return new SelectQuery(df, null, null).run();
    }
    
    
    public static List<String> getKeyList(DataFilter filter) {
        return new ArrayList<String>(DatabaseManager.getKeys(filter).keySet());
    }
    
    public static Map<String, Integer> getKeys(DataFilter filter) {
        return DatabaseManager.getKeys(filter);
    }
    
    /**
     * Retrieve items using the specified data filter.
     * @see DataFilter
     * @param filter
     * @param fields 
     */
    public static List<DcObject> get(DataFilter filter, int[] fields) {
        return new SelectQuery(filter, null, fields).run();
    }

    /** 
     * Overloaded 
     * @see #get(DataFilter, int[])
     */
    public static List<DcObject> get(int modIdx, int[] fields) {
        return get(new DataFilter(modIdx), fields);
    }
    
    /** 
     * Overloaded 
     * @see #get(DataFilter, int[])
     */
    public static List<DcObject> get(DataFilter filter) {
        return get(filter, null);
    }
}
