package net.datacrow.core.modules.security;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.views.MasterView;
import net.datacrow.console.views.UserView;
import net.datacrow.console.views.View;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.DcParentModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.User;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.util.DcImageIcon;

/**
 * The user module represents users.
 * 
 * @see User
 * 
 * @author Robert Jan van der Waals
 */
public class UserModule extends DcParentModule {
    
    private static final long serialVersionUID = 8781289658107612773L;

    /**
     * Creates a new instance.
     */
    public UserModule() {
        super(DcModules._USER, 
              true,
              "User",
              "",
              "User", 
              "Users", 
              "user", 
              "usr");
    }
    
    /**
     * The small icon.
     */
    @Override
    public DcImageIcon getIcon16() {
        return IconLibrary._icoUser16;
    }

    /**
     * The large icon.
     */
    @Override
    public DcImageIcon getIcon32() {
        return IconLibrary._icoUser32;
    }

    /**
     * Retrieves the insert view.
     * @return Always null.
     */
    @Override
    public MasterView getInsertView() {
        return null;
    }

    /**
     * Indicates if other modules are depending on this module.
     * @return Always false.
     */
    @Override
    public boolean hasDependingModules() {
        return false;
    }

    /**
     * Indicates if this module is enabled.
     * @return Depends if the user currently logged on is an administrator. 
     */
    @Override
    public boolean isEnabled() {
        return SecurityCentre.getInstance().getUser() != null ? SecurityCentre.getInstance().getUser().isAdmin() : true;
    }

    /**
     * Indicates if this module has a search view.
     * @return Always true.
     */
    @Override
    public boolean hasSearchView() {
        return true;
    }
    
    /**
     * Indicates if this module has an insert view.
     * @return Always false.
     */
    @Override
    public boolean hasInsertView() {
        return false;
    }

    /**
     * Indicates if items belonging to this module are file based.
     * @return Always false.
     */
    @Override
    public boolean isFileBacked() {
        return false;
    }

    /**
     * Creates a new user instance.
     * @see User
     */
    @Override
    public DcObject createItem() {
        return new User();
    }
    
    /**
     * Retrieves the child module
     * @see PermissionModule
     */
    @Override
    public DcModule getChild() {
        return DcModules.get(DcModules._PERMISSION);
    }
    
    /**
     * Indicates if this module is allowed to be customized.
     * @return Always false.
     */
    @Override
    public boolean isCustomFieldsAllowed() {
        return false;
    }
    
    /**
     * Indicates if this module is a parent module.
     * @return Always true
     */
    @Override
    public boolean isParentModule() {
        return true;
    }

    /**
     * Initializes all views.
     */
    @Override
    protected void initializeUI()  {
        if (searchView == null) {
            searchView = new MasterView(getIndex());
            searchView.setTreePanel(this);
            
            // list view
            DcObjectList list = new DcObjectList(this, DcObjectList._CARDS, true, true);
            View listView = new UserView(searchView, View._TYPE_SEARCH, list, MasterView._LIST_VIEW);
            list.setView(listView);

            searchView.addView(MasterView._LIST_VIEW, listView);            
        }
    }
    
    @Override
    public int[] getSupportedViews() {
        return new int[] {MasterView._LIST_VIEW};
    }
    
    /**
     * Initializes the default fields.
     */
    @Override
    protected void initializeFields() {
        super.initializeFields();
        addField(new DcField(User._A_LOGINNAME, getIndex(), "Login Name",
                false, true, false, true, 
                255, ComponentFactory._LOGINNAMEFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "LoginName"));
        addField(new DcField(User._B_ENABLED, getIndex(), "Enabled",
                false, true, false, true, 
                4, ComponentFactory._CHECKBOX, getIndex(), DcRepository.ValueTypes._BOOLEAN,
                "Enabled"));
        addField(new DcField(User._C_NAME, getIndex(), "Name",
                false, true, false, true, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Name"));
        addField(new DcField(User._D_DESCRIPTION, getIndex(), "Description",
                false, true, false, true, 
                4000, ComponentFactory._LONGTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Description"));
        addField(new DcField(User._E_PHOTO, getIndex(), "Photo",
                true, true, false, false, 
                255, ComponentFactory._PICTUREFIELD, getIndex(), DcRepository.ValueTypes._PICTURE,
                "Photo"));        
        addField(new DcField(User._F_EMAIL, getIndex(), "Email",
                false, true, false, true, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Email"));        
        addField(new DcField(User._G_ADDRESS, getIndex(), "Address",
                false, true, false, true, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Address"));     
        addField(new DcField(User._H_PHONE_HOME, getIndex(), "Phone (Home)",
                false, true, false, false, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "PhoneHome"));     
        addField(new DcField(User._I_PHONE_WORK, getIndex(), "Phone (Work)",
                false, true, false, false, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "PhoneWork"));        
        addField(new DcField(User._J_CITY, getIndex(), "City",
                false, true, false, true, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "City"));
        addField(new DcField(User._K_COUNTRY, getIndex(), "Country",
                false, true, false, true, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Country"));
        addField(new DcField(User._L_ADMIN, getIndex(), "Admin",
                false, true, false, true, 
                4, ComponentFactory._CHECKBOX, getIndex(), DcRepository.ValueTypes._BOOLEAN,
                "Admin"));
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof UserModule ? ((UserModule) o).getIndex() == getIndex() : false);
    }     
}
