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
import java.util.Collection;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.web.model.DcWebField;
import net.datacrow.core.web.model.DcWebObject;
import net.datacrow.util.Utilities;

import org.apache.myfaces.custom.navmenu.NavigationMenuItem;

public abstract class ItemBean extends DcBean {

    protected abstract String afterCreate();
    protected abstract String afterUpdated();
    protected abstract DcWebObject getItem();
    
    @Override
    public List<NavigationMenuItem> getMenuItems() {
        List<NavigationMenuItem> menu = new ArrayList<NavigationMenuItem>();
        
        DcWebObject wo = getItem();
        String itemBeanName =  wo.isChild() ? "webChildObject" : "webObject";
        
        menu.add(getMenuItem("Back", "#{" + (wo.isChild() ? "childDetails" : "itemDetails") + ".back}", null));
        
        // SHOW MENU
        NavigationMenuItem show = getMenuItem("Show", null, null);
        
        if (wo.isInformationTabVisible())
            show.add(getMenuItem("Information", "#{" + itemBeanName + ".switchToInfoTab}", "information.png"));
        
        if (wo.isTechnicalTabVisible())
            show.add(getMenuItem("Technical Information", "#{" + itemBeanName + ".switchToTechTab}", "informationtechnical.png"));

        if (wo.isChildrenTabVisible())
            show.add(getMenuItem(wo.getChildrenLabel(), "#{" + itemBeanName + ".switchToChildTab}", "modules/" + DcModules.get(wo.getModule()).getChild().getName() + "16.png"));

        menu.add(show);
        
        // PICTURE MENU
        if (wo.isPictureTabVisible()) {
            NavigationMenuItem pics = getMenuItem("Pictures", null, null);
            pics.add(getMenuItem("View", "#{itemDetailsImages.open}" ,"picture.png"));
            //if (getUser().isEditingAllowed(DcModules.get(wo.getModule())))
              //  pics.add(getMenuItem("Edit", "#{" + itemBeanName + ".switchToPicTab}", "picture.png"));
            menu.add(pics);
        }
        
        
        // EDIT MENU
        if (getUser().isEditingAllowed(DcModules.get(wo.getModule()))) {
            NavigationMenuItem edit = getMenuItem("Edit", null, null);
            edit.add(getMenuItem("Save", "#{" + (wo.isChild() ? "childDetails" : "itemDetails") + ".save}", "save.png"));
            
            
            menu.add(edit);
        }
        
        addLogoffMenuItem(menu);
        
        return menu;
    }

    protected String save(DcWebObject wo) {

        if (!isLoggedIn())
            return redirect();
        
        if (!isValid(wo))
            return current();
        
        DcObject dco = wo.isNew() ? DcModules.get(wo.getModule()).getDcObject() :
                       DataManager.getObject(wo.getModule(), wo.getID()).clone();
        
        for (DcWebField wf : wo.getFields()) {
            if (!wf.isMultiRelate())
                dco.setValue(wf.getIndex(), wf.getValue());
        }

        for (DcWebField wf : wo.getTechnicalFields()) {
            if (!wf.isMultiRelate())
                dco.setValue(wf.getIndex(), wf.getValue());
        }
        
        if (wo.isChild()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            VariableResolver vr = fc.getApplication().getVariableResolver();
            DcWebObject wod = (DcWebObject) vr.resolveVariable(fc, "webObject");
            dco.setValue( dco.getParentReferenceFieldIndex(), wod.getID());
        }
        
        try {
            dco.setSilent(true);
            
            if (wo.isNew()) {
                dco.setIDs();
                dco.saveNew(false);
            } else { 
                dco.saveUpdate(false);
            }
        } catch (ValidationException ve) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("saveError", new FacesMessage(ve.getMessage()));
            return current();
        }
        
        if (wo.isNew()) {
            wo.initialize(wo.getModule());
            wo.setRowIdx(0);
            wo.setID(dco.getID());
            wo.setName(dco.toString());
            wo.load();
            
            return afterCreate();
        } else { 
            return afterUpdated();
        } 
    }
    
    protected boolean isValid(DcWebObject wo) { 
        FacesContext fc = FacesContext.getCurrentInstance();
        Collection<DcWebField> fields = new ArrayList<DcWebField>(); 
        fields.addAll(wo.getFields());
        fields.addAll(wo.getTechnicalFields());
        
        boolean valid = true;
        int msg = 0;
        for (DcField field :  DcModules.get(wo.getModule()).getFields()) {
            for (DcWebField wf : fields) {
                
                if (wf.getIndex() == field.getIndex()) {
                    
                    if (Utilities.isEmpty(wf.getValue()) && field.isRequired()) {
                        valid = false;
                        fc.addMessage("msg" + msg++, new FacesMessage("Required field " + field.getLabel() + " has not been filled."));
                        continue;

                    } else if (!Utilities.isEmpty(wf.getValue()) && 
                               (field.getValueType() == DcRepository.ValueTypes._BIGINTEGER ||
                                field.getValueType() == DcRepository.ValueTypes._LONG)) {
                        try {
                            Long.valueOf(String.valueOf(wf.getValue()));
                        } catch (NumberFormatException nfe) {
                            valid = false;
                            fc.addMessage("msg" + msg++, new FacesMessage(field.getLabel() + " should contain a numeric value."));
                            continue;
                        }
                    }
                }
            }
        }
        return valid;
    }

}
