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
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.views.MasterView;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

public class GroupingPane extends JPanel {

    private Collection<TreePanel> panels = new ArrayList<TreePanel>();
    private List<DcObject> items = new ArrayList<DcObject>();
    
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
    
    public int getModule() {
        return module;
    }
    
    public MasterView getView() {
        return view;
    }
    
    public void add(DcObject[] items) {
        this.items.clear();
        this.items = new ArrayList<DcObject>();
        for (DcObject item : items)
            this.items.add(item);
        
        for (TreePanel tp : panels)
            tp.buildTree();
    }

    public TreePanel getActiveTree() {
        for (TreePanel tp : panels) {
            if (tp.isShowing())
                return tp;
        }
        
        return null;
    }
    
    public void add(DcObject dco) {
        if (!items.contains(dco)) {
            items.add(dco);
            
            for (TreePanel tp : panels)
                tp.revalidateTree(dco, TreePanel._OBJECT_ADDED);
        }
    }
    
    public int getItemCount() {
        return items.size();
    }
    
    public void groupBy() {
        for (TreePanel tp : panels)
            tp.groupBy();
    }
    
    public List<DcObject> getItems() {
        return new ArrayList<DcObject>(items);
    }
    
    public void saveChanges(boolean b) {
        for (TreePanel tp : panels)
            tp.setSaveChanges(b);
    }
    
    public void reset() {
        // avoid filling the view when it isn't visible.
        if (DcModules.getCurrent().getIndex() == getModule()) {
            add(DataManager.get(getModule(), DataFilters.getCurrent(getModule())));

            for (TreePanel tp : panels)
                tp.reset();
        }
    }
    
    public void remove(String[] ids) {
        for (String ID : ids) {
            DcObject result = null;
            for (DcObject item : items) {
                if (item.getID().equals(ID)) {
                    result = item;
                    break;
                }
            }
            if (result != null)
                remove(result);
        }
    } 
    
    protected void remove(DcObject dco) {
        if (items.contains(dco)) {
            items.remove(dco);
            for (TreePanel tp : panels)
                tp.revalidateTree(dco, TreePanel._OBJECT_REMOVED);
        }
    }    
    
    public void update(DcObject dco) {
        int pos = items.indexOf(dco);
        if (pos != -1) {
            DcObject o = items.get(pos);
            o.reload();
            
            for (TreePanel tp : panels)
                tp.revalidateTree(dco, TreePanel._OBJECT_UPDATED);
        }
    } 
    
    public boolean isActive() {
        boolean active = true;
        for (TreePanel tp : panels)
            active &= tp.isActive();
        return active;
    }
    
    public void updateView() {
        for (TreePanel tp : panels)
            tp.updateView();
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
