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

package net.datacrow.util.movie;

import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

public class FilePropertiesMovie extends FileProperties {
    
    private static final int[][] MAGIC_BYTES = { 
        { 0x00, 0x00, 0x01, 0xb3 }, // MPEG (video)
        { 0x00, 0x00, 0x01, 0xba }, // MPEG (video)
        { 0x52, 0x49, 0x46, 0x46 }, // RIFF (WAV / audio, AVI / video)
        { 0x4f, 0x67, 0x67, 0x53 }, // OGM 
        { 0x44, 0x56, 0x44, 0x56 }, // IFO (DVDV)
        { 0x1a, 0x45, 0xdf, 0xa3 }, // MKV
        { 0x30, 0x26, 0xb2, 0x75 }  // ASF (audio / video)
    };
    
    /**
     * Initializes the movie file properties. 
     * @param filename
     * @throws Exception
     */
	public FilePropertiesMovie(String filename) throws Exception {
		boolean supported = false;
		FileProperties fileProperties = null;
        RandomAccessFile dataStream = null;

		try {
		    FileProperties[] FORMATS = { new FilePropertiesMPEG(),
		            new FilePropertiesMPEG(), new FilePropertiesRIFF(),
		            new FilePropertiesOGM(), new FilePropertiesIFO(),
		            new FilePropertiesMKV(), new FilePropertiesASF()};

			dataStream = new RandomAccessFile(filename, "r");

			// gets the header for file type identification 
			int[] header = new int[4];
			for (int i = 0; i < header.length; i++)
				header[i] = dataStream.readUnsignedByte();

			// finds the right object
			int format = 0;
			for (format = 0; format < FORMATS.length; format++) {
				if (Arrays.equals(header, MAGIC_BYTES[format]))
					break;
			}

			if (format < FORMATS.length) {
				supported = true;

				fileProperties = FORMATS[format];
				fileProperties.process(dataStream, filename);

				setFilename(filename);
				
				setName(fileProperties.getName());
				setLanguage(fileProperties.getLanguage());
				setSubtitles(fileProperties.getSubtitles());
				setVideoResolution(fileProperties.getVideoResolution());
				setVideoCodec(fileProperties.getVideoCodec());
				setVideoRate(fileProperties.getVideoRate());
				setVideoBitRate(fileProperties.getVideoBitRate());
				setDuration(fileProperties.getDuration());
				setAudioCodec(fileProperties.getAudioCodec());
				setAudioRate(fileProperties.getAudioRate());
				setAudioBitRate(fileProperties.getAudioBitRate());
				setAudioChannels(fileProperties.getAudioChannels());
				setContainer(fileProperties.getContainer());
				setMetaData(fileProperties.getMetaData());
			}
			
		} catch (Exception e) {
			throw new Exception("File is corrupted. Some info may have been saved.");
		} finally {
		    if (dataStream != null) dataStream.close();
			if (!supported) throw new Exception("File format not supported.");
		}
	}

	public int getVideoWidth() {
		try {
			String width = getVideoResolution().substring(0, getVideoResolution().indexOf("x"));
			return Integer.valueOf(width);
		} catch (Exception exp) {
			return 0;
		}
	}

	public int getVideoHeight() {
		try {
			String height = getVideoResolution().substring(
					getVideoResolution().indexOf("x") + 1,
					getVideoResolution().length());
			return Integer.valueOf(height);
		} catch (Exception exp) {
			return 0;
		}
	}

	public String getMetaDataTagInfo(String tag) {
	    List<String> metaData = getMetaData();
		if (metaData != null) {
			for (int i = 0; i < metaData.size(); i++) {
				String temp = metaData.get(i);
				if (temp.startsWith(tag))
					return temp.substring(tag.length() + 1, temp.length());
			}
		}
		return "";
	}
}
