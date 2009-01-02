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
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.plugin.Plugins;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.core.security.SecurityException;
import net.datacrow.core.services.Servers;
import net.datacrow.core.settings.upgrade.SettingsConversion;
import net.datacrow.core.web.DcWebServer;
import net.datacrow.enhancers.ValueEnhancers;
import net.datacrow.filerenamer.FilePatterns;
import net.datacrow.settings.DcSettings;
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
    private static Version version = new Version(3, 4, 5, 0);
    
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
    
    private static boolean isWebModuleInstalled = false;
    
    public static MainFrame mainFrame;
    private static SplashScreen splashScreen;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        
        boolean nocache = false;
        boolean webserverMode = false; 

        installationDir = System.getenv("DATACROW_HOME");
        
        if (installationDir == null || installationDir.length() == 0)
            installationDir = System.getProperty("user.dir");
        
        String db = null;
        String dir = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].toLowerCase().startsWith("-dir:")) {
                dir = args[i].substring(5, args[i].length());
            } else if (args[i].toLowerCase().startsWith("-db:")) {
                db = args[i].substring(4, args[i].length());
            } else if (args[i].toLowerCase().startsWith("-nocache")) {
                nocache = true;        
            } else if (args[i].toLowerCase().startsWith("-help")) {
                new StartupHelpDialog().setVisible(true);
                System.exit(0);
            } else if (args[i].toLowerCase().startsWith("-webserver")) {
                webserverMode = true;
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
                System.out.println("Starts the web server without starting the Data Crow GUI.");
                System.out.println("Example: java -jar datacrow.jar -webserver");
                System.out.println("");
                System.out.println("-nocache");
                System.out.println("Starts Data Crow without loading the items from the cache. This will cause the items to be loaded from the database (slow).");
                System.out.println("Example: java -jar datacrow.jar -nocache");                
                System.exit(0);
            }
        }
        
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
            checkFolderPermissions();
            
            installLafs();
            initLog4j();
            
            logger.info("Using installation directory: " + installationDir);
            logger.info("Using data directory: " + dataDir);

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
            
            if (DcSettings.getString(DcRepository.Settings.stLanguage) == null ||
                DcSettings.getString(DcRepository.Settings.stLanguage).trim().equals("")) {
                
                SelectLanguageDialog dlg = new SelectLanguageDialog();
                dlg.setVisible(true);
            }
            
            showSplashScreen();
            
            // check if the web module has been installed
            isWebModuleInstalled = new File(DataCrow.webDir, "WEB-INF").exists();
            
            // initialize plugins
            Plugins.getInstance();
            
            // initialize services
            Servers.getInstance();
    
            // Initialize the Component factory
            splashScreen.setStatusMsg("Loading components");
            new ComponentFactory();
            
            Enumeration en = Logger.getRootLogger().getAllAppenders();
            while (en.hasMoreElements()) {
                Appender appender = (Appender) en.nextElement();
                if (appender instanceof TextPaneAppender)
                    ((TextPaneAppender) appender).addListener(LogForm.getInstance());
            }             
    
            logger.info(DcResources.getText("msgApplicationStarts"));
    
            // Initialize all modules
            splashScreen.setStatusMsg(DcResources.getText("msgLoadingModules"));
            new ModuleUpgrade().upgrade();
            DcModules.load();
    
            logger.info(DcResources.getText("msgModulesLoaded"));
    
            ValueEnhancers.initialize();

            // delete lock file
            for (String file : Directory.read(installationDir + "data", false, false, new String[] {"lck"}))
            	new File(file).delete();
            
            // set the database name
            DcSettings.set(DcRepository.Settings.stConnectionString, "dc");
            if (db != null && db.length() > 0)
                DcSettings.set(DcRepository.Settings.stConnectionString, db);
            
            SecurityCentre.getInstance().initialize();
            
            if (DatabaseManager.isLocked()) {
                new MessageBox(DcResources.getText("msgDatabaseIsLocked"), MessageBox._WARNING);
                System.exit(0);
            }
            
            // log in
            login();
            
            // Establish a connection to the database / server
            splashScreen.setStatusMsg(DcResources.getText("msgInitializingDB"));

            DatabaseManager.initialize();
            
            if (nocache)
                DataManager.setUseCache(false);
            
            // convert the settings
            SettingsConversion.convert();
        
            // Start the UI
            if (splashScreen == null)
                showSplashScreen();

            splashScreen.setStatusMsg(DcResources.getText("msgLoadingItems"));
            DcModules.loadData();
            
            if (!webserverMode)
                splashScreen.setStatusMsg(DcResources.getText("msgLoadingUI"));

            // load the filters & patterns
            DataFilters.load();
            FilePatterns.load();
            
            ComponentFactory.setLookAndFeel();
            
            if (!webserverMode) {
                mainFrame = new MainFrame();
                
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        DataCrow.mainFrame.initialize();
                        DataCrow.mainFrame.setVisible(true);
                        mainFrame.setViews();
                    }
                });
            } else {
                
                if (!SecurityCentre.getInstance().getUser().isAuthorized("WebServer")) {
                    new MessageBox(DcResources.getText("msgWebServerStartUnauthorized"), MessageBox._INFORMATION);
                    new ShutdownThread().run();
                    System.exit(0);
                } else {
                    Runtime.getRuntime().addShutdownHook(new ShutdownThread());
                    
                    splashScreen.setStatusMsg(DcResources.getText("msgStartingWebServer"));
                    
                    DcWebServer.getInstance().start();
                    if (DcWebServer.getInstance().isRunning())
                        splashScreen.setStatusMsg(DcResources.getText("msgWebServerStarted"));
                    
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

        if (!webserverMode && DcSettings.getInt(DcRepository.Settings.stXpMode) == -1) {
            SelectExpienceLevelDialog dlg = new SelectExpienceLevelDialog();
            dlg.setVisible(true);
        }
        
        if (!webserverMode && DcSettings.getBoolean(DcRepository.Settings.stShowTipsOnStartup)) {
            TipOfTheDayDialog dlg = new TipOfTheDayDialog();
            dlg.setVisible(true);
        }  
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
    
    private static void login() {
        if (!SecurityCentre.getInstance().unsecureLogin()) {
            boolean success = false;
            int retry = 0;
            while (!success && retry < 3) {
                splashScreen.setVisible(false);
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
                splashScreen.setVisible(true);
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
        splashScreen = new SplashScreen();
        splashScreen.splash();
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
    
    private static void checkFolderPermissions() {
        String dbName = DcSettings.getString(DcRepository.Settings.stConnectionString);
        
        File f = new File(DataCrow.dataDir + dbName + ".script");
        if (!new File(DataCrow.dataDir).exists() || (f.exists() && !f.canWrite())) {
            new NativeMessageBox("Warning", 
                    "Data Crow does not have permissions to modify files in the data directory. " +
                    "This indicates that the user running Data Crow has insufficient permissions. " +
                    "The user running Data Crow must have full control over the Data Crow folder and its sub folders. " +
                    "Please correct this before starting Data Crow again (see the documentation of your operating system).");
            System.exit(0);
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
