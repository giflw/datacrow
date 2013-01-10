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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFileField;
import net.datacrow.console.components.DcProgressBar;
import net.datacrow.core.DataCrow;

import net.datacrow.util.Directory;
import net.datacrow.util.Utilities;

public class UserDirSetupDialog extends JDialog implements ActionListener {
    
    private DcProgressBar progressBar = new DcProgressBar();
    private DcFileField selectDir = ComponentFactory.getFileField(false, true); 
    
    private JTextArea textLog = ComponentFactory.getTextArea();
    
    private JButton buttonStart = ComponentFactory.getButton("Start");
    private JButton buttonClose = ComponentFactory.getButton("Close");
    
    private boolean success = false;
    
    public UserDirSetupDialog() {
        super();
        buildDialog();
        pack();
        setSize(new Dimension(400, 400));
        setLocation(Utilities.getCenteredWindowLocation(getSize(), false));
        enableActions(true);
    }
    
    protected void initialize() {
        initProgressBar(0);
        enableActions(false);
    }
    
    protected void cancel() {
        enableActions(true);
    }
    
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

    private void setupDataDir() {
        DataDirCreator ddc = new DataDirCreator(selectDir.getFile(), this);
        ddc.start();
    }
    
    private void buildDialog() {
        
        getContentPane().setLayout(Layout.getGBL());

        //**********************************************************
        //Main panel
        //**********************************************************
        JPanel panelMain = new JPanel();
        panelMain.setLayout(Layout.getGBL());
        
        panelMain.add(ComponentFactory.getLabel("Select the data folder"), 
                Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        panelMain.add(selectDir,     Layout.getGBC(0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
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
        //Action panel
        //**********************************************************
        JPanel panelActions = new JPanel();
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        buttonStart.addActionListener(this);
        buttonStart.setActionCommand("start");
        
        panelActions.add(buttonStart);
        panelActions.add(buttonClose);
        
        //**********************************************************
        //Main
        //**********************************************************
        getContentPane().add(panelMain,         Layout.getGBC(0, 0, 1, 1, 3.0, 3.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions,      Layout.getGBC( 0, 5, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                 new Insets(0, 0, 0, 0), 0, 0));
        getContentPane().add(panelLog,          Layout.getGBC( 0, 6, 1, 1, 5.0, 5.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));  
        getContentPane().add(progressBar,       Layout.getGBC( 0, 7, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            System.exit(0);
        else if (ae.getActionCommand().equals("cancel"))
            cancel();
        else if (ae.getActionCommand().equals("start"))
            setupDataDir();
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
         * Creates the data folder:
         * /database
         * /wwwroot
         * /modules
         * /settings
         * /resources
         */
        @Override
        public void run() {
            
            try {
                client.addMessage("Initiliazing the data folder");
                boolean created;
                if (!userDir.exists()) {
                    created = userDir.mkdirs();
                    
                    if (!created) throw new Exception("Folder " + userDir.toString() + " could not be created");
                }
                
                try {
                    Utilities.rename(new File(DataCrow.userDir, "data_crow.log"), new File(userDir, "data_crow.log"));
                } catch (IOException e) {}
                
                setupDatabaseDir();
                setupWebDir();
                setupModulesDir();
                setupApplicationSettingsDir();
                setupModuleSettingsDir();                
                setupResourcesDir();
                setupReportsDir();
                
                File userHome = new File(System.getProperty("user.home"), "datacrow");
                userHome.mkdir();
                
                File userDirSettings = new File(userHome, "datafolder.properties");
                Properties properties = new Properties();
                properties.setProperty("datafolder", userDir.toString());
                FileOutputStream fos = new FileOutputStream(userDirSettings);
                properties.store(fos, "Data Crow user directory setting file. Better to leave it right here.");
                
                client.setSuccess(true);
                fos.close();
            } catch (Exception e) {
                client.setSuccess(false);
                client.addMessage(e.getMessage());
            }
        }       

        private void setupDatabaseDir() throws Exception {
            File databaseDir = new File(userDir, "database");
          
            Directory dir = new Directory(DataCrow.databaseDir, false, new String[] {"script"});
            File file;
            for (String s : dir.read()) {
                file = new File(s);
                Utilities.rename(file, new File(databaseDir, file.getName()));
                
                try {
                    file = new File(file.getParentFile(), file.getName().replace(".script", ".properties"));
                    Utilities.rename(file, new File(databaseDir, file.getName()));
                } catch (FileNotFoundException fne) {}
                
                try {
                    file = new File(file.getParentFile(), file.getName().replace(".properties", ".log"));
                    Utilities.rename(file, new File(databaseDir, file.getName()));
                } catch (FileNotFoundException fne) {}
                
                try {
                    file = new File(file.getParentFile(), file.getName().replace(".log", ".lck"));
                    Utilities.rename(file, new File(databaseDir, file.getName()));
                } catch (FileNotFoundException fne) {}
            }
        }
        
        private void setupWebDir() throws Exception {
            File webDir = new File(userDir, "wwwroot");
            webDir.mkdir();
        }
        
        private void setupModulesDir() throws Exception {
            File modulesDir = new File(userDir, "modules");
            modulesDir.mkdir();
            
            Directory dir = new Directory(DataCrow.moduleDir, false, null);
            File file;
            for (String s : dir.read()) {
                file = new File(s);
                Utilities.copy(file, new File(modulesDir, file.getName()));
            }
        }
        
        private void setupApplicationSettingsDir() throws Exception {
            File applicationSettingsDir = new File(userDir, "settings/application");
            applicationSettingsDir.mkdirs();
            
            try {
                Utilities.rename(new File(DataCrow.userDir, "data_crow.properties"), new File(applicationSettingsDir, "data_crow.properties"));
            } catch (IOException e) {}
            try {
                Utilities.rename(new File(DataCrow.userDir, "data_crow_queries.txt"), new File(applicationSettingsDir, "data_crow_queries.txt"));
            } catch (IOException e) {}
            try {
                Utilities.rename(new File(DataCrow.userDir, "filepatterns.xml"), new File(applicationSettingsDir, "filepatterns.xml"));
            } catch (IOException e) {}
            try {
                Utilities.rename(new File(DataCrow.userDir, "filters.xml"), new File(applicationSettingsDir, "filters.xml"));
            } catch (IOException e) {}
            try {
                Utilities.copy(new File(DataCrow.installationDir, "log4j.properties.properties"), new File(applicationSettingsDir, "log4j.properties.properties"));
            } catch (IOException e) {}
            try {
                Utilities.copy(new File(DataCrow.userDir, "enhancers_autoincrement.properties"), new File(applicationSettingsDir, "enhancers_autoincrement.properties"));
            } catch (IOException e) {}
            try {
                Utilities.copy(new File(DataCrow.userDir, "enhancers_titlerewriters.properties"), new File(applicationSettingsDir, "enhancers_titlerewriters.properties"));
            } catch (IOException e) {}
            try {
                Utilities.copy(new File(DataCrow.userDir, "enhancers_associatenamerewriters.properties"), new File(applicationSettingsDir, "enhancers_associatenamerewriters.properties"));
            } catch (IOException e) {}
        }
        
        private void setupResourcesDir() throws Exception {
            File resourcesSettingsDir = new File(userDir, "resources");
            resourcesSettingsDir.mkdirs();
            
            Directory dir = new Directory(DataCrow.resourcesDir, false, null);
            File file;
            for (String s : dir.read()) {
                file = new File(s);
                Utilities.copy(file, new File(resourcesSettingsDir, file.getName()));
            }
        }
        
        private void setupReportsDir() throws Exception {
            File reportsDir = new File(userDir, "reports");
            reportsDir.mkdirs();
            
            Directory dir = new Directory(DataCrow.reportDir, true, null);
            File file;
            File targetDir;
            int idx;
            for (String s : dir.read()) {
                file = new File(s);
                
                if (file.isDirectory()) continue;
                
                if (file.getParent().endsWith("reports")) {
                    Utilities.copy(file, new File(reportsDir, file.getName()));
                } else {
                    idx = s.indexOf("/reports/") > -1 ? s.indexOf("/reports/") : s.indexOf("\\reports\\");
                    targetDir = new File(reportsDir, s.substring(idx + 9)).getParentFile();
                    targetDir.mkdirs();
                    Utilities.copy(file, new File(targetDir, file.getName()));
                }
            }
        }
        
        private void setupModuleSettingsDir() throws Exception {
            File modulesSettingsDir = new File(userDir, "settings/modules");
            modulesSettingsDir.mkdirs();
            
            Directory dir = new Directory(DataCrow.userDir, false, new String[] {"properties"});
            File file;
            for (String s : dir.read()) {
                file = new File(s);
                Utilities.rename(file, new File(modulesSettingsDir, file.getName()));
            }
        }
    }
}
