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

import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import net.datacrow.console.menu.ContainerTreePanelMenuBar;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;

public class ContainerTreePanel extends TreePanel {

    public ContainerTreePanel(GroupingPane gp) {
        super(gp);
    }
    
    @Override
    protected JMenuBar getMenu() {
        return new ContainerTreePanelMenuBar(getModule(), this);
    }
    
    @Override
    protected void createTopNode() {
        top = new DefaultMutableTreeNode(DcModules.get(DcModules._CONTAINER).getLabel());
//        NodeElement element = new NodeElement(getModule(), DcModules.get(DcModules._CONTAINER).getLabel(), null);
//        element.setValues(new ArrayList<DcObject>());
//        top.setUserObject(element);
    }

    @Override
    public void groupBy() {}
    
    @Override
    protected void removeElement(Long key, DefaultMutableTreeNode parentNode) {
//        DefaultMutableTreeNode node;
//        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
//        while ((node = findNode(dco.toString(), getTopNode(), true)) != null) {
//            model.removeNodeFromParent(node);
//        }
//        
//        if (parentNode.getUserObject() instanceof NodeElement) {
//            NodeElement elem = (NodeElement) parentNode.getUserObject();
//            elem.removeValue(dco);
//        }
    }
    
    @Override
    public String getName() {
        return DcModules.get(DcModules._CONTAINER).getObjectNamePlural();
    }    
    
    @Override
    protected void addElement(Long key, DefaultMutableTreeNode parent, int level) {

//        DcModule module = DcModules.get(DcModules._CONTAINER);
//        boolean flatView = module.getSettings().getBoolean(DcRepository.ModuleSettings.stContainerTreePanelFlat);
//        
//        NodeElement neTop = (NodeElement) top.getUserObject();
//        neTop.addValue(key);
        
//        Container container = (Container) dco;
//        
//        // Additional check to make sure that the correct parent node is being used.
//        if (!flatView && container.getParentContainer() != null) {
//            DefaultMutableTreeNode node = findNode(container.getParentContainer().toString(), getTopNode(), true);
//            parent = node == null ? parent : node;
//        }
//        
//        NodeElement ne = new NodeElement(DcModules._CONTAINER, container.toString(), container.getIcon());
//        ne.addValue(container);
//        DefaultMutableTreeNode masterNode = new DefaultMutableTreeNode(ne);
//        insertNode(masterNode, parent);
//        
//        if (!flatView) {
//            DefaultMutableTreeNode childNode;
//            for (Container child : container.getChildContainers()) {
//                childNode = findNode(child.toString(), masterNode, true);
//                
//                if (childNode == null) {
//                    addElement(child, masterNode, level++);
//                } else {
//                    NodeElement element = (NodeElement) childNode.getUserObject();
//                    element.addValue(child);
//                }
//            }
//        }
    }
    
    @Override
    protected void createTree() {
        build();
        
        if (!isActive()) return;

        createLeafs();
    }
    
    private void createLeafs() {
        build();
        
        DcModule module = DcModules.get(DcModules._CONTAINER);
        boolean flatView = module.getSettings().getBoolean(DcRepository.ModuleSettings.stContainerTreePanelFlat);

        tree.setEnabled(false);
        setListeningForSelection(false);
        setSaveChanges(false);
        
        List<Long> keys = getValues();
        
        for (Long key : keys) {
            //if (flatView || ((Container) dco).getParentContainer() == null)
                addElement(key, top, 0);
        }

        setListeningForSelection(true);
        setSaveChanges(true);
        tree.setEnabled(true);

        expandAll();
        
        if (isShowing())
            setDefaultSelection();
        
        revalidate();
        repaint();
    }
    
    @Override
    public void valueChanged(TreeSelectionEvent e) {
//        
//        if (!isListeningForSelection())
//            return;
//        
//        if (e.getNewLeadSelectionPath() == null || 
//            e.getNewLeadSelectionPath().equals(e.getOldLeadSelectionPath()))
//            return;
//        
//        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
//
//        if (node == null) {
//            currentUserObject = null;
//            return;
//        }
//
//        Object o = node.getUserObject();
//        
//        if (!o.equals(currentUserObject) && getView().getCurrent() != null) {
//            currentUserObject = o;
//            getView().getCurrent().clear(isSaveChanges());
//            NodeElement currentNode = (NodeElement) o;
//            
//            Settings settings = DcModules.get(DcModules._CONTAINER).getSettings();
//            
//            if (settings.getInt(DcRepository.ModuleSettings.stTreePanelShownItems) == DcModules._ITEM) {
//                List<DcObject> containers = currentNode.getItems();
//                List<DcObject> objects = new ArrayList<DcObject>();
//                for (DcObject container : containers ) {
//                    objects.addAll(container.getChildren());
//                }
//                
//                DataFilter df = DataFilters.getCurrent(DcModules._ITEM);
//                df.sort(objects);
//                updateView(objects);
//            } else {
//                updateView(currentNode.getItems());
//            }
//            
//            setSelected(node);
//        }
    }
    
    @Override
    public void updateView() {
//        if (currentUserObject instanceof NodeElement) {
//            gp.getView().clear();
//            NodeElement currentNode = (NodeElement) currentUserObject;
//            
//            if (currentNode.getItems() != null) {
//                Settings settings = DcModules.get(DcModules._CONTAINER).getSettings();
//                if (settings.getInt(DcRepository.ModuleSettings.stTreePanelShownItems) == DcModules._ITEM) {
//                    List<DcObject> containers = currentNode.getItems();
//                    List<DcObject> objects = new ArrayList<DcObject>();
//                    for (DcObject container : containers ) {
//                        objects.addAll(container.getChildren());
//                    }
//                    
//                    DataFilter df = DataFilters.getCurrent(DcModules._ITEM);
//                    df.sort(objects);
//                    updateView(objects);
//                } else {
//                    updateView(currentNode.getItems());
//                }
//            }
//        }
    }  
}
