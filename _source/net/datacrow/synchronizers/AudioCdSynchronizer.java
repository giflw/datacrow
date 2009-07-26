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

import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.AudioCD;
import net.datacrow.core.objects.helpers.AudioTrack;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.OnlineSearchHelper;
import net.datacrow.util.StringUtils;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * Basically the same as the MusicAlbumSynchronizer class. 
 * However, for customization reasons (and the likes) it was decided to keep this class.
 * @author Robert Jan van der Waals
 */
public class AudioCdSynchronizer extends DefaultSynchronizer {

    private static Logger logger = Logger.getLogger(AudioCdSynchronizer.class.getName());
    
    public AudioCdSynchronizer() {
        super(DcResources.getText("lblMassItemUpdate", DcModules.get(DcModules._AUDIOCD).getObjectName()),
              DcModules._AUDIOCD);
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("msgAudioCdMassUpdateHelp");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected boolean matches(DcObject result, String searchString, int fieldIdx) {
        boolean matches = super.matches(result, searchString, fieldIdx);
        if (matches && (client.getSearchMode() == null || client.getSearchMode().keywordSearch())) {
            // Additionally one of the artists has to match. Only used for keyword searches!
            Collection<DcMapping> artists1 = (Collection<DcMapping>) result.getValue(AudioCD._F_ARTIST);
            Collection<DcMapping> artists2 = (Collection<DcMapping>) getDcObject().getValue(AudioCD._F_ARTIST);
            artists1 = artists1 == null ? new ArrayList<DcMapping>() : artists1;
            artists2 = artists2 == null ? new ArrayList<DcMapping>() : artists2;
            for (DcObject person1 : artists1) {
                for (DcObject person2 : artists2) {
                    matches = StringUtils.equals(person1.toString(), person2.toString()); 
                    if (matches) break;
                }
            }
        }
        return matches;    
    }
    
    @Override
    protected void merge(DcObject target, DcObject source, OnlineSearchHelper osh) {
        super.merge(target, source, osh);

        Collection<DcObject> oldTracks = target.getChildren() == null ? new ArrayList<DcObject>() : target.getChildren();
        Collection<DcObject> newTracks = source.getChildren() == null ? new ArrayList<DcObject>() : source.getChildren();
        
        if (oldTracks.size() == 0) {
            for (DcObject track : newTracks) {
                track.setValue(track.getParentReferenceFieldIndex(), target.getID());
                target.addChild(track);
                try {
                    track.saveNew(false);
                } catch (Exception e) {
                    logger.error("Unable to save new audio track " + track, e);
                }
            }
        } else {
            for (DcObject currentTrack : oldTracks) {
                for (DcObject newTrack : newTracks) {
                    if (StringUtils.equals(currentTrack.getDisplayString(AudioTrack._A_TITLE), newTrack.getDisplayString(AudioTrack._A_TITLE))) {
                        currentTrack.copy(newTrack, true);
                        break;
                    } else if (newTracks.size() == oldTracks.size() && 
                            !Utilities.isEmpty(currentTrack.getValue(AudioTrack._H_PLAYLENGTH)) && 
                             currentTrack.getDisplayString(AudioTrack._H_PLAYLENGTH).equals(newTrack.getDisplayString(AudioTrack._H_PLAYLENGTH))) {    
                        currentTrack.copy(newTrack, true);
                        break;
                    }
                }
            }
        }
    }
}
