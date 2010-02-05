package net.datacrow.console.windows.log;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.core.DataCrow;
import net.datacrow.util.logging.ITextPaneAppenderListener;

public class LogPanel extends JPanel implements ITextPaneAppenderListener {
    
    private JTextArea logger;
    private JLabel labelVersion = ComponentFactory.getLabel(DataCrow.getVersion().getFullString());

    private static final LogPanel me = new LogPanel();

    public static LogPanel getInstance() {
        return me;
    }
    
    private LogPanel() {
        buildPanel();
    }
    
    @Override
    public void setFont(Font font) {
        if (logger != null) {
            labelVersion.setFont(ComponentFactory.getSystemFont());
            logger.setFont(ComponentFactory.getStandardFont());
        }
    }
    
    public void add(String message) {
        logger.insert("\r\n\r\n", 0);
        logger.insert(message, 0);
    }      

    private void buildPanel() {
        
        JPanel panelLog = new JPanel();
        JPanel panelInfo = new JPanel();

        //**********************************************************
        //Logging panel
        //**********************************************************
        panelLog.setLayout(Layout.getGBL());

        logger = ComponentFactory.getTextArea();
        logger.setEditable(true);
        
        JScrollPane scroller = new JScrollPane(logger);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panelLog.add(scroller, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 0, 5), 0, 0));

        //**********************************************************
        //Product information panel
        //**********************************************************
        panelInfo.setLayout(Layout.getGBL());

        panelInfo.add(labelVersion, Layout.getGBC( 0, 0, 1, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 5, 0, 0), 0, 0));

        //**********************************************************
        //Main panel
        //**********************************************************
        setLayout(Layout.getGBL());
        
        add(panelLog,    Layout.getGBC( 0, 1, 1, 1, 20.0, 50.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets( 5, 0, 0, 0), 0, 0));
        add(panelInfo, Layout.getGBC( 0, 2, 1, 1, 0.0, 0.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
            new Insets( 5, 0, 5, 0), 0, 0));
    }
}
