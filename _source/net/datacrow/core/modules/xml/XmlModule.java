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

package net.datacrow.core.modules.xml;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.datacrow.core.ModuleUpgrade;
import net.datacrow.core.ModuleUpgradeException;
import net.datacrow.core.modules.InvalidModuleXmlException;
import net.datacrow.core.modules.InvalidValueException;
import net.datacrow.core.objects.DcObject;
import net.datacrow.util.XMLParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A XML representation of a module.
 * 
 * @author Robert Jan van der Waals
 */
@SuppressWarnings("unchecked")
public class XmlModule extends XmlObject {
    
    private Document document;
    
    private Class synchronizer;
    private Class importer;
    private Class moduleClass;
    private Class object;
    
    private int index;
    private int displayIndex;
    private int childIndex = -1;
    private int parentIndex = -1;
    private int nameFieldIdx = 150;
    
    private String label;
    private String name;
    private String description;
    private KeyStroke keyStroke;
    
    private String filename;
    
    private String productVersion;
    
    private String icon16Filename;
    private String icon32Filename;
    
    private byte[] icon16;
    private byte[] icon32;
    
    private String objectName;
    private String objectNamePlural;
    
    private int defaultSortFieldIdx = DcObject._ID;
    
    private boolean changed = false;
    
    private boolean enabled;
    private boolean canBeLended;
    private boolean hasSearchView;
    private boolean hasInsertView;
    private boolean hasDependingModules;
    private boolean isServingMultipleModules;
    private boolean isFileBacked;
    private boolean isContainerManaged;

    private String tableName;
    private String tableNameShort;
    
    private Collection<XmlField> fields = new ArrayList<XmlField>();    
    private String[] services;
    
    public XmlModule() {}

    public XmlModule(byte[] xml) throws InvalidModuleXmlException, ModuleUpgradeException {
        byte[] newXml = new ModuleUpgrade().upgrade(xml);
        
        boolean same = newXml.length == xml.length;
        if (same) {
            for (int i = 0; i < newXml.length; i++)
                same &= newXml[i] == xml[i];
        }
        
        initialize(newXml);
        changed = !same;
    }

    public boolean isChanged() {
        return changed;
    }
    
    private void initialize(byte[] xml) throws InvalidModuleXmlException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            ByteArrayInputStream bis = new ByteArrayInputStream(xml);
            document = db.parse(bis);
            load();
        } catch(Exception e) {
            throw new InvalidModuleXmlException(getName(), e);
        }
    }

    private void load() throws InvalidValueException {
        Element element = document.getDocumentElement();
        NodeList nodes = element.getElementsByTagName("module");
        Element module = (Element) nodes.item(0);
        
        if (XMLParser.getString(element, "synchronizer-class") != null) 
            synchronizer = getClass(module, "synchronizer-class", true);
        
        if (XMLParser.getString(element, "importer-class") != null)
            importer = getClass(module, "importer-class", true);
        
        moduleClass = getClass(module, "module-class", true);
        object = getClass(module, "object-class", true);
        index = XMLParser.getInt(module, "index");
        
        if (XMLParser.getString(element, "display-index") != null)
            displayIndex = XMLParser.getInt(module, "display-index");
        
        if (XMLParser.getString(element, "child-module") != null)
            childIndex = XMLParser.getInt(module, "child-module");
        
        if (XMLParser.getString(element, "parent-module") != null)
            parentIndex = XMLParser.getInt(module, "parent-module");

        isFileBacked = XMLParser.getBoolean(module, "is-file-backed");
        isContainerManaged = XMLParser.getBoolean(module, "is-container-managed");
        isServingMultipleModules = XMLParser.getBoolean(module, "is-serving-multiple-modules");
        tableName =XMLParser.getString(module, "table-name");
        tableNameShort = XMLParser.getString(module, "table-short-name");
        label = XMLParser.getString(module, "label");
        productVersion = XMLParser.getString(module, "product-version");
        name = XMLParser.getString(module, "name");
        description = XMLParser.getString(module, "description");
        objectName = XMLParser.getString(module, "object-name");
        objectNamePlural = XMLParser.getString(module, "object-name-plural");
        enabled = XMLParser.getBoolean(module, "enabled");
        canBeLended = XMLParser.getBoolean(module, "can-be-lended");
        hasSearchView = XMLParser.getBoolean(module, "has-search-view");
        hasInsertView = XMLParser.getBoolean(module, "has-insert-view");
        nameFieldIdx = XMLParser.getInt(module, "name-field-index");
        
        if (XMLParser.getString(element, "default-sort-field-index") != null)
            defaultSortFieldIdx = XMLParser.getInt(module, "default-sort-field-index");

        keyStroke = XMLParser.getKeyStroke(module, "key-stroke");
        icon16Filename = XMLParser.getString(module, "icon-16");
        icon32Filename = XMLParser.getString(module, "icon-32");
        hasDependingModules = XMLParser.getBoolean(module, "has-depending-modules");
        
        setFields(this, module);
        setServices(module);
    }
    
    private void setFields(XmlModule module, Element element) throws InvalidValueException {
        NodeList nodes = element.getElementsByTagName("field");
        for (int i = 0; i < nodes.getLength(); i++) {
            fields.add(new XmlField(module, (Element) nodes.item(i)));
        }
    }
    
    private void setServices(Element module) {
        NodeList nodes = module.getElementsByTagName("online-service");
        services = new String[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            services[i] = XMLParser.getString(element, "class");
        }
    }      

    public boolean canBeLended() {
        return canBeLended;
    }

    public int getChildIndex() {
        return childIndex;
    }

    public String getDescription() {
        return description;
    }

    public Document getDocument() {
        return document;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Collection<XmlField> getFields() {
        return fields;
    }

    public boolean hasInsertView() {
        return hasInsertView;
    }

    public boolean hasSearchView() {
        return hasSearchView;
    }

    public Class getImporter() {
        return importer;
    }
    
    public int getIndex() {
        return index;
    }

    public KeyStroke getKeyStroke() {
        return keyStroke;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public Class getObject() {
        return object;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getObjectNamePlural() {
        return objectNamePlural;
    }

    public Class getModuleClass() {
        return moduleClass;
    }

    public String[] getServices() {
        return services;
    }

    public Class getSynchronizer() {
        return synchronizer;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableNameShort() {
        return tableNameShort;
    } 
    
    public boolean hasDependingModules() {
        return hasDependingModules;
    }

    public int getDisplayIndex() {
        return displayIndex;
    }

    public int getDefaultSortFieldIdx() {
        return defaultSortFieldIdx;
    }

    public int getParentIndex() {
        return parentIndex;
    }

    public int getNameFieldIdx() {
        return nameFieldIdx;
    }

    public void setCanBeLended(boolean canBeLended) {
        this.canBeLended = canBeLended;
    }

    public void setChildIndex(int childIndex) {
        this.childIndex = childIndex;
    }

    public void setDefaultSortFieldIdx(int defaultSortFieldIdx) {
        this.defaultSortFieldIdx = defaultSortFieldIdx;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDisplayIndex(int displayIndex) {
        this.displayIndex = displayIndex;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setFields(Collection<XmlField> fields) {
        this.fields = fields;
    }

    public void setHasDependingModules(boolean hasDependingModules) {
        this.hasDependingModules = hasDependingModules;
    }

    public void setHasInsertView(boolean hasInsertView) {
        this.hasInsertView = hasInsertView;
    }

    public void setHasSearchView(boolean hasSearchView) {
        this.hasSearchView = hasSearchView;
    }

    public void setImporter(Class importer) {
        this.importer = importer;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setKeyStroke(KeyStroke keyStroke) {
        this.keyStroke = keyStroke;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setModuleClass(Class moduleClass) {
        this.moduleClass = moduleClass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNameFieldIdx(int nameFieldIdx) {
        this.nameFieldIdx = nameFieldIdx;
    }

    public void setObject(Class object) {
        this.object = object;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void setObjectNamePlural(String objectNamePlural) {
        this.objectNamePlural = objectNamePlural;
    }

    public void setParentIndex(int parentIndex) {
        this.parentIndex = parentIndex;
    }

    public void setServices(String[] services) {
        this.services = services;
    }

    public void setSynchronizer(Class synchronizer) {
        this.synchronizer = synchronizer;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTableNameShort(String tableNameShort) {
        this.tableNameShort = tableNameShort;
    }

    public String getIcon16Filename() {
        return icon16Filename;
    }

    public void setIcon16Filename(String icon16Filename) {
        this.icon16Filename = icon16Filename;
    }

    public String getIcon32Filename() {
        return icon32Filename;
    }

    public void setIcon32Filename(String icon32Filename) {
        this.icon32Filename = icon32Filename;
    }

    public boolean isFileBacked() {
        return isFileBacked;
    }

    public void setFileBacked(boolean isFileBacked) {
        this.isFileBacked = isFileBacked;
    }
    
    public boolean isContainerManaged() {
        return isContainerManaged;
    }

    public void setContainerManaged(boolean isContainerManaged) {
        this.isContainerManaged = isContainerManaged;
    }

    public boolean isServingMultipleModules() {
        return isServingMultipleModules;
    }

    public void setServingMultipleModules(boolean isServingMultipleModules) {
        this.isServingMultipleModules = isServingMultipleModules;
    }

    public String getFilename() {
        return filename;
    }

    public String getJarFilename() {
    	return filename.replaceAll(".xml", ".jar");
    }
    
    public byte[] getIcon16() {
        return icon16;
    }

    public byte[] getIcon32() {
        return icon32;
    }
    
    public void setIcon16(byte[] b) {
    	if (b != null && b.length > 10)
    		icon16 = b;
    }

    public void setIcon32(byte[] b) {
    	if (b != null && b.length > 10)
    		icon32 = b;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }
}
