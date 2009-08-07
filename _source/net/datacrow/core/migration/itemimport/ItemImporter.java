package net.datacrow.core.migration.itemimport;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.migration.ItemMigrater;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.util.Base64;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

/**
 * Source Readers are capable of reading source file and parsing the information
 * into Data Crow compatible items. Item relationships are imported in a loosely coupled way.
 * 
 * @see DataManager#createReference(DcObject, int, Object)
 * 
 * @author Robert Jan van der Waals
 */
public abstract class ItemImporter extends ItemMigrater {
    
    protected IItemImporterClient client;
    protected ItemImporterFieldMappings mappings = new ItemImporterFieldMappings();

    // Local settings and properties > overrule the general settings
    private Map<String, String> settings = new HashMap<String, String>(); 

    public ItemImporter(int moduleIdx, String key, int mode) throws Exception {
        super(moduleIdx, key, mode);
    }
    
    public boolean requiresMapping() {
        return false;
    }
    
    public void setSetting(String key, String value) {
        settings.put(key, value);
    }
    
    public String getSetting(String key) {
        return settings.get(key);
    }  
    
    /**
     * The official settings which can be used in combination with the 
     * specific source reader implementation.
     */
    public Collection<String> getSettingKeys() {
        return new ArrayList<String>();
    }
    
    /**
     * Adds a field mapping.
     */
    public void addMapping(String source, DcField target) {
        mappings.setMapping(source, target);
    }
    
    /**
     * Retrieves all field mappings.
     * @return
     */
    public ItemImporterFieldMappings getSourceMappings() {
        return mappings;
    }
    
    public abstract String[] getSupportedFileTypes();
    
    public Collection<String> getSourceFields() {
        return mappings.getSourceFields();
    }
    
    public DcField getTargetField(String source) {
        return mappings.getTarget(source);
    }
    
    public void setClient(IItemImporterClient client) {
        this.client = client;
    }

    protected void setValue(DcObject dco, int fieldIdx, String value) throws Exception {
        DcField field = dco.getModule().getField(fieldIdx);
        
        if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
            field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
            DataManager.createReference(dco, field.getIndex(), value);
        } else if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
            try {
                byte[] image = Base64.decode(value.toCharArray());
                dco.setValue(field.getIndex(), new DcImageIcon(image));
            } catch (Exception e) {
                if (value.trim().length() > 0) {
                    File file = new File(value);
                    file = file.exists() ? file : new File(DataCrow.installationDir, value);
                    byte[] image = Utilities.readFile(file);
                    dco.setValue(field.getIndex(), new DcImageIcon(image));
                }
            }
        } else if (field.getValueType() == DcRepository.ValueTypes._ICON) {
            File file = new File(DataCrow.installationDir, value);
            String s = Utilities.fileToBase64String(new URL("file://" + file));
            s = Utilities.isEmpty(s) ? Utilities.fileToBase64String(new URL("file://" + value)) : s;
            dco.setValue(field.getIndex(), s);
        } else {
            dco.setValue(field.getIndex(), value);
        }
    }
}
