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
import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.SearchTask;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.util.StringUtils;

import org.apache.log4j.Logger;

public class AudioCdSynchronizer extends DefaultSynchronizer {

    private DcObject dco;
    
    private static Logger logger = Logger.getLogger(AudioCdSynchronizer.class.getName());
    
    public AudioCdSynchronizer() {
        super(DcResources.getText("lblMassItemUpdate", DcModules.get(DcModules._AUDIOCD).getObjectName()),
              DcModules._AUDIOCD);
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("msgAudioCdMassUpdateHelp");
    }
    
    public DcObject getDcObject() {
        return dco;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean onlineUpdate(DcObject album, IServer server, Region region, SearchMode mode) {
        addMessage(DcResources.getText("msgSearchingOnlineFor", "" + dco));
        boolean updated = exactSearch(album);
        
        if (!updated) {
            String title = (String) album.getValue(AudioCD._A_TITLE);
            Collection<DcMapping> artists = (Collection<DcMapping>) album.getValue(AudioCD._F_ARTIST);
            
            if ((title == null || title.trim().length() == 0) || (artists == null || artists.size() == 0)) 
                return updated;
            
            OnlineSearchHelper osh = new OnlineSearchHelper(album.getModule().getIndex(), SearchTask._ITEM_MODE_SIMPLE);
            osh.setServer(server);
            osh.setRegion(region);
            osh.setMode(mode);
            osh.setMaximum(2);
            Collection<DcObject> c = osh.query((String) album.getValue(AudioCD._A_TITLE));

            for (DcObject albumNew : c) {
                if (match(album, albumNew)) {
                    DcObject albumNew2 = osh.query(albumNew);
                    updateTracks(album, album.getChildren(), albumNew2.getChildren());
                    album.copy(albumNew2, true);
                    updated = true;
                    albumNew2.unload();
                }
            }
            
            c.clear();
        }
        return updated;
    }
    
    private void updateTracks(DcObject album, Collection<DcObject> oldTracks, Collection<DcObject> newTracks) {
        
        oldTracks = oldTracks == null ? new ArrayList<DcObject>() : oldTracks;
        newTracks = newTracks == null ? new ArrayList<DcObject>() : newTracks;
        
        if (oldTracks.size() == 0) {
            for (DcObject track : newTracks) {
                track.setValue(track.getParentReferenceFieldIndex(), album.getID());
                album.addChild(track);
                try {
                    track.saveNew(false);
                } catch (Exception e) {
                    logger.error("Unable to save new audio track " + track, e);
                }
            }
        } else {
            for (DcObject currentTrack : oldTracks) {
                
                String titleOld = (String) currentTrack.getValue(AudioTrack._A_TITLE);
                Long lengthOld = (Long) currentTrack.getValue(AudioTrack._H_PLAYLENGTH);
                Long trackOld = (Long) currentTrack.getValue(AudioTrack._F_TRACKNUMBER);
    
                for (DcObject newTrack : newTracks) {
                    
                    String titleNew = (String) newTrack.getValue(AudioTrack._A_TITLE);
                    Long lengthNew = (Long) newTrack.getValue(AudioTrack._H_PLAYLENGTH);
                    Long trackNew = (Long) newTrack.getValue(AudioTrack._F_TRACKNUMBER);
    
                    if ((titleOld != null && titleNew != null) && 
                         StringUtils.equals(titleNew, titleOld)) {
                        
                        currentTrack.copy(newTrack, true);
                    
                    } else if (newTracks.size() == oldTracks.size() && 
                               ((lengthOld != null && lengthNew != null) && lengthNew.equals(lengthOld)) ||  
                               ((trackOld != null && trackNew != null) && trackNew.equals(trackOld))) {
                    
                        currentTrack.copy(newTrack, true);
                    }
                }
            }
        }
    } 
    
    @SuppressWarnings("unchecked")
    protected boolean match(DcObject dco1, DcObject dco2) {
        String title1 = (String) dco1.getValue(AudioCD._A_TITLE);
        String title2 = (String) dco2.getValue(AudioCD._A_TITLE);

        boolean match = false;
        if (StringUtils.equals(title1, title2)) {
            
            Collection<DcMapping> artists1 = (Collection<DcMapping>) dco1.getValue(AudioCD._F_ARTIST);
            Collection<DcMapping> artists2 = (Collection<DcMapping>) dco2.getValue(AudioCD._F_ARTIST);
            
            artists1 = artists1 == null ? new ArrayList<DcMapping>() : artists1;
            artists2 = artists2 == null ? new ArrayList<DcMapping>() : artists2;

            for (DcObject person1 : artists1) {
                for (DcObject person2 : artists2) {
                    String name1 = person1.toString().trim();
                    String name2 = person2.toString().trim();
                    match = StringUtils.equals(name1, name2); 
                    if (match) break;
                }
            }
        }
        return match;        
    }
}
