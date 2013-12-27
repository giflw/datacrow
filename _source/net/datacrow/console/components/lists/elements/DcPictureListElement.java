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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcPictureField;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.Picture;
import net.datacrow.util.DcImageIcon;

public class DcPictureListElement extends DcObjectListElement {

    private static final FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
    private DcPictureField fldPicture = ComponentFactory.getPictureField(true, false);
    
    private static final Dimension fldPictureSize = new Dimension(400, 300);
    
    public DcPictureListElement() {
        super(DcModules._PICTURE);
    }
    
    @Override
    public void build() {
        fldPicture.setPreferredSize(fldPictureSize);
        fldPicture.setMinimumSize(fldPictureSize);
        fldPicture.setMaximumSize(fldPictureSize);
        
        Picture p = (Picture) getDcObject();
        p.loadImage(false);
        
        if (!p.hasImage()) return;
        
        DcImageIcon image = (DcImageIcon) p.getValue(Picture._D_IMAGE);
        if (image != null) fldPicture.setValue(image);
        add(fldPicture);
    }

    @Override
    public Collection<Picture> getPictures() {
        Collection<Picture> pics = new ArrayList<Picture>();
        pics.add((Picture) getDcObject());
        return pics;
    }

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		fldPicture = null;
	}
}