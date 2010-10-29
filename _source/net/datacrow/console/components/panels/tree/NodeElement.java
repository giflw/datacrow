package net.datacrow.console.components.panels.tree;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import net.datacrow.util.DcImageIcon;

public class NodeElement {

    protected Object key;
    protected String displayValue;
    protected DcImageIcon icon;
    
    private Map<String, Integer> items = new LinkedHashMap<String, Integer>();
    
    public NodeElement(Object key, String displayValue, DcImageIcon icon) {
        this.key = key;   
        this.displayValue = displayValue;
        this.icon = icon;
    }
    
    public void addItem(String item, Integer moduleIdx) {
    	if (!items.containsKey(item))
    		items.put(item, moduleIdx);
    }
    
    public void removeItem(String item) {
    	items.remove(item);
    }
    
    public int getCount() {
        return items != null ? items.size() : 0;
    }
    
    public ImageIcon getIcon() {
        return icon;
    }
    
    public Map<String, Integer> getItems() {
    	return items;
    }
    
    public Map<String, Integer> getItemsSorted(List<String> allOrderedItems) {
    	
    	Map<String, Integer> items = getItems();
    	
    	if (allOrderedItems.size() == items.size()) 
    		return items;
    	
    	Map<String, Integer> result = new LinkedHashMap();
    	for (String orderedItem : allOrderedItems) {
    		for (String item : items.keySet()) {
    			if (item.equals(orderedItem)) {
    				result.put(item, items.get(item));
    			}
    		}
    	}
    	return result;
    }
    
    public void setItems(Map<String, Integer> items) {
    	this.items = items;
    }

    public String getComparableKey() {
        return getDisplayValue().toLowerCase();
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
    
    public Object getKey() {
        return key;
    }
    
    public void clear() {
        if (icon != null) icon.flush();
        
        key = null;
        icon = null;
        displayValue = null;
        
        if (items != null) {
        	items.clear();
        	items = null;
        }
    }

    @Override
    public String toString() {
        return getDisplayValue() + " (" + String.valueOf(getCount()) + ")";
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

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		clear();
	}
}
