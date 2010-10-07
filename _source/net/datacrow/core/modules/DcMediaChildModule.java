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

package net.datacrow.core.modules;

import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.views.CachedChildView;
import net.datacrow.console.views.MasterView;
import net.datacrow.console.views.View;
import net.datacrow.console.windows.itemforms.ChildForm;
import net.datacrow.console.windows.itemforms.DcMinimalisticItemView;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;

/**
 * Items belonging to a child module are dependent on the existence of a parent item.
 * Parent items always belong to another module, represented by the {@link DcMediaParentModule} class.
 * 
 * @author Robert Jan van der Waals
 */
public class DcMediaChildModule extends DcMediaModule implements IChildModule {

    private static final long serialVersionUID = 8257159881800394661L;

    private int[] minimalFields = null;
    
    /**
     * Creates a new instance based on a XML definition.
     * @param xmlModule
     */
    public DcMediaChildModule(XmlModule xmlModule) {
        super(xmlModule);
    }
    
    /**
     * Creates a new instance.
     * @param index The module index.
     * @param topModule Indicates if the module is a top module. Top modules are allowed
     * to be displayed in the module bar and can be enabled or disabled.
     * @param name The internal unique name of the module.
     * @param description The module description
     * @param objectName The name of the items belonging to this module.
     * @param objectNamePlural The plural name of the items belonging to this module.
     * @param tableName The database table name for this module.
     * @param tableShortName The database table short name for this module.
     */
    public DcMediaChildModule(int index, 
                              boolean topModule, 
                              String name, 
                              String description, 
                              String objectName, 
                              String objectNamePlural, 
                              String tableName, 
                              String tableShortName) {
        
        super(index, topModule, name, description, objectName, objectNamePlural,
              tableName, tableShortName);
    }
    
    @Override
    public int[] getMinimalFields(Collection<Integer> include) {
        if (minimalFields == null || include != null) {
            Collection<Integer> fields = new ArrayList<Integer>();
            
            int valueType;
            for (DcField field : getFields()) {
                valueType = field.getValueType();
                if (valueType != DcRepository.ValueTypes._DCOBJECTCOLLECTION &&
                    valueType != DcRepository.ValueTypes._DCOBJECTREFERENCE &&
                    valueType != DcRepository.ValueTypes._PICTURE) {
                    fields.add(Integer.valueOf(field.getIndex()));
                }
            }
            minimalFields = new int[fields.size()];
            int idx = 0;
            
            if (include != null) {
                for (Integer index : include)
                    minimalFields[idx++] = index.intValue();
            }
            
            for (Integer index : fields) {
                minimalFields[idx++] = index.intValue();
            }
        }
        return minimalFields;
    }    
    
    /**
     * Creates a new item view.
     */
    @Override
    public DcMinimalisticItemView getItemView(DcObject parent, int module, boolean readonly) {
        return new ChildForm(parent, module, readonly);
    }
    
    @Override
    public int[] getSupportedViews() {
        return new int[] {MasterView._TABLE_VIEW};
    }
    
    /**
     * Initializes the various views.
     */
    @Override
    protected void initializeUI() {
        if (searchView == null && insertView == null) {
            if (hasInsertView()) {
                insertView = new MasterView();
                DcTable table = new DcTable(this, false, true);
                View view = new CachedChildView(insertView, View._TYPE_INSERT, table, getObjectNamePlural(), getIcon16(), MasterView._TABLE_VIEW);
                table.setView(view);
                
                insertView.addView(MasterView._TABLE_VIEW, view);
            }

            if (hasSearchView()) {
                searchView = new MasterView();
                searchView.setTreePanel(this);
                
                DcTable table = new DcTable(this, false, true);
                View view = new View(searchView, View._TYPE_SEARCH, table, getObjectNamePlural(), getIcon16(), MasterView._TABLE_VIEW);
                table.setView(view);

                searchView.addView(MasterView._TABLE_VIEW, view);
            }
        }
    }   
    
    /**
     * Indicates if this module is a child module.
     */
    @Override
    public boolean isChildModule() {
        return true;
    }
    
    /**
     * Indicates if this module is a top module. Top modules are allowed
     * to be displayed in the module bar and can be enabled or disabled.
     */
    @Override
    public boolean isTopModule() {
        return false;
    } 
}