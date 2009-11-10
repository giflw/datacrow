package net.datacrow.core.objects.helpers;

import net.datacrow.core.DataCrow;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.DeleteUserRequest;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.DcSwingUtilities;

public class User extends DcObject {

    private static final long serialVersionUID = -6350928968206517038L;
    
    public static final int _A_LOGINNAME = 1;
    public static final int _B_ENABLED = 2;
    public static final int _C_NAME = 3;
    public static final int _D_DESCRIPTION = 4;
    public static final int _E_PHOTO = 5;
    public static final int _F_EMAIL = 6;
    public static final int _G_ADDRESS = 7;
    public static final int _H_PHONE_HOME = 8;
    public static final int _I_PHONE_WORK = 9;
    public static final int _J_CITY = 11;
    public static final int _K_COUNTRY = 12;
    public static final int _L_ADMIN = 13;
    
    public User() {
        super(DcModules._USER);
    }
    
    @Override
    public void loadChildren() {
        // Permissions do not need to be reloaded as the number of permission is static.
        // Note that doing this for other modules will re-introduce the bug: deleted children are shown.
        if (getModule().getChild() != null && children.size() == 0) {
            children.clear();
            int childIdx = getModule().getChild().getIndex();
            for (DcObject dco : DataManager.getChildren(getID(), childIdx))
                children.add(dco);
        }
    }      
    
    @Override
    protected void beforeSave() throws ValidationException {
        super.beforeSave();

        Picture picture = (Picture) getValue(_E_PHOTO);
        if (picture == null || picture.getValue(Picture._D_IMAGE) == null)
        	setValue(User._E_PHOTO, new DcImageIcon(DataCrow.installationDir + "icons/" + "user.png"));
        
        String loginname = (String) getValue(_A_LOGINNAME);
        if (loginname != null)
            setValue(_A_LOGINNAME, loginname.toLowerCase());
        else 
            throw new ValidationException(DcResources.getText("msgLoginNameNotFilled"));
    }

    @Override
    public void saveNew(boolean queued) throws ValidationException {
        setValue(_B_ENABLED, Boolean.TRUE);
        super.saveNew(queued);
    }
    
    @Override
    public void checkIntegrity(boolean update) throws ValidationException {
    	super.checkIntegrity(update);
    	
    	if (update & isChanged(User._A_LOGINNAME)) {
    		DcObject original = DataManager.getObject(DcModules._USER, getID());
    		setValue(User._A_LOGINNAME, original.getValue(User._A_LOGINNAME));
    		getValueDef(User._A_LOGINNAME).setChanged(false);
    		throw new ValidationException(DcResources.getText("msgLoginnameIsNotAllowedToChange"));
    	}
    }

    @Override
    public void delete(boolean validate) throws ValidationException {
        boolean canBeDeleted = true;
        
        if (isAdmin()) {
            DataFilter df = new DataFilter(DcModules._USER);
            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, 
                                            DcModules._USER, 
                                            User._L_ADMIN, 
                                            Operator.EQUAL_TO, 
                                            Boolean.TRUE));
         
            canBeDeleted = DataManager.get(DcModules._USER, df).length > 1;
        }
                
        if (canBeDeleted) {
            addRequest(new DeleteUserRequest(this));
            super.delete(validate);
        } else {
            DcSwingUtilities.displayWarningMessage("msgCannotDeleteThisUser");
        }
    }

    public boolean isAdmin() {
        return getValue(_L_ADMIN) != null ? Boolean.valueOf(getValue(_L_ADMIN).toString()) : false;
    }
}
