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
import java.io.IOException;
import java.util.Collection;

import net.datacrow.core.migration.itemexport.IItemExporterClient;
import net.datacrow.core.migration.itemexport.ItemExporter;
import net.datacrow.core.migration.itemexport.ItemExporterSettings;
import net.datacrow.core.migration.itemexport.ItemExporters;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.reporting.templates.ReportTemplate;

import org.apache.log4j.Logger;

public abstract class XmlTransformer {
    
    private static Logger logger = Logger.getLogger(XmlTransformer.class.getName());
    
    protected File source;
    protected File target;
    protected File template;
    
    protected Collection<DcObject> objects;
    protected IItemExporterClient client;
    protected BufferedOutputStream bos;
    
    private ItemExporterSettings settings;
    private ItemExporter exporter;

    protected boolean canceled = false;

    public XmlTransformer() {}
    
    public void transform(IItemExporterClient client, 
                          Collection<DcObject> objects, 
                          File target, 
                          ReportTemplate reportFile) {
        
        this.client = client;
        this.objects = objects;
        
        this.target = target;
        this.template = new File(reportFile.getFilename());
        this.settings = reportFile.getProperties();
        
        setSettings(settings);
        
        String s = target.toString();
        s = s.substring(0, s.lastIndexOf(".")) + ".xml";
        this.source = new File(s);
        
        Transformer rt = new Transformer();
        rt.start();
    }
    
    public void cancel() {
        canceled = true;
        if (exporter != null)
            exporter.cancel();
    }    
    
    public abstract int getType();
    public abstract void transform() throws Exception ;
    public abstract String getFileType();
    
    protected void setSettings(ItemExporterSettings properties) {}
    
    private class Transformer extends Thread {
        
        @Override
        public void run() {
            try {
                canceled = false;
                
                // export the items to an XML file
                ItemExporter exporter = ItemExporters.getInstance().getExporter("XML", DcModules.getCurrent().getIndex(), ItemExporter._MODE_NON_THREADED);
                exporter.setSettings(settings);
                exporter.setFile(source);
                exporter.setClient(client);
                exporter.setItems(objects);
                exporter.start();

                // create the report
                if (exporter.isSuccessfull() && !canceled) {
                    client.notifyMessage(DcResources.getText("msgTransformingOutput", getFileType()));
                    transform();
                    client.notifyMessage(DcResources.getText("msgTransformationSuccessful", target.toString()));
                }

            } catch (Exception exp) {
                logger.error(DcResources.getText("msgErrorWhileCreatingReport", exp.toString()), exp);
                client.notifyMessage(DcResources.getText("msgErrorWhileCreatingReport", exp.toString()));
            } finally {
                if (client != null) client.notifyStopped();
                
                try {
                    source.delete();
                    String name = source.getName();
                    name = name.lastIndexOf(".") > -1 ? name.substring(0, name.lastIndexOf(".")) : name;
                    new File(source.getParent(), name).delete();
                    new File(source.getParent(), name + ".xsd").delete();
                } catch (Exception ignore) {
                    logger.debug("Could not cleanup reporting files.", ignore);
                }
                
                source = null;
                template = null;
                objects = null;
                target = null;
                client = null;
                settings = null;
                exporter = null;
                


                try {
                    if (bos != null) bos.close();
                } catch (IOException ignore) {}
                
                bos = null;                   
            }
        }
    }
}
