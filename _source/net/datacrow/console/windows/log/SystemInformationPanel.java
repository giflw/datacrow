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

package net.datacrow.console.windows.log;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JPanel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcHtmlEditorPane;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.Utilities;

public class SystemInformationPanel extends JPanel {
    
    private Runtime runtime;
    
    public SystemInformationPanel() {
        runtime = Runtime.getRuntime();
        build();
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        DcHtmlEditorPane hep = ComponentFactory.getHtmlEditorPane();
        
        String db = DcResources.getText("lblDatabase");
        String dbDriver = DcResources.getText("lblDatabaseDriver");
        
        long max = Math.round(Math.round(runtime.maxMemory() / 1024) / 1024) + 1;
        long used = Math.round(Math.round(runtime.totalMemory() / 1024) / 1024) + 1;

        String html =
                "<HTML><BODY>" +
                "<TABLE cellpadding=\"5\" cellspacing=\"5\"  " + Utilities.getHtmlStyle() + ">" +
                		"<TR><TD><b>" + db + ":</b></TD><TD>" + DcSettings.getString(DcRepository.Settings.stConnectionString) + "</TD></TR>" +
                		"<TR><TD><b>" + dbDriver + ":</b></TD><TD>" + DcSettings.getString(DcRepository.Settings.stDatabaseDriver) + "</TD></TR>" +
                		"<TR><TD><b>Memory maximum</b></TD><TD>" + max + "MB</TD></TR>" +
                		"<TR><TD><b>Java version</b></TD><TD>" + System.getProperty("java.version") + "</TD></TR>" +
                		"<TR><TD><b>Memory available</b></TD><TD>" + (max - used) + "MB</TD></TR>" +
                		"<TR><TD><b>Operating system</b></TD><TD>" + System.getProperty("os.name") + "</TD></TR>" +
                        "<TR><TD><b>Memory used</TD></b><TD>" + used + "MB</TD></TR>" +
                        "<TR><TD><b>Data Crow version</b></TD><TD>" + DataCrow.getVersion() + "</TD></TR>" + 
                        "<TR><TD><b>Data Crow system file</TD></b><TD>" + DataCrow.getDcproperties().getFile() + "</TD></TR>" + 
                        "<TR><TD><b>Installation folder</b></TD><TD>" + DataCrow.installationDir + "</TD></TR>" + 
                "</TABLE></BODY></HTML>";
        hep.setHtml(html);
       
        add(hep, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));    
    }
    
    private int getRecordCount(DcModule module) {
    	return DataManager.getCount(module.getIndex(), -1, null);
    }    
}
