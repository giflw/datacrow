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
import java.util.Collection;

import javax.swing.JScrollPane;

import net.datacrow.console.Layout;
import net.datacrow.console.components.DcPanel;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.views.ISimpleItemView;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.objects.helpers.Item;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class LoanInformationPanel extends DcPanel implements ISimpleItemView, MouseListener {
	
    private static Logger logger = Logger.getLogger(LoanInformationPanel.class.getName());
    
    private DcTable table = new DcTable(DcModules.get(DcModules._ITEM), true, false);
    private DcObject person;
    
    private LoanFilter filter;
    
    public LoanInformationPanel() {
    	this(null);
    }
    
    public LoanInformationPanel(DcObject person) {
        super();

        setTitle(DcResources.getText("lblLoanInformation"));
        setIcon(IconLibrary._icoLoan);

        this.person = person;
        
        build();
        load();
    }
    
    public void setFilter(LoanFilter filter) {
        this.filter = filter;
    }
    
    public void open() {
        DcObject dco = table.getSelectedItem();
        if (dco != null) {
            dco.load(null);
            
            Loan loan = DataManager.getCurrentLoan(dco.getID());
            if (loan == null || loan.isAvailable(dco.getID())) {
                return;
            } else {
                Collection<DcObject> items = new ArrayList<DcObject>();
                items.add(dco);
                
                try {
                    LoanForm form = new LoanForm(items);
                    form.setListener(this);
                    form.setVisible(true);
                } catch (Exception exp) {
                    logger.warn(exp, exp);
                }
            }
        }
    }
    
    @Override
    public void load() {
        if (filter == null)
            filter = new LoanFilter(); 
        
        table.clear();
        for (DcObject dco : filter.getItems())
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
        table.activate();
        table.addMouseListener(this);
        table.setDynamicLoading(false);

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        setLayout(Layout.getGBL());
        
        add(sp,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));
        
        if (person == null)
            table.setVisibleColumns(new int[] {Item._SYS_MODULE, Item._SYS_DISPLAYVALUE, Item._SYS_LENDBY, Item._SYS_LOANSTARTDATE, Item._SYS_LOANENDDATE, Item._SYS_LOANDUEDATE, Item._SYS_LOANSTATUS, Item._SYS_LOANSTATUSDAYS});
        else 
            table.setVisibleColumns(new int[] {Item._SYS_MODULE, Item._SYS_DISPLAYVALUE, Item._SYS_LOANSTARTDATE, Item._SYS_LOANENDDATE, Item._SYS_LOANDUEDATE, Item._SYS_LOANSTATUS, Item._SYS_LOANSTATUSDAYS});
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        DcTable table = (DcTable) e.getSource();
        if (e.getClickCount() == 2 && table.getSelectedIndex() > -1) 
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
}
