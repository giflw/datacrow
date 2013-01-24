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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcPanel;
import net.datacrow.core.DataCrow;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

public class MaintenancePanel extends DcPanel implements ActionListener {

    public MaintenancePanel() {
        super(null, null);
        buildPanel();
    }
    
    private void buildPanel() {
        setLayout(Layout.getGBL());
        
        JButton buttonCompact = ComponentFactory.getButton(DcResources.getText("lblCompactAndShutdown"));
        buttonCompact.setToolTipText(DcResources.getText("tpCompactAndShutdown"));
        buttonCompact.addActionListener(this);
        buttonCompact.setActionCommand("compact");
        buttonCompact.setMinimumSize(new Dimension(250, 25));
        buttonCompact.setMaximumSize(new Dimension(250, 25));
        buttonCompact.setPreferredSize(new Dimension(250, 25));
        buttonCompact.setMnemonic('C');
        
        add(buttonCompact);
    }
    
    private void compactAndShutDown() {
        try {
        	DatabaseManager.execute("SHUTDOWN COMPACT");
        	DataCrow.mainFrame.setOnExitCheckForChanges(false);
            DataCrow.mainFrame.close();
        } catch (Exception exp) {
            DcSwingUtilities.displayErrorMessage(exp.toString());
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("compact"))
            compactAndShutDown();
    }
}
