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
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.console.components.DcPictureField;
import net.datacrow.console.components.DcTextPane;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.components.lists.DcObjectListComponents;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcImageIcon;

import org.apache.log4j.Logger;

public class DcCardObjectListElement extends DcObjectListElement {

    private static Logger logger = Logger.getLogger(DcCardObjectListElement.class.getName());

    private final static Dimension size = new Dimension(150, 200);
    private static final Dimension dimTxt = new Dimension(145, 45);
    private static final Dimension dimPicLbl = new Dimension(146, 140);

    private DcTextPane fldTitle;
    private DcPictureField fldPicture;
    
    private boolean build = false;

    public DcCardObjectListElement(DcObject dco) {
        super(dco);
        
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
    }

    @Override
    public void update(DcObject dco) {
        clear();
        dco = DataManager.getObject(dco.getModule().getIndex(), dco.getID());
    }    
    
    private String getDescription() {
        int[] fields = (int[]) dco.getModule().getSetting(DcRepository.ModuleSettings.stCardViewItemDescription);
        if (fields != null && fields.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int field :  fields) {
                String disp = dco.getDisplayString(field);
                if (disp.length() > 0) {
                    if (sb.length() > 0)
                        sb.append("/");
                    sb.append(disp);
                }
            }
            return sb.toString();
        } 
        return dco.getName();
    }

    public boolean isBuild() {
        return build;
    }
    
    @Override
    public Collection<Picture> getPictures() {
    	Collection<Picture> pictures = new ArrayList<Picture>();
    	
    	int[] fields = dco.getModule().getSettings().getIntArray(DcRepository.ModuleSettings.stCardViewPictureOrder);
    	
    	if (fields == null || fields.length == 0) {
            for (DcField field : dco.getFields()) {
                if (field.getValueType() == DcRepository.ValueTypes._PICTURE)
                    fields = new int[] {field.getIndex()};
            }
    	}
    	
    	dco.getModule().getSettings().set(DcRepository.ModuleSettings.stCardViewPictureOrder, fields);
    	
    	for (int field : fields)
    		pictures.add((Picture) dco.getValue(field));

		return pictures;
    }
    
    @Override
    public void setBackground(Color color) {
        if (fldTitle != null)
            fldTitle.setBackground(color);
    }    
    
    
    private void addPicture(Collection<Picture> pictures) {
        for (Picture p : pictures) {
            if (p == null) continue;
                
            DcImageIcon scaledImage = p.getScaledPicture();
            DcImageIcon image = (DcImageIcon) p.getValue(Picture._D_IMAGE);
            
            if (scaledImage != null) { 
                fldPicture.setValue(scaledImage);
                fldPicture.setScaled(false);
                break;
            } else if (image != null) {
                fldPicture.setValue(new DcImageIcon(image.getImage()));
                fldPicture.setScaled(true);
                break;
            }                
        }

        fldPicture.setPreferredSize(dimPicLbl);
        fldPicture.setMinimumSize(dimPicLbl);
        fldPicture.setMaximumSize(dimPicLbl);
        add(fldPicture);
    }
    
    
    @Override
    public void build() {

      build = true;
      
      this.fldPicture = DcObjectListComponents.getPictureField();
      this.fldTitle = DcObjectListComponents.getTextPane();
      
    addPicture(getPictures());
      
      fldTitle.setText(getDescription());
      fldTitle.setPreferredSize(dimTxt);
      fldTitle.setMinimumSize(dimTxt);
      fldTitle.setMaximumSize(dimTxt);
      
      add(fldTitle);
      
      super.setBackground(DcSettings.getColor(DcRepository.Settings.stCardViewBackgroundColor));
      
      try {
          DcObjectList list = (DcObjectList) getParent().getParent();
          
          if (list.getView() != null) {
              list.getView().revalidate();
              list.getView().repaint(1000);
          }
      } catch (Exception e) {
          logger.error("Error while updating the view component (revalidate)", e);
      }
    }
    
    @Override
    public void paint(Graphics g) {
        if (!build)
            build();
        
        super.paint(g);
    }
    
    @Override
    public void clear() {
        super.clear();
        
        DcObjectListComponents.release(fldPicture);
        DcObjectListComponents.release(fldTitle);
        
        fldPicture = null;
        fldTitle = null;
        
        build = false;
    }
}