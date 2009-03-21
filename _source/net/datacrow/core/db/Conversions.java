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

package net.datacrow.core.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import net.datacrow.core.DataCrow;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.xml.XmlField;
import net.datacrow.core.objects.DcField;

import org.apache.log4j.Logger;

/**
 * Manages table conversions when
 * @author Robert Jan van der Waals
 */
public class Conversions {

    private static Logger logger = Logger.getLogger(Conversions.class.getName());

    private Collection<Conversion> conversions = new ArrayList<Conversion>();
    private String filename = DataCrow.installationDir + "upgrade" + File.separator + "conversions.properties";
    
    public Conversions() {}
    
    public void calculate() {
        for (DcModule module : DcModules.getAllModules()) {
            
            if (module.getXmlModule() == null)
                continue;
            
            for (XmlField xmlField : module.getXmlModule().getFields()) {
                DcField field = module.getField(xmlField.getIndex());
                
                if (field.getFieldType() != xmlField.getFieldType()) {
                    Conversion conversion = new Conversion(module.getIndex());
                    conversion.setColumnName(field.getDatabaseFieldName());
                    conversion.setOldFieldType(field.getFieldType());
                    conversion.setNewFieldType(xmlField.getFieldType());
                    conversion.setReferencingModuleIdx(xmlField.getModuleReference());
                    conversions.add(conversion);
                }
            }
        }
    }

    public void load() {
        File file = new File(filename);
        if (file.exists()) {
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(filename));
                for (Object value : properties.values())
                    conversions.add(new Conversion((String) value));
                
            } catch (IOException e) {
                logger.error("Failed to load database column conversion scripts", e);
            }
        }        
    }
    
    public void execute() {
        for (Conversion conversion : conversions) 
            conversion.execute();
        
        new File(filename).delete();
    }
    
    public void save() {
        Properties properties = new Properties();
        int count = 0;
        for (Conversion conversion : conversions) {
            properties.put(String.valueOf(count), conversion.toString());
        }
        
        try {
            properties.store(new FileOutputStream(new File(filename)), "");
        } catch (IOException e) {
            logger.error("Failed to persist database column conversion scripts", e);
        }            
    }
}
