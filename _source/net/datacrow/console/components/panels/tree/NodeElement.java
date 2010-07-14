package net.datacrow.console.components.panels.tree;

import java.util.List;

import javax.swing.ImageIcon;

import net.datacrow.core.data.DataFilter;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;

public abstract class NodeElement {

    protected DataFilter df;
    protected Object key;
    protected ImageIcon icon;
    protected int module;

    private int count;
    private int i = 0;
    
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
        if (key instanceof DcObject)
            ((DcObject) key).release();
        
        key = null;
        icon = null;
    }

    @Override
    public String toString() {
        count = i == 0 ? getCount() : count;
        i++;
        i = i > 20 ? 0 : i;
        return getKey() + " (" + String.valueOf(count) + ")";
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
