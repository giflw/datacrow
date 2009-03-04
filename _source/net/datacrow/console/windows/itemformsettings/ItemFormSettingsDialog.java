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

package net.datacrow.console.windows.itemformsettings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JTabbedPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFrame;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Tab;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class ItemFormSettingsDialog extends DcFrame implements ActionListener {

    //private static Logger logger = Logger.getLogger(ItemFormSettingsDialog.class.getName());
    
    private TabPanel panelTab;
    
    private JTabbedPane tp = ComponentFactory.getTabbedPane();
    private Collection<TabFieldsPanel> panels = new ArrayList<TabFieldsPanel>();
    
    public ItemFormSettingsDialog() {
        super(DcResources.getText("lblItemFormSettings"), IconLibrary._icoSettings);
        
        panelTab = new TabPanel(this);
        setHelpIndex("dc.settings.itemform");
        build();
        setResizable(true);
    }
    
    public void refresh() {
        panelTab.refresh();
        
        Collection<DcObject> tabs = panelTab.getTabs();
        Collection<DcObject> currentTabs = new ArrayList<DcObject>();
        for (TabFieldsPanel panel : panels)
            currentTabs.add(panel.getTab());
        
        remove(tp);
        tp.removeAll();
        
        tp = new JTabbedPane();
        for (DcObject tab : tabs) {
            if (!currentTabs.contains(tab))
                panels.add(new TabFieldsPanel(tab));
        }
        
        for (TabFieldsPanel panel : panels.toArray(new TabFieldsPanel[0])) {
            if (!tabs.contains(panel.getTab()))
                panels.remove(panel);
        }
        
        // remove selected fields from the other panels
        for (TabFieldsPanel panel1 : panels) {
            Collection<DcField> fields = new ArrayList<DcField>();
            for (TabFieldsPanel panel2 : panels) {
                if (panel1 != panel2)
                    fields.addAll(panel2.getFields());
            }
            
            panel1.remove(fields);
        }
        
        tp.addTab(DcResources.getText("lblTabs"), panelTab);
        
        for (TabFieldsPanel panel : panels)
            tp.addTab(panel.getTab().getDisplayString(Tab._A_NAME), panel.getTab().getIcon(), panel);
        
        getContentPane().add(tp,  Layout.getGBC( 0, 0, 1, 1, 50.0, 50.0
                ,GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));

        repaint();
        
        Dimension size = getSize();
        pack();
        setSize(size);
        
    }
    
    private void build() {
        getContentPane().setLayout(Layout.getGBL());

        //**********************************************************
        //Main panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());
        
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        buttonClose.setMnemonic('C');

        this.getContentPane().add(buttonClose,  Layout.getGBC( 0, 1, 1, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 10), 0, 0));
        
        refresh();
        
        Dimension size = DcSettings.getDimension(DcRepository.Settings.stItemFormSettingsDialogSize);
        setSize(size);
        
        setCenteredLocation();
    }

    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stItemFormSettingsDialogSize, getSize());
        super.close();
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
    }
}
