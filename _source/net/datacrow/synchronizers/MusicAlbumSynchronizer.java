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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.console.views.View;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.objects.helpers.AudioCD;
import net.datacrow.core.objects.helpers.MusicAlbum;
import net.datacrow.core.objects.helpers.MusicTrack;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.OnlineSearchHelper;
import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.SearchTask;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.fileimporters.MusicFile;
import net.datacrow.util.StringUtils;

import org.jaudiotagger.audio.exceptions.CannotReadException;

public class MusicAlbumSynchronizer extends DefaultSynchronizer {

    private DcObject dco;
    
    public MusicAlbumSynchronizer() {
        super(DcResources.getText("lblMassItemUpdate", DcModules.get(DcModules._MUSICALBUM).getObjectName()),
              DcModules._MUSICALBUM);
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("msgMusicFileMassUpdateHelp");
    }
    
    @Override
    public boolean canParseFiles() {
        return true;
    }

    @Override
    public boolean canUseOnlineServices() {
        return true;
    }
    
    public DcObject getDcObject() {
        return dco;
    }    
    
    @Override
    public Thread getTask() {
        return new Task();
    }    
    
    private class Task extends Thread {

        @Override
        public void run() {
            try {
                initialize();
                
                View view = DcModules.get(module).getCurrentSearchView();
                
                Collection<DcObject> objects = new ArrayList<DcObject>();
                objects.addAll(dlg.getItemPickMode() == _ALL ? view.getItems() : view.getSelectedItems());
                initProgressBar(objects.size());
                
                for (DcObject o : objects) {
                    
                    dco = o;
                    
                    if (isCancelled()) break;
                    
                    dco.loadChildren();
                    

                    boolean updated = false;
                    
                    if (isReparseFiles()) {
                        for (DcObject child : dco.getChildren()) {
                            
                            if (isCancelled()) break;
                            
                            String filename = child.getFilename();
                            
                            if (filename == null || filename.trim().length() == 0)
                                continue;
                            
                            File tst = new File(filename);
                            if (!tst.exists())
                                filename = filename.replaceAll("`", "'");
                            
                            try {
                                addMessage(DcResources.getText("msgParsing", filename));
                                
                                MusicFile musicFile = new MusicFile(filename);
        
                                dco.setValue(MusicAlbum._A_TITLE, musicFile.getAlbum());
                                
                                DcObject artist  = DataManager.createReference(dco, MusicAlbum._F_ARTISTS, musicFile.getArtist());
                                
                                setValue(child, MusicTrack._K_QUALITY, Long.valueOf(musicFile.getBitrate()));
                                setValue(child, MusicTrack._J_PLAYLENGTH, Long.valueOf(musicFile.getLength()));
                                setValue(child, MusicTrack._L_ENCODING, musicFile.getEncodingType());
                                setValue(child, MusicTrack._A_TITLE, musicFile.getTitle());

                                DataManager.createReference(child, MusicTrack._G_ARTIST, artist);
                                
                                setValue(child, MusicTrack._C_YEAR, musicFile.getYear());
                                setValue(child, MusicTrack._F_TRACKNUMBER, Long.valueOf(musicFile.getTrack()));
                                
                                DataManager.createReference(child, MusicTrack._H_GENRES, musicFile.getGenre());
                                
                                child.setSilent(true);
                                updated = true;
                            } catch (CannotReadException exp) {
                                child.markAsUnchanged();
                                addMessage(exp.getMessage());
                            }
                        }
                    }
                    
                    if (useOnlineService())
                        updated = onlineUpdate(dco, getServer(), getRegion(), getSearchMode());

                    updateProgressBar();
                    
                    try {
                        if (updated) {
                            dco.setSilent(true);
                            dco.saveUpdate(true);
                            
                            while (DatabaseManager.getQueueSize() > 0) {
                                try {
                                    sleep(100);
                                } catch (Exception exp) {}
                            }
                        }
                    } catch (ValidationException ve) {
                        addMessage(ve.getMessage());
                    }
                }
            } finally {
                addMessage(DcResources.getText("msgSynchronizerEnded"));
                enableAction(true);
            }  
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean onlineUpdate(DcObject dco, IServer server, Region region, SearchMode mode) {
        addMessage(DcResources.getText("msgSearchingOnlineFor", "" + dco));
        boolean updated = exactSearch(dco);
        
        if (!updated) {
            String title = (String) dco.getValue(MusicAlbum._A_TITLE);
            Collection<DcMapping> artists = (Collection<DcMapping>) dco.getValue(MusicAlbum._F_ARTISTS);
            if ((title == null || title.length() == 0) || (artists == null || artists.size() == 0)) 
                return updated;
            
            OnlineSearchHelper osh = new OnlineSearchHelper(dco.getModule().getIndex(), SearchTask._ITEM_MODE_SIMPLE);
            osh.setServer(server);
            osh.setRegion(region);
            osh.setMode(mode);
            osh.setMaximum(2);
            Collection<DcObject> albums = osh.query((String) dco.getValue(AudioCD._A_TITLE));
    
            for (DcObject albumNew : albums) {
                if (match(dco, albumNew)) {
                    updated = true;
                    DcObject albumNew2 = osh.query(albumNew);
                    updateTracks(dco.getChildren(), albumNew2.getChildren());
                    dco.copy(albumNew2, true);
                    updated = true;
                    albumNew2.unload();
                    break;
                }
            }
            
            albums.clear();
        }
        
        return updated;
    }    
    
    
    public void merge(DcObject dco1, DcObject dco2) {
        dco1.copy(dco2, true);
        updateTracks(dco1.getChildren(), dco2.getChildren());
    }
    
    @SuppressWarnings("unchecked")
    protected boolean match(DcObject dco1, DcObject dco2) {
        String title1 = (String) dco1.getValue(MusicAlbum._A_TITLE);
        String title2 = (String) dco2.getValue(MusicAlbum._A_TITLE);

        boolean match = false;
        if (StringUtils.equals(title1, title2)) {
            
            Collection<DcMapping> artists1 = (Collection<DcMapping>) dco1.getValue(MusicAlbum._F_ARTISTS);
            Collection<DcMapping> artists2 = (Collection<DcMapping>) dco2.getValue(MusicAlbum._F_ARTISTS);
            
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
    
    private void updateTracks(Collection<DcObject> oldTracks, Collection<DcObject> newTracks) {
        oldTracks = oldTracks == null ? new ArrayList<DcObject>() : oldTracks;
        newTracks = newTracks == null ? new ArrayList<DcObject>() : newTracks;
        
        for (DcObject oldTrack : oldTracks) {
            String titleOld = (String) oldTrack.getValue(MusicTrack._A_TITLE);
            Long lengthOld = (Long) oldTrack.getValue(MusicTrack._J_PLAYLENGTH);
            Long trackOld = (Long) oldTrack.getValue(MusicTrack._F_TRACKNUMBER);

            for (DcObject newTrack : newTracks) {
                
                String titleNew = (String) newTrack.getValue(MusicTrack._A_TITLE);
                Long lengthNew = (Long) newTrack.getValue(MusicTrack._J_PLAYLENGTH);
                Long trackNew = (Long) newTrack.getValue(MusicTrack._F_TRACKNUMBER);

                if ((titleOld != null && titleNew != null) && 
                    (StringUtils.equals(titleNew, titleOld))) {
                    merge((MusicTrack) newTrack, (MusicTrack) oldTrack);
                
                } else if (newTracks.size() == oldTracks.size() && 
                         ((lengthOld != null && lengthNew != null) && lengthNew.equals(lengthOld)) ||  
                         ((trackOld != null && trackNew != null) && trackNew.equals(trackOld))) {
                    merge((MusicTrack) newTrack, (MusicTrack) oldTrack);
                }
            }
        }
    }
    
    private void merge(MusicTrack mtOld, MusicTrack mt) {
        setValue(mt, MusicTrack._A_TITLE, mtOld.getValue(MusicTrack._A_TITLE));
        setValue(mt, MusicTrack._B_DESCRIPTION, mtOld.getValue(MusicTrack._B_DESCRIPTION));
        setValue(mt, MusicTrack._C_YEAR, mtOld.getValue(MusicTrack._C_YEAR));
        setValue(mt, MusicTrack._E_RATING, mtOld.getValue(MusicTrack._E_RATING));
        setValue(mt, MusicTrack._F_TRACKNUMBER, mtOld.getValue(MusicTrack._F_TRACKNUMBER));
        setValue(mt, MusicTrack._G_ARTIST, mtOld.getValue(MusicTrack._G_ARTIST));
        setValue(mt, MusicTrack._J_PLAYLENGTH, mtOld.getValue(MusicTrack._J_PLAYLENGTH));
    }    
}
