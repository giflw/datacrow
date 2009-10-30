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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import net.datacrow.core.modules.DcMediaModule;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.IChildModule;
import net.datacrow.core.modules.MappingModule;
import net.datacrow.core.objects.DcAssociate;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcMediaObject;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcProperty;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.Tab;
import net.datacrow.core.objects.helpers.ExternalReference;
import net.datacrow.core.objects.template.Templates;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.OnlineSearchHelper;
import net.datacrow.core.services.SearchTask;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * This is the persistence layer of Data Crow. All items are cached and stored in several
 * data structures for quick retrieval. Large data items such as images are only loaded
 * when needed and are not cached here. Only the reference to the image is stored. 
 * 
 * Data present represents the HSQL database information. Referential integrity is forced
 * by the database and further guaranteed by the Query class.
 * 
 * NOTE: There is no 'smart caching' mechanism implemented as the data sets stored in 
 *       Data Crow are relatively small.
 *       
 * @author Robert Jan van der Waals        
 */ 
public class DataManager {

    private static Logger logger = Logger.getLogger(DataManager.class.getName());
    
    private static boolean saveCache = true;
    
    // objects per module
    private static Map<Integer, List<DcObject>> objects = 
        new HashMap<Integer, List<DcObject>>();
    
    private static Map<Integer, Map<String, DcObject>> objectsByID = 
        new HashMap<Integer, Map<String, DcObject>>();
    
    // all loans by DcObject ID
    private static Map<String, List<Loan>> loans = 
        new HashMap<String, List<Loan>>();
    
    // all pictures by DcObject ID
    private static Map<String, List<Picture>> pictures = 
        new HashMap<String, List<Picture>>();

    // all references by module and DcObject ID
    private static Map<Integer, Map<String, Collection<DcMapping>>> references = 
        new HashMap<Integer, Map<String, Collection<DcMapping>>>();

    // all references by module and DcObject ID
    private static Map<Integer, Map<String, List<DcObject>>> children = 
        new HashMap<Integer, Map<String, List<DcObject>>>();
    
    private static Map<Integer, Collection<IComponent>> listeners = 
        new HashMap<Integer, Collection<IComponent>>();
    
    private static boolean initialized = false;
    
    private static boolean useCache = true;

    /**
     * Creates the data manager and loads all items.
     */
    public DataManager() {
        initialized = false;
        initialize();
        initialized = true;
    }
    
    /**
     * Indicates if the items have beem loaded.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Indicates if the cache should be used when loading the items.
     * If set to false the items will be loaded directly from the database.
     * @param b
     */
    public static void setUseCache(boolean b) {
        useCache = b;
    }
    
    /**
     * Clears the items.
     */
    public static void unload() {
        objects.clear();
        loans.clear();
        pictures.clear();
        references.clear();
        children.clear();
    }

    /**
     * Dispatch the items to the specified view.
     * @param master View The view to be updated.
     * @param module The module from which the items should be displayed.
     * @param df The data filter used to filter the items to be shown.
     */
    public static void bindData(MasterView masterView, int module, DataFilter df) {
        bindData(masterView, get(module, df));
    }    

    /**
     * Dispatch the items to the specified view.
     * @param master View The view to be updated.
     * @param items The items to be shown in the view.
     */
    public static void bindData(final MasterView masterView, final DcObject[] items) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                masterView.bindData(items);                
            }
        });
    }
    
    /**
     * Update the specified cached item.
     * @param dco The item to update.
     * @param module The module to which the item belongs.
     */
    public static void update(DcObject dco, int module) {
        // get the real object
        DcObject o = dco instanceof Loan ? dco : getObject(dco.getModule().getIndex(), dco.getID());
        updatePictures(dco);
        updateRelated(dco, false);

        o.removeRequests();
        o.reload();
        o.initializeReferences();
        o.setValidate(true);
        
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
        
        updatePictures(dco);
        
        String key = getKey(dco);
        if (module == DcModules._LOAN) {
            List<Loan> c = loans.get(key);
            c = c == null ? new ArrayList<Loan>() : c;
            c.add((Loan) dco);
            loans.put(key, c);
        } else if (objects.containsKey(dco.getModule().getIndex())) {
            Collection<DcObject> c = objects.get(dco.getModule().getIndex());
            c.add(dco);
            
            Map<String, DcObject> map = objectsByID.get(dco.getModule().getIndex());
            map = map == null ? new HashMap<String, DcObject>() : map;
            map.put(dco.getID(), dco);
            
        } else if (children.containsKey(dco.getModule().getIndex())) {
            List<DcObject> c = children.get(dco.getModule().getIndex()).get(key);
            c = c == null ? new ArrayList<DcObject>() : c;
            c.add(dco);
            children.get(dco.getModule().getIndex()).put(key, c);
            
            Map<String, DcObject> map = objectsByID.get(dco.getModule().getIndex());
            map = map == null ? new HashMap<String, DcObject>() : map;
            map.put(dco.getID(), dco);
            
        } else if (module == DcModules._PICTURE) {
            List<Picture> c = pictures.get(getKey(dco));
            c = c == null ? new ArrayList<Picture>() : c;
            c.add((Picture) dco);
            pictures.put(key, c);
        } else if (references.containsKey(dco.getModule().getIndex())) {
            Collection<DcMapping> c = references.get(dco.getModule().getIndex()).get(getKey(dco));
            c = c == null ? new ArrayList<DcMapping>() : c;
            c.add((DcMapping) dco);
            references.get(dco.getModule().getIndex()).put(key, c);
        }
        
        if (dco.getModule().canBeLend())
            dco.setValue(DcObject._SYS_AVAILABLE, Boolean.TRUE);

        updateRelated(dco, false);
        updateUiComponents(dco.getModule().getIndex());
        
        if (DataCrow.mainFrame != null)
            updateView(dco, 1, module, MainFrame._INSERTTAB);
    }  
    
    /**
     * Remove the item from the cache.
     * @param dco The item to be removed.
     * @param module The module to which the item belongs.
     */
    public static void remove(DcObject dco, int module) {
    	if (module == DcModules._LOAN) {
            Collection<Loan> c = loans.get(getKey(dco));
            if (c != null) c.remove(dco);
    	} else if (objects.containsKey(dco.getModule().getIndex())) {
            Collection<DcObject> c = objects.get(dco.getModule().getIndex());
            if (c != null) c.remove(dco);
            
            Map<String, DcObject> map = objectsByID.get(dco.getModule().getIndex());
            if (map != null) map.remove(dco.getID());
        } else if (children.containsKey(dco.getModule().getIndex())) {
            List<DcObject> c = children.get(dco.getModule().getIndex()).get(dco.getValue(dco.getParentReferenceFieldIndex()));
            if (c != null) c.remove(dco);
            
            Map<String, DcObject> map = objectsByID.get(dco.getModule().getIndex());
            if (map != null) map.remove(dco.getID());
        } else if (module == DcModules._PICTURE) {
            Collection<Picture> c = pictures.get(getKey(dco));
            if (c != null) c.remove(dco);
        }
        
        updateRelated(dco, true);
        updateUiComponents(dco.getModule().getIndex());
        updateView(dco, 2, module, MainFrame._SEARCHTAB);
    }
    
    /**
     * Retrieves the children for the specified parent.
     * @param parentId The parent object ID.
     * @param childIdx The child module index.
     * @return The children or an empty collection.
     */
    public static Collection<DcObject> getChildren(String parentId, int childIdx) {
        List<DcObject> c = children.get(childIdx).get(parentId);
        c = c == null ? new ArrayList<DcObject>() : new ArrayList<DcObject>(c);
        children.get(childIdx).put(parentId, c);
        
        DcModule module =  DcModules.get(childIdx);
        DataFilter filter = new DataFilter(childIdx);
        filter.setOrder(new DcField[] {module.getField(module.getDefaultSortFieldIdx())});
        
        filter.sort(c);
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
                } else if (module.getType() == DcModule._TYPE_PROPERTY_MODULE) {
                    ref.setValue(DcProperty._A_NAME, name);
                } else {
                    ref.setValue(ref.getDisplayFieldIdx(), name);
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
        
        DcObject[] tabs = get(DcModules._TAB, df);
        if (tabs == null || tabs.length == 0) {
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
        DcObject[] tabs = get(DcModules._TAB, df);
        return tabs != null && tabs.length > 0 ? tabs[0] : null;
    }
    
    public static DcObject[] getTabs(int module) {
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
        DcMapping mapping = (DcMapping) DcModules.get(DcModules.getMappingModIdx(
                parent.getModule().getIndex(), child.getModule().getIndex(), fieldIdx)).getItem();
        mapping.setValue(DcMapping._A_PARENT_ID, parent.getID());
        mapping.setValue(DcMapping._B_REFERENCED_ID, child.getID());
        mapping.setReferencedObject(child);
        
        Collection<DcMapping> mappings = (Collection<DcMapping>) parent.getValue(fieldIdx);
        mappings = mappings == null ? new ArrayList<DcMapping>() : mappings;
        
        // check if a mapping exists already
        for (DcMapping m : mappings) {
            if (m.getReferencedObject() == null) continue;

            if (m.getReferencedObject().equals(child) || m.getReferencedObject().toString().equals(child.toString()))
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

    private static String getKey(DcObject dco) {
        if (dco instanceof Picture)
            return (String) dco.getValue(Picture._A_OBJECTID);
        else if (dco instanceof Loan)
            return (String) dco.getValue(Loan._D_OBJECTID);
        else if (dco.getParentReferenceFieldIndex() > -1)
            return dco.getParentID();

        return null;
    }    
    
    @SuppressWarnings("unchecked")
    private static void updateRelated(DcObject dco, boolean delete) {
        // updated or create the references
        for (DcField field : dco.getFields()) {
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                int mappingIdx = DcModules.getMappingModIdx(field.getModule(), field.getReferenceIdx(), field.getIndex());
                Collection<DcMapping> c = getReferences(mappingIdx, dco.getID());
                c = c == null ? c = new ArrayList<DcMapping>() : c;
                Collection<DcMapping> mappings = (Collection<DcMapping>) dco.getValue(field.getIndex());
                c.clear();
                
                if (!delete && mappings != null) {
                    for (DcMapping mapping : mappings) {
                        mapping.setValue(DcMapping._A_PARENT_ID, dco.getID());
                        c.add(mapping);
                    }
                }
                
                if (delete) {
                    if (mappings != null) {
                        for (DcObject mapping : mappings)
                            mapping.release();
                    }

                    int mappingModIdx = DcModules.getMappingModIdx(dco.getModule().getIndex(), field.getReferenceIdx(), field.getIndex());
                    Map<String, Collection<DcMapping>> map = references.get(mappingModIdx);
                    map.remove(dco.getID());
                }
                
                if (c.size() > 0) {
                    int mappingModIdx = DcModules.getMappingModIdx(dco.getModule().getIndex(), field.getReferenceIdx(), field.getIndex());
                    Map<String, Collection<DcMapping>> map = references.get(mappingModIdx);
                    map.put(dco.getID(), c);
                }
            }
        }
        
        if (delete)
            disgardReferences(dco);
        else if (dco instanceof Loan)
            updateLoanedItems((Loan) dco);
    }   
    
    /**
     * Removes references to this object from any other item. This method should only be called
     * when the provided item is about has been deleted from the database as well as the references
     * to this item. 
     * @param dco The deleted item
     */
    @SuppressWarnings("unchecked")
    private static void disgardReferences(DcObject dco) {
        DcModule mainModule = dco.getModule();
        if (    mainModule.hasDependingModules() || 
                mainModule.getType() == DcModule._TYPE_ASSOCIATE_MODULE ||
                mainModule.getType() == DcModule._TYPE_PROPERTY_MODULE) {

            // remove collection items holding a reference to this item
            for (DcModule referencingMod : DcModules.getReferencingModules(dco.getModule().getIndex())) {
                DcModule module = referencingMod instanceof MappingModule ?
                                    DcModules.get(((MappingModule) referencingMod).getParentModIdx()) :
                                    referencingMod;
                for (DcField field : module.getFields()) {
                    if (field.getReferenceIdx() == dco.getModule().getIndex()) {
                        if (referencingMod instanceof MappingModule) {
                            DataFilter df = new DataFilter(module.getIndex());
                            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, 
                                                            module.getIndex(), 
                                                            field.getIndex(), 
                                                            Operator.IS_FILLED, 
                                                            null));
                            
                            DcObject[] referencingItems = get(module.getIndex(), df);
                            for (int i = 0; i < referencingItems.length; i++) {
                                Collection<DcMapping> references = (Collection<DcMapping>) referencingItems[i].getValue(field.getIndex());
                                Object val = null;
                                boolean changed = false;
                                for (DcMapping mapping : references) {
                                    if (mapping.getReferencedId().equals(dco.getID())) {
                                        val = mapping;
                                        changed = true;
                                    }
                                }
                                references.remove(val);
                                
                                if (changed) 
                                    updateView(referencingItems[i], 0, referencingItems[i].getModule().getIndex(), MainFrame._SEARCHTAB);
                            }
                        } else {
                            DataFilter df = new DataFilter(module.getIndex());
                            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, 
                                                            module.getIndex(), 
                                                            field.getIndex(), 
                                                            Operator.EQUAL_TO, 
                                                            dco));
                            
                            DcObject[] items = get(module.getIndex(), df);
                            for (int i = 0; i < items.length; i++) {
                                items[i].setValueLowLevel(field.getIndex(), null);
                                updateView(items[i], 0, items[i].getModule().getIndex(), MainFrame._SEARCHTAB);
                            }
                        }
                    }
                }
            }
        }
        
        // make sure children are also removed
        if (dco.getModule().getChild() != null) {
            for (DcObject child : new ArrayList<DcObject>(getChildren(dco.getID(), dco.getModule().getChild().getIndex())))
                remove(child, child.getModule().getIndex());
        }
        
        pictures.remove(dco.getID());

        if (dco.getModule().canBeLend())
            loans.remove(dco.getID());
    }

    private static void updatePictures(DcObject dco) {
        for (DcField field : dco.getFields()) {
            if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                Picture picture = (Picture) dco.getValue(field.getIndex());
                
                if (picture == null) 
                    continue;
                    
                List<Picture> pics = pictures.get(dco.getID());
                pics = pics == null ? new ArrayList<Picture>() : pics;

                if (picture.isNew()) {
                    picture.markAsUnchanged();
                    pics.add(picture);
                    pictures.put(dco.getID(), pics);
                } else if (picture.isUpdated()) {
                    picture.setValue(Picture._D_IMAGE, null);
                    if (pics.indexOf(picture) > -1) {
                       Picture pic = pics.get(pics.indexOf(picture));
                       pic.copy(picture, true, true);
                       pic.markAsUnchanged();
                    }
                } else if (picture.isDeleted()) {
                    pics.remove(picture);
                    picture.destroy();
                }  
            }
        }
    }
    
    private static void updateLoanedItems(Loan loan) {
        String parentID = (String) loan.getValue(Loan._D_OBJECTID);
        if (parentID != null) {
            DcObject parent = null;
            
            for (DcModule module : DcModules.getModules()) {

                if (module.canBeLend())
                    parent = getObject(module.getIndex(), parentID);
                
                if (parent != null)
                    break;
            }
            
            if (parent != null) {
                parent.reload();
                updateView(parent, 0, parent.getModule().getIndex(), MainFrame._SEARCHTAB);
            }
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
    
    /**
     * Item count.
     * @param module
     * @param df
     */
    public static int contains(int module, DataFilter df) {
        DcObject[] objects = get(module, df);
        return objects != null ? objects.length : 0;
    }

    /**
     * Retrieves all the loans (actual and historic).
     * @param parentID The item ID for which the loans are retrieved.
     * @return A collection holding loans or an empty collection.
     */
    public static Collection<Loan> getLoans(String parentID) {
        List<Loan> c = loans.get(parentID);
        c = c == null ? new ArrayList<Loan>() : c;
        loans.put(parentID, c);
        return c;
    }
    
    /**
     * Retrieves the actual loan.
     * @param parentID The item ID for which the loan is retrieved.
     */
    public static Loan getCurrentLoan(String parentID) {
        Collection<Loan> loans = getLoans(parentID);
        for (Loan loan : new ArrayList<Loan>(loans)) {
            if (loan.getValue(Loan._B_ENDDATE) == null)
                return loan;
        }
        
        Loan loan = (Loan) DcModules.get(DcModules._LOAN).getItem();
        loans.add(loan);
        return loan;
    }
    
    public static DcObject getExternalReference(int moduleIdx, String ID) {
        DcModule module = DcModules.get(moduleIdx);
        String sql = "SELECT ID FROM " + module.getTableName() + " WHERE " +
             module.getField(ExternalReference._EXTERNAL_ID).getDatabaseFieldName() + " = ?";
        
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, ID);
            List<DcObject> items = DatabaseManager.executeQuery(ps, Query._SELECT);
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
                sql = "SELECT OBJECTID FROM " + mappingMod.getTableName() + 
                      " WHERE " + mappingMod.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + " = ?";
    
                PreparedStatement ps2 = conn.prepareStatement(sql);
                ps2.setString(1, referenceID);
                ResultSet rs2 = ps2.executeQuery();
    
                while (rs2.next()) {
                    result = getObject(moduleIdx, rs2.getString(1));
                    break;
                }
                
                rs2.close();
                break;
            }
        
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
            String query = 
                "SELECT ID FROM " + module.getTableName() + " WHERE " + 
                "UPPER(" + module.getField(module.getSystemDisplayFieldIdx()).getDatabaseFieldName() + ") =  UPPER(?)";
            
            if (module.getType() == DcModule._TYPE_PROPERTY_MODULE)
                query += " OR UPPER(" + module.getField(DcProperty._C_ALTERNATIVE_NAMES).getDatabaseFieldName() + ") LIKE ?"; 
            
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(query);

            ps.setString(1, s);
            if (module.getType() == DcModule._TYPE_PROPERTY_MODULE)
                ps.setString(2,  ";%" + s.toUpperCase() + "%;");
            
            ResultSet rs = ps.executeQuery();
            DcObject result = null;
            while (rs.next()) {
                String ID = rs.getString(1);
                result = getObject(moduleIdx, ID);
                break;
            }
            
            rs.close();
            ps.close();
            
            return result;
            
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
    public static DcObject getObject(int module, String ID) {
        Map<String, DcObject> map = objectsByID.get(Integer.valueOf(module));
        return map != null ? map.get(ID) : null;
    }    
  
    /**
     * Retrieve all referenced items for the given parent ID.
     * @param module
     * @param parentId
     */
    public static Collection<DcMapping> getReferences(int module, String parentId) {
        Collection<DcMapping> c = references.get(module).get(parentId);
        c = c == null ? new ArrayList<DcMapping>() : c;
        references.get(module).put(parentId, c);
        return c; 
    }

    /**
     * Retrieves all pictures for the given parent ID. 
     * @param parentId
     * @return Either the pictures or an empty collection.
     */
    public static Collection<Picture> getPictures(String parentId) {
        List<Picture> c = pictures.get(parentId);
        c = c == null ? new ArrayList<Picture>() : c;
        pictures.put(parentId, c);
        return c;
    }
    
    /**
     * Retrieve items using the specified data filter.
     * @see DataFilter
     * @param modIdx
     * @param filter
     */
    public static DcObject[] get(int modIdx, DataFilter filter) {
        DataFilter df = filter;
        
        List<DcObject> c = new ArrayList<DcObject>();
        if (DcModules.get(modIdx).isAbstract()) {
            for (DcModule m : DcModules.getModules()) {
                if (!m.isAbstract() && m.isTopModule()) {
                    
                    if ((modIdx == DcModules._MEDIA && m instanceof DcMediaModule) ||
                        (modIdx == DcModules._ITEM && (m.isContainerManaged()))) {
                     
                        DcObject[] objects = get(m.getIndex(), df);
                        for (int i = 0; i < objects.length; i++)
                            c.add(objects[i]);
                    }
                }
            }
        } else {
        	if (modIdx == DcModules._LOAN) {
                for (Collection<Loan> objects : loans.values())
                    add(c, df, objects);
        	} else if (objects.containsKey(modIdx)) {
                add(c, df, new ArrayList<DcObject>(objects.get(modIdx)));
            } else if (references.containsKey(modIdx)) {
                for (Collection<DcMapping> objects : references.get(modIdx).values())
                    add(c, df, objects);
            } else if (modIdx == DcModules._PICTURE) {
                for (Collection<Picture> objects : pictures.values())
                    add(c, df, objects);                
            } else if (children.containsKey(modIdx)) {
                for (Collection<DcObject> objects : children.get(modIdx).values())
                    add(c, df, objects);
            }
        }
        
        if (df == null) {
            DcModule module = DcModules.get(modIdx);
            df = new DataFilter(modIdx);
            df.setOrder(new DcField[] {module.getField(DcProperty._SYS_DISPLAYVALUE)});
        }
        
        df.sort(c);
        return c.toArray(new DcObject[0]);
    }

    private static void add(Collection<DcObject> data, DataFilter df, Collection<? extends DcObject> c) {
        for (DcObject dco : c) {
            if (df == null || df.applies(dco)) 
                data.add(dco);
        }
    }    

    
    /***************************************
     * Initialization
     ***************************************/
    
    /**
     * Initialize the data manager. Loads all items from the database or from the cache.
     * @see #useCache
     */
    public void initialize() {
        boolean gracefulShutdown = DcSettings.getBoolean(DcRepository.Settings.stGracefulShutdown);
        
        if (!gracefulShutdown || !useCache || !deserialize()) {
            long start = logger.isDebugEnabled() ? new Date().getTime() : 0;
            loadFromDB();

            if (logger.isDebugEnabled()) {
                long end = new Date().getTime();
                logger.debug("Items were loaded from the database in " + (end - start) + "ms");
            }
        }
    }

    
    /***************************************
     * Cache
     ***************************************/

    private static final int[] cacheTypes =  
        new int[] {CacheJob._OBJECTS, CacheJob._LOANS, CacheJob._PICTURES, CacheJob._REFERENCES, CacheJob._CHILDREN};
    
    public static void clearCache() {
    	try {
    	    saveCache = false;
    		for (String file : new File(DataCrow.cacheDir).list())
    			new File(DataCrow.cacheDir + file).delete();
    	} catch (Exception e) {
    		logger.error("Could not remove cache", e);
    	}
    }
    
    public static void serialize() {
        try {
            if (saveCache) {
                logger.info(DcResources.getText("msgWritingItemCache"));
                CacheWriter writer = new CacheWriter();
                writer.start();
                writer.join();
            }
        } catch (InterruptedException e) {
            logger.error(e, e);
        }
    }
    
    public static boolean deserialize() {
        File file = new File(DataCrow.cacheDir + "objects.dat");
        if (!file.exists()) return false;

        try {
            long start = logger.isDebugEnabled() ? new Date().getTime() : 0;
            
            CacheLoader loader = new CacheLoader();
            loader.start();
            loader.join();

            if (logger.isDebugEnabled()) {
                long end = new Date().getTime();
                logger.debug("Items were loaded from disk in " + (end - start) + "ms");
            }

            start = logger.isDebugEnabled() ? new Date().getTime() : 0;
            
            try {
                DataSetCreator dsc = new DataSetCreator(objects);
                dsc.start();
                dsc.join();
            } catch (Exception e) {
                logger.error(e, e);
            }
            
            if (logger.isDebugEnabled()) {
                long end = new Date().getTime();
                logger.debug("Item sets were created in " + (end - start) + "ms");
            }
            
            return loader.isSuccess();
        } catch (InterruptedException e) {
            logger.error(e, e);
        }
        return false;
    }
    
    private final static class CacheWriter extends Thread {
        
        @Override
        public void run() {
            
            long start = logger.isDebugEnabled() ? new Date().getTime() : 0;
            
            ThreadGroup tg = new ThreadGroup("Cache Writer");
            for (int cacheType : cacheTypes) {
                CacheJob cj = new CacheJob(tg, cacheType, CacheJob._JOBTYPE_WRITE);
                cj.start();
            }

            while (tg.activeCount() > 0)
                try { sleep(10); } catch (InterruptedException e) {}
            
            if (logger.isDebugEnabled()) {
                long end = new Date().getTime();
                logger.debug("Items were written to disk in " + (end - start) + "ms");
            }
        }
    }
    
    private final static class CacheLoader extends Thread {
        
        private boolean success = true;
        
        public boolean isSuccess() {
            return success;
        }
        
        @Override
        public void run() {
            ThreadGroup tg = new ThreadGroup("Cache Loader");
            Collection<CacheJob> jobs = new ArrayList<CacheJob>();
            for (int cacheType : cacheTypes) {
                CacheJob cj = new CacheJob(tg, cacheType, CacheJob._JOBTYPE_READ);
                jobs.add(cj);
                cj.start();
            }

            while (tg.activeCount() > 0)
                try { sleep(10); } catch (InterruptedException e) {}
            
            for (CacheJob cj : jobs) 
                success &= cj.isSuccess();
        }
    }
    
    /**
     * Loads all items from cache. Note that ObjectsByID cannot be cached as these would get other
     * instance IDs which will result in incorrect cached items (mismatch).
     * @author Robert Jan van der Waals
     */
    private final static class CacheJob extends Thread {
        
        public static int _JOBTYPE_READ = 0;
        public static int _JOBTYPE_WRITE = 1;
        
        public static int _OBJECTS = 0;
        public static int _LOANS = 1;
        public static int _PICTURES = 2;
        public static int _REFERENCES = 3;
        public static int _CHILDREN = 4;

        private int cacheType;
        private int jobType;
        
        private boolean success = false;
        
        private final String[] cache = new String[] {
                "objects.dat", "loans.dat", "pictures.dat",
                "references.dat", "children.dat"};
        
        public CacheJob(ThreadGroup tg, int cacheType, int jobType) {
            super(tg,  "Cache Job - " + cacheType);
            this.cacheType = cacheType;
            this.jobType = jobType;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        @Override
        public void run() {
            if (jobType == _JOBTYPE_READ)
                read();
            else if (jobType == _JOBTYPE_WRITE)
                write();
        }     
        
        private void write() {
            for (List<Picture> c : pictures.values()) {
                for (Picture p : c) 
                    p.setValue(Picture._D_IMAGE, null);
            }
            
            if (cacheType == _OBJECTS)
                write(objects);
            if (cacheType == _LOANS)
                write(loans);
            if (cacheType == _PICTURES)
                write(pictures);
            if (cacheType == _REFERENCES)
                write(references);
            if (cacheType == _CHILDREN)
                write(children);
        }
        
        private void write(Object o) {
            try {
                OutputStream os = new FileOutputStream(DataCrow.cacheDir + cache[cacheType]);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                ObjectOutput oi = new ObjectOutputStream(bos);
                oi.writeObject(o);
                oi.close();
                success = true;
            } catch (IOException e) {
                logger.error(e, e);
            }
        }
        
        private void read() {
            try {
                File file = new File(DataCrow.cacheDir + cache[cacheType]);
                
                if (!file.exists()) return;
                
                InputStream is = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(is);
                ObjectInput oi = new ObjectInputStream(bis);
                
                if (cacheType == _OBJECTS)
                    loadObjects(oi);
                if (cacheType == _LOANS)
                    loadLoans(oi);
                if (cacheType == _PICTURES)
                    loadPictures(oi);
                if (cacheType == _REFERENCES)
                    loadReferences(oi);
                if (cacheType == _CHILDREN)
                    loadChildren(oi);
                                
                oi.close();
                bis.close();
                is.close();
                success = true;
            } catch (Exception e) {
                logger.warn("Failed to load the items from cache. The items will be loaded from the database instead.", e);
            } catch (Error err) {
                logger.warn("Failed to load the items from cache. The items will be loaded from the database instead.", err);
            }            
        }
        
        @SuppressWarnings("unchecked")
        private void loadObjects(ObjectInput oi) throws ClassNotFoundException, IOException {
            objects = (Map<Integer, List<DcObject>>) oi.readObject();
        }
        
        @SuppressWarnings("unchecked")
        private void loadLoans(ObjectInput oi) throws ClassNotFoundException, IOException {
            loans = (Map<String, List<Loan>>) oi.readObject();
        }
        
        @SuppressWarnings("unchecked")
        private void loadPictures(ObjectInput oi) throws ClassNotFoundException, IOException {
            pictures = (Map<String, List<Picture>>) oi.readObject();
        }
        
        @SuppressWarnings("unchecked")
        private void loadReferences(ObjectInput oi) throws ClassNotFoundException, IOException {
            references = (Map<Integer, Map<String, Collection<DcMapping>>>) oi.readObject();
        }
        
        @SuppressWarnings("unchecked")
        private void loadChildren(ObjectInput oi) throws ClassNotFoundException, IOException {
            children = (Map<Integer, Map<String, List<DcObject>>>) oi.readObject();
        }
    }
    

    /***************************************
     * Load from Database
     ***************************************/
    private void loadFromDB() {
        objects.clear();
        
        ThreadGroup group = new ThreadGroup("data-fetchers");
        Collection<DataFetcher> fetchers = new ArrayList<DataFetcher>();

        // pictures, mappings and loans
        setPictures();
        setLoans();
        
        for (DcModule module : DcModules.getAllModules()) {
            if (module instanceof MappingModule)
                setReferences((MappingModule) module);
        }         

        // supporting modules
        for (DcModule module : DcModules.getAllModules()) {
            if (    !isLoaded(module.getIndex()) && 
                    !module.isTopModule() && 
                    !module.isChildModule()) {
                
                objects.put(module.getIndex(), null);
                fetchers.add(new DataFetcher(module, group));            
            }
        }        
        
        doFetch(fetchers);

        for (DcModule module : DcModules.getAllModules()) {
            if (   !isLoaded(module.getIndex()) && 
                   !module.isAbstract() &&
                    module.hasDependingModules()) {
                objects.put(module.getIndex(), null);
                fetchers.add(new DataFetcher(module, group));
            }
        }
        
        for (DcModule module : DcModules.getAllModules()) {
            if (module.isChildModule() && !module.isAbstract() && !isLoaded(module.getIndex())) 
                setChildren((IChildModule) module);
        }
        
        // fill the Objects By ID set with the child items
        for (Integer key : children.keySet()) {
            Map<String, DcObject> map = new HashMap<String, DcObject>();
            for (List<DcObject> c : children.get(key).values()) {
                for (DcObject dco : c)
                    map.put(dco.getID(), dco);
            }
            
            objectsByID.put(key, map);
        }

        for (DcModule module : DcModules.getAllModules()) {
            if (!isLoaded(module.getIndex()) && !module.isAbstract() && module.isTopModule()) {
                objects.put(module.getIndex(), null);
                fetchers.add(new DataFetcher(module, group));
            }
        }        
        
        doFetch(fetchers); 
        
        // Check for null values (security issues, etc).
        for (Integer key : objects.keySet()) {
            Collection<DcObject> c = objects.get(key);
            if (c == null)
                objects.put(key, new ArrayList<DcObject>());
        }

        Templates.refresh();
    }
    
    private void doFetch(Collection<DataFetcher> fetchers) {
        for (DataFetcher fetcher : fetchers) {
            try {
                fetcher.start();
                fetcher.join();
            } catch (InterruptedException e) {
                logger.error(e, e);
            }
        }  
        fetchers.clear();
    }
    
    public void setPictures() {
        for (DcObject dco : DcModules.get(DcModules._PICTURE).loadData()) {
            Picture picture = (Picture) dco;
            String key = getKey(dco);
            
            List<Picture> pics = pictures.get(key);
            pics = pics == null ? new ArrayList<Picture>() : pics;
            pics.add(picture);
            
            pictures.put(key, pics);
         }
    }
    
    private void setLoans() {
        for (DcObject dco : DcModules.get(DcModules._LOAN).loadData()) {
            Loan loan = (Loan) dco;
            String key = (String) loan.getValue(Loan._D_OBJECTID);
            
            List<Loan> c = loans.get(key);
            c = c == null ? c = new ArrayList<Loan>() : c;
            c.add(loan);
            
            loans.put(key, c);
         }
    }
    
    private void setChildren(IChildModule childModule) {
        Map<String, List<DcObject>> childrenMap = new HashMap<String, List<DcObject>>();
        for (DcObject child : DcModules.get(childModule.getIndex()).loadData()) {
            String key = child.getParentID();

            List<DcObject> c = childrenMap.get(key);
            c = c == null ? new ArrayList<DcObject>() : c;
            c.add(child);

            childrenMap.put(key, c);
        }
        
        children.put(childModule.getIndex(), childrenMap);
    }  
    
    private void setReferences(MappingModule mappingModule) {
        Map<String, Collection<DcMapping>> mappings = new HashMap<String, Collection<DcMapping>>();
        for (DcObject dco : mappingModule.loadData()) {
            DcMapping mapping = (DcMapping) dco;
            String parentId = mapping.getParentId();
            
            Collection<DcMapping> c = mappings.get(parentId);
            c = c == null ? new ArrayList<DcMapping>() : c;
            c.add(mapping);

            mappings.put(parentId, c);
        }
        references.put(mappingModule.getIndex(), mappings);
    }
    
    private boolean isLoaded(int module) {
        return references.containsKey(module) || 
               (module == DcModules._LOAN && loans.size() > 0) ||
               (module == DcModules._PICTURE && pictures.size() > 0) ||
               children.containsKey(module) ||
               objects.containsKey(module);
    }    
    
    private final static class DataFetcher extends Thread {
        
        private DcModule module;
        
        public DataFetcher(DcModule module, ThreadGroup group) {
            super(group, "datafetcher-" + module.getName());
            this.module = module;
        }
        
        @Override
        public void run() {
            try {
                long start = logger.isDebugEnabled() ? new Date().getTime() : 0;

                List<DcObject> items =  module.loadData();
                objects.put(Integer.valueOf(module.getIndex()), items);
                
                Map<String, DcObject> map = new HashMap<String, DcObject>();
                
                for (DcObject dco : items)
                    map.put(dco.getID(), dco);

                objectsByID.put(Integer.valueOf(module.getIndex()), map);

                if (logger.isDebugEnabled()) {
                    long end = new Date().getTime();
                    logger.debug("Data for " + module.getLabel()+ " was retrieved in " + (end - start) + "ms");
                }
                
            } catch (OutOfMemoryError ome) {
                ome.printStackTrace();
                logger.error(DcResources.getText("msgOutOfMemory"), ome);
                DcSwingUtilities.displayErrorMessage("msgOutOfMemory");
            } catch (Exception exp) {
                exp.printStackTrace();
                logger.error(DcResources.getText("msgDataLoadingError"), exp);
                DcSwingUtilities.displayErrorMessage("msgDataLoadingError");

            }
            
            module = null;
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

                            if (dco instanceof DcMediaObject)
                                DcModules.get(DcModules._MEDIA).getSearchView().add(dco);

                            DcModules.get(DcModules._ITEM).getSearchView().add(dco);
                        }
                    }
                    
                    if (DcModules.get(module).getInsertView() != null)
                        DcModules.get(module).getInsertView().removeItems(new String[] {dco.getID()});
                }
            } else if (tab == MainFrame._SEARCHTAB) {
                
                Collection<DcModule> modules = new ArrayList<DcModule>();
                modules.add(m);
                
                if (!m.isAbstract()) {
                    if (m instanceof DcMediaModule)
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
                            if (mode == 2)
                                masterView.removeItems(new String[] {dco.getID()});
                        }
                    } catch (Exception exp) {
                        logger.error("Error while updating view for module " + mod.getLabel(), exp);
                    }
                }
            }
            
            if (mode != 0) { // only for inserts and removals
                dco.freeResources();
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
                        if (module.getSearchView() != null) {
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
    
    private final static class DataSetCreator extends Thread {
        
        private Map<Integer, List<DcObject>> objects;
        
        public DataSetCreator(Map<Integer, List<DcObject>> objects) {
            this.objects = objects;
        }
        
        @Override
        public void run() {
            for (Integer key : objects.keySet()) {
                Collection<DcObject> c = objects.get(key);
                Map<String, DcObject> map = new HashMap<String, DcObject>();
                for (DcObject dco : c)
                    map.put(dco.getID(), dco);
                
                objectsByID.put(key, map);
            }
            
            for (Integer key : children.keySet()) {
                Map<String, DcObject> map = new HashMap<String, DcObject>();
                for (List<DcObject> c : children.get(key).values()) {
                    for (DcObject dco : c)
                        map.put(dco.getID(), dco);
                }
                
                objectsByID.put(key, map);
            }
        }
    }    
}
