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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.DcLongTextField;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcMediaModule;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class ItemTypeDialog extends DcDialog implements ActionListener {

    private int selectedModule = -1;
    
    public ItemTypeDialog() {
        this(null);
    }
    
    public ItemTypeDialog(JFrame frame) {
        super(frame);
        setTitle(DcResources.getText("lblSelectModule"));
        setModal(true);
        buildDialog();
    }
    
    public int getSelectedModule() {
        return selectedModule;
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stModuleSelectDialogSize, getSize());
        super.close();
    }
    
    
    private void buildDialog() {
        getContentPane().setLayout(Layout.getGBL());
        
        //**********************************************************
        //Create Backup Panel
        //**********************************************************
        JPanel panelModules = new JPanel();
        panelModules.setLayout(Layout.getGBL());
        
        final ButtonGroup buttonGroup = new ButtonGroup();
        class SelectModuleAction implements ItemListener {
            public void itemStateChanged(ItemEvent ev) {
                String command = buttonGroup.getSelection().getActionCommand();
                selectedModule = Integer.parseInt(command);
            }
        }         
        
        DcLongTextField textHelp = ComponentFactory.getLongTextField();
        textHelp.setBorder(null);
        JScrollPane scroller = new JScrollPane(textHelp);
        scroller.setBorder(null);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        textHelp.setEditable(false);
        textHelp.setText(DcResources.getText("lblSelectModuleHelp"));
        panelModules.add(textHelp, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 5, 5), 0, 0));        
        
        int y = 1;
        for (DcModule module : DcModules.getModules()) {
            if (module instanceof DcMediaModule && module.isTopModule() && !module.isAbstract()) {
                JRadioButton radioButton = ComponentFactory.getRadioButton(module.getName(), module.getIcon32(), "" + module.getIndex());
                
                radioButton.addActionListener(this);
                radioButton.addItemListener(new SelectModuleAction());
                buttonGroup.add(radioButton);
                panelModules.add(radioButton, Layout.getGBC( 0, y++, 1, 1, 1.0, 1.0
                        ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                         new Insets( 0, 5, 5, 5), 0, 0));
            }
        }

        //**********************************************************
        //Main panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());
        this.getContentPane().add(panelModules,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                  new Insets( 5, 5, 5, 5), 0, 0));

        pack();
        Dimension size = DcSettings.getDimension(DcRepository.Settings.stModuleSelectDialogSize);
        setSize(size);
        setCenteredLocation();
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof JRadioButton) {
            if (((JRadioButton) source).isSelected())
                close();    
        }
    }
}
