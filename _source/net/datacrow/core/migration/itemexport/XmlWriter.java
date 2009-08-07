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

package net.datacrow.core.migration.itemexport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;

import net.datacrow.core.DataCrow;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class XmlWriter extends XmlBaseWriter {
    
    private static Logger logger = Logger.getLogger(XmlWriter.class.getName());
    
    private ItemExporterSettings settings;
    
    private int tagIdent;
    private int valueIdent;
    
    private String schemaFile;
    private final int stepSize = 4;
    
    private final String reportName;
    private final String baseDir;
    
    public XmlWriter(String filename, String schemaFile, ItemExporterSettings properties) throws IOException {
        this(new BufferedOutputStream(new FileOutputStream(filename)), filename, schemaFile, properties);
    }
    
    public XmlWriter(BufferedOutputStream bos, String filename, String schemaFile, ItemExporterSettings properties) {
        super(bos);
        
        this.baseDir = filename.substring(0, filename.lastIndexOf(File.separator) + 1);
        this.reportName = filename.substring(filename.lastIndexOf(File.separator) + 1, filename.lastIndexOf("."));
        this.schemaFile = schemaFile;
        this.settings = properties;
        
        resetIdent();
        
        if (properties.getBoolean(ItemExporterSettings._COPY_IMAGES))
            createImageDir();
    }    
    
    private String getImageDir() {
        return baseDir + reportName +  "_images/";
    }
    
    private void createImageDir() {
        new File(getImageDir()).mkdirs();
    }
    
    public void resetIdent() {
        tagIdent = stepSize * 1;
        valueIdent =  stepSize * 2;
    }
    
    public void setIdent(int times) {
        tagIdent =  (stepSize * (1 * times)) + stepSize;
        valueIdent = stepSize * (2 * times);
    }
    
    public void startDocument() throws IOException {
        writeTag("<?xml version=\"1.0\"?>");
        newLine();
        writeTag("<" + uberTag + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"file://" + schemaFile + "\">");
        newLine();
    }
    
    public void endDocument() throws IOException  {
        writeTag("</" + uberTag + ">");
        newLine();
        bos.close();
    }    
    
    public void startEntity(DcObject dco) throws IOException {
		ident(tagIdent);
		writeTag("<" + getValidTag(dco.getModule().getSystemObjectName()) + ">");
		newLine();
    }

    public void endEntity(DcObject dco) throws IOException {
        ident(tagIdent);
        writeTag("</" + getValidTag(dco.getModule().getSystemObjectName()) + ">");
        newLine();
    }
    
    public void writeAttribute(DcObject dco, int field) throws IOException {
        ident(valueIdent);
        String tag = getValidTag(dco.getField(field).getSystemName());
        writeTag("<" + tag + ">");
        writeValue(dco, field);
        writeTag("</" + tag + ">");
        newLine();
    }

    public void startRelations(DcModule childModule) throws IOException {
        ident(valueIdent);
        writeTag("<" + getValidTag(childModule.getObjectNamePlural()) + ">");
        newLine();
    }

    public void endRelations(DcModule childModule) throws IOException {
        ident(valueIdent);
        writeTag("</" + getValidTag(childModule.getObjectNamePlural()) + ">");
        newLine();
    }
    
    @SuppressWarnings("unchecked")
    private void writeValue(DcObject dco, int field) throws IOException {
       Object o = dco.getValue(field);
        
        if (o instanceof Collection) {
            newLine();

            tagIdent += (stepSize * 2);
            valueIdent += (stepSize * 2);
            for (Iterator iter = ((Collection) o).iterator(); iter.hasNext(); ){
                DcObject subDco = (DcObject) iter.next();
                
                if (subDco instanceof DcMapping)
                    subDco = ((DcMapping) subDco).getReferencedObject();

                if (subDco != null) { 
	                startEntity(subDco);
	                int fieldIdx = subDco.getSystemDisplayFieldIdx();
	                writeAttribute(subDco, fieldIdx);
	                endEntity(subDco);
                }
            }
            valueIdent -= (stepSize * 2);
            tagIdent -= (stepSize * 2);
            ident(valueIdent);            
            
        } else if (o instanceof Picture) {
            Picture picture = (Picture) o;

            String filename = (String) picture.getValue(Picture._C_FILENAME);
            
            if (filename == null) {
                filename = "";
            } else if (settings.getBoolean(ItemExporterSettings._COPY_IMAGES)) {
                copyImage((Picture) o, getImageDir() + filename);
                
                if (settings.getBoolean(ItemExporterSettings._ALLOWRELATIVEIMAGEPATHS))
                    filename = "./" + reportName +  "_images/" + filename;
                else 
                    filename = "file:///" + getImageDir() + filename;
            } else {
                filename = "file:///" + DataCrow.imageDir + picture.getValue(Picture._C_FILENAME);
            }
            
            write(filename);
            
        } else {
            String text = dco.getDisplayString(field);
            
            int maximumLength = settings.getInt(ItemExporterSettings._MAX_TEXT_LENGTH);
            if (maximumLength > 0 && text.length() > maximumLength) {
                text = text.substring(0, maximumLength);
                text = text.substring(0, text.lastIndexOf(" ")) + "...";
            }
            
            write(text);
        }
    }
    
    private void copyImage(Picture picture, String target) {
        try {
            picture.loadImage();
            ImageIcon icon = (ImageIcon) picture.getValue(Picture._D_IMAGE);

            if (icon != null) {
                if (settings.getBoolean(ItemExporterSettings._SCALE_IMAGES)) {
                    int width = settings.getInt(ItemExporterSettings._IMAGE_WIDTH);
                    int height = settings.getInt(ItemExporterSettings._IMAGE_HEIGHT);
                    Utilities.writeToFile(new DcImageIcon(Utilities.toBufferedImage(icon, DcImageIcon._TYPE_JPEG, width, height)), target);
                } else {
                    Utilities.writeToFile(icon, target);
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred while copying image to " + target, e);
        }
    }
    
    private void ident(int x) throws IOException {
        String s = "";
        for (int i = 0; i < x; i++)
            s += " ";
        bos.write(s.getBytes());
    }    
    
    private void write(String value) throws IOException {
        String s = value;
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("\"", "&quot;");
        s = s.replaceAll("'", "&apos;");
        bos.write(s.getBytes("UTF8"));
    }    
}
