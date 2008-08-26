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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.datacrow.core.DataCrow;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.ModuleUpgradeException;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.modules.xml.XmlModuleWriter;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class ModuleJar {

    private static Logger logger = Logger.getLogger(ModuleJar.class.getName());
    
    private String filename;
    
    private XmlModule module;
    
    public ModuleJar(String filename) {
        this.filename = filename;
    }
    
    public ModuleJar(XmlModule module) {
        this.module = module;
        this.filename = module.getJarFilename();
    }
    
    public XmlModule getModule() {
        return module;
    }
    
    private void addEntry(ZipOutputStream zout, String name, byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        
        ZipEntry ze = new ZipEntry(name);
        zout.putNextEntry(ze);
        
        byte b[] = new byte[512];
        int len = 0;
        while ((len = bais.read(b)) != -1) {
            zout.write(b, 0, len);
        }
        
        zout.closeEntry();
    }
    
    public void save() throws ModuleJarException {
        try {
            logger.debug("Saving Module JAR " + filename);
            
            byte[] icon16 = module.getIcon16();
            byte[] icon32 = module.getIcon32();
            
            icon16 = icon16 == null ? Utilities.getBytes(IconLibrary._icoPicture) : icon16;
            icon32 = icon32 == null ? Utilities.getBytes(IconLibrary._icoPicture) : icon32;

            FileOutputStream fos = new FileOutputStream(DataCrow.moduleDir + filename);
            ZipOutputStream zout = new ZipOutputStream(fos);
            
            module.setIcon16Filename("icon16.png");
            module.setIcon32Filename("icon32.png");
            
            XmlModuleWriter writer = new XmlModuleWriter(module);
            byte[] xml  = writer.getXML();
            
            writer.close();
            
            addEntry(zout, module.getFilename(), xml);
        	addEntry(zout, "icon16.png", icon16);
        	addEntry(zout, "icon32.png", icon32);

            zout.close();
            fos.close();

        } catch (Exception exp) {
            throw new ModuleJarException(exp);
        }
    }
    
    public boolean delete() {
        return new File(DataCrow.moduleDir + filename).delete();
    }
    
    public void load() throws ModuleUpgradeException, ModuleJarException, InvalidModuleXmlException {
        
        try {
            logger.debug("Loading module JAR " + filename);
            
            ZipFile zf = new ZipFile(DataCrow.moduleDir + filename);

            Map<String, byte[]> content = new HashMap<String, byte[]>();
            Enumeration<? extends ZipEntry> list = zf.entries();
            while (list.hasMoreElements()) {
                ZipEntry ze = list.nextElement();

                BufferedInputStream bis = new BufferedInputStream(zf.getInputStream(ze));
                int size = (int) ze.getSize();
                byte[] bytes = new byte[size];
                bis.read(bytes);
                
                String filename = ze.getName();
                content.put(filename, bytes);
                
                bis.close();
            }        
            
            // first get the XML file
            for (String filename : content.keySet()) {
                if (filename.toLowerCase().endsWith("xml")) {
                    module = new XmlModule(content.get(filename));
                    module.setFilename(filename);
                }
            }
            
            byte[] icon16 = content.get(module.getIcon16Filename());
            byte[] icon32 = content.get(module.getIcon32Filename());
            
            module.setIcon16(icon16);
            module.setIcon32(icon32);
            
            zf.close();
        
        } catch (ZipException e) {
            throw new ModuleJarException(e, "An error occured while reading zipfile " + filename);
        } catch (NullPointerException e) {
            throw new ModuleJarException(e);
        } catch (FileNotFoundException e) {
            throw new ModuleJarException(e);
        } catch (IOException e) {
            throw new ModuleJarException(e);
        }
    }
}
