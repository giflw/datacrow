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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.views.ISimpleItemView;
import net.datacrow.console.windows.itemforms.DcMinimalisticItemForm;
import net.datacrow.core.DataCrow;
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

public class LoanInformationDialog extends DcDialog implements ActionListener, ISimpleItemView, MouseListener {
    
    private DcTable table = new DcTable(DcModules.get(DcModules._ITEM), true, false);
    private DcObject person;
    
    
    public LoanInformationDialog(DcObject person) {
        super(DataCrow.mainFrame);
        
        this.person = person;

        setTitle(DcResources.getText("lblLoanInformation"));
        
        build();
        loadItems();
        pack();
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
        DataFilter df = new DataFilter(DcModules._LOAN);
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._B_ENDDATE, Operator.IS_EMPTY, null));
        
        if (person != null)
            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._C_CONTACTPERSONID, Operator.EQUAL_TO, person.getID()));
        
        List<DcObject> items = new ArrayList<DcObject>();
        for (DcObject loan : DataManager.get(DcModules._LOAN, df)) {
            String ID = (String) loan.getValue(Loan._D_OBJECTID);
            for (DcModule module : DcModules.getModules()) {
                if (module.canBeLended()) {
                    DcObject dco = DataManager.getObject(module.getIndex(), ID);
                    if (dco != null)
                        items.add(dco);
                }
            }
        }

        Collections.sort(items, new DcObjectComparator(Item._SYS_LOANDAYSTILLOVERDUE));
        
        for (DcObject dco : items)
            table.add(dco);
    }
    
    @Override
    public void close() {
        if (table != null)
            table.clear();
        
        table = null;
        super.close();
    }
    
    private void build() {
        table.addMouseListener(this);
        
        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        getContentPane().setLayout(Layout.getGBL());
        
        getContentPane().add(sp,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        
        if (person == null)
            table.setVisibleColumns(new int[] {Item._SYS_MODULE, Item._SYS_DISPLAYVALUE, Item._SYS_LENDBY, Item._SYS_LOANDUEDATE, Item._SYS_LOANDAYSTILLOVERDUE});
        else 
            table.setVisibleColumns(new int[] {Item._SYS_MODULE, Item._SYS_DISPLAYVALUE, Item._SYS_LOANDUEDATE, Item._SYS_LOANDAYSTILLOVERDUE});
        
        JButton buttonOk = ComponentFactory.getButton(DcResources.getText("lblOK"));
        buttonOk.setActionCommand("ok");
        buttonOk.addActionListener(this);
        
        JPanel pActions = new JPanel();
        pActions.add(buttonOk);
        
        getContentPane().add(pActions,  Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("ok"))
            close();
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
