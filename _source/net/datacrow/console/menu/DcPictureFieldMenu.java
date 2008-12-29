package net.datacrow.console.menu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcPictureField;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcResources;

public class DcPictureFieldMenu extends JMenuBar {
    
    public DcPictureFieldMenu(DcPictureField pf) {
        build(pf);
    }
        
    private void build(DcPictureField pf) {
        JMenu menuFile = ComponentFactory.getMenu(DcResources.getText("lblFile"));
        JMenu menuEdit = ComponentFactory.getMenu(DcResources.getText("lblEdit"));

        JMenuItem miSaveAs = ComponentFactory.getMenuItem(DcResources.getText("lblSaveAs"));
        JMenuItem miOpenFromFile = ComponentFactory.getMenuItem(DcResources.getText("lblOpenFromFile"));
        JMenuItem miOpenFromURL = ComponentFactory.getMenuItem(DcResources.getText("lblOpenFromURL"));
        
        JMenuItem miRotateRight = ComponentFactory.getMenuItem(IconLibrary._icoRotateRight, DcResources.getText("lblRotateRight"));
        JMenuItem miRotateLeft = ComponentFactory.getMenuItem(IconLibrary._icoRotateLeft, DcResources.getText("lblRotateLeft"));
        
        JMenuItem miDelete = ComponentFactory.getMenuItem(DcResources.getText("lblDelete"));
        
        miRotateRight.setActionCommand("rotate_right");
        miRotateRight.addActionListener(pf);

        miRotateLeft.setActionCommand("rotate_left");
        miRotateLeft.addActionListener(pf);
        
        miOpenFromFile.setActionCommand("open_from_file");
        miOpenFromFile.addActionListener(pf);

        miOpenFromURL.setActionCommand("open_from_url");
        miOpenFromURL.addActionListener(pf);
        
        miDelete.setActionCommand("delete");
        miDelete.addActionListener(pf);
        
        miSaveAs.setActionCommand("Save as");
        miSaveAs.addActionListener(pf);
        
        menuFile.add(miOpenFromFile);
        menuFile.add(miOpenFromURL);
        menuFile.addSeparator();
        menuFile.add(miSaveAs);
        
        menuEdit.add(miRotateLeft);
        menuEdit.add(miRotateRight);
        menuEdit.addSeparator();
        menuEdit.add(miDelete);
        
        add(menuFile);
        add(menuEdit);
    }
}
