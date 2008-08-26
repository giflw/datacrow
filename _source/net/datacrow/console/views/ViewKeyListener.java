package net.datacrow.console.views;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ViewKeyListener implements KeyListener {

    private View view;
    
    private int rowFrom = -1;
    private int rowTo = -1;

    public ViewKeyListener(View view) {
        this.view = view;
    }
    
    public void keyPressed(KeyEvent e) {
        IViewComponent vc = view.getViewComponent();
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN)
           rowFrom = vc.getSelectedIndex();
    }

    public void keyReleased(KeyEvent e) {
        if ((view.allowsVerticalTraversel() && (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN)) ||
            (view.allowsHorizontalTraversel() && ( e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT))) {
            
            IViewComponent vc = view.getViewComponent();
            rowTo = vc.getSelectedIndex();
            if (rowFrom != rowTo)
                view.setSelected(vc.getSelectedIndex());
        }
    }

    public void keyTyped(KeyEvent arg0) {}

}