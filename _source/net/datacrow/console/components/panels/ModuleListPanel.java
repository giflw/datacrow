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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.border.Border;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcPanel;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.settings.DcSettings;

public class ModuleListPanel extends DcPanel {
    
    private Map<Integer, ModuleButton> buttons = new HashMap<Integer, ModuleButton>();
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
        if (buttons == null) return;
        for (ModuleButton mb : buttons.values()) {
            mb.setFont(font);
        }
    }
    
    public void setSelectedModule(int index) {
        if (DataCrow.mainFrame != null)
            DataCrow.mainFrame.changeModule(index);
    }
    
    private void buildPanel() {
        setLayout(Layout.getGBL());

        int x = 0;
        for (DcModule module : DcModules.getModules()) {
            
            if (module.isSelectableInUI() && module.isEnabled()) {
                ModuleSelector bt = new ModuleSelector(module);
                add(bt, Layout.getGBC( x++, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
        }
    }
    
    private class ModuleSelector extends DcPanel implements ActionListener {
        
        private JMenuBar menuBar;
        private ModuleButton mb;
        private List<DcModule> referencedModules = new ArrayList<DcModule>();
        
        private ModuleSelector(DcModule module) {
            mb = new ModuleButton(module);
            mb.setActionCommand("module_change");
            mb.addActionListener(this);
            mb.setBackground(getBackground());
            
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
            
            int x = 0;
            ModuleButton mi;
            if (referencedModules.size() > 0) {
                menuBar = ComponentFactory.getMenuBar();
                menuBar.setBackground(getBackground());
                menuBar.setMinimumSize(new Dimension(30, 35));
                menuBar.setPreferredSize(new Dimension(30, 35));
                menuBar.setMaximumSize(new Dimension(30, 35));
                
                JMenu menu = ComponentFactory.getMenu(IconLibrary._icoArrowDownThin, "");
                menu.setRolloverEnabled(false);
                menu.setContentAreaFilled(false);
                
                for (DcModule rm : referencedModules) {
                    mi = new ModuleButton(rm);
                    mi.setActionCommand("module_change");
                    mi.addActionListener(this);
                    mi.setBackground(getBackground());
                    mi.setMinimumSize(new Dimension(148, 39));
                    mi.setPreferredSize(new Dimension(148, 39));
                         
                    menu.add(mi);
                }
                
                menuBar.add(menu);
                add(menuBar, Layout.getGBC(x++, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, 
                        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            }
            
            add(mb, Layout.getGBC(x++, 0, 1, 1, 2.0, 1.0, GridBagConstraints.NORTHWEST, 
                    GridBagConstraints.BOTH,new Insets(0, 0, 0, 0), 0, 0));
        }
        
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().startsWith("module_change")) {
                ModuleButton mb = (ModuleButton) ae.getSource();
                setSelectedModule(mb.getModule().getIndex());
            }
        }
    }
    
    private class ModuleButton extends JMenuItem {
        
        private DcModule module;
        
        private ModuleButton(DcModule module) {
            this.module = module;
            setBorder(null);
            setRolloverEnabled(false);
            
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
