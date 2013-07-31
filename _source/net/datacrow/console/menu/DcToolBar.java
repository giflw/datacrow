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

package net.datacrow.console.menu;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcPopupMenu;
import net.datacrow.console.components.DcToolBarButton;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.plugin.Plugin;
import net.datacrow.core.plugin.PluginHelper;
import net.datacrow.core.plugin.Plugins;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcSwingUtilities;

public class DcToolBar extends JToolBar implements MouseListener {

    public DcToolBar(DcModule module) {
        
        PluginHelper.add(this, "NewItemWizard");
        PluginHelper.add(this, "NewItems");
        
        addSeparator();
        
        PluginHelper.add(this, "OpenItem", module.getIndex(), Plugin._VIEWTYPE_SEARCH);
        
        addSeparator();

        PluginHelper.add(this, "CreateNew", module.getIndex(), Plugin._VIEWTYPE_SEARCH);
        PluginHelper.add(this, "Delete", module.getIndex(), Plugin._VIEWTYPE_SEARCH);
        
        addSeparator();
        
        PluginHelper.add(this, "SaveSelected", module.getIndex(), Plugin._VIEWTYPE_SEARCH);
        PluginHelper.add(this, "SaveAll", module.getIndex(), Plugin._VIEWTYPE_SEARCH);
        
        addSeparator();

        PluginHelper.add(this, "Filter");
        PluginHelper.add(this, "ApplyFilter");
        
        if (module.deliversOnlineService())
            PluginHelper.add(this, "OnlineSearch");

        PluginHelper.add(this, "Charts");
        
        addSeparator();

        Collection<Plugin> plugins = Plugins.getInstance().getUserPlugins(null, -1, module.getIndex(), Plugin._VIEWTYPE_SEARCH);
        for (Plugin plugin : plugins) {
            if (plugin.isShowOnToolbar())
                add(ComponentFactory.getToolBarButton(plugin));
        }
        
        //boolean showLabels = DcSettings.getBoolean(DcRepository.Settings.stShowMenuBarLabels);
        setLabelsVisible(false);
        
        addMouseListener(this);
        
        setFloatable(false);
	}
    
    @Override
    public Component add(Component c) {
        c.addMouseListener(this);
        return super.add(c);
    }

    public void toggleLabels() {
        setLabelsVisible(!DcSettings.getBoolean(DcRepository.Settings.stShowMenuBarLabels));
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        
        Component c;
        int i = 0;
        while ((c = getComponentAtIndex(i++)) != null) {
            if (c instanceof DcToolBarButton) {
                DcToolBarButton button = (DcToolBarButton) c;
                button.setFont(ComponentFactory.getSystemFont());
            }
        }
    }    
    
	public void setLabelsVisible(boolean b) {
		Component c;
		int i = 0;
		while ((c = getComponentAtIndex(i++)) != null) {
            if (c instanceof DcToolBarButton) {
                DcToolBarButton button = (DcToolBarButton) c;
                
                if (b) button.showText();
                else button.hideText();
            }
		}
        DcSettings.set(DcRepository.Settings.stShowMenuBarLabels, b);
	}
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(DcSwingUtilities.setRenderingHint(g));
    }    
    
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {
//        if (SwingUtilities.isRightMouseButton(e))
//            new PopupMenu().show(this, e.getX(), e.getY());
    }
    
    public class PopupMenu extends DcPopupMenu implements ActionListener {

        public PopupMenu() {
            JMenuItem menuToggleLabels = new JMenuItem(DcResources.getText("lblToggleMenuLabels"));

            menuToggleLabels.addActionListener(this);
            menuToggleLabels.setActionCommand("toggleLabels");
            this.add(menuToggleLabels);
        }
        
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("toggleLabels"))
                setLabelsVisible(!DcSettings.getBoolean(DcRepository.Settings.stShowMenuBarLabels));
        }        
    }    
}