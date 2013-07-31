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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcLabel;
import net.datacrow.console.components.DcMenu;
import net.datacrow.console.components.DcMenuItem;
import net.datacrow.console.components.DcMultiLineToolTip;
import net.datacrow.console.components.DcPanel;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.Utilities;

public class ModuleListPanel extends DcPanel {
    
    private Collection<JComponent> components = new ArrayList<JComponent>();
    private static Border borderDefault;
    private static Border borderSelected;
    
    public ModuleListPanel() {
        super(null, null);
    	buildPanel();
    }
    
    public void rebuild() {
        removeAll();
        buildPanel();
    }
    
    @Override
    public void setFont(Font font) {
        if (components == null) return;
        for (JComponent c : components) {
            c.setFont(font);
        }
    }
    
    public void setSelectedModule(int index) {
        if (DataCrow.mainFrame != null)
            DataCrow.mainFrame.changeModule(index);
    }
    
    private void buildPanel() {
        setLayout(Layout.getGBL());

        int x = 0;
        ModuleSelector ms;
        for (DcModule module : DcModules.getModules()) {
            
            if (module.isSelectableInUI() && module.isEnabled()) {
                ms = new ModuleSelector(module);
                add(ms, Layout.getGBC( x++, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
                components.add(ms);
            }
        }
    }
    
    private class ModuleSelector extends DcPanel implements ActionListener {
        
        private MainModuleButton mmb;
        private List<DcModule> referencedModules = new ArrayList<DcModule>();
        private DcModule module;
        
        private ModuleSelector(DcModule module) {
            this.module = module;
            
            this.mmb = new MainModuleButton(module);
            this.mmb.setBackground(getBackground());
            this.setToolTipText(module.getLabel() + (Utilities.isEmpty(module.getDescription()) ? "" : "\n" + module.getDescription()));
            
            addMouseListener(new ModuleMouseListener(module.getIndex()));
            
            DcModule rm;
            for (DcField field : module.getFields()) {
                rm = DcModules.getReferencedModule(field);
                if (    rm.isEnabled() && 
                        rm.getIndex() != module.getIndex() && 
                        rm.getType() != DcModule._TYPE_PROPERTY_MODULE &&
                        rm.getType() != DcModule._TYPE_EXTERNALREFERENCE_MODULE &&
                        rm.getIndex() != DcModules._CONTACTPERSON &&
                        rm.getIndex() != DcModules._CONTAINER) {
                    referencedModules.add(rm);
                }
            }
            build();
        }
        
        @Override
        public JToolTip createToolTip() {
            return new DcMultiLineToolTip();
        }
        
        @Override
        public Border getBorder() {
            return (mmb != null && mmb.getModule() == DcModules.getCurrent()) ? borderSelected : borderDefault;
        }

        private void build() {
            borderDefault = BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 0));
            borderSelected = BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(DcSettings.getColor(DcRepository.Settings.stSelectionColor), 3));
            
            setBorder(borderDefault);
            setLayout(Layout.getGBL());
            
            mmb.addModule(module, this);
            for (DcModule rm : referencedModules) {
                mmb.addModule(rm, this);
            }
            
            add(mmb, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, 
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        }
        
        public DcModule getModule() {
            return module;
        }
        
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().startsWith("module_change")) {
                ReferenceModuleButton mb = (ReferenceModuleButton) ae.getSource();
                
                module = mb.getModule();
                mmb.setModule(mb.module);
                
                setSelectedModule(mb.getModule().getIndex());
                                
                repaint();
                revalidate();
            }
        }
    }
    
    private class ModuleMouseListener implements MouseListener {
        
        private final int module;
        
        public ModuleMouseListener(int module) {
            this.module = module;
        }
        
        @Override
        public void mouseReleased(MouseEvent me) {
            ModuleSelector mmb = (ModuleSelector) me.getSource();
            setSelectedModule(mmb.getModule().getIndex());
        }
        
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {}
        @Override
        public void mousePressed(MouseEvent e) {}
        @Override
        public void mouseClicked(MouseEvent e) {}
    }
    
    protected class MainModuleButton extends JPanel {
        
        private DcModule module;
        
        private final Color selectedColor;
        private final Color normalColor;
        
        private final JMenuBar mb;
        private final JMenu menu;
        
        DcLabel lblModule;
        
        public MainModuleButton(DcModule module) {
            super(Layout.getGBL());
            
            this.mb = ComponentFactory.getMenuBar();
            this.mb.setBorderPainted(false);
            this.mb.setBackground(getBackground());
            
            components.add(mb);
            
            this.module = module;
            this.selectedColor = DcSettings.getColor(DcRepository.Settings.stSelectionColor);
            this.normalColor = super.getBackground();
            
            setBorder(null);
            
            setMinimumSize(new Dimension(60, 50));
            setPreferredSize(new Dimension(60, 50));
            
            lblModule = ComponentFactory.getLabel(module.getIcon32());
            add(lblModule, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 0), 0, 0));
            
            menu = new DcMenu("");
            menu.setIcon(IconLibrary._icoModuleBarSelector);
            menu.setFont(null);
            
            menu.setHorizontalAlignment(SwingConstants.CENTER);
            menu.setVerticalAlignment(SwingConstants.CENTER);
            
            menu.setRolloverEnabled(false);
            menu.setContentAreaFilled(false);
            
            menu.setMinimumSize(new Dimension(60, 10));
            menu.setPreferredSize(new Dimension(60, 10));
            
            mb.add(menu);
            add(mb, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, 
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        }
        
        public void setModule(DcModule module) {
            this.lblModule.setIcon(module.getIcon32());
            this.module = module;
        }
        
        public void addModule(DcModule module, ModuleSelector ms) {
            ReferenceModuleButton rmb = new ReferenceModuleButton(module);
            rmb.setActionCommand("module_change");
            rmb.addActionListener(ms);
            rmb.setBackground(getBackground());
            rmb.setMinimumSize(new Dimension(180, 39));
            rmb.setPreferredSize(new Dimension(180, 39));
            menu.add(rmb);
        }
        
        public DcModule getModule() {
            return module;
        }
        
        @Override
        public Color getBackground() {
            if (getModule() == DcModules.getCurrent()) {
                return selectedColor;
            } else {
                return normalColor;
            }
        }
        
        @Override
        public JToolTip createToolTip() {
            return new DcMultiLineToolTip();
        }        
        
        @Override
        public String getToolTipText() {
            return module.getDescription();
        }
        
        @Override
        public void setFont(Font font) {
            Component[] components = getComponents();
            for (int i = 0; i < components.length; i++) {
                components[i].setFont(font);
                components[i].setForeground(ComponentFactory.getCurrentForegroundColor());
            }
        }
    }
    
    private class ReferenceModuleButton extends DcMenuItem {
        
        private DcModule module;
        
        private ReferenceModuleButton(DcModule module) {
            super(module.getLabel());
            this.module = module;
            setBorder(null);
            setRolloverEnabled(false);
            setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontBold));
            
            setMinimumSize(new Dimension(50, 35));
            setPreferredSize(new Dimension(50, 35));
        }
        
        public DcModule getModule() {
            return module;
        }

        @Override
        public Icon getIcon() {
            return module.getIcon32();
        }

        @Override
        public String getLabel() {
            return module.getLabel();
        }

        @Override
        public String getText() {
            return module != null ? module.getLabel() : "";
        }
    }
}
