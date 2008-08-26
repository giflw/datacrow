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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
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
import net.datacrow.settings.definitions.WebFieldDefinition;
import net.datacrow.settings.definitions.WebFieldDefinitions;

public class WebFieldDefinitionsDialog extends DcDialog implements ActionListener {

    private DefinitionPanel panelDefinitionsParent = null;
    private DefinitionPanel panelDefinitionsChild = null;

    public WebFieldDefinitionsDialog() {
        super(DataCrow.mainFrame);

        setHelpIndex("dc.settings.webfields");

        buildDialog();

        setTitle(DcResources.getText("lblWebFieldSettings"));
        setModal(true);
    }

    @Override
    public void close() {
        Settings settings = DcModules.getCurrent().getSettings();
        settings.set(DcRepository.ModuleSettings.stWebFieldSettingsDialogSize, getSize());
        
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

        getContentPane().add(tp, Layout.getGBC(0, 0, 1, 1, 10.0, 10.0,
                             GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                             new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                             GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                             new Insets(5, 5, 5, 5), 0, 0));

        pack();

        Settings settings = DcModules.getCurrent().getSettings();
        Dimension dim = settings.getDimension(DcRepository.ModuleSettings.stFieldSettingsDialogSize);
        setSize(dim);

        setCenteredLocation();
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("save"))
            save();
    }

    protected static class DefinitionPanel extends JPanel {

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

            table.setColumnCount(7);

            TableColumn c = table.getColumnModel().getColumn(6);
            table.removeColumn(c);

            c = table.getColumnModel().getColumn(0);
            JTextField textField = ComponentFactory.getTextFieldDisabled();
            c.setCellEditor(new DefaultCellEditor(textField));
            c.setHeaderValue(DcResources.getText("lblField"));

            c = table.getColumnModel().getColumn(1);
            c.setCellEditor(new DefaultCellEditor(ComponentFactory.getNumberField()));
            c.setHeaderValue(DcResources.getText("lblWidth"));

            c = table.getColumnModel().getColumn(2);
            c.setCellEditor(new DefaultCellEditor(ComponentFactory.getNumberField()));
            c.setHeaderValue(DcResources.getText("lblMaxTextLength"));
            
            c = table.getColumnModel().getColumn(3);
            c.setCellEditor(new DefaultCellEditor(ComponentFactory.getCheckBox("")));
            c.setCellRenderer(CheckBoxTableCellRenderer.getInstance());
            c.setHeaderValue(DcResources.getText("lblVisible"));
            
            c = table.getColumnModel().getColumn(4);
            c.setCellEditor(new DefaultCellEditor(ComponentFactory.getCheckBox("")));
            c.setCellRenderer(CheckBoxTableCellRenderer.getInstance());
            c.setHeaderValue(DcResources.getText("lblLinkToDetails"));
            
            c = table.getColumnModel().getColumn(5);
            c.setCellEditor(new DefaultCellEditor(ComponentFactory.getCheckBox("")));
            c.setCellRenderer(CheckBoxTableCellRenderer.getInstance());
            c.setHeaderValue(DcResources.getText("lblQuickSearchField"));            

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
            module.setSetting(DcRepository.ModuleSettings.stWebFieldDefinitions, getDefinitions());

            if (module.getSearchView() != null)
                module.getSearchView().applySettings();

            if (module.getInsertView() != null)
                module.getInsertView().applySettings();
        }

        public void applyDefinitions() {
            table.clear();
            
            for (WebFieldDefinition definition : module.getWebFieldDefinitions().getDefinitions()) {
                if (module.getField(definition.getField()) != null)
                    table.addRow(definition.getDisplayValues(module));
            }
        }

        private WebFieldDefinition getDefinition(int row) {
            int width = table.getValueAt(row, 1, true) instanceof Number ?
                    ((Number) table.getValueAt(row, 1, true)).intValue() : 
                    Integer.valueOf((String) table.getValueAt(row, 1, true));
            int maxText = table.getValueAt(row, 2, true) instanceof Number ?
                    ((Number) table.getValueAt(row, 2, true)).intValue() : 
                    Integer.valueOf((String) table.getValueAt(row, 2, true));
            boolean enabled = ((Boolean) table.getValueAt(row, 3, true)).booleanValue();
            boolean link = ((Boolean) table.getValueAt(row, 4, true)).booleanValue();
            boolean quickSearch = ((Boolean) table.getValueAt(row, 5, true)).booleanValue();
            DcField field = (DcField) table.getValueAt(row, 6, true);

            return new WebFieldDefinition(field.getIndex(), width, maxText, enabled, link, quickSearch);
        }

        public WebFieldDefinitions getDefinitions() {
            table.cancelEdit();
            WebFieldDefinitions definitions = new WebFieldDefinitions();
            for (int i = 0; i < table.getRowCount(); i++) {
                definitions.add(getDefinition(i));
            }
            return definitions;
        }
    }
}
