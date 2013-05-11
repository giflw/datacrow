package net.datacrow.core.migration.itemimport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.migration.ItemMigrater;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * Source Readers are capable of reading source file and parsing the information
 * into Data Crow compatible items. Item relationships are imported in a loosely coupled way.
 * 
 * @see DataManager#createReference(DcObject, int, Object)
 * 
 * @author Robert Jan van der Waals
 */
public abstract class ItemImporter extends ItemMigrater {
    
    private static Logger logger = Logger.getLogger(ItemImporter.class.getName());
    
    protected IItemImporterClient client;
    protected ItemImporterFieldMappings mappings = new ItemImporterFieldMappings();

    // Local settings and properties > overrule the general settings
    private Map<String, String> settings = new HashMap<String, String>(); 

    public ItemImporter(int moduleIdx, String key, int mode) throws Exception {
        super(moduleIdx, key, mode, true);
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
    public void clearMappings() {
        mappings.clear();
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
    
    private File getImagePath(String s) {
        String value = s; 
        value = value.startsWith("file://") ? value.substring("file://".length()) : value;
        File file = new File(value); 
        file = file.exists() ? file : new File(DataCrow.installationDir, value);
        
        if (!file.exists()) {
            // maybe the path is relative ?
            String importName = getFile().getName();
            importName = importName.lastIndexOf(".") > -1 ?  importName.substring(0, importName.lastIndexOf(".")) : importName; 
            file = new File(getFile().getParent(), importName + "_images/" + file.getName());    
            
            if (!file.exists())
                file = new File(DataCrow.installationDir , value);
            
        }
        return file;
    }

    protected void setValue(DcObject dco, int fieldIdx, String value, IItemImporterClient listener) {
        
        if (Utilities.isEmpty(value)) return;
        
        DcField field = dco.getModule().getField(fieldIdx);
        
        if (field.getIndex() == DcObject._SYS_EXTERNAL_REFERENCES)
            return;
        
        try {
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {

                DataManager.createReference(dco, field.getIndex(), value);
                
            } else if (field.getFieldType() == ComponentFactory._TIMEFIELD) { 
                try {
                    dco.setValue(field.getIndex(), Long.valueOf(value));
                } catch (NumberFormatException nfe) {
                    if (value.indexOf(":") > -1) {
                        int hours = Integer.parseInt(value.substring(0, value.indexOf(":")));
                        int minutes = Integer.parseInt(value.substring(value.indexOf(":") + 1, value.lastIndexOf(":")));
                        int seconds = Integer.parseInt(value.substring(value.lastIndexOf(":") + 1));
                        dco.setValue(field.getIndex(), Long.valueOf(seconds + (minutes *60) + (hours * 60 * 60)));
                    }
                }
             } else if (field.getFieldType() == ComponentFactory._RATINGCOMBOBOX ||
                        field.getFieldType() == ComponentFactory._FILESIZEFIELD) {
    
                 value = value.replaceAll("\\.", "");
                 
                 try {
                     dco.setValue(field.getIndex(), Long.valueOf(value));
                 } catch (NumberFormatException nfe) {
                     String sValue = ""; 
                     for (char c : value.toCharArray()) {
                         if (Character.isDigit(c))
                             sValue += c;
                         else 
                             break;
                     }
                     
                     if (!Utilities.isEmpty(sValue))
                         dco.setValue(field.getIndex(), Long.valueOf(sValue));
                }
            } else if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                File file = getImagePath(value);
                if (file.exists())
                    dco.setValue(field.getIndex(), new DcImageIcon(file));
            } else if (field.getValueType() == DcRepository.ValueTypes._ICON) {
                File file = getImagePath(value);
                if (file.exists()) {
                    String s = Utilities.fileToBase64String(file);
                    s = Utilities.isEmpty(s) ? Utilities.fileToBase64String(new File(value)) : s;
                    dco.setValue(field.getIndex(), s);
                } else {
                    dco.setValue(field.getIndex(), value);
                }
            } else if (field.getValueType() == DcRepository.ValueTypes._BOOLEAN) {
                dco.setValue(field.getIndex(), Boolean.valueOf(value));
            } else {
                dco.setValue(field.getIndex(), value);
            }
        } catch (Exception e) {
            String message = DcResources.getText("msgErrorWhileSettingValue", new String[] {value, field.getLabel()});
            listener.notifyMessage(message);
            logger.error(message, e);
        }                             
    }
}
