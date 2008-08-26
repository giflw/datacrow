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
import net.datacrow.core.objects.helpers.Movie;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.OnlineSearchHelper;
import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.util.StringUtils;

public class MovieSynchronizer extends DefaultSynchronizer {

    private DcObject dco;
    
    public MovieSynchronizer() {
        super(DcResources.getText("lblMassItemUpdate", DcModules.get(DcModules._MOVIE).getObjectName()),
              DcModules._MOVIE);
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("msgMovieMassUpdateHelp");
    }
    
    
    public DcObject getDcObject() {
        return dco;
    }

    @Override
    public boolean onlineUpdate(DcObject dco, IServer server, Region region, SearchMode mode) {
        this.dco = dco;
        boolean updated = exactSearch(dco);
        
        if (!updated) {
            String title = (String) dco.getValue(Movie._A_TITLE);
            if (title.trim().length() == 0) return updated;
            
            OnlineSearchHelper osh = new OnlineSearchHelper(dco.getModule().getIndex());
            osh.setServer(server);
            osh.setRegion(region);
            osh.setMode(mode);
            osh.setMaximum(2);
            Collection<DcObject> movies = osh.query((String) dco.getValue(Movie._A_TITLE));
            for (DcObject movie : movies) {
                if (StringUtils.equals(title, (String) movie.getValue(Movie._A_TITLE))) {
                    updated = true;
                    dco.copy(movie, true);
                    break;
                }
            }
            
            for (DcObject movie : movies)
                movie.unload();
        }

        return updated;
    }
}
