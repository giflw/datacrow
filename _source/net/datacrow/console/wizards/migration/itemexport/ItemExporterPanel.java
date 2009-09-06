package net.datacrow.console.wizards.migration.itemexport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.migration.itemexport.IItemExporterClient;
import net.datacrow.core.migration.itemexport.ItemExporter;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class ItemExporterPanel extends ItemExporterWizardPanel implements IItemExporterClient  {

	private static Logger logger = Logger.getLogger(ItemExporterPanel.class.getName());
	
    private JTextArea textLog = ComponentFactory.getTextArea();
    private JProgressBar progressBar = new JProgressBar();
    
    private ItemExporter exporter;
    
    public ItemExporterPanel(ItemExporterWizard wizard) {
        super(wizard);
        build();
    }
    
    public Object apply() throws WizardException {
        return wizard.getDefinition();
    }

	@Override
    public void destroy() {
    	if (exporter != null) exporter.cancel();
    	exporter = null;
    	progressBar = null;
    	textLog = null;
    	wizard = null;
    }

    public String getHelpText() {
        return DcResources.getText("msgExportProcess");
    }
    
    @Override
    public void onActivation() {
    	if (definition != null && definition.getExporter() != null) {
    		this.exporter = wizard.getDefinition().getExporter();
    		start();
    	}
	}

    @Override
	public void onDeactivation() {
		cancel();
	}

    private void start() {
        exporter.setClient(this);
    	
    	try { 
    	    //if (exporter.getFile() == null)
	        exporter.setFile(wizard.getDefinition().getFile());
    	    exporter.setSettings(definition.getSettings());
    	    exporter.setItems(wizard.getItems());
    	    exporter.start();
    	    
    	} catch (Exception e ) {
    	    notifyMessage(e.getMessage());
    	    logger.error(e, e);
    	}
    }
    
    private void build() {
        setLayout(Layout.getGBL());

        //**********************************************************
        //Progress panel
        //**********************************************************
        JPanel panelProgress = new JPanel();
        panelProgress.setLayout(Layout.getGBL());
        panelProgress.add(progressBar, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                         ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                          new Insets(5, 5, 5, 5), 0, 0));
        
        //**********************************************************
        //Log Panel
        //**********************************************************        
        JPanel panelLog = new JPanel();
        panelLog.setLayout(Layout.getGBL());

        JScrollPane scroller = new JScrollPane(textLog);
        textLog.setEditable(false);

        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelLog.add(scroller, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0));

        panelLog.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblLog")));

		add(panelProgress,  Layout.getGBC( 0, 0, 1, 1, 10.0, 1.0
		        ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
		         new Insets( 5, 5, 5, 5), 0, 0));
		add(panelLog,     Layout.getGBC( 0, 1, 1, 1, 20.0, 20.0
				,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				 new Insets( 5, 5, 5, 5), 0, 0));
    }
    
    private void cancel() {
        if (exporter != null) exporter.cancel();
        notifyStopped();
    }    
    
    public void notifyMessage(String message) {
        if (textLog != null) {
            textLog.insert(message + '\n', 0);
            textLog.setCaretPosition(0);
        }
    }

    public void notifyStarted(int count) {
        progressBar.setValue(0);
        progressBar.setMaximum(count);
    }

    public void notifyStopped() {}

    public void notifyProcessed() {
        progressBar.setValue(progressBar.getValue() + 1);       
    }
}
