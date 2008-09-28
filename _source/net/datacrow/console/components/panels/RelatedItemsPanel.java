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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

import net.datacrow.console.Layout;
import net.datacrow.console.components.DcPanel;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.views.ISimpleItemView;
import net.datacrow.console.windows.itemforms.DcMinimalisticItemForm;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.MappingModule;
import net.datacrow.core.modules.TemplateModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcObjectComparator;

public class RelatedItemsPanel extends DcPanel implements MouseListener, ISimpleItemView {
    
    private DcObjectList list = new DcObjectList(DcObjectList._LISTING, false, true);
    private DcObject dco;
    
    public RelatedItemsPanel(DcObject dco) {
        this.dco = dco;

        setIcon(IconLibrary._icoInformation);
        setTitle(DcResources.getText("lblRelatedItems"));

        build();
        loadItems();
    }
    
    private void build() {
        list.addMouseListener(this);
        
        JScrollPane scroller = new JScrollPane(list);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);        
        
        setLayout(Layout.getGBL());
        add(scroller,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
           ,GridBagConstraints.NORTH, GridBagConstraints.BOTH,
            new Insets(5, 5, 5, 5), 0, 0));
    }
    
    public void open() {
        DcObject dco = list.getSelectedItem();
        if (dco != null) {
            dco.markAsUnchanged();
            DcMinimalisticItemForm itemForm = new DcMinimalisticItemForm(false, true, dco, this);
            itemForm.setVisible(true);
        }
    }
    
    public void loadItems() {
        list.clear();
        
        List<DcObject> items = new ArrayList<DcObject>();
        for (DcModule module : DcModules.getActualReferencingModules(dco.getModule().getIndex())) {
            if ( module.getIndex() != dco.getModule().getIndex() && 
               !(module instanceof TemplateModule)) {
            	
                for (DcField field : module.getFields()) {
                    if (field.getReferenceIdx() == dco.getModule().getIndex()) {
                        DataFilter df = new DataFilter(module.getIndex());
                        
                        if (module instanceof MappingModule) {
                            Collection<DcObject> c = new ArrayList<DcObject>();
                            c.add(dco);
                            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, module.getIndex(), field.getIndex(), Operator.CONTAINS, c));
                        } else {
                            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, module.getIndex(), field.getIndex(), Operator.EQUAL_TO, dco));
                        }
                        
                    	for (DcObject dco : DataManager.get(DcModules._ITEM, df)) {
                    		if (!items.contains(dco))
                    			items.add(dco);
                    	}
                    }
                }
            }
        }

        Collections.sort(items, new DcObjectComparator(DcObject._SYS_DISPLAYVALUE));
        list.add(items);
    }
    
    public void mouseReleased(MouseEvent e) {
        DcObjectList list = (DcObjectList) e.getSource();
        if (e.getClickCount() == 2 && list.getSelectedIndex() > -1) 
            open();
    }
        
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}    
}
