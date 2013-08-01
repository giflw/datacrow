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

package net.datacrow.console.windows;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFileField;
import net.datacrow.console.components.DcLongTextField;
import net.datacrow.console.components.DcProgressBar;
import net.datacrow.console.windows.messageboxes.NativeMessageBox;
import net.datacrow.console.windows.messageboxes.NativeQuestionBox;
import net.datacrow.core.DataCrow;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Directory;
import net.datacrow.util.Utilities;

public class UserDirSetupDialog extends NativeDialog implements ActionListener {
    
    private DcProgressBar progressBar = new DcProgressBar();
    private DcFileField selectDir = ComponentFactory.getFileField(false, true); 
    
    private JTextArea textLog = ComponentFactory.getTextArea();
    
    private JButton buttonStart = ComponentFactory.getButton("OK");
    
    private boolean success = false;
    private boolean shutdown = false;
    
    private String[] args;
    private final String selectedUserDir;
    
    private boolean overwrite = false;
    private boolean copymode = true;
    
    public UserDirSetupDialog(String[] args, String selectedUserDir) {
        setTitle("User Folder Configuration");
        setIconImage(new DcImageIcon(new File(DataCrow.installationDir, "icons/datacrow64.png")).getImage());
        this.args = args;
        this.selectedUserDir = selectedUserDir;
    }

    public void build() {
        buildDialog();
        pack();
        setSize(new Dimension(450, 300));
        setLocation(Utilities.getCenteredWindowLocation(getSize(), false));
        enableActions(true);
    }
    
    public void setShutDown(boolean b) {
        this.shutdown = b;
    }
    
    protected void initialize() {
        initProgressBar(0);
        enableActions(false);
    }
    
    protected void cancel() {
        enableActions(true);
    }
    
    @Override
    public void close() {
        setVisible(false);
    }    
    
    protected void enableActions(boolean b) {
        if (buttonStart != null)
            buttonStart.setEnabled(b);
        
        if (!b) {
            progressBar.setValue(0);
            textLog.setText("");
        }
    }    
    
    public void addMessage(String message) {
        if (textLog != null) 
            textLog.insert(message + '\n', 0);
    }

    public void initProgressBar(int maxValue) {
        progressBar.setValue(0);
        progressBar.setMaximum(maxValue);
    }

    public void updateProgressBar() {
        if (progressBar != null)
            progressBar.setValue(progressBar.getValue() + 1);
    }
    
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public void stop() {
        close();
        
        DataCrow.installationDir = null;
        DataCrow.imageDir = null;
        DataCrow.iconsDir = null;
        DataCrow.moduleDir = null;
        DataCrow.reportDir = null;
        DataCrow.pluginsDir = null;
        DataCrow.servicesDir = null;
        DataCrow.webDir = null;
        DataCrow.databaseDir = null;
        DataCrow.moduleSettingsDir = null;
        DataCrow.applicationSettingsDir = null;
        DataCrow.userDir = null;
        DataCrow.resourcesDir = null;
        DataCrow.upgradeDir = null;
        
        DataCrow.restart = true;
        
        if (shutdown) {
            DataCrow.mainFrame.close();
        } else {
            DataCrow.main(args);
        }
    }

    private void setupDataDir() {
        File target = selectDir.getFile();
        File installDir = new File(DataCrow.installationDir);
        
        if (target == null) {
            new NativeMessageBox("Warning", "Please select a folder before continuing");
        } else {
            
           if (Utilities.isEmpty(target.getParent())) {
                NativeQuestionBox qb = new NativeQuestionBox(
                        "It is not recommended to select the root folder for the user folder of Data Crow. " +
                        "Continue anyway?");
                
                if (!qb.isAffirmative()) return;
            }
            
            Directory dir = new Directory(DataCrow.databaseDir, false, new String[] {"script"});
            File f = new File(target, "database");
            boolean filesChecked = false;
            if (dir.read().size() > 0 && f.exists()) {
                NativeQuestionBox qb = new NativeQuestionBox(
                        "It seems the target folder has been set up as a user folder before. Do you want to move " +
                        "the data from the old location to the new location?\n " +
                        "\"No\" = leave target directory as is, \n " +
                        "\"Yes\" = moves all files over from the old to the new location.");
                
                copymode = qb.isAffirmative();
                overwrite = qb.isAffirmative();
                
                filesChecked = true;
            }
            
            if (!filesChecked) {
                dir = new Directory(target.toString(), false, null);
                
                if (dir.read().size() > 0 && !f.exists()) {
                    NativeQuestionBox qb = new NativeQuestionBox(
                            "The selected target folder already contains files and / or directories. " +
                            "Do you want to continue?");
                    
                    if (!qb.isAffirmative()) return;
                }
            }
            
            if (target.equals(installDir)) {
                new NativeMessageBox("Warning", "The installation directory can't selected as the user folder. " +
                        "You CAN select a sub folder within the installation folder though.");
                return;
            }

            buttonStart.setEnabled(false);
            
            DataDirCreator ddc = new DataDirCreator(target, this);
            ddc.start();
        }
    }
    
    private class DataDirCreator extends Thread {
        
        private UserDirSetupDialog client;
        private File userDir;
        
        private DataDirCreator(File target, UserDirSetupDialog dlg) {
            client = dlg;
            client.initProgressBar(5);
            
            userDir = target;
        }
        
        /**
         * Creates the user folder and moves the existing files to the user folder.
         */
        @Override
        public void run() {
            
            try {
                client.addMessage("Initiliazing the data folder");
                boolean created;
                if (!userDir.exists()) {
                    created = userDir.mkdirs();
                    
                    if (!created) throw new Exception("Folder " + userDir.toString() + " could not be created");
                    
                    File file = new File(userDir, "temp.txt");
                    try {
                        if (!file.exists())
                            file.createNewFile();
                        
                        if (!file.exists() || !file.canWrite()) {
                            String message = "Data Crow does not have permissions to modify files in the " + userDir + " directory. " +
                                    "This indicates that the user running Data Crow has insufficient permissions. " +
                                    "The user running Data Crow must have full control over the Data Crow user folder and all of its sub folders. " +
                                    "Please correct this before starting Data Crow again (see the documentation of your operating system).";

                            new NativeMessageBox("Warning", message);
                            throw new Exception(message);
                        }
                    } finally {
                        file.delete();
                    }
                }
                
                if (copymode) {
                    try {
                        Utilities.rename(new File(DataCrow.installationDir, "data_crow.log"), new File(userDir, "data_crow.log"), true);
                    } catch (IOException e) {}
                    
                    setupDatabaseDir();
                    setupModulesDir();
                    setupApplicationSettingsDir();
                    setupModuleSettingsDir();                
                    setupResourcesDir();
                    setupReportsDir();
                    setupIconDir();
                    setupImagesDir();
                    setupWebDir();
                    setupUpgradeDir();
                }
                
                File userHome = new File(System.getProperty("user.home"));
                userHome.mkdir();
                
                DataCrow.getDcproperties().setUserDir(userDir.toString());
                
                client.setSuccess(true);
                client.addMessage("The user folder has been initialized");
                client.stop();
                
            } catch (Exception e) {
                client.setSuccess(false);
                client.addMessage(e.getMessage());
            }
        }       

        private void setupDatabaseDir() throws Exception {
            File databaseDir = new File(userDir, "database");
          
            Directory dir = new Directory(DataCrow.databaseDir, false, new String[] {"script"});
            File file;
            File target;
            for (String s : dir.read()) {
                file = new File(s);
                target = new File(databaseDir, file.getName());
                
                Utilities.rename(file, target, overwrite);
                    
                try {
                    file = new File(file.getParentFile(), file.getName().replace(".script", ".properties"));
                    Utilities.rename(file, new File(databaseDir, file.getName()), overwrite);
                } catch (FileNotFoundException fne) {}
                
                try {
                    file = new File(file.getParentFile(), file.getName().replace(".properties", ".log"));
                    Utilities.rename(file, new File(databaseDir, file.getName()), overwrite);
                } catch (FileNotFoundException fne) {}
                
                try {
                    file = new File(file.getParentFile(), file.getName().replace(".log", ".lck"));
                    Utilities.rename(file, new File(databaseDir, file.getName()), overwrite);
                } catch (FileNotFoundException fne) {}
            }
        }
        
        private void setupWebDir() throws Exception {
            client.addMessage("Starting to set up the web root");
            
            File webDir = new File(userDir, "wwwroot/datacrow");
            webDir.mkdir();
            
            File file;
            File targetDir;
            int idx;
            Directory dir = new Directory(new File(DataCrow.installationDir, "webapp/datacrow").toString(), true, null);
            for (String s : dir.read()) {
                file = new File(s);
                idx = s.indexOf("webapp/datacrow/") > -1 ? s.indexOf("webapp/datacrow/") : s.indexOf("webapp\\datacrow\\");
                
                if (idx == -1) continue;
                
                targetDir = (new File(webDir, s.substring(idx + "webapp/datacrow/".length())).getParentFile());
                targetDir.mkdirs();
                Utilities.copy(file, new File(targetDir, file.getName()), overwrite);
            }
            
            client.addMessage("Web root has been set up");
        }
        
        private void setupUpgradeDir() throws Exception {
            client.addMessage("Starting to set up the upgrade folder");
            
            File upgradeDir = new File(userDir, "upgrade");
            upgradeDir.mkdir();
            
            Directory dir = new Directory(DataCrow.upgradeDir, false, null);
            File file;
            for (String s : dir.read()) {
                file = new File(s);
                Utilities.copy(file, new File(upgradeDir, file.getName()), overwrite);
            }
            
            client.addMessage("Upgrade folder has been set up");
        }
        
        private void setupModulesDir() throws Exception {
            client.addMessage("Starting to set up the modules folder");
            
            File modulesDir = new File(userDir, "modules");
            modulesDir.mkdir();
            
            Directory dir = new Directory(DataCrow.moduleDir, false, null);
            File file;
            for (String s : dir.read()) {
                file = new File(s);
                Utilities.copy(file, new File(modulesDir, file.getName()), overwrite);
            }
            
            File modulesDataDir = new File(modulesDir, "data");
            modulesDataDir.mkdir();
            
            dir = new Directory(new File(DataCrow.moduleDir, "data").toString(), true, null);
            int idx;
            File targetDir;
            for (String s : dir.read()) {
                file = new File(s);
                
                if (file.isDirectory()) continue;
                
                if (file.getParent().endsWith("data")) {
                    Utilities.copy(file, new File(modulesDataDir, file.getName()), overwrite);
                } else {
                    idx = s.indexOf("/data/") > -1 ? s.indexOf("/data/") : s.indexOf("\\data\\");
                    targetDir = new File(modulesDataDir, s.substring(idx + 6)).getParentFile();
                    targetDir.mkdirs();
                    Utilities.copy(file, new File(targetDir, file.getName()), overwrite);
                }
            }
            
            client.addMessage("Modules folder has been set up");
        }
        
        private void setupApplicationSettingsDir() throws Exception {
            client.addMessage("Starting to set up the application settings");
            
            File applicationSettingsDir = new File(userDir, "settings/application");
            applicationSettingsDir.mkdirs();
            
            try {
                Utilities.rename(new File(DataCrow.userDir, "data_crow.properties"), new File(applicationSettingsDir, "data_crow.properties"), overwrite);
            } catch (IOException e) {}
            try {
                Utilities.rename(new File(DataCrow.userDir, "data_crow_queries.txt"), new File(applicationSettingsDir, "data_crow_queries.txt"), overwrite);
            } catch (IOException e) {}
            try {
                Utilities.rename(new File(DataCrow.userDir, "filepatterns.xml"), new File(applicationSettingsDir, "filepatterns.xml"), overwrite);
            } catch (IOException e) {}
            try {
                Utilities.rename(new File(DataCrow.userDir, "filters.xml"), new File(applicationSettingsDir, "filters.xml"), overwrite);
            } catch (IOException e) {}
            try {
                Utilities.copy(new File(DataCrow.installationDir, "log4j.properties"), new File(applicationSettingsDir, "log4j.properties"), overwrite);
            } catch (IOException e) {}
            try {
                Utilities.copy(new File(DataCrow.userDir, "enhancers_autoincrement.properties"), new File(applicationSettingsDir, "enhancers_autoincrement.properties"), overwrite);
            } catch (IOException e) {}
            try {
                Utilities.copy(new File(DataCrow.userDir, "enhancers_titlerewriters.properties"), new File(applicationSettingsDir, "enhancers_titlerewriters.properties"), overwrite);
            } catch (IOException e) {}
            try {
                Utilities.copy(new File(DataCrow.userDir, "enhancers_associatenamerewriters.properties"), new File(applicationSettingsDir, "enhancers_associatenamerewriters.properties"), overwrite);
            } catch (IOException e) {}
            
            client.addMessage("Applications have been set up");
        }
        
        private void setupResourcesDir() throws Exception {
            File resourcesSettingsDir = new File(userDir, "resources");
            resourcesSettingsDir.mkdirs();
            
            client.addMessage("Starting to move resources");
            Directory dir = new Directory(DataCrow.resourcesDir, false, null);
            File file;
            for (String s : dir.read()) {
                file = new File(s);
                Utilities.copy(file, new File(resourcesSettingsDir, file.getName()), overwrite);
            }
            client.addMessage("Resources have been moved");
        }
        
        private void setupReportsDir() throws Exception {
            File reportsDir = new File(userDir, "reports");
            reportsDir.mkdirs();
            
            client.addMessage("Starting to move reports");
            Directory dir = new Directory(DataCrow.reportDir, true, null);
            File file;
            File targetDir;
            int idx;
            for (String s : dir.read()) {
                file = new File(s);
                
                if (file.isDirectory()) continue;
                
                if (file.getParent().endsWith("reports")) {
                    Utilities.copy(file, new File(reportsDir, file.getName()), overwrite);
                } else {
                    idx = s.indexOf("/reports/") > -1 ? s.indexOf("/reports/") : s.indexOf("\\reports\\");
                    targetDir = new File(reportsDir, s.substring(idx + 9)).getParentFile();
                    targetDir.mkdirs();
                    Utilities.copy(file, new File(targetDir, file.getName()), overwrite);
                }
            }
            
            client.addMessage("Reports have been moved");
        }
        
        private void setupModuleSettingsDir() throws Exception {
            File modulesSettingsDir = new File(userDir, "settings/modules");
            modulesSettingsDir.mkdirs();
            
            client.addMessage("Starting to move modules");
            Directory dir = new Directory(DataCrow.userDir, false, new String[] {"properties"});
            File file;
            for (String s : dir.read()) {
                file = new File(s);
                Utilities.rename(file, new File(modulesSettingsDir, file.getName()), overwrite);
            }
            client.addMessage("Module have been moved");
        }
        
        private void setupImagesDir() throws Exception {
            File imagesDir = new File(userDir, "wwwroot/datacrow/mediaimages");
            imagesDir.mkdirs();
            
            client.addMessage("Starting moving images");
            
            Directory dir = new Directory(DataCrow.imageDir, false, null);
            File file;
            List<String> images = dir.read();
            client.initProgressBar(images.size());
            
            for (String s : images) {
                file = new File(s);
                Utilities.rename(file, new File(imagesDir, file.getName()), overwrite);
                client.addMessage("Moved " + file.getName());
                client.updateProgressBar();
            }
            client.addMessage("Images have been moved");
            new File(DataCrow.imageDir).delete();
        }
        
        private void setupIconDir() throws Exception {
            client.addMessage("Starting moving icons");
            
            File iconsDir = new File(userDir, "wwwroot/datacrow/mediaimages/icons");
            iconsDir.mkdirs();
            
            Directory dir = new Directory(new File(DataCrow.imageDir, "icons").toString(), false, null);
            File file;
            for (String s : dir.read()) {
                file = new File(s);
                Utilities.rename(file, new File(iconsDir, file.getName()), overwrite);
            }
            client.addMessage("Icons have been moved");
            new File(DataCrow.imageDir, "icons").delete();
        }
    }
    
    private void buildDialog() {
        
        getContentPane().setLayout(Layout.getGBL());

        //**********************************************************
        //Main panel
        //**********************************************************
        JPanel panelMain = new JPanel();
        panelMain.setLayout(Layout.getGBL());

        buttonStart.addActionListener(this);
        buttonStart.setActionCommand("start");

        DcLongTextField helpText = ComponentFactory.getHelpTextField();
        helpText.setText("Please select the user folder where Data Crow will store it's data. " +
                "Existing information will be migrated to the selected folder. You can also select a folder in the Data Crow installation folder (as per the old Data Crow standard) " +
                "but you will have to make sure that you have the correct priviliges.");
        
        if (selectedUserDir != null) 
            selectDir.setFile(new File(selectedUserDir));
        
        panelMain.add(helpText, Layout.getGBC(0, 0, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        panelMain.add(selectDir,     Layout.getGBC(0, 1, 1, 1, 10.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        panelMain.add(buttonStart,   Layout.getGBC(1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));

        
        //**********************************************************
        //Log panel
        //**********************************************************
        JPanel panelLog = new JPanel();
        panelLog.setLayout(Layout.getGBL());
        
        JScrollPane logScroller = new JScrollPane(textLog);
        logScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panelLog.setBorder(ComponentFactory.getTitleBorder("Messages"));
        panelLog.add(logScroller, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));

        
        //**********************************************************
        //Main
        //**********************************************************
        getContentPane().add(panelMain,         Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelLog,          Layout.getGBC( 0, 1, 1, 1, 5.0, 5.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));  
        getContentPane().add(progressBar,       Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("start"))
            setupDataDir();
    }
}
