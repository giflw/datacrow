/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                       (c) 2003 The Data Crow team                          *
 *                              info@datacrow.net                             *
 *                                                                            *
 *                                                                            *
 *       This library is free software; you can redistribute it and/or        *
 *        modify it under the terms of the GNU Lesser General Public          *
 *       License as published by the Free Software Foundation; either         *
 *     version 2.1 of the License, or (at your option) any later version.     *
 *                                                                            *
 *      This library is distributed in the hope that it will be useful,       *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU       *
 *           Lesser General Public License for more details.                  *
 *                                                                            *
 *     You should have received a copy of the GNU Lesser General Public       *
 *    License along with this library; if not, write to the Free Software     *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA   *
 *                                                                            *
 ******************************************************************************/

package net.datacrow.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.settings.DcSettings;

import org.apache.log4j.Logger;
import org.hsqldb.Token;

public class Utilities {
    
    private static Logger logger = Logger.getLogger(Utilities.class.getName());

    private static final Toolkit tk = Toolkit.getDefaultToolkit();
    private static final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private static final GraphicsDevice gs = ge.getDefaultScreenDevice();
    private static final GraphicsConfiguration gc = gs.getDefaultConfiguration();
    
    private static final Clipboard clipboard = tk.getSystemClipboard();
    
    private static final Properties languages = new Properties();
    
    private static final FileSystemView fsv = new JFileChooser().getFileSystemView();
    
    static {
        try {
            FileInputStream fis = new FileInputStream(new File(DataCrow.installationDir, "resources/languages.properties"));
            languages.load(fis);
            fis.close();
        } catch (Exception e) {
            logger.error("Could not load languages file", e);
        }
    }
    
    public static Toolkit getToolkit() {
        return tk;
    }
    
    public static Object getQueryValue(Object o, DcField field) {
        Object value = o;
        
        if (Utilities.isEmpty(value))
            value = null;
        else if (value instanceof DcObject)
            value = ((DcObject) value).getID();
        else if ((field.getValueType() == DcRepository.ValueTypes._BIGINTEGER ||
                  field.getValueType() == DcRepository.ValueTypes._LONG) &&
                 value instanceof String)
            value = Long.valueOf((String) value);

        return value;
    }  
    
    public static String getMappedFilename(String filename) {
        String s = filename;
        
        if (s != null) {
            String[] mappings = DcSettings.getStringArray(DcRepository.Settings.stDriveMappings);
            if (mappings != null) {
                for (String mapping : mappings) {
                    StringTokenizer st = new StringTokenizer(mapping, "/&/");
                    String drive = (String) st.nextElement();
                    String mapsTo = (String) st.nextElement();
                    
                    if (s.length() > drive.length() && s.substring(0, drive.length()).equalsIgnoreCase(drive)) {
                        s = mapsTo + s.substring(drive.length());
                        break;
                    }
                }
            }
        }
        
        return s;
    }
    
    public static String getOriginalFilename(String filename) {
        String s = filename;
        
        if (s != null) {
            String[] mappings = DcSettings.getStringArray(DcRepository.Settings.stDriveMappings);
            if (mappings != null) {
                for (String mapping : mappings) {
                    StringTokenizer st = new StringTokenizer(mapping, "/&/");
                    String mapsTo = (String) st.nextElement();
                    String drive = (String) st.nextElement();
                    
                    if (s.length() > drive.length() && s.substring(0, drive.length()).equalsIgnoreCase(drive)) {
                        s = mapsTo + s.substring(drive.length());
                        break;
                    }
                }
            }
        }
        
        return s;
    }

    public static boolean isKeyword(String name) {
        String s = name.toUpperCase();
        return Token.isKeyword(s) || s.equals("CREATE") || s.equals("ALTER") || s.equals("SELECT") ||
               s.equals("DROP") || s.equals("TRUNCATE") || s.equals("MODIFY") || s.equals("TABLE") || s.equals("COLUMN");        
    }
    
    public static DcImageIcon getImageFromClipboard() {
        Transferable clipData = clipboard.getContents(clipboard);
        if (clipData != null) {
            if (clipData.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                try {
                    Image image = (Image) clipData.getTransferData(DataFlavor.imageFlavor);
                    return new DcImageIcon(Utilities.getBytes(new DcImageIcon(image)));
                } catch (Exception ignore) {}
            }
        }
        return null;
    }
    
    public static String getLanguage(String iso) {
        return (String) languages.get(iso);
    }
    
    /**
     * Returns a centered location for a window / form / dialog 
     * @param windowSize size of the window
     * @return centered location
     */
    public static Point getCenteredWindowLocation(Dimension windowSize, boolean main) {
        main = main || DataCrow.mainFrame == null;
        
        Dimension dim;
        if (main) {
            dim = tk.getScreenSize();
            dim.height = (dim.height - windowSize.height) / 2;
            dim.width = (dim.width - windowSize.width) / 2;
        } else {
            // relative to the mainframe
            dim = tk.getScreenSize();
            Point p = DataCrow.mainFrame.getLocation();
            dim.height = (p.y) + ((DataCrow.mainFrame.getSize().height - windowSize.height )  / 2);
            dim.width = (p.x) + ((DataCrow.mainFrame.getSize().width - windowSize.width ) / 2);
        }

        return new Point(dim.width, dim.height);
    }
    
    public static boolean isDriveTraversable(File drive) {
        return fsv.isTraversable(drive);
    }
    
    public static boolean canRead(File drive) {
        return fsv.isTraversable(drive) && drive.canRead();        
    }
    
    public static boolean isSystemDrive(File drive) {
    	return getSystemDrives().contains(drive);
    }
    
    public static Collection<File> getSystemDrives() {
        Collection<File> drives = new ArrayList<File>();
        for (File file : File.listRoots())
            drives.add(file);
        return drives;
    }
    
    public static Collection<File> getDrives() {
        Collection<File> drives = getSystemDrives();
        String[] dirs = DcSettings.getStringArray(DcRepository.Settings.stDirectoriesAsDrives);
        
        if (dirs != null) {
            for (String dir: dirs)
                drives.add(new File(dir));
        }
        
        return drives;
    }
    
    public static String getSystemName(File f) {
    	return fsv.getSystemDisplayName(f);
    }
    
    public static boolean sameImage(byte[] img1, byte[] img2) {
        boolean same = img1.length == img2.length;
        if (same) {
            for (int i = 0; i < img1.length; i++) {
                same = img1[i] == img2[i];
                if (!same)
                    break;
            }
        }
        return same;
    }    
    
    public static Collection<String> getCharacterSets() {
        Collection<String> characterSets = new ArrayList<String>(); 
        for (String name :  Charset.availableCharsets().keySet()) {
            characterSets.add(name);
        }
        return characterSets;
    }
    
    public static String toFileSizeString(Long l) {
        if (l == null) return "";
        
        String s = DcSettings.getString(DcRepository.Settings.stDecimalGroupingSymbol);
        char groupingChar = s != null && s.length() > 0 ? s.charAt(0) : ',';

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator(groupingChar);
        symbols.setInternationalCurrencySymbol("EUR");

        DecimalFormat format = new DecimalFormat("###,###", symbols);
        format.setGroupingSize(3);
        return format.format(l) + " bytes";
    }
    
    public static String toString(Double d) {
        if (d == null) return "";
        
        String s = DcSettings.getString(DcRepository.Settings.stDecimalSeparatorSymbol);
        char decimalSep = s != null && s.length() > 0 ? s.charAt(0) : ',';
        s = DcSettings.getString(DcRepository.Settings.stDecimalGroupingSymbol);
        char groupingSep = s != null && s.length() > 0 ? s.charAt(0) : '.';
        
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator(decimalSep);
        symbols.setGroupingSeparator(groupingSep);
        
        
        DecimalFormat format = new DecimalFormat("###,###.00", symbols);
        format.setGroupingSize(3);
        return format.format(d);
    }

    public static Long getSize(File file) {
        return Long.valueOf(file.length());  
    }
    
    /**
     * Creates a unique ID. Can be used for custom IDs in the database.
     * Based on date / time + random number
     * @return unique ID as String
     */
    public static String getUniqueID() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Retrieved the file extension of a file
     * @param f file to get the extension from
     * @return extension or empty string
     */
    public static String getExtension(File f) {
        String name = f.getName().toLowerCase();
        int i = name.lastIndexOf( "." );
        if (i == -1) {
            return "";
        }
        return name.substring( i + 1 );
    }    
    
    public static int getIntegerValue(String s) {
        char[] characters = s.toCharArray();
        String test = "";
        for (int i = 0; i < characters.length; i++) {
            if (Character.isDigit(characters[i])) test += "" + characters[i];
        }
        
        int number = 0;
        try {
            number = Integer.valueOf(test).intValue();
        } catch (Exception ignore) {}
        
        return number;
    }
   
    /**
     * Reads the content of a file (fully)
     * @param file file to retrieve the content from
     * @return content of the file as a byte array
     * @throws Exception
     */
    public static byte[] readFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(is);
        
        long length = file.length();
        if (length > Integer.MAX_VALUE) 
            throw new IOException("File is too large to read " + file.getName());
    
        byte[] bytes = new byte[(int)length];
    
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=bis.read(bytes, offset, bytes.length-offset)) >= 0)
            offset += numRead;
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length)
            throw new IOException("Could not completely read file " + file.getName());
    
        bis.close();
        is.close();
        
        return bytes;    
    }

    public static DcImageIcon base64ToImage(String base64) {
        byte[] bytes = Base64.decode(base64.toCharArray());
        return new DcImageIcon(bytes, false);
    }
    
    public static byte[] getBytes(DcImageIcon icon) {
        return getBytes(icon, DcImageIcon._TYPE_PNG);
    }
    
    public static byte[] getBytes(DcImageIcon icon, int type) {
        return getBytes(icon.getImage(), type);
    }
    
    public static byte[] getBytes(Image image, int type) {
    	BufferedImage bi;
    	if (image instanceof BufferedImage)
    		bi = (BufferedImage) image;
    	else 
    		bi = Utilities.toBufferedImage(new DcImageIcon(image), -1, -1);
    	
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        
        byte[] bytes = null;
        try {
            ImageIO.write(bi, (type == DcImageIcon._TYPE_JPEG ? "JPG" : "PNG"), bos);
            bos.flush();
            bytes = baos.toByteArray();
            bi.flush();
            image.flush();
        } catch (IOException e) {
            logger.error(e, e);
        } 
        
        try {
            baos.close();
            bos.close();
        } catch (IOException e) {
            logger.error(e, e);
        }
        
        return bytes;
    }
    
    public static void writeToFile(DcImageIcon icon, String filename) throws Exception {
        writeScaledImageToFile(icon, filename, DcImageIcon._TYPE_PNG, -1, -1);
    }   

    public static void writeToFile(byte[] b, String filename) throws Exception {
        writeToFile(b, new File(filename));
    } 
    
    public static void writeToFile(byte[] b, File file) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        bos.write(b);
        bos.flush();
        bos.close();
    }   
    
    public static Image getScaledImage(byte[] bytes) {
        return getScaledImage(bytes, 190, 145);
    }

    public static Image getScaledImage(DcImageIcon icon) {
        return getScaledImage(icon, 190, 145);
    }    

    public static Image getScaledImage(byte[] bytes, int width, int height) {
        return toBufferedImage(new DcImageIcon(bytes), width, height);
    }    
    
    public static Image getScaledImage(DcImageIcon icon, int width, int height) {
        return toBufferedImage(icon, width, height);
    }    
    
    public static void writeScaledImageToFile(DcImageIcon icon, String filename) throws Exception {
    	writeScaledImageToFile(icon, filename, DcImageIcon._TYPE_PNG, 190, 145);
    }

    public static void writeScaledImageToFile(DcImageIcon icon, String filename, int type, int w, int h) throws Exception {
        BufferedImage bufferedImage = toBufferedImage(icon, w, h);
        ImageIO.write(bufferedImage, (type == DcImageIcon._TYPE_JPEG ? "JPG" : "PNG"), new File(filename));
        bufferedImage.flush();
    }       
    
    public static String getHexColor(Color color) {
        String hexColor = "#" + Integer.toHexString(color.getRed());
        hexColor += Integer.toHexString(color.getGreen());
        hexColor += Integer.toHexString(color.getBlue()); 
        return hexColor.toUpperCase();
    }
    
    public static String toHex(byte in[]) {
        byte ch = 0x00;
        int i = 0; 

        String pseudo[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8",
                           "9", "A", "B", "C", "D", "E", "F"};

        StringBuffer out = new StringBuffer(in.length * 2);
        while (i < in.length) {

            ch = (byte) (in[i] & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4);     // shift the bits down
            ch = (byte) (ch & 0x0F);    // must do this is high order bit is on!
            out.append(pseudo[ch]);

            ch = (byte) (in[i] & 0x0F); // Strip off low nibble 
            out.append(pseudo[ch]);
            
            i++;
        }

        return out.toString();
    }       
    
    public static boolean isEmpty(Object o) {
        boolean empty = o == null || o.equals(Long.valueOf(-1)) || o.equals(Long.valueOf(0));
        if (!empty && o instanceof String)
            empty = ((String) o).trim().length() == 0;
        else if (!empty && o instanceof Collection)
            empty = ((Collection) o).size() == 0;
        else if (!empty && o instanceof Picture)
            empty = !((Picture) o).hasImage();
        
        return empty;
    }

    public static String getComparableString(Object o) {
        return Utilities.isEmpty(o) ? "" : o instanceof String ? ((String) o) : o.toString();
    }
    
    public static void rename(File currentFile, File newFile) throws IOException {

        if (newFile.getParentFile() != null)
            newFile.getParentFile().mkdirs();
        
        boolean success = currentFile.renameTo(newFile);
        
        if (!success) {
            // native code failed to move the file; do it the custom way
            FileInputStream fis = new FileInputStream(currentFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
        
            FileOutputStream fos = new FileOutputStream(newFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            
            int count = 0;
            int b;
            while ((b = bis.read()) > -1) {
                bos.write(b);
                count++;
                if (count == 2000) {
                    bos.flush();
                    count = 0;
                }
            }
            
            bos.flush();
            
            bis.close();
            bos.close();
            
            currentFile.delete();
        }
    }

    public static String getCurrentDirectory() throws Exception {
    	File fl = new File(".");
    	fl = fl.getCanonicalFile();
        return fl.toString();
    }
    
    public static String getHtmlStyle() {
        return getHtmlStyle(null, null);
    }
    
    public static String getHtmlStyle(Color bg) {
        return getHtmlStyle(null, bg);
    }
    
    public static String getHtmlStyle(String additionalStyleInfo) {
        return getHtmlStyle(additionalStyleInfo, null);
    }
    
    public static String getHtmlStyle(String additionalStyleInfo, Color bg) {
        Font font = DcSettings.getFont(DcRepository.Settings.stSystemFontNormal);
        Color color = ComponentFactory.getCurrentForegroundColor();
        String foreground = Utilities.getHexColor(color);
        
        String fontstyle = "";
        if (font.isItalic())
            fontstyle = ";font-style:italic";
        else if (font.isBold())
            fontstyle = ";font-weigth:bolder";
        
        StringBuffer sb = new StringBuffer();
        sb.append("style=\"");
        sb.append("font-family:");
        sb.append(font.getFamily());
        sb.append(";font-size:");
        sb.append(font.getSize());
        sb.append(fontstyle);
        
        if (bg != null) {
            String background = Utilities.getHexColor(bg);
            sb.append(";background:");
            sb.append(background);
        }
        
        if (additionalStyleInfo != null) {
            if (!additionalStyleInfo.startsWith(";"))
                sb.append(";");
            
            sb.append(additionalStyleInfo);
        }
            
        sb.append(";color:");
        sb.append(foreground);
        sb.append(";\"");
        
        return sb.toString();
    }
    
    /**
     * Gets the content of a file and converts it to a base64 string
     * @param url URL of file
     * @return base64 content of the file
     */
    public static String fileToBase64String(File file) {
        try {
            byte[] b = Utilities.readFile(file);
            file = null;
            return String.valueOf(Base64.encode(b));
        } catch (Exception e) {
            logger.error("Error while converting content from " + file + " to base64", e);
        }
        return "";
    }
    
    public static BufferedImage toBufferedImage(ImageIcon icon) {
        return toBufferedImage(icon, -1, -1);
    }  
    
    public static BufferedImage toBufferedImage(ImageIcon icon, int width, int height) {
        // make sure the image is loaded
        Image image = new DcImageIcon(icon.getImage()).getImage();
        
        int imgW = image.getWidth(null);
        int imgH = image.getHeight(null);
        
        int w = width > 0 ? width : imgW;
        int h = height > 0 ? height : imgH;
        
        if (imgW <= width && imgH <= height) {
            // do not scale down if not needed
            w = imgW;
            h = imgH;
        } else {
            // make sure the image ratio remains the same
            double scaledRatio = (double) w / (double) h;
            double imageRatio = (double) imgW / (double) imgH;
            if (scaledRatio < imageRatio)
                h = (int) (w / imageRatio);
            else
                w = (int) (h * imageRatio);
        }
        
        BufferedImage bi = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        
        Graphics g = bi.createGraphics();
        DcSwingUtilities.setRenderingHint(g);
        g.drawImage(image, 0, 0, w, h, null);
        g.dispose();
        
        bi.flush();
        image.flush();
        
        return bi;
    }
    
}
