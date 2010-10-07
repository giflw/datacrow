package net.datacrow.core.migration.itemimport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.migration.ItemMigrater;
import net.datacrow.core.objects.DcObject;

import org.apache.log4j.Logger;

public class ItemImporterHelper implements IItemImporterClient {

    private static Logger logger = Logger.getLogger(ItemImporterHelper.class.getName());
    
    private Collection<DcObject> items = new ArrayList<DcObject>();
    private File file;
    private ItemImporter reader;
    
    public ItemImporterHelper(String type, int moduleIdx, File file) throws Exception {
        this.file = file;
        this.reader = ItemImporters.getInstance().getImporter(type, moduleIdx, ItemMigrater._MODE_NON_THREADED);
        this.reader.setClient(this);
    }

    public void setSetting(String key, String value) {
        reader.setSetting(key, value);
    }
    
    public void start() throws Exception {
        if (reader != null) {
            reader.setFile(file);
            reader.start();
        } else {
            logger.error("No source reader found for " + file);
        }
    }
    
    public Collection<DcObject> getItems() {
        return items;
    }
    
    public void clear() {
        items = null;
        file = null;
        reader = null;
    }
    
    @Override
    public void notifyMessage(String message) {}

    @Override
    public void notifyProcessed(DcObject item) {
        items.add(item);
    }

    @Override
    public void notifyStarted(int count) {}

    @Override
    public void notifyStopped() {}
}
