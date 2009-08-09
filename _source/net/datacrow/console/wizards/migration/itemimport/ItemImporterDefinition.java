package net.datacrow.console.wizards.migration.itemimport;

import java.io.File;

import net.datacrow.core.migration.itemimport.ItemImporter;

public class ItemImporterDefinition {
    
    private File file;
    private ItemImporter importer;
    
    public ItemImporterDefinition() {}
    
    public void setImporter(ItemImporter importer) {
        this.importer = importer;
    }

    public ItemImporter getImporter() {
        return importer;
    }
    
    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

}
