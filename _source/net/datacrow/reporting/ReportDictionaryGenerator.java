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

package net.datacrow.reporting;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.datacrow.core.DataCrow;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.util.Converter;

import org.apache.log4j.Logger;

/**
 * Creates the report dictionary (dictionary.txt as present in the reports folder),
 * 
 * @author Robert Jan van der Waals
 */
public class ReportDictionaryGenerator {

    private static Logger logger = Logger.getLogger(ReportDictionaryGenerator.class.getName());
    
    public ReportDictionaryGenerator() {}
    
    public void generate() {
        
        String lf = "\r\n";
        
        StringBuffer sb = new StringBuffer();
        for (DcModule module : DcModules.getAllModules()) {
            if (module.isTopModule()) {
                sb.append("Module              [" + module.getName() + "]" + lf);
                sb.append("xsl reference name  [" + Converter.getValidXmlTag(module.getName()) + "]" + lf);
                
                for (int i = 0; i < 80; i++)
                    sb.append("~");
                
                sb.append(lf);
                int maxLength = 0; 
                for (DcField field : module.getFields())
                    maxLength = field.getLabel().length() > maxLength ? field.getLabel().length() : maxLength;
                
                for (DcField field : module.getFields()) {
                    int gap = maxLength - field.getLabel().length();
                    sb.append("  ");
                    sb.append(field.getLabel());
                    
                    for (int i = 0; i < gap; i++)
                        sb.append(" ");
                    
                    sb.append(" = " + Converter.getValidXmlTag(field.getSystemName()) + lf);    
                }
                
                sb.append(lf);
                sb.append(lf);
            }
        }
        
        try {
            File file = new File(DataCrow.reportDir + "dictionary.txt");
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
    
            bos.write(sb.toString().getBytes());
            bos.flush();
            bos.close();
        } catch (IOException e) {
            logger.error("Cannot save report dictionary to " + DataCrow.reportDir + "dictionary.txt", e);
        }
    }
}
