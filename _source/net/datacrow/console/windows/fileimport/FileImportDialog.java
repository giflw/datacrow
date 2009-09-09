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

package net.datacrow.console.windows.fileimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFrame;
import net.datacrow.console.components.panels.OnlineServicePanel;
import net.datacrow.console.components.panels.OnlineServiceSettingsPanel;
import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcProperty;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.fileimporters.FileImporter;
import net.datacrow.fileimporters.IFileImportClient;
import net.datacrow.settings.Settings;
import net.datacrow.util.FileNameFilter;

import org.apache.log4j.Logger;

public class FileImportDialog extends DcFrame implements IFileImportClient, ActionListener {

    private static Logger logger = Logger.getLogger(FileImportDialog.class.getName());
    
    protected boolean cancelled = false;
    
    protected JProgressBar progressBar = new JProgressBar();

    protected JCheckBox checkDirNameAsTitle = ComponentFactory.getCheckBox(DcResources.getText("lblUseDirNameAsTitle"));
    protected JComboBox fldContainer;
    protected JComboBox fldStorageMedium;
    
    protected JButton buttonRun = ComponentFactory.getButton(DcResources.getText("lblRun"));
    protected JButton buttonStop = ComponentFactory.getButton(DcResources.getText("lblStop"));
    protected JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
    
    protected JTextArea textLog = ComponentFactory.getTextArea();
    protected JTextArea textTitleCleanup = ComponentFactory.getTextArea();
    protected JTextArea textTitleCleanupRegex = ComponentFactory.getTextArea();
    
    private OnlineServicePanel panelServer;
    private OnlineServiceSettingsPanel panelServerSettings;
    private LocalArtSettingsPanel panelLocalArt;
    private FileImportFileSelectPanel panelFs;
    
    protected Settings settings;
    protected FileImporter importer;
    
    public FileImportDialog(FileImporter importer) {
        
        super(DcResources.getText("lblXImport", 
                DcModules.get(importer.getModule()).getObjectName()), 
                IconLibrary._icoImport);

        this.importer = importer;
        this.settings = DcModules.get(importer.getModule()).getSettings();

        build();
        
        setHelpIndex("dc.tools.fileinfoimport");
        setSize(settings.getDimension(DcRepository.ModuleSettings.stImportCDDialogSize));
        setCenteredLocation();
    }
    
    public int getDirectoryUsage() {
        return checkDirNameAsTitle.isSelected() ? 1 : 0;
    }
    
    public DcModule getModule() {
        return DcModules.get(importer.getModule());
    }

    public DcProperty getStorageMedium() {
        DcModule module = DcModules.get(importer.getModule());
        
        DcProperty medium = null;
        if (module.getPropertyModule(DcModules._STORAGEMEDIA) != null) {
            try {
                medium = (DcProperty) fldStorageMedium.getSelectedItem();
            } catch (Exception exp) {}
        }
            
        return medium;
    }

    public void initProgressBar(int maxValue) {
        if (progressBar != null) {
            progressBar.setValue(0);
            progressBar.setMaximum(maxValue);
        }
    }

    public void updateProgressBar(int value) {
        if (progressBar != null) progressBar.setValue(value);
    }

    public void addMessage(String message) {
        if (textLog != null) textLog.insert(message + '\n', 0);
    }

    public void addError(Throwable e) {
        logger.error(e, e);
        new MessageBox(DcResources.getText("msgUnexpectedProblemDuringFileImport", e.getMessage()), 
                       MessageBox._ERROR);
    }

    public void finish() {
        if (buttonRun != null) buttonRun.setEnabled(true);
    }

    public boolean cancelled() {
        return cancelled;
    }      
    
    public boolean useOnlineServices() {
        return panelServer != null ? panelServer.useOnlineService() : false;
    }    
    
    public DcObject getDcContainer() {
        return fldContainer != null && fldContainer.getSelectedIndex() > 0 ? (DcObject) fldContainer.getSelectedItem() : null;
    }

    public Region getRegion() {
        return panelServer != null ? panelServer.getRegion() : null;
    }

    public IServer getServer() {
        return panelServer != null ? panelServer.getServer() : null;
    }

    public SearchMode getSearchMode() {
        return panelServer != null ? panelServer.getMode() : null;
    }
    
    private boolean checkRegex() {
        boolean valid = true;
        
        String regex = textTitleCleanupRegex.getText();
        if (regex.trim().length() > 0) {
            try {
                Pattern.compile(regex);
            } catch (PatternSyntaxException pse) {
                new MessageBox(DcResources.getText("msgPatternError", pse.getMessage()), MessageBox._ERROR);
                valid = false;
            }
        }
        
        return valid;
    }
    
    protected void startImport() {
        if (!checkRegex())
            return;
        
        denyActions();
        saveSettings();
        try {
            cancelled = false;
            importer.setClient(this);
            importer.parse(panelFs.getFiles(false));
        } catch (Exception e) {
            new MessageBox(e.getMessage(), MessageBox._INFORMATION);
            finish();
        }
    }
    
    private void saveSettings() {
        settings.set(DcRepository.ModuleSettings.stImportCDDialogSize, getSize());
        settings.set(DcRepository.ModuleSettings.stTitleCleanup, textTitleCleanup.getText());
        settings.set(DcRepository.ModuleSettings.stTitleCleanupRegex, textTitleCleanupRegex.getText());
    }
    
    public void denyActions() {
        progressBar.setValue(0);
        textLog.setText("");

        buttonRun.setEnabled(false);
    }

    @Override
    public void close() {
        cancel();
        saveSettings();
        
        panelLocalArt.clear();
        
        if (panelServer != null) {
            panelServer.clear();
            panelServer = null;
        }
        
        if (panelServerSettings != null) {
            panelServerSettings.save();
            panelServerSettings.clear();
            panelServerSettings = null;
        }
        
        fldContainer = null;
        fldStorageMedium = null;
        buttonRun = null;
        buttonStop = null;
        buttonClose = null;
        textLog = null;
        progressBar = null;
        settings = null;
        
        panelFs.clear();
        panelFs = null;
        
        super.close();
    }

    protected void cancel() {
        cancelled = true;
    }
    
    protected JPanel getDirectoryUsagePanel() {
        JPanel panelDirs = new JPanel();
        panelDirs.setLayout(Layout.getGBL());
        panelDirs.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblDirectoryUsage")));
        panelDirs.add(checkDirNameAsTitle, Layout.getGBC( 0, 3, 3, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 5, 0, 5), 0, 0));
        
        checkDirNameAsTitle.setSelected(false);
        return panelDirs;
    }
    
    private JPanel getTitleCleanupPanel() {
      //**********************************************************
        //Title cleanup
        //**********************************************************
        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());
        
        JScrollPane scroller1 = new JScrollPane(textTitleCleanup);
        scroller1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollPane scroller2 = new JScrollPane(textTitleCleanupRegex);
        scroller2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        textTitleCleanup.setText(settings.getString(DcRepository.ModuleSettings.stTitleCleanup));
        textTitleCleanupRegex.setText(settings.getString(DcRepository.ModuleSettings.stTitleCleanupRegex));
        
        panel.add(ComponentFactory.getLabel(DcResources.getText("lblRemoveWords")), 
                 Layout.getGBC( 0, 0, 2, 1, 5.0, 5.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));  
        panel.add(scroller1, Layout.getGBC( 0, 1, 2, 1, 50.0, 50.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));  
        panel.add(ComponentFactory.getLabel(DcResources.getText("lblRegexPattern")), 
                Layout.getGBC( 0, 2, 2, 1, 5.0, 5.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));  
        
        panel.add(scroller2, Layout.getGBC( 0, 3, 2, 1, 50.0, 50.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));          
        
        return panel;
    }
    
    private JPanel getSettingsPanel() {
        DcModule module = DcModules.get(importer.getModule());
        
        //**********************************************************
        //Input panel
        //**********************************************************
        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());
        
        JPanel panelMediaInfo = new JPanel();
        panelMediaInfo.setLayout(Layout.getGBL());

        JLabel lblContainer = ComponentFactory.getLabel(DcResources.getText("lblContainer"));
        fldContainer = ComponentFactory.getObjectCombo(DcModules._CONTAINER);
        
        JLabel labelMedium = ComponentFactory.getLabel(DcResources.getText("lblStorageMedium"));
        fldStorageMedium = ComponentFactory.getObjectCombo(DcModules.get(importer.getModule()).getPropertyModule(DcModules._STORAGEMEDIA).getIndex());

        panelMediaInfo.add(lblContainer,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 5, 5, 5), 0, 0));
        panelMediaInfo.add(fldContainer, Layout.getGBC( 1, 0, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 5, 5, 5), 0, 0));

        JButton btnCreateContainer = ComponentFactory.getIconButton(IconLibrary._icoOpenNew);
        btnCreateContainer.setActionCommand("createContainer");
        btnCreateContainer.addActionListener(this);
                
        panelMediaInfo.add(btnCreateContainer, Layout.getGBC( 2, 0, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(0, 5, 5, 5), 0, 0));
        
        
        if (module.getPropertyModule(DcModules._STORAGEMEDIA) != null) {
            panelMediaInfo.add(labelMedium,    Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(0, 5, 5, 5), 0, 0));
            panelMediaInfo.add(fldStorageMedium,    Layout.getGBC( 1, 1, 1, 1, 20.0, 20.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(0, 5, 5, 5), 0, 0));
        }
        
        panelMediaInfo.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblMediaInfo")));
        
        panel.add(panelMediaInfo, Layout.getGBC( 0, 0, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(15, 5, 5, 5), 0, 0));  

        //**********************************************************
        //Directory usage
        //**********************************************************
        
        if (getDirectoryUsagePanel() != null)
            panel.add(getDirectoryUsagePanel(), Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets(10, 0, 0, 0), 0, 0));
        
      
        
        return panel;
    }

    private void createContainer() {
        DcObject dco = DcModules.get(DcModules._CONTAINER).getDcObject();
        ItemForm itemForm = new ItemForm(null, false, false, dco, true);
        itemForm.setVisible(true);
    }
    
    protected void build() {
        //**********************************************************
        //Online search panel
        //**********************************************************
        DcModule module = DcModules.get(importer.getModule());
        JPanel panelOs = new JPanel();
        panelOs.setLayout(Layout.getGBL());
        
        if (module.deliversOnlineService()) {
            panelServer = new OnlineServicePanel(module.getOnlineServices().getServers(), true, true);
            panelServerSettings = new OnlineServiceSettingsPanel(null, false, false, false, false, module.getIndex());
            
            panelOs.add(panelServer,     Layout.getGBC( 0, 3, 3, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(15, 5, 0, 5), 0, 0));
            panelOs.add(panelServerSettings,     Layout.getGBC( 0, 4, 3, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(15, 5, 0, 5), 0, 0));
        }

        //**********************************************************
        //Actions Panel
        //**********************************************************
        JPanel panelActions = new JPanel();
        panelActions.setLayout(Layout.getGBL());

        buttonRun.addActionListener(this);
        buttonRun.setActionCommand("import");
        
        buttonStop.addActionListener(this);
        buttonStop.setActionCommand("cancel");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        panelActions.add(buttonRun,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                     new Insets( 5, 5, 5, 5), 0, 0));
        panelActions.add(buttonStop,    Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                     new Insets( 5, 5, 5, 5), 0, 0));
        panelActions.add(buttonClose,     Layout.getGBC( 2, 0, 1, 1, 1.0, 1.0
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

        textLog.setEditable(false);
        JScrollPane scroller = new JScrollPane(textLog);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelLog.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblLog")));
        panelLog.add(scroller, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0));
        
        
        //**********************************************************
        //Local Art
        //**********************************************************
        panelLocalArt = new LocalArtSettingsPanel(importer.getModule());

        FileNameFilter filter =
            importer.getSupportedFileTypes() != null &&
            importer.getSupportedFileTypes().length > 0 ?
            new FileNameFilter(importer.getSupportedFileTypes(), true) : null;
                 
            
        //**********************************************************
        //Files / Directories
        //**********************************************************            
        panelFs = new FileImportFileSelectPanel(filter, module.getIndex());
        
        //**********************************************************
        //Tabs Panel
        //**********************************************************
        JTabbedPane tp = ComponentFactory.getTabbedPane();
        tp.addTab(DcResources.getText("lblDirectoriesFiles"), IconLibrary._icoOpen , panelFs);
        tp.addTab(DcResources.getText("lblSettings"), IconLibrary._icoSettings16, getSettingsPanel());
        tp.addTab(DcResources.getText("lblTitleCleanup"),  IconLibrary._icoSettings16, getTitleCleanupPanel());
        
        if (module.deliversOnlineService())
            tp.addTab(DcResources.getText("lblOnlineSearch"), IconLibrary._icoSearchOnline16,panelOs);
        
        tp.addTab(DcResources.getText("lblLocalArt"), IconLibrary._icoPicture, panelLocalArt);

        
        //**********************************************************
        //Main Panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());

        this.getContentPane().add(
                 tp,      Layout.getGBC( 0, 1, 1, 1, 5.0, 5.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(
                 panelActions, Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        this.getContentPane().add(
                 panelProgress, Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        this.getContentPane().add(
                 panelLog, Layout.getGBC( 0, 4, 2, 1, 5.0, 5.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 0, 0, 0), 0, 0));

        pack();
        setSize(settings.getDimension(DcRepository.ModuleSettings.stImportCDDialogSize));
        setResizable(true);
        setCenteredLocation();
    }    
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("cancel"))
            cancel();
        else if (ae.getActionCommand().equals("import"))
            startImport();
        else if (ae.getActionCommand().equals("createContainer"))
            createContainer();
    }    
}
