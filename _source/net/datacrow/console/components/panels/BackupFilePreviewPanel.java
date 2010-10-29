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

package net.datacrow.console.components.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcLongTextField;

import org.apache.log4j.Logger;

public class BackupFilePreviewPanel extends JPanel implements PropertyChangeListener {
    
    private static Logger logger = Logger.getLogger(BackupFilePreviewPanel.class.getName());
    
    private DcLongTextField preview = ComponentFactory.getLongTextField();
    
    public BackupFilePreviewPanel() {
        build();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        
        // Make sure we are responding to the right event.
        if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            
            preview.setText("");
            
            File selection = (File)e.getNewValue();
            
            try {
                ZipFile zf = new ZipFile(selection);
                ZipEntry ze = zf.getEntry("version.txt");
                
                if (ze != null) {
                    InputStream is = zf.getInputStream(ze);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    
                    StringBuffer sb = new StringBuffer();
                    byte[] b = new byte[4096];
                    for (int n; (n = bis.read(b)) != -1;)
                        sb.append(new String(b, 0, n));

                    bis.close();
                    is.close();
                    
                    preview.setText(sb.toString());
                } 
            } catch (Exception exp) {
                logger.error(exp, exp);
            }
        }
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        JScrollPane scroller = new JScrollPane(preview);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroller, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 0, 5), 0, 0));
    }
}
