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

package net.datacrow.console.components.panels;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListModel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcLabel;
import net.datacrow.console.components.DcPanel;
import net.datacrow.console.components.lists.DcModuleList;
import net.datacrow.core.resources.DcResources;

public class ModuleListPanel extends DcPanel {
    
    private JPanel panelHeader = new JPanel();
    private DcModuleList moduleList = new DcModuleList();
    private JLabel label = new DcLabel(DcResources.getText("lblModules"));

    public ModuleListPanel() {
        super(null, null);
    	buildPanel();
    }
    
    @Override
    public void setFont(Font font) {
        if (moduleList != null) {
            moduleList.setFont(font);
            label.setFont(ComponentFactory.getSystemFont());
        }
    }
    
    public void setSelectedModule(int index) {
    	moduleList.setSelectedModule(index);
    }
    
    public void rebuild() {
        ListModel model = moduleList.getModel();
        if (model instanceof DefaultListModel) {
            ((DefaultListModel) model).clear();    
        } else {
            moduleList.setModel(new DefaultListModel());
        }
        
        moduleList.addModules();
    }
    
    private void buildPanel() {
        setLayout(new BorderLayout());

        label.setFont(ComponentFactory.getSystemFont());
        label.setHorizontalAlignment(JLabel.CENTER);
        panelHeader.setLayout(Layout.getGBL());
        panelHeader.setBorder(BorderFactory.createEtchedBorder());
        panelHeader.add(label, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        
        add(panelHeader, BorderLayout.NORTH);
        add(moduleList, BorderLayout.CENTER);
        ComponentFactory.setBorder(this);
    }
}
