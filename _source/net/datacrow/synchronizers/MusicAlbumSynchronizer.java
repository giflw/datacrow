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
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.objects.helpers.MusicAlbum;
import net.datacrow.core.objects.helpers.MusicTrack;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.fileimporters.MusicFile;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.exceptions.CannotReadException;

public class MusicAlbumSynchronizer extends Synchronizer {

    private static Logger logger = Logger.getLogger(MusicAlbumSynchronizer.class.getName());
    
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
    
    @Override
    public boolean onlineUpdate(DcObject dco, IServer server, Region region, SearchMode mode) {
        boolean updated = false;
        try {
            MusicFile musicFile = new MusicFile();
        
            addMessage(DcResources.getText("msgSearchingOnlineFor", "" + dco));
            DcObject dcoNew = dco.getModule().getOnlineService().query(dco);
            
            if (dcoNew != null) {
                musicFile.merge(dco, dcoNew);
                dcoNew.unload();
                updated = true;
            } else {
                updated = musicFile.onlineUpdate(dco, server, region);    
            }
        } catch (Exception e) {
            logger.error("An error occurred while updating " + dco + " online", e);
        }

        return updated;
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
                    
                    if (isReparseFiles() && dco.getChildren() != null) {
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
                        onlineUpdate(dco, getServer(), getRegion(), getSearchMode());

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
}
