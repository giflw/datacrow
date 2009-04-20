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

package net.datacrow.console.windows.fileimport;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuBar;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcMenu;
import net.datacrow.console.components.DcMenuItem;
import net.datacrow.console.components.fstree.FileSystemTreePanel;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.FileNameFilter;

public class FileImportFileSelectPanel extends FileSystemTreePanel implements ActionListener {
    
    private int module;
    
    public FileImportFileSelectPanel(FileNameFilter filter, int module) {
        super(filter);
        this.module = module;
    }
    
    @Override
    protected JMenuBar getMenu() {
        JMenuBar menu = ComponentFactory.getMenuBar();
        
        menu.setPreferredSize(new Dimension(100, 22));
        menu.setMaximumSize(new Dimension(100, 22));
        menu.setMinimumSize(new Dimension(50, 22));
        
        DcMenu menuFilter = ComponentFactory.getMenu(DcResources.getText("lblFilter"));
        DcMenuItem menuFileTypes = ComponentFactory.getMenuItem(DcResources.getText("lblFileTypes"));
        menuFilter.add(menuFileTypes);
        
        menu.add(menuFilter);
        
        menuFileTypes.addActionListener(this);
        menuFileTypes.setActionCommand("filterFileTypes");
        
        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        super.actionPerformed(ae);
        if (ae.getActionCommand().equals("filterFileTypes")) {
            SelectFileTypesDialog dlg = new SelectFileTypesDialog(module);
            dlg.setVisible(true);
            
            if (dlg.isChanged()) {
                String[] extensions = DcModules.get(module).getSettings().getStringArray(DcRepository.ModuleSettings.stFileImportFileTypes);
                setFilter(extensions.length > 0 ? new FileNameFilter(extensions, true) : null);
            }
        }
    }
}
