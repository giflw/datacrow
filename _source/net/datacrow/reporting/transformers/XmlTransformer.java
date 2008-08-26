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

package net.datacrow.reporting.transformers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.util.Collection;

import net.datacrow.console.windows.reporting.ReportingDialog;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.reporting.reports.XmlReport;
import net.datacrow.reporting.templates.ReportTemplate;
import net.datacrow.reporting.templates.ReportTemplateProperties;

import org.apache.log4j.Logger;

public abstract class XmlTransformer {
    
    private static Logger logger = Logger.getLogger(XmlTransformer.class.getName());
    
    protected File source;
    protected File target;
    protected File template;
    
    protected Collection<DcObject> objects;
    protected ReportingDialog dialog;
    protected BufferedOutputStream bos;
    
    private ReportTemplateProperties properties;
    private XmlReport xmlReport;

    protected boolean keepOnRunning = true;

    public XmlTransformer() {}
    
    public void transform(  ReportingDialog dialog, 
                            Collection<DcObject> objects, 
                            File target, 
                            ReportTemplate reportFile) {
        
        this.dialog = dialog;
        this.objects = objects;
        
        this.target = target;
        this.template = new File(reportFile.getFilename());
        this.properties = reportFile.getProperties();
        
        String s = target.toString();
        s = s.substring(0, s.lastIndexOf(".")) + ".xml";
        this.source = new File(s);
        
        Transformer rt = new Transformer();
        rt.start();
    }
    
    public void cancel() {
        keepOnRunning = false;
        if (xmlReport != null)
            xmlReport.cancel();
    }    
    
    public abstract int getType();
    public abstract void transform() throws Exception ;
    public abstract String getFileType();
    
    private class Transformer extends Thread {
        
        @Override
        public void run() {

            keepOnRunning = true;
            
            xmlReport = new XmlReport(properties);
            xmlReport.compile(dialog, objects, source);
            xmlReport.join();

            try {

                if (xmlReport.isSuccessfull() && keepOnRunning) {
                    dialog.allowActions(false);
                    dialog.addMessage(DcResources.getText("msgTransformingOutput", getFileType()));
                    transform();
                    dialog.addMessage(DcResources.getText("msgTransformationSuccessful", target.toString()));
                }

            } catch (Exception exp) {
                logger.error(DcResources.getText("msgErrorWhileCreatingReport", exp.toString()), exp);
                dialog.addMessage(DcResources.getText("msgErrorWhileCreatingReport", exp.toString()));
            } finally {
                source = null;
                template = null;
                objects = null;
                target = null;
                bos = null;
                dialog.allowActions(true);
                dialog = null;
            }
        }
    }
}
