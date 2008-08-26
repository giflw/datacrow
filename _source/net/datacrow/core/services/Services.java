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

package net.datacrow.core.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.datacrow.core.DataCrow;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.core.services.plugin.ServiceClassLoader;

import org.apache.log4j.Logger;

public class Services {
    
    private static Logger logger = Logger.getLogger(Services.class.getName());
    private static Services instance = new Services();
    
    private final Map<Integer, Collection<IServer>> registered = new HashMap<Integer, Collection<IServer>>();
    
    public Services() {
        initialize();
    }
    
    public Collection<IServer> getServers(int module) {
        return registered.get(Integer.valueOf(module));
    }
    
    private synchronized void initialize() {
        ServiceClassLoader scl = new ServiceClassLoader(DataCrow.servicesDir);
        for (Class<?> clazz : scl.getClasses()) {
            
            IServer server = null;
            try {
                server = (IServer) clazz.newInstance();
            } catch (Exception ignore) {}    
            
            if (server != null) {
                try {
                    Collection<IServer> servers = registered.get(Integer.valueOf(server.getModule()));
                    servers = servers == null ? new ArrayList<IServer>() : servers;
                    servers.add(server);
                    
                    registered.put(Integer.valueOf(server.getModule()), servers);
                    
                    String name = server.getClass().getName();
                    name = name.substring(name.lastIndexOf(".") + 1);
                    logger.info("Registered online server " + name);
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }
    }

    public static Services getInstance() {
        return instance;
    }
}
