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

package net.datacrow.console.components.fstree;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcTree;
import net.datacrow.console.components.renderers.ComboBoxFsRenderer;
import net.datacrow.util.FileNameFilter;
import net.datacrow.util.Utilities;

public abstract class FileSystemTreePanel extends JPanel implements ActionListener {
    
    private Map<File, JScrollPane> scrollers = new HashMap<File, JScrollPane>();
    private Map<File, FileSystemTreeModel> models = new HashMap<File, FileSystemTreeModel>();
    private FileNameFilter filter;
    
    public FileSystemTreePanel(FileNameFilter filter) {
        setFilter(filter);
    }
    
    protected void setFilter(FileNameFilter filter) {
        if (scrollers.size() > 0) {
            scrollers.clear();
            models.clear();
            removeAll();
            revalidate();
            repaint();
        }
        
        this.filter = filter;
        build();
    }
    
    private void setSelectedDrive(File drv) {
        for (JScrollPane scroller : scrollers.values())
            scroller.setVisible(false);

        scrollers.get(drv).setVisible(true);
        
        revalidate();
        repaint();
    }
    
    protected abstract JMenuBar getMenu();
    
    public Collection<String> getFiles(boolean includeDirs) {
        Collection<String> selected = new ArrayList<String>();
        for (FileSystemTreeModel model : models.values()) {
            FileSystemTreeNode parent = (FileSystemTreeNode) model.getRoot();
            addSelectedNodes(parent, selected);
        }
        
        // cleanup (remove selected directories where files have been selected from)
        for (String s : new ArrayList<String>(selected)) {
            File file = new File(s);
            String path = file.getParent();
            if (!includeDirs && file.isDirectory())
                selected.remove(s);
            if (includeDirs && file.isFile() && selected.contains(path))
                selected.remove(path);
        }
        
        return selected;
    }

    @SuppressWarnings("unchecked")
    public void addSelectedNodes(FileSystemTreeNode node, Collection<String> selected) {
        for (Enumeration e = node.children(); e.hasMoreElements(); ) {
            FileSystemTreeNode child = (FileSystemTreeNode) e.nextElement();
            if (child.isSelected())
                selected.add(child.getText());
            addSelectedNodes(child, selected);
        }
    }
    
    protected void build() {
        setLayout(Layout.getGBL());
        
        JMenuBar menu = getMenu();
        if (menu != null) {
            add(menu, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                     ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                      new Insets(0, 0, 0, 0), 0, 0));
        }
        
        JComboBox cbDrives = ComponentFactory.getComboBox();
        cbDrives.setRenderer(ComboBoxFsRenderer.getInstance());
        add(cbDrives, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(15, 5, 5, 5), 0, 0));     
        
        for (File drv : Utilities.getDrives()) {
            DcTree tree = new DcTree(new FileSystemTreeModel(drv, filter));
            
            tree.setCellRenderer(new FileSystemTreeNodeRenderer());
            tree.setCellEditor(new FileSystemTreeNodeEditor());
            tree.setEditable(true);
            
            JScrollPane scroller = new JScrollPane(tree);
            scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            add(scroller, Layout.getGBC( 0, 2, 1, 1, 20.0, 20.0
                        ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                         new Insets(5, 5, 5, 5), 0, 0));      
            
            scroller.setVisible(false);
            scrollers.put(drv, scroller);
            models.put(drv, (FileSystemTreeModel) tree.getModel());
            cbDrives.addItem(drv);
        }
        
        cbDrives.setActionCommand("drvChanged");
        cbDrives.addActionListener(this);
        cbDrives.setSelectedIndex(0);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("drvChanged")) {
            JComboBox cb = (JComboBox) e.getSource();
            setSelectedDrive((File) cb.getSelectedItem());
        }
    }
    
    public void clear() {
        scrollers.clear();
        scrollers = null;
        models.clear();
        models = null;
    }
}
