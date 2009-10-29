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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFrame;
import net.datacrow.console.views.View;
import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

import org.apache.log4j.Logger;

public class UpdateAllDialog extends DcFrame implements ActionListener {

    private static Logger logger = Logger.getLogger(UpdateAllDialog.class.getName());
    
    private JButton buttonApply;
    private JButton buttonClose;

    private boolean keepOnRunning = true;
    private JProgressBar progressBar = new JProgressBar();
    
    private View view;
    private ItemForm itemForm;
    private DcModule module;

    private JCheckBox checkSelectedItemsOnly;

    public UpdateAllDialog(View view) {

        super(DcResources.getText("lblUpdateAll"), IconLibrary._icoUpdateAll);
        
        this.view = view;
        this.module = view.getModule();

        setHelpIndex("dc.tools.updateall");

        buildDialog(module);

        setSize(DcSettings.getDimension(
                DcRepository.Settings.stUpdateAllDialogSize));

        checkSelectedItemsOnly.setSelected(
                DcSettings.getBoolean(DcRepository.Settings.stUpdateAllSelectedItemsOnly));

        setCenteredLocation();
    }

    private void updateAll() {
        Updater updater = new Updater();
        updater.start();
    }

    public void initProgressBar(int maxValue) {
        progressBar.setValue(0);
        progressBar.setMaximum(maxValue);
    }

    public void updateProgressBar(int value) {
        progressBar.setValue(value);
    }    
    
    private class Updater extends Thread {
        
        @Override
        public void run() {
            DcObject dco = itemForm.getItem();
            
            Collection<? extends DcObject> c;
            if (isUpdateSelectedItemsOnly()) {
                c = view.getSelectedItems();
            } else {
                c = view.getItems();
            }

            int count = 1;
            initProgressBar(c.size());
            view.setListSelectionListenersEnabled(false);
            try {
                
	            for (DcObject item : c) {
	                
	                if (!keepOnRunning) break;
	                
	                item.copy(dco, true, false);
	                
	                try {
	                    
	                    if (item.isChanged())
	                        item.saveUpdate(false, false);
	                    
                    } catch (ValidationException ve) {
                        logger.error(ve, ve);
                    }
	                
	                updateProgressBar(count);
	
	                try {
	                    sleep(200);
	                } catch (Exception e) {
	                    logger.error(e, e);
	                }
	                
	                count++;
	            }
            } finally {
            	view.setListSelectionListenersEnabled(true);
            }
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    close();
                }
            });
        }
    }

    public boolean isUpdateSelectedItemsOnly() {
    	return checkSelectedItemsOnly.isSelected();
    }

    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stUpdateAllDialogSize, getSize());
        
        if (checkSelectedItemsOnly != null) {
            DcSettings.set(DcRepository.Settings.stUpdateAllSelectedItemsOnly,
                           checkSelectedItemsOnly.isSelected());
        }

        keepOnRunning = false;
        
        buttonApply = null;
        buttonClose = null;
        view = null;
        checkSelectedItemsOnly = null;
        module = null;
        
        if (itemForm != null) {
            itemForm.close(true);
            itemForm = null;
        }

        super.close();
    }

    private void buildDialog(DcModule module) {
        //**********************************************************
        //Input panel
        //**********************************************************
        JPanel panelInput = new JPanel();
        panelInput.setLayout(Layout.getGBL());
        
        itemForm = new ItemForm(null, false, false, module.getItem(), false);
        for (DcField field : module.getFields()) {
            if (field.getValueType() == DcRepository.ValueTypes._PICTURE)
                itemForm.hide(field);
            else if (field.getIndex() == DcObject._ID)
                itemForm.hide(field);
            else if (field.isUiOnly() && field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION)
                itemForm.hide(field);
        }
        
        panelInput.add(itemForm.getTabbedPane(), Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));

        //**********************************************************
        //Options panel
        //**********************************************************
        checkSelectedItemsOnly = ComponentFactory.getCheckBox(DcResources.getText("lblSelectedItemsOnly"));
        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(Layout.getGBL());

        panelOptions.add(checkSelectedItemsOnly    , Layout.getGBC(0, 0, 1, 1, 0.0, 0.0
                       , GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                         new Insets(5, 5, 5, 5), 0, 0));

        //**********************************************************
        //Action panel
        //**********************************************************
        JPanel panelActions = new JPanel();

        buttonApply = ComponentFactory.getButton(DcResources.getText("lblApply"));
        buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));

        buttonApply.addActionListener(this);
        buttonApply.setActionCommand("updateAll");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        panelActions.add(buttonApply);
        panelActions.add(buttonClose);
        
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
        this.getContentPane().add(panelInput  ,Layout.getGBC(0, 0, 1, 1, 10.0, 10.0
                                              ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                               new Insets(5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelOptions,Layout.getGBC(0, 1, 1, 1, 1.0, 1.0
                                              ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                                               new Insets(10, 5, 10, 5), 0, 0));
        this.getContentPane().add(panelActions,Layout.getGBC(0, 2, 1, 1, 1.0, 1.0
                                              ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelProgress, Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                                              ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                              new Insets( 0, 0, 0, 0), 0, 0));        

        pack();
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("updateAll"))
            updateAll();
    }  
}
