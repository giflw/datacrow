package net.datacrow.console.wizards.migration.itemimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.DataCrow;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.migration.itemimport.IItemImporterClient;
import net.datacrow.core.migration.itemimport.ItemImporter;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class ItemImporterPanel extends ItemImporterWizardPanel implements ActionListener, IItemImporterClient  {

	private static Logger logger = Logger.getLogger(ItemImporterPanel.class.getName());
	
    private ItemImporterWizard wizard;
    private JTextArea textLog = ComponentFactory.getTextArea();
    private JProgressBar progressBar = new JProgressBar();
    
    private ItemImporter reader;
    
    public ItemImporterPanel(ItemImporterWizard wizard) {
        this.wizard = wizard;
        build();
    }
    
	public Object apply() throws WizardException {
        return wizard.getDefinition();
    }

    public void destroy() {
    	if (reader != null) reader.cancel();
    	reader = null;
    	progressBar = null;
    	textLog = null;
    	wizard = null;
    }

    public String getHelpText() {
        return null;
    }
    
    @Override
    public void onActivation() {
    	if (wizard.getDefinition() != null) {
    		this.reader = wizard.getDefinition().getReader();
    		start();
    	}
	}

    @Override
	public void onDeactivation() {
		cancel();
	}

    private void start() {
    	reader.setClient(this);
    	
    	try { 
    	    if (reader.getFile() == null)
    	        reader.setFile(wizard.getDefinition().getFile());
    	    
    	    reader.start();
    	    
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
        textLog.setEnabled(false);

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
        if (reader != null) reader.cancel();
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
        DataCrow.mainFrame.setSelectedTab(net.datacrow.console.MainFrame._INSERTTAB);
    }

    public void notifyStopped() {}

    public void notifyProcessed(DcObject item) {
        if (    wizard.getModule().isTopModule() && 
                wizard.getModule().getCurrentInsertView() != null) {
            
            wizard.getModule().getCurrentInsertView().add(item);
        } else {
            DcObject property = DataManager.getObjectForDisplayValue(item.getModule().getIndex(), item.toString());
            // Check if the item exists and if so, update the item with the found values. Else just create a new item.
            // This is to make sure the order in which XML files are processed (first software, then categories)
            // is of no importance.
            try {
                if (property != null) {
                    property.copy(item, true);
                    property.saveUpdate(false);
                } else {
                    item.saveNew(false);
                }
            } catch (ValidationException ve) {
                notifyMessage(ve.getMessage());
            }
        }
        
        progressBar.setValue(progressBar.getValue() + 1);
        notifyMessage(DcResources.getText("msgAddedX", item.toString()));
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("import"))
            start();
        else if (ae.getActionCommand().equals("cancel"))
            cancel();
    }    
}
