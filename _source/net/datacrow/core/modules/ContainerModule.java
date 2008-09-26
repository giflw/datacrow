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

import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.views.MasterView;
import net.datacrow.console.views.View;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.Container;

public class ContainerModule extends DcParentModule {

    public ContainerModule(int index, boolean topModule, String name,
            String description, String objectName, String objectNamePlural,
            String tableName, String tableShortName, String tableJoin) {
        
        super(index, topModule, name, description, objectName, objectNamePlural,
              tableName, tableShortName, tableJoin);
    }
    
    @Override
    protected void initializeUI()  {
        if (searchView == null && hasSearchView() ) {
            searchView = new MasterView();
            searchView.setTreePanel(this);
            
//            // table view
//            DcTable table = new DcTable(this, false, true);
//            View tableView = new View(searchView, View._TYPE_SEARCH, table, getObjectNamePlural(), getIcon16(), MasterView._TABLE_VIEW);
//            table.setView(tableView);
            
            // list view
            DcObjectList list = new DcObjectList(this, DcObjectList._CARDS, true, true);
            View listView = new View(searchView, View._TYPE_SEARCH, list, getObjectNamePlural(), getIcon16(), MasterView._LIST_VIEW);
            list.setView(listView);

//            searchView.addView(MasterView._TABLE_VIEW, tableView);
            searchView.addView(MasterView._LIST_VIEW, listView);            
        }
    }
    
    @Override
    public boolean isSelectableInUI() {
        return true;
    }

    public ContainerModule(XmlModule module) {
        super(module);
    }

    @Override
    public DcObject getDcObject() {
        return new Container();
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof ContainerModule ? ((ContainerModule) o).getIndex() == getIndex() : false);
    }      
}
