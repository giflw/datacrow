package net.datacrow.console.wizards.itemexport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import net.datacrow.console.Layout;
import net.datacrow.console.components.panels.TaskPanel;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.migration.itemexport.IItemExporterClient;
import net.datacrow.core.migration.itemexport.ItemExporter;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class ItemExporterTaskPanel extends ItemExporterWizardPanel implements IItemExporterClient  {

	private static Logger logger = Logger.getLogger(ItemExporterTaskPanel.class.getName());
	
	private TaskPanel tp = new TaskPanel(TaskPanel._SINGLE_PROGRESSBAR);
	
    private ItemExporter exporter;
    
    public ItemExporterTaskPanel(ItemExporterWizard wizard) {
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
    	if (tp != null) tp.destroy();
    	tp = null;
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
		add(tp,  Layout.getGBC( 0, 01, 1, 1, 1.0, 1.0
				,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				 new Insets( 5, 5, 5, 5), 0, 0));
    }
    
    private void cancel() {
        if (exporter != null) exporter.cancel();
        notifyStopped();
    }    
    
    public void notifyMessage(String message) {
        tp.addMessage(message);
    }

    public void notifyStarted(int count) {
        tp.clear();
        tp.initializeTask(count);
    }

    public void notifyStopped() {}

    public void notifyProcessed() {
        tp.updateProgressTask();       
    }
}