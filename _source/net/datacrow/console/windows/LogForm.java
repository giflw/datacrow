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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFrame;
import net.datacrow.console.components.DcImageLabel;
import net.datacrow.core.DataCrow;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.logging.ITextPaneAppenderListener;

public class LogForm extends DcFrame implements ITextPaneAppenderListener {

    private JTextArea logger;
    private JLabel labelVersion = ComponentFactory.getLabel(DataCrow.getVersion().getFullString());

    private static final LogForm me = new LogForm();

    public static LogForm getInstance() {
        return me;
    }
    
    private LogForm() {
        super(DcResources.getText("lblLog"), IconLibrary._icoMain);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        buildPanel();
    }
    
    @Override
    public void setFont(Font font) {
        if (logger != null) {
            labelVersion.setFont(ComponentFactory.getSystemFont());
            logger.setFont(ComponentFactory.getStandardFont());
        }
    }
    
    public void add(String message) {
        logger.insert("\r\n\r\n", 0);
        logger.insert(message, 0);
    }    

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) toFront();
    }

    @Override
	public void close() {
    	setVisible(false);
	}

	private void buildPanel() {
        
        JPanel panelLogging = new JPanel();
        JPanel panelPicture = new JPanel();
        JPanel panelProductInfo = new JPanel();

        //**********************************************************
        //Logo panel
        //**********************************************************
        panelPicture.setLayout(Layout.getGBL());
        DcImageLabel logo = ComponentFactory.getImageLabel(new ImageIcon(DataCrow.installationDir + "icons/logo.jpg"));
        ComponentFactory.setBorder(panelPicture);

        panelPicture.setPreferredSize(new Dimension(575,200));
        panelPicture.setMinimumSize(new Dimension(575,200));
        panelPicture.add(logo,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                               ,GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                                new Insets(0, 0, 0, 0), 0, 0));

        //**********************************************************
        //Logging panel
        //**********************************************************
        panelLogging.setLayout(Layout.getGBL());

        logger = ComponentFactory.getTextArea();
        logger.setEditable(true);
        
        JScrollPane scroller = new JScrollPane(logger);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panelLogging.add(scroller,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                    new Insets( 0, 5, 0, 5), 0, 0));

        //**********************************************************
        //Product information panel
        //**********************************************************
        panelProductInfo.setLayout(Layout.getGBL());

        panelProductInfo.add(labelVersion,     Layout.getGBC( 0, 0, 1, 1, 0.0, 0.0
                                              ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                               new Insets( 0, 5, 0, 0), 0, 0));

        //**********************************************************
        //Main panel
        //**********************************************************
        getContentPane().setLayout(Layout.getGBL());
        
        // Build the panel
        this.getContentPane().add(   panelPicture,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                                                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                                                    new Insets( 5, 5, 0, 5), 0, 0));
        this.getContentPane().add(   panelLogging,  Layout.getGBC( 0, 1, 1, 1, 20.0, 20.0
                                                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                                    new Insets( 0, 0, 0, 0), 0, 0));
        this.getContentPane().add(panelProductInfo, Layout.getGBC( 0, 2, 1, 1, 0.0, 0.0
                                                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                                                    new Insets( 5, 0, 5, 0), 0, 0));
        pack();

        setSize(590, 600);
        setResizable(false);
        
        setCenteredLocation();
    }
}
