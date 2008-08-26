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

package net.datacrow.core.web.beans;

import java.util.List;

import javax.faces.event.ActionEvent;

import net.datacrow.core.web.DcSecured;

import org.apache.myfaces.custom.navmenu.NavigationMenuItem;
import org.apache.myfaces.custom.navmenu.jscookmenu.HtmlCommandJSCookMenu;

public abstract class DcBean extends DcSecured {
    
    public abstract String current();
    public abstract String back();
    
    public abstract List<NavigationMenuItem> getMenuItems();
    public abstract String getActionListener();
    
    protected void addLogoffMenuItem(List<NavigationMenuItem> menu) {
        NavigationMenuItem user = getMenuItem("User", null, null);
        user.add(getMenuItem("Logoff", "#{security.logoff}", "logoff.png"));
        menu.add(user);
    }

    protected NavigationMenuItem getMenuItem(String label, String action, String icon) {
        NavigationMenuItem item = new NavigationMenuItem(label, action);
        item.setActionListener(getActionListener());
        item.setValue(label);
        
        if (icon != null)
            item.setIcon("images/" + icon);
        
        return item;
    }
    
    public String actionListener(ActionEvent event) {
        return (String) ((HtmlCommandJSCookMenu) event.getComponent()).getValue();
    }
}
