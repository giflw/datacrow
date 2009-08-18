/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
 *                                                                            *
 *                       This file is part of Data Crow.                      *
 *       Data Crow is free software; you can redistribute it and/or           *
 *        modify it under the terms of the GNU General Public                 *
 *       License as published by the Free Software Foundation; either         *
 *              version 3 of the License, or any later version.               *
 *                                                                            *
 *        Data Crow is distributed in the hope that it will be useful,        *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *           MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.             *
 *           See the GNU General Public License for more details.             *
 *                                                                            *
 *        You should have received a copy of the GNU General Public           *
 *  License along with this program. If not, see http://www.gnu.org/licenses  *
 *                                                                            *
 ******************************************************************************/

package net.datacrow.console.windows;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.DcLongTextField;
import net.datacrow.console.components.DcProgressBar;
import net.datacrow.console.components.panels.OnlineServicePanel;
import net.datacrow.console.components.panels.OnlineServiceSettingsPanel;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.settings.DcSettings;
import net.datacrow.synchronizers.ISynchronizerClient;
import net.datacrow.synchronizers.Synchronizer;

public class ItemSynchronizerDialog extends DcDialog implements ActionListener, ISynchronizerClient {
    
    private DcProgressBar progressBar = new DcProgressBar();
    private JTextArea textLog = ComponentFactory.getTextArea();
    private JButton buttonStart = ComponentFactory.getButton(DcResources.getText("lblRun"));
    private JButton buttonStop = ComponentFactory.getButton(DcResources.getText("lblStop"));
    private JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
    private DcLongTextField textHelp = ComponentFactory.getLongTextField();
    
    private JComboBox cbItemPickMode = ComponentFactory.getComboBox();
    
    private JCheckBox checkReparseFiles = ComponentFactory.getCheckBox(DcResources.getText("lblReparseMusicFiles"));
    
    private OnlineServiceSettingsPanel panelOnlineServiceSettings;
    private OnlineServicePanel panelServer;
    private Synchronizer synchronizer;
    
    private final int module;
    private boolean cancelled = true;
    private final boolean canParseFiles;
    
    public ItemSynchronizerDialog(DcModule module) {
        super(DataCrow.mainFrame);

        this.module = module.getIndex();
        this.synchronizer = module.getSynchronizer();
        this.canParseFiles = synchronizer.canParseFiles();

        setTitle(synchronizer.getTitle());
        setHelpIndex(synchronizer.getHelpIndex());

        buildDialog(module.getOnlineServices().getServers());
        
        pack();
        
        setSize(module.getSettings().getDimension(DcRepository.ModuleSettings.stSynchronizerDialogSize));
        setCenteredLocation();
        
        enableActions(true);
    }
    
    protected void synchronize() {
        panelOnlineServiceSettings.save();
        synchronizer.synchronize(this);
    }
    
    public boolean isReparseFiles() {
        return checkReparseFiles.isSelected();
    }
    
    public boolean useOnlineService() {
        return panelServer.useOnlineService();
    }
    
    public int getItemPickMode() {
        return cbItemPickMode.getSelectedIndex() < 1 ?
               Synchronizer._ALL : Synchronizer._SELECTED;
    }

    public void initialize() {
        cancelled = false;
        initProgressBar(0);
        enableActions(false);
    }
    
    protected void cancel() {
        cancelled = true;
        enableActions(true);
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void close() {
        cancel();

        if (cbItemPickMode.getSelectedIndex() > -1)
            DcSettings.set(DcRepository.Settings.stMassUpdateItemPickMode, 
                           Long.valueOf(cbItemPickMode.getSelectedIndex()));
        
        cancelled = true;
        
        progressBar = null;
        textLog = null;
        buttonStart = null;
        buttonClose = null;
        textHelp = null;
        buttonStop = null;
        
        panelOnlineServiceSettings.save();
        panelOnlineServiceSettings.clear();
        panelOnlineServiceSettings = null;
        
        panelServer.clear();
        panelServer = null;
        synchronizer = null;
        cbItemPickMode = null;
        
        DcModules.get(module).setSetting(DcRepository.ModuleSettings.stSynchronizerDialogSize, getSize());

        super.close();
    }    
    
    public IServer getServer() {
        return panelServer.getServer();
    }

    public SearchMode getSearchMode() {
        return panelServer.getMode();
    }
    
    public Region getRegion() {
        return panelServer.getRegion();
    }
    
    public void enableActions(boolean b) {
        if (buttonStart != null)
            buttonStart.setEnabled(b);
        
        if (buttonStop != null)
            buttonStop.setEnabled(!b);
        
        if (!b) {
            progressBar.setValue(0);
            textLog.setText("");
        }
    }    
    
    public void addMessage(String message) {
        if (textLog != null) 
            textLog.insert(message + '\n', 0);
    }

    public void initProgressBar(int maxValue) {
        progressBar.setValue(0);
        progressBar.setMaximum(maxValue);
    }

    public void updateProgressBar() {
        if (progressBar != null)
            progressBar.setValue(progressBar.getValue() + 1);
    }
    
    private void buildDialog(Collection<IServer> servers) {
        getContentPane().setLayout(Layout.getGBL());

        //**********************************************************
        //Help panel
        //**********************************************************
        textHelp.setText(synchronizer.getHelpText());
        textHelp.setBorder(null);

        JScrollPane helpScroller = new JScrollPane(textHelp);
        helpScroller.setBorder(null);
        helpScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        helpScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        textHelp.setEditable(false);
        
        //**********************************************************
        //Settings panel
        //**********************************************************
        JPanel panelSettings = new JPanel();
        panelSettings.setLayout(Layout.getGBL());
        panelSettings.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblSettings")));
        
        panelSettings.add(ComponentFactory.getLabel(DcResources.getText("lblUpdatingWhichItems")), 
                Layout.getGBC(0, 0, 1, 1, 1.0, 1.0, 
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));
        panelSettings.add(cbItemPickMode, 
                Layout.getGBC(1, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        
        cbItemPickMode.addItem(DcResources.getText("lblAllItemsInView"));
        cbItemPickMode.addItem(DcResources.getText("lblSelectedItemsOnly"));
        cbItemPickMode.setSelectedIndex(DcSettings.getInt(DcRepository.Settings.stMassUpdateItemPickMode));
        
        //**********************************************************
        //Online Server panel
        //**********************************************************
        panelServer = new OnlineServicePanel(servers, true, canParseFiles ? true : false);

        //**********************************************************
        //Online Server Settings panel
        //**********************************************************
        panelOnlineServiceSettings = new OnlineServiceSettingsPanel(null, false, false, true, true, module);

        //**********************************************************
        //Re-parse panel
        //**********************************************************
        JPanel panelReparse = new JPanel();
        
        if (canParseFiles) {
            panelReparse.setLayout(Layout.getGBL());
            panelReparse.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblMusicFileProcessingConfig")));
            
            panelReparse.add(checkReparseFiles, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                             new Insets(5, 5, 5, 5), 0, 0));
            checkReparseFiles.setSelected(true);
        }
        
        //**********************************************************
        //Log panel
        //**********************************************************
        JPanel panelLog = new JPanel();
        panelLog.setLayout(Layout.getGBL());
        
        JScrollPane logScroller = new JScrollPane(textLog);
        logScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panelLog.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblLog")));
        panelLog.add(logScroller, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));


        //**********************************************************
        //Action panel
        //**********************************************************
        JPanel panelActions = new JPanel();
        
        buttonStart.addActionListener(this);
        buttonStart.setActionCommand("synchronize");
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        buttonStop.addActionListener(this);
        buttonStop.setActionCommand("cancel");
        
        panelActions.add(buttonStart);
        panelActions.add(buttonStop);
        panelActions.add(buttonClose);
        
        buttonStart.setMnemonic('S');
        buttonStop.setMnemonic('T');
        buttonClose.setMnemonic('C');  
        
        //**********************************************************
        //Main
        //**********************************************************
        getContentPane().add(helpScroller,      Layout.getGBC(0, 0, 1, 1, 3.0, 3.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelSettings,     Layout.getGBC(0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelServer,       Layout.getGBC(0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelReparse,      Layout.getGBC(0, 3, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelOnlineServiceSettings,     Layout.getGBC(0, 4, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions,      Layout.getGBC( 0, 5, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                 new Insets(0, 0, 0, 0), 0, 0));
        getContentPane().add(panelLog,          Layout.getGBC( 0, 6, 1, 1, 5.0, 5.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));
        getContentPane().add(progressBar,       Layout.getGBC( 0, 7, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("cancel"))
            cancel();
        else if (ae.getActionCommand().equals("synchronize"))
            synchronize();
    }
}
