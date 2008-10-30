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

package net.datacrow.console.components.panels;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.FieldSelectorDialog;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.Settings;

public class OnlineServiceSettingsPanel extends JPanel implements ActionListener {
    
    private JButton buttonSettings = ComponentFactory.getButton(IconLibrary._icoSettings);
    private JCheckBox checkOverwrite;
    private JCheckBox checkAutoCreateSubItems;
    private JCheckBox checkAutoAdd;
    private JCheckBox checkOnlineSearchSubItems;
    private JCheckBox checkQueryFullDetails;
    
    private JFrame parent;
    
    private int module;
    
    private boolean updateMode = false;

    private boolean allowAutoAddSelection = true;
    private boolean allowQueryModeSelection = true;
    
    public OnlineServiceSettingsPanel(JFrame parent, 
                                      boolean allowQueryModeSelection,
                                      boolean allowAutoAddSelection,
                                      boolean updateMode, 
                                      int module) {
        this.module = module;
        this.parent = parent;
        this.updateMode = updateMode;
        
        this.allowAutoAddSelection = allowAutoAddSelection;
        this.allowQueryModeSelection = allowQueryModeSelection;
        
        build(updateMode);
        
        Settings settings = DcModules.get(module).getSettings();
        setOverwrite(settings.getBoolean(DcRepository.ModuleSettings.stOnlineSearchOverwrite));
        setAutoCreateSubItems(settings.getBoolean(DcRepository.ModuleSettings.stAutoCreateSubItems));
        setOnlineSearchSubItems(settings.getBoolean(DcRepository.ModuleSettings.stOnlineSearchSubItems));
        setQueryFullDetails(settings.getBoolean(DcRepository.ModuleSettings.stOnlineSearchQueryFullDetailsInitially));
        
        if (allowAutoAddSelection)
            setAutoAdd(settings.getBoolean(DcRepository.ModuleSettings.stAutoAddPerfectMatch));
        
        if (updateMode)
            buttonSettings.setEnabled(isOverwriteAllowed());
    }

    public void save() {
        Settings settings = DcModules.get(module).getSettings();
        settings.set(DcRepository.ModuleSettings.stOnlineSearchOverwrite, checkOverwrite.isSelected());
        settings.set(DcRepository.ModuleSettings.stAutoCreateSubItems, checkAutoCreateSubItems.isSelected());
        settings.set(DcRepository.ModuleSettings.stAutoAddPerfectMatch, checkAutoAdd.isSelected());
        settings.set(DcRepository.ModuleSettings.stOnlineSearchSubItems, checkOnlineSearchSubItems.isSelected());
        settings.set(DcRepository.ModuleSettings.stOnlineSearchQueryFullDetailsInitially, checkQueryFullDetails.isSelected());
    }

    private void setAutoCreateSubItems(boolean b) {
        checkAutoCreateSubItems.setSelected(b);
    }
    
    private void setOverwrite(boolean b) {
        checkOverwrite.setSelected(b);
    }
    
    public boolean isOverwriteAllowed() {
        return checkOverwrite.isSelected();
    }
    
    public boolean isOnlineSearchSubItems() {
        return checkOnlineSearchSubItems.isSelected();
    }
    
    private void setOnlineSearchSubItems(boolean b) {
        checkOnlineSearchSubItems.setSelected(b);
    }
    
    private void setAutoAdd(boolean b) {
        checkAutoAdd.setSelected(b);
    }
    
    public boolean isAutoAddAllowed() {
        return checkAutoAdd.isSelected();
    }   
    
    private void setQueryFullDetails(boolean b) {
        checkQueryFullDetails.setSelected(b);
    }
    
    public boolean isQueryFullDetails() {
        return checkQueryFullDetails.isSelected();
    }
    
    public void clear() {
        buttonSettings = null;
        checkOverwrite = null;
        checkOnlineSearchSubItems = null;
        checkAutoCreateSubItems = null;
        checkAutoAdd = null;
        parent = null;
        removeAll();
    }
    
    private void build(boolean updateMode) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        setLayout(Layout.getGBL());

        checkAutoCreateSubItems = ComponentFactory.getCheckBox(DcResources.getText("lblAutoCreateMissingItems"));
        checkAutoCreateSubItems.setToolTipText(DcResources.getText("tpAutoCreateMissingItems"));

        checkAutoAdd = ComponentFactory.getCheckBox(DcResources.getText("lblAutoAddPerfectMatch"));
        checkAutoAdd.setToolTipText(DcResources.getText("tpAutoAddPerfectMatch"));
        
        checkOverwrite = ComponentFactory.getCheckBox(DcResources.getText("lblOverwriteExistingValues"));
        checkOverwrite.setToolTipText(DcResources.getText("tpOverwriteExistingValues"));
        
        if (updateMode) {
            checkOverwrite.addActionListener(this);
            checkOverwrite.setActionCommand("toggleSettings");
        }
        
        checkOnlineSearchSubItems = ComponentFactory.getCheckBox(DcResources.getText("lblOnlineSearchSubItems"));
        checkOnlineSearchSubItems.setToolTipText(DcResources.getText("tpOnlineSearchSubItems"));
        
        checkQueryFullDetails  = ComponentFactory.getCheckBox(DcResources.getText("lblOnlineSearchQueryFullDetails"));
        checkOnlineSearchSubItems.setToolTipText(DcResources.getText("tpOnlineSearchQueryFullDetails"));
        
        buttonSettings.setMnemonic('S');
        buttonSettings.setMaximumSize(new Dimension(25, 22));
        buttonSettings.setMinimumSize(new Dimension(25, 22));
        buttonSettings.setPreferredSize(new Dimension(25, 22));
        buttonSettings.addActionListener(this);
        buttonSettings.setActionCommand("openOverwriteSettingsDialog");
        
        if (updateMode)
            panel.add(checkOverwrite);
        else
            panel.add(ComponentFactory.getLabel(DcResources.getText("lblRetrieveValues")));
        
        panel.add(buttonSettings);
        
        add(panel, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));

        if (allowQueryModeSelection)
            add(checkQueryFullDetails, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(0, 5, 0, 0), 0, 0));            

        
        add(checkOnlineSearchSubItems, Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
            new Insets(0, 5, 0, 0), 0, 0));
        
        
        if (allowAutoAddSelection)
            add(checkAutoAdd, Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 5, 0, 0), 0, 0));            
        
        
        add(checkAutoCreateSubItems, Layout.getGBC( 0, 4, 1, 1, 1.0, 1.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
            new Insets(0, 5, 0, 0), 0, 0));

        setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblOnlineServiceSettings")));        
    }
    
    private void openOverwriteSettingsDlg() {
        String settingsKey = updateMode ?
                             DcRepository.ModuleSettings.stOnlineSearchFieldOverwriteSettings :
                             DcRepository.ModuleSettings.stOnlineSearchRetrievedFields;
        
        FieldSelectorDialog dlg = new FieldSelectorDialog(parent, module, settingsKey);
        dlg.setVisible(true);
    }    
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("openOverwriteSettingsDialog")) {
            openOverwriteSettingsDlg();
        } else if (e.getActionCommand().equals("toggleSettings")) {
            if (updateMode)
                buttonSettings.setEnabled(checkOverwrite.isSelected());
            else
                buttonSettings.setEnabled(true);            
        }
    }
}
