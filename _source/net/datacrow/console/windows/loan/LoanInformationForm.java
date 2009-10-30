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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.DcFrame;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;

public class LoanInformationForm extends DcFrame implements ActionListener {
    
	public LoanInformationForm() {
		this(null);
	}
	
    public LoanInformationForm(DcObject person) {
        super(DcResources.getText("lblLoanInformation"), IconLibrary._icoLoan);
        build(person);
        pack();
        
        setCenteredLocation();
    }
    
    private void build(DcObject person) {
    	LoanInformationPanel panel = new LoanInformationPanel(person);

    	getContentPane().setLayout(Layout.getGBL());
    	
        getContentPane().add(panel,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        JButton buttonOk = ComponentFactory.getButton(DcResources.getText("lblOK"));
        buttonOk.setActionCommand("ok");
        buttonOk.addActionListener(this);
        
        JPanel pActions = new JPanel();
        pActions.add(buttonOk);
        
        getContentPane().add(pActions,  Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("ok"))
            close();
    }
}
