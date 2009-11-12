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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.security.PermissionModule;
import net.datacrow.core.modules.security.UserModule;
import net.datacrow.core.modules.upgrade.ModuleUpgradeException;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.StringUtils;
import net.datacrow.util.filefilters.FileNameFilter;

import org.apache.log4j.Logger;

/**
 * This class registers and manages all modules present within Data Crow. 
 * 
 * Things to know:
 * - Property modules are stored in "propertyBaseModules".
 *   These modules are used as templates for the actual references.
 *   (DcPropertyModule.getIndex() + DcModule.getIndex())
 * - Multi relation references are stored in the same way except they also get a
 *   mapping module (&lt;ParentModule&gt;.getIndex() + &lt;ReferencedModule&gt;.getIndex() + DcModules._MAPPING)
 * - If defined, a base property module serves multiple modules (such as the music genres).
 * 
 * @author Robert Jan van der Waals
 */
public class DcModules {
    
    private static Logger logger = Logger.getLogger(DcModules.class.getName());

    public static final int _SOFTWARE = 50;
    public static final int _MOVIE = 51;
    public static final int _MUSICALBUM = 52;
    public static final int _AUDIOCD = 53;
    public static final int _BOOK = 54;
    public static final int _IMAGE = 55;
    public static final int _CONTACTPERSON = 56;
    public static final int _MEDIA = 57;
    public static final int _MUSICTRACK = 58;
    public static final int _AUDIOTRACK = 59;
    public static final int _PICTURE = 60;
    public static final int _LOAN = 61;
    public static final int _USER = 62;
    public static final int _PERMISSION = 63;
    public static final int _CONTAINER = 64;
    public static final int _ITEM = 65;
    
    public static final int _CATEGORY = 10000;
    public static final int _STORAGEMEDIA = 11000;
    public static final int _PLATFORM = 12000;
    public static final int _TEMPLATE = 13000;
    public static final int _MUSICGENRE = 14000;
    public static final int _STATE = 15000;
    public static final int _GENRE = 16000;
    public static final int _CONTAINERTYPE = 17000;
    public static final int _TAB = 18000;
    
    public static final int _ACTOR = 30000;
    public static final int _DIRECTOR = 31000;
    public static final int _AUTHOR = 32000;
    public static final int _BOOKPUBLISHER = 33000;
    public static final int _SOFTWAREPUBLISHER = 34000;
    public static final int _DEVELOPER = 35000;
    public static final int _MUSICARTIST = 36000;
    
    public static final int _COUNTRY = 1000000;
    public static final int _LANGUAGE = 1100000;
    public static final int _BINDING = 1200000;
    public static final int _EDITIONTYPE = 1300000;
    public static final int _EXTERNALREFERENCE = 1400000;
    public static final int _MOVIE_COLOR = 1500000;
    public static final int _MOVIE_ASPECT_RATIO = 1600000;
    
    public static final int _MAPPING = 50000;

    private static final Map<Integer, DcPropertyModule> propertyBaseModules = new HashMap<Integer, DcPropertyModule>();
    private static final Map<Integer, DcModule> modules = new LinkedHashMap<Integer, DcModule>();
    
    /**
     * Loads all modules.
     * @throws ModuleUpgradeException
     * @throws InvalidModuleXmlException
     * @throws ModuleJarException
     */
    public static void load() throws ModuleUpgradeException, InvalidModuleXmlException, ModuleJarException {
        propertyBaseModules.clear();
        modules.clear();
        
        initReferenceBaseModules();
        loadSystemModules();
        loadModuleJars();
    }
    
    public static boolean isTopModule(int moduleIdx) {
        DcModule module = get(moduleIdx);
        return moduleIdx == DcModules._CONTACTPERSON || 
               moduleIdx == DcModules._MEDIA ||
               moduleIdx == DcModules._CONTAINER || 
              (module.isTopModule() && !module.hasDependingModules());      
    }
    
    /**
     * Save all module jar files
     */
    public static void save() {
        for (DcModule module : getAllModules()) {
            try {
                XmlModule xmlModule = module.getXmlModule();
                if (xmlModule != null && xmlModule.isChanged() && !module.isAbstract()) 
                    new ModuleJar(module.getXmlModule()).save();
            } catch (Exception e) {
                logger.error("An error occurred while saving the module jar for module " +
                             module.getLabel(), e);
            }
        }
    }
    
    /**
     * Registers the base modules. These modules are used as template.
     */
    private static void initReferenceBaseModules() {
        
        propertyBaseModules.put(DcModules._EXTERNALREFERENCE, new ExternalReferenceModule());
        
        propertyBaseModules.put(DcModules._CATEGORY, 
                       new DcPropertyModule(DcModules._CATEGORY, "Category", "category", "cat", "Category", "Categories"));
        
        DcPropertyModule stateModule = new DcPropertyModule(DcModules._STATE, "State", "state", "st", "State", "States");
        stateModule.setIcon16(IconLibrary._icoState16);
        stateModule.setIcon32(IconLibrary._icoState32);
        propertyBaseModules.put(DcModules._STATE, stateModule);
        
        DcPropertyModule platformModule = new DcPropertyModule(DcModules._PLATFORM, "Platform", "platform", "pf", "Platform", "Platforms");
        platformModule.setIcon16(IconLibrary._icoPlatform16);
        platformModule.setIcon32(IconLibrary._icoPlatform32);
        propertyBaseModules.put(DcModules._PLATFORM, platformModule);    

        
        DcPropertyModule mediumModule = new DcPropertyModule(DcModules._STORAGEMEDIA, "Storage Medium", "storagemedium", "stme", "Storage Medium", "Storage Media");
        mediumModule.setIcon16(IconLibrary._icoStorageMedium16);
        mediumModule.setIcon32(IconLibrary._icoStorageMedium32);
        propertyBaseModules.put(DcModules._STORAGEMEDIA, mediumModule);
        
        propertyBaseModules.put(DcModules._CONTAINERTYPE, new DcPropertyModule(DcModules._CONTAINERTYPE, "Container Type", "containertype", "coty", "Container Type", "Container Types"));  
        propertyBaseModules.put(DcModules._GENRE, new DcPropertyModule(DcModules._GENRE, "Genre", "genre", "gr", "Genre", "Genres"));
        DcPropertyModule musicGenreMod = new DcPropertyModule(DcModules._MUSICGENRE, "Music Genre", "musicgenre", "musgr", "Music Genre", "Music Genres");
        
        musicGenreMod.setServingMultipleModules(true);
        propertyBaseModules.put(DcModules._MUSICGENRE, musicGenreMod);
        
        DcPropertyModule countryModule = new DcPropertyModule(DcModules._COUNTRY, "Country", "country", "country", "Country", "Countries");
        countryModule.setServingMultipleModules(true);
        propertyBaseModules.put(DcModules._COUNTRY, countryModule);
        countryModule.setIcon16(IconLibrary._icoCountry16);
        countryModule.setIcon32(IconLibrary._icoCountry32);
        
        DcPropertyModule languageModule = new DcPropertyModule(DcModules._LANGUAGE, "Language", "language", "language", "Language", "Languages");
        languageModule.setServingMultipleModules(true);
        languageModule.setIcon16(IconLibrary._icoLanguage16);
        languageModule.setIcon32(IconLibrary._icoLanguage32);
        propertyBaseModules.put(DcModules._LANGUAGE, languageModule);
        
        DcPropertyModule aspectRatioModule = new DcPropertyModule(DcModules._MOVIE_ASPECT_RATIO, "Aspect Ratio", "aspectratio", "asrt", "Aspect ratio", "Aspect ratios");
        aspectRatioModule.setServingMultipleModules(true);
        aspectRatioModule.setIcon16(IconLibrary._icoAspectRatio16);
        aspectRatioModule.setIcon32(IconLibrary._icoAspectRatio32);
        propertyBaseModules.put(DcModules._MOVIE_ASPECT_RATIO, aspectRatioModule);
        
        DcPropertyModule colorModule = new DcPropertyModule(DcModules._MOVIE_COLOR, "Color", "color", "clr", "Color", "Colors");
        colorModule.setServingMultipleModules(true);
        colorModule.setIcon16(IconLibrary._icoColor16);
        colorModule.setIcon32(IconLibrary._icoColor32);
        propertyBaseModules.put(DcModules._MOVIE_COLOR, colorModule);
        
        DcPropertyModule bindingModule = new DcPropertyModule(DcModules._BINDING, "Binding", "binding", "binding", "Binding", "Bindings");
        bindingModule.setServingMultipleModules(true);
        bindingModule.setIcon16(IconLibrary._icoBinding16);
        bindingModule.setIcon32(IconLibrary._icoBinding32);
        propertyBaseModules.put(DcModules._BINDING, bindingModule);
        
        DcPropertyModule editionModule = new DcPropertyModule(DcModules._EDITIONTYPE, "Edition Type", "editiontype", "edty", "Edition type", "Edition types");
        editionModule.setServingMultipleModules(true);
        propertyBaseModules.put(DcModules._EDITIONTYPE, editionModule);
    }
    
    /**
     * Determines the correct index for the mapping module. Note that these indices are
     * also used to determine the mapped modules indices.
     */
    public static int getMappingModIdx(int module, int referenceModIdx, int fieldIdx) {
        int baseModIdx = DcModules.get(module).getType() == DcModule._TYPE_TEMPLATE_MODULE ? 
                        ((TemplateModule) DcModules.get(module)).getTemplatedModule().getIndex() : module;
        
        return baseModIdx + referenceModIdx + DcModules._MAPPING + fieldIdx;
    }

    /**
     * Get a new, unused, index. Should only be used by the module wizards
     */
    public static int getAvailableIdx(XmlModule module) {
        int add = module.getModuleClass().equals(DcPropertyModule.class) ? 1000 : 1;
        int index = module.getModuleClass().equals(DcPropertyModule.class) ? 10000000 : 1;
        while (modules.containsKey(index) || propertyBaseModules.containsKey(index))
            index += add;
        
        return index;
    }    
    
    /**
     * Retrieves the user defined modules
     */
    public static Collection<DcModule> getCustomModules() {
        Collection<DcModule> modules = new ArrayList<DcModule>();
        for (DcModule module : getAllModules()) {
            if (module.isCustomModule())
                modules.add(module);
        }
        return modules;
    }

    /**
     * Initializes the system modules such as the picture and the loan module.
     */
    private static void loadSystemModules() {
        register(new PictureModule());
        register(new LoanModule());
        register(new UserModule());
        register(new PermissionModule());
        register(new TabModule());
    }
    
    /**
     * Retrieves and registers the jar files from the modules folder. 
     * @throws ModuleUpgradeException
     * @throws InvalidModuleXmlException
     * @throws ModuleJarException
     */
    private static void loadModuleJars() throws ModuleUpgradeException, InvalidModuleXmlException, ModuleJarException  {
        File file = new File(DataCrow.moduleDir);
        String[] files = file.list(new FileNameFilter("jar", false));
        
        Collection<XmlModule> dependingMods = new ArrayList<XmlModule>();
        Collection<XmlModule> masterMods = new ArrayList<XmlModule>();
        
        for (String filename : files) {
            ModuleJar mj = new ModuleJar(filename);
            mj.load();
                
            XmlModule xmlModule = mj.getModule();
            if (xmlModule.getModuleClass().equals(DcPropertyModule.class))
                registerBasePropertyModule((DcPropertyModule) convert(xmlModule));
            else if (xmlModule.hasDependingModules()) 
                masterMods.add(xmlModule);
            else 
                dependingMods.add(xmlModule);
        }
        
        // First register the modules on which other modules depend (obviously) 
        for (XmlModule xmlModule : masterMods)
            register(convert(xmlModule));
        
        for (XmlModule xmlModule : dependingMods)
            register(convert(xmlModule));
 
        // Lastly, register any property module referenced by the registered modules
        for (DcModule module : new ArrayList<DcModule>(modules.values())) {
            if (!(module instanceof DcPropertyModule))
                registerPropertyModules(module);
        }
    }
    
    /**
     * Converts a XmlModule to a real module.
     */
    @SuppressWarnings("unchecked")
    public static DcModule convert(XmlModule xmlModule) {
        try {
            Class parent = xmlModule.getModuleClass();
            return (DcModule) parent.getConstructor(new Class[] {XmlModule.class}).newInstance(new Object[] {xmlModule});
        } catch (Exception exp) {
            logger.error("Could not instantiate " + xmlModule.getModuleClass() + " or it is of the wrong type.", exp);
        }
        return null;
    }

    /**
     * Holds the global property modules. These are used as a template to create
     * specific referenced modules from.
     */
    public static void registerBasePropertyModule(DcPropertyModule module) {
        propertyBaseModules.put(module.getIndex(), module);
    }
    
    /**
     * A direct add of the module to the modules list. An error is logged if the module
     * was already registered.
     */
    public static void register(DcModule module) {
        
        if (logger.isDebugEnabled()) {
            String name = module.getName();
            name = name == null || name.trim().length() == 0 ? module.getTableName() : name;
            logger.debug("Registering module [" + name + "]");
        }
        
        if (!modules.containsKey(module.getIndex())) {
            modules.put(module.getIndex(), module);
        } else if (!module.isServingMultipleModules()){
            logger.error("Module [" + module + "] has a conflicting index of [" + module.getIndex() + "] or has already been registered." + 
                        "(Registered module: name [" + modules.get(module.getIndex()) + "])");
        }
    }

    /**
     * Registers the property modules referenced by the given module
     */
    public static void registerPropertyModules(DcModule module) {
        if (module.isAbstract()) // special case
            return;
        
        for (DcField field : module.getFields()) {
                DcModule mod = module.getType() == DcModule._TYPE_TEMPLATE_MODULE  ? 
                              ((TemplateModule) module).getTemplatedModule() : module; 
       
            int sourceIdx = field.getSourceModuleIdx();
            int derivedIdx = sourceIdx;

            if (propertyBaseModules.containsKey(sourceIdx)) {
                DcPropertyModule propMod = propertyBaseModules.get(sourceIdx);
                
                if (propMod.isServingMultipleModules()) {
                    // A module which serves multiple other modules gets the same table name..
                    // Also the module will keep its original index. No other magic involved.
                    DcPropertyModule pm = propMod.getInstance(derivedIdx, propMod.getName(),  
                            propMod.getTableName(), propMod.getTableShortName(),
                            propMod.getSystemObjectName(), propMod.getSystemObjectNamePlural());

                    pm.setServingMultipleModules(true);
                    addUserDefinedFields(propMod, pm);
                    register(pm);
                } else {
                    // Else.. derive a new index and assign a new and unique table name.
                    derivedIdx = sourceIdx + mod.getIndex();
                    DcPropertyModule pm = propMod.getInstance(derivedIdx, 
                            StringUtils.capitalize(module.getName()) + " " + StringUtils.capitalize(propMod.getName()),  
                            mod.getTableName() + "_" + propMod.getTableName(), 
                            mod.getTableShortName() + propMod.getTableShortName(),
                            propMod.getSystemObjectName(), propMod.getSystemObjectNamePlural());

                    addUserDefinedFields(propMod, pm);
                    register(pm);
                }
            }
            
            // register the mapping module using the derived index.
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION)
                register(new MappingModule(mod, DcModules.get(derivedIdx), field.getIndex()));
        }
        
        if (module.isTopModule() || module.isChildModule())
            register(new TemplateModule(module));
    }
    
    /**
     * As users can define extra fields for base property modules these fields need to be
     * added to the property module when assigned (by default a property module has only a name 
     * and an icon field). 
     */
    private static void addUserDefinedFields(DcPropertyModule base, DcPropertyModule pm) {
        for (DcField fld1 : base.getFields()) {
            boolean exists = false;
            for (DcField fld2 : pm.getFields())
                exists = fld2.getIndex() == fld1.getIndex() ? true : exists;
            
            if (!exists) {
                DcField field = new DcField(fld1.getIndex(), pm.getIndex(), fld1.getLabel(), fld1.isUiOnly(), 
                                            fld1.isEnabled(), fld1.isReadOnly(), fld1.isSearchable(), 
                                            fld1.isTechnicalInfo(), fld1.getMaximumLength(), 
                                            fld1.getFieldType(), pm.getIndex(), fld1.getValueType(), 
                                            fld1.getDatabaseFieldName());
                pm.addField(field);
            }
        }
        
        pm.setIcon16(base.getIcon16());
        pm.setIcon32(base.getIcon32());
        pm.setXmlModule(base.getXmlModule());
        pm.initializeSettings();
    }
    
    /**
     * Retrieves the property modules used by the specified module.
     * References are determined by the fields of the module.
     */
    public static Collection<DcPropertyModule> getPropertyModules(DcModule module) {
        Collection<DcPropertyModule> references = new ArrayList<DcPropertyModule>();
        for (DcField field : module.getFields()) {
            DcPropertyModule pm = getPropertyModule(field);
            if (pm != null && !references.contains(pm))
                references.add(pm);
        }
        return references;
    }
    
    /**
     * Retrieve the referenced module for the given field. This can either be the module
     * to which the field belongs, the referenced module or null.
     * @param field
     */
    public static DcModule getReferencedModule(DcField field) {
        int parentModule = DcModules.get(field.getModule()).getType() == DcModule._TYPE_TEMPLATE_MODULE ?
                           ((TemplateModule) DcModules.get(field.getModule())).getTemplatedModule().getIndex() : 
                           field.getModule();
        
        DcModule module = DcModules.get(field.getSourceModuleIdx());
        return module == null ? DcModules.get(field.getSourceModuleIdx() + parentModule) : module;
    }
    
    /**
     * Retrieve the referenced property module for the given field. Will return null when
     * the field does not contain a reference to a property module.
     * @param field
     */
    public static DcPropertyModule getPropertyModule(DcField field) {
        DcModule module = getReferencedModule(field);
        return module instanceof DcPropertyModule ? (DcPropertyModule) module : null;
    }

    /**
     * Retrieves the property base modules which are used as templates.
     */
    public static Collection<DcPropertyModule> getPropertyBaseModules() {
        return propertyBaseModules.values();
    }
    
    /**
     * Retrieves the property base module which can be used as template.
     * @param module The module index
     */
    public static DcPropertyModule getPropertyBaseModule(int module) {
        return propertyBaseModules.get(module);
    }    

    /**
     * Get the module based on its internal name.
     * The internal name differs, in most cases, from the label.
     * @param name
     */
    public static DcModule get(String name) {
        for (DcModule module : modules.values()) {
            if (module.getName().equalsIgnoreCase(name))
                return module;
        }
        return null; 
    }

    /**
     * Get the module for the specified key. 
     * @param key
     */
    public static DcModule get(int key) {
        return modules.get(key);
    }

    /**
     * Remove the specified module.
     */
    public static void remove(int key) {
        modules.remove(key);
    }    
    
    /**
     * Returns the currently selected main module.
     */
    public static DcModule getCurrent() {
        DcModule current = DcModules.get(DcSettings.getInt(DcRepository.Settings.stModule));
        
        if (current == null)
            current = DcModules.get(DcModules._SOFTWARE);
        
        if (current.getIndex() == DcModules._CONTAINER &&
            current.getSettings().getInt(DcRepository.ModuleSettings.stTreePanelShownItems) == DcModules._ITEM) {
            
            current = DcModules.get(DcModules._ITEM);
        }
        
        if (SecurityCentre.getInstance().getUser().isAuthorized(current))
            return current;
        
        return null;
    }   
    
    /**
     * Apply all settings on the item.
     * @param definitions
     */
    public static void applySettings() {
        for (DcModule module : getModules())
            module.applySettings();
    }

    /**
     * Finds the module based on the table name and creates a new item.
     * @param tableName The database table name.
     */
    public static DcObject getObjectForTable(String tableName) {
        for (DcModule module : modules.values()) {
            if (module.getTableName().equalsIgnoreCase(tableName))
                return module.getItem();
        }
        return null;
    }

    /**
     * Start to load data for all modules
     */
    public static void loadData() {
        new DataManager();
    }

    public static boolean isUsedInMapping(int modIdx) {
        for (DcModule module : getAllModules()) {
            if (module instanceof MappingModule) {
                MappingModule mappingMod = (MappingModule) module;
                if (mappingMod.getReferencedModIdx() == modIdx) 
                    return true;
            }
        }
        
        return false;
    }
    
    public static Collection<DcModule> getReferencedModules(int moduleIdx) {
        Collection<DcModule> references = new ArrayList<DcModule>();
        for (DcField field : DcModules.get(moduleIdx).getFields()) {
            int referenceIdx = field.getReferenceIdx();
            if (referenceIdx != moduleIdx && referenceIdx > 0) {
                DcModule module = DcModules.get(referenceIdx);
                if (!references.contains(module))
                    references.add(module);
            }
        }
        return references;
    }  
    
    /** 
     * Retrieves all modules having a reference to the specified modules.
     * Note: The check is based on the source module index (base module), 
     * not the actual instances.
     * @see #getActualReferencingModules(int)
     * @param moduleIdx
     */
    public static Collection<DcModule> getReferencingModules(int moduleIdx) {
        Collection<DcModule> refs = new ArrayList<DcModule>();
        for (DcModule module : getAllModules()) {
            if (module.getIndex() != moduleIdx && module.getType() != DcModule._TYPE_TEMPLATE_MODULE) {
                if (module.hasReferenceTo(moduleIdx))
                    refs.add(module);
            }
        }
        return refs;
    }    
    
    /** 
     * Retrieves all modules having a reference to the specified modules.
     * Note: The check is based on the actual module index (the calculated module index).
     * @see #getReferencingModules(int)
     * @param moduleIdx
     */    
    public static Collection<DcModule> getActualReferencingModules(int moduleIdx) {
        Collection<DcModule> refs = new ArrayList<DcModule>();
        for (DcModule module : getAllModules()) {
            if ( module.getIndex() != moduleIdx && 
                 module.getType() != DcModule._TYPE_TEMPLATE_MODULE &&
               !(module.getType() == DcModule._TYPE_MAPPING_MODULE && 
                ((MappingModule) module).getParentModIdx() == moduleIdx)) {
                
                if (module.hasActualReferenceTo(moduleIdx))
                    refs.add(module);
            }
        }
        return refs;
    } 

    /**
     * Retrieves all modules
     */
    public static Collection<DcModule> getAllModules() {
        List<DcModule> c = new ArrayList<DcModule>(modules.values());
        Collections.sort(c); 
        return c;
    }
    
    /**
     * Retrieves all enabled modules
     */
    public static Collection<DcModule> getModules() {
        Collection<DcModule> c = new ArrayList<DcModule>();
        
        for (DcModule module : getAllModules()) {
            if (module.isEnabled())
                c.add(module);
        }
        
        return c;
    }
}