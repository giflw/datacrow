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

import javax.swing.JFrame;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.settings.DcSettings;

public class DcSwingUtilities {
    
    private static JFrame rootFrame = null;

    public static void setRootFrame(JFrame f) {
    	rootFrame = f;
    }
    
    public static JFrame getRootFrame() {
        if (rootFrame != null) 
            return rootFrame;    
        return DataCrow.mainFrame;
    }
    
    public static Graphics setRenderingHint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        try {
            if (g2d != null && DcSettings.getBoolean(DcRepository.Settings.stFontAntiAliasing)) {
                g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            }
        // occurs when settings have not yet been loaded. Ignore all errors and continue!
        } catch (Exception ignore) {
        } catch (Error ignore) {}
        return g;
    }
}
