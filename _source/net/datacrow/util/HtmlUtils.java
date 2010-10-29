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

package net.datacrow.util;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.datacrow.core.http.HttpConnection;
import net.datacrow.core.http.HttpConnectionUtil;

import org.apache.log4j.Logger;
import org.lobobrowser.html.parser.HtmlParser;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class HtmlUtils {
    
    private static Logger logger = Logger.getLogger(HtmlUtils.class.getName());
    private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder builder;
    
    static {
        try {
            builder = factory.newDocumentBuilder();
        } catch (Exception e) {
            logger.fatal("Cannot get a document builder!", e);
        }
    }

    public static Document getDocument(URL url) throws Exception {
        return getDocument(url, "ISO-8859-1");
    }
    
    public static Document getDocument(URL url, String charset) throws Exception {
        
        HttpConnection connection = HttpConnectionUtil.getConnection(url);
        
        String s = connection.getString(charset);
        connection.close();        

        Document document = getDocument(s);
        return document;
    }

    public static String toPlainText(String html) {
        return toPlainText(html, "ISO-8859-1");
    }
    
    /**
     * Clean the string of any unwanted characters
     * @param s string to clean
     */
    public static String toPlainText(String html, String charset) {
        try {
            String s = html;
            if (!s.toUpperCase().startsWith("<HTML")) {
                StringBuffer sb = new StringBuffer(s);
                sb.insert(0, "<html><body>");
                sb.append("</body></html>");
                s = sb.toString();
            }
            
            Document document = getDocument(s);
                
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xpath.evaluate("/html/body", document, XPathConstants.NODE);

            String text = node.getTextContent();
            
            while (text.length() > 1 && (text.startsWith("\r") || text.startsWith("\n")))
                text = text.substring(1);

            while (text.length() > 1 && (text.endsWith("\r") || text.endsWith("\n")))
                text = text.substring(0, text.length() - 1);
            
            return text;
            
        } catch (Exception e) {
            logger.debug("Failed to parse: " + html);
        }

        return html;
    }   
    
    protected static Document getDocument(String html) throws Exception { 
        String s = html;
        ByteArrayInputStream in;
        if (s.contains("<html") || s.contains("<HTML")) {
            String title = StringUtils.getValueBetween("<title>", "</title>", s);
            s = StringUtils.getValueBetween("<body", "</body>", s);
            s = s.substring(s.indexOf(">") + 1);
    
            // start the document
            StringBuffer sb = new StringBuffer();
            sb.append("<html>\n");
            
            // create the title part
            if (!Utilities.isEmpty(title)) {
                sb.append("<head>\n");
                sb.append("<title>");
                sb.append(title);
                sb.append("</title>\n");
                sb.append("</head>\n");
            }
            
            // create the body
            sb.append("<body>\n");
            sb.append(s);
            sb.append("</body>\n");
            sb.append("</html>\n");
            
            String[][] removeSections = {{"<script", "</script>"},
                                         {"<style", "</style>"},   
                                         {"onclick=\"", "\""},
                                         {"rel=\"", "\""},
                                         {"<!--", "-->"}};
            
            int idx;
            for (String[] sections : removeSections) {
                while((idx = sb.indexOf(sections[0])) > 0) {
                    String part1 = sb.substring(0, idx);
                    String part2 = sb.substring(sb.indexOf(sections[1], idx + sections[0].length()) + sections[1].length());
                    
                    sb.setLength(0);
                    sb.append(part1);
                    sb.append(part2);
                }
            }
            
            String[] removeWords = {"&nbsp;", " href=\"#\""};
            for (String word : removeWords) {
                while((idx = sb.indexOf(word)) > 0) {
                    String part1 = sb.substring(0, idx);
                    String part2 = sb.substring(idx + word.length());
                    
                    sb.setLength(0);
                    sb.append(part1);
                    sb.append(part2);
                }
            }
            
            s = sb.toString();
            
            //perform specific fixes
            s = s.replace("width\"", "width=\"");
            
            in = new ByteArrayInputStream(s.getBytes());
            
        } else {
            in = new ByteArrayInputStream(s.getBytes());
        }

        // construct all and create a parser
        Reader reader = new InputStreamReader(in);
        Document document = builder.newDocument();
        
        try {
            HtmlParser parser = new HtmlParser(new SimpleUserAgentContext(), document);
            parser.parse(reader);
        } catch (Exception e) {
            logger.error(e, e);
        }

        in.close();
        
        return document;
    }    
}