package net.datacrow.console.components.panels.tree;

import javax.swing.tree.DefaultMutableTreeNode;

public class DcDefaultMutableTreeNode extends DefaultMutableTreeNode {

    public DcDefaultMutableTreeNode(Object userObject) {
        super(userObject);
    }

    public void addItem(String item) {
        NodeElement ne = (NodeElement) getUserObject();
        ne.addItem(item);
    }
    
    public void removeItem(String item) {
        NodeElement ne = (NodeElement) getUserObject();
        ne.removeItem(item);
    }
    
    public int getItemCount() {
        return ((NodeElement) getUserObject()).getCount();
    }
    
    public boolean contains(String item) {
        return ((NodeElement) getUserObject()).getItems().contains(item);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DcDefaultMutableTreeNode)) return false;
        
        DcDefaultMutableTreeNode node = (DcDefaultMutableTreeNode) obj;
        return  node.getUserObject().equals(getUserObject());
    }
}
