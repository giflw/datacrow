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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcNumberField;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.web.DcWebServer;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.BrowserLauncher;

import org.apache.log4j.Logger;

public class WebServerControlPanel extends JPanel implements java.awt.event.ActionListener {
    
    private static Logger logger = Logger.getLogger(WebServerControlPanel.class.getName());
    
    private final JButton buttonStop = ComponentFactory.getIconButton(IconLibrary._icoStop);
    private final JButton buttonStart = ComponentFactory.getIconButton(IconLibrary._icoStart);
    private final JButton buttonLaunch = ComponentFactory.getButton(DcResources.getText("lblGoToTheWebApp"));
    
    private final DcNumberField fldPort = ComponentFactory.getNumberField();
    private final JTextField fldStatus = ComponentFactory.getTextFieldDisabled(); 

    public WebServerControlPanel() {
        build();
        
        fldPort.setValue(DcSettings.getInt(DcRepository.Settings.stWebServerPort));
        isRunning(DcWebServer.getInstance().isRunning());
    }
    
    public String getTitle() {
        return DcResources.getText("lblWebServerControl");
    }
    
    public ImageIcon getIcon() {
        return IconLibrary._icoSettings;
    }
    
    public void saveSettings() {
        DcSettings.set(DcRepository.Settings.stWebServerPort, fldPort.getValue());
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        
        if (buttonStop != null) {
            buttonStop.setFont(font);
            buttonStart.setFont(font);
            fldStatus.setFont(font);
            fldPort.setFont(font);
            buttonLaunch.setFont(font);
        }
    }
    
    private void start() {
        try {
            long port = (Long) fldPort.getValue();
            if (port < 1) 
                fldPort.setValue(8080);
            
            saveSettings();
            
            DcWebServer.getInstance().start();
            setMessage(DcResources.getText("msgWebServerStarted"));
            isRunning(true);
        } catch (Exception e) {
            setMessage(e.getMessage());
            logger.error(e, e);
            new MessageBox(DcResources.getText("msgWebServerStartError", e.getMessage()), MessageBox._ERROR);
        }
    }

    private void launch() {
        try {
            URL url = new URL("http://localhost:" + fldPort.getValue() + "/datacrow");
            BrowserLauncher.openURL(url.toString());
        } catch (Exception exp) {
            new MessageBox(exp.getMessage(), MessageBox._ERROR);
        }
    }
    
    private void stop() {
        try {
            DcWebServer.getInstance().stop();
            setMessage(DcResources.getText("msgWebServerStopped"));
            isRunning(false);
        } catch (Exception e) {
            setMessage(e.getMessage());
            logger.error(e, e);
            new MessageBox(DcResources.getText("msgWebServerStopError", e.getMessage()), MessageBox._ERROR);
        }
    }
    
    private void setMessage(String msg) {
        fldStatus.setText(msg);
    }
    
    private void isRunning(boolean b) {
        buttonStop.setEnabled(b);
        buttonStart.setEnabled(!b);
        fldPort.setEnabled(!b);
        buttonLaunch.setEnabled(b);
    }    
    
    private void build() {
        setLayout(Layout.getGBL());

        // SETTINGS PANEL
        JPanel panelSettings = new JPanel();
        panelSettings.setLayout(Layout.getGBL());
        panelSettings.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblWebServerSettings")));
        
        JLabel labelPort = ComponentFactory.getLabel(DcResources.getText("lblWebServerPort"));
        panelSettings.add(labelPort,    Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        panelSettings.add(fldPort,      Layout.getGBC(1, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        
        // STATUS PANEL
        JPanel panelStatus = new JPanel();
        panelStatus.setLayout(Layout.getGBL());
        panelStatus.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblWebServerStatus")));
        
        
        JPanel panelActions = new JPanel();
        panelActions.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        buttonStart.setActionCommand("start");
        buttonStop.setActionCommand("stop");
        
        buttonStart.addActionListener(this);
        buttonStop.addActionListener(this);
        
        panelActions.add(buttonStart);
        panelActions.add(buttonStop);

        panelStatus.add(fldStatus,      Layout.getGBC(0, 0, 1, 1, 10.0, 10.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
            new Insets(5, 5, 5, 5), 0, 0));
        panelStatus.add(panelActions,   Layout.getGBC(1, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
            new Insets(0, 5, 5, 5), 0, 0));

        // MAIN PANEL
        add(panelSettings, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        add(panelStatus,   Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        
        buttonLaunch.setPreferredSize(new Dimension(200, buttonLaunch.getPreferredSize().height));
        buttonLaunch.setMaximumSize(new Dimension(200, buttonLaunch.getPreferredSize().height));
        buttonLaunch.setMinimumSize(new Dimension(200, buttonLaunch.getPreferredSize().height));
        buttonLaunch.setActionCommand("launch");
        buttonLaunch.addActionListener(this);
        
        add(buttonLaunch,   Layout.getGBC(0, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));        

    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("start")) 
            start();
        else if (e.getActionCommand().equals("stop"))
            stop();
        else if (e.getActionCommand().equals("launch"))
            launch();
    }
}
