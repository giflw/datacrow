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

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolTip;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.BrowserDialog;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.FileLauncher;
import net.datacrow.util.Utilities;

public class DcFileLauncherField extends JComponent implements IComponent, ActionListener {

    private DcShortTextField text;
    private JButton buttonBrowse;
    private File file;

    public DcFileLauncherField() {
        text = ComponentFactory.getTextFieldDisabled();
        
        setBounds(0,0,0,0);

        buttonBrowse = ComponentFactory.getIconButton(IconLibrary._icoOpen);
        buttonBrowse.addActionListener(this);
        buttonBrowse.setActionCommand("showFileOpenDialog");
        
        JButton buttonLaunch = ComponentFactory.getIconButton(IconLibrary._icoOpenApplication);
        buttonLaunch.addActionListener(this);
        buttonLaunch.setActionCommand("launchFile");
        
        setLayout(Layout.getGBL());
        add( text,         Layout.getGBC( 0, 0, 1, 1, 80.0, 80.0
                ,GridBagConstraints.WEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));
        add( buttonBrowse, Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE,
                 new Insets(0, 0, 0, 0), 0, 0));
        add( buttonLaunch, Layout.getGBC( 2, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE,
                 new Insets(0, 0, 0, 0), 0, 0));    
    }
    
    public void clear() {
        text = null;
        file = null;
        buttonBrowse = null;
    }

    public File getFile() {
        return file;
    }

    public String getFileAsString() {
        return file == null ? "" : Utilities.getOriginalFilename(file.toString());
    }

    public void setFile(File file) {
        String filename = file == null ? "" : file.toString();
        this.file = new File(filename);
        text.setText(Utilities.getMappedFilename(filename));
    }
    
    public void setValue(Object value) {
        if (value != null && value.toString().length() > 0) {
            File file = new File(value.toString());
            setFile(file);
        } else {
            setFile(null);
        }
    }

    public Object getValue() {
        return getFileAsString();
    }    
    
    public void setEditable(boolean b) {
        buttonBrowse.setEnabled(b);
    }

    private void showFileOpenDialog() {
        BrowserDialog dialog = new BrowserDialog(DcResources.getText("lblFileBrowser"), null);
        File fileNew = dialog.showOpenFileDialog(this, null);
        file = fileNew != null ? fileNew : file;
        text.setText(getFileAsString());

        dialog.dispose();
        dialog = null;
    }

    public void actionPerformed(ActionEvent e) {
        if (    e.getActionCommand().equals("launchFile") && 
                text.getText() != null && text.getText().length() > 0) {
            
            String filename = Utilities.getMappedFilename(text.getText());
            new FileLauncher(filename).launchFile();
        } else if (e.getActionCommand().equals("showFileOpenDialog")) {

            showFileOpenDialog();
        }
    }
    
    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(DcSwingUtilities.setRenderingHint(g));
    }    
}
