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

import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.Movie;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.Hash;
import net.datacrow.util.movie.FilePropertiesMovie;

/**
 * Importer for movie files.
 * 
 * @author Robert Jan van der Waals
 */
public class MovieImporter extends FileImporter {

    public MovieImporter() {
        super(DcModules._MOVIE);
    }

    @Override
    public String[] getDefaultSupportedFileTypes() {
        return new String[] {"mpg", "mpeg", "riff", "avi", "vob",
                             "ogm", "mov",  "ifo",  "mkv", "asf", 
                             "wmv"};    
    }
    
    @Override
    public boolean allowReparsing() {
        return true;
    }    
    
    @Override
    public boolean canImportArt() {
        return true;
    }    

    @Override
    public DcObject parse(String filename, int directoryUsage) {
        DcObject movie = DcModules.get(DcModules._MOVIE).getItem();

        movie.setValue(Movie._SYS_FILENAME, filename);
        movie.setValue(Movie._A_TITLE, getName(filename, directoryUsage));

        try {
            Hash.getInstance().calculateHash(movie);

            FilePropertiesMovie fpm = new FilePropertiesMovie(filename);
            Long playlength = Long.valueOf(fpm.getDuration());
            if (playlength.intValue() <= 0 && fpm.getVideoWidth() <= 0)
                return movie;

            if (directoryUsage != 1 && fpm.getName() != null && fpm.getName().trim().length() > 0)
                movie.setValue(Movie._A_TITLE, fpm.getName());
            
            DataManager.createReference(movie, Movie._D_LANGUAGE, fpm.getLanguage());
            
            movie.setValue(Movie._L_PLAYLENGTH, playlength);
            movie.setValue(Movie._N_VIDEOCODEC, fpm.getVideoCodec());
            movie.setValue(Movie._O_AUDIOCODEC, fpm.getAudioCodec());
            movie.setValue(Movie._P_WIDTH, Long.valueOf(fpm.getVideoWidth()));
            movie.setValue(Movie._Q_HEIGHT, Long.valueOf(fpm.getVideoHeight()));
            movie.setValue(Movie._R_FPS, Double.valueOf(fpm.getVideoRate()));
            movie.setValue(Movie._S_FRAMES, (long) (fpm.getDuration() * fpm.getVideoRate()));
            movie.setValue(Movie._T_AUDIOBITRATE, Long.valueOf(fpm.getAudioBitRate()));
            movie.setValue(Movie._U_AUDIOSAMPLINGRATE, Long.valueOf(fpm.getAudioRate()));
            movie.setValue(Movie._V_AUDIOCHANNEL, Long.valueOf(fpm.getAudioChannels()));
            
            DataManager.createReference(movie, Movie._2_SUBTITLELANGUAGE, fpm.getSubtitles());
            
            setImages(filename, movie, Movie._X_PICTUREFRONT, Movie._Y_PICTUREBACK, Movie._Z_PICTURECD);
            
            int bitrate = fpm.getVideoBitRate();
            if (bitrate <= 0) {
                MovieFile mf = new MovieFile(filename);
                bitrate = (int) mf.getVideoBitrate();
            }

            movie.setValue(Movie._W_VIDEOBITRATE, Long.valueOf(bitrate));

        } catch (Exception exp) {
            getClient().addMessage(DcResources.getText("msgCouldNotReadInfoFrom", filename));
        }
        
        return movie;
    }
}
