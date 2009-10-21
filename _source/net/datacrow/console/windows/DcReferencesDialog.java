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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.DcShortTextField;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.components.lists.elements.DcListElement;
import net.datacrow.console.components.lists.elements.DcObjectListElement;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.MappingModule;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class DcReferencesDialog extends DcDialog implements ActionListener, KeyListener {
    
    private Collection<DcObject> selected;
    private Vector<DcListElement> elements;
    private DcObjectList listRight;
    private DcObjectList listLeft;
    private MappingModule mappingModule;
    
    private boolean saved = false;
    
    public DcReferencesDialog(Collection<DcObject> currentItems, MappingModule mappingModule) {
        selected = new ArrayList<DcObject>();
        this.mappingModule = mappingModule;
        
        Collection<DcObject> current = currentItems == null ? new ArrayList<DcObject>() : currentItems;
        
        setTitle(DcModules.get(mappingModule.getReferencedModIdx()).getObjectNamePlural());
        buildDialog();
        
        DcObject[] dcos = DataManager.get(mappingModule.getReferencedModIdx(), null);
        listLeft.add(dcos);
        
        int counter = 0;
        String[] ids = new String[current.size()];
        
        for (DcObject dco : current) {
            DcMapping mapping = (DcMapping) dco;
            DcObject reference = mapping.getReferencedObject();
            if (reference != null) {
                selected.add(reference);
                ids[ counter++] = reference.getID();
            }
        }
        listRight.add(selected.toArray(new DcObject[] {}));
        listLeft.remove(ids);

        // store the elements for filtering purposes
        elements = new Vector<DcListElement>();
        elements.addAll(listLeft.getElements());
        
        pack();
        
        setSize(DcSettings.getDimension(DcRepository.Settings.stReferencesDialogSize));
        setCenteredLocation();
        setModal(true);
    }
    
    public Collection<DcObject> getDcObjects() {
        return selected;
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    private void save() {
        selected.clear();
        
        Collection<DcObject> c = listRight.getItems();
        for (DcObject dco : c) {
            DcMapping mapping = (DcMapping) mappingModule.getItem();
            mapping.setValue(DcMapping._B_REFERENCED_ID, dco.getID());
            selected.add(mapping);
        }
        
        saved = true;
        close();
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stReferencesDialogSize, getSize());
        setVisible(false);
    }
    
    @Override
    public void dispose() {
        if (listLeft != null)
            listLeft.clear();
        
        listLeft = null;
        
        if (listRight != null)
            listRight.clear();
        
        listRight = null;
        
        if (elements != null)
            elements.clear();
        
        elements = null;
        
        mappingModule = null;

        if (selected != null)
            selected.clear();
        
        selected = null;
    }    
    
    private void buildDialog() {
        getContentPane().setLayout(Layout.getGBL());
        
        JTextField txtFilter = ComponentFactory.getShortTextField(255);
        txtFilter.addKeyListener(this);

        listLeft = new DcObjectList(DcObjectList._LISTING, false, true);
        listLeft.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listLeft.addMouseListener(new ListMouseListener(ListMouseListener._RIGHT));

        listRight = new DcObjectList(DcObjectList._LISTING, false, true);
        listRight.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listRight.addMouseListener(new ListMouseListener(ListMouseListener._LEFT));
        
        JScrollPane scrollerLeft = new JScrollPane(listLeft);
        scrollerLeft.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollerLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JScrollPane scrollerRight = new JScrollPane(listRight);
        scrollerRight.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollerRight.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        JPanel panelActions = new JPanel();
        panelActions.add(buttonSave);
        panelActions.add(buttonClose);
        
        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        getContentPane().add(txtFilter,     Layout.getGBC( 0, 0, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblAvailable")),  
                Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblSelected")),  
                Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        getContentPane().add(scrollerLeft,  Layout.getGBC( 0, 2, 1, 1, 40.0, 40.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 0, 0, 0), 0, 0));
        getContentPane().add(scrollerRight, Layout.getGBC( 1, 2, 1, 1, 40.0, 40.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 0, 0, 0), 0, 0));
        getContentPane().add(panelActions,  Layout.getGBC( 0, 3, 2, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 0, 5, 0), 0, 0));
    }
    
    private Vector<DcListElement> getUnselectedItems() {
        Vector<DcListElement> unselected = new Vector<DcListElement>();
        for (DcListElement le1 : elements) {
            DcObjectListElement element1 = (DcObjectListElement) le1;
            String displayValue1 = element1.getDcObject().toString();
            
            boolean exists = false;
            for (DcListElement le2 : listRight.getElements()) {
                DcObjectListElement element2 = (DcObjectListElement) le2;
                String displayValue2 = element2.getDcObject().toString();
                if (displayValue1.equals(displayValue2))
                    exists = true;
            }
            
            if (!exists)
                unselected.add(element1);
        }

        return unselected;
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("save"))
            save();
    }
    
    public void keyPressed(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {
        DcShortTextField txtFilter = (DcShortTextField) e.getSource();
        String filter = txtFilter.getText();
        
        if (filter.trim().length() == 0) {
            listLeft.setListData(getUnselectedItems());
        } else {
            Vector<DcListElement> unselected = getUnselectedItems();
            Vector<DcListElement> newElements = new Vector<DcListElement>();
            
            for (DcListElement element : unselected) {
                String displayValue = ((DcObjectListElement) element).getDcObject().toString();
                if (displayValue.toLowerCase().startsWith(filter.toLowerCase()))
                    newElements.add(element);
            }
            listLeft.setListData(newElements);                
        }
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
                    DcObject dco = listRight.getSelectedItem();
                    listLeft.add(dco);
                    listRight.remove();
                    listRight.clearSelection();
                } else {
                    DcObject dco = listLeft.getSelectedItem();
                    listRight.add(dco);
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
}
