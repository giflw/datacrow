package net.datacrow.console.windows;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFileField;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.ITaskListener;
import net.datacrow.util.filefilters.DcFileFilter;
import net.datacrow.util.ical.ICalendarExporter;

import org.apache.log4j.Logger;

public class ICalendarExporterDialog extends DcDialog implements ActionListener, ITaskListener {

    private static Logger logger = Logger.getLogger(ICalendarExporterDialog.class.getName());
    
    private DcFileField ffTarget = ComponentFactory.getFileField(true, false);
    private JButton btExport = ComponentFactory.getButton(DcResources.getText("lblExport"));
    private JButton btCancel = ComponentFactory.getButton(DcResources.getText("lblCancel"));
    
    private JCheckBox cbFullExport = ComponentFactory.getCheckBox(DcResources.getText("lblFullExport"));
    
    private JTextArea textLog = ComponentFactory.getTextArea();
    private JProgressBar progressBar = new JProgressBar();
    
    private boolean stopped = false;

    public ICalendarExporterDialog() {
        super(DataCrow.mainFrame);
        setTitle(DcResources.getText("lblICalendarLoanExport"));
        setHelpIndex("dc.tools.icalendar_export");

        buildDialog();
    }
    
    private void export() {
        
        File target = ffTarget.getFile();
        
        if (target == null) {
            DcSwingUtilities.displayMessage("msgSelectFileFirst");
        } else {
            ICalendarExporter exporter = new ICalendarExporter(this, target, cbFullExport.isSelected());
            exporter.start();
        }
    }

    @Override
    public void notifyProcessed() {
        
        if (stopped) return;
        
        int value = progressBar.getValue();
        
        if (value >= 10) 
            value = 0;
        
        progressBar.setValue(value + 1);
    }
    
    @Override
    public void notify(String msg) {
        
        if (stopped) return;
        
        textLog.insert(msg + '\n', 0);
        textLog.setCaretPosition(0);
    }

    @Override
    public void notifyTaskStopped() {
        setActionsEnabled(true);
        progressBar.setValue(10);
    }

    @Override
    public void notifyTaskStarted() {
        setActionsEnabled(false);
        
        progressBar.setValue(0);
        progressBar.setMaximum(10);
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }
    
    private void setActionsEnabled(boolean b) {
        btExport.setEnabled(b);
        ffTarget.setEnabled(b);
        cbFullExport.setEnabled(b);
    }

    private void buildDialog() {
        getContentPane().setLayout(Layout.getGBL());
        
        ffTarget.setFileFilter(new DcFileFilter("ics"));

        btCancel.setActionCommand("cancel");
        btExport.setActionCommand("export");
        
        btCancel.addActionListener(this);
        btExport.addActionListener(this);
        
        JPanel panelActions = new JPanel();
        panelActions.add(btExport);
        panelActions.add(btCancel);
        
        //**********************************************************
        //Create Backup Panel
        //**********************************************************
        JPanel panelExport = new JPanel();

        panelExport.setLayout(Layout.getGBL());
        
        panelExport.add(ComponentFactory.getLabel(DcResources.getText("lblTargetFile")), 
                Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 
                new Insets( 0, 5, 5, 5), 0, 0));
        panelExport.add(ffTarget, Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 5, 5, 5), 0, 0));
        panelExport.add(cbFullExport, Layout.getGBC( 0, 1, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 5, 5, 5), 0, 0));

        //**********************************************************
        //Log Panel
        //**********************************************************
        JPanel panelLog = new JPanel();
        panelLog.setLayout(Layout.getGBL());

        JScrollPane scroller = new JScrollPane(textLog);
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

        this.getContentPane().add(panelExport,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelLog,     Layout.getGBC( 0, 1, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelProgress,Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets( 5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelActions, Layout.getGBC( 0, 3, 1, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 10), 0, 0));
        
        pack();

        Dimension size = DcSettings.getDimension(DcRepository.Settings.stICalendarExportDialogSize);
        setSize(size);

        setCenteredLocation();
    }

    @Override
    public void close() {
        
        stopped = true;
        
        DcSettings.set(DcRepository.Settings.stICalendarExportDialogSize, getSize());

        ffTarget = null;
        textLog = null;
        progressBar = null;
        
        super.close();
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("export"))
            export();
        else if (ae.getActionCommand().equals("cancel"))
            close();
    }
}