package net.datacrow.core.objects.helpers;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

public class ExternalReference extends DcObject {

    private static final long serialVersionUID = 9031499353731926500L;

    public static final int _EXTERNAL_ID = 151;
    public static final int _EXTERNAL_ID_TYPE = 152;
    
    public ExternalReference() {
        super(DcModules._EXTERNALREFERENCE);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ExternalReference && ((DcObject) o).getID() != null &&  
               ((DcObject) o).getID().equals(getID());
    }

    @Override
    public int getDefaultSortFieldIdx() {
        return ExternalReference._EXTERNAL_ID;
    }

    @Override
    public int getDisplayFieldIdx() {
        return ExternalReference._EXTERNAL_ID;
    }

    @Override
    public String getName() {
        return getDisplayString(_EXTERNAL_ID_TYPE) + ": " + getDisplayString(_EXTERNAL_ID); 
    }
}
