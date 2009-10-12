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

package net.datacrow.console.components;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcLookAndFeel;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

import org.apache.log4j.Logger;

public class DcLookAndFeelSelector extends JComponent implements IComponent, ActionListener {
    
    private static Logger logger = Logger.getLogger(DcLookAndFeelSelector.class.getName());    

    private DcComboBox cbFieldHeight = ComponentFactory.getComboBox();
    private DcComboBox cbButtonHeight = ComponentFactory.getComboBox();
    private DcComboBox cbTreeNodeHeight = ComponentFactory.getComboBox();
    private DcComboBox cbTableRowHeight = ComponentFactory.getComboBox();
    
    private JCheckBox checkNoLF = ComponentFactory.getCheckBox(DcResources.getText("lblNoLF"));
    private JCheckBox checkSystemLF = ComponentFactory.getCheckBox(DcResources.getText("lblLaf"));
    private JCheckBox checkSkinLF = ComponentFactory.getCheckBox(DcResources.getText("lblSkinLF"));

    private JComboBox comboSkinLF = ComponentFactory.getComboBox();
    private JComboBox comboSystemLF = ComponentFactory.getComboBox();
    
    private DcDialog parent;
    
    private boolean applyModus = true;
   
    public DcLookAndFeelSelector() {
        applyModus = false;
        buildComponent();
        fillSkinLFCombo();
        fillSystemLFCombo();
        applyModus = true;
    }
    
    public void setParent(DcDialog dialog) {
        this.parent = dialog;
    }
    
    public void clear() {
        checkNoLF = null;
        checkSystemLF = null;
        checkSkinLF = null;
        comboSkinLF = null;
        comboSystemLF = null;
        parent = null;
    }    
    
    private void fillSkinLFCombo() {
        File file = new File(DataCrow.installationDir, "themes");
        String [] files = file.exists() && file.isDirectory() ? file.list() : null;
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String filename = files[i];
                if (filename.toLowerCase().endsWith("zip"))
                    comboSkinLF.addItem(
                            new DcLookAndFeel(filename, null, filename, DcLookAndFeel._SKINLF));
            }
        } else {
            logger.error(DcResources.getText("msgNoThemesFound"));
        }
    }

    private void fillSystemLFCombo() {
        LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        for (int i = 0; i < lafs.length; i++) {
            LookAndFeelInfo laf = lafs[i];
            if (laf.getName().toLowerCase().indexOf("nimbus") == -1)
                comboSystemLF.addItem(new DcLookAndFeel(laf.getName(), laf.getClassName(), null, DcLookAndFeel._LAF));
        }
    }
    
    public Object getValue() {
        return getLookAndFeel();
    }
    
    public DcLookAndFeel getLookAndFeel() {
        return null;
    }
    
    /**
     * Applies a value to this field
     */
    public void setValue(Object o) {
        applyModus = false;
        if (o instanceof DcLookAndFeel) { 
            DcLookAndFeel laf = (DcLookAndFeel) o;
            if (laf.getType() == DcLookAndFeel._NONE) {
                checkNoLF.setSelected(true);
            } else if (laf.getType() == DcLookAndFeel._SKINLF) {
                comboSkinLF.setSelectedItem(laf);
                checkSkinLF.setSelected(true);
            } else { 
                comboSystemLF.setSelectedItem(laf);
                checkSystemLF.setSelected(true);
            }
        }
        
        cbFieldHeight.setSelectedItem(Long.valueOf(DcSettings.getInt(DcRepository.Settings.stInputFieldHeight)));
        cbButtonHeight.setSelectedItem(Long.valueOf(DcSettings.getInt(DcRepository.Settings.stButtonHeight)));
        cbTreeNodeHeight.setSelectedItem(Long.valueOf(DcSettings.getInt(DcRepository.Settings.stTreeNodeHeight)));
        cbTableRowHeight.setSelectedItem(Long.valueOf(DcSettings.getInt(DcRepository.Settings.stTableRowHeight)));
        
        applyModus = true;
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        
        if (checkNoLF != null) {
            checkNoLF.setFont(ComponentFactory.getStandardFont());
            checkSkinLF.setFont(ComponentFactory.getStandardFont());
            checkSystemLF.setFont(ComponentFactory.getStandardFont());
            comboSkinLF.setFont(ComponentFactory.getStandardFont());
            comboSystemLF.setFont(ComponentFactory.getStandardFont());
        }
    }     
    
    public void setEditable(boolean b) {
        checkNoLF.setEnabled(b);
        checkSystemLF.setEnabled(b);
        checkSkinLF.setEnabled(b);
        comboSkinLF.setEnabled(b);
        comboSystemLF.setEnabled(b);
        cbButtonHeight.setEnabled(b);
        cbFieldHeight.setEnabled(b);
        cbTreeNodeHeight.setEnabled(b);
        cbTableRowHeight.setEnabled(b);
    }
    
    /**
     * Builds this component
     */
    private void buildComponent() {
        setLayout(Layout.getGBL());
        
        for (int i = 20; i < 50; i++) {
            cbFieldHeight.addItem(Long.valueOf(i));
            cbButtonHeight.addItem(Long.valueOf(i));
            cbTableRowHeight.addItem(Long.valueOf(i));
            cbTreeNodeHeight.addItem(Long.valueOf(i));
        }
        
        cbFieldHeight.addActionListener(this);
        cbFieldHeight.setActionCommand("applyInputFieldHeight");
        cbFieldHeight.setToolTipText(DcResources.getText("tpInputFieldHeight"));

        cbButtonHeight.addActionListener(this);
        cbButtonHeight.setActionCommand("applyButtonHeight");
        cbButtonHeight.setToolTipText(DcResources.getText("tpButtonHeight"));
        
        cbTableRowHeight.addActionListener(this);
        cbTableRowHeight.setActionCommand("applyTableRowHeight");
        cbTableRowHeight.setToolTipText(DcResources.getText("tpTableRowHeight"));

        cbTreeNodeHeight.addActionListener(this);
        cbTreeNodeHeight.setActionCommand("applyTreeNodeHeight");
        cbTreeNodeHeight.setToolTipText(DcResources.getText("tpTreeNodeHeight"));
        
        DcLabel lblTableRowHeight = ComponentFactory.getLabel(DcResources.getText("lblTableRowHeight"));
        DcLabel lblTreeNodeHeight = ComponentFactory.getLabel(DcResources.getText("lblTreeNodeHeight"));
        DcLabel lblFieldHeight = ComponentFactory.getLabel(DcResources.getText("lblInputFieldHeight"));
        lblFieldHeight.setToolTipText(DcResources.getText("tpInputFieldHeight"));
        DcLabel lblButtonHeight = ComponentFactory.getLabel(DcResources.getText("lblButtonHeight"));
        lblButtonHeight.setToolTipText(DcResources.getText("tpButtonHeight"));
        
        checkNoLF.addActionListener(new TypeActionListener(DcLookAndFeel._NONE));
        checkSkinLF.addActionListener(new TypeActionListener(DcLookAndFeel._SKINLF));
        checkSystemLF.addActionListener(new TypeActionListener(DcLookAndFeel._LAF));
        
        comboSystemLF.addActionListener(this);
        comboSkinLF.addActionListener(this);
        
        add(checkNoLF,       Layout.getGBC( 0, 0, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(checkSystemLF,   Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(comboSystemLF,   Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(checkSkinLF,     Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(comboSkinLF,     Layout.getGBC( 1, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(lblFieldHeight,  Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(cbFieldHeight,   Layout.getGBC( 1, 3, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(lblButtonHeight, Layout.getGBC( 0, 4, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(cbButtonHeight,  Layout.getGBC( 1, 4, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(lblTableRowHeight,Layout.getGBC( 0, 5, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(cbTableRowHeight, Layout.getGBC( 1, 5, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));      
        add(lblTreeNodeHeight,Layout.getGBC( 0, 6, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(cbTreeNodeHeight, Layout.getGBC( 1, 6, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));         
    }
    
    public static class FontStyle {
        final int style;
        final String name;
        
        public FontStyle(int index, String name) {
            this.style = index;
            this.name = name;
        }
        
        public int getIndex() {
            return style;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    private void applyLAF() {
        if (applyModus) {
            DcLookAndFeel laf = null;
            
            if (checkNoLF.isSelected()) 
                laf = DcLookAndFeel.getDisabled();
            else if (checkSkinLF.isSelected())
                laf = (DcLookAndFeel) comboSkinLF.getSelectedItem();
            else if (checkSystemLF.isSelected())
                laf = (DcLookAndFeel) comboSystemLF.getSelectedItem();
            
            if (laf != null) {
                DataCrow.mainFrame.updateLAF(laf);
                DataCrow.mainFrame.setViews();
                if (parent != null)
                    SwingUtilities.updateComponentTreeUI(parent.getContentPane());
            }
        }
    }
    
    private class TypeActionListener implements ActionListener {
        
        private int type = DcLookAndFeel._NONE;
        
        public TypeActionListener(int type) {
            this.type = type;
        }
        
        public void actionPerformed(ActionEvent arg0) {
            checkNoLF.setSelected(false);
            checkSkinLF.setSelected(false);
            checkSystemLF.setSelected(false);
            comboSkinLF.setEnabled(false);
            comboSystemLF.setEnabled(false);
            
            if (type == DcLookAndFeel._NONE) {
                checkNoLF.setSelected(true);
            } else if (type == DcLookAndFeel._SKINLF) {
                comboSkinLF.setSelectedIndex(0);
                checkSkinLF.setSelected(true);
                comboSkinLF.setEnabled(true);
            } else if (type == DcLookAndFeel._LAF) {
                comboSkinLF.setSelectedIndex(0);
                checkSystemLF.setSelected(true);
                comboSystemLF.setEnabled(true);
            }
            
            applyLAF();
        }
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("applyInputFieldHeight")) {
            Long value = (Long) cbFieldHeight.getSelectedItem();
            if (value != null) DcSettings.set(DcRepository.Settings.stInputFieldHeight, value);
        } else if (ae.getActionCommand().equals("applyButtonHeight")) {
            Long value = (Long) cbButtonHeight.getSelectedItem();
            if (value != null) DcSettings.set(DcRepository.Settings.stButtonHeight, value);
        } else if (ae.getActionCommand().equals("applyTreeNodeHeight")) {
            Long value = (Long) cbTreeNodeHeight.getSelectedItem();
            if (value != null) DcSettings.set(DcRepository.Settings.stTreeNodeHeight, value);
        } else if (ae.getActionCommand().equals("applyTableRowHeight")) {
            Long value = (Long) cbTableRowHeight.getSelectedItem();
            if (value != null) DcSettings.set(DcRepository.Settings.stTableRowHeight, value);
        } else {
            applyLAF();
        }
    }
    
    public void refresh() {}
}