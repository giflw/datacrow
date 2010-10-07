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

import java.util.ArrayList;
import java.util.Collection;

public abstract class XmlTransformers {

    public static final int _PDF = 0;
    public static final int _HTML = 1;
    public static final int _RTF = 2;
    
    private static Collection<XmlTransformer> transformers = new ArrayList<XmlTransformer>(); 
    
    static {
        transformers.add(new Xml2HtmlTransformer());
        transformers.add(new Xml2PdfTransformer());
        transformers.add(new Xml2RtfTransformer());
    }
    
    public static Collection<XmlTransformer> getTransformers() {
        return transformers;
    }
}
