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

package net.datacrow.console.menu;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcMenu;
import net.datacrow.console.components.DcMenuBar;
import net.datacrow.console.components.DcMenuItem;
import net.datacrow.console.components.panels.tree.FieldTreePanel;
import net.datacrow.console.windows.GroupByDialog;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

public class FieldTreePanelMenuBar extends DcMenuBar implements ActionListener {
    
    protected final int modIdx;
    protected final FieldTreePanel treePanel;
    
    private DcMenu menuFields = ComponentFactory.getMenu(DcResources.getText("lblGroupBy"));

    public FieldTreePanelMenuBar(int modIdx, FieldTreePanel treePanel) {
        this.modIdx = modIdx;
        this.treePanel = treePanel;

        DcMenu menuEdit = ComponentFactory.getMenu(DcResources.getText("lblEdit"));
        DcMenuItem menuExpandAll = ComponentFactory.getMenuItem(DcResources.getText("lblExpandAll"));
        DcMenuItem menuCollapseAll = ComponentFactory.getMenuItem(DcResources.getText("lblCollapseAll"));
        
        menuCollapseAll.addActionListener(this);
        menuCollapseAll.setActionCommand("collapseAll");
        
        menuExpandAll.addActionListener(this);
        menuExpandAll.setActionCommand("expandAll");
        
        menuEdit.add(menuCollapseAll);
        menuEdit.add(menuExpandAll);
        
        
        DcMenuItem miGroupBy = ComponentFactory.getMenuItem(DcResources.getText("msgSelectFields"));
        miGroupBy.setActionCommand("groupBy");
        miGroupBy.addActionListener(this);
        menuFields.add(miGroupBy);
        
        setPreferredSize(new Dimension(100, 22));
        setMaximumSize(new Dimension(100, 22));
        setMinimumSize(new Dimension(50, 22));
        
        add(menuFields);
        add(menuEdit);
    }
    
    protected void hideGroupBy() {
        menuFields.setVisible(false);
    }
    
    private void groupBy(String s) {
        GroupByDialog dlg = new GroupByDialog(modIdx);
        dlg.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("collapseAll")) {
            treePanel.collapseAll();
        } else if (ae.getActionCommand().equals("expandAll")) {
            treePanel.expandAll();
        } else {
            groupBy(ae.getActionCommand());
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(DcSwingUtilities.setRenderingHint(g));
    }    
}

