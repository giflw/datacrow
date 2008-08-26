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

/**
 Copyright 2005 Bytecode Pty Ltd.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import net.datacrow.console.windows.MigrationDialog;

/**
 * A very simple CSV reader released under a commercial-friendly license.
 * 
 * @author Glen Smith
 * 
 */
public class CSVReader {

    private BufferedReader br;
    private boolean hasNext = true;
    private String separator;
    private char quotechar= '"';
    
    private MigrationDialog dlg;
    public static final char DEFAULT_QUOTE_CHARACTER = '\"';
    public static final int DEFAULT_SKIP_LINES = 0;

    public CSVReader(Reader reader, String separator, MigrationDialog dlg) {
        this.br = new BufferedReader(reader);
        this.separator = separator;
        this.dlg = dlg;
    }

    /**
     * Reads the entire file into a List with each element being a String[] of tokens.
     * @return a List of String[], with each String[] representing a line of the file.
     */
    public List<String[]> readAll() throws IOException {
        List<String[]>  allElements = new ArrayList<String[]>();
        while (hasNext) {
            String[] nextLineAsTokens = readNext();
            if (nextLineAsTokens != null)
                allElements.add(nextLineAsTokens);
        }
        return allElements;

    }

    /**
     * Reads the next line from the buffer and converts to a string array.
     * @return a string array with each comma-separated element as a separate entry.
     */
    public String[] readNext() throws IOException {

        String nextLine = getNextLine();
        return hasNext ? parseLine(nextLine) : null;
    }

    /**
     * Reads the next line from the file.
     * @return the next line from the file without trailing newline
     */
    private String getNextLine() throws IOException {
        String nextLine = br.readLine();
        if (nextLine == null)
            hasNext = false;

        return hasNext ? nextLine : null;
    }

    /**
     * Parses an incoming String and returns an array of elements.
     * @param nextLine the string to parse
     * @return the comma-tokenized list of elements, or null if nextLine is null
     * @throws IOException if bad things happen during the read
     */
    private String[] parseLine(String line) throws IOException {
        String nextLine = line;
        if (nextLine == null)
            return null;

        List<String> tokensOnThisLine = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        boolean inQuotes = false;
        do {
        	if (inQuotes) {
                // continuing a quoted section, re-append newline
                sb.append("\n");
                nextLine = getNextLine();
                if (nextLine == null)
                    break;
            }
            for (int i = 0; i < nextLine.length(); i++) {

                if (dlg != null)
                    dlg.updateProgressBar();
                
                char c = nextLine.charAt(i);
                if (c == quotechar) {
                	// the quote may end a quoted block, or escape another quote. do a 1-char lookahead:
                	if( inQuotes  
                	    && nextLine.length() > (i+1)  
                	    && nextLine.charAt(i+1) == quotechar ){

                        sb.append(nextLine.charAt(i+1));
                		i++;
                	} else {
                		inQuotes = !inQuotes;
                		// the tricky case of an embedded quote in the middle: a,bc"d"ef,g
                		if(i>2 //not on the beginning of the line
                				&& this.separator.equals("" + nextLine.charAt(i-1)) //not at the beginning of an escape sequence 
                				&& nextLine.length()>(i+1) &&
                                   !this.separator.equals("" + nextLine.charAt(i+1)) //not at the	end of an escape sequence
                		){
                			sb.append(c);
                		}
                	}
                } else if (("" + c).equals(separator) && !inQuotes) {
                    String val = sb.toString();
                    tokensOnThisLine.add(val.startsWith("\"") ? val.substring(1, val.length()) : val);
                    sb = new StringBuffer(); // start work on next token
                } else {
                    sb.append(c);
                }
            }
        } while (inQuotes);
        
        tokensOnThisLine.add(sb.toString());
        return tokensOnThisLine.toArray(new String[0]);
    }

    /**
     * Closes the underlying reader.
     */
    public void close() throws IOException{
    	br.close();
    }
}
