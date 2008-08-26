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

package net.datacrow.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.ModuleJar;
import net.datacrow.core.modules.xml.XmlField;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.modules.xml.XmlObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.StringUtils;
import net.datacrow.util.XMLParser;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Upgrades the actual module jar file. Fields can be added, removed or altered.
 * @author rj.vanderwaals
 */
public class ModuleUpgrade extends XmlObject {
    
    private static Logger logger = Logger.getLogger(ModuleUpgrade.class.getName());
    
    private File add;
    private File alter;
    private File remove;
    
    /**
     * @param xml
     * @return
     * @throws ModuleUpgradeException
     */
    public byte[] upgrade(byte[] xml) throws ModuleUpgradeException {
        String s;
        try {
            s = new String(xml, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            s = new String(xml);
        }
        
        // check the version. for older version; add the version if it does not exist yet.
        String version = StringUtils.getValueBetween("<product-version>", "</product-version>", s);
        if (version.length() == 0) {
            int idx = s.indexOf("</index>") + 8;
            s = s.substring(0, idx) + 
                "\r\n        <product-version>" + DataCrow.getVersion().toString() + "</product-version>" + 
                s.substring(idx);
        }
        
        Version v = new Version(version);
        if (v.equals(DataCrow.getVersion())) {
            return xml;  
        } else {
            return upgrade(s, version);
        } 
    }
    
    private static byte[] upgrade(String s, String version)  {
        int index = Integer.valueOf(StringUtils.getValueBetween("<index>", "</index>", s));
        
        if (index == DcModules._MUSICALBUM)
            s = s.replaceAll("OnlineAudioCdSearchForm", "OnlineMusicAlbumSearchForm");
        
        s = s.replaceAll("online-search-ui-class", "onlinesearch-class");
        s = s.replaceAll("import-ui-class", "importer-class");
        
        s = s.replaceAll("<readonly>true</readonly>", "<readonly>false</readonly>");
        
        // package names have changed
        s = s.replaceAll("net.sf.dc", "net.datacrow");
        
        // object definitions have changed
        s = s.replaceAll("net.datacrow.core.objects.audiocd", "net.datacrow.core.objects.helpers");
        s = s.replaceAll("net.datacrow.core.objects.book", "net.datacrow.core.objects.helpers");
        s = s.replaceAll("net.datacrow.core.objects.contactperson", "net.datacrow.core.objects.helpers");
        s = s.replaceAll("net.datacrow.core.objects.generic", "net.datacrow.core.objects.helpers");
        s = s.replaceAll("net.datacrow.core.objects.image", "net.datacrow.core.objects.helpers");
        s = s.replaceAll("net.datacrow.core.objects.movie", "net.datacrow.core.objects.helpers");
        s = s.replaceAll("net.datacrow.core.objects.musicalbum", "net.datacrow.core.objects.helpers");
        s = s.replaceAll("net.datacrow.core.objects.software", "net.datacrow.core.objects.helpers");
        s = s.replaceAll("GenericObject", "Media");
        
        // class location changes
        s = s.replaceAll("net.datacrow.processes.synchronizer", "net.datacrow.synchronizers");
        s = s.replaceAll("net.datacrow.console.onlinesearch", "net.datacrow.console.windows.onlinesearch");
        
        s = s.replaceAll("net.datacrow.console.windows.onlinesearch.OnlineSoftwareSearchForm", "net.datacrow.onlinesearch.OnlineSoftwareSearch");
        s = s.replaceAll("net.datacrow.console.windows.onlinesearch.OnlineAudioCdSearchForm", "net.datacrow.onlinesearch.OnlineAudioCdSearch");
        s = s.replaceAll("net.datacrow.console.windows.onlinesearch.OnlineBookSearchForm", "net.datacrow.onlinesearch.OnlineBookSearch");
        s = s.replaceAll("net.datacrow.console.windows.onlinesearch.OnlineMoviePersonSearchForm", "net.datacrow.onlinesearch.OnlineMoviePersonSearch");
        s = s.replaceAll("net.datacrow.console.windows.onlinesearch.OnlineMovieSearchForm", "net.datacrow.onlinesearch.OnlineMovieSearch");
        s = s.replaceAll("net.datacrow.console.windows.onlinesearch.OnlineMusicAlbumSearchForm", "net.datacrow.onlinesearch.OnlineMusicAlbumSearch");

        // person renamed to associate
        s = s.replaceAll("DcPerson", "DcAssociate");
        
        if (index == DcModules._MUSICTRACK)
            s = s.replaceAll("<importer-class />", 
                             "<importer-class>net.datacrow.fileimporters.MusicAlbumImporter</importer-class>");
        
        if (index == DcModules._MUSICARTIST) {
            s = s.replaceAll("<onlinesearch-class></onlinesearch-class>", 
                             "<onlinesearch-class>net.datacrow.onlinesearch.OnlineArtistSearch</onlinesearch-class>");
            s = s.replaceAll("<synchronizer-class></synchronizer-class>", 
                             "<synchronizer-class>net.datacrow.synchronizers.AssociateSynchronizer</synchronizer-class>");
            s = s.replaceAll("<is-serving-multiple-modules>false</is-serving-multiple-modules>", 
                             "<is-serving-multiple-modules>true</is-serving-multiple-modules>");
        }

        if (    index == DcModules._ACTOR || index == DcModules._DEVELOPER ||
                index == DcModules._SOFTWAREPUBLISHER || index == DcModules._DIRECTOR)
            s = s.replaceAll("<synchronizer-class></synchronizer-class>", 
                             "<synchronizer-class>net.datacrow.synchronizers.AssociateSynchronizer</synchronizer-class>");
        
        if (index == DcModules._BOOK)
            s = s.replaceAll("<importer-class></importer-class>", "<importer-class>net.datacrow.fileimporters.EbookImport</importer-class>");
        
        if (index == DcModules._SOFTWAREPUBLISHER || index == DcModules._DEVELOPER) 
            s = s.replaceAll("<onlinesearch-class></onlinesearch-class>", 
                             "<onlinesearch-class>net.datacrow.onlinesearch.OnlineSoftwareCompanySearch</onlinesearch-class>");
        
        String currentVersion = DataCrow.getVersion().toString();
        s = s.replace("<product-version>" + version + "</product-version>", 
                      "<product-version>" + currentVersion + "</product-version>");
        
        if (s.indexOf("<is-file-backed>") == -1) {
            boolean isFileBacked = index == DcModules._BOOK || index == DcModules._IMAGE ||
                                   index == DcModules._MOVIE || index == DcModules._MUSICTRACK ||
                                   index == DcModules._MUSICTRACK || index == DcModules._SOFTWARE;
            
            int idx = s.indexOf("</index>") + 8;
            s = s.substring(0, idx) + 
                "\r\n        <is-file-backed>" + isFileBacked + "</is-file-backed>" +
                s.substring(idx);
        }
        
        if (s.indexOf("<is-container-managed>") == -1) {
            boolean isContainerManaged = 
                s.indexOf("<module-class>net.datacrow.core.modules.DcMediaModule</module-class>") > -1 ||
                index == DcModules._AUDIOCD || index == DcModules._BOOK || index == DcModules._IMAGE ||
                index == DcModules._MOVIE || index == DcModules._MUSICALBUM || index == DcModules._SOFTWARE;
                
            int idx = s.indexOf("</index>") + 8;
            s = s.substring(0, idx) + 
                "\r\n        <is-container-managed>" + isContainerManaged + "</is-container-managed>" +
                s.substring(idx);
        }
        
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            return s.getBytes();
        }
    }
    
    private void removeDuplicates() {
        String[] modules = new File(DataCrow.moduleDir).list();
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

    
    public void upgrade() throws ModuleUpgradeException {
        removeDuplicates();
        
        add = new File(DataCrow.baseDir + "upgrade/add.xml");
        alter = new File(DataCrow.baseDir + "upgrade/alter.xml");
        remove = new File(DataCrow.baseDir + "upgrade/remove.xml");
        
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

    private void save(XmlModule module) throws Exception {
        new ModuleJar(module).save();
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
            
            save(xmlModule);
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
            
            save(xmlModule);
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
            
            save(xmlModule);
        }        
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
