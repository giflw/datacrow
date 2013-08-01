package net.datacrow.core.modules;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.objects.DcAssociate;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;

public class DcAssociateModule extends DcModule {
    
    /**
     * Creates a new instance.
     * @param index The module index.
     * @param topModule Indicates if the module is a top module. Top modules are allowed
     * to be displayed in the module bar and can be enabled or disabled.
     * @param name The internal unique name of the module.
     * @param description The module description
     * @param objectName The name of the items belonging to this module.
     * @param objectNamePlural The plural name of the items belonging to this module.
     * @param tableName The database table name for this module.
     * @param tableShortName The database table short name for this module.
     */
    public DcAssociateModule(int index,
                             String name,
                             String description, 
                             String objectName, 
                             String objectNamePlural,
                             String tableName, 
                             String tableShortName) {
        
        super(index, false, name, description, objectName, objectNamePlural,
              tableName, tableShortName);
    }
    
    @Override
    public int getDisplayFieldIdx() {
        return DcAssociate._A_NAME;
    }
    
    /**
     * Creates this module based on an XML module definition.
     * @param module
     */
    public DcAssociateModule(XmlModule module) {
        super(module);
    }
    
    @Override
    public boolean isContainerManaged() {
        return false;
    }

    /**
     * Indicates whether this module be selected from the module bar.
     */
    @Override
    public boolean isSelectableInUI() {
        return false;
    }
    
    @Override
    public int getDefaultSortFieldIdx() {
        return DcAssociate._A_NAME;
    }
    
    @Override
    public boolean isTopModule() {
        return true;
    }

    /**
     * Creates a new instance of an item belonging to this module.
     */
    @Override
    public DcObject createItem() {
        return new DcAssociate(getIndex());
    }
    
    @Override
    protected void initializeFields() {
        super.initializeFields();
        
        addField(new DcField(DcAssociate._A_NAME, getIndex(), "Name", 
                false, true, false, true, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Name"));
        addField(new DcField(DcAssociate._B_DESCRIPTION, getIndex(), "Description", 
                false, true, false, true, 
                4000, ComponentFactory._LONGTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Description"));
        addField(new DcField(DcAssociate._C_WEBPAGE, getIndex(), "Webpage", 
                false, true, false, true, 
                255, ComponentFactory._URLFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "webpage"));
        addField(new DcField(DcAssociate._D_PHOTO, getIndex(), "Photo", 
                true, true, false, false, 
                0, ComponentFactory._PICTUREFIELD, getIndex(), DcRepository.ValueTypes._PICTURE,
                "Photo"));
        addField(new DcField(DcAssociate._E_FIRSTNAME, getIndex(), "Firstname", 
                false, true, false, true, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Firstname"));
        addField(new DcField(DcAssociate._F_LASTTNAME, getIndex(), "Lastname", 
                false, true, false, true, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "Lastname"));
        
        getField(DcObject._ID).setEnabled(false);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DcAssociateModule ? ((DcAssociateModule) o).getIndex() == getIndex() : false);
    }      
}
