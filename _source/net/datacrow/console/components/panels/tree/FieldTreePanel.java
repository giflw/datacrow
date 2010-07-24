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
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcImageIcon;

import org.apache.log4j.Logger;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class FieldTreePanel extends TreePanel {

    private static Logger logger = Logger.getLogger(FieldTreePanel.class.getName());
    private int[] fields;
    //private String empty = DcResources.getText("lblEmpty");
    
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
        TreeHugger treeHugger = new TreeHugger();
        treeHugger.start();
    }
    
    private class TreeHugger extends Thread {
        
        @Override
        public void run() {
            createTree();
        }
        
        protected void createTree() {
            fields = (int[]) DcModules.get(getModule()).getSetting(DcRepository.ModuleSettings.stGroupedBy);
            
            build();
            if (fields != null) createTree(fields);
            expandAll();

            if (isShowing()) setDefaultSelection();
            tree.setEnabled(true);
            
            revalidate();
            repaint();
        }
        
        private void createTree(int[] fields) {
            DcModule module = DcModules.get(getModule());

            int counter = 0;
            
            StringBuffer columns = new StringBuffer("select ");
            StringBuffer joins = new StringBuffer("from ");
            StringBuffer order = new StringBuffer("order by ");
            
            List<String> joinOn = new ArrayList<String>();
            
            DcField field;
            DcModule reference;

            for (int idx : fields) {
                field = module.getField(idx);
                if (field.isUiOnly() &&
                    field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION &&
                    field.getValueType() != DcRepository.ValueTypes._DCOBJECTREFERENCE) continue;
                
                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                    field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {

                    reference = DcModules.get(field.getReferenceIdx());
                    
//                    // main module or mapping module
//                    main = field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE ? module : 
//                               DcModules.get(DcModules.getMappingModIdx(getModule(), reference.getIndex(), field.getIndex()));
                               
                               
                    
                } else {
                    
                    if (counter == 0) {
                        columns.append(module.getTableName());
                        columns.append(counter);
                        columns.append(".ID,");
                    } else {
                        columns.append(",");
                    }
                    
                    columns.append(module.getTableName());
                    columns.append(counter);
                    columns.append(".");
                    columns.append(field.getDatabaseFieldName());
                    columns.append(",'NULL'");
                    
                    joinOn.add(module.getTableName() + counter + ".ID");
                    
                    if (counter == 0) {
                        joins.append(module.getTableName());
                        joins.append(" ");
                        joins.append(module.getTableName());
                        joins.append(counter);
                    } else {
                        joins.append(" left outer join ");
                        joins.append(module.getTableName());
                        joins.append(" ");
                        joins.append(module.getTableName());
                        joins.append(counter);
                        joins.append(" on ");
                        joins.append(module.getTableName());
                        joins.append(counter);
                        joins.append(".ID = ");
                        joins.append(joinOn.get(counter - 1));
                    }
                    
                    if (counter > 0) order.append(",");
                    
                    order.append(module.getTableName());
                    order.append(counter);
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
                
                String clause;
                FieldNodeElement ne;
                Object value = null;
                String icon = null;

                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                
                DefaultMutableTreeNode current;
                DefaultMutableTreeNode parent;
                DefaultMutableTreeNode previous;
                boolean exists = false;
                while (rs.next()) {
                    int level = 0;
                    parent = top;
                    
                    for (int idx : fields) {
                        field = module.getField(idx);
                        
                        value = rs.getObject((level * 2) + 2);
                        icon = rs.getString((level * 2) + 3);
                        
                        if (value == null)
                            continue;
                        
                        previous = parent.getChildCount() == 0 ? null : ((DefaultMutableTreeNode) parent.getChildAt(parent.getChildCount() - 1));
                        exists = previous == null ? false : ((NodeElement)  previous.getUserObject()).getValue().equals(value);
                        
                        if (!exists) { 
                            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                                field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                                    
                                reference = DcModules.get(field.getReferenceIdx());
                                clause = "select distinct " +
                                        (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ?
                                                " main." + module.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() : " main.ID") +
                                        " from " + reference.getTableName() + " ref" +
                                        " inner join " + module.getTableName() + " main" + 
                                        (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ?
                                              " on main." + module.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + " = ref.ID" 
                                            : " on main." + field.getDatabaseFieldName() + " = ref.ID") +
                                         " and ref.ID = ?";
                            } else {
                                clause = "select ID from " + DcModules.get(getModule()).getTableName() + " where " + field.getDatabaseFieldName() + " = ? order by 1 desc";
                            }
                            
                            ne = new FieldNodeElement(getModule(), field.getIndex(), value, (icon == null ? null : new DcImageIcon(Base64.decode(icon.getBytes()))), clause);
                            current = new DefaultMutableTreeNode(ne);
                            model.insertNodeInto(current, parent, parent.getChildCount());
                            parent = current;
                        } else {
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
        
//        private void createBranches(DcField field, int level) {
//            if (field.isUiOnly() &&
//                field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION &&
//                field.getValueType() != DcRepository.ValueTypes._DCOBJECTREFERENCE) return;
//            
//            if (level == 0) {
//                createBranches(field, top, level);
//            } else {
//            
//                for (int i = 0; i < top.getChildCount(); i++) {
//                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) top.getChildAt(i);
//                    if (node.getDepth() == level - 1) {
//                        if (level == 1)
//                            createBranches(field, node, level);
//                        else
//                            createBranches(field, (DefaultMutableTreeNode) node.getLastChild(), level);
//                    }
//                }
//            }
//        }
//        
//        private void createBranches(final DcField field, final DefaultMutableTreeNode parent, final int level) { 
//            String sql;
//            String clause;
//            final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
//            
//            List<NodeElement> parents = new ArrayList<NodeElement>();
//            for (Object o : parent.getUserObjectPath()) {
//                parents.add((NodeElement) o);
//            }
//            
//            try {
//            
//                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
//                    field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
//                    
//                    DcModule reference = DcModules.get(field.getReferenceIdx());
//                    DcModule module = field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE ? 
//                            DcModules.get(getModule()) : DcModules.get(DcModules.getMappingModIdx(getModule(), reference.getIndex(), field.getIndex()));
//                    
//                    List<DcObject> items = new ArrayList<DcObject>();
//                    sql = "select distinct ref.ID, ref." + reference.getField(reference.getSystemDisplayFieldIdx()).getDatabaseFieldName() + 
//                            (reference.getType() == DcModule._TYPE_PROPERTY_MODULE ? ",ref." + reference.getField(DcProperty._B_ICON) : "") +
//                          " from " + reference.getTableName() + " ref" +
//                          " inner join " + module.getTableName() + " main" + 
//                          (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ?
//                                " on main." + module.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + " = ref.ID" 
//                              : " on main." + field.getDatabaseFieldName() + " = ref.ID");
//                    
//                    
//                    String sub = field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ? 
//                                 " main." + module.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() : "main.ID";
//                    
//                    int counter = 0;
//                    for (NodeElement ne : parents) {
//                        if (!Utilities.isEmpty(ne.getWhereClause())) {
//                            sql += counter > 0 ? " and " + sub + " in (" : " where " + sub + " in (";
//                            sql += ne.getWhereClause() + ")";
//                            counter++;
//                        }
//                    }
//                    
//                    try {
//                        PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
//                        int index = 1;
//                        for (NodeElement e : parents) {
//                            if (!Utilities.isEmpty(e.getWhereClause()) && e.getValue() != null) {
//                                ps.setObject(index, e.getValue());
//                                index++;
//                            }
//                        }
//                        
//                        items.addAll(DatabaseManager.retrieveItems(ps, Query._SELECT));
//
//                        clause = "select distinct " +
//                                (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ?
//                                        " main." + module.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() : " main.ID") +
//                                " from " + reference.getTableName() + " ref" +
//                                " inner join " + module.getTableName() + " main" + 
//                                (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ?
//                                      " on main." + module.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + " = ref.ID" 
//                                    : " on main." + field.getDatabaseFieldName() + " = ref.ID") +
//                                 " and ref.ID = ?";
//                        
//                        FieldNodeElement ne = new FieldNodeElement(getModule(), field.getIndex(), empty, null, clause);
//                        
//                        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(ne);
//                        model.insertNodeInto(node, parent, 0);
//                        
//                        for (DcObject dco : items) {
//                            ne = new FieldNodeElement(getModule(), field.getIndex(), dco, dco.getIcon(), clause);
//                            model.insertNodeInto(new DefaultMutableTreeNode(ne), parent, 0);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    
//                } else {
//                    sql = "select distinct " + field.getDatabaseFieldName() + " from " + DcModules.get(getModule()).getTableName()+ " where " + field.getDatabaseFieldName() + " is not null order by 1 desc";
//                    clause = "select ID from " + DcModules.get(getModule()).getTableName() + " where " + field.getDatabaseFieldName() + " = ? order by 1 desc";
//                    ResultSet rs = DatabaseManager.executeSQL(sql, false);
//                    
//                    FieldNodeElement ne;
//                    DefaultMutableTreeNode node;
//                    while (rs.next()) {
//                        ne = new FieldNodeElement(getModule(), field.getIndex(), rs.getObject(1), null, clause);
//                        parents.add(ne);
//                        node = new DefaultMutableTreeNode(ne);
//                        model.insertNodeInto(node, parent, 0);
//                    }
//                }
//                
//            } catch (Exception e) {
//                logger.error(e, e);
//            }        
//        }
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
        
        FieldNodeElement element = new FieldNodeElement(getModule(), -1, label, null, "");
        top.setUserObject(element);
    }
    
    @Override
    protected void addElement(Long key, DefaultMutableTreeNode node, int level) {}
}
