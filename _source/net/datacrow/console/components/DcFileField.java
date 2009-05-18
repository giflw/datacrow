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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.RandomAccessFile;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.filechooser.FileFilter;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.BrowserDialog;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

public class DcFileField extends JComponent implements IComponent, ActionListener {

    private static Logger logger = Logger.getLogger(DcFileField.class.getName());
    
    private JTextField text;
    private JButton button;

    private ActionListener al;
    private FileFilter filter;

    private String title = DcResources.getText("lblFileBrowser");
    private File file = null;

    public DcFileField(FileFilter filter) {
        this.filter = filter;
        text = ComponentFactory.getTextFieldDisabled();

        button = ComponentFactory.getIconButton(IconLibrary._icoOpen);
        
        this.setLayout(Layout.getGBL());

        add( text,   Layout.getGBC( 0, 0, 1, 1, 80.0, 80.0
                    ,GridBagConstraints.WEST, GridBagConstraints.BOTH,
                     new Insets(0, 0, 0, 0), 0, 0));
        add( button, Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.EAST, GridBagConstraints.NONE,
                     new Insets(0, 0, 0, 0), 0, 0));
    }

    public void setModus(boolean save, boolean dirsOnly) {
        button.removeActionListener(this);
        button.addActionListener(this);

        if (save) {
            this.title = DcResources.getText("lblCreateNewFile");
            button.setActionCommand("showFileSaveDialog");
        } else {
            if (dirsOnly) {
                this.title = DcResources.getText("lblSelectDirectory");
                button.setActionCommand("showDirectoryOpenDialog");
            } else {
                this.title = DcResources.getText("lblSelectFile");
                button.setActionCommand("showFileOpenDialog");
            }
        }
    }

    public void clear() {
        text = null;
        button = null;
        al = null;
        filter = null;
        title = null;
        file = null;
    }
    
    public void setFileFilter(FileFilter filter) {
        this.filter = filter;
    }

    public File getFile() {
        return file;
    }

    public String getFilename() {
    	return file == null ? "" : file.toString();
    }

    public void setFile(File file) {
        String filename = file == null ? "" : file.toString();

        this.file = new File(filename);
        text.setText(filename);
    }

    protected void showFileSaveDialog() {
        BrowserDialog dialog = new BrowserDialog(title, filter);
        dialog.setFont(ComponentFactory.getStandardFont());
        File fileNew = dialog.showCreateFileDialog(this, file);
        file = fileNew != null ? fileNew : file;
        text.setText(getFilename());

        if (fileNew != null) {
            try {
                RandomAccessFile random = new RandomAccessFile(file, "rw");
                random.close();
            } catch (Exception exp) {
                new MessageBox( DcResources.getText("msgFileCouldNotbeCreated"), MessageBox._ERROR);
                logger.error(DcResources.getText("msgFileCouldNotbeCreated"), exp);
            }
        }
        dialog.dispose();
        dialog = null;
    }

    protected void showFileOpenDialog() {
        BrowserDialog dialog = new BrowserDialog(title, filter);
        dialog.setFont(ComponentFactory.getStandardFont());

        file = dialog.showOpenFileDialog(this, null);
        text.setText(getFilename());

        if (al != null) {
        	al.actionPerformed(new ActionEvent(this.getParent(), 1, ""));
        }

        dialog.dispose();
        dialog = null;
    }

    protected void showDirectoryOpenDialog() {
        BrowserDialog dialog = new BrowserDialog(title, filter);
        dialog.setFont(ComponentFactory.getStandardFont());
        
        file = dialog.showSelectDirectoryDialog(this, null);
        text.setText(getFilename());

        dialog.dispose();
        dialog = null;
    }

    public void addListener(ActionListener al) {
        this.al = al;
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
		return file == null ? "" : file.toString();
	}
    
    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
    
    public void setEditable(boolean b) {
        setEnabled(b);
    }
    
    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        button.setEnabled(b);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("showDirectoryOpenDialog"))
            showDirectoryOpenDialog();
        else if (e.getActionCommand().equals("showFileOpenDialog"))
            showFileOpenDialog();
        else if (e.getActionCommand().equals("showFileSaveDialog"))
            showFileSaveDialog();
    }    
}
