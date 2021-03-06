package net.datacrow.core.modules;

import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.ExternalReference;

public class ExternalReferenceModule extends DcPropertyModule {

    public ExternalReferenceModule() {
        super(DcModules._EXTERNALREFERENCE, "External Reference", "ExternalReference", "ExtRef", "External Reference", "External References");
    }
    
    public ExternalReferenceModule(int index, String name, String tableName,
            String tableShortName, String objectName, String objectNamePlural) {
        super(index, name, tableName, tableShortName, objectName, objectNamePlural);
    }
    
    @Override
    public DcPropertyModule getInstance(int index, String name, String tableName,
            String tableShortName, String objectName, String objectNamePlural) {
        
        return new ExternalReferenceModule(index, name, tableName, tableShortName, objectName, objectNamePlural);
    }
    
    @Override
    public int[] getMinimalFields(Collection<Integer> include) {
        Collection<Integer> c = new ArrayList<Integer>();
        
        if (include != null)
            c.addAll(include);
        
        if (!c.contains(Integer.valueOf(ExternalReference._EXTERNAL_ID))) 
            c.add(Integer.valueOf(ExternalReference._EXTERNAL_ID));
        if (!c.contains(Integer.valueOf(ExternalReference._EXTERNAL_ID_TYPE))) 
            c.add(Integer.valueOf(ExternalReference._EXTERNAL_ID_TYPE));        
        
        return super.getMinimalFields(c);
    }

    @Override
    public DcObject createItem() {
        return new ExternalReference(getIndex());
    }
    
    @Override
    public int getType() {
        return DcModule._TYPE_EXTERNALREFERENCE_MODULE;
    }

    @Override
    public boolean hasDependingModules() {
        return true;    
    }
    
    @Override
    public int getDefaultSortFieldIdx() {
        return ExternalReference._EXTERNAL_ID_TYPE;
    }
    
    @Override
    public int getDisplayIndex() {
        return ExternalReference._EXTERNAL_ID;
    }
    
    @Override
    public boolean isServingMultipleModules() {
        return false;
    }

    /**
     * Initializes the default fields.
     */
    @Override
    protected void initializeFields() {
        super.initializeFields();
        addField(new DcField(ExternalReference._EXTERNAL_ID, getIndex(), "External ID", 
                false, true, false, false, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "externalid"));
        
        addField(new DcField(ExternalReference._EXTERNAL_ID_TYPE, getIndex(), "Type", 
                false, true, false, false, 
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                "externalidtype"));        

        getField(DcObject._ID).setEnabled(false);
    } 
}
