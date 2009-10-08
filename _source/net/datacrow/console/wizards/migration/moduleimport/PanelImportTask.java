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

package net.datacrow.console.wizards.migration.moduleimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.console.wizards.migration.itemimport.ItemImporterPanel;
import net.datacrow.core.migration.moduleimport.IModuleImporterClient;
import net.datacrow.core.migration.moduleimport.ModuleImporter;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class PanelImportTask extends ModuleImportWizardPanel implements IModuleImporterClient {

    private static Logger logger = Logger.getLogger(ItemImporterPanel.class.getName());
    
    private JTextArea textLog = ComponentFactory.getTextArea();
    private JProgressBar progressBar = new JProgressBar();
    
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
        progressBar = null;
        textLog = null;
    }

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

        //**********************************************************
        //Progress panel
        //**********************************************************
        JPanel panelProgress = new JPanel();
        panelProgress.setLayout(Layout.getGBL());
        panelProgress.add(progressBar, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        //**********************************************************
        //Log Panel
        //**********************************************************        
        JPanel panelLog = new JPanel();
        panelLog.setLayout(Layout.getGBL());

        JScrollPane scroller = new JScrollPane(textLog);
        textLog.setEditable(false);

        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelLog.add(scroller, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0));

        panelLog.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblLog")));

        add(panelLog,      Layout.getGBC( 0, 0, 1, 1, 20.0, 20.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        add(panelProgress, Layout.getGBC( 0, 1, 1, 1, 10.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 5, 5, 5, 5), 0, 0));
    }
    
    private void cancel() {
        if (importer != null) 
            importer.cancel();
        
        notifyFinished();
    }    
    
    public void notifyMessage(String message) {
        if (textLog != null) {
            textLog.insert(message + '\n', 0);
            textLog.setCaretPosition(0);
        }
    }

    public void notifyStarted(int count) {
        if (textLog != null)
            textLog.setText("");
        
        if (progressBar != null) {
            progressBar.setValue(0);
            progressBar.setMaximum(count);
        }
    }

    public void notifyProcessed() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    public void notifyError(Exception e) {
        logger.error(e, e);
        notifyMessage(DcResources.getText("msgModuleExportError", e.getMessage()));
    }

    public void notifyFinished() {
        notifyMessage(DcResources.getText("msgModuleExportFinished"));
    }
}
