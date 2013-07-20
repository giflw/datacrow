package net.datacrow.core.objects;

import net.datacrow.core.modules.DcModules;

public class ExportedLoan extends Loan {
    
    public static final int _F_SEQUENCE = 6;
    
    public ExportedLoan() {
        super(DcModules._LOANEXPORT);
    }
}
