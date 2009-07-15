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

package net.datacrow.core.wf.requests;

import java.awt.Window;
import java.util.Collection;

import javax.swing.SwingUtilities;

import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.DcFrame;
import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.core.objects.DcObject;

/**
 * Request to close a form.
 * 
 * @author Robert Jan van der Waals
 */
public class CloseWindowRequest implements IRequest {

    private static final long serialVersionUID = 1228283059318769182L;
    private Window window;
    private boolean executeOnFail = false;

    public CloseWindowRequest(DcFrame frame) {
        this.window = frame;
    }

    public CloseWindowRequest(DcDialog dialog) {
        this.window = dialog;
    }    
    
    public void execute(Collection<DcObject> objects) {
        SwingUtilities.invokeLater(new WindowCloser(window));
        end();
    }

    public boolean getExecuteOnFail() {
        return executeOnFail;
    }

    public void setExecuteOnFail(boolean b) {
        executeOnFail = b;
    }
    
    public void end() {
        window = null;
    }
    
    private static class WindowCloser implements Runnable {
        
        private Window window;
        
        public WindowCloser(Window window) {
            this.window = window;
        }
        
        public void run() {
            if (window instanceof DcDialog)
                ((DcDialog) window).close();
            else if (window instanceof ItemForm)
                ((ItemForm) window).close(true);
            else if (window instanceof DcFrame)
                ((DcFrame) window).close();
            else if (window != null)
                window.dispose();
            
            window = null;
        }
    }
}
