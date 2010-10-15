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

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;


/**
 * The file tree panel shows the file paths and file names of the current
 * items. Additionally it shows whether the file or directory exists by checking the file system.
 * @see {@link FileNodeElement}
 * 
 * @author Robert Jan van der Waals
 */
public class FileTreePanel extends TreePanel {
    
    private static Logger logger = Logger.getLogger(FileTreePanel.class.getName());
    
    private TreeHugger treeHugger;
    
    public FileTreePanel(GroupingPane gp) {
        super(gp);
    }

    @Override
    public void groupBy() {}
    
    @Override
    public String getName() {
        return DcResources.getText("lblFileStructure");
    }
    
    @Override
    public DcDefaultMutableTreeNode getFullPath(DcObject dco) {
        DcDefaultMutableTreeNode node = new DcDefaultMutableTreeNode(DcResources.getText("lblFileTreeSystem"));
        String filename = dco.getFilename();
        if (filename != null) {
	        StringTokenizer st = new StringTokenizer(filename, (filename.indexOf("/") > -1 ? "/" : "\\"));
	        while (st.hasMoreElements()) {
	        	node.add(new DcDefaultMutableTreeNode(new FileNodeElement((String) st.nextElement(), new File(filename))));
	        }
        }   
        return node;
    }
    
    /************************************************************************
     * Initialization
     ************************************************************************/
    
    @Override
    protected void createTree() {
        if (treeHugger != null) {
            treeHugger.cancel();
            while (treeHugger.isAlive()) {} // allow it to hang until the thread ends..
        }
        
        treeHugger = new TreeHugger();
        treeHugger.start();
    }
    
    @Override
    protected void createTopNode() {
        top = new DcDefaultMutableTreeNode(DcResources.getText("lblFileTreeSystem"));
        FileNodeElement element = new FileNodeElement(DcResources.getText("lblFileTreeSystem"), null);
        top.setUserObject(element);
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
            build();
            
            DcModule m = DcModules.get(getModule());
            StringBuffer sql = new StringBuffer("");
            Collection<DcModule> modules = new ArrayList<DcModule>();
            
            if (m.isAbstract()) {
            	for (DcModule module : DcModules.getAllModules()) {
            		if (module.getType() == m.getType() && !module.isAbstract())
            			modules.add(module);
            	}
            } else {
            	modules.add(m);
            }
            
            int moduleCounter = 0;
            for (DcModule module : modules) {
            	if (moduleCounter > 0)
            		sql.append(" UNION ");
            	
            	sql.append("SELECT ID, ");
            	sql.append(module.getFileField().getDatabaseFieldName());
            	sql.append(" AS FILENAME FROM ");
            	sql.append(module.getTableName());
            	sql.append(" WHERE ");
            	sql.append(module.getFileField().getDatabaseFieldName());
            	sql.append(" IS NOT NULL ");
            }
            
            if (m.isAbstract()) {
            	sql.insert(0, "select ID, FILENAME from (");
            	sql.append(") ");
        	}

            sql.append(" ORDER BY FILENAME");
            createTree(sql.toString());
            
            SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        @Override
                        public void run() {
                            expandAll();
                        }
                    }));
        }
        
        /**
         * Creates a tree from the result of an SQL statement. 
         * @param sql
         */
        private void createTree(String sql) {
            try {
    			logger.debug(sql);
                
                ResultSet rs = DatabaseManager.executeSQL(sql);
                
                NodeElement existingNe;
                NodeElement ne;
                String id = null;
                String key = null;
                String filename = null;

                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                
                DefaultMutableTreeNode current;
                DefaultMutableTreeNode parent;
                DefaultMutableTreeNode previous;
                boolean exists = false;
                
                while (rs.next() && !stop) {
                    int level = 0;
                    parent = top;
                    
                    id = rs.getString(1);
                    filename = rs.getString(2);
                    
                    StringTokenizer st = new StringTokenizer(filename, (filename.indexOf("/") > -1 ? "/" : "\\"));
                    while (st.hasMoreElements()) {
                    	key = (String) st.nextElement();
                    
                        if (stop) break;
                        
                        // check the whole last leaf
                        top.getLastLeaf();
                        
                        previous = parent.getChildCount() == 0 ? null : ((DefaultMutableTreeNode) parent.getChildAt(parent.getChildCount() - 1));
                        exists = previous != null && ((NodeElement)  previous.getUserObject()).getComparableKey().equals(key.toLowerCase());
                        
                        if (!exists) { 
                            ne = new FileNodeElement(key, new File(filename));
                            
                            ne.addItem(id);
                            current = new DcDefaultMutableTreeNode(ne);
                            model.insertNodeInto(current, parent, parent.getChildCount());
                            parent = current;
                           
                        } else { // exists
                            existingNe =(NodeElement) previous.getUserObject();
                            existingNe.addItem(id);
                            parent = previous;    
                        }
                        level++;
                	}
                }
                
            	rs.close();
                
            } catch (Exception e) {
                logger.error(e, e);
            }
            
            sort();
        }
    }

    @Override
    protected JMenuBar getMenu() {
        return null;
    }
}
