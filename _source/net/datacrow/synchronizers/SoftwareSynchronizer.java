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

import java.util.Collection;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.Software;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.OnlineSearchHelper;
import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.SearchTask;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.util.StringUtils;
import net.datacrow.util.Utilities;

public class SoftwareSynchronizer extends DefaultSynchronizer {

    private DcObject dco;
    
    public SoftwareSynchronizer() {
        super(DcResources.getText("lblMassItemUpdate", DcModules.get(DcModules._SOFTWARE).getObjectName()),
              DcModules._SOFTWARE);
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("msgSoftwareMassUpdateHelp");
    }
    
    public DcObject getDcObject() {
        return dco;
    }
    
    @Override
    public boolean onlineUpdate(DcObject dco, IServer server, Region region, SearchMode mode) {
        boolean updated = exactSearch(dco);
        
        int field = mode == null ? Software._A_TITLE : mode.getFieldBinding();
        
        if (!updated) {
            String value = (String) dco.getValue(field);
            
            if (Utilities.isEmpty(value)) 
                return updated;
            
            String searchString = (String) dco.getValue(field);
            
            if (field == Software._A_TITLE && server.getName().equals("MobyGames"))
                searchString += dco.getValue(Software._H_PLATFORM) != null ? 
                                " " + dco.getDisplayString(Software._H_PLATFORM) : "";

            OnlineSearchHelper osh = new OnlineSearchHelper(dco.getModule().getIndex(), SearchTask._ITEM_MODE_SIMPLE);
            osh.setServer(server);
            osh.setRegion(region);
            osh.setMode(mode);
            Collection<DcObject> items = osh.query(searchString);
            
            for (DcObject software : items) {
                String valueNew = (String) software.getValue(field);
                
                if (valueNew.indexOf(" for ") > -1)
                    valueNew = valueNew.substring(0, valueNew.indexOf(" for "));
                
                if (StringUtils.equals(value, valueNew)) {
                    update(dco, software, osh);
                    updated = true;
                    break;
                }
            }
        }
        
        return updated;
    }
}
