package net.datacrow.console.wizards.itemexport;

import java.io.File;

import net.datacrow.core.migration.itemexport.ItemExporter;
import net.datacrow.core.migration.itemexport.ItemExporterSettings;

public class ItemExporterDefinition {
    
    private File file;
    private ItemExporterSettings settings = new ItemExporterSettings();
    private ItemExporter exporter;
    
    public ItemExporterDefinition() {}

    public File getFile() {
        return file;
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
