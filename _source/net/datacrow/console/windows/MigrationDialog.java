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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.DcFileField;
import net.datacrow.console.components.DcShortTextField;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.components.tables.DcTableModel;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.Base64;
import net.datacrow.util.CSVReader;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class MigrationDialog extends DcDialog implements ActionListener {

    private static Logger logger = Logger.getLogger(MigrationDialog.class.getName());
    
    private DcTable tableMappings;

    private DcFileField ffSource;
    private JButton buttonApply = ComponentFactory.getButton(DcResources.getText("lblApply"));
    private JButton buttonRun = ComponentFactory.getButton(DcResources.getText("lblRun"));

    private DcShortTextField textSep = ComponentFactory.getShortTextField(5);
    private JComboBox cbCharSet = ComponentFactory.getComboBox();

    private JTextArea textLog = ComponentFactory.getTextArea();
    private JProgressBar progressBar = new JProgressBar();
    
    private ImportThread task;

    public MigrationDialog() {
        super(DataCrow.mainFrame);

        setTitle(DcResources.getText("lblFileImport"));
        setHelpIndex("dc.tools.migration");

        ffSource = ComponentFactory.getFileField(false, false);
        buildDialog();
    }

    private void addMessage(String message) {
        if (textLog != null) {
            textLog.insert(message + '\n', 0);
            textLog.setCaretPosition(0);
        }
    }

    public void initProgressBar(int maxValue) {
        progressBar.setValue(0);
        progressBar.setMaximum(maxValue);
    }

    public void updateProgressBar() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    @Override
    public void close() {
        DcModules.getCurrent().getSettings().set(DcRepository.ModuleSettings.stFileImportDialogSize, getSize());
        
        if (task != null)
            task.cancel();
        
        task = null;
        
        cbCharSet = null;
        tableMappings = null;
        ffSource = null;
        buttonApply = null;
        buttonRun = null;
        textSep = null;
        textLog = null;
        progressBar = null;
        
        super.close();
    }

    private void buildDialog() {
        getContentPane().setLayout(Layout.getGBL());

        //**********************************************************
        //Create Import Panel
        //**********************************************************
        JPanel panelImport = new JPanel();
        panelImport.setLayout(Layout.getGBL());

        textSep.setText(DcSettings.getString(DcRepository.Settings.stImportSeperator));

        buttonApply.addActionListener(this);
        buttonApply.setActionCommand("applySource");
        
        for (String charSet : Utilities.getCharacterSets()) 
            cbCharSet.addItem(charSet);

        cbCharSet.setSelectedItem(DcSettings.getString(DcRepository.Settings.stImportCharacterSet));
        
        panelImport.add(ffSource,       Layout.getGBC( 0, 0, 6, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 5, 5, 5), 0, 0));
        panelImport.add(ComponentFactory.getLabel(DcResources.getText("lblValueSeperator")),        
                 Layout.getGBC( 0, 1, 1, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 5, 5, 5), 0, 0));
        panelImport.add(textSep,        Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 5, 5, 5), 0, 0));
        panelImport.add(ComponentFactory.getLabel(DcResources.getText("lblCharacterSet")), 
                 Layout.getGBC( 0, 2, 1, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 5, 5, 5), 0, 0));
        panelImport.add(cbCharSet,        Layout.getGBC( 1, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 5, 5, 5), 0, 0));
        panelImport.add(buttonApply,    Layout.getGBC( 2, 2, 1, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 5, 5), 0, 0));

        panelImport.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblSourceSelection")));

        //**********************************************************
        //Create Import Definition Panel
        //**********************************************************
        tableMappings = ComponentFactory.getDCTable(false, false);

        DcTableModel model = (DcTableModel) tableMappings.getModel();
        model.setColumnCount(2);

        TableColumn columnName = tableMappings.getColumnModel().getColumn(0);
        JTextField text = ComponentFactory.getTextFieldDisabled();
        columnName.setCellEditor(new DefaultCellEditor(text));
        columnName.setHeaderValue(DcResources.getText("lblSourceName"));

        TableColumn columnField = tableMappings.getColumnModel().getColumn(1);
        Collection<DcField> fields = DcModules.getCurrent().getFields();
        JComboBox comboFields = ComponentFactory.getComboBox();
        columnField.setHeaderValue(DcResources.getText("lblTargetName"));

        for (DcField field : fields) {
            if (    (!field.isUiOnly() || 
                      field.getValueType() == DcRepository.ValueTypes._PICTURE || 
                      field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) && 
                     field.getIndex() != DcObject._ID)
                
                comboFields.addItem(field);
        }
        columnField.setCellEditor(new DefaultCellEditor(comboFields));

        tableMappings.applyHeaders();

        JButton buttonCancel = ComponentFactory.getButton(DcResources.getText("lblCancel"));
        buttonCancel.addActionListener(this);
        buttonCancel.setActionCommand("cancel");
        
        buttonRun.addActionListener(this);
        buttonRun.setActionCommand("import");

        JScrollPane tableScroller = new JScrollPane(tableMappings);
        tableScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel panelImportDef = new JPanel();
        panelImportDef.setLayout(Layout.getGBL());

        JPanel panelBtns = new JPanel();
        panelBtns.add(buttonRun);
        panelBtns.add(buttonCancel);
        
        panelImportDef.add(tableScroller,   Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 5, 5), 0, 0));
        panelImportDef.add(panelBtns,       Layout.getGBC( 0, 1, 1, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));

        panelImportDef.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblImportDefinitions")));

        //**********************************************************
        //Log Panel
        //**********************************************************
        JPanel panelLog = new JPanel();
        panelLog.setLayout(Layout.getGBL());

        JScrollPane scroller = new JScrollPane(textLog);
        textLog.setEditable(false);
        textLog.setEnabled(false);

        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelLog.add(scroller, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0));

        panelLog.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblLog")));

        //**********************************************************
        //Progress panel
        //**********************************************************
        JPanel panelProgress = new JPanel();
        panelProgress.setLayout(Layout.getGBL());
        panelProgress.add(progressBar, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                         ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                          new Insets(5, 5, 5, 5), 0, 0));


        //**********************************************************
        //Main panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());

        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));

        buttonApply.setMnemonic('P');
        buttonClose.setMnemonic('C');
        buttonRun.setMnemonic('R');
        buttonCancel.setMnemonic('a');
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        this.getContentPane().add(panelImport,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                  new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelImportDef,  Layout.getGBC( 0, 1, 1, 1, 20.0, 20.0
                                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                  new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelLog,     Layout.getGBC( 0, 2, 1, 1, 10.0, 10.0
                                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                  new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelProgress,Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                  new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(buttonClose,  Layout.getGBC( 0, 4, 1, 1, 0.0, 0.0
                                 ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                                  new Insets( 5, 5, 5, 5), 0, 0));
        pack();

        Dimension size = DcModules.getCurrent().getSettings().getDimension(DcRepository.ModuleSettings.stFileImportDialogSize);
        setSize(size);
        setCenteredLocation();
    }

    private boolean isCorrectSource(File file) {
        boolean correct = file != null && file.exists();
        if (file == null)
            new MessageBox(DcResources.getText("msgNoFileSelected"), MessageBox._WARNING);
        else if (!file.exists())
            new MessageBox(DcResources.getText("msgFileCannotBeUsed"), MessageBox._WARNING);
        return correct;
    }
    
    public String getCharSet() {
        String charSet = (String) cbCharSet.getSelectedItem();
        if (charSet != null && charSet.length() == 0) {
            charSet = "UTF-8";
            cbCharSet.setSelectedItem(charSet);
        }
        return charSet;
    }
    
    public String getSeperator() {
        String sep = textSep.getText().trim();
        if (sep.length() == 0) {
            sep = ",";
            textSep.setText(sep);
        }
        return sep;
    }

    private void applySource() {
        File file = ffSource.getFile();

        String sep = getSeperator();
        String charSet = getCharSet();
        
        tableMappings.clear();
        
        if (isCorrectSource(file)) {
            try {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), charSet);
                CSVReader csvReader = new CSVReader(reader, getSepCharacter(sep), this);
                String[] headers = csvReader.readNext();
                
                Collection<DcField> fields = DcModules.getCurrent().getFields();
                for (int i = 0; i < headers.length; i++) {
                    String columnValue = headers[i];
    
                    DcField field = null;
                    for (DcField fld : fields) {
                        if (fld.getLabel().toUpperCase().equals(columnValue.toUpperCase()))
                            field = fld;
                    }
    
                    tableMappings.addRow(new Object[] {DcResources.getText("lblField") + " " + i + 
                                                       " (" + columnValue + ")", field});
                }
                initProgressBar(0);
            } catch (IOException exp) {
                new MessageBox(DcResources.getText("msgFileCannotBeUsed") + ": " + exp.getMessage(), MessageBox._ERROR);
                addMessage("Error while reading file : " + exp.getMessage());
                logger.error("Error while reading from file", exp);
            }        
        }
    }

    private String getSepCharacter(String sep) {
        String character = sep;
        if (sep.toUpperCase().equals("TAB"))
            character = "\t";
        return character;
    }

    private void doImport() {
        if (tableMappings.getRowCount() == 0) {
            new MessageBox(DcResources.getText("msgNoFieldsFound"), MessageBox._WARNING);
        } else {
            File file = ffSource.getFile();
            if (isCorrectSource(file) && isCorrectMapping()) {
                List<DcField> fields = getFields();
                
                boolean filled = fields.size() > 0;
                if (filled) {
                    boolean b = true;
                    for (DcField field : fields)
                        b &= field == null;
                    
                    filled = !b;
                }
                
                if (!filled) {
                    new MessageBox(DcResources.getText("msgNoMappings"), MessageBox._WARNING);
                } else {
                    task = new ImportThread(this, fields, file, getCharSet(), getSeperator());
                    task.start();
                }
            }
        }        
    }
    
    public List<DcField> getFields() {
        List<DcField> fields = new LinkedList<DcField>();
        for (int i = 0; i < tableMappings.getRowCount(); i++)
            fields.add((DcField) tableMappings.getValueAt(i, 1, true));
        
        return fields;
    }
    
    public boolean isCorrectMapping() {
        Collection<DcField> fields = new LinkedList<DcField>();
        for (int i = 0; i < tableMappings.getRowCount(); i++) {
            DcField field = (DcField) tableMappings.getValueAt(i, 1, true);
            if (field != null && fields.contains(field)) {
                new MessageBox(DcResources.getText("msgDestinationInUse", (String) tableMappings.getValueAt(i, 0)), MessageBox._WARNING);
                return false;
            } else {
                fields.add(field);
            }
        }
        return true;
    }
    
    private void isRunning(boolean b) {
        if (buttonRun != null) {
            buttonRun.setEnabled(!b);
            buttonApply.setEnabled(!b);
        }
    }
    
    private void cancel() {
        if (task != null)
            task.cancel();
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("applySource"))
            applySource();
        else if (ae.getActionCommand().equals("import"))
            doImport();
        else if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("cancel"))
            cancel();
    }
    
    private static class ImportThread extends Thread {
        
        private boolean keepOnRunning = true;
        
        private MigrationDialog dlg;
        private File file;
        private String charSet;
        private String seperator;
        private List<DcField> fields;
        
        public ImportThread(MigrationDialog dlg, 
                            List<DcField> fields, 
                            File file, 
                            String charSet, 
                            String seperator) {
            this.dlg = dlg;
            this.file = file;
            this.charSet = charSet;
            this.seperator = seperator;
            this.fields = fields;
        }
        
        @Override
        public void run() {
            importFile();
        }
        
        public void cancel() {
            keepOnRunning = false;
        }
        
        private void importFile() {

            dlg.isRunning(true);
            DataCrow.mainFrame.setSelectedTab(net.datacrow.console.MainFrame._INSERTTAB);

            try {
                try {
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(file), charSet);
                    CSVReader csvReader = new CSVReader(reader, seperator, dlg);

                    List<String[]> lines = csvReader.readAll();                        
                    dlg.initProgressBar(lines.size() - 1);
                    
                    DcModule module = DcModules.getCurrent();
                    int counter = 0;
                    for (String[] values : lines) {
                        
                        if (!keepOnRunning) break;
                        
                        if (counter != 0) {
                            DcObject dco = module.getDcObject();
    
                            for (int i = 0; i < values.length; i++) {
                                String s = values[i];
                                DcField field = fields.get(i);
                                
                                if (field == null)
                                    continue;
                                
                                if (field.getValueType() == DcRepository.ValueTypes._LONG) {
                                    dco.setValue(field.getIndex(), convertToLong(s, field));
                                } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                                         field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                                    DataManager.createReference(dco, field.getIndex(), s);
                                } else if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                                    try {
                                        byte[] image = Base64.decode(s.toCharArray());
                                        dco.setValue(field.getIndex(), new DcImageIcon(image));
                                    } catch (Exception e) {
                                        if (s.trim().length() > 0) {
                                            byte[] image = Utilities.readFile(new File(s));
                                            dco.setValue(field.getIndex(), new DcImageIcon(image));
                                        }
                                    }
                                } else {
                                    dco.setValue(field.getIndex(),s);
                                }
                            }
                            
                            dco.setIDs();
                            dlg.addMessage(DcResources.getText("msgAddedX", dco.toString()));
                            module.getCurrentInsertView().add(dco);
                            dlg.updateProgressBar();
                        }
                        counter++;
                    }
                    reader.close();
                    dlg.addMessage(DcResources.getText("msgImportFinished"));
                } catch (Exception e) {
                    dlg.addMessage("Error while importing file " + file + ": " + e);
                    logger.error("Error while importing file " + file, e);
                }
            } finally {
                finish();
            }
        }
        
        private void finish() {
            DcSettings.set(DcRepository.Settings.stImportSeperator, seperator);
            DcSettings.set(DcRepository.Settings.stImportCharacterSet, charSet);
            
            dlg.isRunning(false);
            dlg = null;
            file = null;
            charSet = null;
            seperator = null;
            fields.clear();
            fields = null;
        }
        
        private Long convertToLong(String s, DcField field) {
            try {
                if (s != null && s.length() > 0) {
                    return Long.valueOf(s);
                }
            } catch (Exception exp) {
                dlg.addMessage(DcResources.getText("msgCouldNotConvertToInt", new String[] {s, field.getLabel()}));
            }
            return null;
        }        
    }
}
