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

import javax.swing.JMenuBar;
import javax.swing.event.TreeSelectionEvent;

import net.datacrow.console.menu.ContainerTreePanelMenuBar;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

/**
 * TODO implement
 * 
 * @author Robert Jan van der Waals
 */
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
        NodeElement ne = new NodeElement(getModule(), DcModules.get(DcModules._CONTAINER).getLabel(), null);
        top = new DcDefaultMutableTreeNode(ne);
    }

    @Override
    public void groupBy() {}
    
    @Override
	public void sort() {}
    
    @Override
    public DcDefaultMutableTreeNode getFullPath(DcObject dco) {
        return null;
    }  

	@Override
    public String getName() {
        return DcModules.get(DcModules._CONTAINER).getObjectNamePlural();
    }    
    
    @Override
    protected void createTree() {
        build();
        
        if (!isActive()) return;

        createLeafs();
    }
    
    private void createLeafs() {
//        build();
//        
//        DcModule module = DcModules.get(DcModules._CONTAINER);
//        boolean flatView = module.getSettings().getBoolean(DcRepository.ModuleSettings.stContainerTreePanelFlat);
//
//        tree.setEnabled(false);
//        setListeningForSelection(false);
//        setSaveChanges(false);
//        
//        List<Long> keys = getValues();
//        
//        for (Long key : keys) {
//            //if (flatView || ((Container) dco).getParentContainer() == null)
//                addElement(key, top, 0);
//        }
//
//        setListeningForSelection(true);
//        setSaveChanges(true);
//        tree.setEnabled(true);
//
//        expandAll();
//        
//        if (isShowing())
//            setDefaultSelection();
//        
//        revalidate();
//        repaint();
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


}
