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
import java.awt.Point;
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
import net.datacrow.core.DataCrow;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

public class SaveQuestionBox extends DcDialog implements ActionListener {

    public static final int _CONTINUE = 0;
    public static final int _IGNORE = 1;
    public static final int _CANCEL = 2;

    private int result = -1;

    private JTextArea textMessage;
    private JButton buttonContinue;
    private JButton buttonIgnore;
    private JButton buttonCancel;

    private JPanel panel = new JPanel();

    private final JLabel labelIcon = ComponentFactory.getLabel("");

    public SaveQuestionBox(ValidationException vExp) {
        super(DcSwingUtilities.getRootFrame());
        init(vExp);
    }

    public SaveQuestionBox(ValidationException vExp, JFrame parent) {
        super(parent);
        init(vExp);
    }

    @Override
    public void close() {
        textMessage = null;
        buttonContinue = null;
        buttonIgnore = null;
        buttonCancel = null;
        super.close();
    }

    public int getResult() {
        return result;
    }

    private void init(ValidationException vExp) {
        buildDialog();
        textMessage.setText(vExp.getMessage());

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        this.pack();
        this.setModal(true);

        if (DataCrow.mainFrame != null) {
            Dimension size = DataCrow.mainFrame.getSize();
            Point location = DataCrow.mainFrame.getLocation();
            int x = ((location.x + size.width - getSize().width) / 2);
            int y = ((location.y + size.height  - getSize().height) / 2);
            setLocation(x, y);
        } else {
        	setLocation(150, 150);
        }

        buttonContinue.requestFocus();
        this.setVisible(true);
    }

    private void buildDialog() {
        this.setResizable(false);
        this.getContentPane().setLayout(new GridLayout());

        // Input panel
        panel.setLayout(Layout.getGBL());

        textMessage = ComponentFactory.getTextArea();
        textMessage.setBackground(getBackground());
        textMessage.setEditable(false);
        JScrollPane scrollIn = new JScrollPane(textMessage);
        scrollIn.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollIn.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollIn.setPreferredSize(new Dimension(350,50));
        scrollIn.setBorder(null);

        buttonContinue = ComponentFactory.getButton(DcResources.getText("lblContinue"));
        buttonIgnore = ComponentFactory.getButton(DcResources.getText("lblIgnoreErrors"));
        buttonCancel = ComponentFactory.getButton(DcResources.getText("lblCancel"));

        buttonContinue.setToolTipText(DcResources.getText("tpContinue"));
        buttonIgnore.setToolTipText(DcResources.getText("tpIgnoreErrors"));
        buttonCancel.setToolTipText(DcResources.getText("tpCancel"));

        buttonContinue.addActionListener(this);
        buttonContinue.setActionCommand("continue");
        buttonIgnore.addActionListener(this);
        buttonIgnore.setActionCommand("ignore");
        buttonCancel.addActionListener(this);
        buttonCancel.setActionCommand("cancel");

        JPanel panelActions = new JPanel();
        panelActions.add(buttonContinue);
        panelActions.add(buttonIgnore);
        panelActions.add(buttonCancel);
        
        labelIcon.setIcon(IconLibrary._icoQuestion);

        JLabel labelMessage = ComponentFactory.getLabel(DcResources.getText("lblItemCannotBeSaved"));

        panel.setLayout(Layout.getGBL());
        panel.add(labelMessage,     Layout.getGBC( 0, 0, 2, 1, 0.0, 0.0
                                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
								    new Insets(5, 5, 5, 5), 0, 0));
        panel.add(labelIcon,        Layout.getGBC( 0, 1, 1, 1, 0.0, 0.0
                                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                                    new Insets(5, 5, 5, 5), 0, 0));
        panel.add(scrollIn,         Layout.getGBC( 1, 1, 1, 1, 90.0, 90.0
                                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
								    new Insets(5, 5, 5, 5), 0, 0));
        panel.add(panelActions,    Layout.getGBC( 0, 2, 3, 1, 0.0, 0.0
                                   ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
								    new Insets(5, 5, 5, 5), 0, 0));

        this.pack();
        this.setModal(true);
        this.getContentPane().add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("continue"))
            result = SaveQuestionBox._CONTINUE;
        else if (ae.getActionCommand().equals("ignore"))
            result = SaveQuestionBox._IGNORE;
        else if (ae.getActionCommand().equals("cancel"))
            result = SaveQuestionBox._CANCEL;

        close();
    }
}