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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import net.datacrow.console.MainFrame;
import net.datacrow.console.components.IComponent;
import net.datacrow.console.views.MasterView;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.db.Query;
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
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * @author Robert Jan van der Waals        
 */ 
public class DataManager {

    private static Logger logger = Logger.getLogger(DataManager.class.getName());
    
    private static Map<Integer, Collection<IComponent>> listeners = 
        new HashMap<Integer, Collection<IComponent>>();
    
    /**
     * Creates the data manager and loads all items.
     */
    public DataManager() {}
    
    /**
     * Dispatch the items to the specified view.
     * @param master View The view to be updated.
     * @param items The items to be shown in the view.
     */
    public static void bindData(final MasterView masterView, final List<Long> keys) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                masterView.add(keys);                
            }
        });
    }    
    
    /**
     * Dispatch the items to the specified view.
     * @param master View The view to be updated.
     * @param module The module from which the items should be displayed.
     * @param df The data filter used to filter the items to be shown.
     */
    public static void bindData(MasterView masterView, int module, DataFilter df) {
        bindData(masterView, getKeys(module, df));
    }    
    
    public static int getCount(int module, int field, Object value) {
        int count = 0;
        
        try {
            DcModule m = DcModules.get(module);
            DcField f = field > 0 ? m.getField(field) : null;
            String sql;
            if (f == null) {
                sql = "select count(*) from " + m.getTableName();
            } else if (f.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                sql = "select count(*) from " + m.getTableName() + " where " + m.getField(field).getDatabaseFieldName() + " = ?";
            } else {
                m = DcModules.get(DcModules.getMappingModIdx(module, f.getReferenceIdx(), field));
                sql = "select count(*) from " + m.getTableName() + " where " + m.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + " = ?";
            }
                
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
            
            if (f != null) ps.setObject(1, value instanceof DcObject ? ((DcObject) value).getID() : value);
            
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
     * Update the specified cached item.
     * @param dco The item to update.
     * @param module The module to which the item belongs.
     */
    public static void update(DcObject dco, int module) {
        // get the real object
        DcObject o = getItem(dco.getModule().getIndex(), dco.getID());
        updateUiComponents(o.getModule().getIndex());
        updateView(o, 0, module, MainFrame._SEARCHTAB);
    }    
    
    /**
     * Adds the item to the cache.
     * @param dco The item to add.
     * @param module The module to which the item belongs.
     */
    public static void add(DcObject dco, int module) {
        dco.removeRequests();
        dco.setValidate(true);
        
        if (dco.getModule().canBeLend())
            dco.setValue(DcObject._SYS_AVAILABLE, Boolean.TRUE);

        updateUiComponents(dco.getModule().getIndex());
        if (DataCrow.mainFrame != null)
            updateView(dco, 1, module, MainFrame._INSERTTAB);
    }  
    
    public static boolean exists(DcObject o) {
        boolean exists = false;
        
        if (o.getModule().getIndex() == DcModules._PICTURE) {
            Long objectID = (Long) o.getValue(Picture._A_OBJECTID);
            if (objectID != null) {
                Collection<Picture> pictures = getPictures(objectID);
                exists = pictures.contains(o);
            }
        } else if (o.hasPrimaryKey()) {
            exists = getItem(o.getModule().getIndex(), o.getID()) != null;
        } else {
            logger.error("Cannot determine whether the item exists, not a picture and no ID", new Exception());
        }
        
        return exists;
    }
    
    /**
     * Remove the item from the cache.
     * @param dco The item to be removed.
     * @param module The module to which the item belongs.
     */
    public static void remove(DcObject dco, int module) {
        updateUiComponents(dco.getModule().getIndex());
        updateView(dco, 2, module, MainFrame._SEARCHTAB);
    }
    
    /**
     * Retrieves the children for the specified parent.
     * @param parentId The parent object ID.
     * @param childIdx The child module index.
     * @return The children or an empty collection.
     */
    public static Collection<DcObject> getChildren(Long parentId, int childIdx) {
        
        List<DcObject> c = new ArrayList<DcObject>(); // children.get(childIdx).get(parentId);
//        c = c == null ? new ArrayList<DcObject>() : new ArrayList<DcObject>(c);
//        children.get(childIdx).put(parentId, c);
//        
//        DcModule module =  DcModules.get(childIdx);
//        DataFilter filter = new DataFilter(childIdx);
//        filter.setOrder(new DcField[] {module.getField(module.getDefaultSortFieldIdx())});
//        
//        filter.sort(c);
        return c;
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
                    osh.clear(ref);
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
//        for (DcModule module : DcModules.getActualReferencingModules(item.getModule().getIndex())) {
//            if ( module.getIndex() != item.getModule().getIndex() && 
//                 module.getType() != DcModule._TYPE_TEMPLATE_MODULE) {
//                
//                for (DcField field : module.getFields()) {
//                    if (field.getReferenceIdx() == item.getModule().getIndex()) {
//                        DataFilter df = new DataFilter(module.getIndex());
//                        
//                        if (module.getType() == DcModule._TYPE_MAPPING_MODULE) {
//                            Collection<DcObject> c = new ArrayList<DcObject>();
//                            c.add(item);
//                            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, module.getIndex(), field.getIndex(), Operator.CONTAINS, c));
//                        } else {
//                            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, module.getIndex(), field.getIndex(), Operator.EQUAL_TO, item));
//                        }
//                        
//                        for (DcObject dco : DataManager.get(module.getIndex(), df)) {
//                            if (!items.contains(dco))
//                                items.add(dco);
//                        }
//                    }
//                }
//            }
//        }  
        
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
        
        Collection<DcObject> tabs = get(DcModules._TAB, df);
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
        List<DcObject> tabs = get(DcModules._TAB, df);
        return tabs != null && tabs.size() > 0 ? tabs.get(0) : null;
    }
    
    public static List<DcObject> getTabs(int module) {
        DataFilter df = new DataFilter(DcModules._TAB);
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._TAB, Tab._D_MODULE, Operator.EQUAL_TO, Long.valueOf(module)));
        return get(DcModules._TAB, df);
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
        
        Collection<DcMapping> mappings = (Collection<DcMapping>) parent.getValue(fieldIdx);
        mappings = mappings == null ? new ArrayList<DcMapping>() : mappings;
        
        // check if a mapping exists already
        for (DcMapping m : mappings) {
            if (m.getReferencedId().equals(child.getID()) || m.toString().equals(child.toString()))
                return;
        }
        
        mappings.add(mapping);
        parent.setValue(fieldIdx, mappings);
    }    
    
    /**
     * Update the view with the specified object.
     * @param dco The object used to update the view.
     * @param mode The mode used to update the view (@link {@link ViewUpdater}).
     * @param module The module to which the item belongs.
     * @param tab The tab to update. Either {@link MainFrame#_INSERTTAB} or {@link MainFrame#_SEARCHTAB}.
     */
    private static void updateView(final DcObject dco, final int mode, final int module, final int tab) {
        // Thread-safe view update
        ViewUpdater updater = new ViewUpdater(dco, module, tab, mode);
        if (!SwingUtilities.isEventDispatchThread())
            SwingUtilities.invokeLater(updater);
        else
            updater.run();
    }
    
    /**
     * Item count.
     * @param module
     * @param df
     * 
     * TODO: rewrite
     */
    public static int contains(int module, DataFilter df) {
        List<DcObject> objects = get(module, df);
        return objects != null ? objects.size() : 0;
    }

    /**
     * Retrieves all the loans (actual and historic).
     * @param parentID The item ID for which the loans are retrieved.
     * @return A collection holding loans or an empty collection.
     */
    public static Collection<Loan> getLoans(Long parentID) {
        return new ArrayList<Loan>();
    }
    
    /**
     * Retrieves the actual loan.
     * @param parentID The item ID for which the loan is retrieved.
     */
    public static Loan getCurrentLoan(Long parentID) {
//        Collection<Loan> loans = getLoans(parentID);
//        for (Loan loan : new ArrayList<Loan>(loans)) {
//            if (loan.getValue(Loan._B_ENDDATE) == null)
//                return loan;
//        }
//        
//        Loan loan = (Loan) DcModules.get(DcModules._LOAN).getItem();
//        loans.add(loan);
        return new Loan();
    }
    
    public static DcObject getExternalReference(int moduleIdx, String ID) {
        DcModule module = DcModules.get(moduleIdx);
        String sql = "SELECT ID FROM " + module.getTableName() + " WHERE " +
             module.getField(ExternalReference._EXTERNAL_ID).getDatabaseFieldName() + " = ?";
        
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, ID);
            List<DcObject> items = DatabaseManager.retrieveItems(ps, Query._SELECT);
            return items.size() > 0 ? items.get(0) : null;
        } catch (SQLException se) {
            logger.error(se, se);
        }
        
        return null;
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
                List<DcObject> items = DatabaseManager.retrieveItems(ps2, Query._SELECT);
                
                if (items.size() > 0) {
                    result = items.get(0);
                    break;
                }
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
            String query = "SELECT * FROM " + module.getTableName() + " WHERE " + 
                "UPPER(" + module.getField(module.getSystemDisplayFieldIdx()).getDatabaseFieldName() + ") =  UPPER(?)";
            
            if (module.getType() == DcModule._TYPE_PROPERTY_MODULE)
                query += " OR UPPER(" + module.getField(DcProperty._C_ALTERNATIVE_NAMES).getDatabaseFieldName() + ") LIKE ?"; 
            
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(query);

            ps.setString(1, s);
            if (module.getType() == DcModule._TYPE_PROPERTY_MODULE)
                ps.setString(2,  ";%" + s.toUpperCase() + "%;");
            
            List<DcObject> items = DatabaseManager.retrieveItems(ps, Query._SELECT);
            return items.size() > 0 ? items.get(0) : null;
            
        } catch (SQLException e) {
            logger.error(e, e);
        }
        
        return null;
    }    
    
    /**
     * Retrieve the item based on its ID.
     * @param module
     * @param ID
     * @return null or the item if found.
     */
    public static DcObject getItem(int module, Long ID) {
        String sql = "SELECT * FROM " + DcModules.get(module).getTableName() + " WHERE ID = " + ID;
        List<DcObject> items = DatabaseManager.retrieveItems(sql, Query._SELECT);
        return items != null && items.size() > 0 ? items.get(0) : null;
    }    
  
    /**
     * Retrieve all referenced items for the given parent ID.
     * @param module
     * @param parentId
     */
    public static Collection<DcObject> getReferences(int modIdx, Long parentID) {
        DcModule module = DcModules.get(modIdx);
        String sql = "SELECT * FROM " + module.getTableName() + " WHERE " + 
                     module.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() + " = " + parentID;

        return DatabaseManager.retrieveItems(sql, Query._SELECT);
    }

    /**
     * Retrieves all pictures for the given parent ID. 
     * @param parentId
     * @return Either the pictures or an empty collection.
     */
    public static Collection<Picture> getPictures(Long parentID) {
        DcModule module = DcModules.get(DcModules._PICTURE);
        String sql = "SELECT * FROM " + module.getTableName() + " WHERE " + 
                     module.getField(Picture._A_OBJECTID).getDatabaseFieldName() + " = " + parentID;
        
        List<Picture> pictures = new ArrayList<Picture>();
        List<DcObject> items = DatabaseManager.retrieveItems(sql, Query._SELECT);
        
        for (DcObject dco : items) pictures.add((Picture) dco);
        
        return pictures;
    }
    
    public static List<Long> getKeys(int module, DataFilter filter) {
        return DatabaseManager.getKeys(module, filter);
    }
    
    
    /**
     * Retrieve items using the specified data filter.
     * @see DataFilter
     * @param modIdx
     * @param filter
     */
    public static List<DcObject> get(int modIdx, DataFilter filter) {
        try {
            if (filter != null) {
                Query query = new Query(filter, null, null);
                return DatabaseManager.retrieveItems(query);
            } else {
                return DatabaseManager.retrieveItems(DcModules.get(modIdx).getItem());
            }
        } catch (SQLException se) {
            logger.error("Error while querying for item", se);
            return new ArrayList<DcObject>();
        }
    }
    
    private static class ViewUpdater implements Runnable {
        
        private DcObject dco;
        private int module;
        private int tab;
        private int mode;
        
        public ViewUpdater(DcObject dco, int module, int tab, int mode) {
            this.dco = dco;
            this.module = module;
            this.tab = tab;
            this.mode = mode;
        }
        
        public void run() {
            DcModule m = DcModules.get(module);
            if (tab == MainFrame._INSERTTAB) {
                if (mode == 0) {
                    m.getCurrentInsertView().updateItem(dco.getID(), dco);
                } else if (mode == 1) {
                    
                    if (DcModules.get(module).getSearchView() != null) {
                        
                        DcModules.get(module).getSearchView().add(dco);
                        
                        if (!DcModules.get(module).isAbstract() && dco.getModule().isTopModule()) {

                            if (dco.getModule().getType() == DcModule._TYPE_MEDIA_MODULE)
                                DcModules.get(DcModules._MEDIA).getSearchView().add(dco);

                            DcModules.get(DcModules._ITEM).getSearchView().add(dco);
                        }
                    }
                    
//                    if (DcModules.get(module).getInsertView() != null)
//                        DcModules.get(module).getInsertView().removeItems(new Long[] {dco.getID()});
                }
            } else if (tab == MainFrame._SEARCHTAB) {
                
                Collection<DcModule> modules = new ArrayList<DcModule>();
                modules.add(m);
                
                if (!m.isAbstract()) {
                    if (m.getType() == DcModule._TYPE_MEDIA_MODULE)
                        modules.add(DcModules.get(DcModules._MEDIA));
                    
                    if (m.isContainerManaged())
                        modules.add(DcModules.get(DcModules._CONTAINER));
                }
                
                if (m.isAbstract())
                    modules.add(DcModules.get(dco.getModule().getIndex()));
                
                for (DcModule mod : modules) {
                    try {
                        MasterView masterView = mod.getSearchView();
                        if (masterView != null) {
                            if (mode == 0)
                                masterView.updateItem(dco.getID(), dco);
                            if (mode == 1)
                                masterView.add(dco);
//                            if (mode == 2)
//                                masterView.removeItems(new Long[] {dco.getID()});
                        }
                    } catch (Exception exp) {
                        logger.error("Error while updating view for module " + mod.getLabel(), exp);
                    }
                }
            }
            
            if (mode == 0) {
                // after an update make sure that the quick view of the main item is updated with
                // the changed information (as well as the grouping pane).
                if (dco.getModule().getParent() != null) {
                    if (dco.getModule().getParent().getSearchView() != null) {
                        dco.getModule().getParent().getSearchView().refreshQuickView();
                        if (dco.getModule().getParent().getSearchView().getGroupingPane() != null) {
                            dco.getModule().getParent().getSearchView().getGroupingPane().revalidate();
                            dco.getModule().getParent().getSearchView().getGroupingPane().repaint();
                        }
                    }
                }
                
                if (dco.getModule().hasDependingModules()) {
                    for (DcModule module : DcModules.getActualReferencingModules(dco.getModule().getIndex())) {
                        if (module.isValid() && module.isEnabled() && module.getSearchView() != null) {
                            module.getSearchView().refreshQuickView();
                            if (module.getSearchView().getGroupingPane() != null) {
                                module.getSearchView().getGroupingPane().revalidate();
                                module.getSearchView().getGroupingPane().repaint();
                            }
                        }
                    }
                }
            }
            dco = null;
        }
    }
    
    /**
     * Updates components like list boxes holding the module items.
     * @param module The module for which the registered listeners should be updated.
     */
    private static void updateUiComponents(int module) {
        Collection<IComponent> components = listeners.get(module);
        
        if (components == null) return;

        for (IComponent c : components)
            c.refresh();
    }
    
    /**
     * Register a component. The registered component will be updated with the module items.
     * Changed, removal and additions will be reflected on the registered components. 
     * @param component
     * @param module
     */
    public static void registerUiComponent(IComponent component, int module) {
        Collection<IComponent> c = listeners.get(module);
        c = c == null ? new ArrayList<IComponent>() : c;
        c.add(component);
        listeners.put(module, c);
        component.refresh();
    }
    
    public static void unregisterUiComponent(IComponent c) {
        for (Collection<IComponent> components : listeners.values())
            components.remove(c);
    }    
}
