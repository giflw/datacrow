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

    protected IFileImportClient listener;
    protected FileImporter importer;
    protected Collection<String> sources;
    
    /**
     * Creates a new import batch.
     * @param listener 
     * @param importer
     * @param sources
     * @throws Exception
     */
    public ImportBatch(IFileImportClient listener, 
                       FileImporter importer, 
                       Collection<String> sources) throws Exception {
        
        this.listener = listener;
        this.importer = importer;
        this.sources = sources;
        

    }
    
    @Override
    public void run() {
        try {
            parse(sources);
        } catch (Exception e) {
            listener.addError(e);
        }
    }    
    
    /**
     * Parse the files and pass the resulted items to the listener.
     * @param files
     */
    protected void parse(Collection<String> files) {
        try {
            listener.addMessage(DcResources.getText("msgImportFoundXResults", "" + files.size()));
            listener.initProgressBar(files.size());
            int counter = 1;
            
            for (String filename : files) {

                if  (listener.cancelled()) break;

                DcObject dco = parse(filename);
                
                DcModules.getCurrent().getCurrentInsertView().add(dco, false);
                listener.updateProgressBar(counter++);
            }
        } finally {
            listener.addMessage(DcResources.getText("msgImportStops"));
            listener.finish();
            cleanup();
        }
    }   
    
    /**
     * Free resources.
     */
    protected void cleanup() {
        listener = null;
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
        listener.addMessage(DcResources.getText("msgProcessingFileX", filename));
        DcObject dco = null; 
        try {
            dco = importer.parse(filename, listener.getDirectoryUsage());
        } catch (ParseException pe) {
            listener.addMessage(DcResources.getText("msgCouldNotReadInfoFrom", filename));
            dco = DcModules.get(listener.getModule()).getDcObject();
        }
            
        if (listener.getStorageMedium() != null) { 
            for (DcField  field : dco.getFields()) {
                if (field.getSourceModuleIdx() == DcModules._STORAGEMEDIA)
                    dco.setValue(field.getIndex(), listener.getStorageMedium());
            }
        }

        if (listener.getDcContainer() != null && dco.getField(DcObject._SYS_CONTAINER) != null) {
            dco.setValue(DcObject._SYS_CONTAINER, listener.getDcContainer());
        }
        
        dco.applyTemplate();
        dco.setIDs();
        
        try {
            sleep(200);
        } catch (Exception e) {}

        return dco;
    }
}
