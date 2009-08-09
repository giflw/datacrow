package net.datacrow.console.wizards.migration.itemexport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.migration.itemexport.ItemExporter;
import net.datacrow.core.migration.itemexport.ItemExporterSettings;
import net.datacrow.core.objects.DcObject;

public class ItemExporterDefinition {
    
    private File file;
    private ItemExporterSettings settings = new ItemExporterSettings();
    private ItemExporter exporter;
    private Collection<DcObject> items = new ArrayList<DcObject>();    
    
    public ItemExporterDefinition() {}

    public File getFile() {
        return file;
    }

    public Collection<DcObject> getItems() {
        return items;
    }

    public void setItems(Collection<DcObject> items) {
        this.items = items;
    }

    public void setExporter(ItemExporter exporter) {
        this.exporter = exporter; 
    }
    
    public ItemExporter getExporter() {
        return exporter;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ItemExporterSettings getSettings() {
        return settings;
    }

    public void setSettings(ItemExporterSettings settings) {
        this.settings = settings;
    }
}
