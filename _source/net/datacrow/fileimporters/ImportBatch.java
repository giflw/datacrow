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

package net.datacrow.fileimporters;

import java.util.Collection;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;

/**
 * The import batch coaches the import process.
 * This is the class that actually should be used to start a file import. 
 * 
 * @author Robert Jan van der Waals
 */
public class ImportBatch extends Thread {

    protected IFileImportClient client;
    protected FileImporter importer;
    protected Collection<String> sources;
    
    /**
     * Creates a new import batch.
     * @param client 
     * @param importer
     * @param sources
     * @throws Exception
     */
    public ImportBatch(IFileImportClient client, 
                       FileImporter importer, 
                       Collection<String> sources) throws Exception {
        
        this.client = client;
        this.importer = importer;
        this.sources = sources;
        

    }
    
    @Override
    public void run() {
        try {
            parse(sources);
        } catch (Exception e) {
            client.addError(e);
        }
    }    
    
    /**
     * Parse the files and pass the resulted items to the listener.
     * @param files
     */
    protected void parse(Collection<String> files) {
        try {
            client.addMessage(DcResources.getText("msgImportFoundXResults", "" + files.size()));
            client.initProgressBar(files.size());
            int counter = 1;
            
            DcModules.getCurrent().getNewItemsDialog().setVisible(true);
            
            for (String filename : files) {

                if  (client.cancelled()) break;

                DcObject dco = parse(filename);
                
                DcModules.getCurrent().getCurrentInsertView().add(dco, false);
                client.updateProgressBar(counter++);
            }
        } finally {
            client.addMessage(DcResources.getText("msgImportStops"));
            client.finish();
            cleanup();
        }
    }   
    
    /**
     * Free resources.
     */
    protected void cleanup() {
        client = null;
        importer = null;
        sources.clear();
        sources = null;
    }
    
    /**
     * Parse a single file. In case all fails an empty item will be created (only the
     * filename will be set).
     * @param filename
     * @return The result (never null)
     */
    protected DcObject parse(String filename) {
        client.addMessage(DcResources.getText("msgProcessingFileX", filename));
        DcObject dco = importer.parse(filename, client.getDirectoryUsage()); 
            
        if (client.getStorageMedium() != null) { 
            for (DcField  field : dco.getFields()) {
                if (field.getSourceModuleIdx() == DcModules._STORAGEMEDIA)
                    dco.setValue(field.getIndex(), client.getStorageMedium());
            }
        }

        if (client.getDcContainer() != null && dco.getField(DcObject._SYS_CONTAINER) != null) {
            dco.setValue(DcObject._SYS_CONTAINER, client.getDcContainer());
        }
        
        dco.applyTemplate();
        dco.setIDs();
        
        try {
            sleep(200);
        } catch (Exception e) {}

        return dco;
    }
}
