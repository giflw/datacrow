package net.datacrow.core.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import net.datacrow.core.DataCrow;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcTemplate;
import net.datacrow.util.Directory;

import org.apache.log4j.Logger;

/**
 * Holder of all plugins. Caches loaded classes and instances.
 * Not threadsafe, should only be called from the Swing thread.
 */
public class Plugins {

    private static Logger logger = Logger.getLogger(Plugins.class.getName());
    private static Plugins instance = new Plugins();
    private final Pattern pattern = Pattern.compile("[\\\\\\/]");

    private final Collection<RegisteredPlugin> registered = new ArrayList<RegisteredPlugin>();
    
    public Plugins() {
        initialize();
    }
    
    private synchronized void initialize() {
        loadPlugins();
    }

    /**
     * Loads the plugin classes with the help of the Plugin Class Loader.
     */
    private void loadPlugins() {
        String check = File.separator + "plugins" + File.separator;
        Object[] params = new Object[] {null, null, -1, -1};
        PluginClassLoader cl = new PluginClassLoader(DataCrow.pluginsDir);
        for (String filename : Directory.read(DataCrow.pluginsDir, true, false, new String[] { "class" })) {
            try {
                String classname = filename.substring(filename.indexOf(check) + 1, filename.lastIndexOf('.'));
                classname = pattern.matcher(classname).replaceAll(".");
                Class<?> clazz = cl.loadClass(classname);
                Plugin plugin = (Plugin) clazz.getConstructors()[0].newInstance(params);
                registered.add(new RegisteredPlugin(clazz, plugin));
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }
    
    public static Plugins getInstance() {
        return instance;
    }
    
    public Collection<RegisteredPlugin> getRegistered() {
        return new ArrayList<RegisteredPlugin>(registered);
    }
    
    public Collection<Plugin> getUserPlugins(DcObject dco, int viewIdx, int moduleIdx) {
        Collection<Plugin> plugins = new ArrayList<Plugin>();
        for (RegisteredPlugin rp : registered) {
            if (!rp.isSystemPlugin()) {
                Object[] params = new Object[] {dco, null,  
                                                Integer.valueOf(viewIdx), 
                                                Integer.valueOf(moduleIdx)};
                plugins.add(getInstance(rp.getClazz(), params));
            }
        }
        return plugins;
    }
    
    public Plugin get(String key, int moduleIdx) throws InvalidPluginException {
        return get(key, null, null, -1, moduleIdx);
    }

    public Plugin get(String key) throws InvalidPluginException {
        return get(key, null, null, -1, -1);
    }
    
    public Plugin get(String key, DcObject dco, DcTemplate template, int viewIdx, int moduleIdx) throws InvalidPluginException {
        
        RegisteredPlugin registeredPlugin = getRegisteredPlugin(key);
        
        if (registeredPlugin == null) {
            logger.error("Could not find plugin " + key);
            throw new InvalidPluginException("Could not find plugin " + key);
        }
        
        Plugin plugin = registeredPlugin.get(dco, template, viewIdx, moduleIdx);
        if (plugin == null) {
            Object[] params = new Object[] {dco, template,  
                                            Integer.valueOf(viewIdx), 
                                            Integer.valueOf(moduleIdx)};
            
            plugin = getInstance(registeredPlugin.getClazz(), params);
            registeredPlugin.add(plugin);
        }
        
        return plugin;
    }

    private Plugin getInstance(Class<?> clazz, Object[] params) {
        try {
            return (Plugin) clazz.getConstructors()[0].newInstance(params);
        } catch (Exception e) {
            logger.error("Could not create plugin for " + clazz, e);
        }
        return null;
    }
    
    private RegisteredPlugin getRegisteredPlugin(String key) {
        for (RegisteredPlugin registeredPlugin : registered) {
            if (registeredPlugin.getKey().equals(key)) 
                return registeredPlugin;
        }
        return null;
    }
}
