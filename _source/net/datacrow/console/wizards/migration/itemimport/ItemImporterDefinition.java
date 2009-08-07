package net.datacrow.console.wizards.migration.itemimport;

import java.io.File;

import net.datacrow.core.migration.itemimport.ItemImporter;

public class ItemImporterDefinition {
    
    private File file;
    private ItemImporter reader;
    
    public ItemImporterDefinition() {}
    
    public void setReader(ItemImporter reader) {
        this.reader = reader;
    }

    public ItemImporter getReader() {
        return reader;
    }
    
    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

}
