package net.datacrow.console.components;

import javax.swing.JToolTip;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.plugin.Plugin;

public class DcToolBarButton extends DcButton {

    private final String text;
    
    public DcToolBarButton(Plugin plugin) {
        super(plugin.getIcon());
        
        text = plugin.getLabelShort();
        
        setFont(ComponentFactory.getSystemFont());
        setText(plugin.getLabelShort());
        setVerticalTextPosition(BOTTOM);
        setHorizontalTextPosition(CENTER);
        setToolTipText(plugin.getHelpText() == null ? getText() : plugin.getHelpText());
        
        addActionListener(plugin);
    }
    
    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
    
    public void hideText() {
        setText("");
    }
    
    public void showText() {
        setText(text);
    }
}
