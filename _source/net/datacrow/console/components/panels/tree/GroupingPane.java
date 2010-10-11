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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.views.MasterView;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

public class GroupingPane extends JPanel {

    private Collection<TreePanel> panels = new ArrayList<TreePanel>();
    
    private int module;
    private MasterView view;
    
    public GroupingPane(int module, MasterView view) {
        this.module = module;
        this.view = view;
        
        if (module == DcModules._CONTAINER) 
            panels.add(new ContainerTreePanel(this));
        else 
            panels.add(new FieldTreePanel(this));
        
        if (DcModules.get(module).isFileBacked())
            panels.add(new FileTreePanel(this));
        
        build();
    }
    
    public void update(DcObject dco) {
    	for (TreePanel tp : panels)
    		tp.update(dco);
    }
    
    public boolean isLoaded() {
        boolean loaded = false;
        for (TreePanel tp : panels)
            loaded |= tp.isLoaded();
        
        return loaded;
    }
    
    public void remove(String item) {
    	for (TreePanel tp : panels)
    		tp.remove(item);
    }
    
    public void add(DcObject dco) {
    	for (TreePanel tp : panels)
    		tp.add(dco);
    }
    
    public boolean isHoldingItems() {
        for (TreePanel tp : panels) {
            return tp.isHoldingItems();
        }
        
        return false;
    }
    
    public int getModule() {
        return module;
    }
    
    public MasterView getView() {
        return view;
    }
    
    public void clear() {
        for (TreePanel tp : panels) {
            tp.clear();
        }
    }
    
    public void load() {
        for (TreePanel tp : panels) {
            tp.clear();
            tp.createTree();
        }
    }

    public TreePanel getActiveTree() {
        for (TreePanel tp : panels) {
            if (tp.isShowing())
                return tp;
        }
        
        return null;
    }
    
    public void updateView() {
        for (TreePanel tp : panels) {
            if (tp.isShowing()) {
            	DcDefaultMutableTreeNode node = (DcDefaultMutableTreeNode) tp.getLastSelectedPathComponent();
            	
            	if (node != null)
            		tp.updateView(node.getItems());
            	else 
            		tp.setDefaultSelection();
            }
        }
    }
    
    public void groupBy() {
        for (TreePanel tp : panels)
            tp.groupBy();
    }
    
    public void saveChanges(boolean b) {
        for (TreePanel tp : panels)
            tp.setSaveChanges(b);
    }
    
    public boolean isActive() {
        boolean active = true;
        for (TreePanel tp : panels)
            active &= tp.isActive();
        return active;
    }
    
    private void build() {
        JTabbedPane tp = ComponentFactory.getTabbedPane();
        
        for (TreePanel panel : panels)
            tp.addTab(panel.getName(), panel);
        
        setLayout(Layout.getGBL());
        
        add(tp, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(0,0,0,0), 0, 0));
    }
}
