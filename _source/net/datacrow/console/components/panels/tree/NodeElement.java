package net.datacrow.console.components.panels.tree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import net.datacrow.core.resources.DcResources;

public abstract class NodeElement {
        
    protected Object key;
    protected ImageIcon icon;
    protected int module;
    
    private List<Long> keys = new ArrayList<Long>();
    
    public NodeElement(int module, Object key, ImageIcon icon) {
        this.module = module;
        this.key = key;     
        this.icon = icon;
    }
    
    public int getModule() {
        return module;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public ImageIcon getIcon() {
        return icon;
    }
    
    public boolean contains(Long key) {
        return keys != null ? keys.contains(key) : false;
    }
    
    public int size() {
        return keys == null ? 0 : keys.size();
    }
    
    public abstract List<Long> getItems();
    public abstract int getCount();

    public String getComparableKey() {
        return getKey().toLowerCase();
    }
    
    public String getKey() {
        try {
            return key instanceof String ? (String) key : key.toString();
        } catch (Exception e) {
            return DcResources.getText("lblEmpty");
        }
    }
    
    public void clear() {
        key = null;
        
        if (keys != null)
            keys.clear();
        
        keys = null;
    }

    int count;
    int i = 0;
    
    @Override
    public String toString() {
        
        
        
        
        if (keys == null || keys.size() == 1) {
            return getKey();
        } else {
            if (i == 0)
                count = getCount();
            
            
            i++;
            
            if (i > 20)
                i = 0;
            
            return getKey() + " (" + String.valueOf(count) + ")";
        }
        
       
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
