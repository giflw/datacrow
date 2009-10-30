package net.datacrow.console.windows;

import java.awt.Frame;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JDialog;

public class NativeDialog extends JDialog implements IDialog {

    private AtomicBoolean active;
    
    public NativeDialog() {
        super();
    }

    public NativeDialog(Frame owner) {
        super(owner);
    }
    
    public void setModal(AtomicBoolean active) {
        this.active = active;
    }
 
    @Override
    public void dispose() {
        if (active != null) {
            synchronized (active) {
                active.set(false);
                active.notifyAll();
            }
        }
        
        super.dispose();   
    }      
}
