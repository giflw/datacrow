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

package net.datacrow.core.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.wf.WorkFlow;
import net.datacrow.core.wf.requests.IRequest;
import net.datacrow.core.wf.requests.IUpdateUIRequest;
import net.datacrow.core.wf.requests.ImageRequest;
import net.datacrow.core.wf.requests.Requests;

import org.apache.log4j.Logger;

/**
 * Queries are queued here awaiting to be executed. The query queue manages the 
 * back log of queries and executes the requests tied to the queries. The queue
 * is based on the FIFO principle.
 * 
 * @author Robert Jan van der Waals
 */
public class QueryQueue extends Thread {

    private static Logger logger = Logger.getLogger(QueryQueue.class.getName());
    
    private final LinkedList<Query> lQueryQueue = new LinkedList<Query>();

    private boolean isLazy = true;
    private boolean success = true;
    
    public QueryQueue() {}

    /**
     * Indicates the back log.
     */
    public int getQueueSize() {
    	return lQueryQueue.size();
    }
    
    /**
     * Add a query to the end of the queue. 
     * @param query
     */
    public void addQuery(Query query) {
        lQueryQueue.addLast(query);
    }

    @Override
    public void run() {
        synchronized(this) {
            while (true) {
                try {
                    if (lQueryQueue.size() > 0 && isLazy) {
                        success = true;
                        isLazy = false;

                        Query query = lQueryQueue.removeFirst();
                        Collection<DcObject> result = executeQuery(query);
                    	handleRequests(query, result, success);
                        query.unload();
                    } else {
                        isLazy = true;
                        sleep(10000);
                    }
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }
    }
    
    private void logQuery(Query query) {
        if (logger.isDebugEnabled()) {
            String sql = query.getQuery().toString();
            int idx = sql.toLowerCase().indexOf("sql=[");
            if (idx > -1)
                sql = sql.substring(idx + 5, (idx + 5 < sql.length() - 2 ? sql.length() - 2 : sql.length()));
            logger.debug(sql);
        }
    }

    private Collection<DcObject> executeQuery(Query query) {
        
        logQuery(query);
        
        switch(query.getType()) {
            case Query._UPDATE:
                success = executeUpdateQuery(query);
                break;
            case Query._DELETE:
                success = executeDeleteQuery(query);
                break;
            case Query._INSERT:
                success = executeInsertQuery(query);
                break;
            case Query._UNDEFINED:
                if (!query.getSilence()) {
                    logger.info(query.getQuery());
                }
                return executeUndefinedQuery(query);
            default:
                if (!query.getSilence())
                    logger.info(query.getQuery());

            return executeSelectQuery(query);
        }

        return null;
    }

    private boolean executeUpdateQuery(Query query) {
        boolean success = true;
        for (PreparedStatement ps : query.getQueries()) {
            try {
                ps.execute();
            } catch (SQLException e) {
                success = false;
                logger.error("Error while executing query " + ps, e);
            } finally {
                close(ps);
            }
        }
        return success;
    }

    private boolean executeInsertQuery(Query query) {
        boolean success = true;
        for (PreparedStatement ps : query.getQueries()) {            
            try {
                ps.execute();
            } catch (SQLException e) {
                success = false;
                logger.error(e);
                logger.error("Error while executing query " + ps, e);
            } finally {
                close(ps);
            }
        }
        return success;
    }

    private boolean executeDeleteQuery(Query query) {
        boolean success = true;
        for (PreparedStatement ps : query.getQueries()) {            
            try {
                ps.execute();
            } catch (SQLException e) {
                success = false;
                logger.error(e);
                logger.error("Error while executing query " + ps, e);
            } finally {
                close(ps);
            }
        }
        
        return success;
    }

    private Collection<DcObject> executeSelectQuery(Query query) {
        Collection<DcObject> objects = new ArrayList<DcObject>();
        
        for (PreparedStatement ps : query.getQueries()) {
            ResultSet result = null;
            try {
                result = ps.executeQuery();
            } catch (SQLException e) {
                logger.error("Error while executing query " + ps, e);
            }

            objects.addAll(new WorkFlow().convert(result));

            try {
                if (result != null)
                    result.close();
                
                close(ps);
            } catch (SQLException e) {
                logger.error(e);
                logger.error("Error while closing result set", e);
            }
        }

        return objects;
    }
    
    private Collection<DcObject> executeUndefinedQuery(Query query) {
        PreparedStatement ps = query.getQuery();
        ResultSet result = null;
        try {
            result = ps.executeQuery();
        } catch (SQLException e) {
            logger.error("Error while executing query " + ps, e);
        }

        Collection<DcObject> objects = new WorkFlow().convert(result);
        try {
            if (result != null)
                result.close();
            
            close(ps);
        } catch (SQLException e) {
            logger.error("Error while closing result set", e);
        }
        
        return objects;
    }

    private void handleRequests(Query qry, Collection<DcObject> objects, boolean qryWasSuccess) {
        Requests requests = qry.getRequests();
        if (requests != null)  {
            Requests uiRequests = new Requests();

            for (IRequest request : requests.get()) {
                if (request instanceof ImageRequest) {
                    requests.remove(request);
                    request.execute(objects);
                }
            }
            
            for (IRequest request : requests.get()) {
                requests.remove(request);
                if (request instanceof IUpdateUIRequest)
                    uiRequests.add(request);
                else 
                    request.execute(objects);
            }

            WorkFlow.handleRequests(objects, uiRequests, qryWasSuccess);
        }
    }

    private void close(PreparedStatement ps) {
        try {
            ps.getConnection().commit();
            ps.getConnection().close();
            ps.close();
        } catch (SQLException e) {
            logger.error("Error while closing prepared statement " + ps, e);
        }
    }
}
