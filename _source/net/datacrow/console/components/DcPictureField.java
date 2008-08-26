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

import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JToolTip;

import net.datacrow.console.Layout;
import net.datacrow.console.menu.DcPictureFieldMenu;
import net.datacrow.console.windows.BrowserDialog;
import net.datacrow.console.windows.OpenFromUrlDialog;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.windows.messageboxes.QuestionBox;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.Base64;
import net.datacrow.util.DcFileFilter;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.PictureFileFilter;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class DcPictureField extends JComponent implements IComponent, ActionListener, WindowListener, MouseListener {

    private static Logger logger = Logger.getLogger(DcPictureField.class.getName());
    
    private Image img = null;
    private DcImageIcon picture;
    
    private boolean changed = false;
    
    private boolean scaled = true;
    private boolean thumbnail = false;
    private Dimension size = null;
    private Insets insets = new Insets(0, 0, 0, 0);
    
    private DcPictureFieldMenu menu;
    private ImageJ imageJ;
    private String name;
    
    public DcPictureField() {
    	this(true, false, false, "");
    }
    
    public DcPictureField(boolean scaled, boolean allowActions, boolean thumbnail, String name) {
        this.setLayout(Layout.getGBL());
        this.name = name;
        if (allowActions) {
        	this.menu = new DcPictureFieldMenu(this);
            this.add(menu, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                     GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5, 5), 0, 0));
        }
        
        if (allowActions) addMouseListener(this);
        
        this.add(new PicturePane(), Layout.getGBC(0, 1, 1, 1, 80.0, 80.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        this.scaled = scaled;
        this.thumbnail = thumbnail;
    }
    
    public void setValue(Object o) {
        if (o == picture)
            return;
        
        if (o instanceof Picture)
            o = ((Picture) o).getValue(Picture._D_IMAGE);

        clearImage();
        
        try {
            if (o == null) {
                picture = null;
            } else {
                if (o instanceof URL) {
                    URL url = (URL) o;
                    picture = new DcImageIcon(url.getFile());
                } else if (o instanceof String) {
                    String value = (String) o;
                    if (value.endsWith("jpg")) {
                        picture = new DcImageIcon(value);
                    } else {
                        String base64 = (String) o;
                        if (base64.length() > 0)
                            picture = new DcImageIcon(Base64.decode(base64.toCharArray()));
                    }
                } else if (o instanceof DcImageIcon) {
                	
                	if (((DcImageIcon) o).getCurrentBytes() != null)
                		picture = new DcImageIcon(((DcImageIcon) o).getBytes());
                    else
                		picture = (DcImageIcon) o;
                		
                }
            }
        } catch (Exception e) {
            logger.error(DcResources.getText("msgCouldNotLoadPicture"), e);
        }
        
        initialize();
        revalidate();
        repaint();
    }

    public boolean isChanged() {
        return changed;
    }
    
    public void clear() {
        clearImage();
        
        picture = null;
        size = null;
        
        insets = null;
        imageJ = null;
        name = null;
    } 
    
    private void clearImage() {
        if (picture != null)
            picture.getImage().flush();

        if (img != null) {
            img.flush();
            initialize();
        }
    }
    
    public boolean isEmpty() {
    	return img == null;
    }

    public Object getValue() {
        return picture;
    }

    private Image getImage() {
    	return img;
    }

    private boolean scalingAllowed(int width, int height) {
        return scaled && (height >= 50 && width >= 50);
    }

    @Override
    public Dimension getMinimumSize() {
        Image image = getImage();
        insets = getInsets(insets);
        if (thumbnail && image != null) {
            return new Dimension(insets.left + Math.max(32, ((imageWidth / 5) / 10)) + insets.right,
                                 insets.top  + Math.max(32, ((imageHeight / 5) / 10)) + insets.bottom);
        } 

        return super.getMinimumSize();
    }

    @Override
    public Dimension getPreferredSize() {
        insets = getInsets(insets);
        Image image = getImage();
        if (thumbnail && image != null) {
            return new Dimension(insets.left + (imageWidth / 5) + insets.right,
                                 insets.top  + (imageHeight / 5) + insets.bottom);
        }
        return super.getPreferredSize();
    }        
    
    private class PicturePane extends JComponent {
        
        @SuppressWarnings("null")
        @Override
        protected void paintComponent(Graphics g) {
            insets = getInsets(insets);
            size = getSize(size);
    
            if (picture == null) {
                super.paintComponent(g);
                return;
            }
                
            try {
                if (scalingAllowed(imageWidth, imageHeight)) {
                    
                    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                  
                    int newWidth = 0;
                    int newHeight = 0;
                    if (thumbnail) {
                        newWidth =  Math.min(size.width, imageWidth / 5);
                        newHeight = Math.min(size.height, imageHeight / 5);
                    
                    } else if (imageWidth > size.width || imageHeight > size.height) {
                        
                        newWidth =  Math.min(size.width, imageWidth);
                        newHeight = Math.min(size.height, imageHeight);
            
                        double scaledRatio = (double) newWidth / (double) newHeight;
                        double imageRatio = (double) imageWidth / (double) imageHeight;
                        if (scaledRatio < imageRatio) 
                            newHeight = (int) (newWidth / imageRatio);
                        else
                            newWidth = (int) (newHeight * imageRatio);
                        
                        img = picture.getImage();

                        // reload image if necessary
                        if (!prepareImage(img, this))
                            img = new DcImageIcon(picture.getImage()).getImage();
                        
                        g.drawImage(img, 
                                getInset(size.width, newWidth), 
                                getInset(size.height, newHeight), 
                                newWidth, 
                                newHeight,
                                null);
                    } else {
                        picture.paintIcon(this, g, getInset(size.width, imageWidth), getInset(size.height, imageHeight));
                    }
    
                } else {
                    picture.paintIcon(this, g, getInset(size.width, imageWidth), getInset(size.height, imageHeight));
                } 
            } catch (Exception e) {
                logger.error(e, e);
            }
            
            g.dispose();
        }
    }
    
    private int getInset(int sizeComp, int sizePic) {
        int remainingSpace = sizeComp - sizePic;
        return remainingSpace >= 2 ? remainingSpace / 2 : 0;
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
    
    private void openImageFromURL() {
        OpenFromUrlDialog dialog = new OpenFromUrlDialog();
        dialog.setVisible(true);
        
        byte[] image = dialog.getImage();
        if (image != null) {
            picture = new DcImageIcon(image);
            initialize();
            changed = true;
            dialog.setImage(null);
        }
    }

    private void openImageFromFile() {
        try {
            BrowserDialog dialog = new BrowserDialog("Select a new Image", new PictureFileFilter());
            File file = dialog.showOpenFileDialog(this, null);
            if (file != null) {
                picture = new DcImageIcon(Utilities.readFile(file));
                initialize();
                changed = true;
                
                if (DcSettings.getBoolean(DcRepository.Settings.stDeleteImageFileAfterImport)) 
                    file.delete();
            }
        } catch (Exception e) {
            logger.error("An error occured while reading the image", e);
        }
    }
    
    private void saveToFile() {
        if (picture != null) {
            BrowserDialog dlg = new BrowserDialog(DcResources.getText("lblSelectFile"), 
                                                  new DcFileFilter("jpg"));
            
            File file = dlg.showCreateFileDialog(this, null);

            try {
                if (file != null) {
                    String filename = file.toString();
                    filename += filename.toLowerCase().endsWith("jpg") || filename.toLowerCase().endsWith("jpeg") ? "" : ".jpg";
                    Utilities.writeToFile(picture, filename);
                }
            } catch (Exception e) {
                new MessageBox(e.getMessage(), MessageBox._ERROR);
                logger.error("An error occurred while saving the image", e);
            }
        }
    }
    
    private void openEditDialog() {
        img = picture.getImage();
        imageJ = new ImageJ(IconLibrary._icoMain);
        imageJ.setVisible(true);
        ImagePlus imgPlus = new ImagePlus(name, img);
        imgPlus.setIgnoreFlush(true);
        ImageWindow imgWindow = new ImageWindow(imgPlus, false);
        WindowManager.addWindow(imgWindow);
        
        imgWindow.addWindowListener(this);
        imageJ.addWindowListener(this);
        imgWindow.setVisible(true);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g2d);
    }

    public void setEditable(boolean b) {
    	setEnabled(b);
    	if (!b) {
    		remove(menu);
    	}
    }
    
    public void actionPerformed(ActionEvent e) {
    	if (!isEnabled())
    		return;
    	
        String action = e.getActionCommand();
        if (action.equals("open_from_file")) {
            openImageFromFile();
        } else if (action.equals("open_from_url")) {
            openImageFromURL();
        } else if (action.equals("Save as")) {
            saveToFile();
        } else if (action.equals("edit")) {
            openEditDialog();
        } else if (action.equals("delete")) {
            setValue(null);
            changed = true;
        }
    }
    
    public void windowClosing(WindowEvent e) {
        ImagePlus imgPlus = WindowManager.getImage(name);
        WindowManager.closeAllWindows();
        
        if (imgPlus == null)
            return;
        
        try {
            Image image = imgPlus.getImage();
            if (image != null && imgPlus.changes) {
                QuestionBox qb = new QuestionBox(DcResources.getText("msgKeepChanges"));
                if (qb.isAffirmative()) {
                    picture = new DcImageIcon(Utilities.getBytes(image));
                    initialize();
                    changed = true;
                    revalidate();
                    repaint();
                }
            }
            
            imgPlus.setIgnoreFlush(false);
            imgPlus.flush();
        } catch (Exception exp) {
            logger.error("Error while releasing image resources", exp);
        }
        
        if (imageJ != null) {
            imageJ.dispose();
            imageJ = null;
        }
        
        menu = null;
    }
    
    private int imageWidth = -1;
    private int imageHeight = -1;
    
    private void initialize() {
        if (picture != null) {
            imageWidth = picture.getIconWidth();
            imageHeight = picture.getIconHeight();
        } else {
            imageWidth = -1;
            imageHeight = -1;
        }
    }

    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
    
    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}

    public void mouseClicked(MouseEvent e) {
    	if (!isEnabled())
    		return;
    	
        if (e.getClickCount() == 2) {
            if (img != null)
                openEditDialog();
            else 
                openImageFromFile();
        }
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}     
}