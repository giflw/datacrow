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

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcColorSelector;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.panels.NavigationPanel;
import net.datacrow.console.components.renderers.CheckBoxTableCellRenderer;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.Settings;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.IDefinitions;
import net.datacrow.settings.definitions.QuickViewFieldDefinition;
import net.datacrow.settings.definitions.QuickViewFieldDefinitions;

public class QuickViewSettingsDialog extends DcDialog implements ActionListener {

    private DefinitionPanel panelDefinitionsParent = null;
    private DefinitionPanel panelDefinitionsChild = null;

    public QuickViewSettingsDialog() {
        super(DataCrow.mainFrame);

        setHelpIndex("dc.settings.quickview");

        buildDialog();

        setTitle(DcResources.getText("lblQuickViewSettings"));
        setModal(true);
    }

    @Override
    public void close() {
        Settings settings = DcModules.getCurrent().getSettings();
        settings.set(DcRepository.ModuleSettings.stQuickViewSettingsDialogSize, getSize());
        
        if (panelDefinitionsParent != null) {
            panelDefinitionsParent.clear();
            panelDefinitionsParent = null;
        }

        if (panelDefinitionsChild != null) {
            panelDefinitionsChild.clear();
            panelDefinitionsChild = null;
        }
        
        super.close();
    }

    private void save() {
        panelDefinitionsParent.save();
        if (panelDefinitionsChild != null)
            panelDefinitionsChild.save();
        
        DcModule module = DcModules.getCurrent();
        if (module.getSearchView() != null)
            module.getSearchView().applySettings();

        if (module.getInsertView() != null)
            module.getInsertView().applySettings();
    }

    private void buildDialog() {
        getContentPane().setLayout(Layout.getGBL());

        /***********************************************************************
         * ACTIONS PANEL
         **********************************************************************/
        JPanel panelActions = new JPanel();
        panelActions.setLayout(Layout.getGBL());

        JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));

        buttonSave.setMnemonic('S');
        buttonClose.setMnemonic('C');
        
        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        panelActions.add(buttonSave, Layout.getGBC(0, 0, 1, 4, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        panelActions.add(buttonClose, Layout.getGBC(1, 0, 1, 4, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        /***********************************************************************
         * MAIN PANEL
         **********************************************************************/
        
        JTabbedPane tp = ComponentFactory.getTabbedPane();
        
        DcModule module = DcModules.get(DcSettings.getInt(DcRepository.Settings.stModule));
        panelDefinitionsParent = new DefinitionPanel(module);
        tp.addTab(DcResources.getText("lblXFields", module.getLabel()), panelDefinitionsParent);

        if (module.getChild() != null) {
            panelDefinitionsChild = new DefinitionPanel(module.getChild());
            tp.addTab(DcResources.getText("lblXFields", module.getChild().getLabel()), panelDefinitionsChild);
        }
        
        DcColorSelector cs = ComponentFactory.getColorSelector(DcRepository.Settings.stQuickViewBackgroundColor);
        cs.setValue(DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor));
        tp.addTab(DcResources.getText("lblBackgroundColor"), cs);        
        
        getContentPane().add(tp, Layout.getGBC(0, 1, 1, 1, 10.0, 10.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions, Layout.getGBC(0, 2, 1, 1, 1.0, 1.0,
                 GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));

        pack();

        Settings settings = DcModules.getCurrent().getSettings();
        Dimension dim = settings.getDimension(DcRepository.ModuleSettings.stQuickViewSettingsDialogSize);
        setSize(dim);

        setCenteredLocation();
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("save"))
            save();
    }
    
    protected static class DefinitionPanel extends JPanel implements ActionListener {

        private DcTable table;
        private DcModule module;
        private NavigationPanel panelNav;
        
        public DefinitionPanel(DcModule module) {
            this.module = module;
            table = ComponentFactory.getDCTable(false, false);
            build();
        }
        
        protected void clear() {
            table = null;
            module = null;
            
            if (panelNav != null) {
                panelNav.clear();
                panelNav = null;
            }
        }

        private void build() {
            setLayout(Layout.getGBL());

            table.setColumnCount(5);
            TableColumn columnHidden = table.getColumnModel().getColumn(3);
            table.removeColumn(columnHidden);

            TableColumn columnField = table.getColumnModel().getColumn(0);
            JTextField textField = ComponentFactory.getTextFieldDisabled();
            columnField.setCellEditor(new DefaultCellEditor(textField));
            columnField.setHeaderValue(DcResources.getText("lblLabel"));

            TableColumn columnEnabled = table.getColumnModel().getColumn(1);
            JCheckBox checkEnabled = new JCheckBox();
            columnEnabled.setCellEditor(new DefaultCellEditor(checkEnabled));
            columnEnabled.setCellRenderer(CheckBoxTableCellRenderer.getInstance());
            columnEnabled.setHeaderValue(DcResources.getText("lblEnabled"));
            
            JComboBox comboDirection = ComponentFactory.getComboBox(
                    new String[] {DcResources.getText("lblHorizontal"), DcResources.getText("lblVertical")});

            TableColumn columnDirection = table.getColumnModel().getColumn(2);
            columnDirection.setCellEditor(new DefaultCellEditor(comboDirection));
            columnDirection.setHeaderValue(DcResources.getText("lblDirection"));

            TableColumn columnMaxLength = table.getColumnModel().getColumn(3);
            columnMaxLength.setCellEditor(new DefaultCellEditor(ComponentFactory.getNumberField()));
            columnMaxLength.setHeaderValue(DcResources.getText("lblMaxLength"));
            
            applyDefinitions();

            JScrollPane scroller = new JScrollPane(table);
            scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
            
            panelNav = new NavigationPanel(table);

            add(scroller,  Layout.getGBC(0, 0, 1, 1, 50.0, 50.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            add(panelNav,  Layout.getGBC(1, 0, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(0, 5, 5, 5), 0, 0));

            table.applyHeaders();
        }

        private void save() {
            module.setSetting(DcRepository.ModuleSettings.stQuickViewFieldDefinitions, getDefinitions());
        }

        public void applyDefinitions() {
            QuickViewFieldDefinitions definitions = (QuickViewFieldDefinitions) 
                module.getSettings().getDefinitions(DcRepository.ModuleSettings.stQuickViewFieldDefinitions);
            table.clear();
            
            // add re-enabled fields
            for (DcFieldDefinition def : module.getFieldDefinitions().getDefinitions()) {
                boolean exists = false;
                for (QuickViewFieldDefinition qvDef : definitions.getDefinitions()) {
                    if (def.getIndex() == qvDef.getField())
                        exists = true;
                }
                
                if (!exists && def.isEnabled())
                    definitions.add(new QuickViewFieldDefinition(
                            def.getIndex(), false, DcResources.getText("lblHorizontal"), 0));
            }
            
            for (QuickViewFieldDefinition definition : definitions.getDefinitions())
                table.addRow(definition.getDisplayValues(module));
        }

        public IDefinitions getDefinitions() {
            table.cancelEdit();
            
            QuickViewFieldDefinitions qvDefs = new QuickViewFieldDefinitions();
            for (int row = 0; row < table.getRowCount(); row++) {
                boolean enabled = ((Boolean) table.getValueAt(row, 1, true)).booleanValue();
                String direction = (String) table.getValueAt(row, 2, true);
                DcField field = (DcField) table.getValueAt(row, 3, true);
                int length = table.getValueAt(row, 4, true) != null ? 
                             Integer.valueOf(table.getValueAt(row, 4, true).toString()) : 0;
                qvDefs.add(new QuickViewFieldDefinition(field.getIndex(), enabled, direction, length));
            }
            return qvDefs;
        }
        
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("rowToTop"))
                table.moveRowToTop();
            else if (ae.getActionCommand().equals("rowToBottom"))
                table.moveRowToBottom();
            else if (ae.getActionCommand().equals("rowUp"))
                table.moveRowUp();
            else if (ae.getActionCommand().equals("rowDown"))
                table.moveRowDown();
        }
    }
}
