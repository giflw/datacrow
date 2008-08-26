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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDateField;
import net.datacrow.console.components.DcFrame;
import net.datacrow.console.components.DcTextPane;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.windows.LoanForm;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.objects.helpers.ContactPerson;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.CloseWindowRequest;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class LoanPanel extends JPanel {

    private static Logger logger = Logger.getLogger(LoanPanel.class.getName());
    
    private DcObject dco;
    
    private Collection<DcObject> objects;
    private Loan loan;
    private DcFrame owner;
    
    private DcDateField inputEndDate = ComponentFactory.getDateField();    
    private DcDateField inputStartDate = ComponentFactory.getDateField();
    private JComboBox comboPersons = ComponentFactory.getContactPersonCombo();
    private DcTextPane descriptionPane;
    
    private HTMLEditorKit kit = new HTMLEditorKit();
    private HTMLDocument document = new HTMLDocument();
    
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
        Loan l = new Loan();
        for (DcObject o : objects) {
            dco = counter == 0 ? o : dco;
            boolean currentStatus = l.isAvailable(o.getID());
            available = available == null ? currentStatus : available;
            
            if (available.booleanValue() != currentStatus) {
                new MessageBox(DcResources.getText("msgNotSameState"), MessageBox._WARNING);
                throw new Exception(DcResources.getText("msgNotSameState"));
            }
            
            counter++;
        }
        
        loan = DataManager.getCurrentLoan(dco.getID());
        buildPanel(loan.isAvailable(dco.getID())); 
        setLoanInformation(loan);
    }
    
    private void setLoanInformation(Loan loan) {
        Date start = (Date) loan.getValue(Loan._A_STARTDATE);
        start = start == null ? new Date() : start;
        inputStartDate.setValue(start); 
        
        String personID = (String) loan.getValue(Loan._C_CONTACTPERSONID);

        if (!loan.isAvailable(dco.getID())) { 
            if (objects.size() == 1) {
                String s;
                if (loan.getDaysLoaned().intValue() == 0)
                    s = DcResources.getText("msgLoanInformationToday", new String[] {dco.toString(), 
                            ((ContactPerson) DataManager.getObject(DcModules._CONTACTPERSON, personID)).toString()});
                else if (loan.getDaysLoaned().intValue() == 1)
                    s = DcResources.getText("msgLoanInformationYesterday", new String[] {dco.toString(),
                            ((ContactPerson) DataManager.getObject(DcModules._CONTACTPERSON, personID)).toString()});
                else
                    s = DcResources.getText("msgLoanInformation", new String[] {dco.toString(), 
                            ((ContactPerson) DataManager.getObject(DcModules._CONTACTPERSON, personID)).toString(), 
                            loan.getDaysLoaned().toString()});
                
                setDescriptionHtml(s);
            } else {
                setDescriptionHtml(DcResources.getText("msgAllItemsLoanInformation", "" + objects.size()));
            }
        } else {
            if (objects.size() == 1)
                setDescriptionHtml(DcResources.getText("msgItemIsAvailable"));
            else 
                setDescriptionHtml(DcResources.getText("msgAllItemsAreAvailable", "" + objects.size()));
        }
        
        if (personID != null && personID.length() > 0) {
            for (int i = 0; i < comboPersons.getItemCount(); i++) {
                Object o =  comboPersons.getItemAt(i);
                if (o instanceof ContactPerson) {
                    ContactPerson person = (ContactPerson) comboPersons.getItemAt(i);
                    if (person != null && person.getID().equals(personID)) {
                        comboPersons.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }
    
    protected void setDescriptionHtml(String s) {
        try {
            String html = "<html><body " + Utilities.getHtmlStyle() + ">" + s + "</body></html>";
            descriptionPane.setText("");
            
            try {
                kit.insertHTML(document, 0, html, 0, 0, null);
            } catch (Exception exp) {
                // not a nice workaround..
                kit.insertHTML(document, 0, html, 0, 0, null);
            }
        } catch (Exception e) {
            logger.error("Could not set the HTML desription", e);
        }
    }    
    
    public void returnItems() {
        final Date endDate = (Date) inputEndDate.getValue();
        if (endDate == null) {
            new MessageBox(DcResources.getText("msgEnterReturnDate"), MessageBox._WARNING);
            return;
        }
        
        for (DcObject o : objects) {
            Loan currentLoan = DataManager.getCurrentLoan(o.getID());
            Date startDate = (Date) currentLoan.getValue(Loan._A_STARTDATE);
            if (startDate.compareTo(endDate) > 0) {
                new MessageBox(DcResources.getText("msgEndDateMustBeAfterStartDate"), MessageBox._WARNING);
                return;
            }
        }
        
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    int i = 0;
                    for (DcObject o : objects) {
                        Loan currentLoan = DataManager.getCurrentLoan(o.getID());
                        if (currentLoan.getID() != null) {
                            currentLoan.setValue(Loan._D_OBJECTID, o.getID());
                            currentLoan.setValue(Loan._B_ENDDATE, endDate);
                
                            currentLoan.setPartOfBatch(i == 0);
                            currentLoan.setEndOfBatch(i == objects.size()  - 1);
                            currentLoan.setSilent(true);
                            
                            if (owner != null && currentLoan.isEndOfBatch()) 
                                currentLoan.addRequest(new CloseWindowRequest(owner));
                            else if (owner == null)
                                setLoanInformation(currentLoan);
                            
                            o.setValue(DcObject._SYS_AVAILABLE, currentLoan.isAvailable(o.getID()));
                            o.setValue(DcObject._SYS_LOANEDBY, currentLoan.getPersonDescription());
                            o.setValue(DcObject._SYS_LOANDURATION, currentLoan.getDaysLoaned());                

                            currentLoan.saveUpdate(true);
                            i++;
                        }
                        
                        try {
                            wait(100);
                        } catch (Exception ignore) {}                        
                    }        
                } catch (ValidationException e) {
                    logger.error("Error while saving the loan", e);

                }
             }}
        );
        thread.start();
        
        removeAll();
        buildPanel(true);
        revalidate();    
        repaint();
    }

    
    public void lendItems() {
        ContactPerson contactPerson = comboPersons.getSelectedItem() instanceof ContactPerson ? (ContactPerson) comboPersons.getSelectedItem() : null;
        final String contactPersonID = contactPerson != null ? contactPerson.getID() : null;
        final Date startDate = (Date) inputStartDate.getValue();
        
        if (contactPersonID == null) {
            new MessageBox(DcResources.getText("msgSelectPerson"), MessageBox._WARNING);
            return;
        } else if (startDate == null) {
            new MessageBox(DcResources.getText("msgEnterDate"), MessageBox._WARNING);
            return;
        }
        
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    int i = 0;
                    for (DcObject o : objects) {
                        Loan currentLoan = DataManager.getCurrentLoan(o.getID());
                        currentLoan.setValue(Loan._D_OBJECTID, o.getID());
                        currentLoan.setValue(Loan._A_STARTDATE, startDate);
                        currentLoan.setValue(Loan._C_CONTACTPERSONID, contactPersonID);
                        currentLoan.setSilent(true);
            
                        currentLoan.setPartOfBatch(i == 0);
                        currentLoan.setEndOfBatch(i == objects.size() - 1);
                        
                        if (owner != null && currentLoan.isEndOfBatch())
                            currentLoan.addRequest(new CloseWindowRequest(owner));
                        else if (owner == null)
                            setLoanInformation(currentLoan);                        
        
                        o.setValue(DcObject._SYS_AVAILABLE, currentLoan.isAvailable(o.getID()));
                        o.setValue(DcObject._SYS_LOANEDBY, currentLoan.getPersonDescription());
                        o.setValue(DcObject._SYS_LOANDURATION, currentLoan.getDaysLoaned());                
                        
                        if (currentLoan.getID() != null && currentLoan.getID().length() > 0) 
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
        
        removeAll();
        buildPanel(false);
        repaint();
        revalidate();
    }
    
    private void buildPanel(boolean isAvailable) {
        setLayout(Layout.getGBL());

        //**********************************************************
        //Input panel
        //**********************************************************
        JPanel panelInput = new JPanel();
        panelInput.setLayout(Layout.getGBL());
        
        JLabel labelEndDate = ComponentFactory.getLabel(DcResources.getText("lblEndDate"));
        JLabel labelStartDate = ComponentFactory.getLabel(DcResources.getText("lblStartDate"));
        JLabel labelPerson = ComponentFactory.getLabel(DcResources.getText("lblContactPerson"));
        
        if (isAvailable) {
            labelStartDate.setIcon(IconLibrary._icoCalendar);
            labelPerson.setIcon(IconLibrary._icoPersons);
            
            panelInput.add(labelStartDate , Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
            panelInput.add(inputStartDate , Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
            panelInput.add(labelPerson ,    Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
            panelInput.add(comboPersons ,   Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
        } else {
            labelEndDate.setIcon(IconLibrary._icoCalendar);
            
            panelInput.add(labelEndDate , Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
            panelInput.add(inputEndDate , Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5,  5), 0, 0));
        }
            
        //**********************************************************
        //Description panel
        //**********************************************************
        JPanel panelDescription = new JPanel();
        panelDescription.setLayout(Layout.getGBL());
        
        descriptionPane = ComponentFactory.getTextPane();
        descriptionPane.setEditorKit(kit);
        descriptionPane.setDocument(document);
        descriptionPane.setEditable(false);
        descriptionPane.setBounds(1,1,1,1);
        
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
        buttonClose.addActionListener(new CloseButtonAction());
        buttonClose.setMnemonic('C');

        if (isAvailable) {
            JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblLendItem"));
            buttonSave.addActionListener(new SaveButtonAction());
            panelActions.add(buttonSave);
        } else {
            JButton buttonReturn = ComponentFactory.getButton(DcResources.getText("lblReturnItem"));
            buttonReturn.addActionListener(new ReturnItemButtonAction());
            panelActions.add(buttonReturn);
        }
        
        if (owner != null && objects.size() > 1)
            panelActions.add(buttonClose);
        
        //**********************************************************
        //Main
        //**********************************************************
        
        add( panelDescription,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        add( panelInput,        Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        add( panelActions,      Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        
        if (objects.size() == 1) {
            DcObject dco = (DcObject) objects.toArray()[0];
            
            DcTable tableLoans = ComponentFactory.getDCTable(DcModules.get(DcModules._LOAN), true, false);
            JScrollPane scrollHistory = new JScrollPane(tableLoans);
            scrollHistory.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblLoanHistory")));
            
            for (Loan loan : DataManager.getLoans(dco.getID())) {
                if (loan.getValue(Loan._B_ENDDATE) != null) 
                    tableLoans.add(loan, true);
            }
            
            add( scrollHistory,   Layout.getGBC( 0, 3, 1, 1, 4.0, 4.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));                
        }      
        
        if (owner != null && objects.size() == 1) {
            add( buttonClose,   Layout.getGBC( 0, 4, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0)); 
        }
    }
    
    public class CloseButtonAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            close();
        }
    } 

    public class ReturnItemButtonAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            returnItems();
        }
    }    
    
    public class SaveButtonAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            lendItems();
        }
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
        
        this.owner = null;
        this.kit = null;
        this.document = null;
    }
}
