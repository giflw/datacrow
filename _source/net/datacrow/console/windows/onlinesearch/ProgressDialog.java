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

package net.datacrow.console.windows.onlinesearch;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JProgressBar;

import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.util.DcSwingUtilities;

public class ProgressDialog extends DcDialog {
    
    private JProgressBar bar = new JProgressBar();
    
    public ProgressDialog(String title) {
        super(DcSwingUtilities.getRootFrame());
        
        bar.setMinimum(0);
        bar.setMaximum(100);
        
        getContentPane().setLayout(Layout.getGBL());
        getContentPane().add(bar,  Layout.getGBC( 0, 0, 1, 1, 50.0, 50.0
                ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        
        pack();
        
        setSize(new Dimension(500, 100));
        setCenteredLocation();
        setResizable(false);

        setTitle(title);
        setModal(false);
        setVisible(true);
    }
    
    public void update() {
        int value = bar.getValue();
        value = value < bar.getMaximum() ? value + 1 : 0; 
        bar.setValue(value);
    }
    
    @Override
    public void close() {
        bar = null;
        super.close();
    }
}
