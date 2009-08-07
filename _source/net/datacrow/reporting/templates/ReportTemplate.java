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

package net.datacrow.reporting.templates;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import net.datacrow.core.migration.itemexport.ItemExporterSettings;
import net.datacrow.reporting.transformers.XmlTransformers;

import org.apache.log4j.Logger;

public class ReportTemplate {
    
    private static Logger logger = Logger.getLogger(ReportTemplate.class.getName());
    
    private String name;
    private String filename;
    private int[] transformers;
    private ItemExporterSettings properties;
    
    public ReportTemplate(String filename) {
        this.filename = filename;
        
        int start = filename.lastIndexOf(File.separator) > -1 ? filename.lastIndexOf(File.separator)  + 1 : 0;
        int end = filename.lastIndexOf(".") < start ? filename.length() : filename.lastIndexOf(".");
        
        this.properties = new ItemExporterSettings(filename);
        name = properties.get(ItemExporterSettings._NAME);
        if (name == null || name.trim().length() == 0)
            name = filename.substring(start, end).replaceAll("[_.]", " ");
        
        setTransformers();
    }
    
    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }
    
    public File getFile() {
        return new File(filename);
    }
    
    public ItemExporterSettings getProperties() {
        return properties;
    }
    
    public boolean supports(int transformer) {
        for (int i = 0; i < transformers.length; i++) {
            if (transformers[i] == transformer)
                return true;
        }
        return false;
    }
    
    private void setTransformers() {
        transformers = new int[1]; 
        transformers[0] = XmlTransformers._HTML;
        
        File file = getFile();
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            long length = raf.length();
            
            while (raf.getFilePointer() < length) {
                String line = raf.readLine();
                if (line.toLowerCase().indexOf("xmlns:fo=\"http://www.w3.org/1999/xsl/format\"") > -1) {
                    transformers = new int[2];
                    transformers[0] = XmlTransformers._PDF;
                    transformers[1] = XmlTransformers._RTF;
                } 
            }
            
            raf.close();
        } catch (IOException e) {
            logger.error("Could not determine if PDF / RTF is supported for file " + file , e);
        }
    }
    
    @Override
    public String toString() {
        return getName();
    }
}

