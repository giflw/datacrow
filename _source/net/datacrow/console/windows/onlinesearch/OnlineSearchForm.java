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

package net.datacrow.console.windows.onlinesearch;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.MainFrame;
import net.datacrow.console.components.DcFrame;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.components.panels.OnlineServiceSettingsPanel;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.console.views.IViewComponent;
import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.IOnlineSearchClient;
import net.datacrow.core.services.OnlineServices;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.SearchTask;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.Settings;
import net.datacrow.util.StringUtils;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class OnlineSearchForm extends DcFrame implements IOnlineSearchClient, ActionListener, MouseListener, ChangeListener {

    private static Logger logger = Logger.getLogger(OnlineSearchForm.class.getName());
    
    private int module;
    private String ID;

    private boolean startSearchOnOpen = false;
    
    protected SearchTask task;
    
    private JTabbedPane tpResult;
    private ItemForm itemForm;
    private DcObjectList list;
    private DcTable table;
    
    private List<DcObject> items = new ArrayList<DcObject>();
    private Map<Integer, Boolean> loadedItems = new HashMap<Integer, Boolean>();

    private OnlineServices os;
    
    private OnlineServiceSettingsPanel panelSettings;
    private OnlineServicePanel panelService;
    private JTextArea textLog = ComponentFactory.getTextArea();
    private JProgressBar progressBar = new JProgressBar();
    
    private JPanel contentPanel;
    
    private int resultCount = 0;

    public OnlineSearchForm(OnlineServices os, DcObject dco, ItemForm itemForm, boolean advanced) {
        super(DcResources.getText("lblOnlineXSearch", DcModules.getCurrent().getObjectName()),
                                  IconLibrary._icoSearchOnline64);

        startSearchOnOpen = dco != null;

        this.ID = dco != null ? dco.getID() : null;
        this.itemForm = itemForm;
        this.module = dco != null ? dco.getModule().getIndex() : os.getModule();
        this.os = os;
        
        buildDialog(advanced);

        setHelpIndex("dc.onlinesearch");
        
        if (panelService.getQuery() == null || panelService.getQuery().trim().length() == 0)
            panelService.setQuery(dco != null ? dco.getName() : "");

        setSize(getModule().getSettings().getDimension(DcRepository.ModuleSettings.stOnlineSearchFormSize));
        setCenteredLocation();
        stopped();

        if (startSearchOnOpen && panelService.getQuery().length() > 0)
            start();
    }
    
    @Override
    public void close() {
        close(true);
    }
    
    public DcModule getModule() {
        return DcModules.get(module);
    }

    public void addObject(DcObject dco) {
        if (task != null && !task.isCancelled()) {
            dco.applyTemplate();
                
            if (ID == null)
                removeValues(dco);
            
            //dco.setValue(DcObject._ID, ID);
            
            list.add(dco);
            table.add(dco);
            items.add(dco);
            loadedItems.put(items.indexOf(dco), Boolean.valueOf(panelSettings.isQueryFullDetails()));
            
            resultCount++;
            
            checkPerfectMatch(dco);
        }
    }
    
    private IViewComponent getView() {
        int tab = tpResult.getSelectedIndex();
        if (tab == 0) 
            return list;
        else
            return table;
    }
    
    private void removeValues(DcObject dco) {
        int[] fields = getModule().getSettings().getIntArray(DcRepository.ModuleSettings.stOnlineSearchRetrievedFields);

        for (DcField field : dco.getFields()) {
            boolean allowed = false;
            for (int i = 0; fields != null && i < fields.length; i++) {
                if (field.getIndex() == fields[i])
                    allowed = true;
            }
            
            if (!allowed)
                dco.setValueLowLevel(field.getIndex(), null);
        }
    }

    public DcObject getDcObject() {
        return ID != null ? DataManager.getObject(module, ID) : null;
    }    
    
    public DcObject getSelectedObject() {
        int row = getView().getSelectedIndex();
        DcObject dco = null;
        if (row > -1 && items.size() > 0 && row < items.size()) {
            dco = items.get(row);
            dco = fill(dco);
            dco.setValue(DcObject._ID, ID);
        }
        return dco;
    }

    private DcObject fill(final DcObject dco) { 
        if (!loadedItems.get(items.indexOf(dco)).booleanValue()) {
            SearchTask task = panelService.getServer().getSearchTask(this, panelService.getMode(), panelService.getRegion(), panelService.getQuery());
            
            OnlineItemRetriever oir = new OnlineItemRetriever(task, dco);
            if (!SwingUtilities.isEventDispatchThread()) {
                oir.start();
                try { 
                    oir.join();
                } catch (Exception e) {
                    logger.error(e, e);
                }
            } else {
                logger.debug("Task executed in the GUI thread! The GUI will be locked while executing the task!");
                oir.run();
            }
            
            final DcObject o = oir.getDcObject();
            loadedItems.put(items.indexOf(dco), Boolean.TRUE);
            SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        public void run() {
                            list.updateItem(dco.getID(), o.clone(), true, false, false);
                            table.updateItem(dco.getID(), o.clone(), true, false, false);
                        }
                    }));

            return o;
        }
        
        return dco;
    }
    
    public Collection<DcObject> getSelectedObjects() {
        int row = getView().getSelectedIndex();

        if (row < 0) {
            new MessageBox(DcResources.getText("msgSelectRowForTransfer"), MessageBox._WARNING);
            return null;
        }

        // removed the clone option; it somehow managed to make the pictures disappear..
        ArrayList<DcObject> result = new ArrayList<DcObject>();
        if (ID == null) {
            int[] rows = getView().getSelectedIndices();
            for (int i = 0; i < rows.length; i++) {
                DcObject dco = items.get(rows[i]);
                
                
                
                
                result.add(fill(dco));
            }
        } else {
            result.add(getSelectedObject());
        }
        return result;
    }

    public Collection<IServer> getServers() {
        return os.getServers();
    }    

    private void open() {
        saveSettings();
        new Thread(new Runnable() {
            public void run() {
                int selectedRow = getView().getSelectedIndex();
                if (selectedRow == -1) {
                    new MessageBox(DcResources.getText("msgSelectRowToOpen"), MessageBox._WARNING);
                    return;
                }

                final DcObject o = getSelectedObject();
                if (o != null) {
                    SwingUtilities.invokeLater(
                            new Thread(new Runnable() { 
                                public void run() {
                                    ItemForm itemForm = new ItemForm(true, false, o, true);
                                    itemForm.setVisible(true);
                                }
                            }));
                }
            }
        }).start();
    }
    
    private void checkPerfectMatch(DcObject dco) {
        if (!panelSettings.isAutoAddAllowed())
            return;
        
        SearchMode mode = panelService.getMode();
        if (mode != null && mode.singleIsPerfect()) {
            panelService.hasPerfectMatchOccured(true);
        } else {
            String string = panelService.getQuery().toLowerCase();
            String item = StringUtils.normalize(dco.toString()).toLowerCase();
            panelService.hasPerfectMatchOccured(string.equals(item));
        }
        
        if (panelService.hasPerfectMatchOccured()) {
            // set the lastly added item as selected
            getView().setSelected(getView().getItemCount() - 1);
            if (ID != null) { 
                update();
            } else { 
                addNew(); 
                stop();
                clear();
                toFront();
            }
        }            
    }
    
    protected void saveSettings() {
        getModule().setSetting(DcRepository.ModuleSettings.stOnlineSearchFormSize, getSize());
        DcSettings.set(DcRepository.Settings.stOnlineSearchSelectedView, Long.valueOf(tpResult.getSelectedIndex()));
        
        panelService.save();
        panelSettings.save();
    }

    public void update() {
        new Thread(new Runnable() {
            public void run() {
                DcObject o = getSelectedObject();
                
                saveSettings();
        
                if (o == null) return;
                    
                if (itemForm.isVisible()) {
                    DcModule mod = DcModules.get(module);
                    final DcObject dco = mod.getDcObject();
        
                    Settings settings = getModule().getSettings();
                    
                    boolean overwrite = dco.getModule().getSettings().getBoolean(DcRepository.ModuleSettings.stOnlineSearchOverwrite);
                    
                    int[] fields = overwrite ?
                        settings.getIntArray(DcRepository.ModuleSettings.stOnlineSearchFieldOverwriteSettings) :
                        dco.getModule().getFieldIndices();
                        
                    // if all fails, just update all!
                    if (fields == null || fields.length == 0)
                        fields = dco.getModule().getFieldIndices();
        
                    for (int i = 0; i < fields.length; i++) {
                        int field = fields[i];

                        if (!Utilities.isEmpty(o.getValue(fields[i]))) {
                            if ((dco.isFilled(field) && overwrite) || !dco.isFilled(field))
                                dco.setValue(field, o.getValue(fields[i])); 
                        }
                    }  
                    
                    if (o.getCurrentChildren().size() > 0)
                        dco.setChildren(o.getCurrentChildren());
                    
                    SwingUtilities.invokeLater(
                            new Thread(new Runnable() { 
                                public void run() {
                                    itemForm.setData(dco, panelSettings.isOverwriteAllowed(), true);
                                }
                            }));
                }

                SwingUtilities.invokeLater(
                        new Thread(new Runnable() { 
                            public void run() {
                                close();
                            }
                        }));
            }
        }).start();
    }

    public void addNew() {
        new Thread(
                new Runnable() {
                    public void run() {
                        saveSettings();
                        Collection<DcObject> selected = getSelectedObjects();
                        
                        if (selected != null) {
                            // Create clones to prevent the cleaning task from clearing the items..
                            // This is to fix an unconfirmed bug (NullPointerException on saving new items). 
                            Collection<DcObject> clones = new ArrayList<DcObject>();
                            for (DcObject o : selected)
                                clones.add(o.clone());
                            
                            getModule().getCurrentInsertView().add(clones);
                            DataCrow.mainFrame.setSelectedTab(MainFrame._INSERTTAB);
                        }
                        
                    }
                }).start();
    }    

    public void setSelectionMode(int selectionMode) {
        getView().setSelectionMode(selectionMode);        
    }
    
    private void clear() {
        stop();
        panelService.setQuery("");
    	
        list.clear();
    	table.clear();
    	
        updateProgressBar(0);
        items.clear();
        textLog.setText("");
        panelService.setFocus();
    }

    public void initProgressBar(int maxValue) {
        if (progressBar != null) {
            progressBar.setValue(0);
            progressBar.setMaximum(maxValue);
        }
    }

    public void updateProgressBar(int value) {
    	if (progressBar != null && (value == 0 || (task != null && !task.isCancelled())))
    		progressBar.setValue(value);
    }
    
    public void stop() {
        if (task != null)
            task.cancel();

        addMessage(DcResources.getText("msgStoppedSearch"));
        stopped();
    }
    
    public void start() {
    	resultCount = 0;
    	
        if (panelService.getQuery() == null || panelService.getQuery().trim().equals("")) {
            new MessageBox(DcResources.getText("msgEnterKeyword"), MessageBox._INFORMATION);
            return;
        }
        
        saveSettings();
        processing();

        task = panelService.getServer().getSearchTask(this, panelService.getMode(), panelService.getRegion(), panelService.getQuery());
        task.setPriority(Thread.NORM_PRIORITY);
        task.setItemMode(panelSettings.isQueryFullDetails() ? SearchTask._ITEM_MODE_FULL : SearchTask._ITEM_MODE_SIMPLE);
        task.start();
    }     

    public void processed(int i) {
        updateProgressBar(i);
    }

    public void processing() {
        panelService.busy(true);
    }

    public void stopped() {
        if (panelService != null)
            panelService.busy(false);
    }    
    
    public int resultCount() {
        return resultCount;
    }

    public void processingTotal(int i) {
        initProgressBar(i);
    }    
    
    public void addError(Throwable t) {
        new MessageBox(t.getMessage(), MessageBox._ERROR);
        logger.error(t.getMessage(), t);
    }

    public void addError(String message) {
        new MessageBox(message, MessageBox._ERROR);
        addMessage(message);
    }

    public void addWarning(String warning) {
        if (panelService != null && !panelService.hasPerfectMatchOccured())
            new MessageBox(warning, MessageBox._WARNING);
    }    
    
    public void setFocus() {
        panelService.setFocus();
    }
    
    public void addDoubleClickListener(MouseListener ml) {
        list.removeMouseListener(this);
        list.addMouseListener(ml);
        
        table.removeMouseListener(this);
        table.addMouseListener(ml);
    }
    
    private void buildDialog(boolean advanced) {
        getContentPane().setLayout(Layout.getGBL());
        
        contentPanel = getContentPanel(advanced);
        
        JTabbedPane tp = ComponentFactory.getTabbedPane();
        
        panelSettings = new OnlineServiceSettingsPanel(this, true, true, ID != null, false, module);
        
        
        JPanel panel2 = new JPanel();
        panel2.setLayout(Layout.getGBL());
        panel2.add(panelSettings, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(10, 5, 5, 5), 0, 0));
        
        tp.addTab(DcResources.getText("lblSearch"), IconLibrary._icoSearch, contentPanel);
        tp.addTab(DcResources.getText("lblSettings"), IconLibrary._icoSettings, panel2);
        
        getContentPane().add(tp, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                             new Insets(0, 0, 0, 0), 0, 0));
        pack();
    }    
    
    public JPanel getContentPanel() {
        return contentPanel;
    }
    
    private JPanel getContentPanel(boolean advanced) {
        setResizable(true);
        getContentPane().setLayout(Layout.getGBL());

        //**********************************************************
        //Servers
        //**********************************************************
        panelService = new OnlineServicePanel(this, os);
        
        //**********************************************************
        //Progress panel
        //**********************************************************
        JPanel panelProgress = new JPanel();
        panelProgress.setLayout(Layout.getGBL());
        panelProgress.add(progressBar, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                         ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                          new Insets(5, 5, 5, 5), 0, 0));

        
        //**********************************************************
        //Log Panel
        //**********************************************************
        JPanel panelLog = new JPanel();
        panelLog.setLayout(Layout.getGBL());

        JScrollPane scroller = new JScrollPane(textLog);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelLog.add(scroller, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0));

        panelLog.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblLog")));

        //**********************************************************
        //Result panel
        //**********************************************************
        
        tpResult = ComponentFactory.getTabbedPane();

        list = new DcObjectList(DcObjectList._ELABORATE, false, true);
        JScrollPane sp1 = new JScrollPane(list);
        list.addMouseListener(this);
        sp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        table = new DcTable(getModule(), true, false);
        JScrollPane sp2 = new JScrollPane(table);
        table.addMouseListener(this);
        sp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        
        tpResult.addTab(DcResources.getText("lblCardView"), IconLibrary._icoCardView, sp1);
        tpResult.addTab(DcResources.getText("lblTableView"), IconLibrary._icoTableView, sp2);
        
        tpResult.setSelectedIndex(DcSettings.getInt(DcRepository.Settings.stOnlineSearchSelectedView));

        tpResult.addChangeListener(this);
        
        //**********************************************************
        //Actions panel
        //**********************************************************
        JPanel panelActions = new JPanel();

        JButton buttonDetails = ComponentFactory.getButton(DcResources.getText("lblOpen"));
        JButton buttonUpdate = ComponentFactory.getButton(DcResources.getText("lblUpdate"));
        JButton buttonAddNew = ComponentFactory.getButton(DcResources.getText("lblAddNew"));
        JButton buttonClear = ComponentFactory.getButton(DcResources.getText("lblClear"));
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));

        buttonClose.setToolTipText(DcResources.getText("tpClose"));
        buttonUpdate.setToolTipText(DcResources.getText("tpUpdate"));
        buttonAddNew.setToolTipText(DcResources.getText("tpAddNew"));
        
        buttonDetails.setMnemonic('E');
        buttonClose.setMnemonic('C');        
        buttonUpdate.setMnemonic('U');
        buttonAddNew.setMnemonic('A');
        buttonClear.setMnemonic('L');

        buttonDetails.addActionListener(this);
        buttonDetails.setActionCommand("open");
        buttonUpdate.addActionListener(this);
        buttonUpdate.setActionCommand("update");
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        buttonClear.addActionListener(this);
        buttonClear.setActionCommand("clear");
        buttonAddNew.addActionListener(this);
        buttonAddNew.setActionCommand("addnew");

        panelActions.add(buttonDetails);
        
        if (ID != null && advanced)
            panelActions.add(buttonUpdate);

        if (advanced)
            panelActions.add(buttonAddNew);

        panelActions.add(buttonClear);

        if (advanced)
            panelActions.add(buttonClose);

        //**********************************************************
        //Main Panel
        //**********************************************************
        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());

        panel.add(panelService, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        panel.add(tpResult,  Layout.getGBC( 0, 2, 1, 1, 50.0, 50.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        panel.add(panelActions,  Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
        panel.add(panelProgress, Layout.getGBC( 0, 4, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));

        if (advanced)
            panel.add(panelLog,  Layout.getGBC( 0, 5, 1, 1, 20.0, 20.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                             new Insets(5, 5, 5, 5), 0, 0));

        return panel;
    }    
    
    public void addMessage(String message) {
        if (textLog != null && task != null && !task.isCancelled())
            textLog.insert(message + '\n', 0);
    }    
    
    @Override
    public void setVisible(boolean b) {
        if (b)
            panelService.setFocus();
        super.setVisible(b);
    }    
    
    public void close(boolean saveSettings) {
        if (saveSettings)
            saveSettings();
        
        stop();

        list.removeMouseListener(this);
        list.clear();
        list = null;

        table.removeMouseListener(this);
        table.clear();
        table = null;
        
        tpResult = null;
        
        itemForm  = null;
        ID = null;
        
        // result is a direct clone; other items can safely be removed
        for (DcObject dco : items)
            dco.unload();
        
        loadedItems.clear();
        loadedItems = null;
        
        items.clear();
        items = null;
        
        textLog = null;
        progressBar = null;
        panelService.clear();
        panelService = null;
        
        panelSettings.clear();
        panelSettings = null;
        task = null;
        
        super.close();
    }     
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("stopsearch"))
            stop();
        else if (e.getActionCommand().equals("open"))
            open();
        else if (e.getActionCommand().equals("update"))
            update();
        else if (e.getActionCommand().equals("close"))
            close(true);
        else if (e.getActionCommand().equals("clear"))        
            clear();
        else if (e.getActionCommand().equals("addnew"))        
            addNew();
        else if (e.getActionCommand().equals("search")) {        
            panelService.hasPerfectMatchOccured(false);
            start();        
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        if (e.getClickCount() == 2) {
            if (ID != null)
                update();
            else 
                addNew();
        }
    }
    
    public void stateChanged(ChangeEvent e) {
        if (list == null || table == null)
            return;
        
        int tab = ((JTabbedPane) e.getSource()).getSelectedIndex();
        if (tab == 0) {
            if (table.getSelectedIndex() != -1) 
                list.setSelected(table.getSelectedIndex());
        } else {
            if (list.getSelectedIndex() != -1) 
                table.setSelected(list.getSelectedIndex());
        }
    }    
    
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
}