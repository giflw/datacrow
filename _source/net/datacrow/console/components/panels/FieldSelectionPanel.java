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

package net.datacrow.console.components.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

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
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcField;

public class FieldSelectionPanel extends JPanel implements KeyListener {
    
    private Vector<DcListElement> elements;
    private DcFieldList listRight;
    private DcFieldList listLeft;
    
    private DcModule module;
    
    public FieldSelectionPanel(DcModule module) {
        this.module = module;

        build();

        for (DcField field : module.getFields()) {
            if (    field.isEnabled() && 
                    field.getValueType() != DcRepository.ValueTypes._PICTURE &&
                    field.getValueType() != DcRepository.ValueTypes._ICON)
                
                listLeft.add(field);
        }

        elements = new Vector<DcListElement>();
        elements.addAll(listLeft.getElements());
    }

    public DcField[] getSelectedFields() {
        return listRight.getFields().toArray(new DcField[0]);
    }
    
    public void setSelectedFields(DcField[] fields) {
        reset();
        for (int i = 0; i < fields.length; i++) {
            listLeft.remove(fields[i]);
            listRight.add(fields[i]);
        }
    }    
    
    public void setSelectedFields(String[] fields) {
        reset();
        for (int i = 0; i < fields.length; i++) {
            listLeft.remove(module.getField(fields[i]));
            listRight.add(module.getField(fields[i]));
        }
    }
    
    public void setSelectedFields(int[] fields) {
        reset();
        for (int i = 0; i < fields.length; i++) {
            listLeft.remove(module.getField(fields[i]));
            listRight.add(module.getField(fields[i]));
        }
    }
    
    private void reset() {
        listRight.setListData(new Vector<DcListElement>());
    }
    
    public void clear() {
        if (listLeft != null)
            listLeft.clear();
        
        listLeft = null;
        
        if (listRight != null)
            listRight.clear();
        
        listRight = null;
        
        if (elements != null)
            elements.clear();
        
        elements = null;
        module = null;
    }    
    
    private void build() {
        setLayout(Layout.getGBL());
        
        JTextField txtFilter = ComponentFactory.getShortTextField(255);
        txtFilter.addKeyListener(this);

        listLeft = new DcFieldList();
        listLeft.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listLeft.addMouseListener(new ListMouseListener(ListMouseListener._RIGHT));

        listRight = new DcFieldList();
        listRight.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listRight.addMouseListener(new ListMouseListener(ListMouseListener._LEFT));
        
        JScrollPane scrollerLeft = new JScrollPane(listLeft);
        scrollerLeft.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollerLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JScrollPane scrollerRight = new JScrollPane(listRight);
        scrollerRight.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollerRight.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(txtFilter,     Layout.getGBC( 0, 0, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(scrollerLeft,  Layout.getGBC( 0, 1, 1, 1, 20.0, 20.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(scrollerRight, Layout.getGBC( 1, 1, 1, 1, 20.0, 20.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 0, 0, 0), 0, 0));
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
                    DcField field = listRight.getSelected();
                    listLeft.add(field);
                    listRight.remove();
                    listRight.clearSelection();
                } else {
                    DcField field =  listLeft.getSelected();
                    listRight.add(field);
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
