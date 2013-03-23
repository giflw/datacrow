package net.datacrow.console.wizards.itemimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import net.datacrow.console.Layout;
import net.datacrow.console.components.panels.TaskPanel;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.migration.itemimport.IItemImporterClient;
import net.datacrow.core.migration.itemimport.ItemImporter;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class ItemImporterTaskPanel extends ItemImporterWizardPanel implements IItemImporterClient  {

	private static Logger logger = Logger.getLogger(ItemImporterTaskPanel.class.getName());
	
	private int created = 0;
	private int updated = 0;
	
    private ItemImporterWizard wizard;
    private ItemImporter importer;
    
    private TaskPanel tp = new TaskPanel(TaskPanel._SINGLE_PROGRESSBAR);
    
    public ItemImporterTaskPanel(ItemImporterWizard wizard) {
        this.wizard = wizard;
        build();
    }
    
	@Override
    public Object apply() throws WizardException {
        return wizard.getDefinition();
    }

    @Override
    public void destroy() {
    	if (importer != null) importer.cancel();
    	importer = null;
        if (tp != null) tp.destroy();
        tp = null;
    	wizard = null;
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgImportProcess");
    }
    
    @Override
    public void onActivation() {
    	if (wizard.getDefinition() != null) {
    		this.importer = wizard.getDefinition().getImporter();
    		start();
    	}
	}

    @Override
	public void onDeactivation() {
		cancel();
	}

    private void start() {
    	importer.setClient(this);
    	
    	try { 
    	    
    	    created = 0;
    	    updated = 0;
    	    
    	    if (importer.getFile() == null)
    	        importer.setFile(wizard.getDefinition().getFile());
    	    
    	    importer.start();
    	    
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
        if (importer != null) importer.cancel();
        notifyStopped();
    }    
    
    @Override
    public void notifyMessage(String msg) {
        tp.addMessage(msg);
    }

    @Override
    public void notifyStarted(int count) {
        tp.clear();
        tp.initializeTask(count);
    }

    @Override
    public void notifyStopped() {
        notifyMessage("\n");
        notifyMessage(DcResources.getText("msgItemsCreated", String.valueOf(created)));
        notifyMessage(DcResources.getText("msgItemsUpdated", String.valueOf(updated)));
        notifyMessage(DcResources.getText("msgItemsImported", String.valueOf(updated + created)));
        notifyMessage("\n");
        notifyMessage(DcResources.getText("msgImportFinished"));
        
        DcModule m = wizard.getModule();
        if (m.getSearchView() != null) {
            m.getSearchView().refresh();
        }   
    }

    @Override
    public void notifyProcessed(DcObject item) {
        DcObject other = DataManager.getItem(item.getModule().getIndex(), item.getID());
        other = other == null ? DataManager.getObjectForString(item.getModule().getIndex(), item.toString()) : other;
        // Check if the item exists and if so, update the item with the found values. Else just create a new item.
        // This is to make sure the order in which the files are processed (first software, then categories)
        // is of no importance (!).
        try {
            if (other != null) {
                updated++;
                other.copy(item, true, false);
                other.saveUpdate(false, false);
            } else {
                created++;
                item.setUpdateGUI(false);
                item.setValidate(false);
                item.saveNew(false);
            }
        } catch (ValidationException ve) {
            // will not occur as validation has been disabled.
            notifyMessage(ve.getMessage());
        }
        
        tp.updateProgressTask();
        notifyMessage(DcResources.getText("msgImportedX", item.toString()));
        item.destroy();
    }
}
