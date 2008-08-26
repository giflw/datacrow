package net.datacrow.console.windows.filerenamer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.DcLongTextField;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.menu.DcFileRenamerPreviewPopupMenu;
import net.datacrow.console.menu.FileRenamerMenu;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.filerenamer.FilePattern;
import net.datacrow.settings.DcSettings;

public class FileRenamerPreviewDialog extends DcDialog implements ActionListener, MouseListener {

    private DcTable table = ComponentFactory.getDCTable(true, false);
    private JProgressBar progressBar = new JProgressBar();
    private JButton buttonOk = ComponentFactory.getButton(DcResources.getText("lblStart"));
    private JButton buttonCancel = ComponentFactory.getButton(DcResources.getText("lblCancel"));

    private PreviewGenerator generator;
    
    private boolean affirmative = false;
    
    public FileRenamerPreviewDialog(JFrame parent, 
                                    DcObject[] objects, 
                                    FilePattern pattern,
                                    File baseDir) {
        super(parent);
        
        JMenu menu = new FileRenamerMenu(this);
        JMenuBar menuBar = ComponentFactory.getMenuBar();
        menuBar.add(menu);
        setJMenuBar(menuBar);        
        
        setHelpIndex("dc.tools.filerenamer");
        setTitle(DcResources.getText("lblFileRenamePreview"));
        
        build(pattern.getModule());
        generatePreview(objects, pattern, baseDir);

        setSize(DcSettings.getDimension(DcRepository.Settings.stFileRenamerPreviewDialogSize));
        setCenteredLocation();
        setModal(true);
    }
    
    private void generatePreview(DcObject[] objects, FilePattern pattern, File baseDir) {
        if (generator != null) 
            generator.cancel();
        
        generator = new PreviewGenerator(this, objects, pattern, baseDir);
        generator.start();
    }
    
    protected void addPreviewResult(DcObject dco, String oldFilename, String newFilename) {
        table.addRow(new Object[] {dco, oldFilename, newFilename});
    }
    
    protected void setBusy(boolean b) {
        buttonOk.setEnabled(!b);
    }

    protected void initProgressBar(int max) {
        progressBar.setValue(0);
        progressBar.setMaximum(max);
    }
    
    protected void updateProgressBar() {
        progressBar.setValue(progressBar.getValue() + 1);
    }
    
    public boolean isAffirmative() {
        return affirmative;
    }
    
    public DcObject[] getObjects() {
        DcObject[] objects = new DcObject[table.getRowCount()];
        for (int row = 0; row < table.getRowCount(); row++) {
            objects[row] = (DcObject) table.getValueAt(row, 0);
        }
        return objects;
    }
    
    public void clear() {
        DcSettings.set(DcRepository.Settings.stFileRenamerPreviewDialogSize, getSize());
        
        table.clear();
        table = null;
        
        if (generator != null)
            generator.cancel();
        
        generator = null;
        buttonCancel = null;
        buttonOk = null;
        progressBar = null;
        
        super.close();
    }
    
    @Override
    public void close() {
        setVisible(false);
    }

    private void build(int module) {
        
        //**********************************************************
        //Help panel
        //**********************************************************
        
        DcLongTextField textHelp = ComponentFactory.getLongTextField();
        JPanel helpPanel = new JPanel();
        helpPanel.setLayout(Layout.getGBL());

        textHelp.setBorder(null);
        JScrollPane scroller = new JScrollPane(textHelp);
        scroller.setBorder(null);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        textHelp.setEditable(false);
        textHelp.setText(DcResources.getText("msgFileRenamePreviewHelp"));
        textHelp.setMargin(new Insets(5,5,5,5));

        scroller.setPreferredSize(new Dimension(100, 60));
        scroller.setMinimumSize(new Dimension(100, 60));
        scroller.setMaximumSize(new Dimension(800, 60));
        
        helpPanel.add(scroller, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        
        //**********************************************************
        //Preview panel
        //**********************************************************
        JPanel panelPreview = new JPanel();
        
        table.setColumnCount(3);
        TableColumn tcDescription = table.getColumnModel().getColumn(0);
        JTextField textField = ComponentFactory.getTextFieldDisabled();
        tcDescription.setCellEditor(new DefaultCellEditor(textField));
        tcDescription.setHeaderValue(DcModules.get(module).getObjectName());

        TableColumn tcOldFilename = table.getColumnModel().getColumn(1);
        textField = ComponentFactory.getTextFieldDisabled();
        tcOldFilename.setCellEditor(new DefaultCellEditor(textField));
        tcOldFilename.setHeaderValue(DcResources.getText("lblOldFilename"));
        
        TableColumn tcNewFilename = table.getColumnModel().getColumn(2);
        textField = ComponentFactory.getTextFieldDisabled();
        tcNewFilename.setCellEditor(new DefaultCellEditor(textField));
        tcNewFilename.setHeaderValue(DcResources.getText("lblNewFilename"));
        
        table.addMouseListener(this);
        table.applyHeaders();
        
        panelPreview.setLayout(Layout.getGBL());
        panelPreview.add(new JScrollPane(table), Layout.getGBC( 0, 0, 5, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));

        
        // **********************************************************
        //Action panel
        //**********************************************************
        JPanel panelAction = new JPanel();
        
        buttonOk.setEnabled(false);
        
        buttonOk.setMnemonic('O');
        buttonCancel.setMnemonic('C');
        
        buttonOk.addActionListener(this);
        buttonCancel.addActionListener(this);
        
        buttonOk.setActionCommand("confirm");
        buttonCancel.setActionCommand("cancel");
        
        panelAction.add(buttonOk);
        panelAction.add(buttonCancel);
        
        //**********************************************************
        //Main panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());

        this.getContentPane().add(helpPanel,    Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelPreview,  Layout.getGBC( 0, 1, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelAction,   Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(progressBar,   Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 5, 5, 5, 5), 0, 0));
        
        pack();
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("confirm")) {
            affirmative = true;
            close();
        } else if (ae.getActionCommand().equals("remove")) {
            int[] rows = table.getSelectedRows();
            if (rows != null && rows.length > 0)
                table.remove(rows);
            else 
                new MessageBox(DcResources.getText("msgNoItemsSelectedToRemove"), MessageBox._INFORMATION);
        } else if (ae.getActionCommand().equals("cancel")) {
            close();
        }
    }
    
    private static class PreviewGenerator extends Thread {

        private DcObject[] objects;
        private FilePattern pattern;
        private FileRenamerPreviewDialog dlg;
        private File baseDir;
        
        private boolean keepOnRunning = true;
        
        public PreviewGenerator(FileRenamerPreviewDialog dlg, 
                                DcObject[] objects, 
                                FilePattern pattern, 
                                File baseDir) {
            this.dlg = dlg;
            this.objects = objects;
            this.pattern = pattern;
            this.baseDir = baseDir;
        }

        public void cancel() {
            keepOnRunning = false;
        }
        
        @Override
        public void run() {
            dlg.initProgressBar(objects.length);
            dlg.setBusy(true);
            
            for (DcObject dco : objects) {

                if (!keepOnRunning) break;
                
                File oldFile = new File(dco.getFilename());
                String newFilename = pattern.getFilename(dco, oldFile, baseDir);
                
                dlg.addPreviewResult(dco, oldFile.toString(), newFilename);
                dlg.updateProgressBar();
            }

            dlg.setBusy(false);
            objects = null;
            pattern = null;
            dlg = null;
        }
    }
    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            
            DcFileRenamerPreviewPopupMenu popupmenu = new DcFileRenamerPreviewPopupMenu(this);
            popupmenu.validate();
            popupmenu.show(table, e.getX(), e.getY());
        }
    }
}
