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

package net.datacrow.console.windows.reporting;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.DcFileField;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.migration.itemexport.IItemExporterClient;
import net.datacrow.core.migration.itemexport.ItemExporter;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.reporting.ReportDictionaryGenerator;
import net.datacrow.reporting.templates.ReportTemplate;
import net.datacrow.reporting.templates.ReportTemplates;
import net.datacrow.reporting.transformers.XmlTransformer;
import net.datacrow.reporting.transformers.XmlTransformers;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.BrowserLauncher;

import org.apache.log4j.Logger;

public class ReportingDialog extends DcDialog implements IItemExporterClient, ActionListener, ItemListener {

    private static Logger logger = Logger.getLogger(ReportingDialog.class.getName());
    
    private ItemExporter exporter;
    private XmlTransformer transformer;

    private ReportSettingsPanel panelSettings = new ReportSettingsPanel();
    
    private JButton buttonRun = ComponentFactory.getButton(DcResources.getText("lblRun"));
    private JButton buttonStop = ComponentFactory.getButton(DcResources.getText("lblStop"));
    private JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
    private JButton buttonResults = ComponentFactory.getButton(DcResources.getText("lblOpenReport"));

    private JComboBox cbTemplates = ComponentFactory.getComboBox();
    private JComboBox cbTransformer = ComponentFactory.getComboBox();

    private JTextArea textLog = ComponentFactory.getTextArea();
    private JProgressBar progressBar = new JProgressBar();
    private DcFileField fileField;
    
    private List<DcObject> items;

    public ReportingDialog(List<DcObject> items) {
        super(DataCrow.mainFrame);

        new ReportDictionaryGenerator().generate();
        
        try {
            this.items = items;
            
            DataFilters.getDefaultDataFilter(DcModules.getCurrent().getIndex()).sort(items);

            setHelpIndex("dc.reports");
    
            fileField = ComponentFactory.getFileField(true, false, null);
            buildDialog();

        } catch (Exception exp) {
            logger.error(DcResources.getText("msgFailedToOpen", exp.getMessage()), exp);
            new MessageBox(DcResources.getText("msgFailedToOpen", exp.getMessage()) , MessageBox._ERROR);
        }
        
        setModal(true);
    }

    private void saveDefaults() {
        DcSettings.set(DcRepository.Settings.stReportingDialogSize, getSize());
        DcSettings.set(DcRepository.Settings.stReportFile, fileField.getFilename());
    }
    
    public void notifyMessage(String message) {
        if (textLog != null) { 
            textLog.insert(message + '\n', 0);
            textLog.setCaretPosition(0);
        }
    }

    public void notifyProcessed() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    public void notifyStarted(int count) {
        progressBar.setValue(0);
        progressBar.setMaximum(count);
        allowActions(false);
    }

    public void notifyStopped() {
        allowActions(true);
    }

    public void allowActions(boolean b) {
        if (buttonRun != null) {
            buttonRun.setEnabled(b);
            buttonResults.setEnabled(b);
        }
    }

    private void showResults() {
    	File file = fileField.getFile();
        if (file != null && file.exists()) {
            try {
            	BrowserLauncher.openURL(file.toString());
            } catch (Exception e) {
                String msg = DcResources.getText("msgErrorWhileOpeningX",
                                        new String[] {file.toString(), e.getMessage()});
                new MessageBox(msg, MessageBox._WARNING);
                logger.error(msg, e);
            }
        } else {
        	new MessageBox(DcResources.getText("msgCouldNotOpenReport"), MessageBox._WARNING);
        }
    }

    private File getTarget(String extension) throws FileNotFoundException {
        File target = fileField.getFile();
        
        if (target == null)
            throw new FileNotFoundException();
        
        String filename = target.toString();
        if (!filename.toLowerCase().endsWith(extension.toLowerCase())) {
            
            if (filename.lastIndexOf(".") > 0) 
                filename = filename.substring(0, filename.lastIndexOf("."));
            
            target = new File(filename + "." + extension);
            fileField.setFile(target);
        }
        
        return target;
    }
    
    private void createReport() {
        try {
            transformer = (XmlTransformer) cbTransformer.getSelectedItem();
            ReportTemplate template = (ReportTemplate) cbTemplates.getSelectedItem();
            panelSettings.saveSettings(template.getProperties(), true);
            transformer.transform(this, items, getTarget(transformer.getFileType()), template);

            allowActions(false);
            
        } catch (FileNotFoundException fnfe) {
            new MessageBox(DcResources.getText("msgSelectTargetFile"), MessageBox._WARNING);
        } 
    }

    private void cancel() {
        if (exporter != null)
            exporter.cancel();
        
        if (transformer != null)
            transformer.cancel();
        
        allowActions(true);
    }

    @Override
    public void close() {
        saveDefaults();
        saveReportFileProperties((ReportTemplate) cbTemplates.getSelectedItem());
        
        cancel();

        if (items != null) { 
            items.clear();
            items = null;
        }
        
        exporter = null;
        transformer = null;
        panelSettings = null;
        buttonRun = null;
        buttonStop = null;
        buttonClose = null;
        buttonResults = null;
        cbTemplates = null;
        cbTransformer = null;
        textLog = null;
        progressBar = null;
        fileField = null;
        
        super.close();
    }
    
    private void applyReportFileProperties(ReportTemplate reportFile) {
        if (reportFile != null)
            panelSettings.applySettings(reportFile.getProperties());
    }
    
    private void saveReportFileProperties(ReportTemplate reportFile) {
        if (reportFile != null)
            panelSettings.saveSettings(reportFile.getProperties(), true);
    }    
    
    private void applyReportSelection() {
        if (cbTransformer.isEnabled() && cbTransformer.getSelectedIndex() > -1) {
            
            cbTemplates.removeItemListener(this);
            
            XmlTransformer transformer = (XmlTransformer) cbTransformer.getSelectedItem();
            cbTemplates.removeAllItems();
            Collection<ReportTemplate> templates = 
                new ReportTemplates(true).getReportFiles(transformer.getType());
            
            for (ReportTemplate rt : templates)
                cbTemplates.addItem(rt);
            
            cbTemplates.setSelectedIndex(0);
            applyReportFileProperties((ReportTemplate) cbTemplates.getSelectedItem());
        }
        
        cbTemplates.addItemListener(this);
    }
    
    private void buildDialog() {
        //**********************************************************
        //Input Panel
        //**********************************************************
        JPanel panelInput = new JPanel();
        panelInput.setLayout(Layout.getGBL());
        panelInput.add(fileField,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                      ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                       new Insets( 5, 5, 5, 5), 0, 0));
        
        fileField.setValue(DcSettings.getString(DcRepository.Settings.stReportFile));
        
        panelInput.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblTargetFile")));        
        
        //**********************************************************
        //Report Type
        //**********************************************************
        JPanel panelReport = new JPanel(false);
        panelReport.setLayout(Layout.getGBL());

        cbTransformer.setActionCommand("applyReport");
        cbTransformer.addActionListener(this);

        for (XmlTransformer transformer : XmlTransformers.getTransformers())
            cbTransformer.addItem(transformer);

        JLabel lblTransformers = ComponentFactory.getLabel(DcResources.getText("lblReportFormat"));
        
        panelReport.add(lblTransformers,Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                       ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        new Insets( 5, 5, 5, 5), 0, 0));
        panelReport.add(cbTransformer,  Layout.getGBC( 2, 0, 1, 1, 1.0, 1.0
                       ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        new Insets( 5, 5, 5, 5), 0, 0));
        panelReport.add(cbTemplates,      Layout.getGBC( 1, 1, 2, 1, 1.0, 1.0
                       ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        new Insets( 5, 5, 5, 5), 0, 0));
        
        panelReport.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblReportSelection")));

    	//**********************************************************
        //Actions Panel
        //**********************************************************
        JPanel panelActions = new JPanel();
        panelActions.setLayout(Layout.getGBL());

        buttonRun.setActionCommand("createReport");
        buttonRun.addActionListener(this);
        buttonStop.setActionCommand("cancel");
        buttonStop.addActionListener(this);
        buttonResults.setActionCommand("showResults");
        buttonResults.addActionListener(this);
        buttonClose.setActionCommand("close");
        buttonClose.addActionListener(this);

        buttonResults.setMnemonic(KeyEvent.VK_O);

        panelActions.add(buttonRun,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        panelActions.add(buttonStop,    Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        panelActions.add(buttonResults,   Layout.getGBC( 2, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        panelActions.add(buttonClose,     Layout.getGBC( 4, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));

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
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelLog.add(scroller, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0));

        panelLog.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblLog")));

        //**********************************************************
        //Main Panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());
        
        
        JPanel panel = new JPanel();
        panel.add(panelSettings);
        panel.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblSettings")));

        this.getContentPane().add(
                panelReport,    Layout.getGBC( 0, 0, 1, 1, 20.0, 20.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(
                panel,          Layout.getGBC( 1, 0, 1, 2, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(
                panelInput,     Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(
                panelActions,   Layout.getGBC( 0, 3, 2, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                new Insets( 0, 0, 0, 0), 0, 0));
        this.getContentPane().add(
                panelProgress,  Layout.getGBC( 0, 4, 2, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets( 0, 0, 0, 0), 0, 0));
        this.getContentPane().add(
                panelLog,       Layout.getGBC( 0, 5, 2, 1, 40.0, 40.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets( 0, 5, 0, 5), 0, 0));
        
        this.setResizable(true);
        this.pack();
        
        Dimension size = DcSettings.getDimension(DcRepository.Settings.stReportingDialogSize);
        setSize(size);

        setCenteredLocation();
        applyReportSelection();
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("showResults"))
            showResults();
        else if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("cancel"))
            cancel();
        else if (ae.getActionCommand().equals("createReport"))
            createReport();      
        else if (ae.getActionCommand().equals("applyReport"))
            applyReportSelection();      
    }

    public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == ItemEvent.SELECTED)
            applyReportFileProperties((ReportTemplate) ie.getItem());    
        
        if (ie.getStateChange() == ItemEvent.DESELECTED)
            saveReportFileProperties((ReportTemplate) ie.getItem());
    }
}
