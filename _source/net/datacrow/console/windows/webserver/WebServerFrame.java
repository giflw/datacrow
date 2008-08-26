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

package net.datacrow.console.windows.webserver;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFrame;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class WebServerFrame extends DcFrame implements ActionListener {
    
    private final JTabbedPane tp;
    private final WebServerControlPanel controlPanel;
    
    private static WebServerFrame instance;
    
    public static WebServerFrame getInstance() {
        instance = instance == null ? new WebServerFrame() : instance;
        return instance;
    }
    
    public static boolean isInitialized() {
        return instance != null;
    }
    
    private WebServerFrame() {
        super(DcResources.getText("lblWebServer"), IconLibrary._icoWebServer);
        
        controlPanel = new WebServerControlPanel();
        tp = ComponentFactory.getTabbedPane();
        
        setHelpIndex("dc.webserver");
        
        build();
        
        setSize(DcSettings.getDimension(DcRepository.Settings.stWebServerFrameSize));
        setCenteredLocation();
    }
    
    @Override
    public void close() {
        saveSettings();
        setVisible(false);
    }
    
    private void saveSettings() {
        controlPanel.saveSettings();
        DcSettings.set(DcRepository.Settings.stWebServerFrameSize, getSize());
    }
    
    private void build() {
        tp.addTab(controlPanel.getTitle(), controlPanel.getIcon(),  controlPanel);
        
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
