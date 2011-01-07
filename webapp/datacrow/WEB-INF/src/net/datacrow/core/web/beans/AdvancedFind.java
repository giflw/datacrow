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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.web.model.AdvancedFilter;
import net.datacrow.core.web.model.DcWebObjects;
import net.datacrow.util.Utilities;

import org.apache.myfaces.custom.navmenu.NavigationMenuItem;

public class AdvancedFind extends DcBean {
    
    @Override
    public String back() {
        return "search";
    }

    @Override
    public String current() {
        return "advancedfind";
    }

    @Override
    public List<NavigationMenuItem> getMenuItems() {
        List<NavigationMenuItem> menu = new ArrayList<NavigationMenuItem>();
        
        menu.add(getMenuItem(DcResources.getText("lblBack"), "#{advancedFind.back}", null));
        
        addLogoffMenuItem(menu);
        
        return menu;
    }

    @Override
    public String getActionListener() {
        return "#{advancedFind.actionListener}";
    }

    public String open() {
        if (!isLoggedIn())
            return redirect();
        
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        
        DcWebObjects objects = (DcWebObjects) vr.resolveVariable(fc, "webObjects");
        AdvancedFilter af = (AdvancedFilter) vr.resolveVariable(fc, "advancedFilter");

        af.initialize(objects.getModule());
        
        return current();
    }
    
    public String addEntry() {
        
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        AdvancedFilter af = (AdvancedFilter) vr.resolveVariable(fc, "advancedFilter");
        
        DataFilterEntry entry = af.getEntry();
        if (entry.getOperator() == null) {
            fc.addMessage("msg", new FacesMessage("Operator is not filled!"));
        } else if (entry.getOperator().needsValue() && Utilities.isEmpty(entry.getValue())) {
            fc.addMessage("msg", new FacesMessage("Value is not filled!"));
        } else {
            af.addCurrentEntry();
        }
        
        return current();
    }

    public String deleteEntry() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        AdvancedFilter af = (AdvancedFilter) vr.resolveVariable(fc, "advancedFilter");
        Map map = fc.getExternalContext().getRequestParameterMap();
        af.deleteEntry(Integer.parseInt((String) map.get("index")));
        return current();
    }

    public String editEntry() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        AdvancedFilter af = (AdvancedFilter) vr.resolveVariable(fc, "advancedFilter");
        Map map = fc.getExternalContext().getRequestParameterMap();
        af.editEntry(Integer.parseInt((String) map.get("index")));
        return current();
    }
    
    public String search() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        ItemSearch is = (ItemSearch) vr.resolveVariable(fc, "itemSearch");
        return is.search(true);
    }
}
