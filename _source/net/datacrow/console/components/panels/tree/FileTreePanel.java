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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JMenuBar;
import javax.swing.tree.DefaultMutableTreeNode;

import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcObjectComparator;


public class FileTreePanel extends TreePanel {
    
    public FileTreePanel(GroupingPane gp) {
        super(gp);
    }

    @Override
    public void groupBy() {}
    
    @Override
    public String getName() {
        return DcResources.getText("lblFileStructure");
    }
    
    private void createLeafs() {
        build();
        
        setListeningForSelection(false);
        setSaveChanges(false);
        
        List<DcObject> items = getItems();
        
        DcObjectComparator oc = new DcObjectComparator(DcObject._SYS_FILENAME);
        Map<DcObject, List<String>> map = new HashMap<DcObject, List<String>>();
        Collections.sort(items, oc);
        
        for (DcObject dco : items) {
            String filename = dco.getFilename();
            
            if (filename == null)
                continue;
            
            StringTokenizer st = new StringTokenizer(filename, (filename.indexOf("/") > -1 ? "/" : "\\"));
            
            List<String> c = new ArrayList<String>();
            while (st.hasMoreElements())
                c.add((String) st.nextElement());
            
            if (c.size() > 0)
                map.put(dco, c);
        }
        
        List<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
        int level = 0;
        
        DefaultMutableTreeNode parent = top;
        while (true) {
            boolean success = false;
            for (DcObject dco : map.keySet()) {
                List<String> parts = map.get(dco);
                if (parts.size() > level) {
                    success = true;
                    String part = parts.get(level);
                    
                    if (level > 0) {
                        String parentKey = parts.get(level - 1);
                        for (DefaultMutableTreeNode node : nodes) {
                            if (node.getUserObject() instanceof NodeElement) {
                                NodeElement element = (NodeElement) node.getUserObject();
                                if (element.getComparableKey().toLowerCase().equals(parentKey.toLowerCase())) {
                                    parent = node;
                                    break;
                                }
                            }
                        }
                    }
                    
                    DefaultMutableTreeNode node = findNode(part, parent);
                    if (node == null) {
                        node = new DefaultMutableTreeNode();
                        nodes.add(node);
                    }
                    
                    NodeElement element = new NodeElement(getModule(), part, null);
                    node.setUserObject(element);
                    
                    if (parts.size() - 1 == level)
                        element.addValue(dco);
                    
                    insertNode(node, parent);
                }
            }
            
            if (!success)
                break;
            
            level++;
        }
        
        setListeningForSelection(true);
        setSaveChanges(true);
    }
    
    
    
    @Override
    protected void buildTree() {
        build();
        //DefaultMutableTreeNode top = getTopNode();
        
        if (isActive())
            createLeafs();

        //expandAll();
        setDefaultSelection();
        
        revalidate();
        repaint();
    }

    @Override
    protected void createTopNode() {
        DcModule mod = DcModules.get(getModule());
        String orderingOn = mod.getObjectNamePlural();
        
        top = new DefaultMutableTreeNode(orderingOn);
        
        NodeElement element = new NodeElement(getModule(), orderingOn, null);
        element.setValues(new ArrayList<DcObject>());
        top.setUserObject(element);
    }

    @Override
    protected boolean isActive() {
        return true;
    }

    @Override
    protected JMenuBar getMenu() {
        return null;
    }

    @Override
    protected void revalidateTree(DcObject dco, int modus) {}
}
