package net.datacrow.console.wizards.migration.itemexport;

import java.io.File;

import net.datacrow.core.migration.itemexport.ItemExporterSettings;

public class ItemExporterDefinition {
    
    private File file;
    private ItemExporterSettings settings = new ItemExporterSettings();
    
    public ItemExporterDefinition() {}

    public File getFile() {
        return file;
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
