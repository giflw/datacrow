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

import java.util.List;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.windows.itemforms.DcMinimalisticItemView;
import net.datacrow.core.DcRepository;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.db.Query;
import net.datacrow.core.db.QueryOptions;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcProperty;

import org.apache.log4j.Logger;

public class DcPropertyModule extends DcModule {

    private static final long serialVersionUID = -1481435217423089270L;

    private static Logger logger = Logger.getLogger(DcPropertyModule.class.getName());
    
    protected DcMinimalisticItemView form;
    
    public DcPropertyModule(XmlModule xmlModule) {
        super(xmlModule);
        setServingMultipleModules(xmlModule.isServingMultipleModules());
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
     * @param tableJoin The join name.
     */
    public DcPropertyModule(int index, 
                            String name, 
                            String tableName, 
                            String tableShortName, 
                            String objectName, 
                            String objectNamePlural) {
        
        super(index, false, name, "", objectName, objectNamePlural, tableName, tableShortName, "");
    }    

    public DcMinimalisticItemView getForm() {
        initializeUI();
        return form;
    }
    
    @Override
    public List<DcObject> loadData() {
        try {
            QueryOptions options = new QueryOptions(new DcField[] {getField(DcProperty._A_NAME)}, true, false);
            Query qry = new Query(Query._SELECT, getDcObject(), options, null);
            return DatabaseManager.executeQuery(qry, true);
        } catch (Exception e) {
            logger.error("Could not load data for module " + getLabel(), e);
        }
        return null;
    }    
    
    @Override
    public int getDefaultSortFieldIdx() {
        return DcProperty._A_NAME;
    }    
    
    @Override
    public void initializeUI() {
        if (form == null)
            form = new DcMinimalisticItemView(getIndex(), false);
    }
    
    @Override
    public DcObject getDcObject() {
        return new DcProperty(getIndex());
    }
    
    @Override
    public boolean isCustomFieldsAllowed() {
        return false;
    }
    
    @Override
    protected void initializeFields() {
        super.initializeFields();
        addField(new DcField(DcProperty._A_NAME, getIndex(), "Name", 
                             false, true, false, true, false,
                             255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                             "Name"));
        addField(new DcField(DcProperty._B_ICON, getIndex(), "Icon", 
                             false, true, false, false, false,
                             255, ComponentFactory._PICTUREFIELD, getIndex(), DcRepository.ValueTypes._ICON,
                             "Icon"));
        
        getField(DcObject._ID).setEnabled(false);
    }  
    
    @Override
    public TemplateModule getTemplateModule() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DcPropertyModule ? ((DcPropertyModule) o).getIndex() == getIndex() : false);
    }
}
