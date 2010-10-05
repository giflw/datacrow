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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.web.model.DcWebImage;
import net.datacrow.core.web.model.DcWebObject;
import net.datacrow.core.web.model.DcWebObjects;
import net.datacrow.util.DcImageIcon;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.myfaces.custom.navmenu.NavigationMenuItem;

public class ItemImage extends DcBean {
    
    private UploadedFile uploadedFile;
    
    @Override
    public String back() {
        return "details";
    }
    
    @Override
    public String current() {
        return "itemimage";
    }
    
    @Override
    public List<NavigationMenuItem> getMenuItems() {
        List<NavigationMenuItem> menu = new ArrayList<NavigationMenuItem>();
        
        menu.add(getMenuItem(DcResources.getText("lblBack"), "#{itemImage.back}", null));
        
        addLogoffMenuItem(menu);
        
        return menu;
    }
    
    @Override
    public String getActionListener() {
        return "#{itemImage.actionListener}";
    }
    
    public String open() {
        
        if (!isLoggedIn())
            return redirect();
        
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        
        DcWebObject wo = (DcWebObject) vr.resolveVariable(fc, "webObject");
        Map map = fc.getExternalContext().getRequestParameterMap();

        int fieldIdx = Integer.valueOf((String) map.get("fieldIdx"));
        Picture picture = (Picture) wo.getDcObject().getValue(fieldIdx);
        DcWebImage wi = (DcWebImage) vr.resolveVariable(fc, "image");
        wi.setFieldIdx(fieldIdx);
        wi.setModuleIdx(wo.getModule());

        if (picture != null) {
            wi.setPicture(picture);
        } else {
            wi.setPicture(null);
        }
        
        return "itemimage";
    }
    
    public boolean isAllowUpload() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebImage wi = (DcWebImage) vr.resolveVariable(fc, "image");
        DcWebObject wo = (DcWebObject) vr.resolveVariable(fc, "webObject");
        return getUser().isEditingAllowed(DcModules.get(wo.getModule()).getField(wi.getFieldIdx()));
    }

    public UploadedFile getUpFile() {
        return uploadedFile;
    }

    public void setUpFile(UploadedFile upFile) {
        uploadedFile = upFile;
    }

    @SuppressWarnings("unchecked")
    public String upload() throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        
        DcWebImage wi = (DcWebImage) vr.resolveVariable(fc, "image");
        byte[] b = uploadedFile.getBytes();
        
        DcWebObject wo = (DcWebObject) vr.resolveVariable(fc, "webObject");
        DcObject dco = wo.getDcObject();
        dco.setValue(wi.getFieldIdx(), new DcImageIcon(b));
        
        try {
            dco.saveUpdate(false);
            wi.setPicture((Picture) dco.getValue(wi.getFieldIdx()));
            wo.load();
        
            DcWebObjects objects = (DcWebObjects) vr.resolveVariable(fc, "webObjects");
            objects.update(wo);
            
        } catch (ValidationException ve) {
            ve.printStackTrace();
        }
        
        fc.getExternalContext().getApplicationMap().put("fileupload_bytes", uploadedFile.getBytes());
        fc.getExternalContext().getApplicationMap().put("fileupload_type", uploadedFile.getContentType());
        fc.getExternalContext().getApplicationMap().put("fileupload_name", uploadedFile.getName());
        
        return current();
    }

    public boolean isUploaded() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return facesContext.getExternalContext().getApplicationMap().get("fileupload_bytes")!= null;
    }    
}
