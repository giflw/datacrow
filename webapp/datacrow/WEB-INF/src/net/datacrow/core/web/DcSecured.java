package net.datacrow.core.web;


import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import net.datacrow.core.security.SecuredUser;
import net.datacrow.core.web.beans.Security;

public class DcSecured {
    
    protected boolean isLoggedIn() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        Security security = (Security) vr.resolveVariable(fc, "security");
        return security.isLoggedIn();
    }
    
    public SecuredUser getUser() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        Security security = (Security) vr.resolveVariable(fc, "security");
        return security.getUser();
    }
    
    protected String redirect() {
        return "login";
    }
}
