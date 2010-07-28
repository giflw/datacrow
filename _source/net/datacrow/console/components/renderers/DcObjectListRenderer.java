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

package net.datacrow.console.components.renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JList;

import net.datacrow.console.components.lists.elements.DcObjectListElement;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcObject;
import net.datacrow.settings.DcSettings;

public class DcObjectListRenderer extends DcListRenderer  {

    private boolean render = true;
    
    public DcObjectListRenderer() {
    }

    public DcObjectListRenderer(boolean evenOddColors) {
        super(evenOddColors);
    }

    @Override
    public Component getListCellRendererComponent(
            JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        DcObjectListElement c = (DcObjectListElement) value;

        if (render) {
            c.setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontNormal));
            c.load();
            
        	if (c.getDcObject().getModule().canBeLend()) {
        		Long daysTillOverdue = (Long) c.getDcObject().getValue(DcObject._SYS_LOANDAYSTILLOVERDUE);
        		if (daysTillOverdue != null && daysTillOverdue.longValue() < 0)
        			c.setForeground(Color.RED);
        	}
    
        	setElementColor(isSelected, c, index);
        }
        
    	return c;
    }
    
    public void stop() {
        render = false;
    }
    
    public void start() {
        render = true;
    }
    
    @Override
    public void repaint(final long tm, final int x, final int y, final int width, final int height) {}
    
    @Override
    public void repaint(final Rectangle r) {}
    
    @Override
    protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {}
    
    @Override
    public void firePropertyChange(final String propertyName, final byte oldValue, final byte newValue) {}
    
    @Override
    public void firePropertyChange(final String propertyName, final char oldValue, final char newValue) {}
    
    @Override
    public void firePropertyChange(final String propertyName, final short oldValue, final short newValue) {}
    
    @Override
    public void firePropertyChange(final String propertyName, final int oldValue, final int newValue) {}
    
    @Override
    public void firePropertyChange(final String propertyName, final long oldValue, final long newValue) {}
    
    @Override
    public void firePropertyChange(final String propertyName, final float oldValue, final float newValue) {}
    
    @Override
    public void firePropertyChange(final String propertyName, final double oldValue, final double newValue) {}
    
    @Override
    public void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {}    
}