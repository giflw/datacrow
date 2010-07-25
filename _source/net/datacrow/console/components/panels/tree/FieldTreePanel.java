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
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.datacrow.console.menu.FieldTreePanelMenuBar;
import net.datacrow.core.DcRepository;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcProperty;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcImageIcon;

import org.apache.log4j.Logger;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class FieldTreePanel extends TreePanel {

    private static Logger logger = Logger.getLogger(FieldTreePanel.class.getName());
    private int[] fields;
    private String empty = DcResources.getText("lblEmpty");
    
    private TreeHugger treeHugger;
    
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
        if (treeHugger != null) {
            treeHugger.cancel();
         
            while (treeHugger.isAlive()) {
                
            }
        }
        
        treeHugger = new TreeHugger();
        treeHugger.start();
    }
    
    private class TreeHugger extends Thread {
        
        private boolean stop = false;
        
        @Override
        public void run() {
            createTree();
        }
        
        public void cancel() {
            stop = true;
        }
        
        protected void createTree() {
            fields = (int[]) DcModules.get(getModule()).getSetting(DcRepository.ModuleSettings.stGroupedBy);
            
            build();
            
            if (fields != null) 
                createTree(fields);
            
            SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        public void run() {
                            revalidate();
                            repaint();
                            expandAll();
                        }
                    }));

        }
        
        /**
         * Creates a flat structure of the tree to be build.
         * The tree is created based on the result one SQL statement.
         * @param fields
         */
        private void createTree(int[] fields) {
            DcModule module = DcModules.get(getModule());

            int counter = 0;
            
            StringBuffer columns = new StringBuffer("select ");
            StringBuffer joins = new StringBuffer("from ");
            StringBuffer order = new StringBuffer("order by ");
            
            List<String> joinOn = new ArrayList<String>();
            
            DcField field;
            DcModule reference;
            DcModule main;

            for (int idx : fields) {
                field = module.getField(idx);
                if (field.isUiOnly() &&
                    field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION &&
                    field.getValueType() != DcRepository.ValueTypes._DCOBJECTREFERENCE) continue;
                
                
                if (counter == 0) {
                    columns.append(module.getTableName());
                    columns.append(".ID,");
                } else {
                    columns.append(",");
                }
                
                joinOn.add(module.getTableName() + counter + ".ID");
                
                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                    field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {

                    reference = DcModules.get(field.getReferenceIdx());
                    main = field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE ? module : 
                    DcModules.get(DcModules.getMappingModIdx(getModule(), reference.getIndex(), field.getIndex()));

                    if (counter == 0) {
                        joins.append(module.getTableName());
                        joins.append(" inner join ");
                    } else {
                        joins.append(" left outer join ");
                    }
                        
                    if (    field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                            field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                        
                        columns.append("subselect");
                        columns.append(counter);
                        columns.append(".ID,");
                        columns.append("subselect");
                        columns.append(counter);
                        columns.append(".name,");
                        columns.append("subselect");
                        columns.append(counter);                            
                        columns.append(".icon");

                        if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                            joins.append("(select ");
                            joins.append(reference.getTableName());
                            joins.append(".ID as ID,");
                            joins.append(main.getTableName());
                            joins.append(".");
                            joins.append(main.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName());
                            joins.append(" as parentID, ");
                            joins.append(reference.getTableName());
                            joins.append(".");
                            joins.append(reference.getField(reference.getSystemDisplayFieldIdx()).getDatabaseFieldName());
                            joins.append(" as name,");
                            
                            if (reference.getType() == DcModule._TYPE_PROPERTY_MODULE) {
                                joins.append(reference.getTableName());
                                joins.append(".");
                                joins.append(reference.getField(DcProperty._B_ICON).getDatabaseFieldName());
                            } else {
                                joins.append("NULL");
                            }
                            
                            joins.append(" as icon");
                            joins.append(" from ");
                            joins.append(reference.getTableName());
                            joins.append(" inner join ");
                            joins.append(main.getTableName());
                            joins.append(" on ");
                            joins.append(reference.getTableName());
                            joins.append(".ID = ");
                            joins.append(main.getTableName());
                            joins.append(".");
                            joins.append(main.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName());
                            joins.append(") subselect");
                            joins.append(counter);
                            joins.append(" on ");
                            joins.append(" subselect");
                            joins.append(counter);
                            joins.append(".parentID = ");
                            joins.append(module.getTableName());
                            joins.append(".ID ");

                        } else {
                            joins.append(reference.getTableName());
                            joins.append(" ");
                            joins.append(reference.getTableName());
                            joins.append(counter);
                            joins.append(" on ");
                            joins.append(module.getTableName());
                            joins.append(".");
                            joins.append(field.getDatabaseFieldName());
                            joins.append("=");
                            joins.append(reference.getTableName());
                            joins.append(counter);
                            joins.append(".ID");
                        }

                        if (counter > 0) order.append(",");
                        
                        order.append("subselect");
                        order.append(counter);
                        order.append(".Name");
                    }
                } else {
                    columns.append(module.getTableName());
                    columns.append(".");
                    columns.append(field.getDatabaseFieldName());
                    columns.append(",");
                    columns.append(module.getTableName());
                    columns.append(".");
                    columns.append(field.getDatabaseFieldName());
                    columns.append(",NULL");
                    
                    joinOn.add(module.getTableName() + ".ID");
                    
                    if (counter == 0) {
                        joins.append(module.getTableName());
                    } 
                    
                    if (counter > 0) order.append(",");
                    
                    order.append(module.getTableName());
                    order.append(".");
                    order.append(field.getDatabaseFieldName());
                }
                counter++;
            }
            
            createTree(columns.toString() + " " + joins.toString() + " " + order.toString());
        }
        
        private void createTree(String sql) {
            try {
                DcModule reference;
                DcField field;
                DcModule module = DcModules.get(getModule());
                
                ResultSet rs = DatabaseManager.executeSQL(sql, true);
                
                String clause = "";
                FieldNodeElement ne;
                String value = null;
                Object key = null;
                String icon = null;

                DcModule main;
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                
                DefaultMutableTreeNode current;
                DefaultMutableTreeNode parent;
                DefaultMutableTreeNode previous;
                boolean exists = false;
                while (rs.next() && !stop) {
                    int level = 0;
                    parent = top;
                    
                    for (int idx : fields) {
                        
                        if (stop) break;
                        
                        field = module.getField(idx);
                        
                        // for each level the field index is shifted to the end.
                        key = rs.getObject((level * 3) + 2);
                        value = rs.getString((level * 3) + 3);
                        icon = rs.getString((level * 3) + 4);
                        
                        previous = parent.getChildCount() == 0 ? null : ((DefaultMutableTreeNode) parent.getChildAt(parent.getChildCount() - 1));
                        exists = previous == null || (((NodeElement)  previous.getUserObject()).getKey() == null && key != null) ? false : 
                                ((NodeElement)  previous.getUserObject()).getKey() == key || // empty key
                                ((NodeElement)  previous.getUserObject()).getKey().equals(key);
                        
                        if (!exists) { 
                            
                            
                            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                                field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {

                                reference = DcModules.get(field.getReferenceIdx());
                                main = field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE ? module : 
                                       DcModules.get(DcModules.getMappingModIdx(getModule(), reference.getIndex(), field.getIndex()));

                                if (key != null) {
                                    clause = "select distinct " +
                                            (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ?
                                                    " main." + main.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() : " main.ID") +
                                            " from " + reference.getTableName() + " ref" +
                                            " inner join " + main.getTableName() + " main" + 
                                            (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ?
                                                  " on main." + main.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + " = ref.ID" 
                                                : " on main." + field.getDatabaseFieldName() + " = ref.ID") +
                                             " and ref.ID = ?";
                                } else {
                                    if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                                        clause = "select ID from " + DcModules.get(getModule()).getTableName() + " where " + field.getDatabaseFieldName() + " is null";
                                    } else {
                                        clause = "select ID from " + DcModules.get(getModule()).getTableName() + " where ID not in (" +
                                                 "select " + main.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() + " from " +
                                                 main.getTableName() + ")";
                                    }  
                                }
                            } else {
                                if (key != null) {
                                    clause = "select ID from " + DcModules.get(getModule()).getTableName() + " where " + field.getDatabaseFieldName() + " = ?";
                                } else {
                                    clause = "select ID from " + DcModules.get(getModule()).getTableName() + " where " + field.getDatabaseFieldName() + " is null";
                                }
                            }
                            
                            if (key == null) {
                                ne = new FieldNodeElement(getModule(), field.getIndex(), null, empty, null, clause);
                            } else {
                                ne = new FieldNodeElement(getModule(), field.getIndex(), key, value, (icon == null ? null : new DcImageIcon(Base64.decode(icon.getBytes()))), clause);
                            }
                            
                            current = new DefaultMutableTreeNode(ne);
                            model.insertNodeInto(current, parent, parent.getChildCount());
                            parent = current;
                           
                        } else { // exists
                            FieldNodeElement fne =(FieldNodeElement) previous.getUserObject();
                            fne.setCount(fne.getCount() + 1);
                            parent = previous;    
                        }
                        level++;
                    }
                }
                
                rs.close();
                
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
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
        
        FieldNodeElement element = new FieldNodeElement(getModule(), -1, null, label, null, "select ID from " + DcModules.get(getModule()).getTableName());
        
        
        ResultSet rs;
        try {
            rs = DatabaseManager.executeSQL("select count(*) from " + DcModules.get(getModule()).getTableName(), false);
            rs.next();
            element.setCount(rs.getInt(1));
        } catch (Exception e) {
            logger.error(e, e);
        }
        
        top.setUserObject(element);
        
        
        
    }
    
    @Override
    protected void addElement(Long key, DefaultMutableTreeNode node, int level) {}
}
