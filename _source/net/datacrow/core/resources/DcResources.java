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

package net.datacrow.core.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DataCrow;

import org.apache.log4j.Logger;

public class DcResources {
    
    private static Logger logger = Logger.getLogger(DcResources.class.getName());

    private final static String resourcesFile = DataCrow.baseDir + "data/resources.properties";
    private static final Properties resources = new Properties();

    public DcResources() {
        try {
            resources.load(getClass().getResourceAsStream("/net/sf/dc/core/resources/DcLabels.properties"));
            resources.load(getClass().getResourceAsStream("/net/sf/dc/core/resources/DcMessages.properties"));
            resources.load(getClass().getResourceAsStream("/net/sf/dc/core/resources/DcTooltips.properties"));
            resources.load(getClass().getResourceAsStream("/net/sf/dc/core/resources/DcAudioCodecs.properties"));
            resources.load(getClass().getResourceAsStream("/net/sf/dc/core/resources/DcSystem.properties"));
            resources.load(getClass().getResourceAsStream("/net/sf/dc/core/resources/DcTips.properties"));
        } catch (Exception ignore) {
            try {
                resources.load(getClass().getResourceAsStream("DcLabels.properties"));
                resources.load(getClass().getResourceAsStream("DcMessages.properties"));
                resources.load(getClass().getResourceAsStream("DcTooltips.properties"));
                resources.load(getClass().getResourceAsStream("DcAudioCodecs.properties"));
                resources.load(getClass().getResourceAsStream("DcSystem.properties"));
                resources.load(getClass().getResourceAsStream("DcTips.properties"));
            } catch (Exception e) {
                logger.fatal("Could not load system resource files.", e);
                new MessageBox("Could not load system resource files. The application cannot start.", MessageBox._ERROR);
            }
        }

        try {
            File file = new File(resourcesFile);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                resources.load(fis);
                fis.close();
            }
        } catch (Exception e) {
            logger.error("Could not load sustom resource files. Falling back to the default resources.", e);
            new MessageBox("Unable to load custom resources! " + e.getMessage(), MessageBox._ERROR);
        }
    }

    public static Properties getResources() {
        return resources;
    }

    public static void setText(String key, String text) {
        resources.setProperty(key, text);
    }

    public static void save() {
        try {
            File file = new File(resourcesFile);
            FileOutputStream fos = new FileOutputStream(file);
            resources.store(fos, "Custom Resources");
            fos.close();
        } catch (Exception e) {
            logger.error("An error occurred while saving the resource file " + resourcesFile, e);
        }
    }

    public static String getText(String id) {
    	return getText(id, (String[]) null);
    }

    public static String getText(String id, String param1) {
    	return getText(id, new String[] {param1});
    }

    public static String getText(String id, String[] params) {
        String value = (String) resources.get(id);
        return params == null ? value : insertParams(value, params);
    }

    private static String insertParams(String s, String[] params) {
    	String result = s;
    	for (int i = 1; i - 1 < params.length; i++) {
    	    String searchPat = "%" + i;
    	    int index = 0;
    	    boolean escapedPatFound = false;
    	    do {
        		index = result.indexOf(searchPat, index);
        		if (index < 0) 
        		    logger.error("Could not insert the parameter for label " + s);
        		else
        		    escapedPatFound = (index > 0 && result.charAt(index - 1) == '\\');
    	    } while (escapedPatFound);

            StringBuffer sb = new StringBuffer(result);
            try {
            	sb.replace(index, index+searchPat.length(), params[i -1]);
            } catch (Exception e) {}
            
    	    result = sb.toString();
    	}
    	return result;
    }
}
