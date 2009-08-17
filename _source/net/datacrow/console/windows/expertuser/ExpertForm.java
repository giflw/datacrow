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

package net.datacrow.console.windows.expertuser;

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

public class ExpertForm extends DcFrame implements ActionListener {

    public ExpertForm() {
        super(DcResources.getText("lblExpertUser"), IconLibrary._icoSettings32);
        
        buildForm();
        setHelpIndex("dc.tools.expertuser");
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stExpertFormSize, getSize());
        super.close();
    }
    
    private void buildForm() {
        JTabbedPane tabbedPane = ComponentFactory.getTabbedPane();
        
    	SystemInfoPanel systemInfoPanel = new SystemInfoPanel();
        MaintenancePanel maintenancePanel = new MaintenancePanel();
        PerformancePanel performancePanel = new PerformancePanel();
        QueryPanel queryPanel = new QueryPanel();
        
        JPanel systemPanel = new JPanel();
        systemPanel.setLayout(Layout.getGBL());
        
        systemInfoPanel.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblSystemInformation")));
        maintenancePanel.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblMaintenance")));
        performancePanel.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblPerformanceTweaking")));
        
        systemPanel.add(systemInfoPanel,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                  new Insets(5, 5, 5, 5), 0, 0));
        systemPanel.add(maintenancePanel, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                  new Insets(5, 5, 5, 5), 0, 0));
        systemPanel.add(performancePanel, Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                  new Insets(5, 5, 5, 5), 0, 0));

        JPanel panelActions = new JPanel();
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        buttonClose.setActionCommand("close");
        buttonClose.addActionListener(this);
        panelActions.add(buttonClose);
        
        tabbedPane.addTab(DcResources.getText("lblSystem"), IconLibrary._icoSettings16, systemPanel);
        tabbedPane.addTab(DcResources.getText("lblSQLTool"), IconLibrary._icoSQLTool, queryPanel);

        getContentPane().setLayout(Layout.getGBL());
        getContentPane().add(tabbedPane, Layout.getGBC( 0, 0, 1, 1, 50.0, 50.0
        		            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
							 new Insets(0, 0, 0, 0), 0, 0));
        getContentPane().add(panelActions, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                             new Insets(0, 0, 0, 10), 0, 0));
        
        pack();
        setSize(DcSettings.getDimension(DcRepository.Settings.stExpertFormSize));
        setCenteredLocation();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("close"))
            close();
    }
}
