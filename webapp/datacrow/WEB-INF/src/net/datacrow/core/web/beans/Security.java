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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import net.datacrow.core.security.SecuredUser;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.core.web.model.DcWebModules;
import net.datacrow.core.web.model.DcWebObjects;
import net.datacrow.core.web.model.DcWebUser;
import net.datacrow.core.security.SecurityException;

public class Security {

    public String login() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebUser wu = (DcWebUser) vr.resolveVariable(fc, "user");
        
        try {
            SecuredUser su = SecurityCentre.getInstance().login(wu.getUsername(), wu.getPassword(), true);
            wu.setSecuredUser(su);
            DcWebModules modules = (DcWebModules) vr.resolveVariable(fc, "modules");
            modules.load();
        } catch (SecurityException se) {
            fc.addMessage("loginError", new FacesMessage(se.getMessage()));
            return "login";
        }
        
        return "search";
    }
    
    public String getUsername() {
        return getUser() != null ? getUser().getUser().toString() : "";
    }
    
    public SecuredUser getUser() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebUser wu = (DcWebUser) vr.resolveVariable(fc, "user");
        return wu != null ? wu.getSecuredUser() : null; 
    }
    
    public boolean isLoggedIn() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebUser wu = (DcWebUser) vr.resolveVariable(fc, "user");
        return wu != null && SecurityCentre.getInstance().isLoggedIn(wu.getSecuredUser());
    }
    
    public String logoff() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebUser wu = (DcWebUser) vr.resolveVariable(fc, "user");
        SecurityCentre.getInstance().logoff(wu.getSecuredUser().getUser());
        wu.setPassword(null);
        wu.setUsername(null);
        wu.setSecuredUser(null);
        
        DcWebModules modules = (DcWebModules) vr.resolveVariable(fc, "modules");
        modules.load();
        
        DcWebObjects objects = (DcWebObjects) vr.resolveVariable(fc, "webObjects");
        objects.setModule(0);
        
        return "login";
    }
}
