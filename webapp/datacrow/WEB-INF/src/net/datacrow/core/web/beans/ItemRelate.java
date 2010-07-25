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
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;
import javax.faces.model.SelectItem;

import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.web.model.DcReferences;
import net.datacrow.core.web.model.DcWebObject;

import org.apache.myfaces.custom.navmenu.NavigationMenuItem;

public class ItemRelate extends DcBean {
    
    private boolean isChild;
    
    @Override
    public String back() {
        return isChild ? "childdetails" : "details";
    }
    
    @Override
    public String current() {
        return "itemrelate";
    }
    
    @Override
    public String getActionListener() {
        return "#{itemRelate.actionListener}";
    }
    
    @Override
    public List<NavigationMenuItem> getMenuItems() {
        List<NavigationMenuItem> menu = new ArrayList<NavigationMenuItem>();
        
        menu.add(getMenuItem(DcResources.getText("lblBack"), "#{itemRelate.back}", null));
        
        // EDIT MENU
        NavigationMenuItem edit = getMenuItem(DcResources.getText("lblEdit"), null, null);
        edit.add(getMenuItem(DcResources.getText("lblSave"), "#{itemRelate.save}", "save.png"));
        menu.add(edit);
        
        addLogoffMenuItem(menu);
        
        return menu;
    }

    private DcWebObject getParentObject() {
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();

        return isChild ? 
               (DcWebObject) vr.resolveVariable(fc, "webChildObject"):
               (DcWebObject) vr.resolveVariable(fc, "webObject");
    }
    
    public String open() {

        if (!isLoggedIn())
            return redirect();
        
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        
        Map map = fc.getExternalContext().getRequestParameterMap();
        int fieldIdx = Integer.valueOf((String) map.get("fieldIdx"));
        isChild = Boolean.valueOf((String) map.get("isChild"));
        
        DcWebObject wod = getParentObject();
        DcReferences references = (DcReferences) vr.resolveVariable(fc, "references");

        references.setFieldIdx(fieldIdx);
        references.setModuleIdx(wod.getModule());

        setCurrentReferences(wod, references, fieldIdx);
        setListItems(wod, references, fieldIdx);
        
        return current();
    }
    
    public String save() {
        
        if (!isLoggedIn())
            return redirect();
        
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcReferences references = (DcReferences) vr.resolveVariable(fc, "references");
        
        Long[] keys = references.getKeys();
        
        DcWebObject wod = getParentObject();
        DcObject dco = wod.getDcObject();
        dco.setValue(references.getFieldIdx(), null);

        int referenceModIdx = dco.getField(references.getFieldIdx()).getReferenceIdx();
        for (Long key : keys) {
            DcObject reference = DataManager.getItem(referenceModIdx, key);
            DataManager.createReference(dco, references.getFieldIdx(), reference);
        }
        
        try {
            dco.setSilent(true);
            dco.saveUpdate(false);
        } catch (ValidationException ve) {
            ve.printStackTrace();
        }
        
        wod.load();
        
        return back();
    }
    
    private void setListItems(DcWebObject wod, DcReferences references, int fieldIdx) {
        int refIdx = DcModules.get(wod.getModule()).getField(fieldIdx).getReferenceIdx();
        List<DcObject> refs = DataManager.get(refIdx, null);
        
        List<SelectItem> values = new ArrayList<SelectItem>();
        for (DcObject reference : refs) {
            values.add(new SelectItem(reference.getID(), reference.toString()));
        }
        references.setListItems(values);
    }
    
    @SuppressWarnings("unchecked")
    private void setCurrentReferences(DcWebObject wod, DcReferences references, int fieldIdx) {
        Collection<DcMapping> currentRefs = (Collection<DcMapping>) wod.getDcObject().getValue(fieldIdx);
        if (currentRefs != null) {
            int counter = 0;
            Long[] keys = new Long[currentRefs.size()];
            for (DcMapping mapping : currentRefs) {
                keys[counter++] = mapping.getReferencedObject().getID();
            }
            references.setKeys(keys);
        }        
    }
}
