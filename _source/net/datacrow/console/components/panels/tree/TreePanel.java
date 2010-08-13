/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
 *                                                                            *
 *                       This file is part of Data Crow.                      *
 *       Data Crow is free software; you can redistribute it and/or           *
 *        modify it under the terms of the GNU General Public                 *
 *       License as published by the Free Software Foundation; either         *
 *              version 3 of the License, or any later version.               *
 *                                                                            *
 *        Data Crow is distributed in the hope that it will be useful,        *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *           MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.             *
 *           See the GNU General Public License for more details.             *
 *                                                                            *
 *        You should have received a copy of the GNU General Public           *
 *  License along with this program. If not, see http://www.gnu.org/licenses  *
 *                                                                            *
 ******************************************************************************/

package net.datacrow.console.components.panels.tree;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcTree;
import net.datacrow.console.views.MasterView;
import net.datacrow.console.views.View;
import net.datacrow.core.data.DataFilters;

import org.apache.log4j.Logger;

public abstract class TreePanel extends JPanel implements TreeSelectionListener {
    
    private static Logger logger = Logger.getLogger(TreePanel.class.getName());
    
    public static final int  _OBJECT_ADDED = 0;
    public static final int  _OBJECT_UPDATED = 1;
    public static final int  _OBJECT_REMOVED = 2;
    
    protected DcTree tree;
    private JScrollPane scroller;
    protected DefaultMutableTreeNode top;
    
    private boolean listenForSelection = true;
    
    protected GroupingPane gp;
    protected Object currentUserObject;
    
    private boolean saveChanges = true;
    
    public TreePanel(GroupingPane gp) {
        this.gp = gp;
        
        setLayout(Layout.getGBL());
        
        JMenuBar menu = getMenu();
        if (menu != null) {
            add(menu, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 0, 5), 0, 0));
        }
        
        build();
    }
    
    public MasterView getView() {
        return gp.getView();
    }
    
    public boolean isListeningForSelection() {
        return listenForSelection;
    }
    
    public void setListeningForSelection(boolean b) {
        listenForSelection = b;
    }
    
    public boolean isHoldingItems() {
        return top != null;
    }
    
    public Object getLastSelectedPathComponent() {
        return tree.getLastSelectedPathComponent();
    }
    
    public DefaultMutableTreeNode getTopNode() {
        return top;
    }
    
    public void setSaveChanges(boolean b) {
        saveChanges = b;
    }
    
    public boolean isSaveChanges() {
        return saveChanges;
    }
    
    public int getModule() {
        return gp.getModule();
    }
    
    /**
     * Checks if the keys chain exists.
     * @param keys
     */
    protected boolean exists(String[] keys) {
        boolean exists = true;
        DefaultMutableTreeNode node = top;
        for (int level = 1; level < keys.length; level++) {
            node = findNode(keys[level], node, false);
            exists &= node != null; 
        }
        
        return exists;
    }
    
    public void collapseAll() {
        if (top == null) return;
        
        collapseChildren(top);
        tree.collapsePath(new TreePath(top.getPath()));
        
        getView().clear();
        setSelected(top);
    }   
    
    public void expandAll() {
        expandAll(top);
    }   
    
    private void expandAll(DefaultMutableTreeNode node) {
        try {
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            int count = node.getChildCount();
            tree.expandPath(new TreePath(model.getPathToRoot(node)));
            
            DefaultMutableTreeNode current;
            for (int i = 0; i < count; i++) {
                current = (DefaultMutableTreeNode) node.getChildAt(i);
                tree.expandPath(new TreePath(model.getPathToRoot(current)));
                expandAll(current);
            }
        } catch (Exception e) {
            logger.warn("Failed to expand all nodes", e);
        }
    }
    
    protected void updateView(List<String> keys) {
        getView().getCurrent().add(keys);  
    }
    
    public void setDefaultSelection() {
        setListeningForSelection(true);
        try {
            if (isActive())
                tree.setSelectionInterval(1, 1);
            else 
                tree.setSelectionInterval(0, 0);
        } catch (Exception e) {
            logger.error(e, e);
        }
    }
    
    private void collapseChildren(DefaultMutableTreeNode parent) {
        int size = parent.getChildCount();
        
        DefaultMutableTreeNode child;
        for (int i = 0; i < size; i++) {
            try {
                child = (DefaultMutableTreeNode) parent.getChildAt(i);
                collapseChildren(child);
                tree.collapsePath(new TreePath(child.getPath()));
            } catch (Exception e) {
                logger.error("An error occurred while collapsing leafs of " + parent, e);
            }
        }
    }

    public void clear() {
        if (tree == null)
            return;

        if (top != null) {
            DefaultMutableTreeNode pn;
            DefaultMutableTreeNode cn;
            for (int i = 0; i < top.getChildCount(); i++) {
                pn = (DefaultMutableTreeNode) top.getChildAt(i);
                if (pn.getUserObject() != null) {
                    ((NodeElement) pn.getUserObject()).clear();
                    for (int j = 0; j < pn.getChildCount(); j++) {
                        cn = (DefaultMutableTreeNode) pn.getChildAt(j);
                        if (cn.getUserObject() != null)
                            ((NodeElement) cn.getUserObject()).clear();
                    }
                }
            }
        
            ComponentFactory.clean(tree);
            tree.removeTreeSelectionListener(this);
            tree = null;

            top.removeAllChildren();
            top = null;
        }
        
        if (scroller != null)
            remove(scroller);
        
        currentUserObject = null;
    }
    
    
    @Override
    public void setFont(Font font) {
        if (tree != null)
            tree.setFont(font);
    }
    
    protected void build() {
        clear();
        
        createTopNode();
        
        tree = new DcTree(new DefaultTreeModel(top));
        tree.setFont(ComponentFactory.getStandardFont());
        tree.addTreeSelectionListener(this);
        
        scroller = new JScrollPane(tree);
        
        add(scroller,  Layout.getGBC( 0, 1, 1, 1, 50.0, 50.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets(0, 5, 5, 5), 0, 0));
    }     
    
    public void reset() {
        createTree();
    }
    
    protected void setSelected(DefaultMutableTreeNode node) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        TreePath treePath = new TreePath(model.getPathToRoot(node));
        tree.setSelectionPath(treePath);
        tree.expandPath(treePath);
        tree.scrollPathToVisible(treePath);
        
        currentUserObject = node.getUserObject();
    }
    
    /**
     * Inserts a node. The node needs to have a valid user object defined
     * to insert it at the right position (ordering)
     */
    protected void insertNode(DefaultMutableTreeNode node, DefaultMutableTreeNode parent) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        
        NodeElement ne = (NodeElement) node.getUserObject();
        
        List<String> elements = new ArrayList<String>();
        NodeElement child;
        for (int i = 0; i < parent.getChildCount(); i++) {
            child = ((NodeElement) ((DefaultMutableTreeNode) parent.getChildAt(i)).getUserObject());
            elements.add(child.getComparableKey());
        }
        
        elements.add(ne.getComparableKey());
        Collections.sort(elements);
        int idx = elements.indexOf(ne.getComparableKey());
        
        model.insertNodeInto(node, parent, idx);
    }
    
    /**
     * Retrieves the node path for the given keys. 
     * The returned path is the actual existing path, starting with the parent node.
     * The returned path can be shorter then the key set if the path does not (fully) 
     * exists in the tree.
     * @param keys
     */
    protected List<DefaultMutableTreeNode> findPath(Object[] keys) {
        List<DefaultMutableTreeNode> path = new ArrayList<DefaultMutableTreeNode>();
        DefaultMutableTreeNode parentNode = null;
        
        for (Object key : keys) {
            parentNode = findNode(key, parentNode, false);
            
            if (parentNode != null) 
                path.add(parentNode);
            else 
                break;
        }
        
        return path;
    }
    
    /**
     * Recursive search method for tree nodes.
     * @param key
     * @param parentNode
     * @param recurse
     */
    protected DefaultMutableTreeNode findNode(Object key, DefaultMutableTreeNode parentNode, boolean recurse) {
        int count = parentNode != null ? parentNode.getChildCount() : 0;
        
        if (parentNode == null && key.equals(((NodeElement) getTopNode().getUserObject()).getKey()))
            return getTopNode();
        
        DefaultMutableTreeNode node;
        NodeElement ne;
        String s;
        DefaultMutableTreeNode result = null;
        for (int i = 0; i < count; i++) {
            node = (DefaultMutableTreeNode) parentNode.getChildAt(i);
            
            if (node.getUserObject() instanceof NodeElement) {
                ne = (NodeElement) node.getUserObject();
                s = key instanceof String ? ((String) key).toLowerCase() : key.toString().toLowerCase();
                if (ne.getComparableKey().equalsIgnoreCase(s))
                    result = node;
            }
            
            if (result == null && recurse)
                result = findNode(key, node, recurse);
            
            if (result != null) return result;
        }
        return null;
    }  
    
    protected abstract JMenuBar getMenu();
    protected abstract void createTopNode();
    protected abstract void createTree();
    protected abstract void addElement(Long key, DefaultMutableTreeNode node, int level);
    
    protected void refresh() {
        TreePath path = tree.getSelectionPath();
        groupBy();
        tree.setSelectionPath(path);
        
        if (tree.getSelectionPath() == null)
            setDefaultSelection();
    }
    
    protected boolean isActive() {
        return true;
    }
    
    public abstract void groupBy();
    
    /************************************************************************
     * Selection listener
     ************************************************************************/
    
    public void valueChanged(TreeSelectionEvent e) {
        
        if (!isListeningForSelection())
            return;
        
        if (e.getNewLeadSelectionPath() == null || 
            e.getNewLeadSelectionPath().equals(e.getOldLeadSelectionPath()))
            return;
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node == null) {
            currentUserObject = null;
            return;
        }

        Object o = node.getUserObject();
        
        View currentView = getView().getCurrent();
        if (currentView != null) {
            
            List<NodeElement> parents = new ArrayList<NodeElement>();
            
            for (Object parent : tree.getSelectionPath().getPath()) {
                if (parent instanceof DefaultMutableTreeNode) {
                    
                    if (parent == top && !DataFilters.isFilterActive(getModule()))
                        continue;
                    
                    parents.add((NodeElement) ((DefaultMutableTreeNode) parent).getUserObject());
                }
            }
            
            currentView.clear(isSaveChanges());
            NodeElement currentNode = (NodeElement) o;
            setSelected(node);
            updateView(currentNode.getItems(parents));
        }
    }
}
