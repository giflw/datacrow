package net.datacrow.core.modules.security;

import javax.swing.ImageIcon;

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
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.security.SecurityCentre;

public class PermissionModule extends DcChildModule {

    private static final long serialVersionUID = -7129402893574458367L;

    public PermissionModule() {
        super(DcModules._PERMISSION, 
              false,
              DcResources.getText("sysPermission"),
              "",
              DcResources.getText("sysPermission"), 
              DcResources.getText("sysPermissionPlural"), 
              "permission", 
              "perm");
    }
    
    @Override
    public DcObject getDcObject() {
        return new Permission();
    }
    
    @Override
    public DcModule getParent() {
        return DcModules.get(DcModules._USER);
    }
    
    @Override
    public ImageIcon getIcon16() {
        return IconLibrary._icoPermission16;
    }

    @Override
    public ImageIcon getIcon32() {
        return IconLibrary._icoPermission32;
    }

    @Override
    public MasterView getInsertView() {
        return null;
    }

    @Override
    public boolean hasDependingModules() {
        return false;
    }

    @Override
    public boolean hasSearchView() {
        return true;
    }
    
    @Override
    public boolean hasInsertView() {
        return false;
    }

    @Override
    public boolean isFileBacked() {
        return false;
    }
    
    @Override
    public boolean isCustomFieldsAllowed() {
        return false;
    }
    
    @Override
    public boolean isChildModule() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return SecurityCentre.getInstance().getUser() != null ? SecurityCentre.getInstance().getUser().isAdmin() : true;
    }
    
    @Override
    protected void initializeUI()  {
        if (searchView == null) {
            searchView = new MasterView();
            searchView.setTreePanel(this);
            
            DcTable table = new DcTable(this, true, true);
            View view = new View(searchView, View._TYPE_SEARCH, table, getObjectNamePlural(), getIcon16(), MasterView._TABLE_VIEW);
            table.setView(view);

            searchView.addView(MasterView._TABLE_VIEW, view);       
        }
    }    

    @Override
    protected void initializeFields() {
        super.initializeFields();
        addField(new DcField(Permission._A_PLUGIN, getIndex(), "Plugin",
                false, true, false, false, false,
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Plugin"));
        addField(new DcField(Permission._B_FIELD, getIndex(), "Field",
                false, true, false, false, false,
                4000, ComponentFactory._NUMBERFIELD, getIndex(), DcRepository.ValueTypes._LONG,
                "Field"));
        addField(new DcField(Permission._C_MODULE, getIndex(), "Module",
                false, true, false, false, false,
                10, ComponentFactory._NUMBERFIELD, getIndex(), DcRepository.ValueTypes._LONG,
                "Module"));
        addField(new DcField(Permission._D_VIEW, getIndex(), "View",
                false, true, false, false, false,
                1, ComponentFactory._CHECKBOX, getIndex(), DcRepository.ValueTypes._BOOLEAN,
                "View"));
        addField(new DcField(Permission._E_EDIT, getIndex(), "Edit",
                false, true, false, false, false,
                1, ComponentFactory._CHECKBOX, getIndex(), DcRepository.ValueTypes._BOOLEAN,
                "Edit"));
        addField(new DcField(Permission._F_USER, DcModules._USER, "User",
                false, true, false, false, false,
                20, ComponentFactory._NUMBERFIELD, getIndex(), DcRepository.ValueTypes._DCPARENTREFERENCE,
                "User"));
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof PermissionModule ? ((PermissionModule) o).getIndex() == getIndex() : false);
    }     
}
