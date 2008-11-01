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

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.MainFrame;
import net.datacrow.console.components.DcFrame;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.components.panels.OnlineServiceSettingsPanel;
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
import net.datacrow.core.services.OnlineService;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.SearchTask;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.Settings;
import net.datacrow.util.StringUtils;

import org.apache.log4j.Logger;

public class OnlineSearchForm extends DcFrame implements IOnlineSearchClient, ActionListener, MouseListener {

    private static Logger logger = Logger.getLogger(OnlineSearchForm.class.getName());
    
    private int module;
    private String ID;

    private boolean startSearchOnOpen = false;
    
    protected SearchTask task;
    
    private ItemForm itemForm;
    private DcObjectList list;
    
    private List<DcObject> items = new ArrayList<DcObject>();
    private Map<Integer, Boolean> loadedItems = new HashMap<Integer, Boolean>();

    private OnlineService os;
    
    private OnlineServiceSettingsPanel panelSettings;
    private OnlineServicePanel panelService;
    private JTextArea textLog = ComponentFactory.getTextArea();
    private JProgressBar progressBar = new JProgressBar();
    
    private JPanel contentPanel;
    
    private int resultCount = 0;

    public OnlineSearchForm(OnlineService os, DcObject dco, ItemForm itemForm, boolean advanced) {
        super(DcResources.getText("lblOnlineXSearch", DcModules.getCurrent().getObjectName()),
                                  IconLibrary._icoSearchOnline);

        startSearchOnOpen = dco != null;

        this.ID = dco != null ? dco.getID() : null;
        this.itemForm = itemForm;
        this.module = dco != null ? dco.getModule().getIndex() : DcSettings.getInt(DcRepository.Settings.stModule);
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
            
            dco.setValue(DcObject._ID, ID);
            
            list.add(dco);
            items.add(dco);
            loadedItems.put(items.indexOf(dco), Boolean.valueOf(panelSettings.isQueryFullDetails()));
            
            resultCount++;
            
            checkPerfectMatch(dco);
        }
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
        int row = list.getSelectedIndex();

        DcObject dco = null;
        
        if (row > -1 && items.size() > 0 && row < items.size()) {
            dco = items.get(row);
            dco = fill(dco);
            dco.setValue(DcObject._ID, ID);
        }
        return dco;
    }

    private DcObject fill(DcObject dco) { 
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
            
            DcObject o = oir.getDcObject();
            loadedItems.put(items.indexOf(dco), Boolean.TRUE);
            list.updateItem(dco.getID(), o.clone(), true, false, false);
            return o;
        }
        return dco;
    }
    
    public Collection<DcObject> getSelectedObjects() {
        int row = list.getSelectedIndex();

        if (row < 0) {
            new MessageBox(DcResources.getText("msgSelectRowForTransfer"), MessageBox._WARNING);
            return null;
        }

        // removed the clone option; it somehow managed to make the pictures disappear..
        ArrayList<DcObject> result = new ArrayList<DcObject>();
        if (ID == null) {
            int[] rows = list.getSelectedIndices();
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
                int selectedRow = list.getSelectedIndex();
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
            list.setSelected(list.getModel().getSize() - 1);
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
                    
                    int[] fields = 
                        settings.getBoolean(DcRepository.ModuleSettings.stOnlineSearchOverwrite) ?
                        settings.getIntArray(DcRepository.ModuleSettings.stOnlineSearchFieldOverwriteSettings) :
                        dco.getModule().getFieldIndices();
        
                    for (int i = 0; i < fields.length; i++) {
                        int field = fields[i];
                        
                        if (dco.isFilled(field)) {
                            if (settings.getBoolean(DcRepository.ModuleSettings.stOnlineSearchOverwrite) && 
                                (o.getValue(fields[i]) != null && !o.getValue(fields[i]).equals("") && !o.getValue(fields[i]).equals("-1"))) {
                                dco.setValue(field, o.getValue(fields[i]));
                            }
                        } else {
                            dco.setValue(field, o.getValue(fields[i]));
                        }
                    }  
                    
                    if (o.getChildren() != null && o.getChildren().size() > 0)
                        dco.setChildren(o.getChildren());
                    
                    
                    SwingUtilities.invokeLater(
                            new Thread(new Runnable() { 
                                public void run() {
                                    itemForm.setData(dco, panelSettings.isOverwriteAllowed());
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
                            getModule().getCurrentInsertView().add(selected);
                            DataCrow.mainFrame.setSelectedTab(MainFrame._INSERTTAB);
                        }
                        
                    }
                }).start();
    }    

    public void setSelectionMode(int selectionMode) {
        list.setSelectionMode(selectionMode);        
    }
    
    private void clear() {
        stop();
        panelService.setQuery("");
    	list.clear();
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
            task.cancelSearch();

        addMessage(DcResources.getText("msgStoppedSearch"));
        stopped();
    }
    
    public void start() {
    	resultCount = 0;
    	
        if (panelService.getQuery() == null || panelService.getQuery().trim().equals("")) {
            new MessageBox(this, DcResources.getText("msgEnterKeyword"), MessageBox._INFORMATION);
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
        return resultCount;// list.getItems().size();
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
    }
    
    private void buildDialog(boolean advanced) {
        getContentPane().setLayout(Layout.getGBL());
        
        contentPanel = getContentPanel(advanced);
        
        JTabbedPane tp = ComponentFactory.getTabbedPane();
        
        panelSettings = new OnlineServiceSettingsPanel(this, true, true, ID != null, module);
        
        tp.addTab("Search", contentPanel);
        tp.addTab("Settings", panelSettings);
        
        
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
        JPanel panelResults = new JPanel();
        panelResults.setLayout(Layout.getGBL());

        list = new DcObjectList(DcObjectList._ELABORATE, false, true);

        JScrollPane scrollTable = new JScrollPane(list);
        list.addMouseListener(this);
        scrollTable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panelResults.add(scrollTable,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                        ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                         new Insets(5, 5, 5, 5), 0, 0));

        //**********************************************************
        //Actions panel
        //**********************************************************
        JPanel panelActions = new JPanel();
        panelActions.setLayout(Layout.getGBL());

        JButton buttonDetails = ComponentFactory.getButton(DcResources.getText("lblDetails"));
        
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
        buttonDetails.setActionCommand("showdetails");
        buttonUpdate.addActionListener(this);
        buttonUpdate.setActionCommand("update");
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        buttonClear.addActionListener(this);
        buttonClear.setActionCommand("clear");
        buttonAddNew.addActionListener(this);
        buttonAddNew.setActionCommand("addnew");

        panelActions.add(buttonDetails,    Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        if (ID != null && advanced) {
            panelActions.add(buttonUpdate,    Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                             new Insets(5, 5, 5, 5), 0, 0));
        } 

        if (advanced)
            panelActions.add(buttonAddNew,    Layout.getGBC( 2, 0, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                             new Insets(5, 5, 5, 5), 0, 0));

        panelActions.add(buttonClear,        Layout.getGBC( 3, 0, 1, 1, 1.0, 1.0
                        ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                        new Insets(5, 5, 5, 5), 0, 0));

        if (advanced)
            panelActions.add(buttonClose,     Layout.getGBC( 4, 0, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                             new Insets(5, 5, 5, 5), 0, 0));

        //**********************************************************
        //Main Panel
        //**********************************************************
        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());

        panel.add(panelService, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        
        panel.add(panelResults,  Layout.getGBC( 0, 2, 1, 1, 50.0, 50.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                             new Insets(5, 5, 5, 5), 0, 0));
        panel.add(panelActions,  Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                             new Insets(5, 5, 5, 5), 0, 0));
        panel.add(panelProgress, Layout.getGBC( 0, 4, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                             new Insets(5, 5, 5, 5), 0, 0));

        if (advanced)
            panel.add(panelLog,  Layout.getGBC( 0, 5, 1, 1, 10.0, 10.0
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
        else if (e.getActionCommand().equals("showdetails"))
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
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
}