package net.datacrow.console.views;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;

import net.datacrow.console.menu.ViewPopupMenu;

public class ViewMouseListener implements MouseListener {
    
    public void mouseReleased(java.awt.event.MouseEvent e) {
        IViewComponent vc = (IViewComponent) e.getSource();
        View view = vc.getView();
        
        if (SwingUtilities.isRightMouseButton(e)) {
            // Only change the selection for single item selections
            // or when nothing has been selected as yet.
            if (vc.getSelectedIndices() == null ||
                vc.getSelectedIndices().length == 1) {
                int index = vc.locationToIndex(e.getPoint());
                vc.setSelected(index);
            }

            if (vc.getSelectedIndex() > -1) {
                ViewPopupMenu menu = new ViewPopupMenu(vc.getSelectedItem(), view.getType()); 
                Component component = (Component) vc;
                menu.setInvoker(component);
                menu.show(component, e.getX(), e.getY());
            }
        }

        if (e.getClickCount() == 2 && vc.getSelectedIndex() > -1) {
            vc.getView().open();
            e.consume();
        }
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
}
