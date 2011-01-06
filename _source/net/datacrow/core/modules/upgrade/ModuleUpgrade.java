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

package net.datacrow.core.modules.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.datacrow.core.DataCrow;
import net.datacrow.core.modules.ModuleJar;
import net.datacrow.core.modules.xml.XmlField;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.modules.xml.XmlObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.XMLParser;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Upgrades the actual module jar file. Fields can be added, removed or altered.
 * 
 * @author Robert Jan van der Waals
 */
public class ModuleUpgrade extends XmlObject {
    
    private static Logger logger = Logger.getLogger(ModuleUpgrade.class.getName());
    
    private File add;
    private File alter;
    private File remove;
    
    /**
     * Upgrades the module based on a XML upgrade definition.
     * 
     * @param xml
     * @return
     * @throws ModuleUpgradeException
     */
    public byte[] upgrade(byte[] xml) throws ModuleUpgradeException {
        return xml;
    }
    
    public void upgrade() throws ModuleUpgradeException {
        removeDuplicates();
        add = new File(DataCrow.installationDir + "upgrade/add.xml");
        alter = new File(DataCrow.installationDir + "upgrade/alter.xml");
        remove = new File(DataCrow.installationDir + "upgrade/remove.xml");
        
        try {
            
            if (remove.exists())
                remove();
            
            if (add.exists())
                add();
            
            if (alter.exists())
                alter();
                
        } catch (Exception exp) {
            throw new ModuleUpgradeException(exp);
        }
    }

    private void save(XmlModule module, String filename) throws Exception {
        ModuleJar mj = new ModuleJar(module);
        mj.setFilename(filename);
        mj.save();
    }
    
    private XmlField getField(int index, Collection<XmlField> fields) {
        for (XmlField field : fields)
            if (field.getIndex() == index) 
                return field;

        return null;
    }
    
    private void add() throws Exception {
        Document document = read(add);
        
        Element element = document.getDocumentElement();
        NodeList nodes = element.getElementsByTagName("module");
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Element module = (Element) nodes.item(i);
            String jarfile = XMLParser.getString(module, "module-jar");
            int index = XMLParser.getInt(module, "module-index");
            
            if (!new File(DataCrow.moduleDir + jarfile).exists()) continue;
            
            ModuleJar jar = new ModuleJar(jarfile);
            jar.load();
            
            // get the fields to add
            XmlModule xmlModule = jar.getModule();
            
            for (XmlField field :  getFields(module, index)) {
                if (getField(field.getIndex(), xmlModule.getFields()) == null) {
                    xmlModule.getFields().add(field);
                    logger.info(DcResources.getText("msgUpgradedModuleXAdded", 
                                new String[]{xmlModule.getName(), field.getName()}));                    
                }
            }
            
            save(xmlModule, jarfile);
        }
    }

    private void alter() throws Exception {
        Document document = read(alter);
        
        Element element = document.getDocumentElement();
        NodeList nodes = element.getElementsByTagName("module");
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Element module = (Element) nodes.item(i);
            String jarfile = XMLParser.getString(module, "module-jar");
            int index = XMLParser.getInt(module, "module-index");
            
            if (!new File(DataCrow.moduleDir + jarfile).exists()) continue;
            
            ModuleJar jar = new ModuleJar(jarfile);
            jar.load();
            
            // get the fields to add
            XmlModule xmlModule = jar.getModule();
            
            for (XmlField fieldNew :  getFields(module, index)) {
                XmlField fieldOrg = getField(fieldNew.getIndex(), xmlModule.getFields());
                
                if (fieldOrg == null) continue;

                fieldOrg.setColumn(fieldNew.getColumn());
                fieldOrg.setEnabled(fieldNew.isEnabled());
                fieldOrg.setFieldType(fieldNew.getFieldType());
                fieldOrg.setIndex(fieldNew.getIndex());
                fieldOrg.setMaximumLength(fieldNew.getMaximumLength());
                fieldOrg.setModuleReference(fieldNew.getModuleReference());
                fieldOrg.setName(fieldNew.getName());
                fieldOrg.setOverwritable(fieldNew.isOverwritable());
                fieldOrg.setReadonly(fieldNew.isReadonly());
                fieldOrg.setSearchable(fieldNew.isSearchable());
                fieldOrg.setTechinfo(fieldNew.isTechinfo());
                fieldOrg.setUiOnly(fieldNew.isUiOnly());
                fieldOrg.setValueType(fieldNew.getValueType());
                
                logger.info(DcResources.getText("msgUpgradedModuleXAltered", 
                            new String[]{xmlModule.getName(), fieldOrg.getName()}));
            }
            
            save(xmlModule, jarfile);
        }        
    }

    private void remove() throws Exception {
        Document document = read(remove);
        
        Element element = document.getDocumentElement();
        NodeList nodes = element.getElementsByTagName("module");
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Element module = (Element) nodes.item(i);
            String jarfile = XMLParser.getString(module, "module-jar");
            int index = XMLParser.getInt(module, "module-index");
            
            if (!new File(DataCrow.moduleDir + jarfile).exists()) continue;
            
            ModuleJar jar = new ModuleJar(jarfile);
            jar.load();
            
            // get the fields to add
            XmlModule xmlModule = jar.getModule();
            
            for (XmlField field :  getFields(module, index)) {
                XmlField fieldOrg = getField(field.getIndex(), xmlModule.getFields());
                if (fieldOrg != null) {
                    xmlModule.getFields().remove(fieldOrg);
                    logger.info(DcResources.getText("msgUpgradedModuleXRemoved", 
                                 new String[]{xmlModule.getName(), fieldOrg.getName()}));
                }
            }
            
            save(xmlModule, jarfile);
        }        
    }
    
    private void removeDuplicates() {
        String[] modules = new File(DataCrow.moduleDir).list();
        
        if (modules == null) return;
        
        for (String module : modules) {
            if (module.toLowerCase().endsWith(".jar")) {
                
                boolean containsUpper = false;
                for (char c : module.toCharArray()) {
                    if (Character.isUpperCase(c)) {
                        containsUpper = true;
                        break;
                    }
                }
                
                if (containsUpper)
                    removeDuplicate(module.toLowerCase(), module);
            }
        }
    }

    private void removeDuplicate(String lowercase, String uppercase) {
        String[] modules = new File(DataCrow.moduleDir).list();
        boolean foundLowercase = false;
        boolean foundUppercase = false;
        for (String module : modules) {
            if (module.equals(uppercase))
                foundUppercase = true;
            else if (module.equals(lowercase))
                foundLowercase = true;
        }
        
        if (foundLowercase && foundUppercase)
            new File(DataCrow.moduleDir + uppercase).delete();
    }    
    
    private Collection<XmlField> getFields(Element element, int module) throws Exception {
        Collection<XmlField> fields = new ArrayList<XmlField>(); 
        NodeList nodes = element.getElementsByTagName("field");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            XmlModule xmlModule = new XmlModule();
            xmlModule.setIndex(module);
            fields.add(new XmlField(xmlModule, el));
        }
        return fields;
    }    
    
    private Document read(File file) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        FileInputStream bis = new FileInputStream(file);
        return db.parse(bis);
    }
}
