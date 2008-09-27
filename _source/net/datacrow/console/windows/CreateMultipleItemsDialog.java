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

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.DcPropertyModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcProperty;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.objects.helpers.AudioTrack;
import net.datacrow.core.objects.helpers.MusicTrack;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

public class CreateMultipleItemsDialog extends DcDialog implements ActionListener {
	
	private DcTable table;
	
	private int moduleIdx;
	
	private JButton buttonAdd = ComponentFactory.getButton(DcResources.getText("lblAdd"));
    private JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
    private JButton buttonCancel = ComponentFactory.getButton(DcResources.getText("lblCancel"));
	
	private SavingTask task;
	
	public CreateMultipleItemsDialog(int moduleIdx) {
		super();
		
		this.moduleIdx = moduleIdx;
		this.table = new DcTable(DcModules.get(moduleIdx), false, false);
		
		build();
		
		pack();
		setCenteredLocation();
	}
	
	private void save() {
		if (task == null || !task.isAlive()) {
			task = new SavingTask();
			task.start();
		}
	}
	
	private void add() {
		DcModule module = DcModules.get(moduleIdx);
		DcObject dco = module.getDcObject();
		if (module.isChildModule()) {
			String parentID;
			if (DcSwingUtilities.getRootFrame() instanceof ItemForm)
				parentID = ((ItemForm) DcSwingUtilities.getRootFrame()).getItem().getID();
			else
				parentID = module.getParent().getCurrentSearchView().getSelectedItem().getID();
			
			dco.setValue(dco.getParentReferenceFieldIndex(), parentID);
			
			if (dco instanceof AudioTrack)
				dco.setValue(AudioTrack._F_TRACKNUMBER, Long.valueOf(table.getRowCount() + 1));
			else if (dco instanceof MusicTrack)
				dco.setValue(AudioTrack._F_TRACKNUMBER, Long.valueOf(table.getRowCount() + 1));
		}
		table.add(dco);
	}
	
	public void setActionsAllowed(boolean b) {
		buttonAdd.setEnabled(b);
		buttonSave.setEnabled(b);
		buttonCancel.setEnabled(b);
	}
	
	@Override
    public void close() {
		if (task == null || !task.isAlive()) {
			buttonAdd = null;
			buttonSave = null;
			buttonCancel = null;
			task = null;
			super.close();
		}
    }

	private void build() {
        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        if (DcModules.get(moduleIdx) instanceof DcPropertyModule) {
        	table.setIgnoreSettings(true);
        	table.setVisibleColumns(new int[] {DcProperty._A_NAME});
        }
        
        getContentPane().setLayout(Layout.getGBL());
        
        getContentPane().add(sp,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));

        JPanel panelActions = new JPanel();
        
        buttonAdd.addActionListener(this);
        buttonSave.addActionListener(this);
        buttonCancel.addActionListener(this);
        
        buttonAdd.setActionCommand("add");
        buttonSave.setActionCommand("save");
        buttonCancel.setActionCommand("cancel");
        
        panelActions.add(buttonAdd);
        panelActions.add(buttonSave);
        panelActions.add(buttonCancel);
        
        getContentPane().add(panelActions, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets(0, 0, 0, 0), 0, 0));
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("save"))
			save();
		else if (ae.getActionCommand().equals("cancel"))
			close();
		else if (ae.getActionCommand().equals("add"))
			add();
    }
	
	private class SavingTask extends Thread {
		@Override
		public void run() {
			for (int row = table.getRowCount(); row > 0; row--) {
				DcObject dco = table.getItemAt(row - 1);
				dco.setIDs();
				
				try {
					dco.saveNew(false);
					table.removeRow(row - 1);
				} catch (ValidationException e) {
					new MessageBox(e.getMessage(), MessageBox._WARNING);
				}
			}
		}
	}	
}