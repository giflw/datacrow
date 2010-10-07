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
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import net.datacrow.core.migration.itemexport.ItemExporterSettings;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;

public class Xml2PdfTransformer extends XmlTransformer implements ErrorListener {
    
    private static Logger logger = Logger.getLogger(XmlTransformer.class.getName());
    
    @Override
    public void transform() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance();
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        OutputStream out = new FileOutputStream(target);
        out = new BufferedOutputStream(out);
        
        try {
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
            TransformerFactory factory = TransformerFactory.newInstance();
            
            javax.xml.transform.Transformer transformer = 
                    factory.newTransformer(new StreamSource(template));
            
            transformer.setErrorListener(this);
            transformer.setParameter("versionParam", "1.0");
            transformer.transform(new StreamSource(source), new SAXResult(fop.getDefaultHandler()));
        } finally {
            out.close();
        }
    }
    
    @Override
    protected void setSettings(ItemExporterSettings properties) {
        properties.set(ItemExporterSettings._ALLOWRELATIVEIMAGEPATHS, Boolean.FALSE);
    }



    @Override
    public int getType() {
        return XmlTransformers._PDF;
    }
    
    @Override
    public String getFileType() {
        return "pdf";
    }
    
    @Override
    public String toString() {
        return "Pdf";
    }

    @Override
    public void error(TransformerException e) {
        client.notifyMessage(e.getMessage());
        logger.error(e, e);
    }

    @Override
    public void fatalError(TransformerException e) {
        client.notifyMessage(e.getMessage());
        logger.error(e, e);
    }

    @Override
    public void warning(TransformerException e) {
        client.notifyMessage(e.getMessage());
        logger.error(e, e);
    }
}
