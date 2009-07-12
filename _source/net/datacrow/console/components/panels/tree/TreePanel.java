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
import java.util.Collection;
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
import net.datacrow.core.objects.DcObject;

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
    
    public List<DcObject> getItems() {
        return gp.getItems();
    }
    
    public boolean isListeningForSelection() {
        return listenForSelection;
    }
    
    public void setListeningForSelection(boolean b) {
        listenForSelection = b;
    }
    
    public Object getLastSelectedPathComponent() {
        return tree.getLastSelectedPathComponent();
    }
    
    public DefaultMutableTreeNode getTopNode() {
        return top;
    }
    
    public void updateView() {
        if (currentUserObject instanceof NodeElement) {
            gp.getView().clear(isSaveChanges());
            NodeElement currentNode = (NodeElement) currentUserObject;
            
            if (currentNode.getValues() != null)
                updateView(currentNode.getSortedValues());
        }
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
            node = findNode(keys[level], node);
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
            for (int i = 0; i < count; i++) {
                DefaultMutableTreeNode current = (DefaultMutableTreeNode) node.getChildAt(i);
                tree.expandPath(new TreePath(model.getPathToRoot(current)));
                expandAll(current);
            }
        } catch (Exception e) {
            logger.warn("Failed to expand all nodes", e);
        }
    }
    
    protected void updateView(Collection<DcObject> dcos) {
        getView().getCurrent().cancelCurrentTask();    
        getView().getCurrent().add(dcos);      
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
        for (int i = 0; i < size; i++) {
            try {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
                tree.collapsePath(new TreePath(child.getPath()));
            } catch (Exception e) {
                logger.error("An error occurred while collapsing leafs of " + parent, e);
            }
        }
    }

    public void clear() {
        if (tree == null)
            return;

        for (int i = 0; i < top.getChildCount(); i++) {
            DefaultMutableTreeNode pn = (DefaultMutableTreeNode) top.getChildAt(i);
            if (pn.getUserObject() != null) {
                ((NodeElement) pn.getUserObject()).clear();
                for (int j = 0; j < pn.getChildCount(); j++) {
                    DefaultMutableTreeNode cn = (DefaultMutableTreeNode) pn.getChildAt(j);
                    if (cn.getUserObject() != null)
                        ((NodeElement) cn.getUserObject()).clear();
                }
            }
        }
        
        ComponentFactory.clean(tree);
        tree.removeTreeSelectionListener(this);
        tree = null;
        
        if (scroller != null)
            remove(scroller);
        
        top.removeAllChildren();
        top = null;
        
        currentUserObject = null;
        
        revalidate();
        repaint();
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
    
    public void reset() {}
    
    protected void setSelected(DefaultMutableTreeNode node) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        TreePath treePath = new TreePath(model.getPathToRoot(node));
        tree.setSelectionPath(treePath);
        tree.expandPath(treePath);
        tree.scrollPathToVisible(treePath);
    }
    
    /**
     * Inserts a node. The node needs to have a valid user object defined
     * to insert it at the right position (ordering)
     */
    protected void insertNode(DefaultMutableTreeNode node, DefaultMutableTreeNode parent) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        
        List<String> elements = new ArrayList<String>();
        for (int i = 0; i < parent.getChildCount(); i++)
            elements.add(((DefaultMutableTreeNode) parent.getChildAt(i)).getUserObject().toString());
        
        elements.add(node.getUserObject().toString());
        Collections.sort(elements);
        int idx = elements.indexOf(node.getUserObject().toString());
        
        model.insertNodeInto(node, parent, idx);
    }
    
    protected DefaultMutableTreeNode findNode(String key, DefaultMutableTreeNode parentNode) {
        int count = parentNode != null ? parentNode.getChildCount() : 0;
        for (int i = 0; i < count; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) parentNode.getChildAt(i);
            
            if (node.getUserObject() instanceof NodeElement) {
                NodeElement ne = (NodeElement) node.getUserObject();
                if (ne.getComparableKey().equals(key.toLowerCase()))
                    return node;
            }
        }
        return null;
    }  
    
    protected abstract JMenuBar getMenu();
    protected abstract void createTopNode();
    protected abstract void buildTree();
    protected abstract void revalidateTree(DcObject dco, int modus);
    protected abstract boolean isActive();
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
        
        if (o != currentUserObject && getView().getCurrent() != null) {
            currentUserObject = o;
            getView().getCurrent().clear(isSaveChanges());
            NodeElement currentNode = (NodeElement) o;
            updateView(currentNode.getSortedValues());
            setSelected(node);
        }
    }

}
