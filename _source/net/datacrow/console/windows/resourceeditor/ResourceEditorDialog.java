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

package net.datacrow.console.windows.resourceeditor;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class ResourceEditorDialog extends DcDialog implements ActionListener {
    
    private JTabbedPane tp = ComponentFactory.getTabbedPane();
    private Collection<LanguageResourcePanel> panels = new ArrayList<LanguageResourcePanel>();
    
    public ResourceEditorDialog() {
        super(DataCrow.mainFrame);
        
        build();
        
        pack();
        setSize(DcSettings.getDimension(DcRepository.Settings.stResourcesEditorViewSize));
        setCenteredLocation();
        setModal(true);
        
        setTitle(DcResources.getText("lblResourceEditor"));
    }
    
    private void save() {
        for (LanguageResourcePanel panel : panels)
            panel.save();
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stResourcesEditorViewSize, getSize());
        
        tp.removeAll();

        for (LanguageResourcePanel panel : panels)
            panel.clear();

        panels.clear();
        panels = null;

        super.close();
    }
    
    private void build() {
        getContentPane().setLayout(Layout.getGBL());

        //**********************************************************
        //Language tabs
        //**********************************************************
        for (String language : DcResources.getLanguages()) {
            LanguageResourcePanel lrp = new LanguageResourcePanel(language);
            tp.addTab(language, lrp);
            panels.add(lrp);
        }
        
        //**********************************************************
        //Action panel
        //**********************************************************
        JPanel panelActions = new JPanel();
        
        JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        buttonSave.setMnemonic('S');
        buttonClose.setMnemonic('C');
        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        panelActions.add(buttonSave);
        panelActions.add(buttonClose);

        getContentPane().add(tp, Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("save"))
            save();
    } 
}
