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

package net.datacrow.reporting.reports;

import java.io.File;
import java.io.IOException;

import net.datacrow.core.DataCrow;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.reporting.templates.ReportTemplateProperties;
import net.datacrow.reporting.writer.XmlSchemaWriter;
import net.datacrow.reporting.writer.XmlWriter;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class XmlReport extends Report {
    
    private static Logger logger = Logger.getLogger(XmlReport.class.getName());
    
    private ReportTemplateProperties properties;
    
    public XmlReport(ReportTemplateProperties properties) {
        super();
        this.properties = properties;
    }  

    @Override
    public String getFileType() {
        return "xml";
    }
    
    @Override
    public void create() throws Exception {
        initialize();
        
        String schemaFile = target.toString();
        schemaFile = schemaFile.substring(0, schemaFile.lastIndexOf(".xml")) + ".xsd";

        generateXsd(schemaFile);
        generateXml(schemaFile);
        copyStylesheet();
    }
    
    /**
     * Writes the schema file. The schema is based on the object of the current module.
     * @param schemaFile
     * @throws IOException
     */
    private void generateXsd(String schemaFile) throws Exception {
        XmlSchemaWriter schema = new XmlSchemaWriter(schemaFile);
        schema.create(DcModules.getCurrent().getDcObject());
    }
    
    private void generateXml(String schemaFile) throws Exception {
        if (objects == null || objects.size() == 0) return;
        
        XmlWriter xmlWriter = new XmlWriter(bos, target.toString(), schemaFile, properties);
        xmlWriter.startDocument();
        
        int counter = 0;
        
        for (DcObject dco : objects) {
            if (!keepOnRunning) break;
            
            if (DcModules.getCurrent().isAbstract())
                dco.reload();
            
            xmlWriter.startEntity(dco);
            dialog.addMessage(DcResources.getText("msgAddingToReport", dco.toString()));

            xmlWriter.writeAttribute(dco, DcObject._SYS_MODULE);
            
            for (DcField field : dco.getFields()) {
                if (!keepOnRunning) break;
                xmlWriter.writeAttribute(dco, field.getIndex());
            }

            if (dco.getChildren() == null || dco.getChildren().size() == 0)
                dco.loadChildren();
            
            if (dco.getModule().getChild() != null) {
                
                xmlWriter.startRelations(dco.getModule().getChild());
                xmlWriter.setIdent(2);

                if (dco.getChildren() != null) {
                    for (DcObject child : dco.getChildren()) {
                        if (!keepOnRunning) break;
                        
                        xmlWriter.startEntity(child);
                        xmlWriter.writeAttribute(child, DcObject._SYS_MODULE);
                        int[] fields = child.getFieldIndices();
                        for (int i = 0; i < fields.length; i++)
                            xmlWriter.writeAttribute(child, fields[i]);
                        
                        xmlWriter.endEntity(child);
                    }
                }
                
                xmlWriter.resetIdent();
                xmlWriter.endRelations(dco.getModule().getChild());
            } 
            
            xmlWriter.endEntity(dco);
            dialog.updateProgressBar(counter + 1);
            bos.flush();
            
            counter++;
        }
        
        xmlWriter.endDocument();
        dialog.addMessage(DcResources.getText("lblExportHasFinished"));
    }

    /**
     * copies the style sheet to the directory of the generated report.
     * The stylesheet filename as taken from the properties file is relative to the Data Crow install
     * directory. If the file cannot be copied for whatever reason a message will be placed in the
     * log file.
     */
    private void copyStylesheet() {
    	String stylesheet = properties.get(ReportTemplateProperties._STYLESHEET);
    	
    	if (stylesheet != null && stylesheet.trim().length() > 0) {
    		stylesheet = stylesheet.startsWith("/") || stylesheet.startsWith("\\") ? stylesheet.substring(1) : stylesheet;
    		String filename = DataCrow.baseDir + stylesheet;
    		File file = new File(filename);
    		
    		if (file.exists()) {
    			int idx = stylesheet.lastIndexOf("/") > 0 ? stylesheet.lastIndexOf("/") : stylesheet.lastIndexOf("\\");  
    			stylesheet = idx == -1 ? stylesheet : stylesheet.substring(idx);
    			
    			String targetFile = target.toString();
    			idx = targetFile.lastIndexOf("/") > 0 ? targetFile.lastIndexOf("/") : targetFile.lastIndexOf("\\");
    			targetFile = idx == -1 ? targetFile : targetFile.substring(0, idx);
    			
    			targetFile += stylesheet;
    			
    			try {
    				byte[] content = Utilities.readFile(file);
    				Utilities.writeToFile(content, targetFile);
    			} catch (Exception e) {
    				String msg = DcResources.getText("msgCouldNotCopyStylesheet", 
                                        new String[] {filename, targetFile});
    				logger.error(msg);
    				dialog.addMessage(msg);    			
    			}
    		}
    	}
    }
    
    @Override
    public String toString() {
        return DcResources.getText("lblXmlReport");
    }    
}
