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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
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
    
    
    protected ArrayList<DefaultMutableTreeNode> findNode(Collection<String> parts, DefaultMutableTreeNode parentNode) {
        ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
        int level = 0;
        for (String part : parts) {
            DefaultMutableTreeNode parent = nodes.size() == 0 ? parentNode : nodes.get(level - 1);
            DefaultMutableTreeNode node = findNode(part, parent);
            if (node != null)
                nodes.add(node);
            else
                break;
            
            level++;
        }
        return nodes;
    }  
    
    private void createLeafs() {
        
        SwingUtilities.invokeLater(
        new Thread() {
            @Override
            public void start() {
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
                
                for (DcObject dco : map.keySet()) {
                    List<String> parts = map.get(dco);
                    ArrayList<DefaultMutableTreeNode> nodes = findNode(parts, top);
                    if (nodes.size() != parts.size()) {
                        for (int i = nodes.size(); i < parts.size(); i++) {
                            DefaultMutableTreeNode node = new DefaultMutableTreeNode();
                            
                            NodeElement element = new NodeElement(getModule(), parts.get(i), null);
                            node.setUserObject(element);
                            
                            DefaultMutableTreeNode parent = i == 0 ? top : nodes.get(i - 1);
                            
                            if (parts.size() - 1 == i) 
                                element.addValue(dco);
                            
                            nodes.add(node);
                            insertNode(node, parent);
                        }
                    } else if (nodes.size() > 0) {
                        ((NodeElement) nodes.get(nodes.size() - 1).getUserObject()).addValue(dco);
                    }
                    
                    try {
                        sleep(10);
                    } catch (Exception ignore) {}
                }
                
                setListeningForSelection(true);
                setSaveChanges(true);
                expandAll();
            }
        }); 
    }
    
    @Override
    protected void buildTree() {
        build();
        
        if (isActive())
            createLeafs();

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
