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

package net.datacrow.console.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcComboBox;
import net.datacrow.console.components.DcEditableComboBox;
import net.datacrow.console.components.DcReferenceField;
import net.datacrow.console.components.renderers.ComboBoxRenderer;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcProperty;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.PollerTask;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class DcQuickFilterToolBar extends JToolBar implements ActionListener, MouseListener, KeyListener {

    private static Logger logger = Logger.getLogger(DcQuickFilterToolBar.class.getName());
    
    private final DcEditableComboBox comboCriteria = new DcEditableComboBox();
    private final DcComboBox comboFields = ComponentFactory.getComboBox(new DefaultComboBoxModel());
    private final DcComboBox comboFilters = ComponentFactory.getComboBox();
    
    private final DcModule module;
    
    public DcQuickFilterToolBar(DcModule module) {
        this.module = module;
        build();
        
        // set default value (if applicable)
        int fieldIdx = module.getSettings().getInt(DcRepository.ModuleSettings.stQuickFilterDefaultField);
        comboFields.setSelectedItem(new Field(module.getField(fieldIdx)));
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        for (Component c : getComponents()) 
            c.setFont(font);
    }    
    
    private void search() {
        search(getDataFilter());
    }
    
    private class FilterTask extends Thread {
        
        private final DataFilter df;
        
        public FilterTask(DataFilter df) {
            this.df = df;
        }
        
        @Override
        public void run() {
            PollerTask poller = new PollerTask(this, DcResources.getText("lblFiltering"));
            poller.start();
            
            DataFilters.setCurrent(module.getIndex(), df);
            List<Long> keys = DataManager.getKeys(df == null ? DataFilters.getCurrent(module.getIndex()) : df);
            DcModules.getCurrent().getSearchView().add(keys);
            
            try {
                poller.finished(true);
            } catch (Exception e) {
                logger.error(e, e);
                DcSwingUtilities.displayErrorMessage(Utilities.isEmpty(e.getMessage()) ? e.toString() : e.getMessage());
            }
        }
    }
    
    private void search(DataFilter df) {
        FilterTask ft = new FilterTask(df);
        ft.start();
    }
    
    private Object getValue() {
        DcField field = ((Field) comboFields.getSelectedItem()).getField();
        
        Object value = comboCriteria.getSelectedItem();
        if (field.getFieldType() == ComponentFactory._RATINGCOMBOBOX)
            value = value != null && value.equals(Long.valueOf(-1)) ? null : value;
        
        value = value != null && value instanceof String ? ((String) value).trim() : value;
        
        return value;
    }
    
    private DataFilter getDataFilter() {
        DcField field = ((Field) comboFields.getSelectedItem()).getField();
        Object value = getValue();
        
        if (value != null && !value.equals("")) {
            Operator operator = Operator.CONTAINS;
            
            if (value.equals(DcResources.getText("lblIsEmpty"))) {
                operator = Operator.IS_EMPTY;
                value = null;
            } else if (value.equals(DcResources.getText("lblIsFilled"))) {
                operator = Operator.IS_FILLED;
                value = null;
            } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                Collection<Object> c = new ArrayList<Object>();
                c.add(value);
                value = c;
            } else if (field.getValueType() == DcRepository.ValueTypes._LONG) {
                operator = Operator.EQUAL_TO;
            }
            
            DataFilter df = DataFilters.getDefaultDataFilter(module.getIndex());
            DataFilterEntry dfe = new DataFilterEntry(DataFilterEntry._AND, 
                                                      field.getModule(), 
                                                      field.getIndex(), 
                                                      operator,
                                                      value);
            df.addEntry(dfe);
            return df;
        } else {
            return null;
        }
    }

    private void setSearchField(DcField field) {
        JComponent c = ComponentFactory.getComponent(field.getModule(), field.getReferenceIdx(), field.getIndex(), field.getFieldType(), field.getLabel(), 255);
        
        if (c instanceof DcReferenceField) 
            c = ((DcReferenceField) c).getComboBox();
        
        comboCriteria.removeAllItems();
        
        if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
            
            int[] fields;
            if (DcModules.get(field.getReferenceIdx()).getType() == DcModule._TYPE_PROPERTY_MODULE) {
                fields = new int[] {DcObject._ID, DcProperty._A_NAME, DcProperty._B_ICON};
            } else {
                fields = new int[] {DcObject._ID, DcModules.get(field.getReferenceIdx()).getDisplayFieldIdx()};
            }
            
            List<DcObject> objects = DataManager.get(field.getReferenceIdx(), fields);
            
            comboCriteria.addItem(" ");
            for (Object o : objects)
                comboCriteria.addItem(o);
            
            comboCriteria.setRenderer(ComboBoxRenderer.getInstance());
            comboCriteria.setEditable(false);
            
        } else if (c instanceof JComboBox) {
            JComboBox combo = (JComboBox) c;
            for (int i = 0; i < combo.getItemCount(); i++)
                comboCriteria.addItem(combo.getItemAt(i));
            
            comboCriteria.setRenderer(combo.getRenderer());
            comboCriteria.setEditable(false);
        } else {
        	comboCriteria.addItem(" ");
            comboCriteria.setEditable(true);
        }

        comboCriteria.setEnabled(true);
        
        comboCriteria.addItem(DcResources.getText("lblIsFilled"));
        comboCriteria.addItem(DcResources.getText("lblIsEmpty"));

        revalidate();
    }
    
    private void applySelectedField() {
        comboCriteria.removeAllItems();
        
        if (comboFields.getSelectedItem() != null) {
            DcField field = ((Field) comboFields.getSelectedItem()).getField();
            setSearchField(field);
            module.getSettings().set(DcRepository.ModuleSettings.stQuickFilterDefaultField, field.getIndex());
        }
    }
    
    private void applySelectedFilter() {
        DataFilter df = comboFilters.getSelectedIndex() > 0 ? (DataFilter) comboFilters.getSelectedItem() : null;
        search(df);
    }
    
    private void build() {
        List<Field> allFields = new ArrayList<Field>();
        for (DcField field : module.getFields()) {
            if (field.isSearchable() && field.isEnabled())
                allFields.add(new Field(field));
        }
        
        if (module.getChild() != null) {
            for (DcField field : module.getChild().getFields()) {
                if (field.isSearchable() && field.isEnabled())
                    allFields.add(new Field(field));
            }
        }
        
        Collections.sort(allFields);
        for (Field field : allFields) 
            comboFields.addItem(field);
        
        Collection<DataFilter> filters = DataFilters.get(module.getIndex());
        comboFilters.addItem(" ");
        for (DataFilter df : filters)
            comboFilters.addItem(df);
        
        comboCriteria.getEditor().getEditorComponent().addKeyListener(this);
        comboCriteria.getEditor().getEditorComponent().addMouseListener(this);
        comboCriteria.setRenderer(new DefaultListCellRenderer());
        
        comboFilters.addActionListener(this);
        comboFilters.setActionCommand("filterSelected");
        
        comboFields.addActionListener(this);
        comboFields.setActionCommand("fieldSelected");
        
        comboFields.setMinimumSize(new Dimension(175, ComponentFactory.getPreferredFieldHeight()));
        comboFields.setPreferredSize(new Dimension(175, ComponentFactory.getPreferredFieldHeight()));
        
        comboFilters.setPreferredSize(new Dimension(175, ComponentFactory.getPreferredFieldHeight()));
        comboFilters.setMinimumSize(new Dimension(175, ComponentFactory.getPreferredFieldHeight()));
        
        comboCriteria.setMinimumSize(new Dimension(175, ComponentFactory.getPreferredFieldHeight()));
        comboCriteria.setPreferredSize(new Dimension(175, ComponentFactory.getPreferredFieldHeight()));

        
        add(ComponentFactory.getLabel(DcResources.getText("lblQuickFilter") + " "));
        add(comboFields);
        add(comboCriteria);

        JButton buttonCancel = ComponentFactory.getIconButton(IconLibrary._icoRemove);
        buttonCancel.setActionCommand("cancel");
        buttonCancel.addActionListener(this);
        
        JButton button1 = ComponentFactory.getIconButton(IconLibrary._icoAccept);
        button1.setActionCommand("search");
        button1.addActionListener(this);

        JButton button2 = ComponentFactory.getIconButton(IconLibrary._icoAccept);
        button2.setActionCommand("searchOnSelectedFilter");
        button2.addActionListener(this);

        add(button1);
        add(buttonCancel);
        
        if (filters.size() > 0) {
            addSeparator();
            
            add(ComponentFactory.getLabel(DcResources.getText("lblFilters") + " "));
            add(comboFilters);
            add(button2);
        }
    }    
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("searchOnSelectedFilter"))
            search(comboFilters.getSelectedIndex() > 0 ? (DataFilter) comboFilters.getSelectedItem() : null);    
        else if (ae.getActionCommand().equals("search"))
            search();
        else if (ae.getActionCommand().equals("cancel"))
            search(null);
        else if (ae.getActionCommand().equals("fieldSelected"))
            applySelectedField();
        else if (ae.getActionCommand().equals("filterSelected"))
            applySelectedFilter();
    }
    
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            search();
    }

    public void keyPressed(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {
        if (e.getClickCount() == 2) {
            search();
        }            
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(DcSwingUtilities.setRenderingHint(g));
    }
    
    private class Field implements Comparable<Field> {
        
        private DcField field;
        
        protected Field(DcField field) {
            this.field = field;
        }
        
        public DcField getField() {
            return field;
        }
        
        @Override
        public String toString() {
            if (DcModules.getCurrent().isParentModule())
                return field.getLabel() + " (" + DcModules.get(field.getModule()).getLabel() + ")";
            else
                return field.getLabel();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof Field && getField() != null ? getField().equals(((Field) obj).getField()) : false;
        }
        
        public int compareTo(Field o) {
            return toString().compareTo(o.toString());
        }
    }
}
