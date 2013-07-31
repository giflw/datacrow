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

package net.datacrow.util.freedb;

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

import org.apache.log4j.Logger;

public class FreedbDiscImport {
	
	private static Logger logger = Logger.getLogger(FreedbDiscImport.class.getName());
	
	public FreedbDiscImport() {}
	
	public void start() {
		new DiscQueryTask().start();
	}
	
	private class DiscQueryTask extends Thread {
		
		@Override
		public void run() {
			DiscIDUtil util = new DiscIDUtil();
	        String discId = "";
	        try {
	        	discId = util.readDiscID();
	        	logger.info(DcResources.getText("msgFoundDiscID", discId));
	        } catch (Exception e) {
	            DcSwingUtilities.displayWarningMessage(DcResources.getText("msgErrorDiscID", e.getMessage()));
	        	logger.error(DcResources.getText("msgErrorDiscID", e.getMessage()));
	        	return;
	        }

	        if (!util.isSuccess()) {
                DcSwingUtilities.displayWarningMessage("msgCouldNotReadDiscID");
	        	logger.info(DcResources.getText("msgCouldNotReadDiscID"));
	            return;
	        }

	        Freedb freeDb = new Freedb();
	        discId = discId.replaceAll(" ", "+");

	        try {
	            DcObject[] audioCDs = freeDb.queryDiscId(discId);

	            if (audioCDs.length == 0) {
	                DcSwingUtilities.displayWarningMessage(DcResources.getText("msgNoResultsForDiscID", discId));
	            } else {
	            	for (DcObject audioCD : audioCDs) {
	            	    audioCD.setValidate(false);
	            	    audioCD.saveNew(false);
	            	    audioCD.setValidate(true);
	            	}
	            }
	        } catch (Exception e) {
                DcSwingUtilities.displayErrorMessage(DcResources.getText("msgErrorDiscID", e.getMessage()));
	            logger.error(DcResources.getText("msgErrorDiscID", e.getMessage()), e);
	        }
		}
	}
}
