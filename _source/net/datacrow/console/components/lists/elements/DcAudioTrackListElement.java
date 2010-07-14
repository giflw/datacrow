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
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.helpers.AudioTrack;

public class DcAudioTrackListElement extends DcObjectListElement {

    private JPanel panelInfo;
    
    public DcAudioTrackListElement(int module) {
        super(module);
    }
    
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        if (panelInfo != null)
            panelInfo.setBackground(color);
    }     
    
    @Override
    public Collection<Picture> getPictures() {
        return new ArrayList<Picture>();
    }

    @Override
    public void build() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        panelInfo = getPanel();
        JLabel labelNr = getLabel(AudioTrack._F_TRACKNUMBER, false, 30);
        JLabel labelTitle = getLabel(AudioTrack._A_TITLE, false, 300);
        JLabel labelLength = getLabel(AudioTrack._H_PLAYLENGTH, false, 100);
        
        panelInfo.add(labelNr);
        panelInfo.add(labelTitle);
        panelInfo.add(labelLength);
        
        panelInfo.setPreferredSize(new Dimension(50000, fieldHeight));
        add(panelInfo);
    } 
}