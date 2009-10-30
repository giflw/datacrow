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

package net.datacrow.console.windows.drivemanager;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.windows.BrowserDialog;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcResources;
import net.datacrow.drivemanager.DriveManager;
import net.datacrow.drivemanager.JobAlreadyRunningException;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcSwingUtilities;

public class DriveScannerPanel extends DriveManagerPanel implements ActionListener {

    private DcTable tableSkipDirs;
    private DriveSelectorField driveSelector;
    private JButton buttonAdd;
    private JButton buttonRemove;    
    
    public DriveScannerPanel() {
        super();
        DriveManager.getInstance().addScannerListener(this);
    }
    
    @Override
    protected String getHelpText() {
        return DcResources.getText("msgDriveScannerHelp");
    }
    
    @Override
    protected void allowActions(boolean b) {
        driveSelector.setEnabled(b);
        tableSkipDirs.setEnabled(b);
        buttonAdd.setEnabled(b);
        buttonRemove.setEnabled(b);
    }
    
    @Override
    protected ImageIcon getIcon() {
        return IconLibrary._icoDriveScanner;
    }

    @Override
    protected String getTitle() {
        return DcResources.getText("lblDriveScanner");
    }    
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        
        if (tableSkipDirs != null) {
            tableSkipDirs.setFont(ComponentFactory.getStandardFont());
            driveSelector.setFont(ComponentFactory.getSystemFont());
            buttonAdd.setFont(ComponentFactory.getSystemFont());
            buttonRemove.setFont(ComponentFactory.getSystemFont());
        }
    }
    
    
    @Override
    protected void start() throws JobAlreadyRunningException {
        DriveManager dm = DriveManager.getInstance();
        dm.setDrives(driveSelector.getDrives());
        dm.setExcludedDirectories(getExcludedDirectories());
        dm.startScanners();
    }
    
    @Override
    protected void stop() {
        DriveManager.getInstance().stopScanners();
    }
    
    private Collection<String> getExcludedDirectories() {
        Collection<String> dirs = new ArrayList<String>();
        for (int i = 0; i < tableSkipDirs.getRowCount(); i++) {
            dirs.add((String) tableSkipDirs.getValueAt(i, 0));
        }
        return dirs;
    }

    @Override
    protected void build() {
        super.build();

        // drive selector
        driveSelector = new DriveSelectorField();
        driveSelector.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblDrivesToScan")));
        
        // excluded directories
        JPanel panelExludeDirs = new JPanel();
        panelExludeDirs.setLayout(Layout.getGBL());

        tableSkipDirs = ComponentFactory.getDCTable(true, false);
        tableSkipDirs.setColumnCount(1);
        TableColumn columnDir = tableSkipDirs.getColumnModel().getColumn(0);
        JTextField textField = ComponentFactory.getTextFieldDisabled();
        columnDir.setCellEditor(new DefaultCellEditor(textField));
        columnDir.setHeaderValue(DcResources.getText("lblDirectory"));
        
        JPanel panelActions = new JPanel();
        buttonAdd = ComponentFactory.getButton(DcResources.getText("lblAdd"));
        buttonRemove = ComponentFactory.getButton(DcResources.getText("lblRemove"));
        
        buttonAdd.addActionListener(this);
        buttonRemove.addActionListener(this);
        buttonAdd.setActionCommand("addExcludedDir");
        buttonRemove.setActionCommand("removeExcludedDir");
        
        panelActions.add(buttonAdd);
        panelActions.add(buttonRemove);
        
        panelExludeDirs.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblExludeDirs")));        
        panelExludeDirs.add(new JScrollPane(tableSkipDirs), 
                Layout.getGBC(0, 0, 1, 1, 10.0, 10.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        panelExludeDirs.add(panelActions, 
                Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        
        // main
        add(driveSelector, Layout.getGBC(0, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        add(panelExludeDirs, Layout.getGBC(0, 3, 1, 1, 5.0, 5.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        
        String[] excludedDirs = DcSettings.getStringArray(DcRepository.Settings.stDriveManagerExcludedDirs);
        
        if (excludedDirs != null) {
            for (String excludedDir : excludedDirs)
                tableSkipDirs.addRow(new Object[] {excludedDir});
        }
        
        driveSelector.setValue(DcSettings.getStringArray(DcRepository.Settings.stDriveManagerDrives));
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("addExcludedDir")) {
            BrowserDialog dlg = new BrowserDialog(DcResources.getText("msgSelectDirToExclude"));
            File directory = dlg.showSelectDirectoryDialog(this, null);
            
            if (directory != null) 
                tableSkipDirs.addRow(new Object[] {directory.toString()});
            
        } else if (e.getActionCommand().equals("removeExcludedDir")) {
            if (tableSkipDirs.getSelectedRow() != -1) {
                for (int i = tableSkipDirs.getSelectedRows().length; i > 0; i--)
                    tableSkipDirs.removeRow(tableSkipDirs.getSelectedRows()[i - 1]);
            } else {
                DcSwingUtilities.displayMessage("msgSelectItemBeforeDelete");
            }
        }
    }
}
