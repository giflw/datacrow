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
import java.util.Collection;
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
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.ITaskListener;

import org.apache.log4j.Logger;

public class FindReplaceTaskDialog extends DcDialog implements ActionListener, ITaskListener {

    private static Logger logger = Logger.getLogger(FindReplaceTaskDialog.class.getName());
    
    private JProgressBar pb = new DcProgressBar();
    
    private boolean stopped = false;
    
    private JButton buttonApply;
    private JButton buttonClose;

    private DcModule module;
    private View view;
    
    private DcTable tblItems;
    
    private int[] fields;
    private int field;
    
    private Object replacement;
    private Object value;
    
    private List<DcObject> items;

    public FindReplaceTaskDialog(
            JFrame parent, 
            View view, 
            List<DcObject> items, 
            int[] fields, 
            int field, 
            Object value, 
            Object replacement) {
        
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

    private void replace() {
        Updater updater = new Updater(this);
        updater.start();
    }

    @Override
    public void notifyTaskSize(int size) {
        pb.setValue(0);
        pb.setMaximum(size);
    }

    @Override
    public void notify(String msg) {}

    @Override
    public void notifyTaskStopped() {
        pb.setValue(pb.getMaximum());
        buttonApply.setEnabled(true);
        close();
    }

    @Override
    public void notifyTaskStarted() {
        pb.setValue(0);
        buttonApply.setEnabled(false);
    }

    @Override
    public void notifyProcessed() {
        pb.setValue(pb.getValue() + 1);
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    private class Updater extends Thread {
        
        private ITaskListener listener;
        
        public Updater(ITaskListener listener) {
            this.listener = listener;
        }
        
        @Override
        public void run() {
            
            listener.notifyTaskStarted();
            listener.notifyTaskSize(items.size());
            view.setListSelectionListenersEnabled(false);

            try {
                DcObject item = module.getItem();
                int colID = tblItems.getColumnIndexForField(DcObject._ID);
                int colValue = tblItems.getColumnIndexForField(field);
                int colEnabled = tblItems.getColumnModel().getColumnIndex("ENABLED");
                
                for (int row = 0; row < tblItems.getRowCount(); row++) {
                    
	                if (listener.isStopped()) break;
	                
	                if ((Boolean) tblItems.getValueAt(row, colEnabled)) {
    	                item.markAsUnchanged();
    	                item.setValue(DcObject._ID, tblItems.getValueAt(row, colID, true));
    	                item.setValue(field, tblItems.getValueAt(row, colValue, true));
    	                try {
    	                    item.setUpdateGUI(false);
                        	item.saveUpdate(false, false);
                        } catch (Exception e) {
                            // warn the user of the event that occurred (for example an incorrect parent for a container)
                            DcSwingUtilities.displayErrorMessage(e.getMessage());
                        }
	                }

	                listener.notifyProcessed();

	                try {
	                    sleep(100);
	                } catch (Exception e) {
	                    logger.error(e, e);
	                }
	            }
            } finally {
                module.getSearchView().refresh();
                
            	if (view != null) {
            	    view.setListSelectionListenersEnabled(true);
            	}
            	
            	listener.notifyTaskStopped();
            }
        }
    }

    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stFindReplaceTaskDialogSize, getSize());
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
        
        Object oldValue;
        Object newValue;
        String s;
        for (DcObject item : items) {
            oldValue = (Object) item.getValue(field);
            
            if (oldValue instanceof String) {
                try {
                    s = (String) value;
                    newValue = ((String) oldValue).replaceAll("(?i)" + s, (String) replacement);
                    item.setValue(field, newValue);
                } catch (Exception e) {
                    logger.error(e, e);
                    DcSwingUtilities.displayErrorMessage(e.getMessage());
                    break;
                }
            } else if (oldValue instanceof Collection) {
                Collection<DcObject> collection = (Collection<DcObject>) oldValue;
                for (DcObject o : collection) {
                    oldValue = o;
                    if (o.getValue(DcMapping._B_REFERENCED_ID).equals(((DcObject) value).getID()))
                        break;
                }
                
                if (oldValue != null)
                    collection.remove(oldValue);
                
                DataManager.addMapping(item, (DcObject) replacement, field);
            } else {
                item.setValue(field, replacement);
            }
            
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
        buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));

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
        this.getContentPane().add(panelInput  ,Layout.getGBC(0, 0, 1, 1, 20.0, 20.0
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
            stopped = true;
            close();
        } else if (ae.getActionCommand().equals("start")) {
            replace();
        }
    }  
}
