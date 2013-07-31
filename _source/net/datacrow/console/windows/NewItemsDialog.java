package net.datacrow.console.windows;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.views.MasterView;
import net.datacrow.console.views.View;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class NewItemsDialog extends DcFrame implements ActionListener {

    private DcModule module;
    
    public NewItemsDialog(DcModule module) {
        super(DcResources.getText("lblAddMultiple", module.getObjectNamePlural()), IconLibrary._icoItemsNew);
        
        this.module = module;
        
        build();
        
        pack();
        setSize(DcSettings.getDimension(DcRepository.Settings.stNewItemsDialogSize));
        setResizable(true);
        setCenteredLocation();
    }
    
    private void build() {
        
        MasterView mv = DcModules.getCurrent().getInsertView();
        View view = mv.getCurrent();
            
        view.activate();
        getContentPane().setLayout(Layout.getGBL());
        getContentPane().add(view, Layout.getGBC(0, 0, 1, 1, 100.0, 100.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        
        JPanel panelAction = new JPanel();
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        panelAction.add(buttonClose);
        
        getContentPane().add(panelAction, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 10), 0, 0));
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stNewItemsDialogSize, getSize());
        setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close")) {
            close();
        }
    }
}
