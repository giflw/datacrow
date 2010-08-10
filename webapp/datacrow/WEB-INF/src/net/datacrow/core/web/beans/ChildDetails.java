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

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.web.model.DcWebObject;

public class ChildDetails extends ItemBean {

    @Override
    public String back() {
        return "details";
    }

    @Override
    public String current() {
        return "childdetails";
    }

    @Override
    protected DcWebObject getItem() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        return (DcWebObject) vr.resolveVariable(fc, "webChildObject");
    }

    @Override
    public String getActionListener() {
        return "#{childDetails.actionListener}";
    }

    @Override
    protected String afterCreate() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebObject wo = (DcWebObject) vr.resolveVariable(fc, "webObject");
        wo.loadChildren();
        
        return back();
    }

    @Override
    protected String afterUpdated() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebObject wo = (DcWebObject) vr.resolveVariable(fc, "webObject");
        wo.loadChildren();
        
        return back();
    }

    @SuppressWarnings("unchecked")
    public String open() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebObject wo = (DcWebObject) vr.resolveVariable(fc, "webObject");
        List<Object> row = (List<Object>) wo.getChildren().getRowData();
        int moduleIdx = DcModules.get(wo.getModule()).getChild().getIndex(); 
        
        DcWebObject child = (DcWebObject) vr.resolveVariable(fc, "webChildObject");
        
        if (!getUser().isAuthorized(DcModules.get(moduleIdx)))
            return redirect();

        child.initialize(moduleIdx);
        child.setID((String) row.get(row.size() - 1));
        child.setName(child.getDcObject().toString());
        child.load();
        child.setChild(true);
        
        return current();
    }
    
    public String create() {
        if (!isLoggedIn())
            return redirect();
        
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebObject wo = (DcWebObject) vr.resolveVariable(fc, "webObject");
        
        int moduleIdx = wo.getModule();
        
        if (!getUser().isAuthorized(DcModules.get(moduleIdx)))
            return redirect();
        
        DcWebObject child = (DcWebObject) vr.resolveVariable(fc, "webChildObject");
        child.initialize(DcModules.get(moduleIdx).getChild().getIndex());
        child.setNew(true);
        
        return current();
    }

    public String save() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebObject wod = (DcWebObject) vr.resolveVariable(fc, "webChildObject");
        return save(wod);
    }
}
