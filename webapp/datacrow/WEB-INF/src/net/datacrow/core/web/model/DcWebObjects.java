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

package net.datacrow.core.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.web.WebUtilities;

/**
 * Data model for the search page. 
 */
public class DcWebObjects extends Sortable {

    private DataModel data;
    private DataModel columnHeaders;
    private List<DcWebField> filterFields;
    
    private int module;
    
    public DcWebObjects() {
        super(null);
    }
    
    public String getName() {
        return DcModules.get(module).getObjectNamePlural();
    }
    
    public void setFilterFields(List<DcWebField> filterFields) {
        this.filterFields = filterFields;
    }
    
    public List<DcWebField> getFilterFields() {
        return filterFields;
    }

    public void setFields(List<DcWebField> fields) {
        columnHeaders = new ListDataModel(fields);
    }

    public void setObjects(List<?> objects) {
        data = new ListDataModel(objects);
    }

    public int getModule() {
        return module;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public DataModel getData() {
        sort(getSort(), isAscending());
        return data;
    }

    public DataModel getColumnHeaders() {
        return columnHeaders;
    }

    @SuppressWarnings("unchecked")
    public Object getColumnValue() {
        Object columnValue = null;
        if (data.isRowAvailable() && columnHeaders.isRowAvailable()) {
            columnValue = ((List) data.getRowData()).get(columnHeaders.getRowIndex());
        }
        return columnValue;
    }
    
    @SuppressWarnings("unchecked")
    public void setColumnValue(Object value) {
        if (data.isRowAvailable() && columnHeaders.isRowAvailable()) {
            ((List) data.getRowData()).set(columnHeaders.getRowIndex(), value);
        }
    }

    public String getColumnWidth() {
        if (data.isRowAvailable() && columnHeaders.isRowAvailable())
            return getField().getWidth();

        return null;
    }

    private DcWebField getField() {
        return ((DcWebField) columnHeaders.getRowData());
    }
    
    public boolean isValueModifiable() {
        if (data.isRowAvailable() && columnHeaders.isRowAvailable())
            return !getField().isReadonly();

        return false;
    }
    
    public boolean isLinkToDetails() {
        return getField().isLinkToDetails();
    }
    
    public boolean isImage() {
        return getField().isImage();
    }

    public boolean isUrl() {
        return getField().isUrl();
    }
    
    public boolean isText() {
        return !isImage() && !isUrl() && !isLinkToDetails() && !isFile();
    }

    public boolean isFile() {
        return getField().isFile();
    }
    
    @SuppressWarnings("unchecked")
    public void add(DcWebObject wod) {
        DataModel model = getData();
        List<List<Object>> list = (List) model.getWrappedData();
        List<DcWebField> fields = (List<DcWebField>) getColumnHeaders().getWrappedData();
        
        DcObject dco = wod.getDcObject();
        List<Object> row = new ArrayList<Object>();
        for (DcWebField wfs : fields) {
            for (DcField field : dco.getFields()) {
                if (wfs.getIndex() == field.getIndex() && field.getIndex() != DcObject._ID)
                    row.add(WebUtilities.getValue(dco, wfs, dco.getValue(wfs.getIndex())));
            }
        }   
        
        row.add(wod.getID());
        list.add(0, row);
    }
    
    @SuppressWarnings("unchecked")
    public void update(DcWebObject wod) {
        DataModel model = getData();
        List<List<Object>> list = (List) model.getWrappedData();
        List<Object> row = list.get(wod.getRowIdx());
        List<DcWebField> fields = (List<DcWebField>) getColumnHeaders().getWrappedData();
        
        int idx = 0;
        DcObject dco = wod.getDcObject();
        for (DcWebField wfs : fields) {
            for (DcField field : dco.getFields()) {
                if (wfs.getIndex() == field.getIndex() && field.getIndex() != DcObject._ID)
                    row.set(idx++, WebUtilities.getValue(dco, wfs, dco.getValue(wfs.getIndex())));
            }
        }     
        
        model.setRowIndex(wod.getRowIdx());
    }

    @SuppressWarnings("unchecked")
    public void remove(DcWebObject wod) {
        DataModel model = getData();
        List<List<Object>> list = (List) model.getWrappedData();
        list.remove(wod.getRowIdx());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void sort(String column, final boolean ascending) {
        if (column == null)
            return;
        
        final int columnIdx =  getColumnIndex(column);
        
        Comparator<List<?>> comparator = new Comparator<List<?>>() {
            public int compare(List<?> column1, List<?> column2) {
                Object o1 = column1.get(columnIdx);
                Object o2 = column2.get(columnIdx);
                
                int result = 0;
                if (o1 == null && o2 == null) {
                    result = 0;
                } else if (o1 == null || o2 == null) {
                    result = -1;
                } else if (o1 instanceof String) {
                    result = ((String) o1).toLowerCase().compareTo(((String) o2).toLowerCase());
                } else if (o1 instanceof Comparable) {
                    result = ((Comparable) o1).compareTo(o2);
                }
                return ascending ? result : result * -1;
            }
        };
        
        // as old sorting column is being remembered..
        // do not removed this!!
        try {
            Collections.sort((List) data.getWrappedData(), comparator);
        } catch (Exception ignore) {}
    }
    
    @SuppressWarnings("unchecked")
    private int getColumnIndex(final String columnName) {
        int columnIndex = -1;
        List headers = (List) columnHeaders.getWrappedData();
        for (int i = 0; i < headers.size() && columnIndex == -1; i++) {
            DcWebField header = (DcWebField) headers.get(i);
            if (header.getLabel().equals(columnName)) {
                columnIndex = i;
            }
        }
        return columnIndex;
    }
}
