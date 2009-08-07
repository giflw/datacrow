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

package net.datacrow.console.wizards.migration.itemimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.migration.itemimport.ItemImporter;
import net.datacrow.core.migration.itemimport.ItemImporters;

public class ItemImporterSelectionPanel extends ItemImporterWizardPanel implements MouseListener {

	private ItemImporterWizard wizard;
	private ButtonGroup bg;
	private Collection<ItemImporter> readers = new ArrayList<ItemImporter>();
	
    public ItemImporterSelectionPanel(ItemImporterWizard wizard) {
    	this.wizard = wizard;
    	this.bg = new ButtonGroup();
    	
    	build();
    }

    public String getHelpText() {
		return "";
	}

	public Object apply() {
	    return wizard.getDefinition();
    }

    public void destroy() {
        wizard = null;
        bg  = null;
        if (readers != null) readers.clear();
        readers = null;
    }      
    
    private void build() {
        setLayout(Layout.getGBL());
        
        int y = 0;
        int x = 0;
        
        for (ItemImporter reader : ItemImporters.getInstance().getSourceReaders(wizard.getModuleIdx())) {
        	readers.add(reader);

        	JRadioButton rb = ComponentFactory.getRadioButton(reader.getName(), reader.getIcon(), reader.getKey());
            rb.addMouseListener(this);
            bg.add(rb);
            add(rb, Layout.getGBC( x, y++, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets( 0, 5, 5, 5), 0, 0));
        }
    }
    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {
        String command = bg.getSelection().getActionCommand();
        for (ItemImporter reader : readers) {
        	if (reader.getKey().equals(command)) {
        		wizard.getDefinition().setReader(reader);
        		try {
        			wizard.next();
        		} catch (WizardException we) {
        			new MessageBox(we.getMessage(), MessageBox._WARNING);
        		}
        	}
        }
    }    
}
