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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcButton;
import net.datacrow.console.components.DcComboBox;
import net.datacrow.console.components.DcDateField;
import net.datacrow.console.components.DcObjectComboBox;
import net.datacrow.console.windows.DcFrame;
import net.datacrow.core.DcRepository;
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
import net.datacrow.settings.DcSettings;
import net.datacrow.util.comparators.DcObjectComparator;

public class LoanInformationForm extends DcFrame implements ActionListener {
    
    private LoanInformationPanel panelLoans;
    
	public LoanInformationForm() {
		this(null);
	}
	
    public LoanInformationForm(DcObject person) {
        super(DcResources.getText("lblLoanInformation"), IconLibrary._icoLoan);
        build(person);
        pack();
        
        setSize(DcSettings.getDimension(DcRepository.Settings.stLoanAdminFormSize));
        
        setCenteredLocation();
        setHelpIndex("dc.loans");
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stLoanAdminFormSize, getSize());
        
        if (panelLoans != null)
            panelLoans.clear();

        panelLoans = null;
        super.close();
    }
    
    private void build(DcObject person) {
        JPanel panelFilter = new LoanFilterPanel();
        panelLoans = new LoanInformationPanel(person);
        
    	getContentPane().setLayout(Layout.getGBL());

    	getContentPane().add(panelFilter,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelLoans,        Layout.getGBC( 0, 1, 1, 1, 10.0, 10.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        JButton buttonOk = ComponentFactory.getButton(DcResources.getText("lblOK"));
        buttonOk.setActionCommand("ok");
        buttonOk.addActionListener(this);
        
        JPanel pActions = new JPanel();
        pActions.add(buttonOk);
        
        getContentPane().add(pActions,  Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("ok"))
            close();
    }
    
    private class LoanFilterPanel extends JPanel implements ActionListener {
        
        private static final int _CURRENT_LOANS = 0;
        private static final int _HISTORIC_LOANS = 1;
        private static final int _ALL_LOANS = 2;
        
        private DcObjectComboBox cbPersons = ComponentFactory.getObjectCombo(DcModules._CONTACTPERSON);
        private DcComboBox cbModules = ComponentFactory.getComboBox();
        private DcDateField dtFrom = ComponentFactory.getDateField();
        private DcDateField dtTo = ComponentFactory.getDateField();
        private DcComboBox cbLoans = ComponentFactory.getComboBox(); 
                
        public LoanFilterPanel() {
            build();
        }
        
        protected List<DcObject> getItems() {
            DataFilter df = new DataFilter(DcModules._LOAN);
            
            Calendar cal = Calendar.getInstance();
            
            Date from = (Date) dtFrom.getValue();
            Date to = (Date) dtTo.getValue();
            
            if (from != null) {
                cal.setTime(from);
                cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
                from = cal.getTime();
            }

            if (to != null) {
                cal.setTime(to);
                cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
                to = cal.getTime();
            }
            
            if (from != null)
                df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._E_DUEDATE, Operator.AFTER, from));
            if (to != null && (from == null || to.after(from)))
                df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._E_DUEDATE, Operator.BEFORE, to));

            if (cbLoans.getSelectedIndex() == _CURRENT_LOANS)
                df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._B_ENDDATE, Operator.IS_EMPTY, null));
            else if (cbLoans.getSelectedIndex() == _HISTORIC_LOANS)
                df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._B_ENDDATE, Operator.IS_FILLED, null));
            
            if (cbPersons.getSelectedItem() instanceof DcObject)
                df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._C_CONTACTPERSONID, Operator.EQUAL_TO, ((DcObject) cbPersons.getSelectedItem()).getID()));
            
            List<DcObject> items = new ArrayList<DcObject>();
            DcModule selectedModule = cbModules.getSelectedItem() instanceof DcModule ? (DcModule) cbModules.getSelectedItem() : null;

            DcObject dco;
            for (DcObject loan : DataManager.get(df)) {
                String ID = (String) loan.getValue(Loan._D_OBJECTID);
                if (selectedModule != null) {
                    dco = DataManager.getItem(selectedModule.getIndex(), ID);
                    if (dco != null) {
                        dco.setLoanInformation((Loan) loan);
                        items.add(dco);
                    }
                } else { 
                    for (DcModule module : DcModules.getModules()) {
                        if (module.canBeLend() && !module.isAbstract()) {
                            dco = DataManager.getItem(module.getIndex(), ID);
                            if (dco != null) {
                                dco.setLoanInformation((Loan) loan);
                                items.add(dco);
                            }
                        }
                    }
                }
            }

            Collections.sort(items, new DcObjectComparator(Item._SYS_LOANDUEDATE));
            
            return items;
        }
        
        private void build() {
            DcModule module = DcModules.getCurrent();
            
            cbModules.addItem(" ");
            for (DcModule m : DcModules.getModules()) {
                if (m.canBeLend())
                    cbModules.addItem(m);
            }
            
            cbLoans.addItem(DcResources.getText("lblOnlyCurrentLoans"));
            cbLoans.addItem(DcResources.getText("lblOnlyHistoricalLoans"));
            cbLoans.addItem(DcResources.getText("lblAllLoans"));
            cbLoans.setSelectedIndex(0);
    
            setLayout(Layout.getGBL());
            
            add(ComponentFactory.getLabel(module.getField(DcObject._SYS_LENDBY).getLabel()),  
                    Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5), 0, 0));
            add(cbPersons,   
                    Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets(5, 5, 5, 5), 0, 0));
            
            add(ComponentFactory.getLabel(module.getField(DcObject._SYS_MODULE).getLabel()),  
                    Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5), 0, 0));
            add(cbModules,  
                    Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets(5, 5, 5, 5), 0, 0));             
            
            add(ComponentFactory.getLabel(module.getField(DcObject._SYS_LOANDUEDATE).getLabel() + " - " +
                    DcResources.getText("lblBetween")),  
                    Layout.getGBC( 2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets(5, 50, 5, 5), 0, 0));
            add(dtFrom,  
                    Layout.getGBC( 3, 0, 1, 1, 1.0, 1.0,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets(5, 5, 5, 5), 0, 0));        
            add(ComponentFactory.getLabel(DcResources.getText("lblAnd")),  
                    Layout.getGBC( 2, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets(5, 50, 5, 5), 0, 0));        
            add(dtTo,  
                    Layout.getGBC( 3, 1, 1, 1, 1.0, 1.0,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets(5, 5, 5, 5), 0, 0));
            
            add(ComponentFactory.getLabel(DcResources.getText("lblLoanType")),  
                    Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5), 0, 0));
            add(cbLoans,  
                    Layout.getGBC( 1, 2, 1, 1, 1.0, 1.0,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets(5, 5, 5, 5), 0, 0));             
            
            DcButton btSearch = ComponentFactory.getButton(DcResources.getText("lblSearch"));
            btSearch.addActionListener(this);
            add(btSearch,  
                    Layout.getGBC(3, 3, 1, 1, 1.0, 1.0,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5), 0, 0));             
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            panelLoans.setItems(getItems());
        }
    }
}
