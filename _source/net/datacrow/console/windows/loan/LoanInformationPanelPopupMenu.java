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

package net.datacrow.console.windows.loan;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcPopupMenu;
import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.security.SecurityCentre;

import org.apache.log4j.Logger;

public class LoanInformationPanelPopupMenu extends DcPopupMenu implements ActionListener {

    private static Logger logger = Logger.getLogger(LoanInformationPanelPopupMenu.class.getName());
    
    private DcObject dco;
    
    public LoanInformationPanelPopupMenu(DcObject dco) {
        this.dco = dco;
        JMenuItem menuOpen = ComponentFactory.getMenuItem(IconLibrary._icoOpen, DcResources.getText("lblOpenItem", dco.getModule().getObjectName()));
        JMenuItem menuEdit = ComponentFactory.getMenuItem(IconLibrary._icoOpen, DcResources.getText("lblEditItem", dco.getModule().getObjectName()));
         
        menuOpen.setActionCommand("openItem");
        menuEdit.setActionCommand("editItem");
        
        menuOpen.addActionListener(this);
        menuEdit.addActionListener(this);
        
        this.add(menuOpen);

        if (SecurityCentre.getInstance().getUser().isAuthorized(dco.getModule())) {
            this.add(menuEdit);
            if (SecurityCentre.getInstance().getUser().isEditingAllowed(dco.getModule()))
                this.add(menuEdit);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dco.markAsUnchanged();
        dco.getModule();
        
        if (e.getActionCommand().equals("openItem")) {
            ItemForm form = new ItemForm(true, true, dco, false);
            form.setVisible(true);
        } else if (e.getActionCommand().equals("editItem")) {
            ItemForm form = new ItemForm(false, true, dco, false);
            form.setVisible(true);
        }
    }
}
