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
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.util.StringUtils;

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
        this.dco = dco;
        boolean updated = exactSearch(dco);
        
        if (!updated) {
            String title = StringUtils.normalize((String) dco.getValue(Software._A_TITLE));
            
            if ((title == null || title.length() == 0)) 
                return updated;
            
            String searchString = (String) dco.getValue(Software._A_TITLE);
            
            searchString += server.getName().equals("MobyGames") && dco.getValue(Software._H_PLATFORM) != null ? 
                                " " + dco.getDisplayString(Software._H_PLATFORM) : "";

            OnlineSearchHelper osh = new OnlineSearchHelper(dco.getModule().getIndex());
            osh.setServer(server);
            osh.setRegion(region);
            osh.setMode(mode);
            Collection<DcObject> items = osh.query(searchString);
            
            for (DcObject software : items) {
                String titleNew = StringUtils.normalize((String) software.getValue(Software._A_TITLE));
                
                if (titleNew.indexOf(" for ") > -1)
                    titleNew = titleNew.substring(0, titleNew.indexOf(" for "));
                
                if (StringUtils.equals(titleNew, title)) {
                    dco.copy(software, true);
                    updated = true;
                    break;
                }
            }
        }
        
        return updated;
    }
}
