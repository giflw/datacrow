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
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFrame;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcResources;
import net.datacrow.drivemanager.DriveManager;
import net.datacrow.settings.DcSettings;

public class DriveManagerDialog extends DcFrame implements ActionListener {

    private static DriveManagerDialog dlg;
    
    private final DrivePollerPanel pollerPanel;
    private final FileSynchronizerPanel synchronizerPanel;
    private final DriveScannerPanel scannerPanel;
    private final JTabbedPane tp;
    
    public static DriveManagerDialog getInstance() {
        dlg = dlg == null ? new DriveManagerDialog() : dlg;
        return dlg;
    }
    
    public static boolean isInitialized() {
        return dlg != null;
    }
    
    private DriveManagerDialog() {
        super(DcResources.getText("lblDriveManager"), IconLibrary._icoDriveManager);
        
        pollerPanel = new DrivePollerPanel();
        synchronizerPanel = new FileSynchronizerPanel();
        scannerPanel = new DriveScannerPanel();
        tp = ComponentFactory.getTabbedPane();
        
        setHelpIndex("dc.tools.drivemanager");
        
        build();
        
        setSize(DcSettings.getDimension(DcRepository.Settings.stDriveManagerDialogSize));
        
        setCenteredLocation();
    }

    @Override
    public void close() {
        saveSettings();
        setVisible(false);
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        
        if (tp != null) {
            tp.setFont(ComponentFactory.getSystemFont());
            scannerPanel.setFont(font);
            synchronizerPanel.setFont(font);
            pollerPanel.setFont(font);
        }
    }
    
    private void saveSettings() {
        Collection<String> excluded = DriveManager.getInstance().getExcludedDirs();
        Collection<File> drives = DriveManager.getInstance().getDrives();
        String[] s = new String[drives.size()];
        int i = 0;
        for (File drive : drives)
            s[i++] = drive.toString();
        
        DcSettings.set(DcRepository.Settings.stDriveManagerDialogSize, getSize());
        DcSettings.set(DcRepository.Settings.stDriveManagerExcludedDirs, excluded.toArray(new String[0]));
        DcSettings.set(DcRepository.Settings.stDriveManagerDrives, s);
    }

    private void build() {
        tp.addTab(scannerPanel.getTitle(), scannerPanel.getIcon(),  scannerPanel);
        tp.addTab(pollerPanel.getTitle(), pollerPanel.getIcon(), pollerPanel);
        tp.addTab(synchronizerPanel.getTitle(), synchronizerPanel.getIcon(), synchronizerPanel);
        
        JPanel panelActions = new JPanel();
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        buttonClose.setMnemonic('C');
        panelActions.add(buttonClose);
        
        getContentPane().setLayout(Layout.getGBL());
        getContentPane().add(tp, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        getContentPane().add(panelActions, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        
        setFont(ComponentFactory.getStandardFont());
        
        pack();
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("close"))
            close();
    }    
}
