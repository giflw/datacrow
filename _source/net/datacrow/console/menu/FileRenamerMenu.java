package net.datacrow.console.menu;

import javax.swing.JMenuItem;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcMenu;
import net.datacrow.console.windows.filerenamer.FileRenamerPreviewDialog;
import net.datacrow.core.resources.DcResources;

public class FileRenamerMenu extends DcMenu {
    
    public FileRenamerMenu(FileRenamerPreviewDialog listener) {
        super(DcResources.getText("lblEdit"));
        
        JMenuItem menuRemove = ComponentFactory.getMenuItem(DcResources.getText("lblRemove"));
        menuRemove.setActionCommand("remove");
        menuRemove.addActionListener(listener);
        add(menuRemove);
    }
}
