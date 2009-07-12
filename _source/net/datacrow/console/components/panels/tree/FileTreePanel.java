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
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import net.datacrow.console.views.View;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcObjectComparator;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class FileTreePanel extends TreePanel {
    
    private static Logger logger = Logger.getLogger(FileTreePanel.class.getName());
    
    private FillerThread filler;
    private ThreadGroup tg = new ThreadGroup("tree-fillers");
    
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
        
        if (filler != null)
            filler.cancel();
        
        filler = new FillerThread();
        filler.start();
    }
    
    @Override
    protected void buildTree() {
        build();
        
        if (isActive())
            createLeafs();
    }

    @Override
    protected void createTopNode() {
        top = new DefaultMutableTreeNode(DcResources.getText("lblFileTreeSystem"));
        NodeElement element = new NodeElement(getModule(), DcResources.getText("lblFileTreeSystem"), null);
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
    
    private class FillerThread extends Thread {
        
        private boolean canceled = false;
        
        public FillerThread() {
            super(tg, "");
        }
        
        public void cancel() {
            canceled = true;
        }
        
        @Override
        public void run() {
            
            while (getThreadGroup().activeCount() > 1) {
                try {
                    sleep(100);
                } catch (Exception ignore) {}
            }
            
            build();

            tree.setEnabled(false);
            setListeningForSelection(false);
            setSaveChanges(false);
            
            List<DcObject> items = getItems();
            
            DcObjectComparator oc = new DcObjectComparator(DcObject._SYS_FILENAME);
            Collections.sort(items, oc);

            // thread safe
            View view = DcModules.get(getModule()).getSearchView().getCurrent();
            if (isShowing())
                view.initProgressBar(items.size());
            
            int counter = 0;
            for (DcObject dco : items) {

                // thread safe
                if (isShowing()) {
                    view.setMaxForProgressBar(items.size());
                    view.updateProgressBar(counter++);
                    view.setStatus(DcResources.getText("msgAddingXToTree", dco.toString()));
                }
                
                if (canceled) break;
                
                String filename = dco.getFilename();
                if (Utilities.isEmpty(filename)) continue;
                
                StringTokenizer st = new StringTokenizer(filename, (filename.indexOf("/") > -1 ? "/" : "\\"));
                
                List<String> parts = new ArrayList<String>();
                while (st.hasMoreElements())
                    parts.add((String) st.nextElement());
                
                ArrayList<DefaultMutableTreeNode> nodes = findNode(parts, top);
                if (nodes.size() != parts.size()) {
                    for (int i = nodes.size(); i < parts.size(); i++) {
                        final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
                        
                        NodeElement element = new NodeElement(getModule(), parts.get(i), null);
                        node.setUserObject(element);
                        
                        final DefaultMutableTreeNode parent = i == 0 ? top : nodes.get(i - 1);
                        
                        if (parts.size() - 1 == i) 
                            element.addValue(dco);
                        
                        nodes.add(node);
                        
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    insertNode(node, parent);
                                };
                            });
                        } catch (Exception e) {
                            logger.error(e, e);
                        }                    
                        
                        try {
                            sleep(5);
                        } catch (Exception ignore) {}
                    }
                } else if (nodes.size() > 0) {
                    ((NodeElement) nodes.get(nodes.size() - 1).getUserObject()).addValue(dco);
                }
            }
            
            
            if (isShowing()) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            expandAll();
                            setDefaultSelection();
                        };
                    });
                } catch (Exception e) {
                    logger.error(e, e);
                }    
            }
            
            setListeningForSelection(true);
            setSaveChanges(true);

            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        tree.setEnabled(true);
                        setDefaultSelection();
                        
                        revalidate();
                        repaint();
                    };
                });
            } catch (Exception e) {
                logger.error(e, e);
            }  
        }
    }
}
