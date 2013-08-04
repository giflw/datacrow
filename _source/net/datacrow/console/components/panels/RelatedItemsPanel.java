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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcPanel;
import net.datacrow.console.components.DcShortTextField;
import net.datacrow.console.components.lists.DcListModel;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.components.lists.elements.DcListElement;
import net.datacrow.console.components.lists.elements.DcObjectListElement;
import net.datacrow.console.views.ISimpleItemView;
import net.datacrow.console.windows.itemforms.DcMinimalisticItemForm;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.IRequest;
import net.datacrow.core.wf.requests.RefreshSimpleViewRequest;

public class RelatedItemsPanel extends DcPanel implements MouseListener, ISimpleItemView, ActionListener, KeyListener {
    
    private DcObjectList list = new DcObjectList(DcObjectList._LISTING, false, true);
    private List<DcListElement> all = new ArrayList<DcListElement>();
    
    private boolean stopped = true;
    private DcObject dco;
    
    public RelatedItemsPanel(DcObject dco) {
        this.dco = dco;

        setIcon(IconLibrary._icoRelations);
        setTitle(DcResources.getText("lblRelatedItems"));

        build();
        load();
    }
    
    private void open() {
        DcObject dco = list.getSelectedItem();
        if (dco != null) {
            dco.markAsUnchanged();
            DcMinimalisticItemForm itemForm = new DcMinimalisticItemForm(false, true, dco, this);
            itemForm.setVisible(true);
        }
    }
    
    public IRequest getAfterDeleteRequest() {
        return new RefreshSimpleViewRequest(this);
    }
    
//    private void delete() {
//        Collection<DcObject> objects = list.getSelectedItems();
//        if (objects.size() > 0) {
//            if (!DcSwingUtilities.displayQuestion("msgDeleteQuestion")) 
//                return;
//            
//            task = new DeleteItemsTask(this, objects);
//            task.addRequest(getAfterDeleteRequest());
//            task.start();
//        } else {
//            DcSwingUtilities.displayWarningMessage("msgSelectItemToDel");
//        }
//    }
    
    public void open(boolean edit) {
        DcObject dco = list.getSelectedItem();
        if (dco != null) {
            dco.markAsUnchanged();
            DcMinimalisticItemForm itemForm = new DcMinimalisticItemForm(!edit, true, dco, this);
            itemForm.setVisible(true);
        }
    }
    
    @Override
    public void load() {
        SwingUtilities.invokeLater(
                new Thread(new Runnable() { 
                    @Override
                    public void run() {
                        list.clear();
                        all.clear();
                        
                        List<DcObject> items = DataManager.getReferencingItems(dco);
                        Collections.sort(items);
                        list.add(items);
                        all.addAll(list.getElements());
                    }
                }));
    }
    
    
    
//    @Override
//    public void notifyTaskSize(int size) {
//        initProgressBar(size);
//    }
//
//    @Override
//    public void notify(String msg) {}
//
//    @Override
//    public void notifyTaskStopped() {}
//
//    @Override
//    public void notifyTaskStarted() {}
//
//    @Override
//    public void notifyProcessed() {
//        updateProgressBar();
//    }
//
//    @Override
//    public boolean isStopped() {
//        return false;
//    }
    
    
    @Override
    public void clear() {
        if (all != null) all.clear();
        if (list != null) list.clear();
        
        all = null;
        list = null;
        stopped = true;
        
        super.clear();
    }

    private void build() {
        list.addMouseListener(this);
        
        JScrollPane scroller = new JScrollPane(list);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroller.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        setLayout(Layout.getGBL());
        
        JTextField txtFilter = ComponentFactory.getShortTextField(255);
        txtFilter.addKeyListener(this);        

        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());
        panel.add(ComponentFactory.getLabel(DcResources.getText("lblFilter")), Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        panel.add(txtFilter, Layout.getGBC( 1, 0, 1, 1, 100.0, 100.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        
        add(panel, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 10, 5, 0, 5), 0, 0));
        add(scroller,  Layout.getGBC( 0, 2, 1, 1, 100.0, 100.0
                ,GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        add(getProgressPanel(), Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        DcObjectList list = (DcObjectList) e.getSource();
        if (e.getClickCount() == 2 && list.getSelectedIndex() > -1) 
            open();
    }
        
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("open_readonly"))
            open(false);        
        else if (e.getActionCommand().equals("open_edit"))
            open(true);
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        DcShortTextField txtFilter = (DcShortTextField) e.getSource();
        String filter = txtFilter.getText();
        
        if (filter.trim().length() == 0) {
            ((DcListModel) list.getModel()).clear();
            list.addElements(all);
        } else {
            List<DcListElement> filtered = new ArrayList<DcListElement>();
            for (DcListElement el : all) {
                DcObjectListElement element = (DcObjectListElement) el;
                if (element.getDcObject().toString().toLowerCase().contains(filter.toLowerCase()))
                    filtered.add(el);
            }
        
            // DO NOT USE LIST.CLEAR() - will cleared the stored elements and force a reload of the items.
            ((DcListModel) list.getModel()).clear();
            list.addElements(filtered);
        }
    }  
    
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}
}
