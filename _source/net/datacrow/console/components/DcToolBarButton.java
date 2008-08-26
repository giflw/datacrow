package net.datacrow.console.components;

import javax.swing.JToolTip;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.plugin.Plugin;

public class DcToolBarButton extends DcButton {

    public DcToolBarButton(Plugin plugin) {
        super(plugin.getIcon());
        setFont(ComponentFactory.getSystemFont());
        setText(plugin.getLabelShort());
        setVerticalTextPosition(BOTTOM);
        setHorizontalTextPosition(CENTER);
        setToolTipText(plugin.getLabelShort());
        
        addActionListener(plugin);
    }
    
    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
}
