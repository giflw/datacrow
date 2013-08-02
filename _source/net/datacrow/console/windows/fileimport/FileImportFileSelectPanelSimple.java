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

package net.datacrow.console.windows.fileimport;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcCheckBox;
import net.datacrow.console.components.DcFileField;
import net.datacrow.console.components.DcMenu;
import net.datacrow.console.components.DcMenuItem;
import net.datacrow.console.components.renderers.CheckBoxTableCellRenderer;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.Directory;
import net.datacrow.util.ITaskListener;
import net.datacrow.util.filefilters.DcFileFilter;
import net.datacrow.util.filefilters.FileNameFilter;

public class FileImportFileSelectPanelSimple extends JPanel implements ActionListener, ITaskListener {
    
    private static Logger logger = Logger.getLogger(FileImportFileSelectPanelSimple.class.getName());
    
    private JTextArea textLog = ComponentFactory.getTextArea();
    private DcCheckBox cbRecursive = ComponentFactory.getCheckBox(DcResources.getText("lblRecursive"));
    private JButton btAdd = ComponentFactory.getButton(DcResources.getText("lblAdd"), IconLibrary._icoAdd);
    
    private int module;

    private FileNameFilter filter;
    private JButton btStart;
    private DcTable tblFiles;
    private DcFileField ff;
    private DcFileField ffSingle;
    private FileImportDialog dlg;
    
    private boolean canceled = false;
    
    public FileImportFileSelectPanelSimple(FileImportDialog dlg, FileNameFilter filter, int module) {
        this.filter = filter;
        this.module = module;
        this.dlg = dlg;

        build();
    }
    
    public void save() {
        DcModules.getCurrent().setSetting(DcRepository.ModuleSettings.stFileImportRecursive, Boolean.valueOf(cbRecursive.isSelected()));
    }
    
    public void clear() {
        filter = null;
        btStart = null;
        tblFiles = null;
        ff = null;
        ffSingle = null;
        dlg = null;
    }
    
    public void cancel() {
        canceled = true;
    }
    
    @Override
    public void notifyTaskSize(int size) {}

    @Override
    public boolean isStopped() {
        return canceled;
    }

    public Collection<String> getFiles() {
        Collection<String> files = new ArrayList<String>();
        for (int row = 0; row < tblFiles.getRowCount(); row++) {
            if (tblFiles.getValueAt(row, 0).equals(Boolean.TRUE)) {
                files.add(tblFiles.getValueAt(row, 1).toString());
            }
        }
        return files;
    }
    
    public void log(String message) {
        textLog.insert(message + '\n', 0);
        textLog.setCaretPosition(0);
    }
    
    private void addFile() {
        canceled = false;
        
        File file = ffSingle.getFile();
        if (file == null) {
            DcSwingUtilities.displayErrorMessage(DcResources.getText("msgSelectFileFirst"));
        } else {
            tblFiles.addRow(new Object[] {Boolean.TRUE, file});
        }
    }
    
    private void readDir() {
        canceled = false;

        final File directory = ff.getFile();
        if (directory == null) {
            DcSwingUtilities.displayErrorMessage(DcResources.getText("msgSelectDirFirst"));
        } else {
            
            notifyTaskStarted();
            
            final Directory dir = new Directory(
                    directory.toString(), 
                    cbRecursive.isSelected(), 
                    filter != null ? filter.getExtensions() : null);
            dir.setListener(this);
            
            Thread task = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (final String file : dir.read()) {
                        try {
                            SwingUtilities.invokeAndWait(
                                    new Thread(new Runnable() { 
                                        @Override
                                        public void run() {
                                            tblFiles.addRow(new Object[] {Boolean.TRUE, new File(file)});
                                        }
                                    }));
                        } catch (Exception exp) {
                            logger.error(exp, exp);
                        }
                    }
                    
                    notifyTaskStopped();
                }
            });
            task.start();
        }
    }
    
    @Override
    public void notify(String msg) { 
        dlg.addMessage(msg);
    }

    @Override
    public void notifyProcessed() { 
        int value = dlg.progressBar.getValue() + 1;
        value = value > 100 ? 0 : value;
        dlg.updateProgressBar(value);
    }
    
    @Override
    public void notifyTaskStopped() {
        dlg.updateProgressBar(100);
        btStart.setEnabled(true);
        btAdd.setEnabled(true);
        cbRecursive.setEnabled(true);
    }

    @Override
    public void notifyTaskStarted() {
        dlg.initProgressBar(100);
        btStart.setEnabled(false);
        btAdd.setEnabled(false);
        cbRecursive.setEnabled(false);
    }

    private void build() {
        setLayout(Layout.getGBL());
        
        //**********************************************************
        // Read Panel
        //**********************************************************
        JPanel pRead = new JPanel();
        pRead.setLayout(Layout.getGBL());

        ff = ComponentFactory.getFileField(false, true);
        btStart = ComponentFactory.getButton(DcResources.getText("lblStart"), IconLibrary._icoAdd);
        btStart.setActionCommand("readDir");
        btStart.addActionListener(this);
        
        ffSingle = filter != null ? 
                ComponentFactory.getFileField(false, false, new DcFileFilter(filter.getExtensions())) :
                ComponentFactory.getFileField(false, false);
            
        btAdd.setActionCommand("addFile");
        btAdd.addActionListener(this);

        pRead.add(ComponentFactory.getLabel(DcResources.getText("lblSelectDirectory")), 
                 Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        pRead.add(ff, Layout.getGBC( 1, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 5, 5, 5, 5), 0, 0));
        pRead.add(btStart,  Layout.getGBC( 2, 0, 1, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 5, 0, 5, 5), 0, 0));
        pRead.add(cbRecursive,  Layout.getGBC(3, 0, 1, 1, 0.0, 0.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 5, 5), 0, 0));
        
        pRead.add(ComponentFactory.getLabel(DcResources.getText("lblSelectFile")), 
                Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets( 0, 5, 5, 5), 0, 0));
        pRead.add(ffSingle, Layout.getGBC( 1, 1, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 5, 5, 5), 0, 0));
        pRead.add(btAdd,  Layout.getGBC( 2, 1, 1, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 5, 5), 0, 0));
        
        //**********************************************************
        // Files Panel
        //**********************************************************
        tblFiles = ComponentFactory.getDCTable(false, false);
        JScrollPane sp = new JScrollPane(tblFiles);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        tblFiles.setColumnCount(2);
        
        TableColumn columnEnabled = tblFiles.getColumnModel().getColumn(0);
        JCheckBox checkEnabled = new JCheckBox();
        columnEnabled.setCellEditor(new DefaultCellEditor(checkEnabled));
        columnEnabled.setPreferredWidth(30);
        columnEnabled.setMaxWidth(30);
        columnEnabled.setMinWidth(30);
        columnEnabled.setCellRenderer(CheckBoxTableCellRenderer.getInstance());
        columnEnabled.setHeaderValue("");
        
        TableColumn tcDescription = tblFiles.getColumnModel().getColumn(1);
        JTextField textField = ComponentFactory.getTextFieldDisabled();
        tcDescription.setCellEditor(new DefaultCellEditor(textField));
        tcDescription.setHeaderValue(DcResources.getText("lblFile"));

        tblFiles.applyHeaders();
        
        //**********************************************************
        // Main Panel
        //**********************************************************
        add(getMenu()   ,Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        add(pRead       ,Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));        
        add(sp          ,Layout.getGBC( 0, 2, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));        
        
        cbRecursive.setSelected(((Boolean) DcModules.getCurrent().getSetting(DcRepository.ModuleSettings.stFileImportRecursive)).booleanValue());
    }
    
    protected JMenuBar getMenu() {
        JMenuBar menu = ComponentFactory.getMenuBar();
        
        menu.setPreferredSize(new Dimension(100, 22));
        menu.setMaximumSize(new Dimension(100, 22));
        menu.setMinimumSize(new Dimension(50, 22));
        
        DcMenu menuFilter = ComponentFactory.getMenu(DcResources.getText("lblFileTypes"));
        DcMenuItem menuFileTypes = ComponentFactory.getMenuItem(DcResources.getText("lblFileTypes"));
        menuFilter.add(menuFileTypes);
        
        menu.add(menuFilter);
        
        menuFileTypes.addActionListener(this);
        menuFileTypes.setActionCommand("filterFileTypes");
        
        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("filterFileTypes")) {
            SelectFileTypesDialog dlg = new SelectFileTypesDialog(module);
            dlg.setVisible(true);
            
            if (dlg.isChanged()) {
                String[] extensions = DcModules.get(module).getSettings().getStringArray(DcRepository.ModuleSettings.stFileImportFileTypes);
                filter = extensions.length > 0 ? new FileNameFilter(extensions, true) : null;
            }
        } else if (ae.getActionCommand().equals("readDir")) {
            readDir();
        } else if (ae.getActionCommand().equals("addFile")) {
            addFile();
        }
    }
}
