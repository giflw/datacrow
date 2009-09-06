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

package net.datacrow.reporting.templates;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.datacrow.core.DataCrow;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.util.Directory;

public class ReportTemplates {

    private static Map<Integer, String> folders = new HashMap<Integer, String>();
    
    public ReportTemplates(boolean reload) {
    	
    	if (reload || folders.size() == 0) {
    		folders.clear();
            for (DcModule module : DcModules.getModules()) {
	            if (module.isSelectableInUI()) {
	                String path = DataCrow.reportDir + module.getName().toLowerCase().replaceAll("[/\\*%., ]", "");
	                File file = new File(path);
	                if (!file.exists())
	                    file.mkdirs();
	                
	                folders.put(module.getIndex(), path);
	            }
	        }
    	}
    }
    
    public Collection<String> getFolders() {
        return folders.values();
    }
    
    public boolean hasReports(int module) {
        String folder = folders.get(module);
        if (folder != null) {
            String[] extensions = {"xsl", "xslt"};
            Collection<String> files = Directory.read(folder, true, false, extensions);
            if (files.size() > 0) return true;
        }
        return false;
    }
    
    public Collection<ReportTemplate> getReportFiles(int transformer) {
        return getReportFiles(DcModules.getCurrent().getIndex(), transformer);
    }
    
    public Collection<ReportTemplate> getReportFiles(int module, int transformer) {
        String folder = folders.get(module);
        
        Collection<ReportTemplate> reports = new ArrayList<ReportTemplate>();
        if (folder != null) {
            String[] extensions = {"xsl", "xslt"};
            Collection<String> files = Directory.read(folder, true, false, extensions);
            for (String filename : files) {
                ReportTemplate rf = new ReportTemplate(filename);
                if (rf.supports(transformer))
                    reports.add(rf);
            }
        }
        
        return reports;
    }
}
