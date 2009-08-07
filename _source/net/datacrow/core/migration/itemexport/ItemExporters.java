package net.datacrow.core.migration.itemexport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class ItemExporters {

	private static Logger logger = Logger.getLogger(ItemExporters.class.getName());
	
    private static ItemExporters instance;
    private Map<String, Class<?>> exporters = new HashMap<String, Class<?>>(); 
    
    private ItemExporters() {
        exporters.put("CSV", CsvExporter.class);
        exporters.put("XML", XmlExporter.class);
    }

    public static ItemExporters getInstance() {
        instance = instance == null ? new ItemExporters() : instance;
        return instance;
    }

    public Collection<ItemExporter> getExporters(int moduleIdx) {
    	Collection<ItemExporter> c = new ArrayList<ItemExporter>();
    	for (String key : exporters.keySet()) {
    		try {
    			c.add(getExporter(key, moduleIdx));
    		} catch (Exception e) {
    			logger.error(e, e);
    		}
    	}
    	return c;
    }
    
    public ItemExporter getExporter(String type, int moduleIdx) throws Exception {
        return getExporter(type, moduleIdx, ItemExporter._MODE_THREADED);
    }

    public ItemExporter getExporter(String type, int moduleIdx, int mode) throws Exception {
        Class<?> clazz = exporters.get(type.toUpperCase());
        if (clazz != null) {
            return (ItemExporter) clazz.getConstructors()[0].newInstance(
                    new Object[] {Integer.valueOf(moduleIdx), Integer.valueOf(mode)});
        } else {
            throw new Exception("No item exporter found for " + type);
        }
    }
}
