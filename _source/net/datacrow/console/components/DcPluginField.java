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
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.core.plugin.Plugin;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

public class DcPluginField extends AbstractButton {
    
    private JCheckBox cb = ComponentFactory.getCheckBox("");
    private Plugin plugin;
    
    public DcPluginField(Plugin plugin) {
        this.plugin = plugin;
        build();
    }
    
    @Override
    public Object[] getSelectedObjects() {
        return cb.getSelectedObjects();
    }

    @Override
    public String getActionCommand() {
        return plugin.getKey();
    }

    @Override
    public ButtonModel getModel() {
        return cb.getModel();
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public boolean isSelected() {
        return cb.isSelected();
    }

    @Override
    public void setSelected(boolean b) {
        cb.setSelected(b);
    }

    private void build() {
        DcLongTextField fldHelp = ComponentFactory.getHelpTextField();
        
        fldHelp.setText(plugin.getHelpText());
        cb.setActionCommand(plugin.getKey());

        BufferedImage src = Utilities.toBufferedImage(plugin.getIcon());
        BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        DcImageIcon deselected = new DcImageIcon(Utilities.getBytes(new ImageIcon(op.filter(src, null))));
        
        cb.setIcon(deselected);
        cb.setSelectedIcon(plugin.getIcon());
        
        setLayout(Layout.getGBL());
        
        add(cb, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));
        add(fldHelp, Layout.getGBC(1, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
    }
}
