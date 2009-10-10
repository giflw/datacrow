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

package net.datacrow.console.wizards.moduleimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import net.datacrow.console.Layout;
import net.datacrow.console.components.panels.TaskPanel;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.console.wizards.itemimport.ItemImporterPanel;
import net.datacrow.core.migration.IModuleWizardClient;
import net.datacrow.core.migration.moduleimport.ModuleImporter;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class PanelImportTask extends ModuleImportWizardPanel implements IModuleWizardClient {

    private static Logger logger = Logger.getLogger(ItemImporterPanel.class.getName());
    
    private TaskPanel tp = new TaskPanel(TaskPanel._DUPLICATE_PROGRESSBAR);
    
    private ModuleImporter importer;
    
    public PanelImportTask() {
        build();
    }
    
    public Object apply() throws WizardException {
        return getDefinition();
    }

    public void destroy() {
        if (importer != null) 
            importer.cancel();
        
        importer = null;
        
        if (tp != null)
            tp.destroy();
        
        tp = null;
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgModuleImportHelp");
    }
    
    @Override
    public void onActivation() {
        if (getDefinition() != null && getDefinition().getFile() != null)
            start();
    }

    @Override
    public void onDeactivation() {
        cancel();
    }

    private void start() {
        ImportDefinition def = getDefinition();
        
        if (importer != null)
            importer.cancel();
        
        importer = new ModuleImporter(def.getFile());
        
        try { 
            importer.start(this);
        } catch (Exception e ) {
            notifyMessage(e.getMessage());
            logger.error(e, e);
        }
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        add(tp, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets( 5, 5, 5, 5), 0, 0));
    }
    
    private void cancel() {
        if (importer != null) 
            importer.cancel();
        
        notifyFinished();
    }    
    
    public void notifyMessage(String msg) {
        tp.addMessage(msg);
    }

    public void notifyNewTask() {
        tp.clear();
    }
    
    public void notifyStarted(int count) {
        tp.initializeTask(count);
    }

    public void notifyStartedSubProcess(int count) {
        tp.initializeSubTask(count);
    }

    public void notifyProcessed() {
        tp.updateProgressTask();
    }

    public void notifySubProcessed() {
        tp.updateProgressSubTask();
    }

    public void notifyError(Exception e) {
        logger.error(e, e);
        notifyMessage(DcResources.getText("msgModuleExportError", e.toString()));
    }

    public void notifyFinished() {
        notifyMessage(DcResources.getText("msgModuleExportFinished"));
    }
}
