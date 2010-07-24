package net.datacrow.console.components.panels.tree;

import java.util.List;

import javax.swing.ImageIcon;

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;

public abstract class NodeElement {

    protected Object key;
    protected Object value;
    protected ImageIcon icon;
    protected int module;
    private String clause;
    private int count;
    
    public NodeElement(int module, Object key, ImageIcon icon, String clause) {
        this.module = module;
        this.key = key;   
        this.value = key instanceof DcObject ? ((DcObject) key).getID() : key.equals(DcResources.getText("lblEmpty")) ? null : key;
        this.icon = icon;
        this.clause = clause;
    }
    
    public void setCount(int count) {
        this.count = count;
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
    
    public abstract List<Long> getItems(List<NodeElement> parents);
    public abstract int getCount();

    public String getComparableKey() {
        return getKey().toLowerCase();
    }
    
    public Object getValue() {
        return value;
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
        clause = null;
    }

    @Override
    public String toString() {
        return getKey() + " (" + String.valueOf(count) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof NodeElement))
            return false;
        else 
            return getComparableKey().equals(((NodeElement) o).getComparableKey());
    }

    public String getWhereClause() {
        return clause;
    }
    
    @Override
    public int hashCode() {
        return getComparableKey().hashCode();
    }
}
