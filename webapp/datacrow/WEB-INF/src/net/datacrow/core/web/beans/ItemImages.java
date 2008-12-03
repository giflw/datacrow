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

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.web.model.DcWebField;
import net.datacrow.core.web.model.DcWebImage;
import net.datacrow.core.web.model.DcWebImages;
import net.datacrow.core.web.model.DcWebObject;

import org.apache.myfaces.custom.navmenu.NavigationMenuItem;

public class ItemImages extends DcBean {
    
    @Override
    public String back() {
        return "search";
    }
    
    @Override
    public String current() {
        return "itemimages";
    }
    
    @Override
    public List<NavigationMenuItem> getMenuItems() {
        List<NavigationMenuItem> menu = new ArrayList<NavigationMenuItem>();
        menu.add(getMenuItem("Back", "#{itemImages.back}", null));
        addLogoffMenuItem(menu);
        return menu;
    }
    
    public String open() {
        loadImages();
        return "itemimages";
    }
    
    @SuppressWarnings("unchecked")
    public String setCurrent() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebImages images = (DcWebImages) vr.resolveVariable(fc, "images");

        Map map = fc.getExternalContext().getRequestParameterMap();
        images.setCurrent(Integer.valueOf((String) map.get("fieldIdx")));
        
        return "itemimages";
    }
    
    private void loadImages() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebObject wo = (DcWebObject) vr.resolveVariable(fc, "webObject");
        DcWebImages images = (DcWebImages) vr.resolveVariable(fc, "images");

        images.clear();
        for (DcWebField field : wo.getFields()) {
            if (field.getDcField().getValueType() == DcRepository.ValueTypes._PICTURE) {
                Picture picture = (Picture) wo.getDcObject().getValue(field.getIndex());
                if (picture != null) {
                    picture.loadImage();
                    if (picture.getValue(Picture._D_IMAGE) != null) {
                        DcWebImage wi = new DcWebImage();
                        wi.setFieldIdx(field.getIndex());
                        wi.setModuleIdx(field.getDcModule().getIndex());
                        wi.setPicture(picture);
                        
                        if (images.getCurrent() == null)
                            images.setCurrent(field.getIndex());
                        
                        images.add(wi);
                    }
                }
            }
        }
    }
    
    @Override
    public String getActionListener() {
        return "#{itemImages.actionListener}";
    }    
}
