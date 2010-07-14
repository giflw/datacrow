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
import net.datacrow.core.web.model.DcWebObjects;

public class ItemDetails extends ItemBean {
    
    @Override
    public String back() {
        return "search";
    }
    
    @Override
    public String current() {
        return "details";
    }

    @Override
    protected DcWebObject getItem() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        return (DcWebObject) vr.resolveVariable(fc, "webObject");
    }
    
    @Override
    public String getActionListener() {
        return "#{itemDetails.actionListener}";
    }
    
    @SuppressWarnings("unchecked")
    public String open() {

        if (!isLoggedIn())
            return redirect();
        
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebObjects objects = (DcWebObjects) vr.resolveVariable(fc, "webObjects");
        List<?> data = (List) objects.getData().getRowData();
        
        DcWebObject wod = (DcWebObject) vr.resolveVariable(fc, "webObject");
        
        int moduleIdx = objects.getModule();

        if (!getUser().isAuthorized(DcModules.get(moduleIdx)))
            return redirect();

        wod.initialize(moduleIdx);
        wod.setRowIdx(objects.getData().getRowIndex());
        wod.setID((Long) data.get(data.size() - 1));
        wod.setName(wod.getDcObject().toString());
        wod.load();
        
        return current();
    }
    
    public String create() {
        if (!isLoggedIn())
            return redirect();
        
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebObjects objects = (DcWebObjects) vr.resolveVariable(fc, "webObjects");
        
        int moduleIdx = objects.getModule();
        
        if (!getUser().isAuthorized(DcModules.get(moduleIdx)))
            return redirect();
        
        DcWebObject wod = (DcWebObject) vr.resolveVariable(fc, "webObject");
        wod.initialize(moduleIdx);
        wod.setNew(true);
        
        return current();
    }
    
    public String save() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebObject wod = (DcWebObject) vr.resolveVariable(fc, "webObject");
        return save(wod);
    }  
    
    @Override
    protected String afterUpdated() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();

        DcWebObjects objects = (DcWebObjects) vr.resolveVariable(fc, "webObjects");
        DcWebObject wo = (DcWebObject) vr.resolveVariable(fc, "webObject");

        objects.update(wo);
        return back();
    }

    @Override
    protected String afterCreate() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();

        DcWebObjects objects = (DcWebObjects) vr.resolveVariable(fc, "webObjects");
        DcWebObject wo = (DcWebObject) vr.resolveVariable(fc, "webObject");

        objects.add(wo);
        return current();
    }
}
