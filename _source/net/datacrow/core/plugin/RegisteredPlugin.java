package net.datacrow.core.plugin;

import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcTemplate;

/**
 * A plugin which has been loaded by Data Crow.
 * 
 * @author Robert Jan van der Waals
 */
public class RegisteredPlugin {
    
    private Class<?> clazz;
    
    private Plugin base;
    private String label;
    
    private Collection<Plugin> cache = new ArrayList<Plugin>();

    /**
     * Creates a new instance.
     * @param clazz
     * @param base
     */
    public RegisteredPlugin(Class<?> clazz, Plugin base) {
        super();
        this.clazz = clazz;
        this.base = base;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getKey() {
        return clazz.getSimpleName();
    }

    public boolean isAdminOnly() {
        return base.isAdminOnly();
    }
    
    public boolean isSystemPlugin() {
        return base.isSystemPlugin();
    }

    public boolean isAuthorizable() {
        return base.isAuthorizable();
    }
    
    public Plugin get(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx) {
        if (dco != null) return null;

        for (Plugin plugin : cache) {
            if (plugin.getTemplate() == template &&
                plugin.getViewIdx() == viewIdx &&
                plugin.getModuleIdx() == moduleIdx)
                return plugin;
        }
        return null;
    }
    
    public void add(Plugin plugin) {
        if (plugin.getItem() == null)
            cache.add(plugin);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
