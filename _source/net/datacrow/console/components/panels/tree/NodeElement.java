package net.datacrow.console.components.panels.tree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;

public class NodeElement {
        
    private Object key;
    
    private ImageIcon icon;
    private int module;
    
    private List<DcObject> values = new ArrayList<DcObject>();
    
    public NodeElement(int module, Object key, ImageIcon icon) {
        this.module = module;
        this.key = key instanceof DcMapping ? ((DcMapping) key).getReferencedObject() : key;     
        this.icon = icon;
    }

    public void setValues(List<DcObject> values) {
        this.values = values;
    }
    
    public ImageIcon getIcon() {
        return icon;
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
        return getKey().toLowerCase();
    }
    
    public String getKey() {
        return key instanceof String ? (String) key : key.toString();
    }
    
    public void clear() {
        key = null;
        
        if (values != null)
            values.clear();
        
        values = null;
    }

    @Override
    public String toString() {
        if (values == null || values.size() == 1) 
            return getKey();
        else 
            return getKey() + " (" + String.valueOf(values.size()) + ")";    
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof NodeElement))
            return false;
        else 
            return getComparableKey().equals(((NodeElement) o).getComparableKey());
    }

    @Override
    public int hashCode() {
        return getComparableKey().hashCode();
    }
}
