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

package net.datacrow.console.components;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DataCrow;
import net.datacrow.core.plugin.PluginHelper;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class DcDialog extends JDialog {

    private static Logger logger = Logger.getLogger(DcDialog.class.getName());
    
    private String helpIndex = null;

    public DcDialog(JFrame parent) {
        super(parent);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);

        PluginHelper.registerKey(getRootPane(), "Help");
        PluginHelper.registerKey(getRootPane(), "CloseWindow");
    }

    public DcDialog() {
        this(DcSwingUtilities.getRootFrame());
    }
    
    @Override
    public void setVisible(boolean b) {
        if (isModal() && DataCrow.isSplashScreenActive())
            DataCrow.showSplashScreen(false);

        super.setVisible(b);
    }

    public void close() {
        long start = logger.isDebugEnabled() ? new Date().getTime() : 0;
        
        helpIndex = null;
        
        if (rootPane.getInputMap() != null)
            rootPane.getInputMap().clear();
        
        if (rootPane.getActionMap() != null)
            rootPane.getActionMap().clear();
        
        rootPane.setActionMap(null);        
        
        ComponentFactory.clean(getContentPane());
        
        setVisible(false);
        if (DataCrow.isSplashScreenActive())
            DataCrow.showSplashScreen(true);
        
        dispose();
        
        if (logger.isDebugEnabled()) {
            long end = new Date().getTime();
            logger.debug("Disposing of the dialog and its resources took " + (end - start) + "ms");
        }  
    }

    public void setHelpIndex(String helpIndex) {
    	this.helpIndex = helpIndex;
    }

    public String getHelpIndex() {
        return helpIndex;
    }

    public void setCenteredLocation() {
        setLocation(Utilities.getCenteredWindowLocation(getSize()));
    }
    
    protected void addKeyListener(KeyStroke keyStroke, Action action, String name) {
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, name);
        rootPane.getActionMap().put(name, action);
    }    
}
