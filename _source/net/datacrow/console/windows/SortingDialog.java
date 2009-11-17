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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.panels.FieldSelectionPanel;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class SortingDialog extends DcDialog implements ActionListener {

    private FieldSelectionPanel panelSorting;
    private int module;
    
    public SortingDialog(int module) {
        super(DataCrow.mainFrame);
        
        setHelpIndex("dc.items.sort");
        setTitle(DcResources.getText("lblSort"));
        
        this.module = module;
        this.panelSorting = new FieldSelectionPanel(DcModules.get(module), true);
        this.panelSorting.setSelectedFields(
                DcModules.get(module).getSettings().getStringArray(DcRepository.ModuleSettings.stSearchOrder));
        
        build();
        
        setSize(DcSettings.getDimension(DcRepository.Settings.stSortDialogSize));
        setCenteredLocation();
    }
    
    @Override
    public void close() {
        panelSorting.clear();
        panelSorting = null;
        DcSettings.set(DcRepository.Settings.stSortDialogSize, getSize());
        super.close();
    }
    
    private void sort() {
        if (DcModules.get(module).getSetting(DcRepository.ModuleSettings.stSearchOrder) != null) {
            DcField[] fields = panelSorting.getSelectedFields();
            String[] order = new String[fields.length];

            int counter = 0;
            for (DcField field : fields)
                order[counter++] = field.getDatabaseFieldName();
            
            // Set the sort order in the settings and overrule the sorting of the currently
            // applied filter.
            DcModules.get(module).setSetting(DcRepository.ModuleSettings.stSearchOrder, order);
            DataFilters.getCurrent(module).setOrder(order);
        }

        DcModules.get(module).getSearchView().sort();
    }
    
    private void build() {
        
        getContentPane().setLayout(Layout.getGBL());
        
        //**********************************************************
        //Action Panel
        //**********************************************************
        JPanel panelActions = new JPanel();
        JButton buttonApply = ComponentFactory.getButton(DcResources.getText("lblApply"));
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        buttonApply.addActionListener(this);
        buttonApply.setActionCommand("sort");
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        panelActions.add(buttonApply);
        panelActions.add(buttonClose);
        
        //**********************************************************
        //Main Panel
        //**********************************************************
        getContentPane().add(panelSorting,  Layout.getGBC( 0, 0, 1, 1, 40.0, 40.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions,  Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        
        pack();
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("sort"))
            sort();
    }
}

