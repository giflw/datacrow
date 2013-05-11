package net.datacrow.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.UUID;

import net.datacrow.util.Utilities;

public class DataCrowProperties {
    
    private static final String _USERDIR = "user.home";
    private static final String _CLIENTID = "user.clientid";
    
    private static final File file = new File(System.getProperty("user.home"), "datacrow.properties");
    
    private static Properties properties;
    
    public DataCrowProperties() {
        properties = new Properties();
        
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                properties.load(fis);
                fis.close();
            } catch (Exception e) {
                e.printStackTrace(); // logger not yet available at this stage
            }
        }
        
        initClientID();
        save();
    }
    
    public File getFile() {
        return file;
    }
    
    public void save() {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            properties.store(fos, "Data Crow system settings file. Better to leave it right here.");
            fos.close();
        } catch (Exception e) {
            e.printStackTrace(); // logger not yet available at this stage
        }  
    }
    
    public boolean exists() {
        return file.exists();
    }
    
    private void initClientID() {
        String clientID = properties.getProperty(_CLIENTID);
        if (clientID == null || clientID.trim().equals(""))
            properties.setProperty(_CLIENTID, UUID.randomUUID().toString());
    }
    
    public File getUserDir() {
        String userDir = (String) properties.get(_USERDIR);
        return Utilities.isEmpty(userDir) ? null : new File(userDir);
    }
    
    public void setUserDir(String userDir) {
        properties.setProperty(_USERDIR, userDir);
        save();
    }
    
    public String getClientID() {
        return properties.getProperty(_CLIENTID);
    }
}
