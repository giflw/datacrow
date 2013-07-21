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
import net.datacrow.util.ITaskListener;

import org.apache.log4j.Logger;

public class ICalendarExporter extends Thread{
    
    private static Logger logger = Logger.getLogger(ICalendarExporter.class.getName());

    private ITaskListener listener;
    
    private Calendar cal = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
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
        DataFilter df = new DataFilter(DcModules._LOAN);
        df.addEntry(new DataFilterEntry(
                DcModules._LOAN, 
                Loan._E_DUEDATE, 
                Operator.AFTER, 
                new Date()));
        df.toSQL(new int[] {
                Loan._A_STARTDATE, 
                Loan._B_ENDDATE,
                Loan._C_CONTACTPERSONID,
                Loan._D_OBJECTID,
                Loan._E_DUEDATE}, false, false);
        
        List<DcObject> loans = DataManager.get(df);
        
        df = new DataFilter(DcModules._LOANEXPORT);
        List<DcObject> exportedLoans = DataManager.get(df);
        
        StringBuffer sb = new StringBuffer();
        
        sb.append("BEGIN:VCALENDAR");
        sb.append("PRODID:-//datacrow-ical v1.0//Data Crow//EN");
        sb.append("VERSION:1.0");
        sb.append("CALSCALE:GREGORIAN");
        sb.append("METHOD:PUBLISH");

        for (DcObject loan : loans) {
            
            // Check if already exported. If so we are skipping this
            if (!full && exportedLoans.contains(loan)) continue;
            
            addEvent(sb, loan, "CONFIRMED", 0);
        }
        
        // cancel previously exported loans where necessary (only for incremental exports)
        if (!full) {
            for (DcObject exportedLoan : exportedLoans) {
                
                // The exported loan still exists - no need to remove it
                // If it doesn't it either means the item has been returned, or the item
                // has been remove (along with the loan objects).
                // In this case we can simply cancel the event.
                if (loans.contains(exportedLoan)) continue;
                
                addEvent(sb, exportedLoan, "CANCELLED", 1);
            }
            sb.append("END:VCALENDAR");
        }
        
        cal.clear();
        
        try {
            writeToFile(sb, file);
            
            if (!full) storeExport(loans);
            
        } catch (Exception e) {
            logger.error(e, e);
        }
    }
    
    private void writeToFile(StringBuffer sb, File file) throws Exception {
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
            for (DcObject loan : loans) {
                exportedLoan = mod.getItem();
                
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
        } catch (SQLException se) {
            logger.error(se, se);
            listener.notify("An error occurred while trying to store the exported loans in the database");
        }
    }
    
    private void addEvent(StringBuffer sb, DcObject dco, String status, int sequence) {
        Loan loan = (Loan) dco;
        String person = loan.getPerson().toString();
        DcObject item = loan.getItem();
        
        String description = "Item : " + item + ". Person: " + person;
        
        sb.append("BEGIN:VEVENT");
        
        cal.setTime((Date) loan.getValue(ExportedLoan._E_DUEDATE));
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        sb.append("DTSTART:" + sdf.format(cal.getTime()));
        
        cal.set(Calendar.HOUR_OF_DAY, 10);
        sb.append("DTEND:" + sdf.format(cal.getTime()));
        
        sb.append("SEQUENCE:" + sequence);
        sb.append("DTSTAMP:" + sdf.format(dt));
        sb.append("UID:" + loan.getID());
        sb.append("CREATED:" + sdf.format(dt));
        sb.append("DESCRIPTION:" + description);
        sb.append("LAST-MODIFIED:" + sdf.format(new Date()));
        sb.append("STATUS:" + status);
        sb.append("SUMMARY:" + description);
        sb.append("TRANSP:OPAQUE");
        
        sb.append("END:VEVENT");
    }
}
