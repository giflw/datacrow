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

import java.io.FileOutputStream;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class Xml2HtmlTransformer extends XmlTransformer {
    
    @Override
    public void transform() throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        FileOutputStream fos = new FileOutputStream(target.toString());
        javax.xml.transform.Transformer transformer = 
                              factory.newTransformer(new StreamSource(template.toString()));
        transformer.transform(new StreamSource(source.toString()), 
                              new StreamResult(fos));
        
        fos.close();
    }
    
    @Override
    public int getType() {
        return XmlTransformers._HTML;
    }
    
    @Override
    public String getFileType() {
        return "html";
    }
    
    @Override
    public String toString() {
        return "Html";
    }
}
