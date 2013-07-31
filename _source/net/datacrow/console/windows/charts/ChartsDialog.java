package net.datacrow.console.windows.charts;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.DcDialog;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class ChartsDialog extends DcDialog implements ActionListener {

    public ChartsDialog() {
        super();
        
        setIconImage(IconLibrary._icoChart.getImage());
        setTitle(DcResources.getText("lblCharts"));
        
        build();
        
        pack();
        setSize(DcSettings.getDimension(DcRepository.Settings.stChartsDialogSize));
        setResizable(true);
        setCenteredLocation();
    }
    
    private void build() {
        
        ChartPanel chartPanel = new ChartPanel(DcModules.getCurrent().getIndex());
        
        getContentPane().setLayout(Layout.getGBL());
        getContentPane().add(chartPanel, Layout.getGBC(0, 0, 1, 1, 100.0, 100.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        
        
        JPanel panelAction = new JPanel();
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        panelAction.add(buttonClose);
        
        getContentPane().add(panelAction, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stChartsDialogSize, getSize());
        
        super.close();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close")) {
            close();
        }
    }
}
