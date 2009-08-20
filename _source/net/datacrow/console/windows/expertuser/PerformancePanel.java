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
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcNumberField;
import net.datacrow.console.components.DcPanel;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DcRepository;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class PerformancePanel extends DcPanel implements ActionListener {

    private final JComboBox comboHSqlCacheScale = ComponentFactory.getComboBox();
    private final JComboBox comboHSqlCacheSizeScale = ComponentFactory.getComboBox();
    private final DcNumberField numGarbageCollectionInterval = ComponentFactory.getNumberField();
    
    public PerformancePanel() {
        super(null, null);
        buildPanel();
        setCurrentValues();
    }
    
    private void buildPanel() {
        setLayout(Layout.getGBL());
        
        JLabel labelHSqlCacheScale = ComponentFactory.getLabel(DcResources.getText("lblHsqlCacheScale"));
        JLabel labelHSqlCacheSizeScale = ComponentFactory.getLabel(DcResources.getText("lblHsqlCacheSizeScale"));
        JLabel labelGarbageCollectionInterval = ComponentFactory.getLabel(DcResources.getText("lblGarbageCollectionInterval"));
        
        labelHSqlCacheScale.setToolTipText(DcResources.getText("tpHsqlCacheScale"));
        labelHSqlCacheSizeScale.setToolTipText(DcResources.getText("tpHsqlCacheSizeScale"));
        labelGarbageCollectionInterval.setToolTipText(DcResources.getText("tpGarbageCollectionIntervalMs"));
        
        for (int i = 8; i < 19; i++)
            comboHSqlCacheScale.addItem(i);
        
        for (int i = 6; i < 21; i++)
            comboHSqlCacheSizeScale.addItem(i);
        
        JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");
        
        add(labelHSqlCacheScale,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
        add(comboHSqlCacheScale,  Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        add(labelHSqlCacheSizeScale,  Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
        add(comboHSqlCacheSizeScale,  Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        add(labelGarbageCollectionInterval,  Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
        add(numGarbageCollectionInterval,    Layout.getGBC( 1, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        add(buttonSave,    Layout.getGBC( 1, 3, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
    }
    
    private void setCurrentValues() {
        int cacheScale = DcSettings.getInt(DcRepository.Settings.stHsqlCacheScale);
        int cacheSizeScale = DcSettings.getInt(DcRepository.Settings.stHsqlCacheSizeScale);
        int gcInterval = DcSettings.getInt(DcRepository.Settings.stGarbageCollectionIntervalMs);
        
        comboHSqlCacheScale.setSelectedItem(Integer.valueOf(cacheScale));
        comboHSqlCacheSizeScale.setSelectedItem(Integer.valueOf(cacheSizeScale));
        numGarbageCollectionInterval.setValue(gcInterval);
    }
    
    private void saveSettings() {
        Integer cacheScale = (Integer) comboHSqlCacheScale.getSelectedItem();
        Integer cacheSizeScale = (Integer) comboHSqlCacheSizeScale.getSelectedItem();
        Long gcInterval = (Long) numGarbageCollectionInterval.getValue();
            
        if (cacheScale != null)
            DcSettings.set(DcRepository.Settings.stHsqlCacheScale, cacheScale);
        
        if (cacheSizeScale != null)
            DcSettings.set(DcRepository.Settings.stHsqlCacheSizeScale, cacheSizeScale);

        if (gcInterval != null)
            DcSettings.set(DcRepository.Settings.stGarbageCollectionIntervalMs, gcInterval);      
        
        DatabaseManager.applySettings();
        
        new MessageBox(DcResources.getText("msgSettingsSavedUnapplied"), MessageBox._INFORMATION);
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("save"))
            saveSettings();
    }
}
