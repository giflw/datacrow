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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

/**
 * Reads all files from a specific location
 * @author Robert Jan van der Waals
 */
public class Directory {

    private static Logger logger = Logger.getLogger(Directory.class.getName());
    
    /**
     * Retrieves all files and directories from the given locations
     *
     * @param installationDir starting location for reading data
     * @param confirm ask for confirmation for each directory
     * @param recurse read sub directories
     * @param includeDirs allow directory names in the result
     * @param vExtensions extensions to filter on
     * @param logInformation write messages to the screen and the log
     */
    public static Collection<String> read(String dir,
                                          boolean recurse,
                                          boolean includeDirs,
                                          String[] extensions) {

        String baseDir = dir;
        Collection<String> result = new ArrayList<String>();
        File directory = new File(baseDir);
        baseDir = directory.toString();

        if (!directory.exists()) {
            return null;
        }

        boolean useFilter = false;
        if (extensions != null && extensions.length > 0)
            useFilter = true;
        

        List<String> unhandled = new ArrayList<String>();
        unhandled.add(directory.toString());
        
        File tempFile = null;

        while (unhandled.size() > 0) {
            String s = unhandled.get(0);
            String current = s.startsWith(baseDir) ? s : baseDir + (baseDir.endsWith(File.separator) ? "" : File.separator) + s;
            tempFile = new File(current);
            if (tempFile.isDirectory()) {
                        
                logger.debug(DcResources.getText("msgReadingFrom", current));

                String[] list = tempFile.list();
                if (list != null) {
                    for (int j = 0; j < list.length; j++) {
                        String sTempCurrent = current + (current.endsWith(File.separator) ? "" : File.separator) + list[j];
                        if (!unhandled.contains(sTempCurrent)) {
                            tempFile = new File(sTempCurrent);
                            if (tempFile.isDirectory()) {
                                String part = sTempCurrent.substring(baseDir.length());
                                part = part.startsWith(File.separator) ? part.substring(1) : part;
                                unhandled.add(part);
                            } else {
                                if (useFilter && isValid(sTempCurrent, extensions)) {
                                    result.add(sTempCurrent);
                                } else if (!useFilter) {
                                    result.add(sTempCurrent);
                                }
                            }
                        }
                    }
                }
            } else {
                if (useFilter && isValid(current, extensions)) {
                    result.add(current);
                } else if (!useFilter) {
                    result.add(current);
                }
            }

            unhandled.remove(0);

            if (!recurse)
                return result;
        }

        return result;
    }

    private static boolean isValid(String file, String[] extensions) {
        String filename = file;
        int last = filename.lastIndexOf(".");
        boolean valid = false;
        if (last + 1 < filename.length()) {
            filename = filename.substring(last + 1, filename.length());
            for (int i = 0; i < extensions.length; i++) {
                if (extensions[i].equals(filename.toLowerCase())) {
                    valid = true;
                    break;
                }
            }
        }
        return valid;
    }
}