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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.datacrow.console.menu.FieldTreePanelMenuBar;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.db.Query;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;

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
        createTree();
    }
    
    @Override
    public void groupBy() {
        fields = (int[]) DcModules.get(getModule()).getSetting(DcRepository.ModuleSettings.stGroupedBy);
        groupBy(fields);
    }

    @Override
    protected JMenuBar getMenu() {
        return new FieldTreePanelMenuBar(getModule(), this);
    }
    
    @Override
    public boolean isActive() {
        boolean active = isEnabled() && top != null && fields != null && fields.length > 0;
        
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
    protected void createTree() {
        tree.setEnabled(false);
        fields = (int[]) DcModules.get(getModule()).getSetting(DcRepository.ModuleSettings.stGroupedBy);
        
        build();
        DefaultMutableTreeNode top = getTopNode();
        
        if (fields != null) {
            
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            DcModule module = DcModules.get(getModule());

            String sql = "";
            DcField field;
            for (int idx : fields) {
                field = module.getField(idx);
                
                if (field.isUiOnly() &&
                    field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION &&
                    field.getValueType() != DcRepository.ValueTypes._DCOBJECTREFERENCE) continue;
                
                try {
                
                    if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                        field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                        
                        DcModule reference = DcModules.get(field.getReferenceIdx());
                        
                        List<DcObject> items = new ArrayList<DcObject>();
                        if (DataManager.isCached(reference.getIndex())) {
                            items.addAll(DataManager.get(reference.getIndex(), null));
                        } else {
                            sql = "select distinct ID, " + reference.getField(reference.getDisplayFieldIdx()).getDatabaseFieldName() + 
                                  " from " + reference.getTableName() + " order by 2 desc";
                            items.addAll(DatabaseManager.retrieveItems(sql, Query._SELECT));
                        }

                        FieldNodeElement ne = new FieldNodeElement(getModule(), idx, empty, null);
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(ne);
                        model.insertNodeInto(node, top, 0);
                        
                        for (DcObject dco : items) {
                            ne = new FieldNodeElement(getModule(), idx, dco, dco.getIcon());
                            node = new DefaultMutableTreeNode(ne);
                            model.insertNodeInto(node, top, 0);
                        }
                        
                    } else {
                        sql = "select " + field.getDatabaseFieldName() + " from " + DcModules.get(getModule()).getTableName()+ " order by 1 desc";
                        ResultSet rs = DatabaseManager.executeSQL(sql, false);
                        
                        FieldNodeElement ne;
                        DefaultMutableTreeNode node;
                        while (rs.next()) {
                            ne = new FieldNodeElement(getModule(), idx, rs.getObject(1), null);
                            node = new DefaultMutableTreeNode(ne);
                            model.insertNodeInto(node, top, 0);
                        }
                    }
                    
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }
        
        expandAll();

        if (isShowing())
            setDefaultSelection();
        
        // if the tree is not active (at all) make sure the view is at least filled.
        // Fix for: 2831212
        if (!isActive()) {
            setListeningForSelection(true);
            tree.setSelectionInterval(0, 0);
        }
        
        tree.setEnabled(true);
        revalidate();
        repaint();
    }
    
    @Override
    protected void createTopNode() {
        DcModule mod = DcModules.get(getModule());
        String label = mod.getObjectNamePlural();
        
        if (isActive()) {
            label += " by ";
            
            for (int i = 0; i < fields.length; i++)
                label += (i > 0 ? " & " : "") + mod.getField(fields[i]).getLabel();
        }
        
        top = new DefaultMutableTreeNode(label);
        
        FieldNodeElement element = new FieldNodeElement(getModule(), -1, label, null);
        
        top.setUserObject(element);
    }
    
    @Override
    protected void addElement(Long key, DefaultMutableTreeNode node, int level) {
        // TODO Auto-generated method stub
        
    }
}
