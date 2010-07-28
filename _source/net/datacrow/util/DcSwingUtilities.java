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

package net.datacrow.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.datacrow.console.windows.IDialog;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.windows.messageboxes.QuestionBox;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

import org.apache.log4j.Logger;

public class DcSwingUtilities {
    
    private static Logger logger = Logger.getLogger(DcSwingUtilities.class.getName());
    
    private static JFrame rootFrame = null;

    public static void setRootFrame(JFrame f) {
    	rootFrame = f;
    }
    
    public static JFrame getRootFrame() {
        if (rootFrame != null) 
            return rootFrame;    
        return DataCrow.mainFrame;
    }

    /**
     * Opens a Question dialog. The message can either be a string or a resource key. 
     * @param msg Message string or resource key.
     * @return
     */
    public static boolean displayQuestion(String msg) {
        QuestionBox mb = new QuestionBox(msg.startsWith("msg") ? DcResources.getText(msg) : msg);
        open(mb);
        return mb.isAffirmative();
    }

    /**
     * Opens an information dialog. The message can either be a string or a resource key. 
     * @param msg Message string or resource key.
     * @return
     */    
    public static void displayMessage(String msg) {
        MessageBox mb = new MessageBox(msg.startsWith("msg") ? DcResources.getText(msg) : msg, MessageBox._INFORMATION);
        open(mb);
    }

    /**
     * Opens an error dialog. The message can either be a string or a resource key. 
     * @param msg Message string or resource key.
     * @return
     */    
    public static void displayErrorMessage(String msg) {
        MessageBox mb = new MessageBox(msg.startsWith("msg") ? DcResources.getText(msg) : msg, MessageBox._ERROR);
        open(mb);
    }
    
    /**
     * Opens a warning dialog. The message can either be a string or a resource key. 
     * @param msg Message string or resource key.
     * @return
     */    
    public static void displayWarningMessage(String msg) {
        String text = msg != null && msg.startsWith("msg") ? DcResources.getText(msg) : msg;
        MessageBox mb = new MessageBox(text, MessageBox._WARNING);
        open(mb);
    }    

    /**
     * Opens a dialog in the right way:

     * - When the GUI has not been initialized the dialog is opened in a native way.
     * - If we are not in the event dispatching thread the SwingUtilities way of opening 
     *   dialogs is used.
     * - Else we just open the dialog and wait for it to finish.
     * 
     * @param dialog Any dialog implementing the IDialog interface.
     */
    private static void open(final IDialog dialog) {
        if (DataCrow.mainFrame == null) {
            openDialogNativeModal(dialog);
        } else if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(
                        new Thread(new Runnable() { 
                            public void run() {
                                dialog.setVisible(true);
                            }
                        }));
            } catch (Exception e) {
                logger.error(e, e);
            }
        } else {
            dialog.setVisible(true);
        }
    }

    /**
     * Opens a dialog in a native fashion. The dialog blocks all input and any 
     * current running operation. This way of opening dialogs is ideal for the startup process
     * where there is no main window yet to use as the blocking source.
     *
     * @param dialog
     */
    public static void openDialogNativeModal(final IDialog dialog) {
        try {
            final AtomicBoolean active = new AtomicBoolean(true);
            
            dialog.setModal(true);
            dialog.setModal(active);
            
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        dialog.setVisible(true);
                    }
                });
                
                synchronized (active) {
                    while (active.get() == true)
                        active.wait();
                }
                
            } else {
                dialog.setVisible(true);
            }

        } catch (Exception ite) {
            // can't depend on the logger; most likely the logger has not yet been initialized
            ite.printStackTrace();
        }
    }
    
    public static Graphics setRenderingHint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        try {
            
//            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int renderingValue = (int) DcSettings.getLong(DcRepository.Settings.stFontRendering);
            if (renderingValue == 0)
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);    
            else if (renderingValue == 1)
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            else if (renderingValue == 2)
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
            else if (renderingValue == 3)
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB);
            else if (renderingValue == 4)
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR);
                
        } catch (Exception ignore) {
            logger.debug(ignore, ignore);
        } catch (Error ignore) {
            logger.debug(ignore, ignore);
        }
        
        return g;
    }
}
