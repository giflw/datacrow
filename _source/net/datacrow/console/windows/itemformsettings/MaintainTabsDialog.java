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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.panels.NavigationPanel;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.windows.DcDialog;
import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Tab;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.UpdateItemFormSettingsWindow;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcSwingUtilities;

import org.apache.log4j.Logger;

public class MaintainTabsDialog extends DcDialog implements ActionListener {
    
    private static Logger logger = Logger.getLogger(MaintainTabsDialog.class.getName());
    
    private ItemFormSettingsDialog dlg;
    private DcTable tblTabs = ComponentFactory.getDCTable(DcModules.get(DcModules._TAB), true, false);
    
    public MaintainTabsDialog(ItemFormSettingsDialog dlg) {
        super(dlg);
        
        this.dlg = dlg;
        build();
        
        setModal(false);
        setSize(DcSettings.getDimension(DcRepository.Settings.stMaintainTabsDialogSize));
        setCenteredLocation();
    }
    
    protected void clear() {
        dlg = null;
        if (tblTabs != null) tblTabs.clear();
        tblTabs = null;
    }
    
    public void refresh() {
        tblTabs.clear();
        tblTabs.add(DataManager.getTabs(dlg.getModule()));
        dlg.refresh();
    }
    
    public void save() {
        int order = 0;
        for (DcObject dco : tblTabs.getItems()) {
            try {
                dco.setValue(Tab._C_ORDER, Integer.valueOf(order++));
                dco.saveUpdate(false);
            } catch (ValidationException ve) {
                logger.error("Could not set the order for tab " + dco, ve);
            }
        }
        
        dlg.refresh();
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stMaintainTabsDialogSize, getSize());
        super.close();
    }
    
    private void addTab() {
        DcObject tab = DcModules.get(DcModules._TAB).getItem();
        tab.setValue(Tab._D_MODULE, Long.valueOf(dlg.getModule()));
        tab.addRequest(new UpdateItemFormSettingsWindow(this, false));
        ItemForm frm = new ItemForm(null, false, false, tab, true);
        frm.setVisible(true);
    }
    
    private void deleteTab() {
        if (tblTabs.getSelectedIndex() == -1) {
            DcSwingUtilities.displayMessage("msgTabDeleteNoRowSelected");
            return;
        }
        
        int[] rows = tblTabs.getSelectedIndices();
        DcObject dco;
        for (int i = rows.length - 1; i > -1; i--) {
            dco = tblTabs.getItemAt(rows[i]);
            dco.addRequest(new UpdateItemFormSettingsWindow(this, true));
            try {
                dco.delete(false);
            } catch (ValidationException e) {}
        }
    }
    
    protected Collection<DcObject> getTabs() {
        return tblTabs.getItems();
    }
    
    private void build() {
        
        //**********************************************************
        //Table Panel
        //**********************************************************
        JPanel panelTable = new JPanel();
        panelTable.setLayout(Layout.getGBL());
        
        JScrollPane sp = new JScrollPane(tblTabs);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        NavigationPanel panelNav = new NavigationPanel(tblTabs);
        tblTabs.setDynamicLoading(false);
        tblTabs.activate();
        
        panelTable.add(sp,  Layout.getGBC( 0, 0, 1, 1, 20.0, 20.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        panelTable.add(panelNav, Layout.getGBC(1, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets( 5, 5, 5, 5), 0, 0));
        
        //**********************************************************
        //Create Action Panel
        //**********************************************************
        JPanel panelActions = new JPanel();
        
        JButton btAdd = ComponentFactory.getButton(DcResources.getText("lblAdd"));
        JButton btDelete = ComponentFactory.getButton(DcResources.getText("lblDelete"));
        JButton btSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        JButton btClose = ComponentFactory.getButton(DcResources.getText("lblClose"));

        btAdd.addActionListener(this);
        btAdd.setActionCommand("addTab");
        btDelete.addActionListener(this);
        btDelete.setActionCommand("deleteTab");
        btSave.addActionListener(this);
        btSave.setActionCommand("save");
        btClose.addActionListener(this);
        btClose.setActionCommand("close");

        panelActions.add(btAdd);
        panelActions.add(btDelete);
        panelActions.add(btSave);
        panelActions.add(btClose);
        
        //**********************************************************
        //Main Panel
        //**********************************************************
        getContentPane().setLayout(Layout.getGBL());
        
        getContentPane().add(panelTable,    Layout.getGBC( 0, 0, 1, 1, 30.0, 30.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions,  Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        
        refresh();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("addTab"))
            addTab();
        else if (e.getActionCommand().equals("deleteTab"))
            deleteTab();
        else if (e.getActionCommand().equals("close"))
            close();
        else if (e.getActionCommand().equals("save"))
            save();
    }
}
