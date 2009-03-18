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

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Tab;

public class TabModule extends DcPropertyModule {

    public TabModule() {
        super(DcModules._TAB, "Tab", "Tab", "Tab", "Tab", "Tabs");
    }

    @Override
    public DcObject getDcObject() {
        return new Tab();
    }

    @Override
    public int getDefaultSortFieldIdx() {
        return Tab._C_ORDER;
    }

    /**
     * Initializes the default fields.
     */
    @Override
    protected void initializeFields() {
        super.initializeFields();
        addField(new DcField(Tab._C_ORDER, getIndex(), "Order", 
                false, true, false, false, false,
                4, ComponentFactory._NUMBERFIELD, getIndex(), DcRepository.ValueTypes._LONG,
                "SortOrder"));
        
        addField(new DcField(Tab._D_MODULE, getIndex(), "Module", 
                false, true, false, false, false,
                4, ComponentFactory._NUMBERFIELD, getIndex(), DcRepository.ValueTypes._LONG,
                "Module"));        
        
        getField(DcObject._ID).setEnabled(false);
        getField(Tab._C_ORDER).setEnabled(false);
    }      
}
