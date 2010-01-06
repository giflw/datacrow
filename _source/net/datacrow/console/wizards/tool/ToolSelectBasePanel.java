package net.datacrow.console.wizards.tool;

import javax.swing.JPanel;

import net.datacrow.console.wizards.IWizardPanel;
import net.datacrow.console.wizards.Wizard;

public abstract class ToolSelectBasePanel extends JPanel implements IWizardPanel {

    private Tool tool;
    private Wizard wizard;
    
    public ToolSelectBasePanel(Wizard wizard) {
        this.wizard = wizard;
    }
    
    public void setTool(Tool tool) {
        this.tool = tool;
    }
    
    public Tool getTool() {
        tool = tool == null ? new Tool() : tool;
        return tool;
    }
    
    public Wizard getWizard() {
        return wizard;
    }
    
    public void onActivation() {}

    public void onDeactivation() {}

    public void destroy() {
        tool = null;
        wizard = null;
    }
}
