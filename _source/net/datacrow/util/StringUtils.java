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

import java.io.StringReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;

public class StringUtils {

    private static Logger logger = Logger.getLogger(StringUtils.class.getName());
    
    private static final Collator collator = Collator.getInstance(Locale.FRANCE);

    private static final Pattern[] normalizer = {
        Pattern.compile("('|~|\\!|@|#|\\$|%|\\^|\\*|_|\\[|\\{|\\]|\\}|\\||\\\\|;|:|`|\"|<|,|>|\\.|\\?|/|&|_|-)"),
        Pattern.compile("^(de|het|een|the|a|an|el|le|les|la|los|die|der|das|den) "),
        Pattern.compile(" (de|het|een|the|a|an|el|le|les|la|los|die|der|das|den) "),
        Pattern.compile("[(,)]")};
    
    private static final HTMLEditorKit kit = new HTMLEditorKit();

    public static String removeValuesBetween(String start, String end, String s) {
        String remove = null;
        while ((remove = StringUtils.getValueBetween(start, end, s)).length() > 0) {
            remove = start + remove + end;
            s = s.substring(0, s.indexOf(remove)) +
                s.substring(s.indexOf(remove) + remove.length());
        }
        return s;
    }
    
    public static String capitalize(String s) {
        StringBuffer sb = new StringBuffer();
        char prev = ' ';
        for (char c : s.toCharArray()) {
            if (prev == ' ')
                sb.append(Character.toUpperCase(c));
            else 
                sb.append(c);
            
            prev = c;
        }
        return sb.toString();
    }
    
    public static String getValueBetween(String start, String end, String s) {
        return getValueBetween(start, end, s, 0);
    }

    public static String getValueBetween(String start, String end, String s, int offset) {
        int indexBegin = s.indexOf(start, offset);
        if (indexBegin > -1) {
            indexBegin += start.length();
            int indexEnd = s.indexOf(end, indexBegin);
            if (indexEnd > -1) return s.substring(indexBegin, indexEnd);
        }
        return "";
    }

    public static boolean equals(String s1, String s2) {
        
        String text1 = s1 != null ? s1 : "";
        String text2 = s2 != null ? s2 : ""; 
        
        text1 = normalize(text1.toLowerCase().trim());
        text2 = normalize(text2.toLowerCase().trim());
        
        text1 = text1.replaceAll(" ", "");
        text2 = text2.replaceAll(" ", "");
        
        collator.setStrength(Collator.FULL_DECOMPOSITION);
        return collator.compare(text1, text2) == 0;
    }
    
    public static String concatUserFriendly(String s, int length) {
        if (s.length() <= length)
            return s;
        
        s = s.substring(0, length - 1);
        for (int i = s.length() - 1; i > 0; i--) {
            if (s.charAt(i) == ' ') {
                s = s.substring(0, i) + " ....";
                break;
            }
        }
        return s;
    }
    
    public static Collection<String> getValuesBetween(String start, String end, String s) {
        Collection<String> result = new ArrayList<String>();
        int startPos = 0;
        while ((startPos = s.indexOf(start, startPos)) > -1) {
            if (startPos > -1 ) {
                startPos += start.length();
                int endPos = s.indexOf(end, startPos);
                if (endPos > -1) {
                    String part = s.substring(startPos, endPos);
                    if (part.length() > 0 && !result.contains(part))
                        result.add(part);
                }
            }
        }
        return result;
    }      
    
    public static Long getContainedNumber(String s) {
        String number = "";
        for (int i = 0; i < s.length(); i++)
            number += Character.isDigit(s.charAt(i)) ? "" + s.charAt(i) : "";  
    	
        return number.length() > 0 ? Long.valueOf(number) : null;
    }
    
    public static int backtrack(String s, int start, String to) {
        String check = "";
        for (int i = start; i > 0; i--) {
            check = s.charAt(i) + check;
            if (check.startsWith(to))
                return i + to.length();
        }
        return -1;
    }
    
    public static String[] getListElements(String s, String sep) {
        StringTokenizer st = new StringTokenizer(s, sep);
        String[] result = new String[st.countTokens()];
        
        int counter = 0;
        while (st.hasMoreElements())
            result[counter++] = (String) st.nextElement(); 
        
        return result;
    }    
    
    public static String normalize(String text) {
        String s = text == null ? "" : text.trim().toLowerCase();
        
        s = removeValuesBetween("]", "[", s);
        s = removeValuesBetween("(", ")", s);
        s = removeValuesBetween("[", "]", s);

        for (int i = 0; i < normalizer.length; i++) {
            Matcher ma = normalizer[i].matcher(s);
            s = ma.replaceAll(" ");
        }
        
        s = s.replaceAll("\n", "");
        s = s.replaceAll("\r", "");
        
        s = s.replaceAll("[èéêë]","e");
        s = s.replaceAll("[ûù]","u");
        s = s.replaceAll("[ïî]","i");
        s = s.replaceAll("[àâ]","a");
        s = s.replaceAll("Ô","o");

        s = s.replaceAll("[ÈÉÊË]","E");
        s = s.replaceAll("[ÛÙ]","U");
        s = s.replaceAll("[ÏÎ]","I");
        s = s.replaceAll("[ÀÂ]","A");
        s = s.replaceAll("Ô","O");
        
        return s.trim();
    }
    
    /**
     * Clean the string of any unwanted characters
     * @param s string to clean
     */
    public static String toPlainText(String html) {
        if ((html.startsWith("\"")) && (html.endsWith("\"")))
            html = html.substring(1, html.length() - 1);

        try {
            int idx = html.indexOf("<body");
            if (idx > -1) html = getValueBetween(">", "</body>", html, idx);
        	
        	StringBuffer sb = new StringBuffer(html);
        	sb.insert(0, "<html><body>");
        	sb.append("</body></html>");
        
        	Document document = kit.createDefaultDocument();
        	StringReader sr = new StringReader(sb.toString());
        	kit.read(sr, document, 0);
        	sr.close();
        	html = document.getText(0, document.getLength());
        } catch (Exception e) {
        	logger.error("Error while trying to convert " + html, e);
        }
        
        html = replaceAll(html, "&amp;", "&");
        html = replaceAll(html, "&lt;", "<");
        html = replaceAll(html, "&gt;", ">");
        
        
        int indexBegin = html.toLowerCase().indexOf("<a href");
        int indexEnd = html.toLowerCase().indexOf("</a>");
        while (indexBegin > -1 && indexEnd > -1 && indexBegin < indexEnd) {
            html = html.substring(0, indexBegin) + " " + html.substring(indexEnd + 4, html.length());

            indexBegin = html.toLowerCase().indexOf("<a href");
            indexEnd = html.toLowerCase().indexOf("</a>");
        }
        
        html = replaceAll(html, "<hr>", "");
        html = replaceAll(html, "</hr>", "\n");
        
        html = replaceAll(html, "<h1>", "");
        html = replaceAll(html, "</h1>", "\n");

        html = replaceAll(html, "<h2>", "");
        html = replaceAll(html, "</h2>", "\n");
        
        html = replaceAll(html, "<td>", "");
        html = replaceAll(html, "</td>", "\n");

        html = replaceAll(html, "<h3>", "");
        html = replaceAll(html, "</h3>", "\n");

        html = replaceAll(html, "&nbsp;", " ");

        html = replaceAll(html, "<br />", "\n");
        html = replaceAll(html, "<br/>", "\n");
        html = replaceAll(html, "<br >", "\n");
        html = replaceAll(html, "<br>", "\n");
        
        html = replaceAll(html, "<b>", "");
        html = replaceAll(html, "</b>", "");

        html = replaceAll(html, "<i>", "");
        html = replaceAll(html, "</i>", "");
        
        html = replaceAll(html, "<li>", "");
        html = replaceAll(html, "</li>", "");

        html = replaceAll(html, "<ul>", "");
        html = replaceAll(html, "</ul>", "");
        
        html = replaceAll(html, "<p>", "");
        html = replaceAll(html, "</p>", "");
        
        html = replaceAll(html, "&#x27;", "`");
        html = replaceAll(html, "&#09;", "\t");
        html = replaceAll(html, "&#10;", "\n");
        html = replaceAll(html, "&#13;", "\r");
        html = replaceAll(html, "&#32;", "");
        html = replaceAll(html, "&#33;", "!");
        html = replaceAll(html, "&quot;", "\"");
        html = replaceAll(html, "&#34;", "\"");
        html = replaceAll(html, "&#35;", "#");
        html = replaceAll(html, "&#38;", "&");
        html = replaceAll(html, "&#39;", "`");
        html = replaceAll(html, "&#40;", "(");
        html = replaceAll(html, "&#41;", ")");
        html = replaceAll(html, "&#42;", "*");
        html = replaceAll(html, "&#43;", "+");
        html = replaceAll(html, "&#44;", ",");
        html = replaceAll(html, "&#45;", "-");
        html = replaceAll(html, "&#46;", ".");
        html = replaceAll(html, "&#47;", "/");
        html = replaceAll(html, "&#58;", ":");
        html = replaceAll(html, "&#59;", ";");
        html = replaceAll(html, "&#60;", "<");
        html = replaceAll(html, "&#61;", "=");
        html = replaceAll(html, "&#62;", ">");
        html = replaceAll(html, "&#63;", "?");        
        html = replaceAll(html, "&#64;", "@");
        html = replaceAll(html, "&#91;", "[");
        html = replaceAll(html, "&#92;", "\\");
        html = replaceAll(html, "&#93;", "]");
        html = replaceAll(html, "&#94;", "^");
        html = replaceAll(html, "&#95;", "_");
        html = replaceAll(html, "&#96;", "`");
        html = replaceAll(html, "&#123;", "{");
        html = replaceAll(html, "&#125;", "}");
        html = replaceAll(html, "&#126;", "~");
        html = replaceAll(html, "&#133;", "");
        html = replaceAll(html, "&#146;", "`");
        html = replaceAll(html, "&#150;", " ");
        html = replaceAll(html, "&#151;", " ");
        html = replaceAll(html, "&#160;", " ");
        html = replaceAll(html, "&#192;", "À");
        html = replaceAll(html, "&#192;", "À");
        html = replaceAll(html, "&#193;", "Á");
        html = replaceAll(html, "&#194;", "Â");
        html = replaceAll(html, "&#195;", "Ã");
        html = replaceAll(html, "&#196;", "Ä");
        html = replaceAll(html, "&#197;", "Å");
        html = replaceAll(html, "&#198;", "Æ");
        html = replaceAll(html, "&#199;", "Ç");
        html = replaceAll(html, "&#200;", "È");
        html = replaceAll(html, "&#201;", "É"); 
        html = replaceAll(html, "&#202;", "Ê"); 
        html = replaceAll(html, "&#203;", "Ë");
        html = replaceAll(html, "&#204;", "Ì");
        html = replaceAll(html, "&#205;", "Í"); 
        html = replaceAll(html, "&#206;", "Î"); 
        html = replaceAll(html, "&#207;", "Ï"); 
        html = replaceAll(html, "&#208;", "Ð"); 
        html = replaceAll(html, "&#209;", "Ñ"); 
        html = replaceAll(html, "&#210;", "Ò"); 
        html = replaceAll(html, "&#211;", "Ó"); 
        html = replaceAll(html, "&#212;", "Ô"); 
        html = replaceAll(html, "&#213;", "Õ"); 
        html = replaceAll(html, "&#214;", "Ö"); 
        html = replaceAll(html, "&#215;", "×"); 
        html = replaceAll(html, "&#216;", "Ø"); 
        html = replaceAll(html, "&#217;", "Ù"); 
        html = replaceAll(html, "&#218;", "Ú"); 
        html = replaceAll(html, "&#219;", "Û"); 
        html = replaceAll(html, "&#220;", "Ü"); 
        html = replaceAll(html, "&#221;", "Ý"); 
        html = replaceAll(html, "&#222;", "Þ"); 
        html = replaceAll(html, "&#223;", "ß");
        html = replaceAll(html, "&#224;", "à"); 
        html = replaceAll(html, "&#225;", "á"); 
        html = replaceAll(html, "&#226;", "â"); 
        html = replaceAll(html, "&#227;", "ã"); 
        html = replaceAll(html, "&#228;", "ä"); 
        html = replaceAll(html, "&#229;", "å"); 
        html = replaceAll(html, "&#230;", "æ"); 
        html = replaceAll(html, "&#231;", "ç"); 
        html = replaceAll(html, "&#232;", "è"); 
        html = replaceAll(html, "&#233;", "é"); 
        html = replaceAll(html, "&#234;", "ê"); 
        html = replaceAll(html, "&#235;", "ë"); 
        html = replaceAll(html, "&#236;", "ì"); 
        html = replaceAll(html, "&#237;", "í"); 
        html = replaceAll(html, "&#238;", "î"); 
        html = replaceAll(html, "&#239;", "ï"); 
        html = replaceAll(html, "&#240;", "ð"); 
        html = replaceAll(html, "&#241;", "ñ"); 
        html = replaceAll(html, "&#242;", "ò"); 
        html = replaceAll(html, "&#243;", "ó"); 
        html = replaceAll(html, "&#244;", "ô"); 
        html = replaceAll(html, "&#245;", "õ"); 
        html = replaceAll(html, "&#246;", "ö"); 
        html = replaceAll(html, "&#247;", "÷"); 
        html = replaceAll(html, "&#248;", "ø"); 
        html = replaceAll(html, "&#249;", "ù"); 
        html = replaceAll(html, "&#250;", "ú"); 
        html = replaceAll(html, "&#251;", "û"); 
        html = replaceAll(html, "&#252;", "ü"); 
        html = replaceAll(html, "&#253;", "ý"); 
        html = replaceAll(html, "&#254;", "þ"); 
        html = replaceAll(html, "&#255;", "ÿ");
        html = replaceAll(html, "&#338;", "Œ"); 
        html = replaceAll(html, "&#339;", "œ"); 
        html = replaceAll(html, "&#352;", "Š"); 
        html = replaceAll(html, "&#353;", "š"); 
        html = replaceAll(html, "&#376;", "Ÿ"); 
        html = replaceAll(html, "&#402;", "ƒ"); 
        
        html = replaceAll(html, "&#710;", "^");
        html = replaceAll(html, "&#732;", "˜");
        html = replaceAll(html, "&#8194;", "");
        html = replaceAll(html, "&#8195;", "");
        html = replaceAll(html, "&#8201;", "");
        html = replaceAll(html, "&#8204;", "");
        html = replaceAll(html, "&#8205;", "");
        html = replaceAll(html, "&#8206;", "");
        html = replaceAll(html, "&#8207;", "");
        
        html = replaceAll(html, "&#8211;", "-");
        html = replaceAll(html, "&#8212;", "--");
        html = replaceAll(html, "&#8216;", "`");
        html = replaceAll(html, "&#8217;", "`");
        html = replaceAll(html, "&#8218;", ",");
        html = replaceAll(html, "&#8220;", " \"");
        html = replaceAll(html, "&#8221;", "\" ");
        html = replaceAll(html, "&#8222;", " \"");
        html = replaceAll(html, "&#8224;", "†");
        html = replaceAll(html, "&#8225;", "‡");
        html = replaceAll(html, "&#8225;", "‡");
        html = replaceAll(html, "&#8225;", "‡");
        html = replaceAll(html, "&#8240;", "‰");
        html = replaceAll(html, "&#8249;", "{");
        html = replaceAll(html, "&#8250;", "}");
        html = replaceAll(html, "&#8364;", "€"); 
        html = replaceAll(html, "&#8482;", "™");   
        html = replaceAll(html, "  ", " ");
        html = replaceAll(html, "<blockquote", "");
        
        try {
            if (html.indexOf("<table") > -1 && html.indexOf("</table>") > -1)
                html = html.substring(0, html.indexOf("<table")) + html.substring(html.indexOf("</table>") +  8, html.length());
        } catch (Exception ignore) {}
        
        return html.trim();         
    }   
    
    private static String replaceAll(String s, String c, String with) {
        String result = s.replaceAll(c, with);
        result = result.replaceAll(c.toUpperCase(), with);
        result = result.replaceAll(c.toLowerCase(), with);
        return result;
    }    
}
