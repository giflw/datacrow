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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.datacrow.console.menu.FieldTreePanelMenuBar;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcObjectComparator;

import org.apache.log4j.Logger;

public class FieldTreePanel extends TreePanel {

    private static Logger logger = Logger.getLogger(FieldTreePanel.class.getName());
    private int[] fields;
    private String empty = DcResources.getText("lblEmpty");
    
    public FieldTreePanel(GroupingPane gp) {
        super(gp);
    }
    
    public int[] getGroupBy() {
        return fields;
    }
    
    @Override
    public String getName() {
        return DcResources.getText("lblDefault");
    }
    
    public void groupBy(int[] fields) {
        this.fields = fields;
        DcModules.get(getModule()).setSetting(DcRepository.ModuleSettings.stGroupedBy, fields);
        buildTree();
    }
    
    @Override
    public void groupBy() {
        fields = (int[]) DcModules.get(getModule()).getSetting(DcRepository.ModuleSettings.stGroupedBy);
        groupBy(fields);
    }

    /**
     * Determines if and how the object should be selected based on a previous selection.
     * @param dco
     */
    @SuppressWarnings("unchecked")
    private void select(String[] oldPath, DcObject dco, int modus) {
        boolean removed = modus == _OBJECT_REMOVED;
        boolean oldPathExists = removed ? exists(oldPath) : false;
        
        String[] keys = removed || oldPathExists ? oldPath : new String[fields.length + 1];
        keys[0] = ((NodeElement) top.getUserObject()).getKey();
        
        if (!removed && !oldPathExists) {
            int counter = 1;
            for (int fieldIdx : fields) {
                DcField field = DcModules.get(getModule()).getField(fieldIdx);
                String key = empty;
                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    List<DcObject> references = (List<DcObject>) dco.getValue(fieldIdx);
                    if (references != null && references.size() > 0)
                        key = references.get(0).toString();
                } else {
                    key = dco.getDisplayString(fieldIdx);
                }
                keys[counter++] = key.trim().length() == 0 ? empty : key;
            }
        } 
        
        int max = oldPath.length >= keys.length ? keys.length :  oldPath.length; 
        boolean isSame = oldPath.length == keys.length;
        for (int i = 0; isSame && i < oldPath.length; i++) {
            isSame = oldPath[i].equals(keys[i]);
        }
        
        boolean exists = exists(keys);
        
        setListeningForSelection(!isSame || !exists);
        select(keys, 0, max, top);
        setListeningForSelection(true);
        
        if (!isSame)
            updateView();
    }
    
    /**
     * Loops thru the tree and selects the nodes with the corresponding keys.
     * The selected node will be the last existing key.
     * @param keys
     * @param level
     * @param max
     * @param parent
     */
    private boolean select(String[] keys, int level, int max, DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode node = level == 0 ? top : findNode(keys[level], parent);
        
        boolean selected = false;
        if (node != null) {
            setSelected(node);
            selected = true;
        }
        
        if (level + 1 < max) {
            int count = parent.getChildCount();
            for (int i = 0; i < count; i++) {
                if (select(keys, level+1, max, node))
                    return true;
            }
        }
        return selected;
    }    
    
    /**
     * Removes the item / element from the tree and removes the leaf if necessary.
     * This method is called recursively.
     */
    private void removeElement(DcObject dco, DefaultMutableTreeNode parent) {
    	
        if (parent.getUserObject() instanceof NodeElement) {
            NodeElement elem = (NodeElement) parent.getUserObject();
            elem.removeValue(dco);
        }
    	
        int count = parent.getChildCount();
        for (int pos = count; pos > 0; pos--) {
            try {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(pos -1);
                NodeElement ne = (NodeElement) node.getUserObject();
                ne.removeValue(dco);
                if (ne.size() == 0) {
                    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                    model.removeNodeFromParent(node);
                    ne.clear();
                } else {
                    removeElement(dco, node);
                }
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }
    
    @Override
    protected JMenuBar getMenu() {
        return new FieldTreePanelMenuBar(getModule(), this);
    }
    
    @Override
    protected void revalidateTree(DcObject dco, int modus) {
        setListeningForSelection(false);
        setSaveChanges(false);
        
        long start = logger.isDebugEnabled() ? new Date().getTime() : 0;
        
        String[] s;
        if (tree.getSelectionPath() != null) {
            s = new String[tree.getSelectionPath().getPath().length];
            int i = 0;
            for (Object o : tree.getSelectionPath().getPath())
                s[i++] = ((NodeElement) ((DefaultMutableTreeNode) o).getUserObject()).getKey();
        } else {
            s = new String[0];
        }
        
        if (modus == _OBJECT_REMOVED) {
            removeElement(dco, top);
        } else if (modus == _OBJECT_UPDATED) {
            removeElement(dco, top);
            addElement(dco, top, 0);
        } else if (modus == _OBJECT_ADDED) {
            removeElement(dco, top);
            addElement(dco, top, 0);
        }

        if (logger.isDebugEnabled()) 
            logger.debug("Tree was update in " + (new Date().getTime() - start) + "ms");

        repaint();
        revalidate();

        setListeningForSelection(true);
        
        // if the top node was selected leave as is
        if (s.length != 1) {
            if (fields != null) // a grouping has been specified. check and correct selection 
                select(s, dco, modus);
            else
                setDefaultSelection(); // set the default selection
        }

        setSaveChanges(true);
    } 
    
    @SuppressWarnings("unchecked")
    private void addElement(DcObject dco, DefaultMutableTreeNode parent, int level) {
        DcField field = null;

        if (parent.getUserObject() instanceof NodeElement) {
            NodeElement elem = (NodeElement) parent.getUserObject();
            elem.addValue(dco);
        }

        if (fields == null || fields.length - 1 < level)
            return;
            
        field = DcModules.get(getModule()).getField(fields[level]);
        
        try {
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                Collection<DcObject> references = (Collection<DcObject>) dco.getValue(field.getIndex());
                if (references != null && references.size() > 0) {
                    for (DcObject reference : references) {
                        String key = reference.toString();
                        DefaultMutableTreeNode node = addElement(key, reference.getIcon(), dco, parent);
                        if (level + 1 < fields.length)
                            addElement(dco, node, level + 1);
                    }
                } else {
                    DefaultMutableTreeNode node = addElement(empty, null, dco, parent);
                    if (level + 1 < fields.length)
                        addElement(dco, node, level + 1);
                }
            } else {
                String key = dco.getDisplayString(field.getIndex());
                key = key.trim().length() == 0 ? empty : key;
                
                
                DefaultMutableTreeNode node;
                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                    DcObject ref = (DcObject) dco.getValue(field.getIndex());
                    node = addElement(key, (ref != null ? ref.getIcon() : null), dco, parent);    
                } else {
                    node = addElement(key, null, dco, parent);
                }
                
                if (level + 1 < fields.length)
                    addElement(dco, node, level + 1);
            }
        } catch (Exception e) {
            logger.error("Error while adding " + dco + " to tree level " + level, e);
        }
    }
    
    /**
     * Adds the object to the tree based on its key. If no leaf exists for the given key it
     * is created and inserted in the correct location.
     * @param key
     * @param dco
     * @param parent
     * @return
     */
    private DefaultMutableTreeNode addElement(String key, ImageIcon icon, DcObject dco, DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode node = findNode(key, parent);
        if (node == null) {
            NodeElement ne = new NodeElement(getModule(), key, icon);
            ne.addValue(dco);
            node = new DefaultMutableTreeNode(ne);

            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            
            List<String> elements = new ArrayList<String>();
            for (int i = 0; i < parent.getChildCount(); i++)
                elements.add(((DefaultMutableTreeNode) parent.getChildAt(i)).getUserObject().toString());
            
            elements.add(node.getUserObject().toString());
            Collections.sort(elements);
            int idx = elements.indexOf(node.getUserObject().toString());
            
            model.insertNodeInto(node, parent, idx);
            tree.expandPath(new TreePath(model.getPathToRoot(node)));
        } else {
            NodeElement ne = (NodeElement) node.getUserObject();
            ne.addValue(dco);
        }
        return node;
    }
    
    @Override
    public boolean isActive() {
        boolean active = isEnabled() && isVisible() && top != null && fields != null && fields.length > 0;
        
        if (active) {
            for (int i = 0; i < fields.length; i++)
                active &= fields[i] != -1;
        }
            
        return active;
    }
    
    @Override
    public void reset() {
        DcModules.get(getModule()).setSetting(DcRepository.ModuleSettings.stGroupedBy, null);
        fields = null;
        super.reset();
    } 

    /************************************************************************
     * Initialization
     ************************************************************************/
    
    @Override
    protected void buildTree() {
        SwingUtilities.invokeLater(
        new Thread() {
            @Override
            public void start() {
                fields = (int[]) DcModules.get(getModule()).getSetting(DcRepository.ModuleSettings.stGroupedBy);
                
                build();
                DefaultMutableTreeNode top = getTopNode();
                
                if (isActive()) {
                    createLeafs(top, gp.getItems(), 0);
                    if (fields != null && fields.length > 1)
                        buildTree(top, 1);
                }
        
                expandAll();
                setDefaultSelection();
                
                revalidate();
                repaint();
            }
        });
    }
    
    /**
     * Creates the tree for each level. Recursive call using the level parameter.
     * Only to be used for initialization, no checks for existence.
     * @param parent
     * @param level
     */
    private void buildTree(DefaultMutableTreeNode parent, int level) {
        // navigate the tree, starting from the top
        int size = parent.getChildCount();
        for (int posa = 0; posa < size; posa++) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent.getChildAt(posa);
            NodeElement ne = (NodeElement) parentNode.getUserObject();
            createLeafs(parentNode, ne.getValues(), level);
            
            if (level + 1 < fields.length)
                buildTree(parentNode, level + 1);
        }
    }
    
    @Override
    protected void createTopNode() {
        DcModule mod = DcModules.get(getModule());
        String orderingOn = mod.getObjectNamePlural();
        
        if (isActive()) {
            orderingOn += " by ";
            
            for (int i = 0; i < fields.length; i++)
                orderingOn += (i > 0 ? " & " : "") + mod.getField(fields[i]).getLabel();
        }
        
        top = new DefaultMutableTreeNode(orderingOn);
        
        NodeElement element = new NodeElement(getModule(), orderingOn, null);
        element.setValues(getItems());
        top.setUserObject(element);
    }
    
    /**
     * Creates the leafs for the given objects. No checks for existing leafs. 
     * Only to be used for initialization.
     * @param parentNode
     * @param objects
     * @param level
     */
    @SuppressWarnings("unchecked")
    protected void createLeafs(DefaultMutableTreeNode parentNode, List<DcObject> objects, int level) {
        // determine the leafs to create
        LinkedHashMap<NodeElement, Collection<DcObject>> keys = new LinkedHashMap<NodeElement, Collection<DcObject>>();
        DcField field = DcModules.get(getModule()).getField(fields[level]);
        
        // field does not longer exist (upgraded version), resetting tree
        if (field == null) {
            reset();
            return;
        }
        
        Collections.sort(objects, new DcObjectComparator(field.getIndex()));
        
        for (DcObject dco : objects) {
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                List<DcObject> references = (List<DcObject>) dco.getValue(field.getIndex());
                
                if (references == null || references.size() == 0) {
                    
                    addKey(keys, new NodeElement(getModule(), empty, null), dco);
                } else {
                    Collections.sort(references);
                    for (DcObject reference : references)
                        addKey(keys, new NodeElement(getModule(), reference.toString(), reference.getIcon()), dco);
                }
            } else {

                String key = dco.getDisplayString(field.getIndex());
                key = key.trim().length() == 0 ? empty : key;

                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                    DcObject ref = (DcObject) dco.getValue(field.getIndex());
                    addKey(keys, new NodeElement(getModule(), key, (ref != null ? ref.getIcon() : null)), dco);
                } else {
                    addKey(keys, new NodeElement(getModule(), key, null), dco);
                }
                
            }
        }
        
        // create the actual leafs
        for (NodeElement key : keys.keySet())
            addLeaf(parentNode, keys.get(key), key);
        
        keys.clear();
    }   
    
    protected void addKey(Map<NodeElement, Collection<DcObject>> keys, NodeElement key, DcObject dco) {
        Collection<DcObject> values = keys.get(key);
        values = values == null ? new ArrayList<DcObject>() : values;
        values.add(dco);
        keys.put(key, values);
    }
    
    private void addLeaf(DefaultMutableTreeNode parentNode, Collection<DcObject> values, NodeElement key) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(key);
        for (DcObject dco : values)
            ((NodeElement) newNode.getUserObject()).addValue(dco);
        
        insertNode(newNode, parentNode);
    }
}
