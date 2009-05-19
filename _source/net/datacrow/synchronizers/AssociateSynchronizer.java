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

package net.datacrow.synchronizers;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcAssociate;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.OnlineSearchHelper;
import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.SearchTask;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.util.StringUtils;

public class AssociateSynchronizer extends DefaultSynchronizer {

    private DcObject dco;
    
    public AssociateSynchronizer() {
        super(DcResources.getText("lblMassItemUpdate", DcModules.getCurrent().getObjectName()),
              DcModules.getCurrent().getIndex());    
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("msgAssociateMassUpdateHelp",
                                    new String[] {DcModules.getCurrent().getObjectNamePlural().toLowerCase(),
                                                  DcModules.getCurrent().getObjectName().toLowerCase()});
    }

    public DcObject getDcObject() {
        return dco;
    }
    
    @Override
    public boolean onlineUpdate(DcObject dco, IServer server, Region region, SearchMode mode) {
        
        this.dco = dco;
        boolean updated = exactSearch(dco);
        
        int field = mode != null ? mode.getFieldBinding() : DcAssociate._A_NAME;
        
        if (!updated) {
            String value = StringUtils.normalize((String) dco.getValue(field));

            if (value == null || value.length() == 0) 
                return updated;
            
            OnlineSearchHelper osh = new OnlineSearchHelper(dco.getModule().getIndex(), SearchTask._ITEM_MODE_SIMPLE);
            osh.setMode(mode);
            DcObject result = osh.query(dco, (String) dco.getValue(field), new int[] {field});
            update(dco, result, osh);
        }
        
        return updated;
    }
}
