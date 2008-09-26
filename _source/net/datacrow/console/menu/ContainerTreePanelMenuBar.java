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

import java.awt.event.ActionEvent;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcMenu;
import net.datacrow.console.components.DcMenuItem;
import net.datacrow.console.components.panels.tree.FieldTreePanel;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.Settings;

public class ContainerTreePanelMenuBar extends FieldTreePanelMenuBar {

    public ContainerTreePanelMenuBar(int modIdx, FieldTreePanel treePanel) {
        super(modIdx, treePanel);
        
        DcMenu menuView = ComponentFactory.getMenu(DcResources.getText("lblView"));
        
        DcMenuItem menuViewContainers = ComponentFactory.getMenuItem(DcResources.getText("lblViewContainers"));
        DcMenuItem menuViewItems = ComponentFactory.getMenuItem(DcResources.getText("lblViewItems"));
        
        menuView.add(menuViewContainers);
        menuView.add(menuViewItems);
        
        menuViewContainers.setActionCommand("viewContainers");
        menuViewItems.setActionCommand("viewItems");
        
        menuViewContainers.addActionListener(this);
        menuViewItems.addActionListener(this);
        
        hideGroupBy();
        
        add(menuView);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Settings settings = DcModules.get(DcModules._CONTAINER).getSettings();
        if (ae.getActionCommand().equals("viewContainers")) {
            settings.set(DcRepository.ModuleSettings.stTreePanelShownItems, Long.valueOf(DcModules._CONTAINER));
            DcModules.get(DcModules._CONTAINER).getSearchView().applySettings();
            
        } else if (ae.getActionCommand().equals("viewItems")) {
            settings.set(DcRepository.ModuleSettings.stTreePanelShownItems, Long.valueOf(DcModules._ITEM));
            DcModules.get(DcModules._CONTAINER).getSearchView().applySettings();
        } else {
            super.actionPerformed(ae);
        }
    }
}
