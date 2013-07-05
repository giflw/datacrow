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

import net.datacrow.core.DataCrow;

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
    
	private static DcWebServer instance = new DcWebServer();
	
	private int port;
	private boolean isRunning;
	private Server server;
	
	/**
	 * Returns the instance of the web server.
	 */
	public static DcWebServer getInstance() {
	    
	    return instance;
	}
	
	public void setPort(int port) {
	    this.port = port;
	}
	
	/**
	 * Creates a new instance.
	 */
	private DcWebServer() {}
	
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
	    connector.setPort(port);
	    
	    server.setConnectors(new Connector[]{connector});
	    
	    String baseDir = DataCrow.webDir;
	    
	    WebAppContext webapp = new WebAppContext();
	    webapp.setContextPath(context);
	    webapp.setWar(baseDir + context);
	    webapp.setDefaultsDescriptor(baseDir + context + "/WEB-INF/webdefault.xml");
	    
	    server.setHandler(webapp);
	    
	    server.start();
	    isRunning = true;
	}
}
