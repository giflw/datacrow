package net.datacrow.console.components.panels.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

public class DcDefaultMutableTreeNode extends DefaultMutableTreeNode {

    public DcDefaultMutableTreeNode(Object userObject) {
        super(userObject);
    }

    public void addItem(String item, Integer moduleIdx) {
        NodeElement ne = (NodeElement) getUserObject();
        ne.addItem(item, moduleIdx);
    }
    
    public void removeItem(String item) {
        NodeElement ne = (NodeElement) getUserObject();
        ne.removeItem(item);
    }
    
    public int getItemCount() {
        return ((NodeElement) getUserObject()).getCount();
    }
    
    public Map<String, Integer> getItems() {
        return ((NodeElement) getUserObject()).getItems();
    }

    public List<String> getItemList() {
        return new ArrayList<String>(((NodeElement) getUserObject()).getItems().keySet());
    }
    
    public Map<String, Integer> getItemsSorted(List<String> allSortedItems) {
        return ((NodeElement) getUserObject()).getItemsSorted(allSortedItems);
    }
    
    public boolean contains(String item) {
        return ((NodeElement) getUserObject()).getItems().containsKey(item);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DcDefaultMutableTreeNode)) return false;
        
        DcDefaultMutableTreeNode node = (DcDefaultMutableTreeNode) obj;
        return  node.getUserObject().equals(getUserObject());
    }
}
