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

package net.datacrow.core.web;

import java.io.File;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * The web server. This is the wrapper around the Jetty server.  
 * 
 * @author Robert Jan van der Waals
 */
public class DcWebServer {
    
    private static final String context = "/datacrow";
    
    private static Logger logger = Logger.getLogger(DcWebServer.class.getName());
	private static DcWebServer instance = new DcWebServer();
	
	private boolean isRunning;

	private Server server;
	
	/**
	 * Returns the instance of the web server.
	 */
	public static DcWebServer getInstance() {
	    return instance;
	}
	
	/**
	 * Creates a new instance.
	 */
	private DcWebServer() {
	    try {
	        copyIcons();
	    } catch (Exception e) {
	        logger.error(e, e);
	    }
	}
	
	/**
	 * Indicates if the server is currently up and running.
	 */
	public boolean isRunning() {
        return isRunning;
    }

    /**
     * Stops the server.
     * @throws Exception
     */
	public void stop() throws Exception {
	    server.stop();
        isRunning = false;
	}
	
	/**
	 * Starts the Web Server. The port is configurable.
	 */
	public void start() throws Exception {
	    server = server == null ? new Server() : server;
	    
	    Connector connector = new SelectChannelConnector();
	    connector.setPort(DcSettings.getInt(DcRepository.Settings.stWebServerPort)); 
	    server.setConnectors(new Connector[]{connector});
	    
	    String baseDir = DataCrow.baseDir + "webapp";
	    
	    WebAppContext webapp = new WebAppContext();
	    webapp.setContextPath(context);
	    webapp.setWar(baseDir + context);
	    webapp.setDefaultsDescriptor(baseDir + context + "/WEB-INF/webdefault.xml");
	    
	    server.setHandler(webapp);
	    
	    server.start();
	    isRunning = true;
	}
	
    private void copyIcons() throws Exception {
        File dir = new File(DataCrow.webDir + "images" + File.separator + "modules");
        if (!dir.exists())
            dir.mkdirs();
        
        for (DcModule module : DcModules.getAllModules()) {
            XmlModule xm = module.getXmlModule();
            
            if (xm == null) continue;
            
            if (xm.getIcon16() != null)
                Utilities.writeToFile(module.getXmlModule().getIcon16(), new File(dir, module.getName() + "16.png"));
            
            if (xm.getIcon32() != null)
                Utilities.writeToFile(module.getXmlModule().getIcon32(), new File(dir, module.getName() + "32.png"));
        }
    }	
}
