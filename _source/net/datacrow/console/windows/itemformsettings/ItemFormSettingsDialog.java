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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.DcFrame;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Tab;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.DcFieldDefinitions;

public class ItemFormSettingsDialog extends DcFrame implements ActionListener, ChangeListener {

    private List<TabDesignPanel> panels = new ArrayList<TabDesignPanel>();
    private DcModule module;
    
    public ItemFormSettingsDialog(DcModule module) {
        super(DcResources.getText("lblItemFormSettings"), IconLibrary._icoSettings16);
        
        this.module = module;
        setHelpIndex("dc.settings.itemformsettings");
        setResizable(true);
        build();
    }
    
    public int getModule() {
        return module.getIndex();
    }
    
    public void save() {
        DcFieldDefinitions definitions = new DcFieldDefinitions(module.getIndex());
        
        for (TabDesignPanel panel : panels)
            panel.save(definitions);

        // takes care for any missing field definition
        for (DcFieldDefinition def : module.getFieldDefinitions().getDefinitions()) {
            if (!definitions.exists(def)) 
                definitions.add(def);
        }
        
        module.setSetting(DcRepository.ModuleSettings.stFieldDefinitions, definitions);
    }
    
    private void build() {
        getContentPane().setLayout(Layout.getGBL());
        
        //**********************************************************
        //Menu
        //**********************************************************
        JMenuBar mb = ComponentFactory.getMenuBar();
        JMenu menu = ComponentFactory.getMenu(DcResources.getText("lblEdit"));
        JMenuItem menuEdit = ComponentFactory.getMenuItem(DcResources.getText("lblEditItem", DcResources.getText("lblTabs")));
        menuEdit.setActionCommand("editTabs");
        menuEdit.addActionListener(this);
        menu.add(menuEdit);
        mb.add(menu);
        setJMenuBar(mb);
        
        //**********************************************************
        //Tab Pane
        //**********************************************************
        JTabbedPane tp = ComponentFactory.getTabbedPane();
        
        tp.addChangeListener(this);

        List<DcObject> tabs = DataManager.getTabs(module.getIndex());
        
        TabDesignPanel panel;
        for (DcObject tab : tabs) {
            panel = new TabDesignPanel(module, tab);
            panels.add(panel);
            tp.addTab(tab.getDisplayString(Tab._A_NAME), tab.getIcon(), panel);
        }
        
        //**********************************************************
        //Action panel
        //**********************************************************
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        
        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");
        
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
                 new Insets( 5, 5, 5, 12), 0, 0));

        pack();
        
        Dimension size = DcSettings.getDimension(DcRepository.Settings.stItemFormSettingsDialogSize);
        setSize(size);
        setCenteredLocation();
    }

    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stItemFormSettingsDialogSize, getSize());
        super.close();
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("save"))
            save();
        else if (ae.getActionCommand().equals("editTabs"))
            save();
        
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        JTabbedPane tp = (JTabbedPane) e.getSource();
        if (tp.getSelectedIndex() > -1) {
            TabDesignPanel panel = panels.get(tp.getSelectedIndex());
            panel.refresh();
        }
    }
}
