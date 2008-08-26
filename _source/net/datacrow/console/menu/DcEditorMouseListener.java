package net.datacrow.console.menu;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

public class DcEditorMouseListener implements MouseListener {
    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    
    public void mouseReleased(MouseEvent e) {
        JTextComponent c = (JTextComponent) e.getSource();
        if (SwingUtilities.isRightMouseButton(e) && c.isEditable() && c.isEnabled()) {
            DcEditorPopupMenu popupmenu = new DcEditorPopupMenu(c);
            popupmenu.validate();
            popupmenu.show(c, e.getX(), e.getY());
        }
    }        
}
