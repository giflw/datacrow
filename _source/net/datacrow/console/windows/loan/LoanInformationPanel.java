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

package net.datacrow.console.windows.loan;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JScrollPane;

import net.datacrow.console.Layout;
import net.datacrow.console.components.DcPanel;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.views.ISimpleItemView;
import net.datacrow.console.windows.itemforms.DcMinimalisticItemForm;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.objects.helpers.Item;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcObjectComparator;

public class LoanInformationPanel extends DcPanel implements ISimpleItemView, MouseListener {
	
    private DcTable table = new DcTable(DcModules.get(DcModules._ITEM), true, false);
    private DcObject person;
    
    public LoanInformationPanel() {
    	this(null);
    }
    
    public LoanInformationPanel(DcObject person) {
        super();

        setTitle(DcResources.getText("lblLoanInformation"));
        setIcon(IconLibrary._icoLoan);

        this.person = person;
        
        build();
        loadItems();
    }
    
    public void open() {
        DcObject dco = table.getSelectedItem();
        if (dco != null) {
            dco.reload();
            dco.markAsUnchanged();
            DcMinimalisticItemForm itemForm = new DcMinimalisticItemForm(false, true, dco, this);
            itemForm.setVisible(true);
        }
    }
    
    public void loadItems() {
    	table.clear();
    	
        DataFilter df = new DataFilter(DcModules._LOAN);
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._B_ENDDATE, Operator.IS_EMPTY, null));
        
        if (person != null)
            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._C_CONTACTPERSONID, Operator.EQUAL_TO, person.getID()));
        
        List<DcObject> items = new ArrayList<DcObject>();
        for (DcObject loan : DataManager.get(DcModules._LOAN, df)) {
            String ID = (String) loan.getValue(Loan._D_OBJECTID);
            for (DcModule module : DcModules.getModules()) {
                if (module.canBeLended() && !module.isAbstract()) {
                    DcObject dco = DataManager.getObject(module.getIndex(), ID);
                    if (dco != null && !items.contains(dco))
                        items.add(dco);
                }
            }
        }

        Collections.sort(items, new DcObjectComparator(Item._SYS_LOANDAYSTILLOVERDUE));
        
        for (DcObject dco : items)
            table.add(dco);
    }
    
    @Override
    public void clear() {
        if (table != null)
            table.clear();
        
        person = null;
        table = null;
        super.clear();
    }
    
    private void build() {
        table.addMouseListener(this);
        
        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        setLayout(Layout.getGBL());
        
        add(sp,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));
        
        if (person == null)
            table.setVisibleColumns(new int[] {Item._SYS_MODULE, Item._SYS_DISPLAYVALUE, Item._SYS_LENDBY, Item._SYS_LOANDUEDATE, Item._SYS_LOANDAYSTILLOVERDUE});
        else 
            table.setVisibleColumns(new int[] {Item._SYS_MODULE, Item._SYS_DISPLAYVALUE, Item._SYS_LOANDUEDATE, Item._SYS_LOANDAYSTILLOVERDUE});
    }

    public void mouseReleased(MouseEvent e) {
        DcTable table = (DcTable) e.getSource();
        if (e.getClickCount() == 2 && table.getSelectedIndex() > -1) 
            open();
    }
        
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}        
}
