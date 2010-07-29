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

import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.web.WebUtilities;
import net.datacrow.core.web.model.AdvancedFilter;
import net.datacrow.core.web.model.DcWebField;
import net.datacrow.core.web.model.DcWebModule;
import net.datacrow.core.web.model.DcWebObjects;
import net.datacrow.settings.definitions.WebFieldDefinition;
import net.datacrow.settings.definitions.WebFieldDefinitions;

import org.apache.myfaces.custom.navmenu.NavigationMenuItem;

/**
 * The search bean. Manages filters and the items to be displayed.
 * Uses the DataManager and other Data Crow core functionality.
 */
public class ItemSearch extends DcBean {
    
    private static final int max = 0;

    public String search() {
        return search(false);
    }
    
    @Override
    public String back() {
        return "search";
    }

    @Override
    public String current() {
        return "search";
    }

    @Override
    public List<NavigationMenuItem> getMenuItems() {
        
        List<NavigationMenuItem> menu = new ArrayList<NavigationMenuItem>();

        // edit
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebObjects objects = (DcWebObjects) vr.resolveVariable(fc, "webObjects");
        
        if (getUser().isEditingAllowed(DcModules.get(objects.getModule()))) {
            NavigationMenuItem edit = getMenuItem(DcResources.getText("lblEdit"), null, null);
            
            new DcWebModule(objects.getModule(), "").getIcon16();
            
            edit.add(getMenuItem(DcResources.getText("lblCreateNew"), "#{itemDetails.create}", "modules/" + DcModules.get(objects.getModule()).getName().toLowerCase() + "16.png"));
            menu.add(edit);
        }
        
        addLogoffMenuItem(menu);
        
        return menu;
    }
    
    @Override
    public String getActionListener() {
        return "#{itemSearch.actionListener}";
    }

    public String search(boolean advanced) {
        
        if (!isLoggedIn())
            return redirect();
        
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        Map map = fc.getExternalContext().getRequestParameterMap();
        
        DcWebObjects objects = (DcWebObjects) vr.resolveVariable(fc, "webObjects");
        if (map.get("moduleId") != null && !map.get("moduleId").equals("")) { 
            int moduleIdx = Integer.valueOf((String) map.get("moduleId"));
            
            if (getUser().isAuthorized(DcModules.get(moduleIdx))) {
                objects.setModule(moduleIdx);
                setFields(objects);
                setFilterFields(objects);
            } else {
                return redirect();
            }
        }
        
        applyFilter(objects, advanced);
        return current();
    }
    
    private void setFilterFields(DcWebObjects wo) {
        List<DcWebField> filterFields = new ArrayList<DcWebField>();
        for (DcField field : DcModules.get(wo.getModule()).getFields()) {
            
            WebFieldDefinitions wfd = (WebFieldDefinitions) DcModules.get(field.getModule()).getSetting(DcRepository.ModuleSettings.stWebFieldDefinitions);
            
            if (     getUser().isAuthorized(field) && 
                     wfd.get(field.getIndex()).isQuickSearch() &&
                     field.isEnabled() && 
                     field.isSearchable()) {
                
                DcWebField wf = new DcWebField(field);
                if (field.getIndex() == DcObject._SYS_AVAILABLE) 
                    wf.setValue(Boolean.TRUE);

                if (wf.isLongTextfield())
                    wf.setType(DcWebField._TEXTFIELD);
                
                if (wf.isMultiRelate())
                    wf.setType(DcWebField._DROPDOWN);
                
                filterFields.add(wf);
            }
        }
        wo.setFilterFields(filterFields);
    }
    
    private void setFields(DcWebObjects wo) {
        List<DcWebField> fields = new ArrayList<DcWebField>();
        
        for (WebFieldDefinition def : DcModules.get(wo.getModule()).getWebFieldDefinitions().getDefinitions()) {
            DcField field = DcModules.get(wo.getModule()).getField(def.getField());
            
            if (field != null) {
                if (def.isOverview() && getUser().isAuthorized(field) && field.isEnabled()) {
                    DcWebField wf = new DcWebField(field);
                    wf.setWidth(def.getWidth());
                    wf.setLinkToDetails(def.isLink());
                    wf.setMaxTextLength(def.getMaxTextLength());
                    fields.add(wf);
                }
            }
        }
        
        wo.setFields(fields);
    }
    
    private DataFilter getFilter(DcWebObjects wo) {
        DataFilter df = new DataFilter(wo.getModule());
        
        for (DcWebField wf : wo.getFilterFields()) {
            Object value = wf.getValue();
            String s = value instanceof String ? (String) value : value == null ? "" : value.toString();
                    
            if (s != null && s.trim().length() > 0) {
                DcField field = wf.getDcField(); 

                DataFilterEntry dfe = new DataFilterEntry(DataFilterEntry._AND, 
                        wo.getModule(), field.getIndex(), 
                        Operator.CONTAINS, value);
                
                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                    dfe.setValue(DataManager.getItem(field.getReferenceIdx(), Long.valueOf(s)));
                } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    Collection<DcObject> references = new ArrayList<DcObject>();
                    references.add(DataManager.getItem(field.getReferenceIdx(), Long.valueOf(s)));
                    dfe.setValue(references);
                }
                
                df.addEntry(dfe);
            }
        }
        return df;
    }
    
    private void applyFilter(DcWebObjects wo, boolean advanced) {
        List<List<?>> result = new ArrayList<List<?>>();
        int count = 0;
        
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        AdvancedFilter af = (AdvancedFilter) vr.resolveVariable(fc, "advancedFilter");

        DataFilter df = advanced ?  af.getFilter() : getFilter(wo);
        for (DcObject dco :  DataManager.get(df)) {

            if (count == max) break;

            List<Object> values = new ArrayList<Object>();
            
            
            for (WebFieldDefinition def : DcModules.get(wo.getModule()).getWebFieldDefinitions().getDefinitions()) {
                DcField field = DcModules.get(wo.getModule()).getField(def.getField());
                
                if (field != null && def.isOverview() && getUser().isAuthorized(field) && field.isEnabled())
                    values.add( WebUtilities.getValue(dco, def, dco.getValue(def.getField())));
            }
            
            values.add(dco.getID());
            result.add(values);
            count++;
        }
        wo.setObjects(result);
    }
}
