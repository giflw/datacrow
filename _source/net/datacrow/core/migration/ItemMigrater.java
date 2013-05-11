package net.datacrow.core.migration;

import java.io.File;

import javax.swing.ImageIcon;

import net.datacrow.core.DcThread;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;

public abstract class ItemMigrater {

    /** Runs the parser in threaded mode  */
    public static final int _MODE_THREADED = 0;
    /** Runs the parser in a non threaded mode  */
    public static final int _MODE_NON_THREADED = 1;

    /** Indicate whether child items should be processed or not. Convenient for container items. */
    protected final boolean processChildren;
    protected final int moduleIdx;
    protected final int mode;
    protected String key;
    protected File file;
    protected DcThread task;
    
    public ItemMigrater(int moduleIdx, String key, int mode, boolean processChildren) throws Exception {
        this.processChildren = processChildren;
        this.moduleIdx = moduleIdx;
        this.key = key;
        this.mode = mode;
    }
    
    public abstract DcThread getTask();
    public abstract String getName();
    protected abstract void initialize() throws Exception;

    /**
     * The icon used to represent this source reader.
     */
    public ImageIcon getIcon() {
        return null;
    }
    
    /**
     * The unique key used to represent this source reader.
     */
    public String getKey() {
        return key;
    }    
    
    /**
     * Prepares this reader. The file is set and initialized.
     * @param file
     * @throws Exception
     */
    public void setFile(File file) throws Exception {
        this.file = file;
        initialize();
    }
    
    public File getFile() {
        return file;
    }    
    
    protected DcModule getModule() {
        return DcModules.get(moduleIdx);
    }
    
    public void start() throws Exception {
        if (task != null && task.isAlive())
            task.cancel();
        
        task = getTask();
        
        if (mode == _MODE_NON_THREADED)
            task.run();
        else
            task.start();
    }
    
    public void cancel() {
        if (task != null) task.cancel();
        task = null;
    }    
}
