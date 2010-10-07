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

package net.datacrow.console.windows.security;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcPasswordField;
import net.datacrow.console.components.DcShortTextField;
import net.datacrow.console.windows.DcDialog;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

public class LoginDialog extends DcDialog implements ActionListener, KeyListener {
    
    private DcShortTextField fldLoginName = ComponentFactory.getShortTextField(255);
    private DcPasswordField fldPassword = ComponentFactory.getPasswordField();
    
    private boolean canceled = false;
    
    public LoginDialog() {
        super((JFrame) null);
        build();
        setTitle(DcResources.getText("lblLogin"));
        pack();
        toFront();
        setCenteredLocation();
        fldLoginName.requestFocusInWindow();
    }
    
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void close() {
        setVisible(false);
    }
    
    public String getLoginName() {
        return fldLoginName.getText().toLowerCase();
    }

    public String getPassword() {
        return String.valueOf(fldPassword.getPassword());
    }

    private void login() {
        if (fldLoginName.getText().length() == 0) 
            DcSwingUtilities.displayMessage("msgPleaseEnterUsername");
        else
            close();
    }
    
    public void clear() {
        super.close();
        fldLoginName = null;
        fldPassword = null;
    }

    private void build() {
         getContentPane().setLayout(Layout.getGBL());
         getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblLoginname")),   
                 Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
         getContentPane().add(fldLoginName, Layout.getGBC(1, 0, 1, 1, 1.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
         getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblPassword")),   
                 Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
         getContentPane().add(fldPassword, Layout.getGBC(1, 1, 1, 1, 1.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
         
         JPanel panelActions = new JPanel();
         
         JButton btOk = ComponentFactory.getButton(DcResources.getText("lblOK"));
         JButton btCancel = ComponentFactory.getButton(DcResources.getText("lblCancel"));
         
         btOk.setActionCommand("ok");
         btCancel.setActionCommand("cancel");
         btOk.addActionListener(this);
         btCancel.addActionListener(this);
         
         fldPassword.addKeyListener(this);
         fldLoginName.addKeyListener(this);
         
         panelActions.add(btOk);
         panelActions.add(btCancel);
         
         getContentPane().add(panelActions, Layout.getGBC(0, 2, 2, 1, 1.0, 1.0,
                 GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("ok")) {
            login();
        } else if (ae.getActionCommand().equals("cancel")) {
            canceled = true;
            close();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            login();
    }

    @Override
    public void keyPressed(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}
