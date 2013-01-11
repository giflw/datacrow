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

package net.datacrow.console.windows;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.filechooser.FileFilter;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFileField;
import net.datacrow.console.components.DcMultiLineToolTip;
import net.datacrow.console.components.panels.BackupFilePreviewPanel;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.backup.Backup;
import net.datacrow.core.backup.IBackupRestoreListener;
import net.datacrow.core.backup.Restore;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class BackupDialog extends DcDialog implements ActionListener, IBackupRestoreListener {

    private static Logger logger = Logger.getLogger(BackupDialog.class.getName());
    
    private DcFileField fileFieldTarget;
    private DcFileField fileFieldSource;

    private JButton buttonBackup = ComponentFactory.getButton(DcResources.getText("lblBackup"));
    private JButton buttonRestore = ComponentFactory.getButton(DcResources.getText("lblRestore"));
    private JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
    
    private JCheckBox checkRestoreModules = ComponentFactory.getCheckBox(DcResources.getText("lblRestoreModules"));
    private JCheckBox checkRestoreReports = ComponentFactory.getCheckBox(DcResources.getText("lblRestoreReports"));
    private JCheckBox checkRestoreDatabase = ComponentFactory.getCheckBox(DcResources.getText("lblRestoreDatabase"));
    
    private JTextArea textLog = ComponentFactory.getTextArea();
    
    private JTextArea textComment = ComponentFactory.getTextArea();
    
    private JProgressBar progressBar = new JProgressBar();
    
    private boolean canBeClosed = true;

    public BackupDialog() {
        super(DataCrow.mainFrame);
        setTitle(DcResources.getText("lblBackupAndRestore"));
        setHelpIndex("dc.tools.backup_restore");

        BackupFileFilter filter = new BackupFileFilter();
        fileFieldSource = ComponentFactory.getFileField(false, false, filter);
        fileFieldSource.setPreview(new BackupFilePreviewPanel());
        
        fileFieldTarget = ComponentFactory.getFileField(false, true);

        buildDialog();
        fileFieldTarget.setValue(DcSettings.getString(DcRepository.Settings.stBackupLocation));
    }

    @Override
    public void notifyProcessed() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    @Override
    public void notifyStarted() {
        canBeClosed = false;
        buttonBackup.setEnabled(false);
        buttonRestore.setEnabled(false);
        buttonClose.setEnabled(false);

        textLog.setText("");
    }
    
    @Override
    public void notifyProcessingCount(int count) {
        initProgressBar(count);
    }

    @Override
    public void notifyStopped() {
        canBeClosed = true;
        
        buttonRestore.setEnabled(true);
        buttonBackup.setEnabled(true);
        buttonClose.setEnabled(true);
        canBeClosed = true;
        
        progressBar.setValue(progressBar.getMaximum());
    }

    private void restore() {
        File source = fileFieldSource.getFile();
        
        if (source != null && !source.isDirectory()) {
            Restore restore = new Restore(this, source);
            restore.setRestoreDatabase(checkRestoreDatabase.isSelected());
            restore.setRestoreModules(checkRestoreModules.isSelected());
            restore.setRestoreReports(checkRestoreReports.isSelected());
            restore.start();
        } else {
            DcSwingUtilities.displayWarningMessage("msgSelectBackupFile");
        }
    }
    
    private void backup() {
        File directory = fileFieldTarget.getFile();
        if (directory != null) {
            Backup bck = new Backup(this, fileFieldTarget.getFile(), textComment.getText().trim());
            bck.start();
        } else {
            DcSwingUtilities.displayWarningMessage("msgSelectOutputDir");
        }
    }
    
    @Override
    public void sendError(Exception e) {
        DcSwingUtilities.displayErrorMessage(DcResources.getText("msgBackupFileCreationError", e.getMessage()));
        logger.error(e, e);
    }

    @Override
    public void sendMessage(String message) {
        textLog.insert(message + '\n', 0);
        textLog.setCaretPosition(0);
    }

    public void initProgressBar(int maxValue) {
        progressBar.setValue(0);
        progressBar.setMaximum(maxValue);
    }

    private void buildDialog() {
        getContentPane().setLayout(Layout.getGBL());

        //**********************************************************
        //Create Backup Panel
        //**********************************************************
        JPanel panelBackup = new JPanel() {
            @Override
            public JToolTip createToolTip() {
                return new DcMultiLineToolTip();
            }
        };

        panelBackup.setLayout(Layout.getGBL());

        buttonBackup.addActionListener(this);
        buttonBackup.setActionCommand("backup");
        
        JScrollPane commentScroller = new JScrollPane(textComment);
        commentScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelBackup.add(fileFieldTarget, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 5, 5, 5), 0, 0));
        panelBackup.add(buttonBackup,  Layout.getGBC( 1, 0, 1, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 5, 5), 0, 0));
        panelBackup.add(ComponentFactory.getLabel(DcResources.getText("lblComment")), 
                Layout.getGBC( 0, 1, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
        panelBackup.add(commentScroller, Layout.getGBC( 0, 2, 2, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        panelBackup.setToolTipText(DcResources.getText("tpBackup"));
        panelBackup.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblBackupHeader")));

        //**********************************************************
        //Restore Restore Panel
        //**********************************************************
        JPanel panelRestore = new JPanel()  {
            @Override
            public JToolTip createToolTip() {
                return new DcMultiLineToolTip();
            }
        };

        panelRestore.setLayout(Layout.getGBL());
        buttonRestore.addActionListener(this);
        buttonRestore.setActionCommand("restore");

        panelRestore.add(fileFieldSource, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 5, 5, 5), 0, 0));
        panelRestore.add(buttonRestore,  Layout.getGBC( 1, 0, 1, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 5, 5), 0, 0));
        panelRestore.add(checkRestoreDatabase,  Layout.getGBC( 0, 1, 2, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 5, 5, 5), 0, 0));
        panelRestore.add(checkRestoreModules,   Layout.getGBC( 0, 2, 2, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 5, 5, 5), 0, 0));
        panelRestore.add(checkRestoreReports,   Layout.getGBC( 0, 3, 2, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 5, 5, 5), 0, 0));

        checkRestoreReports.setSelected(DcSettings.getBoolean(DcRepository.Settings.stRestoreReports));
        checkRestoreModules.setSelected(DcSettings.getBoolean(DcRepository.Settings.stRestoreModules));
        checkRestoreDatabase.setSelected(DcSettings.getBoolean(DcRepository.Settings.stRestoreDatabase));
        
        panelRestore.setToolTipText(DcResources.getText("tpRestore"));
        panelRestore.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblRestoreHeader")));

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
        //Progress panel
        //**********************************************************
        JPanel panelProgress = new JPanel();
        panelProgress.setLayout(Layout.getGBL());
        panelProgress.add(progressBar, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                         ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                          new Insets(5, 5, 5, 5), 0, 0));


        //**********************************************************
        //Main panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        buttonBackup.setMnemonic('B');
        buttonRestore.setMnemonic('R');

        this.getContentPane().add(panelBackup,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                  new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelRestore, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                  new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelLog,     Layout.getGBC( 0, 2, 1, 1, 10.0, 10.0
                                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                  new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelProgress,Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                  new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(buttonClose,  Layout.getGBC( 0, 4, 1, 1, 0.0, 0.0
                                 ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                                  new Insets( 5, 5, 5, 10), 0, 0));
        pack();

        Dimension size = DcSettings.getDimension(DcRepository.Settings.stBackupDialogSize);
        setSize(size);

        setCenteredLocation();
    }

    @Override
    public void close() {
        if (!canBeClosed)
            return;
        
        DcSettings.set(DcRepository.Settings.stBackupDialogSize, getSize());
        DcSettings.set(DcRepository.Settings.stRestoreDatabase, checkRestoreDatabase.isSelected());
        DcSettings.set(DcRepository.Settings.stRestoreModules, checkRestoreModules.isSelected());
        DcSettings.set(DcRepository.Settings.stRestoreReports, checkRestoreReports.isSelected());
        
        fileFieldTarget = null;
        fileFieldSource = null;
        buttonBackup = null;
        buttonRestore = null;
        textLog = null;
        progressBar = null;
        
        super.close();
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("backup"))
            backup();
        else if (ae.getActionCommand().equals("restore"))
            restore();
        else if (ae.getActionCommand().equals("close"))
            close();
    }
    
    /**
     * Image file filter
     */
    private static class BackupFileFilter extends FileFilter {

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            } else if (Utilities.getExtension(file).equals("bck")) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return DcResources.getText("lblBckFileFilter");
        }
    }
}
