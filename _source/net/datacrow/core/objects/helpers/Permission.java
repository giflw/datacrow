package net.datacrow.core.objects.helpers;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

public class Permission extends DcObject {

    private static final long serialVersionUID = -6649072271337366239L;
    
    public static final int _A_PLUGIN = 1;
    public static final int _B_FIELD = 2;
    public static final int _C_MODULE = 3;
    public static final int _D_VIEW = 4;
    public static final int _E_EDIT = 5;
    public static final int _F_USER = 6;
    
    public Permission() {
        super(DcModules._PERMISSION);
    }

    public String getPlugin() {
        return (String) getValue(Permission._A_PLUGIN);
    }
    
    public boolean isEditingAllowed() {
        Object o = getValue(Permission._E_EDIT);
        return o == null ? false : (Boolean) getValue(Permission._E_EDIT);
    }

    public boolean isViewingAllowed() {
        Object o = getValue(Permission._D_VIEW);
        return o == null ? false : (Boolean) getValue(Permission._D_VIEW);
    }
    
    public int getFieldIdx() {
        return getValue(Permission._B_FIELD) != null ?  
               ((Long) getValue(Permission._B_FIELD)).intValue() : -1;
    }
    
    public int getModuleIdx() {
        return getValue(Permission._C_MODULE) != null ?  
               ((Long) getValue(Permission._C_MODULE)).intValue() : -1;
    }

}
