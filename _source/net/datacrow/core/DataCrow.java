/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
 *                                                                            *
 *                       This file is part of Data Crow.                      *
 *       Data Crow is free software; you can redistribute it and/or           *
 *        modify it under the terms of the GNU General Public                 *
 *       License as published by the Free Software Foundation; either         *
 *              version 3 of the License, or any later version.               *
 *                                                                            *
 *        Data Crow is distributed in the hope that it will be useful,        *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *           MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.             *
 *           See the GNU General Public License for more details.             *
 *                                                                            *
 *        You should have received a copy of the GNU General Public           *
 *  License along with this program. If not, see http://www.gnu.org/licenses  *
 *                                                                            *
 ******************************************************************************/

package net.datacrow.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.MainFrame;
import net.datacrow.console.windows.LogForm;
import net.datacrow.console.windows.SelectExpienceLevelDialog;
import net.datacrow.console.windows.SelectLanguageDialog;
import net.datacrow.console.windows.SplashScreen;
import net.datacrow.console.windows.TipOfTheDayDialog;
import net.datacrow.console.windows.help.StartupHelpDialog;
import net.datacrow.console.windows.loan.LoanInformationForm;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.windows.messageboxes.NativeMessageBox;
import net.datacrow.console.windows.security.LoginDialog;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.upgrade.ModuleUpgrade;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.plugin.Plugins;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.core.security.SecurityException;
import net.datacrow.core.services.Servers;
import net.datacrow.core.web.DcWebServer;
import net.datacrow.enhancers.ValueEnhancers;
import net.datacrow.filerenamer.FilePatterns;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.util.Directory;
import net.datacrow.util.MemoryMonitor;
import net.datacrow.util.Utilities;
import net.datacrow.util.logging.TextPaneAppender;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * This is the starting point of the application.
 * 
 * @author Robert Jan van der Waals
 */
public class DataCrow {

    private static Logger logger = Logger.getLogger(DataCrow.class.getName());
    
    private static Platform platform = new Platform();
    private static Version version = new Version(3, 4, 16, 0);
    
    public static String installationDir;
    public static String imageDir;
    public static String moduleDir;
    public static String reportDir;
    public static String pluginsDir;
    public static String servicesDir;
    public static String webDir;
    public static String dataDir;
    public static String cacheDir;
    public static String resourcesDir;
    
    private static boolean noSplash = false;
    private static boolean isWebModuleInstalled = false;
    
    public static MainFrame mainFrame;
    private static SplashScreen splashScreen;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        
        long totalstart = logger.isDebugEnabled() ? new Date().getTime() : 0;
        
        boolean nocache = false;
        boolean webserverMode = false; 
        
        String password = null;
        String username = null;

        installationDir = System.getenv("DATACROW_HOME");
        
        if (installationDir == null || installationDir.length() == 0)
            installationDir = getInstallationDirDeprecated();
        
        String db = null;
        String dir = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].toLowerCase().startsWith("-dir:")) {
                dir = args[i].substring(5, args[i].length());
            } else if (args[i].toLowerCase().startsWith("-db:")) {
                db = args[i].substring("-db:".length());
            } else if (args[i].toLowerCase().startsWith("-nocache")) {
                nocache = true;        
            } else if (args[i].toLowerCase().startsWith("-help")) {
                new StartupHelpDialog().setVisible(true);
                System.exit(0);
            } else if (args[i].toLowerCase().startsWith("-nosplash")) {
                noSplash = true;
            } else if (args[i].toLowerCase().startsWith("-webserver")) {
                webserverMode = true;
            } else if (args[i].toLowerCase().startsWith("-credentials:")) {
                
                String credentials = args[i].substring("-credentials:".length());
                int index = credentials.indexOf("/");
                username = index > -1 ? credentials.substring(0, index) : credentials;
                password = index > -1 ? credentials.substring(index + 1) : "";
                
            } else if (dir != null) {
                dir += " " + args[i];
            } else { 
                System.out.println("The following optional parameters can be used:");
                System.out.println("");
                System.out.println("-dir:<installdir>");
                System.out.println("Specifies the installation directory.");
                System.out.println("Example: java -jar datacrow.jar -dir:d:/datacrow");
                System.out.println("");
                System.out.println("-db:<databasename>");
                System.out.println("Forces Data Crow to use an alternative database.");
                System.out.println("Example: java -jar datacrow.jar -db:testdb");
                System.out.println("");
                System.out.println("-webserver");
                System.out.println("Starts the web server without starting the Data Crow GUI. Specify -credentials to avoid the login dialog.");
                System.out.println("Example: java -jar datacrow.jar -webserver");
                System.out.println("");
                System.out.println("-nocache");
                System.out.println("Starts Data Crow without loading the items from the cache. This will cause the items to be loaded from the database (slow).");
                System.out.println("Example: java -jar datacrow.jar -nocache");                
                System.out.println("");
                System.out.println("-credentials:username/password");
                System.out.println("Specify the login credentials to start Data Crow without displaying the login dialog.");
                System.out.println("Example (username and password): java -jar datacrow.jar -credentials:sa/12345");                
                System.out.println("Example (username without a password): java -jar datacrow.jar -credentials:sa");
                System.exit(0);
            }
        }
        
        noSplash = !noSplash ? webserverMode && username != null : noSplash;
        
        installationDir = dir != null ? dir : installationDir;
        
        if (installationDir == null || installationDir.trim().length() == 0) {
            new NativeMessageBox("Warning", "The installation directory could not be determined. " +
                    "Please set the DATACROW_HOME environment variable or supply the -dir:<installation directory> parameter. " +
                    "The DATACROW_HOME variable value should point to the Data Crow intallation directory.");
            return;
        }
        
        DataCrow.installationDir = DataCrow.installationDir.replaceAll("\\\\", "/");
        DataCrow.installationDir += !DataCrow.installationDir.endsWith("\\") && !DataCrow.installationDir.endsWith("/") ? "/" : "";
        
        try {
            checkCurrentDir();
            createDirectories();
            initLog4j();
            
            installLafs();
            
            logger.info("Using installation directory: " + installationDir);
            logger.info("Using data directory: " + dataDir);
            logger.info("Using images directory: " + imageDir);
            logger.info("Using cache directory: " + cacheDir);
            
            // upgrade purposes (version 3.4.3 and older)
            if (new File(DataCrow.dataDir + "resources.properties").exists()) {
                try {
                    Utilities.rename(new File(DataCrow.dataDir + "resources.properties"), new File(DataCrow.resourcesDir + "Custom_resources.properties"));
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
            
            // load resources
            new DcResources();
            
            // load the settings
            new DcSettings();
            
            checkPlatform();
            
            if (DcSettings.getString(DcRepository.Settings.stLanguage) == null ||
                DcSettings.getString(DcRepository.Settings.stLanguage).trim().equals("")) {
                
                final java.util.concurrent.atomic.AtomicBoolean activeDialog = new java.util.concurrent.atomic.AtomicBoolean(true);
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        SelectLanguageDialog dlg = new SelectLanguageDialog(activeDialog);
                        dlg.setVisible(true);
                    }
                });
                
                synchronized (activeDialog) {
                    while (activeDialog.get() == true)
                        activeDialog.wait();
                }
            }
            
            showSplashScreen();
            
            // check if the web module has been installed
            isWebModuleInstalled = new File(DataCrow.webDir, "WEB-INF").exists();
            
            // initialize plugins
            Plugins.getInstance();
            
            // initialize services
            Servers.getInstance();
    
            // Initialize the Component factory
            showSplashMsg("Loading components");
            new ComponentFactory();
            
            Enumeration en = Logger.getRootLogger().getAllAppenders();
            while (en.hasMoreElements()) {
                Appender appender = (Appender) en.nextElement();
                if (appender instanceof TextPaneAppender)
                    ((TextPaneAppender) appender).addListener(LogForm.getInstance());
            }             
    
            logger.info(DcResources.getText("msgApplicationStarts"));
    
            // Initialize all modules
            showSplashMsg(DcResources.getText("msgLoadingModules"));

            long start = logger.isDebugEnabled() ? new Date().getTime() : 0;
            new ModuleUpgrade().upgrade();
            if (logger.isDebugEnabled()) {
                long end = new Date().getTime();
                logger.debug("Upgrading the modules took " + (end - start) + "ms");
            }  
    
            start = logger.isDebugEnabled() ? new Date().getTime() : 0;
            DcModules.load();
            if (logger.isDebugEnabled()) {
                long end = new Date().getTime();
                logger.debug("Loading the modules took " + (end - start) + "ms");
            }  
            
            logger.info(DcResources.getText("msgModulesLoaded"));
    
            ValueEnhancers.initialize();

            // delete lock file
            for (String file : Directory.read(installationDir + "data", false, false, new String[] {"lck"}))
            	new File(file).delete();
            
            // set the database name
            DcSettings.set(DcRepository.Settings.stConnectionString, "dc");
            if (db != null && db.length() > 0)
                DcSettings.set(DcRepository.Settings.stConnectionString, db);
            
            start = logger.isDebugEnabled() ? new Date().getTime() : 0;
            SecurityCentre.getInstance().initialize();
            if (logger.isDebugEnabled()) {
                long end = new Date().getTime();
                logger.debug("Initilization of the security center took " + (end - start) + "ms");
            } 
            
            if (DatabaseManager.isLocked()) {
                new MessageBox(DcResources.getText("msgDatabaseIsLocked"), MessageBox._WARNING);
                System.exit(0);
            }
            
            // log in
            login(username, password);
            
            // Establish a connection to the database / server
            showSplashMsg(DcResources.getText("msgInitializingDB"));

            DatabaseManager.initialize();
            
            if (nocache)
                DataManager.setUseCache(false);
            
            // Start the UI
            if (splashScreen == null)
                showSplashScreen();

            showSplashMsg(DcResources.getText("msgLoadingItems"));
            DcModules.loadData();

            checkTabs();
            
            if (!webserverMode)
                showSplashMsg(DcResources.getText("msgLoadingUI"));

            // load the filters & patterns
            DataFilters.load();
            FilePatterns.load();
            
            ComponentFactory.setLookAndFeel();
            
            if (!webserverMode) {
                mainFrame = new MainFrame();
                
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {

                        long start = logger.isDebugEnabled() ? new Date().getTime() : 0;

                        DataCrow.mainFrame.initialize();
                        DataCrow.mainFrame.setVisible(true);
                        mainFrame.setViews();
                        
                        if (logger.isDebugEnabled()) {
                            long end = new Date().getTime();
                            logger.debug("Initilization of the UI took " + (end - start) + "ms");
                        } 

                    }
                });
            } else {
                
                if (!SecurityCentre.getInstance().getUser().isAuthorized("WebServer")) {
                    new MessageBox(DcResources.getText("msgWebServerStartUnauthorized"), MessageBox._INFORMATION);
                    new ShutdownThread().run();
                    System.exit(0);
                } else {
                    Runtime.getRuntime().addShutdownHook(new ShutdownThread());
                    
                    showSplashMsg(DcResources.getText("msgStartingWebServer"));
                    
                    DcWebServer.getInstance().start();
                    if (DcWebServer.getInstance().isRunning())
                        showSplashMsg(DcResources.getText("msgWebServerStarted"));
                    
                    System.out.println(DcResources.getText("msgCloseWebServerConsole"));
                }
            }
            
            MemoryMonitor monitor = new MemoryMonitor();
            monitor.start();

            Thread splashCloser = new Thread(new SplashScreenCloser());
            splashCloser.start();
            
            if (!webserverMode) {
                DataFilter df = new DataFilter(DcModules._LOAN);
                df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._B_ENDDATE, Operator.IS_EMPTY, null));
                df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._E_DUEDATE, Operator.IS_FILLED, null));
                
                for (DcObject loan : DataManager.get(DcModules._LOAN, df)) {
                    Long overdue = ((Loan) loan).getDaysTillOverdue();
                	if (overdue != null && overdue.longValue() < 0) {
                		new MessageBox(DcResources.getText("msgThereAreOverdueItems"), MessageBox._WARNING);
                		new LoanInformationForm().setVisible(true);
                		break;
                	}
                }
            }
            
            DcSettings.set(DcRepository.Settings.stGracefulShutdown, Boolean.FALSE);
            DcSettings.save();
            
        } catch (Exception e) {
            logger.fatal("Severe error occurred while starting Data Crow. The application cannot continue.", e);
            e.printStackTrace();
            new NativeMessageBox("Error", e.getMessage());
            System.exit(0);
        }
        
        new FreeResourcesTask();

        int xp = DcSettings.getInt(DcRepository.Settings.stXpMode);
        if (!webserverMode && xp == -1) {
            SelectExpienceLevelDialog dlg = new SelectExpienceLevelDialog();
            dlg.setVisible(true);
        }
        
        if (!webserverMode && DcSettings.getBoolean(DcRepository.Settings.stShowTipsOnStartup)) {
            TipOfTheDayDialog dlg = new TipOfTheDayDialog();
            dlg.setVisible(true);
        }  
        
        if (logger.isDebugEnabled()) {
            long end = new Date().getTime();
            logger.debug("Total startup time was " + (end - totalstart) + "ms");
        }  
    }
    
    private static void checkTabs() {
        for (DcModule module : DcModules.getAllModules()) {
            
            if (module.getFieldDefinitions() == null ||!module.isTopModule()) continue;
            
            Collection<String> tabs = new ArrayList<String>();
            for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
                String tab = definition.getTab(module.getIndex());
                if (!Utilities.isEmpty(tab) && !tabs.contains(tab))
                    tabs.add(tab);
            }
            
            for (String tab : tabs)
                DataManager.checkTab(module.getIndex(), tab);
        }
    }
    
    private static void showSplashMsg(String msg) {
        if (!noSplash)
            splashScreen.setStatusMsg(msg);
    }
    
    /**
     * The current product version.
     */
    public static Version getVersion() {
        return version;
    }

    /**
     * Is the web module installed or not? 
     */
    public static boolean isWebModuleInstalled() {
        return isWebModuleInstalled; 
    }
    
    private static void login(String username, String password) {
        // use the login dialog method
        if (username == null) {
            if (!SecurityCentre.getInstance().unsecureLogin()) {
                boolean success = false;
                int retry = 0;
                while (!success && retry < 3) {
                    showSplashScreen(false);
                    LoginDialog dlg = new LoginDialog();
                    dlg.setVisible(true);
                    
                    if (dlg.isCanceled()) break;
                    
                    try {
                        success = SecurityCentre.getInstance().login(dlg.getLoginName(), dlg.getPassword(), false) != null;
                    } catch (SecurityException se) {
                        new MessageBox(se.getMessage(), MessageBox._INFORMATION);
                        retry ++;
                    }
                }
                
                if (!success) 
                    System.exit(0);
                else
                    showSplashScreen(true);
            }
        // use the blunt message
        } else {
            try {
                SecurityCentre.getInstance().login(username, password, false);
            } catch (SecurityException se) {
                logger.info(se, se);
                System.out.println(se.getMessage());
                System.exit(0);
            }
        }
    }
    
    /**
     * Retrieves the platform information (OS).
     */
    public static Platform getPlatform() {
        return platform;
    }
    
    /**
     * Is the splash screen currently being shown?
     */
    public static boolean isSplashScreenActive() {
        return splashScreen != null;
    }

    /**
     * Hides or shows the splash screen.
     */
    public static void showSplashScreen(boolean b) {
        if (splashScreen != null) {
            splashScreen.setVisible(b);
            if (b) splashScreen.toFront();
        }
    }

    /**
     * Creates and shows the splash screen.
     */
    public static void showSplashScreen(){
        if (!noSplash) {
            splashScreen = new SplashScreen();
            splashScreen.splash();
        }
    }

    /**
     * Closes the splash screen.
     */
    public static void closeSplashScreen() {
        if (splashScreen != null) {
            splashScreen.dispose();
            splashScreen = null;
        }
    }

    /**
     * Removes the focus from the splash screen.
     */
    public static void moveSplashScreenToBack() {
        if (splashScreen != null) splashScreen.toBack();
    }

    /**
     * Pushes the splash screen to the front. 
     */
    public static void moveSplashScreenToFront() {
        if (splashScreen != null) splashScreen.toFront();
    }

    private static final class SplashScreenCloser implements Runnable {
        public void run() {
            if (splashScreen != null) 
                splashScreen.dispose();
                
            splashScreen = null;
        }
    }
    
    /**
     * Checks whether the specified installation directory is valid.
     */
    private static void checkCurrentDir() {
        if (!new File(DataCrow.installationDir + "plugins").exists() ||
            !new File(DataCrow.installationDir + "modules").exists()) {
        
            new NativeMessageBox("Warning", 
                    "The installation directory could not be determined. " +
                    "Please set the DATACROW_HOME environment variable or supply the -dir:<installation directory> parameter. " +
                    "The DATACROW_HOME variable value should point to the Data Crow intallation directory.");
            
            System.exit(0);
        }
    }
    
    private static String getInstallationDirDeprecated() {
       String classLocation = DataCrow.class.getName().replace('.', '/') + ".class";
        ClassLoader loader = DataCrow.class.getClassLoader();
        
        URL location;
        if (loader == null)
            location = ClassLoader.getSystemResource(classLocation);
        else
            location = loader.getResource(classLocation);

        String dir = location.getFile();

        dir = dir.substring(0, dir.indexOf("/net/"));
        dir = dir.endsWith("_build") ? dir.substring(0, dir.indexOf("_build")) : dir;
        dir = dir.endsWith("_classes") ? dir.substring(0, dir.indexOf("_classes")) : dir;
        dir = dir.endsWith("classes") ? dir.substring(0, dir.indexOf("classes")) : dir;
        dir = dir.indexOf("webapp") > 0 ? dir.substring(0, dir.indexOf("webapp")) : dir;
        dir = dir.indexOf("datacrow.jar") > 0 ? dir.substring(0, dir.indexOf("datacrow.jar")) : dir;
        dir = dir.replaceAll("%20", " ");
        dir = dir.startsWith("file:") ? dir.substring(5) : dir;
        
        return dir;
    }

    /** 
     * Determines the current directory.
     */
    private static void createDirectories() {
        dataDir = DataCrow.installationDir + "data/";
        webDir = DataCrow.installationDir + "webapp/datacrow/";
        imageDir = DataCrow.installationDir + "webapp/datacrow/mediaimages/";
        reportDir = DataCrow.installationDir + "reports/";
        moduleDir = DataCrow.installationDir + "modules/";
        pluginsDir = DataCrow.installationDir + "plugins/";
        servicesDir = DataCrow.installationDir + "services/";
        cacheDir = DataCrow.installationDir + "data/cache/";
        resourcesDir = DataCrow.installationDir + "resources/";
        
        DataCrow.createDirectory(new File(dataDir), "data");
        DataCrow.createDirectory(new File(cacheDir), "cache");
        DataCrow.createDirectory(new File(installationDir + "data/temp"), "temp");
        DataCrow.createDirectory(new File(imageDir), "images");
        DataCrow.createDirectory(new File(reportDir), "reports");
        DataCrow.createDirectory(new File(servicesDir), "services");
    }
    
    private static void createDirectory(File dir, String name) {

        dir.mkdirs();
        
        if (!dir.exists()) {
            String message = "Data Crow was unable to create the " + name + " directory. " +
                "This indicates that the user running Data Crow has insufficient permissions. " +
                "The user running Data Crow must have full control over the Data Crow folder " +
                "and all if its sub directories.";
    
            new NativeMessageBox("Warning", message);
            System.exit(0);
        }
        
        File file = new File(dir, "temp.txt");
        try {
        
            if (!file.exists())
                file.createNewFile();
            
            if (!file.exists() || !file.canWrite()) 
                throw new IOException("File cannot be created in directory " + dir);

        } catch (IOException e) {
            new NativeMessageBox("Warning", 
                    "Data Crow does not have permissions to modify files in the " + name + " directory. " +
                    "This indicates that the user running Data Crow has insufficient permissions. " +
                    "The user running Data Crow must have full control over the Data Crow folder and all of its sub folders. " +
                    "Please correct this before starting Data Crow again (see the documentation of your operating system).");
        } finally {
            if (!name.equals("data"))
                file.delete();
        }
    }
    
    private static void initLog4j() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(DataCrow.installationDir + "log4j.properties"));
            properties.setProperty("log4j.appender.logfile.File", DataCrow.dataDir + "data_crow.log");
            properties.store(new FileOutputStream(DataCrow.installationDir + "log4j.properties"), "");
        } catch (Exception e) {
            logger.error("Could not find the log4j properties file.", e);
        }
        
        PropertyConfigurator.configure(DataCrow.installationDir + "log4j.properties");        
    }
    
    private static void checkPlatform() {
        logger.info(version.getFullString());
        logger.info("Java version: " + System.getProperty("java.version"));
        logger.info("Java vendor: " + System.getProperty("java.vendor"));
        logger.info("Operating System: " + System.getProperty("os.name"));
        
        boolean alreadyChecked = DcSettings.getBoolean(DcRepository.Settings.stCheckedForJavaVersion);
        if (!getPlatform().isJavaSun() && !alreadyChecked) {
            new NativeMessageBox("Warning", "Data Crow has only been tested on Java from Sun (version 1.5 or higher). " +
            		"Make sure the latest Java version from Sun has been installed. You are currently using the Java version from " + System.getProperty("java.vendor") + " " +
            		"Data Crow will now continue and will not display this message again. Upgrade your Java version in case Data Crow does not continue (hangs) or " +
            		"if you experience any other kind of malfunction.");
            
            DcSettings.set(DcRepository.Settings.stCheckedForJavaVersion, Boolean.TRUE);
        }
    }
    
    private static void installLafs() {
        UIManager.installLookAndFeel("JTattoo - Smart", "com.jtattoo.plaf.smart.SmartLookAndFeel");
        UIManager.installLookAndFeel("JTattoo - Acryl", "com.jtattoo.plaf.acryl.AcrylLookAndFeel");
        UIManager.installLookAndFeel("JTattoo - Aero", "com.jtattoo.plaf.aero.AeroLookAndFeel");
        UIManager.installLookAndFeel("JTattoo - Aluminium", "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
        UIManager.installLookAndFeel("JTattoo - Bernstein", "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
        UIManager.installLookAndFeel("JTattoo - Fast", "com.jtattoo.plaf.fast.FastLookAndFeel");
        UIManager.installLookAndFeel("JTattoo - HiFi", "com.jtattoo.plaf.hifi.HiFiLookAndFeel");
        UIManager.installLookAndFeel("JTattoo - McWin", "com.jtattoo.plaf.mcwin.McWinLookAndFeel");
        UIManager.installLookAndFeel("JTattoo - Mint", "com.jtattoo.plaf.mint.MintLookAndFeel");
        UIManager.installLookAndFeel("JTattoo - Noire", "com.jtattoo.plaf.mint.MintLookAndFeel");
        UIManager.installLookAndFeel("JTattoo - Luna", "com.jtattoo.plaf.luna.LunaLookAndFeel");
    }
    
    private static class ShutdownThread extends Thread {
        
        @Override
        public void run() {
            System.out.println(DcResources.getText("msgClosingWebServer"));
            
            try {
                if (DcWebServer.getInstance().isRunning())
                    DcWebServer.getInstance().stop();
            } catch (Exception e) {
                logger.error(e, e);
            }
            
            System.out.println(DcResources.getText("msgClosingDB"));
            DataManager.serialize();
            DatabaseManager.closeDatabases(false);
            System.out.println(DcResources.getText("msgShutdownComplete"));
        }
    }    
}
