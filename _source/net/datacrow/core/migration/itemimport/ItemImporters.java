package net.datacrow.core.migration.itemimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import net.datacrow.core.migration.ItemMigrater;

import org.apache.log4j.Logger;

public class ItemImporters {

	private static Logger logger = Logger.getLogger(ItemImporters.class.getName());
	
    private static ItemImporters instance;
    private Map<String, Class<?>> readers = new HashMap<String, Class<?>>(); 
    
    private ItemImporters() {
        readers.put("CSV", CsvImporter.class);
        readers.put("XML", XmlImporter.class);
    }

    public static ItemImporters getInstance() {
        instance = instance == null ? new ItemImporters() : instance;
        return instance;
    }

    public Collection<ItemImporter> getSourceReaders(int moduleIdx) {
    	Collection<ItemImporter> c = new ArrayList<ItemImporter>();
    	for (String key : readers.keySet()) {
    		try {
    			c.add(getSourceReader(key, moduleIdx));
    		} catch (Exception e) {
    			logger.error(e, e);
    		}
    	}
    	return c;
    }
    
    /**
     * Gets a (threaded) importer which can handle the specified file type.
     * This method only looks at the default (not module specific) importers.
     * @param type
     * @param moduleIdx
     * @throws Exception
     */
    public ItemImporter getSourceReader(String type, int moduleIdx) throws Exception {
        return getSourceReader(type, moduleIdx, ItemMigrater._MODE_THREADED);
    }
    
    /**
     * Gets an importer which can handle the specified file type.
     * This method only looks at the default (not module specific) importers.
     * @param type
     * @param moduleIdx
     * @param mode
     * @throws Exception
     */
    public ItemImporter getSourceReader(String type, int moduleIdx, int mode) throws Exception {
        Class<?> clazz = readers.get(type.toUpperCase());
        if (clazz != null) {
            return (ItemImporter) clazz.getConstructors()[0].newInstance(
                    new Object[] {Integer.valueOf(moduleIdx), Integer.valueOf(mode)});
        } else {
            throw new Exception("No item importer found for " + type);
        }
    }
}
