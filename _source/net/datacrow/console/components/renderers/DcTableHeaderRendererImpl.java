package net.datacrow.console.components.renderers;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.table.DefaultTableCellRenderer;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcMultiLineToolTip;
import net.datacrow.core.DcRepository;
import net.datacrow.settings.DcSettings;

public abstract class DcTableHeaderRendererImpl extends DefaultTableCellRenderer {

    private final JButton button = ComponentFactory.getTableHeader("");
    
    protected DcTableHeaderRendererImpl() {}
    
    public void applySettings() {
        button.setBorder(BorderFactory.createLineBorder(DcSettings.getColor(DcRepository.Settings.stTableHeaderColor)));
        button.setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontBold));
        button.setBackground(DcSettings.getColor(DcRepository.Settings.stTableHeaderColor));
    }
    
    public JButton getButton() {
        return button;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        button.setText(String.valueOf(value));
        button.setToolTipText(String.valueOf(value));
        return button;
    }
    
    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
}
