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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.lists.DcFieldList;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Tab;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.DcFieldDefinitions;

public class TabFieldsPanel extends JPanel implements ActionListener, ComponentListener {
    
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
        DcFieldDefinitions definitions = new DcFieldDefinitions();
        for (String key : listsRight.keySet()) {
            for (DcField field : listsRight.get(key).getFields()) {
                DcFieldDefinition definition = field.getDefinition(); 
                definition.setTab(key);
                definitions.add(definition);
            }
        }
        
        for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
            if (definitions.get(definition.getIndex()) == null)
                definitions.add(definition);
        }
        
        module.setSetting(DcRepository.ModuleSettings.stFieldDefinitions, definitions);
    }
    
    private void applyTab(String tab) {
        for (JScrollPane scroller : scrollersRight.values())
            scroller.setVisible(false);
        
        scrollersRight.get(tab).setVisible(true);
        
        revalidate();
        repaint();
    }
    
    public void refresh(boolean tabDelete) {
        Collection<String> previous = new ArrayList<String>();
        for (int i = 0; i < cbTabs.getItemCount(); i++)
            previous.add((String) cbTabs.getItemAt(i));
        
        cbTabs.removeActionListener(this);
        cbTabs.removeAllItems();
        
        for (DcObject tab : DataManager.getTabs(module.getIndex())) {
            String name = tab.getDisplayString(Tab._A_NAME);
            cbTabs.addItem(name);
            
            if (!scrollersRight.containsKey(name)) {
                createTabPanel(name);
                revalidate();
                repaint();
            }
        }
        
        if (tabDelete) {
            Collection<String> current = new ArrayList<String>();
            for (int j = 0; j < cbTabs.getItemCount(); j++)
                current.add((String) cbTabs.getItemAt(j));

            for (String prev : previous) {
                if (!current.contains(prev)) {
                    for (DcField field : listsRight.get(prev).getFields()) {
                        field.getDefinition().setTab(null);
                        listLeft.add(field);
                    }
                }
            }
        }
        
        cbTabs.addActionListener(this);
    	cbTabs.setSelectedIndex(0);
    }
    
    private void createTabPanel(String tab) {
        DcFieldList listRight = new DcFieldList();
        listRight.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listRight.addMouseListener(new ListMouseListener(ListMouseListener._LEFT));
        listsRight.put(tab, listRight);
        
        JScrollPane scrollerRight = new JScrollPane(listRight);
        scrollerRight.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollerRight.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollersRight.put(tab, scrollerRight);
        
        add(scrollerRight, Layout.getGBC( 1, 3, 1, 1, 40.0, 40.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 0, 0, 0), 0, 0));
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
        
        module = null;
    }    
    
    private DcFieldList getList() {
        return listsRight.get(cbTabs.getSelectedItem());
    }
    
    private void initialize() {
        
        for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
            
            DcField field = module.getField(definition.getIndex());
            
            if ((!field.isUiOnly() || field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) && 
                field.isEnabled() && 
                field.getValueType() != DcRepository.ValueTypes._PICTURE && // check the field type
                field.getValueType() != DcRepository.ValueTypes._ICON &&
               (field.getIndex() != module.getDcObject().getParentReferenceFieldIndex() || 
                field.getIndex() == DcObject._SYS_CONTAINER )) {

                listLeft.add(field);
            }
        }
        
        for (DcField field : listLeft.getFields()) {
            String tab = field.getDefinition().getTab(field.getModule());
            
            if (tab != null && !tab.trim().equals("")) {
                listsRight.get(tab).add(field);
                listLeft.remove(field);
            }
        }
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        /*****************************************************************************
         * Navigation panel
         *****************************************************************************/
        JPanel panelNav = new JPanel();
        panelNav.setLayout(Layout.getGBL());
        
        JButton buttonTop = ComponentFactory.getIconButton(IconLibrary._icoArrowTop);
        JButton buttonUp = ComponentFactory.getIconButton(IconLibrary._icoArrowUp);
        JButton buttonDown = ComponentFactory.getIconButton(IconLibrary._icoArrowDown);
        JButton buttonBottom = ComponentFactory.getIconButton(IconLibrary._icoArrowBottom);

        buttonTop.addActionListener(this);
        buttonTop.setActionCommand("rowToTop");
        buttonUp.addActionListener(this);
        buttonUp.setActionCommand("rowUp");
        buttonDown.addActionListener(this);
        buttonDown.setActionCommand("rowDown");
        buttonBottom.addActionListener(this);
        buttonBottom.setActionCommand("rowToBottom");
        
        panelNav.add(buttonTop, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        panelNav.add(buttonUp,  Layout.getGBC(0, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        panelNav.add(buttonDown,Layout.getGBC(0, 3, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        panelNav.add(buttonBottom,Layout.getGBC(0, 4, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        
        /*****************************************************************************
         * Input panel
         *****************************************************************************/
        
        for (DcObject tab : DataManager.getTabs(module.getIndex())) {
            String name = tab.getDisplayString(Tab._A_NAME);
            cbTabs.addItem(name);
            createTabPanel(name);
        }
        
        cbTabs.setActionCommand("tabSelect");
        cbTabs.addActionListener(this);
        
        listLeft = new DcFieldList();
        listLeft.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listLeft.addMouseListener(new ListMouseListener(ListMouseListener._RIGHT));

        JScrollPane scrollerLeft = new JScrollPane(listLeft);
        scrollerLeft.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollerLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JPanel panelInput = new JPanel();
        panelInput.setLayout(Layout.getGBL());
        
        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblTab")), Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        panelInput.add(cbTabs,     Layout.getGBC( 1, 0, 1, 1, 20.0, 20.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));

        
        /*****************************************************************************
         * Main panel
         *****************************************************************************/

        add(panelInput, Layout.getGBC( 0, 0, 3, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 5, 5, 0, 5), 0, 0));
        add(ComponentFactory.getLabel(DcResources.getText("lblAvailableFields")),  Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 10, 5, 0, 0), 0, 0));
        add(ComponentFactory.getLabel(DcResources.getText("lblSelectedFields")), Layout.getGBC( 1, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 10, 0, 0, 0), 0, 0));
        add(scrollerLeft,  Layout.getGBC( 0, 3, 1, 1, 20.0, 20.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 0, 0), 0, 0));
        add(panelNav, Layout.getGBC(2, 3, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 5, 5, 5), 0, 0));
        
        addComponentListener(this);
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
                    if (field != null) {
	                    field.getDefinition().setTab(null);
	                    listLeft.add(field);
	                    getList().remove();
	                    getList().clearSelection();
                    }
                } else {
                    DcField field =  listLeft.getSelected();
                    if (field != null) {
	                    getList().add(field);
	                    listLeft.remove();
	                    listLeft.clearSelection();
                    }
                }
                
                listLeft.revalidate();
                getList().revalidate();
                revalidate();
                repaint();
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
        else if (ae.getActionCommand().equals("rowUp"))
            getList().moveRowUp();
        else if (ae.getActionCommand().equals("rowDown"))
            getList().moveRowDown();
        else if (ae.getActionCommand().equals("rowToTop"))
            getList().moveRowToTop();
        else if (ae.getActionCommand().equals("rowToBottom"))
            getList().moveRowToBottom();
    }

    private void checkListSizes() {
//        Dimension dimLeft = listLeft.getSize();
//        
//        Dimension dim = null;
//        for (DcFieldList listRight : listsRight.values()) {
//            if (listRight.isVisible()) {
//                int width = (int) ((dimLeft.getWidth() + listRight.getWidth()) - 10) / 2; 
//                dim = new Dimension(width, (int) dimLeft.getHeight());
//                break;
//            }
//        }
//        
//        for (DcFieldList listRight : listsRight.values()) {
//            listRight.setPreferredSize(dim);
//            listRight.setMinimumSize(dim);
//            listRight.setMaximumSize(dim);
//            
//            if (listRight.isVisible()) {
//                listRight.revalidate();
//                listRight.repaint();
//            }
//        }
//        
//        listLeft.setPreferredSize(dim);
//        listLeft.setMinimumSize(dim);
//        listLeft.setMaximumSize(dim);
//        listLeft.revalidate();
//        listLeft.repaint();
//        
//        revalidate();
//        repaint();
    }
    
    public void componentHidden(ComponentEvent e) {}

    public void componentMoved(ComponentEvent e) {}

    public void componentResized(ComponentEvent e) {
        checkListSizes();
    }

    public void componentShown(ComponentEvent e) {}
    
}
