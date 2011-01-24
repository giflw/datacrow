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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcProgressBar;
import net.datacrow.console.components.renderers.CheckBoxTableCellRenderer;
import net.datacrow.console.components.renderers.DcTableHeaderRenderer;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.views.View;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcSwingUtilities;

import org.apache.log4j.Logger;

public class FindReplaceTaskDialog extends DcDialog implements ActionListener {

    private static Logger logger = Logger.getLogger(FindReplaceTaskDialog.class.getName());
    
    private JProgressBar pb = new DcProgressBar();
    
    private boolean keepOnRunning = true;
    
    private JButton buttonApply;
    private JButton buttonClose;

    private DcModule module;
    private View view;
    
    private DcTable tblItems;
    
    private int[] fields;
    private int field;
    private String replacement;
    private String value;
    private List<DcObject> items;

    public FindReplaceTaskDialog(JFrame parent, View view, List<DcObject> items, int[] fields, int field, String value, String replacement) {
        super(parent);
        setTitle(DcResources.getText("lblFindReplace"));
        
        this.view = view;
        this.module = view.getModule();
        this.items = items;
        this.field = field; 
        this.replacement = replacement;
        this.value = value;
        this.fields = fields;
        
        setHelpIndex("dc.tools.findreplace");
        buildDialog(module);
        setSize(DcSettings.getDimension(DcRepository.Settings.stFindReplaceTaskDialogSize));

        setCenteredLocation();
    }
    
    public void stop() {
        keepOnRunning = false;
    }

    private void replace() {
        Updater updater = new Updater();
        updater.start();
    }

    public void initProgressBar(int maxValue) {
        pb.setValue(0);
        pb.setMaximum(maxValue);
    }

    public void updateProgressBar(int value) {
        pb.setValue(value);
    }    
    
    private class Updater extends Thread {
        
        @Override
        public void run() {
            
            int count = 1;
            initProgressBar(items.size());
            view.setListSelectionListenersEnabled(false);
            try {
                
                DcObject item = module.getItem();
                int colID = tblItems.getColumnIndexForField(DcObject._ID);
                int colValue = tblItems.getColumnIndexForField(field);
                int colEnabled = tblItems.getColumnModel().getColumnIndex("ENABLED");
                
                for (int row = 0; row < tblItems.getRowCount(); row++) {
                    
	                if (!keepOnRunning) break;
	                
	                if ((Boolean) tblItems.getValueAt(row, colEnabled)) {
    	                item.markAsUnchanged();
    	                item.setValue(DcObject._ID, tblItems.getValueAt(row, colID, true));
    	                item.setValue(field, tblItems.getValueAt(row, colValue, true));
    	                try {
                            if (view.getType() == View._TYPE_SEARCH) {
                            	item.saveUpdate(false, false);
                            } else if (view.getType() == View._TYPE_INSERT) {
                                view.updateItem(item.getID(), item);
                            }
                        } catch (Exception e) {
                            // warn the user of the event that occurred (for example an incorrect parent for a container)
                            DcSwingUtilities.displayErrorMessage(e.getMessage());
                        }
	                }

	                updateProgressBar(count);

	                try {
	                    sleep(100);
	                } catch (Exception e) {
	                    logger.error(e, e);
	                }
	                
	                count++;
	            }
            } finally {
            	if (view != null) view.setListSelectionListenersEnabled(true);
            }
        }
    }

    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stFindReplaceTaskDialogSize, getSize());
        
        buttonApply = null;
        buttonClose = null;
        view = null;
        module = null;
        
        super.close();
    }

    private void buildDialog(DcModule module) {
        //**********************************************************
        //Input panel
        //**********************************************************
        JPanel panelInput = new JPanel();
        panelInput.setLayout(Layout.getGBL());
        
        tblItems = ComponentFactory.getDCTable(module, false, false);
        JScrollPane sp = new JScrollPane(tblItems);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tblItems.activate();
        tblItems.setVisibleColumns(fields);
        
        TableColumn c = new TableColumn();
        
        JCheckBox checkEnabled = new JCheckBox();
        checkEnabled.addActionListener(this);
        checkEnabled.setActionCommand("checkDependencies");
        c.setCellEditor(new DefaultCellEditor(checkEnabled));
        c.setCellRenderer(CheckBoxTableCellRenderer.getInstance());
        c.setHeaderValue(DcResources.getText("lblReplace"));
        c.setIdentifier("ENABLED");
        c.setHeaderRenderer(DcTableHeaderRenderer.getInstance());
        tblItems.addColumn(c);
        
        for (DcObject item : items) {
            String oldvalue = (String) item.getValue(field);
            oldvalue = oldvalue.replaceAll("(?i)" + value, replacement);
            item.setValue(field, oldvalue);
            tblItems.add(item);
        }
        
        for (int row = 0; row < tblItems.getRowCount(); row++)
            tblItems.getDcModel().setValueAt(Boolean.TRUE, row, tblItems.getColumn("ENABLED").getModelIndex());
        
        panelInput.add(sp, Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));        
  
        //**********************************************************
        //Action panel
        //**********************************************************
        JPanel panelActions = new JPanel();

        buttonApply = ComponentFactory.getButton(DcResources.getText("lblApply"));
        buttonClose = ComponentFactory.getButton(DcResources.getText("lblCancel"));

        buttonApply.addActionListener(this);
        buttonApply.setActionCommand("start");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        panelActions.add(buttonApply);
        panelActions.add(buttonClose);
        
        //**********************************************************
        //Progress panel
        //**********************************************************        
        JPanel panelProgress = new JPanel();
        panelProgress.setLayout(Layout.getGBL());
        panelProgress.add(pb, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                         ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                          new Insets(5, 5, 5, 5), 0, 0));        
        
        //**********************************************************
        //Main panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());
        this.getContentPane().add(panelInput  ,Layout.getGBC(0, 0, 1, 1, 10.0, 10.0
                                              ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                               new Insets(5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelActions,Layout.getGBC(0, 2, 1, 1, 1.0, 1.0
                                              ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelProgress, Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                                              ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                              new Insets( 0, 0, 0, 0), 0, 0));        

        pack();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close")) {
            stop();
            close();
        } else if (ae.getActionCommand().equals("start")) {
            replace();
        }
    }  
}
