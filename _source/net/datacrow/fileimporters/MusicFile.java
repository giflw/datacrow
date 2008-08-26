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

package net.datacrow.fileimporters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcMediaObject;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.MusicAlbum;
import net.datacrow.core.objects.helpers.MusicTrack;
import net.datacrow.core.services.IOnlineSearchClient;
import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.SearchTask;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.util.StringUtils;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

public class MusicFile implements IOnlineSearchClient {
    
    private static Logger logger = Logger.getLogger(MusicFile.class.getName());
    
    private String album;
    private String artist;
    private String genre;
    private String year;
    private String title;
    private String encodingType;
    
    private int track;
    private int bitrate;
    private int length;
    
    private Collection<DcObject> albums = new ArrayList<DcObject>();
    
    public MusicFile() {}
    
    @SuppressWarnings("unchecked")
    public MusicFile(String filename) throws CannotReadException {
        AudioFile audioFile;
        try {
        	audioFile = AudioFileIO.read(new File(filename));
        } catch (IOException e1) {
            // Will not be thrown for now (by AudioFileIO.read)
            throw new CannotReadException(e1);
        } catch (TagException e1) {
            // Will not be thrown for now (by AudioFileIO.read)
            throw new CannotReadException(e1);
        } catch (ReadOnlyFileException e1) {
            // Will not be thrown for now (by AudioFileIO.read)
            throw new CannotReadException(e1);
        } catch (InvalidAudioFrameException e1) {
            // Will not be thrown for now (by AudioFileIO.read)
            throw new CannotReadException(e1);
        }
        
        Tag tag = audioFile.getTag();
        if (tag != null) {
            album = tag.getFirstAlbum();
            artist = tag.getFirstArtist();
            genre = getGenre(tag.getFirstGenre());
            year = tag.getFirstYear();
            title = tag.getFirstTitle();
            
            bitrate = (int)audioFile.getAudioHeader().getBitRateAsNumber();
            length = audioFile.getAudioHeader().getTrackLength();
            encodingType = audioFile.getAudioHeader().getEncodingType();
            
            try {
                String s = tag.getFirstTrack();
                if (s != null && s.length() > 0) {
                    if (s.indexOf("/") > 0)
                        s = s.substring(0, s.indexOf("/"));
                    
                    track = Integer.parseInt(s);
                }
                    
            } catch (Exception e) {
                logger.debug("Could not parse track [" + tag.getTrack() + "]", e);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public boolean onlineUpdate(DcObject dco, IServer server, Region region) {
        boolean updated = false;
        
        String title = (String) dco.getValue(MusicAlbum._A_TITLE);
        Collection<DcMapping> artists = (Collection<DcMapping>) dco.getValue(MusicAlbum._F_ARTISTS);
        if ((title == null || title.length() == 0) || (artists == null || artists.size() == 0)) 
            return updated;
        
        for (SearchMode mode : server.getSearchModes()) {
            SearchTask process = 
                server.getSearchTask(this, mode, region, (String) dco.getValue(DcMediaObject._A_TITLE));
            
            process.setMaximum(10);
            process.start();
            
            try {
                process.join(30000);
            } catch (Exception e) {
                logger.error("Could not join threads", e);
            }
            
            process.cancelSearch();
            
            for (DcObject album : new ArrayList<DcObject>(albums)) {
                String titleNew = (String) album.getValue(MusicAlbum._A_TITLE);
                Collection<DcMapping> artistsNew = (Collection<DcMapping>) dco.getValue(MusicAlbum._F_ARTISTS);
                
                if (titleNew != null && artistsNew != null) {
                    boolean sameArtists = false;
                    
                    for (DcObject artist : artists) {
                        for (DcObject artistNew : artistsNew) 
                            sameArtists |=  StringUtils.equals(artist.toString(), artistNew.toString());
                    }
                    
                    if (StringUtils.equals(titleNew, title) && sameArtists) {
                        if (updateTracks(dco.getChildren(), album.getChildren())) {
                            updated = true;
                            dco.copy(album, true);
                            break;
                        }
                    }
                }
            }
            
            for (DcObject album : albums)
                album.unload();
            
            albums.clear();
        }
        return updated;
    }
    
    public void merge(DcObject dco1, DcObject dco2) {
        dco1.copy(dco2, true);
        updateTracks(dco1.getChildren(), dco2.getChildren());
    }
    
    private boolean updateTracks(Collection<DcObject> oldTracks, Collection<DcObject> newTracks) {
        boolean match = false;
        
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

                    match = true;
                    merge((MusicTrack) newTrack, (MusicTrack) oldTrack);
                
                } else if (newTracks.size() == oldTracks.size() && 
                         ((lengthOld != null && lengthNew != null) && lengthNew.equals(lengthOld)) ||  
                         ((trackOld != null && trackNew != null) && trackNew.equals(trackOld))) {

                    match = true;
                    merge((MusicTrack) newTrack, (MusicTrack) oldTrack);
                }
            }
        }
        return match;
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
    
    private void setValue(DcObject dco, int field, Object newValue) {
        if (newValue != null && newValue.toString().length() > 0)
            dco.setValue(field, newValue);
    }
    
    private String getGenre(String s) {
        String genre = s != null && s.length() > 0 ? s : null;
        
        if (genre != null) {
            try {
                String genreIdx = StringUtils.getValueBetween("(", ")", genre);
                int index = Integer.parseInt(genreIdx);
                genre = DcRepository.Collections.colMusicGenres[index];
            } catch (Exception exp) {}
        }
        
        return genre;
    }    
        
    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public String getTitle() {
        return title;
    }

    public int getTrack() {
        return track;
    }

    public String getYear() {
        return year;
    }

    public int getBitrate() {
        return bitrate;
    }

    public String getEncodingType() {
        return encodingType;
    }

    public int getLength() {
        return length;
    }

    public void addError(Throwable t) {
        logger.error(t.getMessage(), t);
    }

    public void addError(String message) {}

    public void addMessage(String message) {}

    public void addObject(DcObject dco) {
        albums.add(dco);
    }

    public void addWarning(String warning) {}

    public DcModule getModule() {
        return DcModules.get(DcModules._MUSICALBUM);
    }

    public void processed(int i) {}

    public void processing() {}

    public void processingTotal(int i) {}

    public int resultCount() {
        return albums.size();
    }

    public void stopped() {}

    public DcObject getDcObject() {
        return null;
    } 
}


