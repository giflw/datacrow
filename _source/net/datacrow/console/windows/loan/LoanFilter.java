package net.datacrow.console.windows.loan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.SwingUtilities;

import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.objects.helpers.Item;
import net.datacrow.util.comparators.DcObjectComparator;

import org.apache.log4j.Logger;

public class LoanFilter extends Thread {
    
    private static Logger logger = Logger.getLogger(LoanFilter.class.getName());

    public static final int _CURRENT_LOANS = 0;
    public static final int _HISTORIC_LOANS = 1;
    public static final int _ALL_LOANS = 2;
    
    private Date startDateFrom;
    private Date startDateTo;

    private Date dueDateFrom;
    private Date dueDateTo;
    
    private boolean onlyOverdue = false;
    
    private LoanInformationPanel listener;
    
    public LoanFilter() {}
    
    public void setListener(LoanInformationPanel listener) {
        this.listener = listener;
    }
    
    public void setOnlyOverdue(boolean onlyOverdue) {
        this.onlyOverdue = onlyOverdue;
    }

    private int loanType = _CURRENT_LOANS;
    private DcObject person;
    
    private DcModule selectedModule;

    public void setStartDateFrom(Date startDateFrom) {
        this.startDateFrom = startDateFrom;
    }

    public void setStartDateTo(Date startDateTo) {
        this.startDateTo = startDateTo;
    }

    public Date getDueDateFrom() {
        return dueDateFrom;
    }

    public void setDueDateFrom(Date dueDateFrom) {
        this.dueDateFrom = dueDateFrom;
    }

    public Date getDueDateTo() {
        return dueDateTo;
    }

    public void setDueDateTo(Date dueDateTo) {
        this.dueDateTo = dueDateTo;
    }

    public void setPerson(DcObject person) {
        this.person = person;
    }

    public void setModule(DcModule module) {
        this.selectedModule = module;
    }

    public void setLoanType(int loanType) {
        this.loanType = loanType;
    }
    
    @Override
    public void run() {
        
        DataFilter df = new DataFilter(DcModules._LOAN);
        
        if (dueDateFrom != null)
            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._E_DUEDATE, Operator.AFTER, dueDateFrom));
        if (dueDateTo != null && (dueDateFrom == null || dueDateTo.after(dueDateFrom)))
            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._E_DUEDATE, Operator.BEFORE, dueDateTo));

        if (startDateFrom != null)
            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._A_STARTDATE, Operator.AFTER, startDateFrom));
        if (startDateTo != null && (startDateFrom == null || startDateTo.after(startDateFrom)))
            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._A_STARTDATE, Operator.BEFORE, startDateTo));
        
        if (loanType == _CURRENT_LOANS)
            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._B_ENDDATE, Operator.IS_EMPTY, null));
        else if (loanType == _HISTORIC_LOANS)
            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._B_ENDDATE, Operator.IS_FILLED, null));
        
        if (person != null)
            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._C_CONTACTPERSONID, Operator.EQUAL_TO, person));
        
        List<DcObject> items = new ArrayList<DcObject>();
        
        DcObject dco;
        Date endDate;
        Date dueDate;
        String ID;
        Collection<DcObject> loans = DataManager.get(df);
        listener.setMaximum(loans.size());
        int processed = 0;
        for (DcObject loan : loans) {
            
            if (onlyOverdue) {
                endDate = (Date) loan.getValue(Loan._B_ENDDATE);
                dueDate = (Date) loan.getValue(Loan._E_DUEDATE);
                
                if (dueDate == null)
                    continue;
                else if (endDate != null && !endDate.after(dueDate))
                    continue;
                else if (endDate == null && !((Loan) loan).isOverdue())
                    continue;
            }
            
            
            ID = (String) loan.getValue(Loan._D_OBJECTID);
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
                        
                        try {
                            sleep(10);
                        } catch (Exception e) {}
                    }
                }
            }
            
            listener.setProcessed(processed++);
        }
        
        listener.setProcessed(loans.size());

        if (loanType == _CURRENT_LOANS) {
            DcObjectComparator comparator = new DcObjectComparator(Item._SYS_LOANDUEDATE);
            comparator.setAllowReloading(false);
            Collections.sort(items, comparator);
        } else { 
            DcObjectComparator comparator = new DcObjectComparator(Item._SYS_LOANSTARTDATE, DcObjectComparator._SORTORDER_DESCENDING);
            comparator.setAllowReloading(false);
            Collections.sort(items, comparator);
        }
        
        for (final DcObject item : items) {
            if (!SwingUtilities.isEventDispatchThread()) {
                try {
                    SwingUtilities.invokeAndWait(
                            new Thread(new Runnable() { 
                                @Override
                                public void run() {
                                    listener.addItem(item);
                                }
                            }));
                } catch (Exception e) {
                    logger.error(e, e);
                }
            } else {
                listener.addItem(item);
            }
        }
    }
}
