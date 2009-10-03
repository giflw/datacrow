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

package net.datacrow.console.wizards.module;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.DcPropertyModule;
import net.datacrow.core.modules.IChildModule;
import net.datacrow.core.modules.MappingModule;
import net.datacrow.core.resources.DcResources;

public class PanelSelectModule extends ModuleWizardPanel {

    private int selectedModule = -1;
    
    private Map<Integer, JRadioButton> components = new HashMap<Integer, JRadioButton>();
    
    public PanelSelectModule(Wizard wizard) {
        super(wizard);
        build();
    }

    public Object apply() {
        if (selectedModule == -1) {
            new MessageBox(DcResources.getText("msgSelectModuleFirst"), MessageBox._INFORMATION);
            return null;
        }
        
        return DcModules.get(selectedModule).getXmlModule();
    }

    public int getSelectedModule() {
        return selectedModule;
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgSelectModuleToAlter");
    }
    
    public void destroy() {
        if (components != null) {
            components.clear();
            components = null;
        }        
    }  
    
    protected JRadioButton getRadioButton(int module) {
        return components.get(module);
    }
    
    protected boolean isModuleAllowed(DcModule module) {
        return  module instanceof DcPropertyModule && module.getXmlModule() != null || (
               (module.isTopModule() || module instanceof IChildModule) && 
              !(module instanceof DcPropertyModule || module instanceof MappingModule) &&
               !module.isAbstract());
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        final ButtonGroup bg = new ButtonGroup();
        class ModuleSelectionListener implements MouseListener {
            public void mouseClicked(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}

            public void mouseReleased(MouseEvent e) {
                String command = bg.getSelection().getActionCommand();
                selectedModule = Integer.parseInt(command);
                try {
                    
                    if (getWizard().isAtTheEnd())
                        getWizard().finish();
                    else
                        getWizard().next();
                    
                } catch (WizardException wi) {
                    new MessageBox(wi.getMessage(), MessageBox._WARNING);
                }
            }
        } 

        int y = 0;
        int x = 0;
        
        for (DcModule module : DcModules.getAllModules()) {
            if (isModuleAllowed(module)) {
                JRadioButton rb = ComponentFactory.getRadioButton(module.getLabel(), module.getIcon16(), "" + module.getIndex());
                rb.addMouseListener(new ModuleSelectionListener());
                bg.add(rb);
                
                components.put(module.getIndex(), rb);
                
                add(rb, Layout.getGBC( x, y++, 1, 1, 1.0, 1.0
                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets( 0, 5, 5, 5), 0, 0));
                
                if (y == 7) {
                    ++x;
                    y = 0;
                }
            }
        }
    }
}
