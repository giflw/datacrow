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

package net.datacrow.console.windows.itemformsettings;

import java.awt.Dimension;
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
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class ItemFormSettingsDialog extends DcFrame implements ActionListener {

    private int module;
    
    private TabPanel panelTab;
    private TabFieldsPanel panelTabFields;
    
    public ItemFormSettingsDialog(int module) {
        super(DcResources.getText("lblItemFormSettings"), IconLibrary._icoSettings);
        
        this.module = module;
        
        panelTab = new TabPanel(this);
        panelTabFields = new TabFieldsPanel(DcModules.get(module));
        
        setHelpIndex("dc.settings.itemform");
        
        setResizable(true);
        
        build();
    }
    
    public void refresh(boolean tabDelete) {
        panelTab.refresh();
        panelTabFields.refresh(tabDelete);
    }
    
    public int getModule() {
        return module;
    }
    
    public void save() {
        panelTabFields.save();
        panelTab.save();
    }

    private void build() {
        getContentPane().setLayout(Layout.getGBL());
        
        //**********************************************************
        //Tab Pane
        //**********************************************************
        JTabbedPane tp = ComponentFactory.getTabbedPane();
        tp.addTab(DcResources.getText("lblTabs"), panelTab);
        tp.addTab(DcResources.getText("lblTabDesign"), panelTabFields);

        //**********************************************************
        //Action panel
        //**********************************************************
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        buttonClose.setMnemonic('C');

        JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        
        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");
        buttonSave.setMnemonic('S');
        
        JPanel panelActions = new JPanel();
        panelActions.add(buttonSave);
        panelActions.add(buttonClose);

        
        //**********************************************************
        //Main panel
        //**********************************************************
        getContentPane().add(tp,  Layout.getGBC( 0, 0, 1, 1, 20.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions,  Layout.getGBC( 0, 1, 1, 1, 0.0, 0.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));

        pack();
        
        Dimension size = DcSettings.getDimension(DcRepository.Settings.stItemFormSettingsDialogSize);
        setSize(size);
        setCenteredLocation();

        refresh(false);
    }

    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stItemFormSettingsDialogSize, getSize());
        super.close();
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("save"))
            save();
    }
}
