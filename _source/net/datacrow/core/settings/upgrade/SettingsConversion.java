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

package net.datacrow.core.settings.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import net.datacrow.core.DcRepository;
import net.datacrow.core.Version;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.settings.Setting;
import net.datacrow.settings.DcModuleSettings;

import org.apache.log4j.Logger;

/**
 * Used to convert settings when needed.
 * This all non standard stuff and should only be used to upgrade between versions. 
 * 
 * @author Robert Jan van der Waals
 */
public abstract class SettingsConversion {
    
    private static Logger logger = Logger.getLogger(SettingsConversion.class.getName());
    
    @SuppressWarnings("deprecation")
    public static void convert() {
        
        for (DcModule module : DcModules.getModules()) {
            
            if (DatabaseManager.getOriginalVersion().isOlder(new Version(3, 4, 0, 0))) {
                try {
                    DcModuleSettings settings = new DcModuleSettings(module);
                    File file = settings.getSettings().getSettingsFile();
                    
                    Properties properties = new Properties();
                    FileInputStream fis = new FileInputStream(file);
                    properties.load(fis);
                    fis.close();
                    
                    // get the old field definitions
                    String value = (String) properties.get(DcRepository.ModuleSettings.stFieldDefinitions);
                    LegacyFieldDefinitions definitions = new LegacyFieldDefinitions();
                    
                    int group = value.indexOf("}");
                    while (group > -1) {
                        String s = value.substring(1, group);
                        definitions.add(s);
                        if (value.length() > group + 1) {
                            value = value.substring(group + 1);
                            group = value.indexOf("}");
                        } else {
                            group = -1;
                        }
                    }
                    
                    // create the table column order
                    Collection<Integer> c = new ArrayList<Integer>();
                    for (LegacyFieldDefinition definition : definitions.getDefinitions()) {
                        if (definition.isVisible())
                            c.add(Integer.valueOf(definition.getIndex()));
                    }
                    
                    int[] order = new int[c.size()];
                    int idx = 0;
                    for (Integer i : c)
                        order[idx++] = i.intValue();
                    
                    if (order.length > 0) {
                        String s = new Setting(DcRepository.ValueTypes._INTEGERARRAY, 
                                DcRepository.ModuleSettings.stTableColumnOrder,
                                order, -1, "", "", false, false).getValueAsString();
                        properties.put(DcRepository.ModuleSettings.stTableColumnOrder, s);
                    }
                    
                    // create the new field definitions
                    net.datacrow.settings.definitions.DcFieldDefinitions newDefs = new net.datacrow.settings.definitions.DcFieldDefinitions();
                    for (LegacyFieldDefinition definition : definitions.getDefinitions()) {
                        newDefs.add(new net.datacrow.settings.definitions.DcFieldDefinition(
                                definition.getIndex(),
                                definition.getLabel(),
                                definition.isEnabled(),
                                definition.isRequired(),
                                definition.isDescriptive()));
                    }
                    
                    String s = new Setting(DcRepository.ValueTypes._DEFINITIONGROUP, 
                            DcRepository.ModuleSettings.stFieldDefinitions,
                            newDefs, -1, "", "", false, false).getValueAsString();
                    properties.put(DcRepository.ModuleSettings.stFieldDefinitions, s);
                    
                    // and store the new settings to the module settings file
                    FileOutputStream fos = new FileOutputStream(file);
                    properties.store(fos, "");
                    fos.close();
                    
                    module.initializeSettings();
                    
                } catch (Exception e) {
                    logger.debug("Could not convert settings for module " + module.getLabel(), e);
                } 
            }
        }
    }
}
