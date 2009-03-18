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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcShortTextField;
import net.datacrow.console.components.lists.DcFieldList;
import net.datacrow.console.components.lists.elements.DcFieldListElement;
import net.datacrow.console.components.lists.elements.DcListElement;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Tab;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.definitions.DcFieldDefinition;

public class TabFieldsPanel extends JPanel implements KeyListener, ActionListener {
    
    private Vector<DcListElement> elements;
    
    private DcFieldList listLeft;
    private JComboBox cbTabs = ComponentFactory.getComboBox();
    
    private Map<String, DcFieldList> listsRight = new HashMap<String, DcFieldList>();
    private Map<String, JScrollPane> scrollersRight = new HashMap<String, JScrollPane>();
    
    private DcModule module;
    
    public TabFieldsPanel(DcModule module) {
        this.module = module;

        build();
    }
    
    protected void save() {
        for (String key : listsRight.keySet()) {
            for (DcField field : listsRight.get(key).getFields())
                field.getDefinition().setTab(key);
        }
    }
    
    private void applyTab(String tab) {
        for (JScrollPane scroller : scrollersRight.values())
            scroller.setVisible(false);
        
        scrollersRight.get(tab).setVisible(true);
        
        revalidate();
        repaint();
    }
    
    public void refresh() {
        cbTabs.removeActionListener(this);
        cbTabs.removeAllItems();
        
        for (DcObject tab : DataManager.getTabs(module.getIndex()))
            cbTabs.addItem(tab.getDisplayString(Tab._A_NAME));
        
        cbTabs.addActionListener(this);
    }

    public void clear() {
        if (listLeft != null)
            listLeft.clear();
        
        listLeft = null;
        
        if (listsRight != null) {
            for (DcFieldList list : listsRight.values())
                list.clear();
            
            listsRight.clear();
            listsRight = null;
        }
        
        if (elements != null)
            elements.clear();
        
        elements = null;
        module = null;
    }    
    
    private DcFieldList getList() {
        return listsRight.get(cbTabs.getSelectedItem());
    }
    
    private void initialize() {
        
        for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
            
            DcField field = module.getField(definition.getIndex());
            if (field.isEnabled() && 
               (field.getValueType() != DcRepository.ValueTypes._PICTURE &&
                field.getValueType() != DcRepository.ValueTypes._ICON)) {

                listLeft.add(field);
            }
        }
        
        elements = new Vector<DcListElement>();
        elements.addAll(listLeft.getElements());
        
        for (DcField field : listLeft.getFields()) {
            String tab = field.getDefinition().getTab();
            
            if (tab != null && !tab.trim().equals("")) {
                listsRight.get(tab).add(field);
                listLeft.remove(field);
            }
        }
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        for (DcObject tab : DataManager.getTabs(module.getIndex())) {
            cbTabs.addItem(tab.getDisplayString(Tab._A_NAME));
            
            DcFieldList listRight = new DcFieldList();
            listRight.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            listRight.addMouseListener(new ListMouseListener(ListMouseListener._LEFT));
            listsRight.put(tab.getDisplayString(Tab._A_NAME), listRight);
            
            JScrollPane scrollerRight = new JScrollPane(listRight);
            scrollerRight.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollerRight.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollersRight.put(tab.getDisplayString(Tab._A_NAME), scrollerRight);
        }
        
        cbTabs.setActionCommand("tabSelect");
        cbTabs.addActionListener(this);
        
        JTextField txtFilter = ComponentFactory.getShortTextField(255);
        txtFilter.addKeyListener(this);

        listLeft = new DcFieldList();
        listLeft.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listLeft.addMouseListener(new ListMouseListener(ListMouseListener._RIGHT));

        JScrollPane scrollerLeft = new JScrollPane(listLeft);
        scrollerLeft.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollerLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JPanel panelInput = new JPanel();
        panelInput.setLayout(Layout.getGBL());
        
        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblTab")), Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        panelInput.add(cbTabs,     Layout.getGBC( 1, 0, 1, 1, 20.0, 20.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblFilter")), 
                 Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        panelInput.add(txtFilter,     Layout.getGBC( 1, 1, 1, 1, 20.0, 20.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        
        add(panelInput, Layout.getGBC( 0, 0, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(ComponentFactory.getLabel(DcResources.getText("lblAvailableFields")),  Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 5, 0, 0, 0), 0, 0));
        add(ComponentFactory.getLabel(DcResources.getText("lblSelectedFields")), Layout.getGBC( 1, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 5, 0, 0, 0), 0, 0));
        add(scrollerLeft,  Layout.getGBC( 0, 3, 1, 1, 20.0, 20.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 0, 0, 0), 0, 0));
        
        for (JScrollPane scroller : scrollersRight.values()) {
            add(scroller, Layout.getGBC( 1, 3, 1, 1, 40.0, 40.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets( 0, 0, 0, 0), 0, 0));
        }
        
        initialize();
        cbTabs.setSelectedIndex(0);
    }
    
    private class ListMouseListener implements MouseListener {
        
        public static final int _LEFT = 0;
        public static final int _RIGHT = 1;
        
        int direction;
        
        public ListMouseListener(int direction) {
            this.direction = direction;
        }
        
        public void mouseReleased(MouseEvent e) {
            if (e.getClickCount() == 2) {
                if (direction == _LEFT) {
                    DcField field = getList().getSelected();

                    field.getDefinition().setTab(null);
                    
                    listLeft.add(field);
                    getList().remove();
                    getList().clearSelection();
                } else {
                    DcField field =  listLeft.getSelected();
                    getList().add(field);
                    listLeft.remove();
                    listLeft.clearSelection();
                }
            }  
        }
        
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {}
        public void mouseClicked(MouseEvent e) {}
    } 
    
    public void keyPressed(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("tabSelect"))
            applyTab((String) cbTabs.getSelectedItem());
    }

    public void keyReleased(KeyEvent e) {
        DcShortTextField txtFilter = (DcShortTextField) e.getSource();
        String filter = txtFilter.getText();
        
        if (filter.trim().length() == 0) {
            listLeft.setListData(elements);
        } else {
            Vector<DcListElement> newElements = new Vector<DcListElement>();
            for (DcListElement element : elements) {
                String displayValue = ((DcFieldListElement) element).getField().getLabel();
                if (displayValue.toLowerCase().startsWith(filter.toLowerCase()))
                    newElements.add(element);
            }
            listLeft.setListData(newElements);
        }
    }
}
