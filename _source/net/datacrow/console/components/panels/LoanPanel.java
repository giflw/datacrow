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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDateField;
import net.datacrow.console.components.DcHtmlEditorPane;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.windows.DcFrame;
import net.datacrow.console.windows.loan.LoanForm;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.objects.helpers.ContactPerson;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.CloseWindowRequest;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class LoanPanel extends JPanel implements ActionListener {

    private static Logger logger = Logger.getLogger(LoanPanel.class.getName());
    
    private DcObject dco;
    
    private List<DcObject> objects;
    private Loan loan;
    private DcFrame owner;
    
    private DcDateField inputEndDate = ComponentFactory.getDateField();    
    private DcDateField inputStartDate = ComponentFactory.getDateField();
    private DcDateField inputDueDate = ComponentFactory.getDateField();
    private JComboBox comboPersons = ComponentFactory.getObjectCombo(DcModules._CONTACTPERSON);
    
    private DcTable tableLoans = ComponentFactory.getDCTable(DcModules.get(DcModules._LOAN), true, false);
    
    private JButton buttonLend = ComponentFactory.getButton(DcResources.getText("lblLendItem"));
    private JButton buttonReturn = ComponentFactory.getButton(DcResources.getText("lblReturnItem"));
    
    private PanelLend panelLend = new PanelLend();
    private PanelReturn panelReturn = new PanelReturn();

    private DcHtmlEditorPane descriptionPane = ComponentFactory.getHtmlEditorPane();
    
    public LoanPanel(DcObject dco, DcFrame owner) {
        objects = new ArrayList<DcObject>();
        objects.add(dco);
        this.dco = dco;
        this.owner = owner;
        
        buildPanel(DataManager.getCurrentLoan(dco.getID()).isAvailable(dco.getID())); 
        setLoanInformation(DataManager.getCurrentLoan(dco.getID()));
    }    
    
    public LoanPanel(Collection<? extends DcObject> objects, DcFrame owner) throws Exception {
        this.objects = new ArrayList<DcObject>(objects);
        this.owner = owner;
        
        int counter = 0;
        Boolean available = null;
        Loan l = (Loan) DcModules.get(DcModules._LOAN).getItem();
        boolean currentStatus;
        for (DcObject o : objects) {
            dco = counter == 0 ? o : dco;
            currentStatus = l.isAvailable(o.getID());
            available = available == null ? currentStatus : available;
            
            if (available.booleanValue() != currentStatus) {
                DcSwingUtilities.displayWarningMessage("msgNotSameState");
                throw new Exception(DcResources.getText("msgNotSameState"));
            }
            
            counter++;
        }
        
        loan = DataManager.getCurrentLoan(dco.getID());
        buildPanel(loan.isAvailable(dco.getID())); 
        setLoanInformation(loan);
    }
    
    private String getPersonLink(String personID) {
        DcObject person = DataManager.getItem(DcModules._CONTACTPERSON, personID);
        return person != null ? descriptionPane.createLink(person, person.toString()) : "";
    }
    
    private String getLoanDescription(Loan loan) {
        String personID = (String) loan.getValue(Loan._C_CONTACTPERSONID);
        boolean available = loan.isAvailable(dco.getID());
        
        String s;
        if (!available) {
            
            String due = "";
            if (loan.getDaysTillOverdue() != null) {
                due = DcResources.getText("msgLoanInformationDue", String.valueOf(loan.getDueDate()));
            }
            
            if (objects.size() == 1) {
                if (loan.getDaysLoaned().intValue() == 0) {
                    s = DcResources.getText("msgLoanInformationToday", 
                            new String[] {dco.toString(), 
                                          getPersonLink(personID),
                                          due});
                } else if (loan.getDaysLoaned().intValue() == 1) {
                    s = DcResources.getText("msgLoanInformationYesterday", 
                            new String[] {dco.toString(), 
                                          getPersonLink(personID),
                                          due});
                } else {
                    s = DcResources.getText("msgLoanInformation", 
                            new String[] {dco.toString(), 
                                          getPersonLink(personID), 
                                          loan.getDaysLoaned().toString(),
                                          due});
                }
            } else {
                s = DcResources.getText("msgAllItemsLoanInformation", String.valueOf(objects.size()));
            }
        } else {
            s = objects.size() == 1 ? DcResources.getText("msgItemIsAvailable") : DcResources.getText("msgAllItemsAreAvailable", String.valueOf(objects.size()));
        }
        
        return s;
    }
    
    private void setLoanInformation(Loan loan) {
        String personID = (String) loan.getValue(Loan._C_CONTACTPERSONID);
        Date start = (Date) loan.getValue(Loan._A_STARTDATE);
        start = start == null ? new Date() : start;
        inputStartDate.setValue(start); 
        
        setDescriptionHtml(getLoanDescription(loan));

        if (personID == null || personID.length() == 0)
            return;
        
        Object o;
        for (int i = 0; i < comboPersons.getItemCount(); i++) {
            o =  comboPersons.getItemAt(i);
            if (o instanceof ContactPerson) {
                ContactPerson person = (ContactPerson) comboPersons.getItemAt(i);
                if (person != null && person.getID().equals(personID)) {
                    comboPersons.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    protected void setDescriptionHtml(String s) {
        descriptionPane.setHtml("<html><body " + Utilities.getHtmlStyle() + ">" + s + "</body></html>");
    }    
    
    public void returnItems() {
        final Date endDate = (Date) inputEndDate.getValue();
        if (endDate == null) {
            DcSwingUtilities.displayWarningMessage("msgEnterReturnDate");
            return;
        }
        
        Loan currentLoan;
        Date startDate;
        for (DcObject o : objects) {
            currentLoan = DataManager.getCurrentLoan(o.getID());
            startDate = (Date) currentLoan.getValue(Loan._A_STARTDATE);
            if (startDate.compareTo(endDate) > 0) {
                DcSwingUtilities.displayWarningMessage("msgEndDateMustBeAfterStartDate");
                return;
            }
        }
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i = 0;
                    Loan currentLoan;
                    for (DcObject o : objects) {
                        currentLoan = DataManager.getCurrentLoan(o.getID());
                        if (currentLoan.getID() != null) {
                            currentLoan.setValue(Loan._D_OBJECTID, o.getID());
                            currentLoan.setValue(Loan._B_ENDDATE, endDate);
                            currentLoan.setValue(Loan._E_DUEDATE, null);
                            
                            if (owner != null && i == objects.size()  - 1) // last item
                                currentLoan.addRequest(new CloseWindowRequest(owner));
                            else if (owner == null)
                                setLoanInformation(currentLoan);
                            
                            dco.setLoanInformation(currentLoan);

                            currentLoan.saveUpdate(false, false);
                            i++;
                        }
                        
                        try {
                            wait(10);
                        } catch (Exception ignore) {}                        
                    }   
                    
                    setLendModus();
                } catch (ValidationException e) {
                    logger.error("Error while saving the loan", e);

                }
             }}
        );
        thread.start();
    }
    
    private void setLendModus() {
        panelLend.setVisible(true);
        panelReturn.setVisible(false);
        
        if (objects.size() == 1) {
            tableLoans.clear();
            DcObject dco = objects.get(0);
            for (Loan loan : DataManager.getLoans(dco.getID())) {
                if (loan.getValue(Loan._B_ENDDATE) != null) 
                    tableLoans.add(loan, true);
            }
        }
    }
    
    private void setReturnModus() {
        panelReturn.setVisible(true);
        panelLend.setVisible(false);
        
        if (objects.size() == 1) {
            tableLoans.clear();
            DcObject dco = objects.get(0);
            for (Loan loan : DataManager.getLoans(dco.getID())) {
                if (loan.getValue(Loan._B_ENDDATE) != null) 
                    tableLoans.add(loan, true);
            }
        }
    }
    
    public void lendItems() {
        ContactPerson contactPerson = comboPersons.getSelectedItem() instanceof ContactPerson ? (ContactPerson) comboPersons.getSelectedItem() : null;
        final String contactPersonID = contactPerson != null ? contactPerson.getID() : null;
        final Date startDate = (Date) inputStartDate.getValue();
        final Date dueDate = (Date) inputDueDate.getValue();
        
        if (contactPersonID == null) {
            DcSwingUtilities.displayWarningMessage("msgSelectPerson");
            return;
        } else if (startDate == null) {
            DcSwingUtilities.displayWarningMessage("msgEnterDate");
            return;
        } else if (dueDate != null && dueDate.before(startDate)) {
            DcSwingUtilities.displayWarningMessage("msgDueDateBeforeStartDate");
            return;
        }
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i = 0;
                    Loan currentLoan;
                    for (DcObject o : objects) {
                        currentLoan = DataManager.getCurrentLoan(o.getID());
                        currentLoan.setValue(Loan._D_OBJECTID, o.getID());
                        currentLoan.setValue(Loan._A_STARTDATE, startDate);
                        currentLoan.setValue(Loan._C_CONTACTPERSONID, contactPersonID);
                        currentLoan.setValue(Loan._E_DUEDATE, dueDate);
                        
                        if (owner != null && i == objects.size() - 1) // last item
                            currentLoan.addRequest(new CloseWindowRequest(owner));
                        else if (owner == null)
                            setLoanInformation(currentLoan);                        
        
                        o.setLoanInformation(currentLoan);
                        
                        if (currentLoan.getID() != null) 
                        	currentLoan.saveUpdate(true);
                        else 
                            currentLoan.saveNew(true);
                        
                        try {
                            wait(100);
                        } catch (Exception ignore) {}
                        
                        i++;
                    }  
                } catch (ValidationException e) {
                    logger.error("Error while saving the loan", e);
                }
            }}
        );
        thread.start();    
        
        setReturnModus();
    }
    
    private void buildPanel(boolean isAvailable) {
        setLayout(Layout.getGBL());

        tableLoans.activate();
        
        //**********************************************************
        //Description panel
        //**********************************************************
        JPanel panelDescription = new JPanel();
        panelDescription.setLayout(Layout.getGBL());
        
        descriptionPane.setEditable(false);
        JScrollPane scroller = new JScrollPane(descriptionPane);
        
        scroller.setPreferredSize(new Dimension(100, 50));
        scroller.setMinimumSize(new Dimension(100, 50));
        scroller.setMaximumSize(new Dimension(800, 50));
        
        panelDescription.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblDescription")));
        
        panelDescription.add(scroller, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                             new Insets(0, 0, 0, 0), 0, 0));
        
        //**********************************************************
        //Action panel
        //**********************************************************
        JPanel panelActions = new JPanel();
 
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        buttonClose.addActionListener(this);
        
        if (owner != null && objects.size() > 1)
            panelActions.add(buttonClose);
        
        //**********************************************************
        //Main
        //**********************************************************
        add( panelDescription,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        add( panelLend,        Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        add( panelReturn,        Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        add( panelActions,      Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        
        if (objects.size() == 1) {
            
            JScrollPane scrollHistory = new JScrollPane(tableLoans);
            scrollHistory.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblLoanHistory")));

            add( scrollHistory,   Layout.getGBC( 0, 3, 1, 1, 4.0, 4.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));                
        }      
        
        if (owner != null && objects.size() == 1) {
            add( buttonClose,   Layout.getGBC( 0, 4, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0)); 
        }
        
        if (isAvailable)
            setLendModus();
        else 
            setReturnModus();
    }
    
    public void close() {
        this.descriptionPane = null;
        this.dco = null;
        this.loan = null;
        this.comboPersons = null;
        this.objects.clear();
        this.objects = null;
        
        if (owner instanceof LoanForm) { 
            ((LoanForm) owner).close();
        } else {
            owner.dispose();
            owner.setVisible(false);
        }
        
        panelLend = null;
        panelReturn = null;
        
        buttonLend = null;
        buttonReturn = null;
        
        inputEndDate = null;
        inputDueDate = null;
        inputStartDate = null;
        
        this.tableLoans.clear();
        this.tableLoans = null;
        
        this.owner = null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        close();
    }
    
    private class PanelLend extends JPanel implements ActionListener {
        
        public PanelLend() {
            setLayout(Layout.getGBL());
            
            JLabel labelStartDate = ComponentFactory.getLabel(DcResources.getText("lblStartDate"), IconLibrary._icoCalendar);
            JLabel labelDueDate = ComponentFactory.getLabel(DcResources.getText("lblDueDate"), IconLibrary._icoCalendar);
            JLabel labelPerson = ComponentFactory.getLabel(DcResources.getText("lblContactPerson"), IconLibrary._icoPersons);
            
            add(labelStartDate , Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
            add(inputStartDate , Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
            
            add(labelDueDate , Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
            add(inputDueDate , Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));            
            
            add(labelPerson ,    Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
            add(comboPersons ,   Layout.getGBC( 1, 2, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
            
            JPanel panelActions = new JPanel();
            buttonLend.addActionListener(this);
            panelActions.add(buttonLend);
            
            add(panelActions,    Layout.getGBC( 1, 3, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                     new Insets(5, 5, 5,  5), 0, 0));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            lendItems();
        }
    }
    
    private class PanelReturn extends JPanel implements ActionListener {
        
        public PanelReturn() {
            setLayout(Layout.getGBL());
            
            JLabel labelEndDate = ComponentFactory.getLabel(DcResources.getText("lblEndDate"), IconLibrary._icoCalendar);
            
            add(labelEndDate , Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
            add(inputEndDate , Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
            
            buttonReturn.addActionListener(this);
            
            JPanel panelActions = new JPanel();
            panelActions.add(buttonReturn);
            
            add(panelActions , Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                     new Insets(5, 5, 5,  5), 0, 0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            returnItems();
        }
    }
}
