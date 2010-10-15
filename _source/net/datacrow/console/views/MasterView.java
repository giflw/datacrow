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

package net.datacrow.console.views;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.datacrow.console.Layout;
import net.datacrow.console.components.panels.tree.GroupingPane;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

/**
 * Thread safe view encapsulation
 * @author Robert Jan van der Waals
 */
public class MasterView {

    public static final int _TABLE_VIEW = 0;
    public static final int _LIST_VIEW = 1;
    
    private final Map<Integer, View> views = new HashMap<Integer, View>();
    private GroupingPane groupingPane;
    
    public void setTreePanel(DcModule module) {
        if (!module.isChildModule())
            this.groupingPane = new GroupingPane(module.getIndex(), this);
    }
    
    public GroupingPane getGroupingPane() {
    	return groupingPane;
    }
    
    public JPanel getViewPanel() {
        JPanel panel = new JPanel();
        View view = getCurrent();
        panel.setLayout(Layout.getGBL());
        panel.add( view, Layout.getGBC( 0, 0, 2, 1, 2.0, 2.0
                  ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                   new Insets(5, 5, 5,  5), 0, 0));
        
        return panel;
    }
    
    public void setView(int index) {
        DataCrow.mainFrame.applyView(index);
        
        if (groupingPane != null)
            groupingPane.saveChanges(false);
        
        if (groupingPane != null)
            groupingPane.saveChanges(true);
    }
    
    public View get(int index) {
        return views.get(index);
    }
    
    public void refreshQuickView() {
        for (View view : views.values())
            view.refreshQuickView();
    }
    
    public View getCurrent() {
        int view = DcModules.getCurrent().getSettings().getInt(DcRepository.ModuleSettings.stDefaultView);
        View current = get(view);
        
        // Get the first available view if the view cannot be found (for whatever reason)
        if (current == null) {
            for (Integer key : views.keySet()) { 
                current = views.get(key);
                break;
            }
        }
        return current;
    }      
    
    public boolean isLoaded() {
        return (groupingPane != null && groupingPane.isLoaded()) || getCurrent().isLoaded();
    }
    
    public void update(DcObject dco) {
        if (groupingPane != null)
            groupingPane.update(dco);
        
        for (View view : getViews())
            view.update(dco.getID());
    }
    
    public void add(DcObject dco) {
    	getCurrent().add(dco);
        
        if (groupingPane != null)
            groupingPane.add(dco);
    }
    
    public void remove(String key) {
        
        for (View view : getViews())
            view.remove(new String[] {key});

        if (groupingPane != null)
            groupingPane.remove(key);
    }
    
    public void addView(int index, View view) {
        views.put(index, view);        
    }

    public void setStatus(String message) {
        getCurrent().setStatus(message);
    }    
    
    public void saveSettings() {
        for (View view : getViews())
            view.saveSettings();
    }    
    
    public void applySettings() {
        for (View view : getViews())
            view.applySettings();
    }
    
    public void sort() {
    	if (getGroupingPane() == null || !getGroupingPane().isActive()) {
    		groupingPane.sort();
    	} else {
    		getCurrent().sort();
    	}
    }
    
    public void refresh() {
        for (View view : getViews()) {
            view.clear();
        }
        
        if (groupingPane != null)
            groupingPane.groupBy();
        
    }

    public void removeFromCache(String ID) {
        for (View view : getViews())
            view.removeFromCache(ID);
    }    
    
    public void clear() {
        for (View view : getViews())
            view.clear();
        
        if (groupingPane != null)
            groupingPane.clear();
    }

    public void clear(boolean saveChanges) {
        for (View view : getViews())
            view.clear(saveChanges);

        if (groupingPane != null)
            groupingPane.clear();
    }
    
    public void add(final List<String> keys) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	for (View view : getViews())
                    view.clear();
                
                if (groupingPane != null || !groupingPane.isActive()) {
                    groupingPane.load();
                } else { 
                	for (View view : getViews())
                		view.add(keys);
                }
            }
        });
    }
    
    public Collection<View> getViews() {
        Collection<View> c = new ArrayList<View>();
        c.addAll(views.values());
        return c;
    }
}
