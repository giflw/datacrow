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

package net.datacrow.console.windows.filtering;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.DcShortTextField;
import net.datacrow.console.components.panels.FieldSelectionPanel;
import net.datacrow.console.views.MasterView;
import net.datacrow.console.views.View;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.windows.messageboxes.QuestionBox;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class FilterDialog extends DcDialog implements ActionListener {

    private static Logger logger = Logger.getLogger(FilterDialog.class.getName());
    
    private final DcModule module;
    
    private final MasterView parent;
    
    private JTabbedPane filterTabs = ComponentFactory.getTabbedPane();
    
    private DefineFilterEntryPanel defineFilterPanel;
    private FieldSelectionPanel sortingPanel;
    private ManageFiltersPanel manageFiltersPanel;
    
    public FilterDialog(DcModule module, MasterView parent) {
        super(DataCrow.mainFrame);
        
        setTitle(DcResources.getText("lblFilter"));
        setHelpIndex("dc.filters");
        
        this.parent = parent;
        this.module = module;
        
        buildForm(); 
        
        setSize(module.getSettings().getDimension(DcRepository.ModuleSettings.stFilterDialogSize));
        setCenteredLocation();
        
        setModal(true);
    }

    public void filter() {
        FilterThread thread = new FilterThread();
        thread.start();
    }   
    
    public DataFilter getDataFilter() {
        DataFilter filter = new DataFilter(module.getIndex());
        filter.setEntries(defineFilterPanel.getEntries());
        filter.setOrder(sortingPanel.getSelectedFields());
        return filter;
    }

    private void buildForm() {
        getContentPane().setLayout(Layout.getGBL());
        
        defineFilterPanel = new DefineFilterEntryPanel(module);
        sortingPanel = new FieldSelectionPanel(module);
        sortingPanel.setSelectedFields(module.getSettings().getStringArray(DcRepository.ModuleSettings.stSearchOrder));
        
        manageFiltersPanel = new ManageFiltersPanel(); 
        
        filterTabs.addTab(DcResources.getText("lblFilter"), IconLibrary._icoSearch, defineFilterPanel);
        filterTabs.addTab(DcResources.getText("lblSort"), IconLibrary._icoSort, sortingPanel);
        filterTabs.addTab(DcResources.getText("lblManage"), IconLibrary._icoFilter, manageFiltersPanel);
        
        getContentPane().add(filterTabs,       Layout.getGBC( 0, 0, 1, 1, 100.0, 100.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 2), 0, 0));

        getContentPane().add(getActionPanel(), Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));

        pack();     
    }
    
    private void applyFilter(DataFilter filter) {
        defineFilterPanel.clear();
        for (DataFilterEntry entry : filter.getEntries()) {
            defineFilterPanel.applyEntry(entry);
            defineFilterPanel.addEntry();
        }
        sortingPanel.setSelectedFields(filter.getOrder());
    }
    
    private class ManageFiltersPanel extends JPanel implements ActionListener {
        
        private JComboBox comboFilters;
        private DcShortTextField textName;
        
        public ManageFiltersPanel() {
            setLayout(Layout.getGBL());
            
            // ********* save ********
            JPanel panelSave = new JPanel();
            panelSave.setLayout(Layout.getGBL());
            
            textName = ComponentFactory.getShortTextField(255);
            JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
            
            buttonSave.addActionListener(this);
            buttonSave.setActionCommand("save");

            JLabel labelName = ComponentFactory.getLabel(DcResources.getText("lblName"));
            Dimension labelSize = new Dimension(150, ComponentFactory.getPreferredFieldHeight());
            labelName.setPreferredSize(labelSize);
            labelName.setMinimumSize(labelSize);
            labelName.setMinimumSize(labelSize);
            
            panelSave.add(labelName,    Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                     ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                      new Insets( 5, 5, 5, 5), 0, 0));
            panelSave.add(textName,     Layout.getGBC( 1, 0, 1, 1, 30.0, 30.0
                     ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                      new Insets( 5, 5, 5, 5), 0, 0));
            panelSave.add(buttonSave,   Layout.getGBC( 2, 0, 1, 1, 1.0, 1.0
                     ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                      new Insets( 5, 5, 5, 5), 0, 0));
            panelSave.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblSaveNewFilter")));
            
            // ********* manage ********
            JPanel panelManage = new JPanel();
            panelManage.setLayout(Layout.getGBL());
            
            comboFilters = ComponentFactory.getComboBox();
            JButton buttonApply = ComponentFactory.getButton(DcResources.getText("lblApply"));
            JButton buttonEdit = ComponentFactory.getButton(DcResources.getText("lblEdit"));
            JButton buttonDelete = ComponentFactory.getButton(DcResources.getText("lblDelete"));
            
            for (DataFilter df : DataFilters.get(module.getIndex()))
                comboFilters.addItem(df);
            
            buttonApply.addActionListener(this);
            buttonApply.setActionCommand("filter");
            
            buttonEdit.addActionListener(this);
            buttonEdit.setActionCommand("edit");
            
            buttonDelete.addActionListener(this);
            buttonDelete.setActionCommand("delete");

            JLabel labelFilters = ComponentFactory.getLabel(DcResources.getText("lblFilters"));
            labelFilters.setPreferredSize(labelSize);
            labelFilters.setMinimumSize(labelSize);
            labelFilters.setMinimumSize(labelSize);
            
            panelManage.add(labelFilters,   Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                     ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                      new Insets( 5, 5, 5, 5), 0, 0));
            panelManage.add(comboFilters,   Layout.getGBC( 1, 0, 1, 1, 30.0, 30.0
                     ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                      new Insets( 5, 5, 5, 5), 0, 0));
            panelManage.add(buttonApply,    Layout.getGBC( 2, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                     new Insets( 5, 5, 5, 5), 0, 0));
            panelManage.add(buttonEdit,     Layout.getGBC( 2, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                     new Insets( 5, 5, 5, 5), 0, 0));
            panelManage.add(buttonDelete,   Layout.getGBC( 2, 2, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                     new Insets( 5, 5, 5, 5), 0, 0));
            
            panelManage.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblManageExistingFilters")));
            
            // ********* main ********
            add(panelSave,   Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets( 5, 5, 5, 5), 0, 0));
            add(panelManage, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets( 5, 5, 5, 5), 0, 0));
        }

        private void setFilters() {
            comboFilters.removeAllItems();
            comboFilters.getEditor().setItem(null);
            comboFilters.repaint();
            
            Collection<DataFilter> filters = DataFilters.get(module.getIndex());
            if (filters.size() == 0)
                comboFilters.addItem("");
            
            for (DataFilter df : filters)
                comboFilters.addItem(df);            
        }
        
        private void save() {
            DataFilter df = getDataFilter();
            if (textName.getText() == null || textName.getText().trim().length() == 0) {
                new MessageBox(DcResources.getText("msgEnterFilterName"), MessageBox._INFORMATION);
            } else {
                df.setName(textName.getText());
                DataFilters.add(df);
                setFilters();
            }
        }
        
        private DataFilter getSelected() {
            return comboFilters.getSelectedItem() instanceof DataFilter ? 
                    (DataFilter) comboFilters.getSelectedItem() : null;
        }
        
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("delete")) {
                DataFilter filter = (DataFilter) comboFilters.getSelectedItem();
                
                if (filter != null)
                    DataFilters.delete(filter);
                
                setFilters();
            } else if (ae.getActionCommand().equals("edit")) {
                DataFilter filter = getSelected();
                if (filter != null) {
                    applyFilter(filter);
                    filterTabs.setSelectedIndex(0);
                    textName.setText(filter.getName());
                }                
            } else if (ae.getActionCommand().equals("filter")) {
                DataFilter filter = getSelected();
                if (filter != null) {
                    applyFilter(filter);
                    textName.setText(filter.getName());
                    filter();
                }                
            } else if (ae.getActionCommand().equals("save")) {
                save();
            }
        }
    }    
    
    private JPanel getActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        JButton buttonApply = ComponentFactory.getButton(DcResources.getText("lblApply"));
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        buttonApply.setToolTipText(DcResources.getText("tpSearch"));
        buttonApply.addActionListener(this);
        buttonClose.addActionListener(this);
        buttonApply.setActionCommand("filter");
        buttonClose.setActionCommand("close");
        buttonClose.setMnemonic('C');
        buttonApply.setMnemonic('A');

        panel.add(buttonApply);
        panel.add(buttonClose);
        
        buttonApply.requestFocus();
        
        return panel; 
    }
    
    @Override
    public void close() {
        module.getSettings().set(DcRepository.ModuleSettings.stFilterDialogSize, getSize());
        DataCrow.mainFrame.updateQuickFilterBar();
        setVisible(false);
    }
    
    private class FilterThread extends Thread {

        @Override
        public void run() {
            View view = parent.getCurrent();
            boolean saved = view.isChangesSaved();
            
            if (!saved && view.getCurrentTask() != null) {
                QuestionBox qb = new QuestionBox(DcResources.getText("msgNotSaved"));
                if (qb.isAffirmative())
                    view.save();
                
                try {
                    view.getCurrentTask().join();
                } catch (Exception e) {
                    logger.error("Error while joining threads", e);
                }
            }
            
            view.undoChanges();

            DataFilter df = getDataFilter(); 
            DataFilters.setCurrent(module.getIndex(), df);
            
            DcObject[] result = DataManager.get(module.getIndex(), df);
            parent.setStatus(DcResources.getText("msgSearchHasBeenExecuted"));

            if (result.length == 0)
                DcModules.getCurrent().getSearchView().clear();

            DataManager.bindData(module.getSearchView(),  result);
            DataFilters.setCurrent(module.getIndex(), df);
            DataCrow.mainFrame.setSelectedTab(0);        
            close();
        }
    }    
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("filter"))
            filter();
    }
}