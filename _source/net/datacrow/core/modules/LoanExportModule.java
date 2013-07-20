package net.datacrow.core.modules;

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ExportedLoan;
import net.datacrow.core.objects.Loan;

import org.apache.log4j.Logger;

public class LoanExportModule extends LoanModule {

    
    private static Logger logger = Logger.getLogger(DcModule.class.getName());
    
    private static final long serialVersionUID = -1777037389578494831L;

    /**
     * Creates a new instance of this module.
     */
    public LoanExportModule() {
        super(DcModules._LOANEXPORT, 
              false, 
              "Exported Loans", 
              "", 
              "Exported Loan",
              "Exported Loans",
              "exportedloans", 
              "el");
    }

    /**
     * Creates a new instance of a loan.
     * @see Loan
     */
    @Override
    public DcObject createItem() {
        return new ExportedLoan();
    }

    /**
     * This module does not have any views and therefore this method has not having to do.
     */

    @Override
    public boolean equals(Object o) {
        return (o instanceof LoanModule ? ((LoanModule) o).getIndex() == getIndex() : false);
    }    
}
