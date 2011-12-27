package net.datacrow.core.modules.security;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.views.MasterView;
import net.datacrow.console.views.View;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcChildModule;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.Permission;
import net.datacrow.core.objects.helpers.User;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.util.DcImageIcon;

/**
 * The permission module holds all permissions as part of the security functionality.
 * The permission module is a child module of the user module.
 * 
 * @see UserModule
 * @see User
 * @see Permission
 * 
 * @author Robert Jan van der Waals
 */
public class PermissionModule extends DcChildModule {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = -7129402893574458367L;

    /**
     * Creates a new instance.
     */
    public PermissionModule() {
        super(DcModules._PERMISSION, 
              false,
              "Permission",
              "",
              "Permission", 
              "Permissions", 
              "permission", 
              "perm");
    }
    
    /**
     * Creates a new permission.
     * @see Permission
     */
    @Override
    public DcObject createItem() {
        return new Permission();
    }
    
    /**
     * Retrieves the parent module.
     * @see UserModule
     */
    @Override
    public DcModule getParent() {
        return DcModules.get(DcModules._USER);
    }

    /**
     * The small icon.
     */
    @Override
    public DcImageIcon getIcon16() {
        return IconLibrary._icoPermission16;
    }

    /**
     * The large icon.
     */
    @Override
    public DcImageIcon getIcon32() {
        return IconLibrary._icoPermission32;
    }

    /**
     * The insert view.
     * @return Always null for this module.
     */
    @Override
    public MasterView getInsertView() {
        return null;
    }

    /**
     * Indicates of other modules are depending on this module.
     * @return Always false for this module.
     */
    @Override
    public boolean hasDependingModules() {
        return false;
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
     * Indicates if this module is allowed to be customized.
     * @return Always false.
     */
    @Override
    public boolean isCustomFieldsAllowed() {
        return false;
    }
    
    /**
     * Indicates if this is a child module.
     * @return Always true.
     */
    @Override
    public boolean isChildModule() {
        return true;
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
     * Creates all the views.
     */
    @Override
    protected void initializeUI()  {
        if (searchView == null) {
            searchView = new MasterView(getIndex());
            searchView.setTreePanel(this);
            
            DcTable table = new DcTable(this, true, true);
            View view = new View(searchView, View._TYPE_SEARCH, table, getObjectNamePlural(), getIcon16(), MasterView._TABLE_VIEW);
            table.setView(view);

            searchView.addView(MasterView._TABLE_VIEW, view);       
        }
    }    

    /**
     * Creates the default fields.
     */
    @Override
    protected void initializeFields() {
        super.initializeFields();
        addField(new DcField(Permission._A_PLUGIN, getIndex(), "Plugin",
                false, true, false, false, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Plugin"));
        addField(new DcField(Permission._B_FIELD, getIndex(), "Field",
                false, true, false, false, 
                4000, ComponentFactory._NUMBERFIELD, getIndex(), DcRepository.ValueTypes._LONG,
                "Field"));
        addField(new DcField(Permission._C_MODULE, getIndex(), "Module",
                false, true, false, false, 
                10, ComponentFactory._NUMBERFIELD, getIndex(), DcRepository.ValueTypes._LONG,
                "Module"));
        addField(new DcField(Permission._D_VIEW, getIndex(), "View",
                false, true, false, false, 
                1, ComponentFactory._CHECKBOX, getIndex(), DcRepository.ValueTypes._BOOLEAN,
                "View"));
        addField(new DcField(Permission._E_EDIT, getIndex(), "Edit",
                false, true, false, false, 
                1, ComponentFactory._CHECKBOX, getIndex(), DcRepository.ValueTypes._BOOLEAN,
                "Edit"));
        addField(new DcField(Permission._F_USER, DcModules._USER, "User",
                false, true, false, false, 
                36, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._DCPARENTREFERENCE,
                "User"));
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof PermissionModule ? ((PermissionModule) o).getIndex() == getIndex() : false);
    }     
}
