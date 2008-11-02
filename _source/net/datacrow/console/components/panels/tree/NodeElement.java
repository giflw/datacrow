package net.datacrow.console.components.panels.tree;

import java.util.ArrayList;
import java.util.List;

import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.objects.DcObject;

public class NodeElement {
        
    private String key;
    private String keyComparable;
    private int module;
    
    private List<DcObject> values = new ArrayList<DcObject>();
    
    public NodeElement(int module, String key) {
        this.module = module;
        this.key = key;
        this.keyComparable = key.toLowerCase();        
    }

    public void setValues(List<DcObject> values) {
        this.values = values;
    }
    
    public void addValue(DcObject dco) {
        if (!values.contains(dco))
            values.add(dco);
    }
    
    public void updateValue(DcObject dco) {
        int pos = values.indexOf(dco);
        if (pos != -1) {
            DcObject o = values.get(pos);
            o.reload();
        }
    }
    
    public boolean removeValue(DcObject dco) {
        return values.remove(dco);
    }
    
    public boolean contains(DcObject dco) {
        return values != null ? values.contains(dco) : false;
    }
    
    public int size() {
        return values == null ? 0 : values.size();
    }
    
    public List<DcObject> getSortedValues() {
        if (values != null && values.size() > 1) {
            DataFilter df = DataFilters.getDefaultDataFilter(module);
            df.sort(values);
        }
        return values;
    }
    
    public List<DcObject> getValues() {
        return values;
    }
    
    public String getComparableKey() {
        return keyComparable;
    }
    
    public String getKey() {
        return key;
    }
    
    public void clear() {
        key = null;
        keyComparable = null;
        
        if (values != null)
            values.clear();
        
        values = null;
    }

    @Override
    public String toString() {
        if (values == null || values.size() == 1) 
            return key;
        else 
            return key + " (" + String.valueOf(values.size()) + ")";    
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
