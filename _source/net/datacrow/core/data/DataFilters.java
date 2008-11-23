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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModules;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * Holder of all saved data filters
 * 
 * @author Robert Jan van der Waals
 */
public class DataFilters {
    
    private static Logger logger = Logger.getLogger(DataFilters.class.getName());
    
    private static final Map<Integer, Collection<DataFilter>> filters = 
        new HashMap<Integer, Collection<DataFilter>>(); 
    private static final Map<Integer, DataFilter> activeFilters = 
        new HashMap<Integer, DataFilter>();
    
    private static final String filename = DataCrow.baseDir + "data" + File.separator + "filters.xml";

    /**
     * Creates a new instance.
     */
    private DataFilters() {}
    
    /**
     * Loads the settings file
     */
    public static void load() {
        
        if (!new File(filename).exists())
            return;
        
        try {
            filters.clear();
            byte[] b = Utilities.readFile(new File(filename));
            String xml = new String(b, "UTF-8");
            
            while (xml.indexOf("<FILTER>") > -1) {
                String part = xml.substring(xml.indexOf("<FILTER>"), xml.indexOf("</FILTER>") + 9);
                DataFilter df = new DataFilter(part);
                
                Collection<DataFilter> c = filters.get(df.getModule());
                c = c == null ? new ArrayList<DataFilter>() : c;
                c.add(df);
                filters.put(df.getModule(), c);
                
                xml = xml.substring(xml.indexOf("</FILTER>") + 9, xml.length());
            }
        } catch (Exception exp) {
            logger.error("An error occurred while loading filters from " + filename, exp);
        }
    }
    
    /**
     * Stores the currently applied filter.
     * @param module
     * @param df
     */
    public static void setCurrent(int module, DataFilter df) {
    	activeFilters.put(module, df);
    }
    
    /**
     * Retrieves the default filter. This filter is an empty filter with the default ordering set.
     * @param module
     * @return
     */
    public static DataFilter getDefaultDataFilter(int module) {
        DataFilter filter = new DataFilter(module);
        filter.setOrder(DcModules.get(module).getSettings().getStringArray(DcRepository.ModuleSettings.stSearchOrder));
        return filter;
    }
    
    /**
     * Retrieves the currently applied filter for the specified module.
     * @param module
     */
    public static DataFilter getCurrent(int module) {
    	DataFilter df = activeFilters.get(module);
    	return df == null ? getDefaultDataFilter(module) : df;
    }
    
    /**
     * Save all filters to file for future use.
     */
    public static void save() {
        String xml = "<FILTERS>\n";

        for (Collection<DataFilter> c : filters.values()) {
            for (DataFilter df : c)
                xml += "\n" + df.toStorageString();
        }
        
        xml += "</FILTERS>";
        
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(xml.getBytes("UTF-8"));
            fos.close();
        } catch (Exception exp) {
            logger.error("An error occurred while saving filters to " + filename, exp);
        }         
    }
    
    /**
     * Delete a filter.
     * @param df
     */
    public static void delete(DataFilter df) {
        Collection<DataFilter> c = get(df.getModule());
        c.remove(df);
    }
    
    /**
     * Gets all saved filters for the specified module.
     * @param module
     */
    public static Collection<DataFilter> get(int module) {
        Collection<DataFilter> c = filters.get(module);
        return c != null ? c : new ArrayList<DataFilter>();
    }

    /**
     * Adds a filter. This filter will be saved to file.
     * @param df
     */
    public static void add(DataFilter df) {
        Collection<DataFilter> c = filters.get(df.getModule());
        c = c == null ? new ArrayList<DataFilter>() : c;
        c.remove(df);
        c.add(df);
        filters.put(df.getModule(), c);
    }
}
