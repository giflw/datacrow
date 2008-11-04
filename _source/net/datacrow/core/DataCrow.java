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
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.MainFrame;
import net.datacrow.console.windows.LogForm;
import net.datacrow.console.windows.SelectExpienceLevelDialog;
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
import net.datacrow.core.services.Services;
import net.datacrow.core.settings.upgrade.SettingsConversion;
import net.datacrow.enhancers.ValueEnhancers;
import net.datacrow.filerenamer.FilePatterns;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.Directory;
import net.datacrow.util.logging.TextPaneAppender;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class DataCrow {

    private static Logger logger = Logger.getLogger(DataCrow.class.getName());
    
    private static Platform platform = new Platform();
    private static Version version = new Version(3, 4, 2, 0);
    
    public static String baseDir;
    public static String imageDir;
    public static String moduleDir;
    public static String reportDir;
    public static String pluginsDir;
    public static String servicesDir;
    public static String webDir;
    public static String dataDir;
    public static String cacheDir;
    
    private static boolean isWebModuleInstalled = false;
    
    public static MainFrame mainFrame;
    private static SplashScreen splashScreen;
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        
        boolean nocache = false;
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
            } else if (dir != null) {
                dir += " " + args[i];
            } else { 
                System.out.println("The following optional parameters can be used:");
                System.out.println("-dir:<installdir>");
                System.out.println("example: java -jar datacrow.jar -dir:d:/datacrow");
                System.out.println("");
                System.out.println("-db:<databasename>");
                System.out.println("example: java -jar datacrow.jar -db:testdb");
                System.exit(0);
            }
        }
        
        try {
        	
            installLafs();
            
            setBaseDir(dir);
            createDirectories();
            PropertyConfigurator.configure(DataCrow.baseDir + "log4j.properties");

            // Initialize the resources and settings
            new DcResources();
            new DcSettings();
            
            showSplashScreen();
            
            // check if the web module has been installed
            isWebModuleInstalled = new File(DataCrow.webDir, "WEB-INF").exists();
            
            // initialize plugins
            Plugins.getInstance();
            
            // initialize services
            Services.getInstance();
    
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
            splashScreen.setStatusMsg("Loading modules");
            new ModuleUpgrade().upgrade();
            DcModules.load();
    
            logger.info(DcResources.getText("msgModulesLoaded"));
    
            ValueEnhancers.initialize();

            // delete lock file
            for (String file : Directory.read(baseDir + "data", false, false, new String[] {"lck"}))
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
            splashScreen.setStatusMsg("Initializing database");

            DatabaseManager.initialize();
            
            if (nocache)
                DataManager.setUseCache(false);
            
            // convert the settings
            SettingsConversion.convert();
        
            // Start the UI
            if (splashScreen == null)
                showSplashScreen();

            splashScreen.setStatusMsg("Loading items");
            DcModules.loadData();
            
            splashScreen.setStatusMsg("Loading UI");

            // load the filters & patterns
            DataFilters.load();
            FilePatterns.load();
            
            ComponentFactory.setLookAndFeel();
            mainFrame = new MainFrame();
            
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    DataCrow.mainFrame.initialize();
                    DataCrow.mainFrame.setVisible(true);
                    mainFrame.setViews();
                }
            });

            Thread splashCloser = new Thread(new SplashScreenCloser());
            splashCloser.start();
            
            DataFilter df = new DataFilter(DcModules._LOAN);
            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._B_ENDDATE, Operator.IS_EMPTY, null));
            df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._E_DUEDATE, Operator.IS_FILLED, null));
            
            for (DcObject loan : DataManager.get(DcModules._LOAN, df)) {
            	if (((Loan) loan).getDaysTillOverdue().longValue() < 0) {
            		new MessageBox(DcResources.getText("msgThereAreOverdueItems"), MessageBox._WARNING);
            		new LoanInformationForm().setVisible(true);
            		break;
            	}
            }
            
            DcSettings.set(DcRepository.Settings.stGracefulShutdown, Boolean.FALSE);
            DcSettings.save();
            
        } catch (Exception e) {
            logger.fatal("Severe error occurred while starting Data Crow. The application cannot continue.", e);
            e.printStackTrace();
            new NativeMessageBox(e.getMessage());
            System.exit(0);
        }
        
        new FreeResourcesTask();

        if (DcSettings.getInt(DcRepository.Settings.stXpMode) == -1) {
            SelectExpienceLevelDialog dlg = new SelectExpienceLevelDialog();
            dlg.setVisible(true);
        }
        
        if (DcSettings.getBoolean(DcRepository.Settings.stShowTipsOnStartup)) {
            TipOfTheDayDialog dlg = new TipOfTheDayDialog();
            dlg.setVisible(true);
        }  
    }
    
    public static Version getVersion() {
        return version;
    }
    
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
    
    public static Platform getPlatform() {
        return platform;
    }
    
    private static void createDirectories() {
        File file = new File(baseDir + "data");
        if (!file.exists()) file.mkdir();

        file = new File(baseDir + "data/temp");
        if (!file.exists()) file.mkdir();
        
        file = new File(imageDir);
        if (!file.exists()) file.mkdirs();
        
        file = new File(reportDir);
        if (!file.exists()) file.mkdir();
        
        file = new File(pluginsDir);
        if (!file.exists()) file.mkdir();
        
        file = new File(servicesDir);
        if (!file.exists()) file.mkdir();
    }    
    
    public static boolean isSplashScreenActive() {
        return splashScreen != null;
    }
    
    public static void showSplashScreen(boolean b) {
        if (splashScreen != null) {
            splashScreen.setVisible(b);
            if (b) splashScreen.toFront();
        }
    }

    public static void showSplashScreen(){
        splashScreen = new SplashScreen();
        splashScreen.splash();
    }

    public static void closeSplashScreen() {
        if (splashScreen != null) {
            splashScreen.dispose();
            splashScreen = null;
        }
    }

    public static void moveSplashScreenToBack() {
        if (splashScreen != null) splashScreen.toBack();
    }

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

    private static void setBaseDir(String s) {
        baseDir = s;
        
        if (baseDir == null || baseDir.trim().length() == 0) {
            final URL location;
            final String classLocation = DataCrow.class.getName().replace('.', '/') + ".class";
            final ClassLoader loader = DataCrow.class.getClassLoader();
            if (loader == null)
                location = ClassLoader.getSystemResource(classLocation);
            else
                location = loader.getResource(classLocation);
    
            String dir = location.getFile();
            int index = dir.indexOf("_classes/net/datacrow");
            if (index > -1) dir = dir.substring(0, index);
    
            index = dir.indexOf("_build/net/datacrow");
            if (index > -1) dir = dir.substring(0, index);
    
            index = dir.indexOf("classes/net/datacrow");
            if (index > -1) dir = dir.substring(0, index);
    
            index = dir.indexOf("datacrow.jar");
            if (index > -1) dir = dir.substring(0, index);
    
            if (dir.startsWith("file:"))
                dir = dir.substring(5, dir.length());
    
            dir = dir.replaceAll("%20", " ");
            baseDir = dir;
        } else {
            while (baseDir.indexOf('\\') > -1)
                baseDir = baseDir.replace('\\', '/');
            
            baseDir += baseDir.endsWith("/") ? "" : "/";
        }
                
        try {
            dataDir = DataCrow.baseDir + "data";
            File file = new File(dataDir);
            file.mkdirs();
            
            file = new File(DataCrow.baseDir + "data/data_crow.log");
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.close();
            
            file = new File(DataCrow.baseDir + "icons_system");
            
            if (!file.exists())
                throw new Exception("Directory " + DataCrow.baseDir + " is not the Data Crow installation directory!");
            
        } catch (Exception exp) {
            String message = "Data Crow was unable to get a valid location for its data (" + exp.getMessage() + "). \n" +
                             "Correct the error or specify the installation directory via the parameter '-dir:'\n" +
                             "(java -jar datacrow.jar -dir:d:/datacrow).";
            
            new NativeMessageBox(message);
            System.out.println(message);
            System.exit(0);
        }
        
        webDir = baseDir + "webapp/datacrow/";
        imageDir = baseDir + "webapp/datacrow/mediaimages/";
        reportDir = baseDir + "reports/";
        moduleDir = baseDir + "modules/";
        pluginsDir = baseDir + "plugins/";
        servicesDir = baseDir + "services/";
        cacheDir = baseDir + "data/cache/";
        
        File file = new File(cacheDir);
        file.mkdirs();
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
}
