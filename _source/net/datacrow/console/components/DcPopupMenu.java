package net.datacrow.console.components;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;

import net.datacrow.util.DcSwingUtilities;

public class DcPopupMenu extends JPopupMenu {
    
    @Override
    public JMenuItem add(JMenuItem menuItem) {
        if (menuItem != null)
            super.add(menuItem);
        return menuItem;
    }
    
    @Override
    public void addSeparator() {
        if (getComponentCount() > 0) {
            Component[] c = getComponents();
            if (c[c.length - 1] instanceof JMenuItem) 
                super.addSeparator();
        }
    }

    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(DcSwingUtilities.setRenderingHint(g));
    }
}
