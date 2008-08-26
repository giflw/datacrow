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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.StringTokenizer;

abstract class FileProperties {

    private String filename = "";
    private String language = "";
    private String name = "";
    private String subtitles = "";
    private String videoResolution = "";
    private String videoCodec = "";
    private int videoRate = 0;
    private int videoBitRate = 0;
    private int duration = -1;
    private String audioCodec = "";
    private int audioRate = 0;
    private int audioBitRate = 0;
    private int audioChannels = 0;
    private String container = "";
    private String mediaType = "";
    
    private List<String> metaData;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the subtitles.
     */
    public String getSubtitles() {
        return subtitles;
    }

    /**
     * Returns the resolution.
     */
    public String getVideoResolution() {
        return videoResolution;
    }

    /**
     * Returns the video codec.
     */
    public String getVideoCodec() {
        return videoCodec;
    }

    /**
     * Returns the video rate.
     */
    public int getVideoRate() {
        return videoRate;
    }

    /**
     * Returns the video bit rate.
     */
    public int getVideoBitRate() {
        return videoBitRate;
    }

    /**
     * Returns the duration.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns the audio codec.
     */
    public String getAudioCodec() {
        return audioCodec;
    }

    /**
     * Returns the audio rate.
     */
    public int getAudioRate() {
        return audioRate;
    }

    /**
     * Returns the audio bit rate.
     */
    public int getAudioBitRate() {
        return audioBitRate;
    }

    /**
     * Returns the audio channels.
     */
    public int getAudioChannels() {
        return audioChannels;
    }

    /**
     * Sets the subtitles.
     */
    protected void setSubtitles(String subtitles) {
        this.subtitles = subtitles;
    }

    /**
     * Sets the resolution.
     */
    protected void setVideoResolution(String videoResolution) {
        this.videoResolution = videoResolution;
    }

    /**
     * Sets the video codec (video handler).
     */
    protected void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }

    /**
     * Sets the video rate.
     */
    protected void setVideoRate(int videoRate) {
        this.videoRate = videoRate;
    }

    /**
     * Sets the video bit rate.
     */
    protected void setVideoBitRate(int videoBitRate) {
        this.videoBitRate = videoBitRate;
    }

    /**
     * Sets the duration.
     */
    protected void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Sets the audio Codec (auda handler).
     */
    protected void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    /**
     * Sets the audio rate.
     */
    protected void setAudioRate(int audioRate) {
        this.audioRate = audioRate;
    }

    /**
     * Sets the audio bit rate.
     */
    protected void setAudioBitRate(int audioBitRate) {
        this.audioBitRate = audioBitRate;
    }

    /**
     * Sets the audio channels.
     */
    protected void setAudioChannels(int audioChannels) {
        this.audioChannels = audioChannels;
    }

    /**
     * Sets the container.
     */
    protected void setContainer(String container) {
        this.container = container;
    }

    /**
     * Returns the container.
     */
    protected String getContainer() {
        return this.container;
    }

    /**
     * Sets the Media Type.
     */
    protected void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Returns the Media Type.
     */
    protected String getMediaType() {
        return this.mediaType;
    }

    /**
     * Sets the meta data ArrayList.
     */
    protected void setMetaData(List<String> metaData) {
        this.metaData = metaData;
    }

    /**
     * Returns the meta data ArrayList.
     */
    protected List<String> getMetaData() {
        return metaData;
    }

    /**
     * Processes a file from the given DataInputStream.
     */
    protected void process(RandomAccessFile dataStream, String filename) throws Exception {}

    /**
     * Reads an unsigned 8-bit integer.
     */
    protected int readUnsignedByte(byte[] b, int offset) throws Exception {
        return b[offset];
    }

    /**
     * Reads an unsigned 16-bit integer.
     */
    protected int readUnsignedInt16(byte[] b, int offset) throws Exception {
        return (b[offset] | (b[offset + 1] << 8));
    }

    /**
     * Reads an unsigned 32-bit integer.
     */
    protected int readUnsignedInt32(byte[] b, int offset) throws Exception {
        return (readUnsignedInt16(b, offset) | (readUnsignedInt16(b, offset + 2) << 16));
    }

    /**
     * Returns a 16-bit integer.
     */
    protected int getUnsignedInt16(int byte1, int byte2) throws Exception {
        return (byte2 | (byte1 << 8));
    }

    /**
     * Returns a 16-bit integer.
     */
    protected int getUnsignedInt16(byte byte1, byte byte2) throws Exception {
        return byte2 | byte1 << 8;
    }

    /**
     * Returns an unsigned 32-bit integer.
     */
    protected int getUnsignedInt32(byte byte1, byte byte2) throws Exception {
        return byte2 | byte1 << 16;
    }

    /**
     * Returns an unsigned 32-bit integer.
     */
    protected int getUnsignedInt32(int byte1, int byte2) throws Exception {
        return (byte1 | byte2 << 16);
    }

    /**
     * Reads an unsigned byte and returns its int representation.
     */
    protected int readUnsignedByte(RandomAccessFile dataStream) throws Exception {
        int data = dataStream.readUnsignedByte();
        if (data == -1) {
            throw new Exception("Unexpected end of stream.");
        }
        return data;
    }

    /**
     * Reads n unsigned bytes and returns it in an int[n].
     */
    protected int[] readUnsignedBytes(RandomAccessFile dataStream, int n) throws Exception {
        int[] data = new int[n];
        for (int i = 0; i < data.length; i++) {
            data[i] = readUnsignedByte(dataStream);
        }
        return data;
    }

    /**
     * Reads an unsigned 16-bit integer.
     */
    protected int readUnsignedInt16(RandomAccessFile dataStream) throws Exception {
        return (readUnsignedByte(dataStream) | (readUnsignedByte(dataStream) << 8));
    }

    /**
     * Reads an unsigned 32-bit integer.
     */
    protected int readUnsignedInt32(RandomAccessFile dataStream) throws Exception {
        return (readUnsignedInt16(dataStream) | (readUnsignedInt16(dataStream) << 16));
    }

    /**
     * Discards n bytes.
     */
    protected void skipBytes(RandomAccessFile dataStream, int n) throws Exception {
        readUnsignedBytes(dataStream, n);
    }

    /**
     * Reverses the byte order
     */
    protected int changeEndianness(int num) {
        return (num >>> 24) | (num << 24) | ((num << 8) & 0x00FF0000 | ((num >> 8) & 0x0000FF00));
    }

    /**
     * Returns the ASCII value of id
     */
    protected String fromByteToAscii(int j, int numberOfBytes) throws Exception {
        int id = j;
        StringBuffer buffer = new StringBuffer(4);

        for (int i = 0; i < numberOfBytes; i++) {
            int c = id & 0xff;
            buffer.append((char) c);
            id >>= 8;
        }
        return new String(buffer);
    }

    /**
     * Returns the decimal value of a specified number of bytes from a specific
     * part of a byte.
     */
    protected int getDecimalValue(int[] bits, int start, int stop, boolean printBits) {
        String dec = "";
        for (int i = start; i >= stop; i--) {
            dec += bits[i];
        }

        return Integer.parseInt(dec, 2);
    }

    /**
     * Returns an array containing the bits from the value.
     */
    protected int[] getBits(int value, int numberOfBytes) {

        int[] bits = new int[numberOfBytes * 8];

        for (int i = bits.length - 1; i >= 0; i--) {
            bits[i] = (value >>> i) & 1;
        }
        return bits;
    }

    /**
     * Searches in the inputStream stream the name following the string id
     * (separated by a \t).
     */
    protected String findName(InputStream stream, String id) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = null;

        while ((line = reader.readLine()) != null) {
            if (line.length() > 0) {
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                if (tokenizer.countTokens() > 0
                        && id.compareToIgnoreCase(tokenizer.nextToken()) == 0) {
                    return tokenizer.nextToken();
                }
            }
        }
        return "";
    }
}
