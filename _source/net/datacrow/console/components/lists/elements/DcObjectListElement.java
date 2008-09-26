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

package net.datacrow.console.components.lists.elements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcLabel;
import net.datacrow.console.components.DcPictureField;
import net.datacrow.console.components.DcTextPane;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public abstract class DcObjectListElement extends DcListElement {

    private static Logger logger = Logger.getLogger(DcObjectListElement.class.getName());
    
    private static final Dimension dimTxt = new Dimension(145, 45);
    private static final Dimension dimPicLbl = new Dimension(146, 140);

    protected static final int fieldHeight = 21;
    protected DcObject dco;
    
    protected DcObjectListElement() {}
    
    public DcObjectListElement(DcObject dco) {
        this.dco = dco;
        build();
    }

    public DcObject getDcObject() {
        return dco;
    }
    
    public abstract Collection<Picture> getPictures();
    
    public void update(DcObject o, boolean overwrite, boolean allowDeletes, boolean mark) {
        int[] indices = dco.getFieldIndices();
        
        for (int i = 0; i < indices.length; i++) {
            try {
                if (o.isChanged(indices[i])) {
                    Object newValue = o.getValue(indices[i]);
                    Object existingValue = dco.getValue(indices[i]);

                    boolean isNewEmpty = Utilities.isEmpty(newValue);
                    boolean isOldEmpty = Utilities.isEmpty(existingValue);
                    
                    if (newValue instanceof Picture)
                        newValue = ((Picture) newValue).isDeleted() ? null : newValue;
                    
                    if ((overwrite && !isNewEmpty) || allowDeletes) {
                        dco.setValue(indices[i], newValue);
                    } else if (isOldEmpty) {
                        dco.setValue(indices[i], newValue);
                    }
                }
            } catch (Exception e) {
                logger.error("Could not update element [" + dco + "]", e);
            }
        }        

        dco.removeChildren();
        dco.loadChildren();
        
        if (!mark)
            dco.markAsUnchanged();
        
        update();
        revalidate();
    }    
    
    private String getDescription() {
        int[] fields = (int[]) dco.getModule().getSetting(DcRepository.ModuleSettings.stCardViewItemDescription);
        if (fields != null && fields.length > 0) {
            String name = "";
            for (int field :  fields) {
                String disp = dco.getDisplayString(field);
                if (disp.length() > 0)
                    name += (name.length() > 0 ? ", " + disp : disp);
            }
            return name;
        } 
        return dco.getName();
    }

    @Override
    protected void build() {
        addPicture(getPictures());
        
        DcTextPane title = ComponentFactory.getTextPane();
        JScrollPane scroller = new JScrollPane(title);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        title.setText(getDescription());
        title.setPreferredSize(dimTxt);
        title.setMinimumSize(dimTxt);
        title.setMaximumSize(dimTxt);
        add(title);
        
        super.setBackground(DcSettings.getColor(DcRepository.Settings.stCardViewBackgroundColor));
    }
    
    private void addPicture(Collection<Picture> pictures) {
        DcPictureField lbl = null;
        for (Picture p : pictures) {
            if (p == null)
                continue;
            
            if (lbl != null)
                break;
                
            DcImageIcon scaledImage = p.getScaledPicture();
            DcImageIcon image = (DcImageIcon) p.getValue(Picture._D_IMAGE);
            DcPictureField pictureFld = null;

            if (scaledImage != null) { 
                pictureFld = new DcPictureField(false, false, false, "");
                pictureFld.setValue(scaledImage);
            } else if (image != null) {
                pictureFld = new DcPictureField(true, false, false, "");
                pictureFld.setValue(new DcImageIcon(image.getImage()));
            }                
            
            lbl = pictureFld;
        }
        
        if (lbl == null)
            lbl = new DcPictureField();
        
        lbl.setPreferredSize(dimPicLbl);
        lbl.setMinimumSize(dimPicLbl);
        lbl.setMaximumSize(dimPicLbl);
        add(lbl);
    }
    
    public JPanel getPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        return panel;
    }
    
    protected DcLabel getLabel(int field, boolean label, int width) {
        DcLabel lbl = new DcLabel();
        if (label) {
            Font font = DcSettings.getFont(DcRepository.Settings.stSystemFontBold);
            lbl.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            lbl.setText(dco.getLabel(field));
        } else {
            lbl.setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontNormal));
            lbl.setText(dco.getDisplayString(field));
        }
        
        lbl.setPreferredSize(new Dimension(width, fieldHeight));
        
        return lbl;
    }       
    
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        for (int i = 0; i < getComponents().length; i++) {
            getComponents()[i].setBackground(color);
        }
    }
    
    @Override
    public void destroy() {
    	super.destroy();
    	dco = null;
    }
    
    @Override
    public void clear() {
    	if (dco != null)
    		dco.freeResources();

        super.clear();
    }
}
