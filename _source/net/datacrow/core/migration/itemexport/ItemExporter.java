package net.datacrow.core.migration.itemexport;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Collection;

import net.datacrow.core.migration.ItemMigrater;
import net.datacrow.core.objects.DcObject;

public abstract class ItemExporter extends ItemMigrater {
    
    protected Collection<DcObject> items;
    protected BufferedOutputStream bos;

    protected IItemExporterClient client;
    protected ItemExporterSettings settings;
    
    protected boolean success = true;
    
    public ItemExporter(int moduleIdx, String key, int mode) throws Exception {
        super(moduleIdx, key, mode);
    }

    @Override
    public void start() throws Exception {
        client.notifyStarted(items.size());
        success = true;
        super.start();
    }

    public void setClient(IItemExporterClient client) {
        this.client = client;
    }
    
    public void setItems(Collection<DcObject> items) {
        this.items = items;
    }

    public void setSettings(ItemExporterSettings properties) {
        this.settings = properties;
    }

    @Override
    protected void initialize() throws Exception {
        bos = new BufferedOutputStream(new FileOutputStream(file));
    }    
    
    public boolean isSuccessfull() {
        return success;
    }
    
    /**
     * The file type.
     * @return File extension.
     */
    public abstract String getFileType();
}
