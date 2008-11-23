package net.datacrow.core.modules.security;

import javax.swing.ImageIcon;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.components.tables.DcTable;
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
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.security.SecurityCentre;

public class UserModule extends DcParentModule {
    
    private static final long serialVersionUID = 8781289658107612773L;

    public UserModule() {
        super(DcModules._USER, 
              true,
              DcResources.getText("sysUser"),
              "",
              DcResources.getText("sysUser"), 
              DcResources.getText("sysUserPlural"), 
              "user", 
              "usr");
    }

    @Override
    public ImageIcon getIcon16() {
        return IconLibrary._icoUser16;
    }

    @Override
    public ImageIcon getIcon32() {
        return IconLibrary._icoUser32;
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
    public boolean isEnabled() {
        return SecurityCentre.getInstance().getUser() != null ? SecurityCentre.getInstance().getUser().isAdmin() : true;
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
    public DcObject getDcObject() {
        return new User();
    }
    
    @Override
    public DcModule getChild() {
        return DcModules.get(DcModules._PERMISSION);
    }
    
    @Override
    public boolean isCustomFieldsAllowed() {
        return false;
    }
    
    @Override
    public boolean isParentModule() {
        return true;
    }

    @Override
    protected void initializeUI()  {
        if (searchView == null) {
            searchView = new MasterView();
            searchView.setTreePanel(this);
            
            // table view
            DcTable table = new DcTable(this, false, true);
            View tableView = new UserView(searchView, View._TYPE_SEARCH, table, getObjectNamePlural(), getIcon16(), MasterView._TABLE_VIEW);
            table.setView(tableView);
            
            // list view
            DcObjectList list = new DcObjectList(this, DcObjectList._CARDS, true, true);
            View listView = new UserView(searchView, View._TYPE_SEARCH, list, getObjectNamePlural(), getIcon16(), MasterView._LIST_VIEW);
            list.setView(listView);

            searchView.addView(MasterView._TABLE_VIEW, tableView);
            searchView.addView(MasterView._LIST_VIEW, listView);            
        }
    }
    
    @Override
    protected void initializeFields() {
        super.initializeFields();
        addField(new DcField(User._A_LOGINNAME, getIndex(), "Login Name",
                false, true, false, true, false,
                255, ComponentFactory._LOGINNAMEFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "LoginName"));
        addField(new DcField(User._B_ENABLED, getIndex(), "Enabled",
                false, true, false, true, false,
                4, ComponentFactory._CHECKBOX, getIndex(), DcRepository.ValueTypes._BOOLEAN,
                "Enabled"));
        addField(new DcField(User._C_NAME, getIndex(), "Name",
                false, true, false, true, false,
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Name"));
        addField(new DcField(User._D_DESCRIPTION, getIndex(), "Description",
                false, true, false, true, false,
                4000, ComponentFactory._LONGTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Description"));
        addField(new DcField(User._E_PHOTO, getIndex(), "Photo",
                true, true, false, false, false,
                255, ComponentFactory._PICTUREFIELD, getIndex(), DcRepository.ValueTypes._PICTURE,
                "Photo"));        
        addField(new DcField(User._F_EMAIL, getIndex(), "Email",
                false, true, false, true, false,
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Email"));        
        addField(new DcField(User._G_ADDRESS, getIndex(), "Address",
                false, true, false, true, false,
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Address"));     
        addField(new DcField(User._H_PHONE_HOME, getIndex(), "Phone (Home)",
                false, true, false, false, false,
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "PhoneHome"));     
        addField(new DcField(User._I_PHONE_WORK, getIndex(), "Phone (Work)",
                false, true, false, false, false,
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "PhoneWork"));        
        addField(new DcField(User._J_CITY, getIndex(), "City",
                false, true, false, true, false,
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "City"));
        addField(new DcField(User._K_COUNTRY, getIndex(), "Country",
                false, true, false, true, false,
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Country"));
        addField(new DcField(User._L_ADMIN, getIndex(), "Admin",
                false, true, false, true, false,
                4, ComponentFactory._CHECKBOX, getIndex(), DcRepository.ValueTypes._BOOLEAN,
                "Admin"));
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof UserModule ? ((UserModule) o).getIndex() == getIndex() : false);
    }     
}
