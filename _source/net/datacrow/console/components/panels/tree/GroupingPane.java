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
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.views.MasterView;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.settings.DcSettings;

public class GroupingPane extends JPanel implements ChangeListener {

	private List<TreePanel> panels = new ArrayList<TreePanel>();
    
	private int current = 0;
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
    
    @Override
	public boolean isEnabled() {
		return super.isEnabled() && DcSettings.getBoolean(DcRepository.Settings.stShowGroupingPanel);
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
    
    private TreePanel getCurrent() {
    	return panels.get(current);
    }
    
    public void load() {
    	TreePanel tp = getCurrent();
		tp.groupBy();
    }
    
    public void updateView() {
        for (TreePanel tp : panels) {
            if (tp.isEnabled()) {
            	DcDefaultMutableTreeNode node = (DcDefaultMutableTreeNode) tp.getLastSelectedPathComponent();
            	if (node != null)
            		tp.updateView(node.getItemsSorted(tp.top.getItemList()));
            	else 
            		tp.setDefaultSelection();
            }
        }
    }
    
    public void sort() {
        for (TreePanel tp : panels) {
        	if (tp.isEnabled() && tp.isLoaded()) {
	            tp.sort();
	            tp.refreshView();
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
    
    private void build() {
        JTabbedPane tp = ComponentFactory.getTabbedPane();
        
        tp.addChangeListener(this);
        
        for (TreePanel panel : panels)
            tp.addTab(panel.getName(), panel);
        
        setLayout(Layout.getGBL());
        
        add(tp, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(0,0,0,0), 0, 0));
    }
    
    @Override
	public void stateChanged(ChangeEvent ce) {
        JTabbedPane pane = (JTabbedPane) ce.getSource();
        current = pane.getSelectedIndex();
        panels.get(current).activate();
	}
}
