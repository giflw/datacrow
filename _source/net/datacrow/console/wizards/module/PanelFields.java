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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcLabel;
import net.datacrow.console.components.renderers.XmlFieldCellRenderer;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.windows.messageboxes.QuestionBox;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcMediaModule;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcPropertyModule;
import net.datacrow.core.modules.xml.XmlField;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.resources.DcResources;

public class PanelFields extends ModuleWizardPanel implements ActionListener {

    private JButton buttonUp = ComponentFactory.getButton(IconLibrary._icoArrowUp);
    private JButton buttonDown = ComponentFactory.getButton(IconLibrary._icoArrowDown);
    private JButton buttonAdd = ComponentFactory.getButton(DcResources.getText("lblAddField"));
    private JButton buttonRemove = ComponentFactory.getButton(DcResources.getText("lblRemoveField"));
    
    private boolean canHaveReferences;
    private DcTable table;
    private DcTable tableSysFields;
    
    private final boolean dbCheck;
    
    public PanelFields(Wizard wizard, boolean dbCheck) {
        super(wizard);
        
        this.dbCheck = dbCheck;
        table = ComponentFactory.getDCTable(true, false);
        tableSysFields = ComponentFactory.getDCTable(true, false);
        build();
    }
    
    @Override
    public void setModule(XmlModule module) {
        super.setModule(module);
        canHaveReferences = !getModule().getModuleClass().equals(DcPropertyModule.class); 
        addDefaultFields();
        
        table.clear();
        for (XmlField field : module.getFields()) 
            table.addRow(new Object[] {field});
    }
    
    public Object apply() {
        XmlModule module = getModule();

        Collection<XmlField> fields = new ArrayList<XmlField>();
        for (int i = 0; i < table.getRowCount(); i++) {
            XmlField field = (XmlField) table.getValueAt(i, 0);
            fields.add(field);
        }
        
        module.setFields(fields);
        
        return module;
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("msgModuleFields");
    }
    
    private Collection<String> getCurrentFieldNames() {
        Collection<String> names = new ArrayList<String>();
        for (int i = 0; i < table.getRowCount(); i++) {
            XmlField field = (XmlField) table.getValueAt(i, 0);
            names.add(field.getColumn());
        }

        for (int i = 0; i < tableSysFields.getRowCount(); i++) {
            DcField field = (DcField) tableSysFields.getValueAt(i, 0);
            names.add(field.getDatabaseFieldName());
        }
        
        return names;
    }
    
    private void createField() {
        DefineFieldDialog dlg = new DefineFieldDialog(getWizard(), getCurrentFieldNames(), canHaveReferences);
        dlg.setVisible(true);
        XmlField field = dlg.getField();
        if (field != null)
            table.addRow(new Object[] {field});
    }
    
    private Collection<DcField> getDefaultFields() {
        XmlModule xmlModule = getModule();

        DcModule module = null;
        if (xmlModule.getModuleClass() != null) { 
            if (xmlModule.getModuleClass().equals(DcMediaModule.class))
                module = new DcMediaModule(10, false, "", "", "", "", "", "", "");
            else if (xmlModule.getModuleClass().equals(DcPropertyModule.class))
                module = new DcPropertyModule(10, "", "", "", "", "");
        }
        
        return module != null ? module.getFields() : new ArrayList<DcField>();
    }
    
    private void addDefaultFields() {
        tableSysFields.clear();
        for (DcField field : getDefaultFields())
            tableSysFields.addRow(new Object[] {field});
    }

    public void destroy() {
        buttonUp = null;
        buttonDown = null;
        buttonAdd = null;
        buttonRemove = null;
        
        if (table != null) {
            table.clear();
            table = null;
        }

        if (tableSysFields != null) {
            tableSysFields.clear();
            tableSysFields = null;
        }
    }
    
    private void delete() {
        if (table.getSelectedRow() == -1)
            return;
        
        if (dbCheck) {
            XmlField field = (XmlField) table.getValueAt(table.getSelectedRow(), 0);
            
            if (field.isOverwritable()) {
                QuestionBox qb = new QuestionBox(DcResources.getText("msgDeleteField"));
                if (!qb.isAffirmative())
                    return;
            } else {
                new MessageBox(DcResources.getText("msgFieldCannotBeRemoved"), MessageBox._INFORMATION);
                return;
            }
        }
        
        if (table.getSelectedRow() != -1)
            table.removeRow(table.getSelectedRow());
    }
    
    private void build() {
        // Actions
        JPanel panelActions = new JPanel();
        panelActions.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        buttonAdd.addActionListener(this);
        buttonAdd.setActionCommand("createField");
        buttonRemove.addActionListener(this);
        buttonRemove.setActionCommand("delete");
        panelActions.add(buttonRemove);
        panelActions.add(buttonAdd);
        
        // Fields
        JPanel panelFields = new JPanel();
        panelFields.setLayout(Layout.getGBL());

        DcLabel labelPredefined = ComponentFactory.getLabel(DcResources.getText("lblPredefinedFields"));
        DcLabel labelNew = ComponentFactory.getLabel(DcResources.getText("lblNewFields"));
        
        tableSysFields.setColumnCount(1);
        TableColumn colSysField = tableSysFields.getColumnModel().getColumn(0);
        colSysField.setCellEditor(new DefaultCellEditor(ComponentFactory.getTextFieldDisabled()));
        colSysField.setHeaderValue(DcResources.getText("lblName"));

        JScrollPane scrollerSysFields = new JScrollPane(tableSysFields);
        scrollerSysFields.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollerSysFields.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
        table.setColumnCount(1);
        TableColumn colField = table.getColumnModel().getColumn(0);
        JTextField textField = ComponentFactory.getTextFieldDisabled();
        colField.setCellEditor(new DefaultCellEditor(textField));
        colField.setHeaderValue(DcResources.getText("lblName"));
        colField.setCellRenderer(XmlFieldCellRenderer.getInstance());

        JScrollPane scroller = new JScrollPane(table);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
        JLabel labelNav = ComponentFactory.getLabel("");

        buttonUp.addActionListener(this);
        buttonUp.setActionCommand("moveUp");
        buttonDown.addActionListener(this);
        buttonDown.setActionCommand("moveDown");

        buttonUp.setPreferredSize(new Dimension(25, 22));
        buttonUp.setMaximumSize(new Dimension(25, 22));
        buttonUp.setMinimumSize(new Dimension(25, 22));
        
        buttonDown.setPreferredSize(new Dimension(25, 22));
        buttonDown.setMaximumSize(new Dimension(25, 22));
        buttonDown.setMinimumSize(new Dimension(25, 22));

        panelFields.add(labelPredefined, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        panelFields.add(scrollerSysFields, Layout.getGBC(0, 1, 1, 1, 50.0, 50.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        panelFields.add(labelNew, Layout.getGBC(0, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(10, 0, 0, 0), 0, 0));
        panelFields.add(scroller, Layout.getGBC(0, 3, 1, 4, 50.0, 50.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        panelFields.add(labelNav, Layout.getGBC(1, 3, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 5, 5, 5), 0, 0));
        panelFields.add(buttonUp, Layout.getGBC(1, 4, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));
        panelFields.add(buttonDown, Layout.getGBC(1, 5, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(5, 5, 0, 5), 0, 0));

        table.applyHeaders();
        tableSysFields.applyHeaders();
        
        setLayout(Layout.getGBL());
        add(panelFields,    Layout.getGBC(0, 1, 1, 1, 50.0, 50.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0)); 
        add(panelActions,   Layout.getGBC(0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 10), 0, 0)); 
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("createField"))
            createField();
        else if (ae.getActionCommand().equals("delete"))
            delete();
        else if (ae.getActionCommand().equals("moveUp"))
            table.moveRowUp();
        else if (ae.getActionCommand().equals("moveDown"))
            table.moveRowDown();
    }
}