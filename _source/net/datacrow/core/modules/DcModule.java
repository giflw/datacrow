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

package net.datacrow.core.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.components.panels.ChartPanel;
import net.datacrow.console.components.panels.QuickViewPanel;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.menu.MainFrameMenuBar;
import net.datacrow.console.views.MasterView;
import net.datacrow.console.views.View;
import net.datacrow.console.windows.filerenamer.FileRenamerDialog;
import net.datacrow.console.windows.filtering.FilterDialog;
import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.db.Query;
import net.datacrow.core.modules.xml.XmlField;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMediaObject;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcTemplate;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.helpers.Item;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.core.services.OnlineService;
import net.datacrow.core.services.Services;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.enhancers.IValueEnhancer;
import net.datacrow.fileimporters.FileImporter;
import net.datacrow.settings.DcModuleSettings;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.DcFieldDefinitions;
import net.datacrow.settings.definitions.QuickViewFieldDefinition;
import net.datacrow.settings.definitions.QuickViewFieldDefinitions;
import net.datacrow.settings.definitions.WebFieldDefinitions;
import net.datacrow.synchronizers.Synchronizer;
import net.datacrow.util.CSVReader;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class DcModule implements Comparable<DcModule> {

    private static Logger logger = Logger.getLogger(DcModule.class.getName());
    
    private final int index;
    
    private int displayIndex;
    private int defaultSortFieldIdx;
    private int nameFieldIdx;
    
    private String label;
    
    private final String name;
    private final String tableName;
    private final String tableShortName;
    private final String tableJoin;
    private final String description;
    
    private final String objectName;
    private final String objectNamePlural;
    
    private net.datacrow.settings.Settings settings;
    
    @SuppressWarnings("unchecked")
    private Class synchronizerClass;
    @SuppressWarnings("unchecked")
    private Class objectClass;
    @SuppressWarnings("unchecked")
    private Class importerClass;
    
    private int childIdx = -1;
    private int parentIdx = -1;
    
    private boolean hasSearchView = true;
    private boolean hasInsertView  = true;
    
    private boolean isServingMultipleModules = false;
    
    private ImageIcon icon16;
    private ImageIcon icon32;
    
    private String icon16filename;
    private String icon32filename;
    
    protected MasterView insertView;
    protected MasterView searchView;
    private FilterDialog filterForm;
    private ChartPanel chartPanel;
    private FileRenamerDialog fileRenamerDialog;
    
    private Map<Integer, DcField> fields = new HashMap<Integer, DcField>();
    private Map<Integer, DcField> systemFields = new HashMap<Integer, DcField>();
    private Collection<DcField> sortedFields;
    
    private OnlineService service;

    private KeyStroke keyStroke;
    
    private boolean canBeLended = false;
    private boolean topModule = false;
    private boolean isFileBacked = false;
    private boolean isContainerManaged = false;
    private boolean hasDependingModules = true;
    
    private XmlModule xmlModule;
    
    protected DcModule(int index, 
                       String name,
                       String description,
                       String objectName,
                       String objectNamePlural,
                       String tableName, 
                       String tableShortName, 
                       String tableJoin,
                       boolean topModule) { 

        this.index = index;
        
        Collection<IServer> servers = Services.getInstance().getServers(index);
        if (servers != null) {
            this.service = new OnlineService(index);
            for (IServer server : Services.getInstance().getServers(index)) 
                service.addServer(server);
        }
        
        this.tableName = (tableName == null ? "" : tableName).toLowerCase();
        this.tableShortName  = (tableShortName == null ? "" : tableShortName).toLowerCase();
        this.tableJoin = tableJoin;
        this.name = name;
        this.label = name;
        this.description = description;
        
        this.topModule = topModule;
        this.objectName = objectName;
        this.objectNamePlural = objectNamePlural;
        
    }
    
    public DcModule(int index, 
                    boolean topModule, 
                    String name,
                    String description,
                    String objectName,
                    String objectNamePlural,
                    String tableName, 
                    String tableShortName, 
                    String tableJoin) {

        this(index, name, description, objectName, objectNamePlural, tableName, 
             tableShortName, tableJoin, topModule);
        
        initializeSystemFields();
        initializeFields();
        initializeSettings();
    }
    
    public DcModule(XmlModule module) {
        this(module.getIndex(), module.getName(), module.getDescription(), module.getObjectName(), 
             module.getObjectNamePlural(), module.getTableName(), module.getTableNameShort(),
             module.getTableName(), true);

        this.xmlModule = module;
        
        childIdx = module.getChildIndex();
        parentIdx = module.getParentIndex();
        label = module.getLabel();
        
        isFileBacked = module.isFileBacked();
        isContainerManaged = module.isContainerManaged();
        hasInsertView = module.hasInsertView();
        hasSearchView = module.hasSearchView();
        keyStroke = module.getKeyStroke();
        objectClass = module.getObject();
        synchronizerClass = module.getSynchronizer();
        importerClass = module.getImporter();
        hasDependingModules = module.hasDependingModules();
        
        icon16filename = module.getIcon16Filename();
        icon32filename = module.getIcon32Filename();
        icon16 = new ImageIcon(module.getIcon16());
        icon32 = new ImageIcon(module.getIcon32());
        
        nameFieldIdx = module.getNameFieldIdx();
        canBeLended = module.canBeLended();
        
        displayIndex = module.getDisplayIndex();
        defaultSortFieldIdx = module.getDefaultSortFieldIdx();
        
        initializeSystemFields();
        
        if (module.getChildIndex() > -1)
            setChild(module.getChildIndex());
        
        for (XmlField xmlField : module.getFields())
            addField(new DcField(xmlField, getIndex()));
        
        initializeFields();
        
        initializeSettings();
        setServingMultipleModules(module.isServingMultipleModules());
        
        // Set it to disabled only if the XML module is defined as disabled.
        // There is no use case for this (yet).
        if (!module.isEnabled()) isEnabled(false);
    }

    public boolean isAbstract() {
        return getIndex() == DcModules._ITEM ||
               getIndex() == DcModules._MEDIA;
    }
    
    public boolean isEditingAllowd() {
        return SecurityCentre.getInstance().getUser().isEditingAllowed(this);
    }
    
    public boolean isChildModule() {
        return getParent() != null;
    }
    
    public boolean isParentModule() {
        return getChild() != null;
    }    
    
    public int getIndex() {
        return index;
    }    
    
    public ImageIcon getIcon16() {
        return icon16;
    }
    
    public ImageIcon getIcon32() {
        return icon32;
    }

    public String getObjectName() {
        return objectName;
    }
    
    public String getObjectNamePlural() {
        return objectNamePlural;
    }
    
    public KeyStroke getKeyStroke() {
        return keyStroke;
    }    
    
    public void setIcon16(ImageIcon icon16) {
        this.icon16 = icon16;
    }

    public void setIcon32(ImageIcon icon32) {
        this.icon32 = icon32;
    }
    
    public boolean isEnabled() {
        if (    SecurityCentre.getInstance().getUser() == null ||
                SecurityCentre.getInstance().getUser().isAuthorized(this)) 
            return settings.getBoolean(DcRepository.ModuleSettings.stEnabled);
        else
            return false;
    }

    public void isEnabled(boolean b) {
        settings.set(DcRepository.ModuleSettings.stEnabled, b);
    }    
    
    public String getName() {
        return name;
    }

    public  String getDescription() {
        return description;
    }    

    public String getTableName() {
        return tableName;
    }

    public String getTableShortName() {
        return tableShortName;
    }

    public String getTableJoin() {
        return tableJoin;
    }

    public OnlineService getOnlineService() {
        return service;
    }
    
    public boolean deliversOnlineService() {
        return Services.getInstance().getServers(getIndex()) != null;
    }
    
    public boolean isCustomModule() {
        return  (getIndex() < 50 || getIndex() >= 20000) &&
               !(this instanceof TemplateModule) && 
               !(this instanceof MappingModule) && 
               !DcModules.isUsedInMapping(getIndex());
    } 
    
    public DcObject getDcObject() {
        try {
            try {
                return (DcObject) objectClass.getConstructors()[0].newInstance(new Object[] {});    
            } catch (Exception exp) {
                return (DcObject) objectClass.getConstructors()[0].newInstance(new Object[] {Integer.valueOf(getIndex())});
            }
        } catch (Exception e) {
            logger.error("Could not instantiate " + objectClass, e);
        }

        return null;
    }  
    
    public ItemForm getItemForm(DcTemplate template,
                                boolean readonly,
                                boolean update,
                                DcObject o,
                                boolean applyTemplate) {
        try {
            try {
                return (ItemForm) objectClass.getConstructors()[0].newInstance(new Object[] {});    
            } catch (Exception exp) {
                return (ItemForm) objectClass.getConstructors()[0].newInstance(new Object[] {Integer.valueOf(getIndex())});
            }
        } catch (Exception e) {
            logger.error("Could not instantiate " + objectClass, e);
        }

        return null;
    }  
    
    public Synchronizer getSynchronizer() {
        if (synchronizerClass != null) {
            try {
                return (Synchronizer) synchronizerClass.getConstructors()[0].newInstance(new Object[] {});    
            } catch (Exception e) {
                logger.error("Could not instantiate " + synchronizerClass, e);
            }
        }
        return null;
    }
    
    public MasterView getInsertView() {
        initializeUI();
        return insertView;
    }

    public MasterView getSearchView() {
        initializeUI();
        return searchView;
    }

    public View getCurrentSearchView() {
        initializeUI();
        return getSearchView() != null ? getSearchView().getCurrent() : null;
    }

    public View getCurrentInsertView() {
        initializeUI();
        return getInsertView() != null ? getInsertView().getCurrent() : null;
    }    
    
    public DcPropertyModule getPropertyModule(int modIdx) {
        return (DcPropertyModule) DcModules.get(getIndex() + modIdx);
    }

    public TemplateModule getTemplateModule() {
        if ((isTopModule() || isChildModule()) && !isAbstract())
            return (TemplateModule) DcModules.get(getIndex() + DcModules._TEMPLATE);
        else 
            return null;
    }
    
    public boolean isSelectableInUI() {
        return getIndex() == DcModules._CONTACTPERSON || 
               getIndex() == DcModules._MEDIA || 
              (isTopModule() && isEnabled() && !hasDependingModules());
    }
    
    public void addField(DcField field) {
        fields.put(field.getIndex(), field);
    }
    
    public List<DcObject> loadData() {
        try {
            if (isAbstract()) {
                List<DcObject> items = new ArrayList<DcObject>();
                for (DcObject dco : DataManager.get(getIndex(), null))
                    items.add(dco);
                return items;
            } else {
                return DatabaseManager.executeQuery(getDcObject(), true);
            }
        } catch (Exception e) {
            logger.error("Could not load data for module " + getLabel(), e);
        }
        return null;
    }    

    public MasterView[] getViews() {
        if (getSearchView() != null && getInsertView() != null)
            return new MasterView[] {getSearchView(), getInsertView()};
        else if (getSearchView() != null)
            return new MasterView[] {getSearchView()};
        else if (getInsertView() != null)
            return new MasterView[] {getInsertView()};
        else
        	return new MasterView[0];
    }

    public DcField getField(int index) {
        DcField field = fields.get(index);
        return field == null ? getSystemField(index) : field;
    }
    
    public DcField getField(String columnName) {
        for (DcField field : getFields()) {
            if (field.getDatabaseFieldName().equals(columnName))
                return field;
        }
        return null;
    }
    
    public void addValueEnhancer(IValueEnhancer enhancer, int field) {
        getField(field).addValueEnhancer(enhancer);
    }
    
    public DcField getSystemField(int index) {
        return systemFields.get(index);
    }

    public void setChild(int module) {
    	this.childIdx = module;
    }
    
    public MainFrameMenuBar getMenuBar() {
        return isTopModule() ? new MainFrameMenuBar(this) : null;
    }

    public DcModule getParent() {
        return parentIdx > 0 ? DcModules.get(parentIdx) : null;
    }
    
    public DcModule getChild() {
    	return childIdx > 0 ? DcModules.get(childIdx) : null;
    }
    
    public int getParentReferenceFieldIndex() {
        if (isContainerManaged())
            return DcObject._SYS_CONTAINER;
        
        for (DcField field : getFields()) {
            if (field.getValueType() == DcRepository.ValueTypes._DCPARENTREFERENCE)
                return field.getIndex();
        }
        
        return -1;
    }

    public FilterDialog getFilterForm(boolean create) {
        if (create)
            filterForm = filterForm == null && isTopModule() ? new FilterDialog(this, getSearchView()) : filterForm;
            
        return filterForm;
    }
    
    public FileRenamerDialog getFileRenamerDialog(boolean create) {
        if (create)
            fileRenamerDialog = fileRenamerDialog == null ? new FileRenamerDialog(getIndex()) : fileRenamerDialog;

        return fileRenamerDialog;
    }    

    public ChartPanel getChartPanel(boolean create) {
        if (create)
            chartPanel = chartPanel == null && isTopModule() ? new ChartPanel(getIndex()) : chartPanel;
            
        return chartPanel;
    }
    
    public boolean isServingMultipleModules() {
        return isServingMultipleModules;
    }

    public void setServingMultipleModules(boolean isServingMultipleModules) {
        this.isServingMultipleModules = isServingMultipleModules;
    }    
    
    public boolean isTopModule() {
        return topModule;
    }

    public int getFieldCount() {
        return fields.size();
    }

    public boolean canBeLended() {
        return canBeLended &&  
              (DcModules.get(DcModules._CONTACTPERSON) == null || 
               DcModules.get(DcModules._CONTACTPERSON).isEnabled());
    }

    public boolean hasDependingModules() {
        return hasDependingModules;    
    }
    
    public Object getSetting(String key) {
        return settings.get(key);
    }
    
    public void setSetting(String key, Object value) {
        settings.set(key, value);
    }

    public QuickViewPanel getQuickView() {
        return new QuickViewPanel(true);
    }

    public boolean hasReferenceTo(int module) {
        for (DcField field : getFields()) {
            if (field.getSourceModuleIdx() == module)
                return true;
        }
        return false;
    }

    public Collection<DcField> getFields() {
        if (sortedFields == null || sortedFields.size() < fields.size()) {
            sortedFields = new ArrayList<DcField>();
            sortedFields.addAll(fields.values());
            Collections.sort((List<DcField>) sortedFields, new Comparator<DcField>() {
                public int compare(DcField fld1, DcField fld2) {
                    return fld1.getLabel().compareTo(fld2.getLabel());
                }
            });
        }
        return sortedFields;
    }

    public DcFieldDefinitions getFieldDefinitions() {
        return (DcFieldDefinitions) getSetting(DcRepository.ModuleSettings.stFieldDefinitions);
    }

    public WebFieldDefinitions getWebFieldDefinitions() {
        return (WebFieldDefinitions) getSetting(DcRepository.ModuleSettings.stWebFieldDefinitions);
    }
    
    public int[] getFieldIndices() {
        Set<Integer> keys = fields.keySet();
        int[] indices = new int[keys.size()];
        int counter = 0;
        for (Integer key : keys)
            indices[counter++] = key.intValue();

        return indices;
    }
    
    public void removeEnhancers() {
        for (DcField field : fields.values())
            field.removeEnhancers();
    }    

    @SuppressWarnings("unchecked")
    public Class getImporterClass() {
        return importerClass;
    }
    
    public FileImporter getImporter() {
        if (importerClass != null) {
            try {
                return (FileImporter) importerClass.newInstance();
            } catch (Exception e) {
                logger.error("Could not instantiate " + importerClass, e);
            }
        }
        return null;
    }
    
    public boolean isCustomFieldsAllowed() {
        return true;
    }
    
    public net.datacrow.settings.Settings getSettings() {
        return settings;
    }
    
    public boolean isContainerManaged() {
        return isContainerManaged;
    }
    
    protected void initializeFields() {
        try {
            addField(new DcField(DcObject._ID, getIndex(), "ID",
                                 false, true, true, false, false,
                                 15, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._BIGINTEGER,
                                 "ID"));
            addField(new DcField(DcObject._SYS_CREATED, getIndex(), "Created",
                                 false, true, true, true, true,
                                 10, ComponentFactory._DATEFIELD, getIndex(), DcRepository.ValueTypes._DATE,
                                 "Created"));
            addField(new DcField(DcObject._SYS_MODIFIED, getIndex(), "Modified",
                                 false, true, true, true, true,
                                 10, ComponentFactory._DATEFIELD, getIndex(), DcRepository.ValueTypes._DATE,
                                 "Modified"));
            
            if ((isTopModule() || isChildModule()) && isCustomFieldsAllowed()) {
                addField(new DcField(DcMediaObject._U1_USER_LONGTEXT, getIndex(), "User Long Text Field", 
                        false, false, false, true, false, 
                        4000, ComponentFactory._LONGTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                        "UserLongText1"));         
                addField(new DcField(DcMediaObject._U2_USER_SHORTTEXT1, getIndex(), "User Short Text Field 1",  
                        false, false, false, true, false, 
                        255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                        "UserShortText1")); 
                addField(new DcField(DcMediaObject._U3_USER_SHORTTEXT2, getIndex(), "User Short Text Field 2",  
                        false, false, false, true, false, 
                        255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                        "UserShortText2")); 
                addField(new DcField(DcMediaObject._U4_USER_NUMERIC1, getIndex(), "User Numeric Field 1",  
                        false, false, false, true, false, 
                        255, ComponentFactory._NUMBERFIELD, getIndex(), DcRepository.ValueTypes._LONG,
                        "UserInteger1"));
                addField(new DcField(DcMediaObject._U5_USER_NUMERIC2, getIndex(), "User Numeric Field 2",  
                        false, false, false, true, false, 
                        255, ComponentFactory._NUMBERFIELD, getIndex(), DcRepository.ValueTypes._LONG,
                        "UserInteger2"));            
            }
    
            if (isContainerManaged())
                addField(getField(DcObject._SYS_CONTAINER));
            
            if (isTopModule() && deliversOnlineService()) {
                addField(getField(DcObject._SYS_SERVICE));
                addField(getField(DcObject._SYS_SERVICEURL));
            }
            
            addField(getField(DcObject._SYS_DISPLAYVALUE));
            
            if (isFileBacked) {
                addField(getField(DcObject._SYS_FILENAME));
                addField(getField(DcObject._SYS_FILESIZE));
                addField(getField(DcObject._SYS_FILEHASH));
                addField(getField(DcObject._SYS_FILEHASHTYPE));
            }
            
            if (canBeLended()) {
                addField(getField(DcObject._SYS_AVAILABLE));
                addField(getField(DcObject._SYS_LOANEDBY));
                addField(getField(DcObject._SYS_LOANDURATION));
            }
    
            addField(getField(DcObject._SYS_MODULE));
        } catch (Exception e) {
            logger.error(e, e);
        }
    }    

    /**
     * Initializes and corrects the module settings (if necessary)
     */
    public void initializeSettings() {
        settings = new DcModuleSettings(this);
    
        // check whether the definitions are still correct; 
        // - there can be more definitions for a field; the actual field can be removed by a user
        QuickViewFieldDefinitions qvDefinitions = 
            (QuickViewFieldDefinitions) settings.get(DcRepository.ModuleSettings.stQuickViewFieldDefinitions);
        
        QuickViewFieldDefinitions newQvDefinitions = new QuickViewFieldDefinitions();
        for (QuickViewFieldDefinition definition : qvDefinitions.getDefinitions()) {
            if (getField(definition.getField()) != null)
                newQvDefinitions.add(definition);
        }  
        
        DcFieldDefinitions definitions = 
            (DcFieldDefinitions) settings.get(DcRepository.ModuleSettings.stFieldDefinitions);
        
        DcFieldDefinitions newDefinitions = new DcFieldDefinitions();
        for (DcFieldDefinition definition : definitions.getDefinitions()) {
            if (getField(definition.getIndex()) != null)
                newDefinitions.add(definition);
        }
        
        settings.set(DcRepository.ModuleSettings.stQuickViewFieldDefinitions, newQvDefinitions);
        settings.set(DcRepository.ModuleSettings.stFieldDefinitions, newDefinitions);
    }
    
    protected void initializeSystemFields() {
        systemFields.put(DcObject._SYS_MODULE,
                         new DcField(DcObject._SYS_MODULE, getIndex(), "Item",
                                     true, true, true, true, false,
                                     255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                                     ""));
        systemFields.put(DcObject._SYS_AVAILABLE,
                         new DcField(DcObject._SYS_AVAILABLE, getIndex(), "Available",
                                     true, true, true, true, false,
                                     4, ComponentFactory._AVAILABILITYCOMBO, getIndex(), DcRepository.ValueTypes._BOOLEAN,
                                     ""));
        systemFields.put(DcObject._SYS_LOANEDBY,
                         new DcField(DcObject._SYS_LOANEDBY, getIndex(), "Loaned By",
                                     true, true, true, true, false,
                                     255, ComponentFactory._REFERENCEFIELD, DcModules._CONTACTPERSON, DcRepository.ValueTypes._DCOBJECTREFERENCE,
                                     ""));
        systemFields.put(DcObject._SYS_LOANDURATION,
                         new DcField(DcObject._SYS_LOANDURATION, getIndex(), "Days Loaned",
                                     true, true, true, true, false,
                                     10, ComponentFactory._NUMBERFIELD, getIndex(), DcRepository.ValueTypes._LONG,
                                     ""));
        
        if (isTopModule() && deliversOnlineService()) {
            systemFields.put(Integer.valueOf(DcObject._SYS_SERVICE),
                             new DcField(DcObject._SYS_SERVICE, getIndex(), "Service",
                                         false, true, true, false, true,
                                         255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                                         "service"));
            systemFields.put(Integer.valueOf(DcObject._SYS_SERVICEURL),
                             new DcField(DcObject._SYS_SERVICEURL, getIndex(), "Service URL",
                                         false, true, true, false, true,
                                         255, ComponentFactory._URLFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                                         "serviceurl"));        
        }
        
        if (isContainerManaged()) {
            systemFields.put(Integer.valueOf(DcObject._SYS_CONTAINER),
                    new DcField(DcObject._SYS_CONTAINER, getIndex(), "Container",
                                false, true, false, true, false,
                                10, ComponentFactory._REFERENCEFIELD, DcModules._CONTAINER, DcRepository.ValueTypes._DCOBJECTREFERENCE,
                                "Container"));
        }
        
        systemFields.put(Integer.valueOf(DcObject._SYS_DISPLAYVALUE),
                new DcField(Item._SYS_DISPLAYVALUE, getIndex(), "Label",
                            true, true, true, false, false,
                            255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                            ""));
        
        if (isFileBacked) {
            systemFields.put(Integer.valueOf(DcObject._SYS_FILENAME),
                    new DcField(DcObject._SYS_FILENAME, getIndex(), "Filename",
                                false, true, false, true, false,
                                500, ComponentFactory._FILELAUNCHFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                                "Filename"));
            systemFields.put(Integer.valueOf(DcObject._SYS_FILEHASH),
                    new DcField(DcObject._SYS_FILEHASH, getIndex(), "Filehash",
                                false, false, true, false, true,
                                32, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                                "Filehash"));
            systemFields.put(Integer.valueOf(DcObject._SYS_FILESIZE),
                    new DcField(DcObject._SYS_FILESIZE, getIndex(), "Filesize",
                                false, true, true, true, true,
                                10, ComponentFactory._FILESIZEFIELD, getIndex(), DcRepository.ValueTypes._LONG,
                                "Filesize"));
            systemFields.put(Integer.valueOf(DcObject._SYS_FILEHASHTYPE),
                    new DcField(DcObject._SYS_FILEHASHTYPE, getIndex(), "Filehash Type",
                                false, false, true, false, true,
                                10, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                                "FilehashType"));
        }
    }    
    
    public boolean hasInsertView() {
        return hasInsertView;
    }

    public boolean hasSearchView() {
        return hasSearchView;
    }
    
    protected void initializeUI()  {
        if (insertView == null && hasInsertView()) {
            insertView = new MasterView();

            // table view
            DcTable table = new DcTable(this, false, true);
            View tableView = new View(insertView, View._TYPE_INSERT, table, 
                    DcResources.getText("lblNewItem", getObjectNamePlural()), 
                    IconLibrary._icoItemsNew, MasterView._TABLE_VIEW);
            table.setView(tableView);
            
            // list view
            DcObjectList list = new DcObjectList(this, DcObjectList._CARDS, true, true);
            View listView = new View(insertView, View._TYPE_INSERT, list, 
                    DcResources.getText("lblNewItem", getObjectNamePlural()), 
                    IconLibrary._icoItemsNew, MasterView._LIST_VIEW);
            list.setView(listView);
            
            insertView.addView(MasterView._TABLE_VIEW, tableView);
            insertView.addView(MasterView._LIST_VIEW, listView);
        }
        
        if (searchView == null && hasSearchView() ) {
            searchView = new MasterView();
            searchView.setTreePanel(this);
            
            // table view
            DcTable table = new DcTable(this, false, true);
            View tableView = new View(searchView, View._TYPE_SEARCH, table, getObjectNamePlural(), getIcon16(), MasterView._TABLE_VIEW);
            table.setView(tableView);
            
            // list view
            DcObjectList list = new DcObjectList(this, DcObjectList._CARDS, true, true);
            View listView = new View(searchView, View._TYPE_SEARCH, list, getObjectNamePlural(), getIcon16(), MasterView._LIST_VIEW);
            list.setView(listView);

            searchView.addView(MasterView._TABLE_VIEW, tableView);
            searchView.addView(MasterView._LIST_VIEW, listView);            
        }
    }

    public boolean isFileBacked() {
        return isFileBacked;
    }
    
    public int getDefaultSortFieldIdx() {
        return defaultSortFieldIdx;
    }

    public int getDisplayIndex() {
        return displayIndex;
    }

    public int getNameFieldIdx() {
        return nameFieldIdx;
    }

    public String getIcon16Filename() {
        return icon16filename;
    }

    public String getIcon32Filename() {
        return icon32filename;
    }

    @Override
    public int hashCode() {
        return index;
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof DcModule ? ((DcModule) o).getIndex() == getIndex() : false);
    }

    public XmlModule getXmlModule() {
        return xmlModule;
    }

    public void setXmlModule(XmlModule xmlModule) {
        this.xmlModule = xmlModule;
    }    
    
    public String getLabel() {
        return label;
    }
    
    public int compareTo(DcModule module) {
        return getLabel().toLowerCase().compareTo(module.getLabel().toLowerCase());
    }

    public DcObject[] getDefaultData() throws Exception  {
        String filename = getTableName() + ".data";
        File file = new File(DataCrow.moduleDir + "data/" + filename);
        
        DcObject[] data = null;
        if (file.exists()) {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            CSVReader csvReader = new CSVReader(reader, "\t", null);
            List<String[]> lines = csvReader.readAll();  
            
            data = new DcObject[lines.size() - 1];
            
            String[] headers = lines.get(0);
            for (int i = 1; i < lines.size(); i++) {
                Object[] line = lines.get(i);
                DcObject dco = getDcObject();
                for (int j = 0; j < headers.length; j++) {
                    Object value = line[j];
                    if (value != null && !value.equals("")) {
                        for (DcField field : dco.getFields()) {
                            if (field.getSystemName().equalsIgnoreCase(headers[j])) {
                                if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                                    Picture picture = new Picture();
                                    picture.isNew(true);
                                    picture.setValue(Picture._C_FILENAME, DataCrow.baseDir + value);
                                    picture.setValue(Picture._D_IMAGE, new DcImageIcon(DataCrow.baseDir + value));
                                    value = picture;
                                } else if (field.getValueType() == DcRepository.ValueTypes._ICON) {
                                    String s = DataCrow.baseDir + value;
                                    value = Utilities.fileToBase64String(new URL("file://" + s));
                                }
                                
                                dco.setValue(field.getIndex(), value);
                                break;
                            }
                        }                        
                    }
                }
                data[i - 1] = dco;
            }
        }
        
        return data;
    }

    @Override
    public String toString() {
        return getName();
    }
    
    /**
     * Deletes this module. Cannot be undone.
     */
    public void delete() throws Exception {
        if (getXmlModule() != null && !new ModuleJar(getXmlModule()).delete())
            throw new Exception("Module file could not be deleted. " +
                                "Please check the access rights for file: " + getXmlModule().getFilename()); 

        
        if (this instanceof DcPropertyModule && getXmlModule() != null && !getXmlModule().isServingMultipleModules()) {
            // We are (or might be) working on a property base module with a calculated tablename
            DcModule module = DcModules.get(getName()); 
            DatabaseManager.executeQuery("DROP TABLE " + module.getTableName(), Query._DELETE, true);
        } else {
            DatabaseManager.executeQuery("DROP TABLE " + getTableName(), Query._DELETE, true);
            if (getTemplateModule() != null)
                getTemplateModule().delete();
        }
    }
}
