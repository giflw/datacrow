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
import net.datacrow.console.windows.DonateDialog;
import net.datacrow.console.windows.SelectExpienceLevelDialog;
import net.datacrow.console.windows.SelectLanguageDialog;
import net.datacrow.console.windows.SplashScreen;
import net.datacrow.console.windows.TipOfTheDayDialog;
import net.datacrow.console.windows.UserDirSetupDialog;
import net.datacrow.console.windows.drivemanager.DriveManagerDialog;
import net.datacrow.console.windows.help.StartupHelpDialog;
import net.datacrow.console.windows.loan.LoanInformationForm;
import net.datacrow.console.windows.log.LogPanel;
import net.datacrow.console.windows.messageboxes.NativeMessageBox;
import net.datacrow.console.windows.security.LoginDialog;
import net.datacrow.console.wizards.tool.ToolSelectWizard;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.db.upgrade.UpgradeHsqlEngine;
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
import net.datacrow.drivemanager.DriveManager;
import net.datacrow.enhancers.ValueEnhancers;
import net.datacrow.filerenamer.FilePatterns;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.Directory;
import net.datacrow.util.SystemMonitor;
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

    private static Logger logger;

    private static SplashScreen splashScreen;
    
    private static Platform platform = new Platform();
    private static Version version = new Version(3, 9, 21, 0);
    
    public static String installationDir;
    public static String imageDir;
    public static String iconsDir;
    public static String moduleDir;
    public static String reportDir;
    public static String pluginsDir;
    public static String servicesDir;
    public static String webDir;
    public static String databaseDir;
    public static String moduleSettingsDir;
    public static String applicationSettingsDir;
    public static String userDir;
    public static String resourcesDir;
    
    private static boolean initialized = false;
    private static boolean debug = false;
    private static boolean noSplash = false;
    private static boolean isWebModuleInstalled = false;
    
    public static boolean loadSettings = true;
    public static MainFrame mainFrame;

    public static void main(String[] args) {
        try {
            boolean webserverMode = false; 
            
            String password = null;
            String username = null;
    
            // legacy stuff but it still works..
            installationDir = System.getenv("DATACROW_HOME");
            
            if (installationDir == null || installationDir.length() == 0)
                installationDir = getInstallationDirDeprecated();
            
            String db = null;
            String pInstallationDir = null;
            String pUserDir = null;
            boolean determiningDir = false;
            boolean determiningUserDir = false;
            for (int i = 0; i < args.length; i++) {
                if (args[i].toLowerCase().startsWith("-dir:")) {
                    pInstallationDir = args[i].substring(5, args[i].length());
                    determiningDir = true;
                    determiningUserDir = false;
                } else if (args[i].toLowerCase().startsWith("-userdir:")) {
                    pUserDir = args[i].substring("-userdir:".length(), args[i].length());
                    determiningUserDir = true;
                    determiningDir = false;
                } else if (args[i].toLowerCase().startsWith("-db:")) {
                    db = args[i].substring("-db:".length());
                } else if (args[i].toLowerCase().startsWith("-help")) {
                    StartupHelpDialog dialog = new StartupHelpDialog();
                    DcSwingUtilities.openDialogNativeModal(dialog);
                    System.exit(0);
                } else if (args[i].toLowerCase().startsWith("-debug")) {
                    debug = true;
                } else if (args[i].toLowerCase().startsWith("-nosplash")) {
                    noSplash = true;
                } else if (args[i].toLowerCase().startsWith("-webserver")) {
                    webserverMode = true;
                } else if (args[i].toLowerCase().startsWith("-clearsettings")) {
                    loadSettings = false;
                } else if (args[i].toLowerCase().startsWith("-credentials:")) {
                    String credentials = args[i].substring("-credentials:".length());
                    int index = credentials.indexOf("/");
                    username = index > -1 ? credentials.substring(0, index) : credentials;
                    password = index > -1 ? credentials.substring(index + 1) : "";
                } else if (determiningDir) {
                    pInstallationDir += " " + args[i];
                } else if (determiningUserDir) {
                    pUserDir += " " + args[i];                    
                } else { 
                    System.out.println("The following optional parameters can be used:");
                    System.out.println("");
                    System.out.println("-dir:<installdir>");
                    System.out.println("Specifies the installation directory.");
                    System.out.println("Example: java -jar datacrow.jar -dir:d:/datacrow");
                    System.out.println("");
                    System.out.println("-userdir:<userdir>");
                    System.out.println("Specifies the user directory. Start the name with a dot (.) to make the path relative to the installation folder.");
                    System.out.println("Example: java -jar datacrow.jar -userdir:d:/datacrow");
                    System.out.println("");                    
                    System.out.println("-db:<databasename>");
                    System.out.println("Forces Data Crow to use an alternative database.");
                    System.out.println("Example: java -jar datacrow.jar -db:testdb");
                    System.out.println("");
                    System.out.println("-webserver");
                    System.out.println("Starts the web server without starting the Data Crow GUI. Specify -credentials to avoid the login dialog.");
                    System.out.println("Example: java -jar datacrow.jar -webserver");
                    System.out.println("");
                    System.out.println("-debug");
                    System.out.println("Debug mode for additional logging information.");
                    System.out.println("Example: java -jar datacrow.jar -debug");                
                    System.out.println("");
                    System.out.println("-clearsettings");
                    System.out.println("Loads the default Data Crow settings. Disgards all user settings.");
                    System.out.println("Example: java -jar datacrow.jar -clearsettings");                
                    System.out.println("");
                    System.out.println("-credentials:username/password");
                    System.out.println("Specify the login credentials to start Data Crow without displaying the login dialog.");
                    System.out.println("Example (username and password): java -jar datacrow.jar -credentials:sa/12345");                
                    System.out.println("Example (username without a password): java -jar datacrow.jar -credentials:sa");
                    System.exit(0);
                }
            }
            
            noSplash = !noSplash ? webserverMode && username != null : noSplash;
            
            installationDir = pInstallationDir != null ? pInstallationDir : installationDir;
            
            if (installationDir == null || installationDir.trim().length() == 0) {
                NativeMessageBox dialog = new NativeMessageBox("Warning", 
                        "The installation directory could not be determined. " +
                        "Please set the DATACROW_HOME environment variable or supply the -dir:<installation directory> parameter. " +
                        "The DATACROW_HOME variable value should point to the Data Crow intallation directory.");
                DcSwingUtilities.openDialogNativeModal(dialog);
                return;
            }
            
            DataCrow.installationDir = DataCrow.installationDir.replaceAll("\\\\", "/");
            DataCrow.installationDir += !DataCrow.installationDir.endsWith("\\") && !DataCrow.installationDir.endsWith("/") ? "/" : "";
            
            long totalstart = 0;
            
            DataCrow.userDir = pUserDir != null ? pUserDir : DataCrow.userDir;
            
            if (DataCrow.userDir != null && (DataCrow.userDir.startsWith("./") || DataCrow.userDir.startsWith(".\\"))) {
                DataCrow.userDir = DataCrow.installationDir + DataCrow.userDir.substring(2);
                DataCrow.userDir = DataCrow.installationDir.replaceAll("\\\\", "/");
            }
            
            try {
                
                boolean userFolderExists = DataCrow.userDir != null;

                File userHome = new File(System.getProperty("user.home"));
                File userDirSettings = new File(userHome, "datacrow.properties");
                
                if (!userFolderExists && userDirSettings.exists()) {
                    Properties properties = new Properties();
                    properties.load(new FileInputStream(userDirSettings));
                    String userDirSetting =  (String) properties.get("userfolder");
                    
                    if (new File(userDirSetting).exists()) {
                        userDir = userDirSetting;
                        userDir += userDir.endsWith("/") || userDir.endsWith("\\") ? "" : "/";
                        userFolderExists = true;
                    }
                } 
                
                if (!userFolderExists) {
                    moduleDir = DataCrow.installationDir + "modules/";
                    pluginsDir = DataCrow.installationDir + "plugins/";
                    servicesDir = DataCrow.installationDir + "services/";
                    resourcesDir = DataCrow.installationDir + "resources/";
                    reportDir = DataCrow.installationDir + "reports/";
                    userDir = DataCrow.installationDir + "data/";
                    databaseDir  = DataCrow.installationDir + "data/";
                    applicationSettingsDir = databaseDir  = DataCrow.installationDir + "data/";
                    imageDir = DataCrow.installationDir + "webapp/datacrow/mediaimages";
                    
                    new DcSettings();
                    
                    UserDirSetupDialog dlg = new UserDirSetupDialog(args);
                    dlg.setVisible(true);
                } else {
                    checkCurrentDir();
                    createDirectories();
                    
                    initLog4j();
                    
                    logger = Logger.getLogger(DataCrow.class.getName());
                    totalstart = logger.isDebugEnabled() ? new Date().getTime() : 0;                
                    
                    installLafs();
                    
                    File installDir = new File(DataCrow.installationDir);
                    if (pUserDir != null && installDir.equals(new File(DataCrow.userDir))) {
                        DcSwingUtilities.displayMessage(
                                "The installation directory can't selected as the user folder. " +
                                "You CAN select a sub folder within the installation folder though.");
                        
                        System.exit(0);
                    }
                    
                    logger.info(new Date() + " Starting Data Crow.");
                    
                    logger.info("Using installation directory: " + installationDir);
                    logger.info("Using user directory: " + userDir);
                    logger.info("Using images directory: " + imageDir);
                    logger.info("Using database directory: " + databaseDir);
                    
                    // load resources
                    new DcResources();
                    new DcSettings();
                    
                    checkPlatform();
                    
                    if (DcSettings.getString(DcRepository.Settings.stLanguage) == null ||
                        DcSettings.getString(DcRepository.Settings.stLanguage).trim().equals("")) {
                        
                        SelectLanguageDialog dlg = new SelectLanguageDialog();
                        DcSwingUtilities.openDialogNativeModal(dlg);
                    }
                    
                    showSplashScreen();
                    
                    // check if the web module has been installed
                    isWebModuleInstalled = new File(DataCrow.webDir, "datacrow/WEB-INF").exists();
                    
                    // initialize plugins
                    Plugins.getInstance();
                    
                    // initialize services
                    Servers.getInstance();
            
                    // Initialize the Component factory
                    new ComponentFactory();
                    
                    Enumeration en = Logger.getRootLogger().getAllAppenders();
                    while (en.hasMoreElements()) {
                        Appender appender = (Appender) en.nextElement();
                        if (appender instanceof TextPaneAppender)
                            ((TextPaneAppender) appender).addListener(LogPanel.getInstance());
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
                    Directory directory = new Directory(databaseDir, false, new String[] {"lck"});
                    for (String file : directory.read())
                    	new File(file).delete();
                    
                    // set the database name
                    DcSettings.set(DcRepository.Settings.stConnectionString, "dc");
                    if (db != null && db.length() > 0)
                        DcSettings.set(DcRepository.Settings.stConnectionString, db);
                    
                    File file = new File(DataCrow.databaseDir, DcSettings.getString(DcRepository.Settings.stConnectionString) + ".script");
                    if (file.exists()) {
                        new UpgradeHsqlEngine().run();
                    }
                    
                    initDbProperties();
                    
                    start = logger.isDebugEnabled() ? new Date().getTime() : 0;
                    SecurityCentre.getInstance().initialize();
                    if (logger.isDebugEnabled()) {
                        long end = new Date().getTime();
                        logger.debug("Initilization of the security center took " + (end - start) + "ms");
                    } 
                    
                    // log in
                    login(username, password);
                    
                    // Establish a connection to the database / server
                    showSplashMsg(DcResources.getText("msgInitializingDB"));
        
                    DatabaseManager.initialize();
                    
                    // Start the UI
                    if (splashScreen == null)
                        showSplashScreen();
        
                    showSplashMsg(DcResources.getText("msgLoadingItems"));
                    DcModules.loadData();
        
                    checkTabs();
                    
                    loadDefaultData();
                    
                    if (!webserverMode)
                        showSplashMsg(DcResources.getText("msgLoadingUI"));
        
                    // load the filters & patterns
                    DataFilters.load();
                    FilePatterns.load();
                    
                    ComponentFactory.setLookAndFeel();
                    
                    if (!webserverMode) {
                        mainFrame = new MainFrame();
                        
                        SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
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
                            DcSwingUtilities.displayWarningMessage("msgWebServerStartUnauthorized");
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
                    
                    SystemMonitor monitor = new SystemMonitor();
                    monitor.start();
        
                    Thread splashCloser = new Thread(new SplashScreenCloser());
                    splashCloser.start();
    
                    if (DcSettings.getBoolean(DcRepository.Settings.stCheckForNewVersion))
                        new VersionChecker().start();
                    
                    if (!webserverMode) {
                        DataFilter df = new DataFilter(DcModules._LOAN);
                        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._B_ENDDATE, Operator.IS_EMPTY, null));
                        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._E_DUEDATE, Operator.IS_FILLED, null));
                        
                        for (DcObject loan : DataManager.get(df)) {
                           if (((Loan) loan).isOverdue()) {
                                DcSwingUtilities.displayWarningMessage("msgThereAreOverdueItems");
                        		new LoanInformationForm().setVisible(true);
                        		break;
                        	}
                        }
                    }
                    
                    DcSettings.set(DcRepository.Settings.stGracefulShutdown, Boolean.FALSE);
                    DcSettings.save();
                    
                    if (DcSettings.getBoolean(DcRepository.Settings.stDriveScannerRunOnStartup)) {
                        DriveManagerDialog.getInstance();
                        DriveManager.getInstance().startScanners();
                    }
                    
                    if (DcSettings.getBoolean(DcRepository.Settings.stDrivePollerRunOnStartup)) {
                        DriveManagerDialog.getInstance();
                        DriveManager.getInstance().startDrivePoller();
                    }
                    
                    initialized = true;
                    
                    int xp = DcSettings.getInt(DcRepository.Settings.stXpMode);
                    if (!webserverMode && xp == -1) {
                        SelectExpienceLevelDialog dlg = new SelectExpienceLevelDialog();
                        dlg.setVisible(true);
                    }
                    
                    if (!webserverMode && DcSettings.getBoolean(DcRepository.Settings.stShowTipsOnStartup)) {
                        TipOfTheDayDialog dlg = new TipOfTheDayDialog();
                        dlg.setVisible(true);
                    }  
            
                    if (!webserverMode && DcSettings.getBoolean(DcRepository.Settings.stShowToolSelectorOnStartup)) {
                        ToolSelectWizard wizard = new ToolSelectWizard();
                        wizard.setVisible(true);
                    }  
                    
                    if (logger.isDebugEnabled()) {
                        long end = new Date().getTime();
                        logger.debug("Total startup time was " + (end - totalstart) + "ms");
                    }
                    
                    int usage = DcSettings.getInt(DcRepository.Settings.stUsage) + 1;
                    DcSettings.set(DcRepository.Settings.stUsage, Long.valueOf(usage));
                    
                    boolean itsTime = usage == 15 || usage == 150 || usage == 1000 || usage == 1500 || usage == 500 || usage == 50;
                    if (itsTime && DcSettings.getBoolean(DcRepository.Settings.stAskForDonation))
                        new DonateDialog().setVisible(true);
                } 
            } catch (Exception e) {
                if (logger != null)  logger.fatal("Severe error occurred while starting Data Crow. The application cannot continue.", e);
                e.printStackTrace();
                new NativeMessageBox("Error", e.toString());
                System.exit(0);
            }
        } catch (Throwable e) {
            System.out.println("Data Crow could not be started: " + e);
            e.printStackTrace();
            new NativeMessageBox("Error", "Data Crow could not be started: " + e);
            
            try {
                DcSettings.set(DcRepository.Settings.stGracefulShutdown, Boolean.FALSE);
                DcSettings.save();
            } catch (Exception ignore) {}
            
            System.exit(0);
        }

    }
    
    public static boolean isInitialized() {
    	return initialized;
    }
    
    private static void loadDefaultData() {
        
        showSplashMsg(DcResources.getText("msgCreatingDefaultData"));
        
        try {
            for (DcModule module : DcModules.getAllModules()) {
                
                if (module.isTopModule()) {
    
                    if (module.isParentModule())
                        loadDefaultData(module.getChild());
    
                    loadDefaultData(module);
                }
            }
        } catch (Exception e) {
            logger.error(e, e);
        }
    }
    
    private static void loadDefaultData(DcModule module) {
        for (DcModule referenced1 : DcModules.getReferencedModules(module.getIndex())) {
            for (DcModule referenced2 : DcModules.getReferencedModules(referenced1.getIndex())) {
                saveDefaultData(referenced2);
            }
            saveDefaultData(referenced1);
        }
        saveDefaultData(module);
    }
        
    private static void saveDefaultData(DcModule module) {
        
        if (!module.isNew() || module.isDefaultDataLoaded()) 
            return;
        
        module.setDefaultDataLoaded(true);
        
        try {
            Collection<DcObject> items = module.getDefaultData();
            if (items != null) {
                for (DcObject item : items) {
                    item.saveNew(false);
                    for (DcObject child : item.getCurrentChildren()) {
                        child.saveNew(false);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error occured while saving default data for " + module, e);
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
                showSplashScreen(false);
                while (!success && retry < 3) {
                                       
                    LoginDialog dlg = new LoginDialog();
                    DcSwingUtilities.openDialogNativeModal(dlg);
                    if (dlg.isCanceled()) break;
                    
                    try {
                        success = SecurityCentre.getInstance().login(dlg.getLoginName(), dlg.getPassword(), false) != null;
                    } catch (SecurityException se) {
                        DcSwingUtilities.displayMessage(se.getMessage());
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
                System.out.println(se.toString());
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
        @Override
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
        if (!new File(DataCrow.installationDir, "plugins").exists()) {
            String msg = "The installation directory could not be determined. " +
                "Please set the DATACROW_HOME environment variable or supply the -dir:<installation directory> parameter. " +
                "The DATACROW_HOME variable value should point to the Data Crow intallation directory.";

            System.out.println(msg);

            NativeMessageBox dlg = new NativeMessageBox("Warning", msg);
            DcSwingUtilities.openDialogNativeModal(dlg);
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
        userDir = !userDir.endsWith("/") && !userDir.endsWith("\\") ? userDir + "/" : userDir;
        webDir = DataCrow.userDir + "wwwroot/";
        imageDir = DataCrow.userDir + "wwwroot/datacrow/mediaimages/";
        iconsDir = DataCrow.userDir + "wwwroot/datacrow/mediaimages/icons/";
        reportDir = DataCrow.userDir + "reports/";
        moduleDir = DataCrow.userDir + "modules/";
        resourcesDir = DataCrow.userDir + "resources/";
        databaseDir = DataCrow.userDir + "database/";
        moduleSettingsDir = DataCrow.userDir + "settings/modules/";
        applicationSettingsDir = DataCrow.userDir + "settings/application/";
        
        pluginsDir = DataCrow.installationDir + "plugins/";
        servicesDir = DataCrow.installationDir + "services/";
                
        DataCrow.createDirectory(new File(moduleDir), "modules");
        DataCrow.createDirectory(new File(databaseDir), "database");
        DataCrow.createDirectory(new File(userDir, "temp"), "temp");
        DataCrow.createDirectory(new File(imageDir), "images");
        DataCrow.createDirectory(new File(iconsDir), "icons");
        DataCrow.createDirectory(new File(reportDir), "reports");
        DataCrow.createDirectory(new File(servicesDir), "services");
        DataCrow.createDirectory(new File(moduleSettingsDir), "moduleSettingsDir");
        DataCrow.createDirectory(new File(applicationSettingsDir), "applicationSettingsDir");
        DataCrow.createDirectory(new File(resourcesDir), "resourcesDir");
    }
    
    private static void createDirectory(File dir, String name) {

        dir.mkdirs();
        
        if (!dir.exists()) {
            String message = "Data Crow was unable to create the " + name + " directory (" + dir + "). " +
                "This indicates that the user running Data Crow has insufficient permissions. " +
                "The user running Data Crow must have full control over the Data Crow folder " +
                "and all if its sub directories.";

            if (platform.isVista()) {
                message += " For Windows Vista it is best to install Data Crow to another directory" +
                		   " then the Program Files folder.";
            }

            System.out.println(message);
            new NativeMessageBox("Warning", message);
            System.exit(0);
        }
        
        File file = new File(dir, "temp.txt");
        try {
        
            if (!file.exists())
                file.createNewFile();
            
            if (!file.exists() || !file.canWrite()) 
                throw new IOException("File cannot be created in directory " + dir);

        } catch (Throwable e) {
             String message = "Data Crow does not have permissions to modify files in the " + name + " directory. " +
                "This indicates that the user running Data Crow has insufficient permissions. " +
                "The user running Data Crow must have full control over the Data Crow folder and all of its sub folders. " +
                "Please correct this before starting Data Crow again (see the documentation of your operating system).";

            if (platform.isVista())
                message += " For Windows Vista it is best to install Data Crow to another directory then the Program Files folder.";
            
            System.out.println(message);
            new NativeMessageBox("Warning", message);
            System.exit(0);
            
        } finally {
            file.delete();
        }
    }
    
    private static void initLog4j() {
        try {
            Properties properties = new Properties();
            
            File fileLog4j = new File(DataCrow.applicationSettingsDir, "log4j.properties");
            if (!fileLog4j.exists()) {
                fileLog4j.getParentFile().mkdirs();
                Utilities.copy(new File(DataCrow.installationDir, "log4j.properties"), fileLog4j);
            }
            
            if (!fileLog4j.exists())
                fileLog4j.createNewFile(); 
            
            properties.load(new FileInputStream(fileLog4j));
            properties.setProperty("log4j.appender.logfile.File", DataCrow.userDir + "data_crow.log");
            
            if (DataCrow.debug)
                properties.setProperty("log4j.rootLogger", "debug, textpane, logfile, stdout");
            else
                properties.setProperty("log4j.rootLogger", "info, textpane, logfile");
            
            properties.store(new FileOutputStream(DataCrow.applicationSettingsDir + "log4j.properties"), "");
        } catch (Exception e) {
            System.out.println("Could not find the log4j properties file. " + e);
        }
        
        PropertyConfigurator.configure(DataCrow.applicationSettingsDir + "log4j.properties");        
    }
    
    private static void initDbProperties() {
        try {
            File file = new File(DataCrow.databaseDir, DcSettings.getString(DcRepository.Settings.stConnectionString) + ".properties");
            if (file.exists()) {
                Properties properties = new Properties();
                properties.load(new FileInputStream(file));
                
                properties.setProperty("readonly", "false");
                properties.setProperty("hsqldb.nio_data_file", "true");
                properties.setProperty("hsqldb.lock_file", "false");
                properties.setProperty("hsqldb.log_size", "10000");

                properties.store(new FileOutputStream(file), "Default properties for the DC database of Data Crow.");
            }
        } catch (Exception e) {
            logger.error("Could not set the default database properties.", e);
        }
    }    
    
    private static void checkPlatform() {
        logger.info(version.getFullString());
        logger.info("Java version: " + System.getProperty("java.version"));
        logger.info("Java vendor: " + System.getProperty("java.vendor"));
        logger.info("Operating System: " + System.getProperty("os.name"));
        
        boolean alreadyChecked = DcSettings.getBoolean(DcRepository.Settings.stCheckedForJavaVersion);
        if (!getPlatform().isJavaSun() && !alreadyChecked && !getPlatform().isJava16()) {
            new NativeMessageBox("Warning", 
                    "Data Crow has only been tested on Java from Sun (version 1.6 or higher). " +
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
            DatabaseManager.closeDatabases(false);
            System.out.println(DcResources.getText("msgShutdownComplete"));
        }
    }    
}
