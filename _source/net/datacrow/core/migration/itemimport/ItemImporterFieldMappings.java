package net.datacrow.core.migration.itemimport;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;

public class ItemImporterFieldMappings {

    private Map<String, DcField> mappings = new LinkedHashMap<String, DcField>();
    
    public ItemImporterFieldMappings() {}
    
    public void clear() {
        mappings.clear();
    }
    
    public void setMapping(String source, DcField target) {
        mappings.put(source, target);
    }
    
    public DcField getTarget(String fieldName) {
        return mappings.get(fieldName);
    }
    
    public DcField getTarget(int index) {
        int counter = 0;
        for (DcField field : mappings.values()) {
            if (counter == index) return field;
            counter++;
        }
        return null;
    }
    
    public Collection<String> getSourceFields() {
        return mappings.keySet();
    }
    
    /**
     * Initializes the mapping table.
     * Tries to find the corresponding module fields.
     * @param moduleIdx
     * @param fields
     */
    public void setFields(int moduleIdx, Collection<String> fields) {
        DcModule module = DcModules.get(moduleIdx);
        for (String fieldName : fields) {
            DcField target = null;
            for (DcField field : module.getFields()) {
                if (field.getDatabaseFieldName().equals(fieldName) || 
                    field.getLabel().equals(fieldName) || 
                    field.getOriginalLabel().equals(fieldName)) {
                    target = field;
                    break;
                }
            }
            setMapping(fieldName, target);
        }
    }
}
