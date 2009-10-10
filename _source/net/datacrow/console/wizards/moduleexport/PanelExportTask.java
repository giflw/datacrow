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

package net.datacrow.console.wizards.moduleexport;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;

import net.datacrow.console.Layout;
import net.datacrow.console.components.panels.TaskPanel;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.console.wizards.itemimport.ItemImporterTaskPanel;
import net.datacrow.core.migration.IModuleWizardClient;
import net.datacrow.core.migration.moduleexport.ModuleExporter;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class PanelExportTask extends ModuleExportWizardPanel implements IModuleWizardClient {

    private static Logger logger = Logger.getLogger(ItemImporterTaskPanel.class.getName());

    private TaskPanel tp = new TaskPanel(TaskPanel._DUPLICATE_PROGRESSBAR);
    
    private ModuleExporter exporter;
    
    public PanelExportTask() {
        build();
    }
    
    public Object apply() throws WizardException {
        return getDefinition();
    }

    public void destroy() {
        if (exporter != null) 
            exporter.cancel();
        
        exporter = null;
        
        if (tp != null)
            tp.destroy();
        
        tp = null;
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgModuleExportHelp");
    }
    
    @Override
    public void onActivation() {
        if (getDefinition() != null && getDefinition().getModule() != 0)
            start();
    }

    @Override
    public void onDeactivation() {
        cancel();
    }

    private void start() {
        ExportDefinition def = getDefinition();
        
        if (exporter != null)
            exporter.cancel();
        
        exporter = new ModuleExporter(def.getModule(), new File(def.getPath()));
        exporter.setExportData(def.isExportDataMainModule());
        exporter.setExportDataRelatedMods(def.isExportDataRelatedModules());
        exporter.setExportRelatedMods(def.isExportRelatedModules());
        
        try { 
            exporter.start(this);
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
        if (exporter != null) 
            exporter.cancel();
        
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
