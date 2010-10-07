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

package net.datacrow.console.windows.messageboxes;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.DcDialog;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

public class QuestionBox extends DcDialog implements ActionListener {

    private JTextArea textMessage;
    private JButton buttonYes;
    private JButton buttonNo;
    private JPanel panel = new JPanel();
    private boolean affirmative = false;
    
    private final JLabel labelIcon = ComponentFactory.getLabel("");

    public QuestionBox(String message) {
        super(DcSwingUtilities.getRootFrame());
        init(message);
    }

    public QuestionBox(String message, JFrame parent) {
        super(parent);
        init(message);
    }

    public boolean isAffirmative() {
        return affirmative;
    }
    
    @Override
    public void close() {
        textMessage = null;
        buttonYes = null;
        buttonNo = null;
        panel = null;   
        
        super.close();
    }
    
    private void init(String message) {
        buildDialog();
        textMessage.setText(message);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        this.pack();
        this.setModal(true);

        setCenteredLocation();

        buttonYes.requestFocus();
    }

    private void buildDialog() {
        this.setResizable(false);
        this.getContentPane().setLayout(new GridLayout());

        // Input panel
        panel.setLayout(Layout.getGBL());

        textMessage = ComponentFactory.getTextArea();
        textMessage.setEditable(false);
        textMessage.setBackground(panel.getBackground());
        
        JScrollPane scrollIn = new JScrollPane(textMessage);
        scrollIn.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollIn.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollIn.setPreferredSize(new Dimension(350,50));
        scrollIn.setBorder(null);

        buttonYes = ComponentFactory.getButton(DcResources.getText("lblYes"));
        buttonNo = ComponentFactory.getButton(DcResources.getText("lblNo"));

        buttonYes.addActionListener(this);
        buttonYes.setActionCommand("confirm");
        buttonNo.addActionListener(this);
        buttonNo.setActionCommand("cancel");
        
        JPanel panelAction = new JPanel();
        panelAction.add(buttonYes);
        panelAction.add(buttonNo);

        labelIcon.setIcon(IconLibrary._icoQuestion);

        panel.setLayout(Layout.getGBL());
        panel.add(labelIcon,   Layout.getGBC( 0, 0, 1, 1, 0.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
        panel.add(scrollIn,    Layout.getGBC( 1, 0, 1, 1, 90.0, 90.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        panel.add(panelAction, Layout.getGBC( 0, 1, 2, 1, 0.0, 0.0
               ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));

        this.pack();
        this.setModal(true);
        this.getContentPane().add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("confirm")) {
            affirmative = true;
            close();
        } else if (e.getActionCommand().equals("cancel")) {
            affirmative = false;
            close();
        }
    }
}