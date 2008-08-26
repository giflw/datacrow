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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.DcFieldSelectorField;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class FieldSelectorDialog extends DcDialog implements ActionListener { 
    
    private static Logger logger = Logger.getLogger(FieldSelectorDialog.class.getName());    
    
    private final int modIdx;
    private DcFieldSelectorField fs;
    private String settingsKey;
    
    public FieldSelectorDialog(JFrame parent, int modIdx, String settingsKey) {
        
        super(parent);
        
        this.modIdx = modIdx;
        this.settingsKey = settingsKey;
        
        setTitle(DcResources.getText("lblOnlineUpdateFieldSettings"));
        
        setModal(true);
        
        build();

        try {
            fs.setSelectedFields((int[]) getModule().getSetting(settingsKey));
        } catch (Exception e) {
            logger.error("Could not set the selected fields (" +  getModule().getSetting(settingsKey) + ")", e);
        }
        
        setSize(getModule().getSettings().getDimension(DcRepository.ModuleSettings.stOnlineSearchFieldSettingsDialogSize));
        setCenteredLocation();
    }
    
    private DcModule getModule() {
        return DcModules.get(modIdx);
    }
    
    @Override
    public void close() {
        getModule().getSettings().set(DcRepository.ModuleSettings.stOnlineSearchFieldSettingsDialogSize, getSize());
        fs = null;
        super.close();
    }

    private void apply() {
        getModule().setSetting(settingsKey, fs.getSelectedFieldIndices());
        close();
    }    
    
    private void build() {
        getContentPane().setLayout(Layout.getGBL());
        
        fs = new DcFieldSelectorField(modIdx);
        
        getContentPane().add(fs, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                             new Insets(5, 5, 5, 5), 0, 0));        
        
        JPanel panelActions = new JPanel();
        panelActions.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton buttonApply = ComponentFactory.getButton(DcResources.getText("lblApply"));
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        buttonApply.addActionListener(this);
        buttonApply.setActionCommand("apply");
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        panelActions.add(buttonApply);  
        panelActions.add(buttonClose);
        
        getContentPane().add(panelActions, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                             new Insets(0, 0, 5, 5), 0, 0));
        
        JMenu editMenu = createMenu();
        JMenuBar mb = ComponentFactory.getMenuBar();
        mb.add(editMenu);
        setJMenuBar(mb);
        
        pack();
    }
    
    private JMenu createMenu() {
        JMenu menu = ComponentFactory.getMenu("Edit");
        
        JMenuItem menuSelectAll = ComponentFactory.getMenuItem(DcResources.getText("lblSelectAll"));
        JMenuItem menuUnselectAll = ComponentFactory.getMenuItem(DcResources.getText("lblUnselectAll"));
        JMenuItem menuInvertSelection = ComponentFactory.getMenuItem(DcResources.getText("lblInvertSelection"));

        menuSelectAll.addActionListener(this);
        menuSelectAll.setActionCommand("selectAll");
        menuUnselectAll.addActionListener(this);
        menuUnselectAll.setActionCommand("unselectAll");
        menuInvertSelection.addActionListener(this);
        menuInvertSelection.setActionCommand("invertSelection");
        
        menuSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        menuUnselectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));
        menuInvertSelection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
        
        menu.add(menuSelectAll);
        menu.add(menuUnselectAll);
        menu.addSeparator();
        menu.add(menuInvertSelection);
        
        return menu;
    }    

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("unselectAll"))
            fs.unselectAll();
        else if (ae.getActionCommand().equals("selectAll"))
            fs.selectAll();
        else if (ae.getActionCommand().equals("invertSelection"))
            fs.invertSelection();
        else if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("apply"))
            apply();
    }
}

