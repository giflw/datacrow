package net.datacrow.console.menu;

import javax.swing.JMenuItem;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcPopupMenu;
import net.datacrow.console.windows.filerenamer.FileRenamerPreviewDialog;
import net.datacrow.core.resources.DcResources;

public class DcFileRenamerPreviewPopupMenu extends DcPopupMenu {
    
    public DcFileRenamerPreviewPopupMenu(FileRenamerPreviewDialog listener) {
        JMenuItem menuRemove = ComponentFactory.getMenuItem(DcResources.getText("lblRemove"));
        menuRemove.setActionCommand("remove");
        menuRemove.addActionListener(listener);
        add(menuRemove);
    }
}
