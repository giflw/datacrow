package net.datacrow.console.windows;

import java.util.concurrent.atomic.AtomicBoolean;

public interface IDialog {

    public void setModal(AtomicBoolean active);
    
    public void setVisible(boolean b);
    
    public void setModal(boolean b);
}
