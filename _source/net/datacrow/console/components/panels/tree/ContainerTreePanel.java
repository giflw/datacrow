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
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import net.datacrow.console.menu.ContainerTreePanelMenuBar;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.settings.Settings;

public class ContainerTreePanel extends FieldTreePanel {

    public ContainerTreePanel(GroupingPane gp) {
        super(gp);
    }
    
    @Override
    protected JMenuBar getMenu() {
        return new ContainerTreePanelMenuBar(getModule(), this);
    }
    
    @Override
    public void updateView() {
        if (currentUserObject instanceof NodeElement) {
            gp.getView().clear();
            NodeElement currentNode = (NodeElement) currentUserObject;
            
            if (currentNode.getValues() != null) {
                Settings settings = DcModules.get(DcModules._CONTAINER).getSettings();
                if (settings.getInt(DcRepository.ModuleSettings.stTreePanelShownItems) == DcModules._ITEM) {
                    List<DcObject> containers = currentNode.getValues();
                    List<DcObject> objects = new ArrayList<DcObject>();
                    for (DcObject container : containers ) {
                        objects.addAll(container.getChildren());
                    }
                    
                    DataFilter df = DataFilters.getDefaultDataFilter(DcModules._ITEM);
                    df.sort(objects);
                    updateView(objects);
                } else {
                    updateView(currentNode.getSortedValues());
                }
            }
        }
    }    

    @Override
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
        
        if (!o.equals(currentUserObject) && getView().getCurrent() != null) {
            currentUserObject = o;
            getView().getCurrent().clear(isSaveChanges());
            NodeElement currentNode = (NodeElement) o;
            
            Settings settings = DcModules.get(DcModules._CONTAINER).getSettings();
            
            if (settings.getInt(DcRepository.ModuleSettings.stTreePanelShownItems) == DcModules._ITEM) {
                List<DcObject> containers = currentNode.getValues();
                List<DcObject> objects = new ArrayList<DcObject>();
                for (DcObject container : containers ) {
                    objects.addAll(container.getChildren());
                }
                
                DataFilter df = DataFilters.getDefaultDataFilter(DcModules._ITEM);
                df.sort(objects);
                updateView(objects);
            } else {
                updateView(currentNode.getSortedValues());
            }
            
            setSelected(node);
        }
    }
}
