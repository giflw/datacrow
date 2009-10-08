package net.datacrow.console.wizards.migration.moduleimport;

import java.io.File;

public class ImportDefinition {
    
    private File file;
    
    public ImportDefinition() {}

    public File getFile() {
        return file;
    }
    
    public void setFile(String filename) {
        this.file = new File(filename);
    }

    public void setFile(File file) {
        this.file = file;
    }
}
