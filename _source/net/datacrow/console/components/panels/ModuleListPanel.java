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
import net.datacrow.console.components.DcLongTextField;
import net.datacrow.console.components.DcMenu;
import net.datacrow.console.components.DcMenuItem;
import net.datacrow.console.components.DcMultiLineToolTip;
import net.datacrow.console.components.DcPanel;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

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
        
        private JMenuBar menuBar;
        private MainModuleButton mb;
        private List<DcModule> referencedModules = new ArrayList<DcModule>();
        
        private ModuleSelector(DcModule module) {
            mb = new MainModuleButton(module);
            mb.setBackground(getBackground());
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
        public void setFont(Font font) {
            super.setFont(font);
            
            if (menuBar != null) menuBar.setFont(font);
            if (mb != null) mb.setFont(font);
        }
        
        @Override
        public Border getBorder() {
            if (mb != null && mb.getModule() == DcModules.getCurrent()) {
                return borderSelected;
            } else {
                return borderDefault;
            }
        }

        private void build() {
            
            borderDefault = BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            borderSelected = BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(DcSettings.getColor(DcRepository.Settings.stSelectionColor), 1));
            
            setBorder(borderDefault);
            setLayout(Layout.getGBL());
            
            ReferenceModuleButton mi;
            if (referencedModules.size() > 0) {
                menuBar = ComponentFactory.getMenuBar();
                menuBar.setBorderPainted(false);
                menuBar.setBackground(getBackground());
                
                components.add(menuBar);
                
                JMenu menu = new DcMenu(DcResources.getText("lblSubModule")) {
                    private Font f = new Font(DcSettings.getFont(DcRepository.Settings.stSystemFontBold).getFontName(), Font.ITALIC, 9);
                    @Override
                    public void setFont(Font font) {
                        super.setFont(this.f);
                    }
                };
                
                menu.setFont(null);
                
                menu.setHorizontalAlignment(SwingConstants.CENTER);
                menu.setVerticalAlignment(SwingConstants.CENTER);
                
                menu.setRolloverEnabled(false);
                menu.setContentAreaFilled(false);
                
                menu.setMinimumSize(new Dimension(100, 12));
                menu.setPreferredSize(new Dimension(100, 12));
                //menu.setMaximumSize(new Dimension(120, 12));
                
                menu.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
                
                for (DcModule rm : referencedModules) {
                    mi = new ReferenceModuleButton(rm);
                    mi.setActionCommand("module_change");
                    mi.addActionListener(this);
                    mi.setBackground(getBackground());
                    mi.setMinimumSize(new Dimension(118, 39));
                    mi.setPreferredSize(new Dimension(118, 39));
                         
                    menu.add(mi);
                }
                
                menuBar.add(menu);
                add(menuBar, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.SOUTH, 
                        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            }
            
            add(mb, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, 
                    GridBagConstraints.BOTH,new Insets(0, 0, 0, 0), 0, 0));
        }
        
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().startsWith("module_change")) {
                ReferenceModuleButton mb = (ReferenceModuleButton) ae.getSource();
                setSelectedModule(mb.getModule().getIndex());
            }
        }
        

    }
    
    private class ModuleMouseListener implements MouseListener {
        
        private final int module;
        
        public ModuleMouseListener(int module) {
            this.module = module;
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            setSelectedModule(module);
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
        
        private final DcModule module;
        
        public MainModuleButton(DcModule module) {
            super(Layout.getGBL());
            this.module = module;
            
            setBorder(null);
            
            setMinimumSize(new Dimension(100, 35));
            setPreferredSize(new Dimension(100, 35));
            add(ComponentFactory.getLabel(module.getIcon32()), Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets(0, 3, 0, 0), 0, 0));
            
            DcLongTextField ltf = ComponentFactory.getHelpTextField();
            ltf.setText(module.getLabel());
            for (MouseListener ml : ltf.getMouseListeners())
                ltf.removeMouseListener(ml);
            
            ltf.setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontBold));
            ltf.addMouseListener(new ModuleMouseListener(module.getIndex()));
            ltf.setBackground(getBackground());
            
            add(ltf, Layout.getGBC( 1, 0, 1, 1, 100.0, 100.0
                    ,GridBagConstraints.WEST, GridBagConstraints.BOTH,
                    new Insets(2, 2, 0, 0), 0, 0));;
        }
        
        public DcModule getModule() {
            return module;
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
            
            setMinimumSize(new Dimension(120, 35));
            setPreferredSize(new Dimension(120, 35));
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
