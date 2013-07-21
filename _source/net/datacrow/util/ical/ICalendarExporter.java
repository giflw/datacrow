package net.datacrow.util.ical;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ExportedLoan;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.ITaskListener;

import org.apache.log4j.Logger;

public class ICalendarExporter extends Thread{
    
    private static Logger logger = Logger.getLogger(ICalendarExporter.class.getName());

    private ITaskListener listener;
    
    private Calendar cal = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("dd' 'MMM' 'yyyy");
    private Date dt = new Date();
    private boolean full = false;
    
    private File file;
    
    public ICalendarExporter(ITaskListener listener, File file, boolean full) {
        this.listener = listener;
        this.full = full;
        this.file = file;
    }
    
    @Override
    public void run() {
        
        listener.notifyTaskStarted();
        
        DataFilter df = new DataFilter(DcModules._LOAN);
        df.addEntry(new DataFilterEntry(
                DcModules._LOAN, 
                Loan._B_ENDDATE, 
                Operator.IS_EMPTY,
                null));
        df.addEntry(new DataFilterEntry(
                DcModules._LOAN, 
                Loan._E_DUEDATE, 
                Operator.IS_FILLED,
                null)); 
        
        List<DcObject> loans = DataManager.get(df, 
                new int[] {
                Loan._ID,
                Loan._A_STARTDATE, 
                Loan._B_ENDDATE,
                Loan._C_CONTACTPERSONID,
                Loan._D_OBJECTID,
                Loan._E_DUEDATE});
        
        df = new DataFilter(DcModules._LOANEXPORT);
        List<DcObject> exportedLoans = DataManager.get(df);
        
        StringBuffer sb = new StringBuffer();
        
        sb.append("BEGIN:VCALENDAR" + "\n");
        sb.append("PRODID:-//datacrow-ical v1.0//Data Crow//EN" + "\n");
        sb.append("VERSION:1.0" + "\n");
        sb.append("CALSCALE:GREGORIAN" + "\n");
        sb.append("METHOD:PUBLISH" + "\n");

        for (DcObject loan : loans) {
            
            if (listener.isStopped()) break;
            
            // Check if already exported. If so we are skipping this
            if (!full && exportedLoans.contains(loan)) continue;
            
            listener.notifyProcessed();
            
            addEvent(sb, loan, "CONFIRMED", 0);
        }
        
        // cancel previously exported loans where necessary (only for incremental exports)
        if (!full && !listener.isStopped()) {
            for (DcObject exportedLoan : exportedLoans) {
                
                if (listener.isStopped()) break;
                
                // The exported loan still exists - no need to remove it
                // If it doesn't it either means the item has been returned, or the item
                // has been remove (along with the loan objects).
                // In this case we can simply cancel the event.
                if (loans.contains(exportedLoan)) continue;
                
                listener.notifyProcessed();
                
                addEvent(sb, exportedLoan, "CANCELLED", 1);
            }
            sb.append("END:VCALENDAR");
        }
        
        cal.clear();
        
        try {
            
            if (!listener.isStopped())writeToFile(sb, file);
            
            if (!full && !listener.isStopped()) storeExport(loans);
            
        } catch (Exception e) {
            logger.error(e, e);
        }
        
        listener.notify(DcResources.getText("msgCalendarExportComplete"));
        listener.notifyTaskStopped();
    }
    
    private void writeToFile(StringBuffer sb, File file) throws Exception {
        
        if (file.exists()) file.delete();
        
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(sb.toString());
        out.close();
    }
    
    private void storeExport(List<DcObject> loans) {
        try {
            DatabaseManager.execute("DELETE FROM " + DcModules.get(DcModules._LOANEXPORT).getTableName());
            DcModule mod = DcModules.get(DcModules._LOANEXPORT);
            DcObject exportedLoan;
            Long zero = new Long(0);
            
            listener.notify(DcResources.getText("msgStoringExportedLoansDB"));
            for (DcObject loan : loans) {
                exportedLoan = mod.getItem();
                
                listener.notifyProcessed();
                
                exportedLoan.setValueLowLevel(ExportedLoan._ID, loan.getID());
                exportedLoan.setValueLowLevel(ExportedLoan._A_STARTDATE, loan.getValue(Loan._A_STARTDATE));
                exportedLoan.setValueLowLevel(ExportedLoan._B_ENDDATE, loan.getValue(Loan._B_ENDDATE));
                exportedLoan.setValueLowLevel(ExportedLoan._C_CONTACTPERSONID, loan.getValue(Loan._C_CONTACTPERSONID));
                exportedLoan.setValueLowLevel(ExportedLoan._D_OBJECTID, loan.getValue(Loan._D_OBJECTID));
                exportedLoan.setValueLowLevel(ExportedLoan._E_DUEDATE, loan.getValue(Loan._E_DUEDATE));
                
                // For future use - in case we want to modify items (instead of canceling)
                exportedLoan.setValueLowLevel(ExportedLoan._F_SEQUENCE, zero);
                
                try {
                    exportedLoan.saveNew(false);
                } catch (ValidationException ve) {
                    logger.error("An error occured while saving the exported loan into the database", ve);
                }
            }
            
            listener.notify(DcResources.getText("msgStoringExportedLoansDBCompleted"));
            
        } catch (SQLException se) {
            logger.error(se, se);
            listener.notify("An error occurred while trying to store the exported loans in the database");
        }
    }
    
    private void addEvent(StringBuffer sb, DcObject dco, String status, int sequence) {
        Loan loan = (Loan) dco;
        String person = loan.getPerson().toString();
        DcObject item = loan.getItem();
        
        if (item == null) return;
        
        item.load(item.getModule().getMinimalFields(null));
        
        String summary = DcResources.getText("msgCalendarSummary", new String[] {
                DcResources.getText(item.getModule().getItemResourceKey()),
                item.toString(),
                person});
        
        
        String description = DcResources.getText("msgCalendarDescription", new String[] {
                DcResources.getText(item.getModule().getItemResourceKey()),
                item.toString(),
                person,
                sdf2.format(loan.getValue(Loan._A_STARTDATE))});
        
        listener.notify(DcResources.getText("msgCalendarAddedEvent", new String[] {status, item.toString()}));
        
        sb.append("BEGIN:VEVENT" + "\n");
        
        cal.setTime((Date) loan.getValue(ExportedLoan._E_DUEDATE));
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        sb.append("DTSTART:" + sdf.format(cal.getTime()) + "\n");
        
        cal.set(Calendar.HOUR_OF_DAY, 10);
        sb.append("DTEND:" + sdf.format(cal.getTime()) + "\n");
        
        sb.append("SEQUENCE:" + sequence + "\n");
        sb.append("DTSTAMP:" + sdf.format(dt) + "\n");
        sb.append("UID:" + loan.getID() + "\n");
        sb.append("CREATED:" + sdf.format(dt) + "\n");
        sb.append("DESCRIPTION:" + description + "\n");
        sb.append("LAST-MODIFIED:" + sdf.format(new Date()) + "\n");
        sb.append("STATUS:" + status + "\n");
        sb.append("SUMMARY:" + summary + "\n");
        sb.append("TRANSP:OPAQUE" + "\n");
        
        sb.append("END:VEVENT" + "\n");
    }
}
