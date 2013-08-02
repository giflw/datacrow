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

package net.datacrow.console.windows.itemforms;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcMenu;
import net.datacrow.console.components.DcMenuBar;
import net.datacrow.console.components.DcMenuItem;
import net.datacrow.console.components.DcPanel;
import net.datacrow.console.components.DcShortTextField;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.components.lists.elements.DcListElement;
import net.datacrow.console.components.lists.elements.DcObjectListElement;
import net.datacrow.console.menu.DcPropertyViewPopupMenu;
import net.datacrow.console.views.ISimpleItemView;
import net.datacrow.console.windows.CreateMultipleItemsDialog;
import net.datacrow.console.windows.DcFrame;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.plugin.PluginHelper;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.RefreshSimpleViewRequest;
import net.datacrow.core.wf.requests.Requests;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.Settings;
import net.datacrow.util.DataTask;
import net.datacrow.util.DcSwingUtilities;

public class DcMinimalisticItemView extends DcFrame implements ActionListener, MouseListener, ISimpleItemView, KeyListener {

    private static final FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
    
    protected DeletingThread task;
    
    private final boolean readonly;
    
    private JScrollPane scroller;
    
    private JPanel panelActions = new JPanel();
    private JPanel statusPanel;
    
    private JButton buttonCreateMultiple = ComponentFactory.getButton(DcResources.getText("lblCreateMultiple"));
    private JButton buttonNew = ComponentFactory.getButton(DcResources.getText("lblNew"));
    private JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
    
    protected DcObjectList list;
    protected int module;

    private List<DcListElement> all = new ArrayList<DcListElement>();
    
    private DcPanel panel = new DcPanel();
    
    public DcMinimalisticItemView(int module, boolean readonly) {
        super(DcModules.get(module).getObjectNamePlural(), DcModules.get(module).getIcon32());
        
        this.list = new DcObjectList(DcObjectList._LISTING, false, true);
        this.readonly = readonly;
        
        DcSwingUtilities.setRootFrame(this);
        this.module = module;
        
        buildPanel();

        pack();
        
        setHelpIndex("dc.items.administration");
        
        Settings settings = DcModules.get(module).getSettings();
        setSize(settings.getDimension(DcRepository.ModuleSettings.stSimpleItemViewSize));
        setCenteredLocation();
    }
    
    public void hideDialogActions(boolean b) {
        buttonClose.setVisible(!b);
        statusPanel.setVisible(!b);
    }
    
    @Override
    public void close() {
        setVisible(false);
    }
    
    public void open() {
        open(true);
    }
    
    public void open(boolean edit) {
        DcObject dco = list.getSelectedItem();
        if (dco != null) {
            dco.markAsUnchanged();
            DcMinimalisticItemForm itemForm = new DcMinimalisticItemForm(!edit, true, dco, this);
            itemForm.setVisible(true);
        }
    }
    
    public void createMultiple() {
    	CreateMultipleItemsDialog dlg = new CreateMultipleItemsDialog(getModuleIdx());
    	dlg.setVisible(true);
    	load();
    }
    
    public void createNew() {
        DcMinimalisticItemForm itemForm = new DcMinimalisticItemForm(false, false, DcModules.get(module).getItem(), this);
        itemForm.setVisible(true);
    }
    
    public Requests getAfterDeleteRequests() {
        Requests requests = new Requests();
        requests.add(new RefreshSimpleViewRequest(this));
        return requests;
    }    
    
    public void setObjects(List<DcObject> objects) {
        all.clear();
        list.clear();
        list.add(objects);
        
        all.addAll(list.getElements());
    }
    
    public Collection<DcObject> getItems() {
        return list.getItems();
    }
    
    protected JPopupMenu getPopupMenu() {
        return new DcPropertyViewPopupMenu(this);
    }
    
    protected int getModuleIdx() {
        return module;
    }
    
    public DcModule getModule() {
        return DcModules.get(getModuleIdx());
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            load();
            setCenteredLocation();
        }
    }    
    
    public void clear() {
        if (task != null)
            task.cancel();
        
        if (all != null)
            all.clear();
        
        all = null;
        task = null;
        scroller = null;
        panelActions = null;
        statusPanel = null;
        buttonNew = null;
        buttonCreateMultiple = null;
        buttonClose = null;
        list.clear();
        list = null;
        panel = null;
    }
    
    @Override
    public void load() {
        if (all != null) all.clear();
        list.clear();
        DcObject dco = DcModules.get(module).getItem();
        DataFilter filter = new DataFilter(module);
        filter.setOrder(new DcField[] {dco.getField(DcModules.get(module).getDefaultSortFieldIdx())});
        list.add(DataManager.getKeys(filter));
        
        all.addAll(list.getElements());
    }    

    @Override
    public void setFont(Font font) {
        Font fontNormal = DcSettings.getFont(DcRepository.Settings.stSystemFontNormal);
        Font fontSystem = DcSettings.getFont(DcRepository.Settings.stSystemFontBold);

        super.setFont(fontNormal);
        if (panel != null) {
            panel.setFont(fontSystem);
            buttonClose.setFont(fontSystem);
            buttonNew.setFont(fontSystem);
            buttonCreateMultiple.setFont(fontSystem);
            list.setFont(fontNormal);
        }
    }      
    
    public void denyActions() {
        list.removeMouseListener(this);    
    }

    public void allowActions() {
        list.removeMouseListener(this);
        list.addMouseListener(this);
    }    
    
    /**
     * Indicates whether there is a data task running at this moment
     */
    protected boolean isTaskRunning() {
        boolean isTaskRunning = task != null && task.isRunning();
        if (isTaskRunning)
            DcSwingUtilities.displayWarningMessage("msgJobRunning");

        return isTaskRunning; 
    }     

    private void buildPanel() {
        getContentPane().setLayout(Layout.getGBL());
        
        scroller = new JScrollPane(list);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);        
        
        list.addMouseListener(this);
        
        //**********************************************************
        //Result panel
        //**********************************************************
        panel.setLayout(Layout.getGBL());
        panel.add(scroller,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        //**********************************************************
        //Action panel
        //**********************************************************
        panelActions.setLayout(layout);
        
        buttonCreateMultiple.addActionListener(this);
        buttonCreateMultiple.setActionCommand("createMultiple");
        buttonCreateMultiple.setToolTipText(DcResources.getText("tpCreateMultiple"));
        
        buttonNew.addActionListener(this);
        buttonNew.setActionCommand("createNew");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        if (!getModule().isAbstract() && getModule().getType() != DcModule._TYPE_TEMPLATE_MODULE)
            panelActions.add(buttonCreateMultiple);
        
        panelActions.add(buttonNew);
        panelActions.add(buttonClose);
        
        //**********************************************************
        //Main panel
        //**********************************************************
        statusPanel = panel.getStatusPanel();
        
        if (DcModules.get(module).getType() == DcModule._TYPE_PROPERTY_MODULE) {
            setJMenuBar(new DcMinimalisticItemViewMenu(DcModules.get(module), this));
        
            JTextField txtFilter = ComponentFactory.getShortTextField(255);
            txtFilter.addKeyListener(this);        
    
            getContentPane().add(txtFilter, Layout.getGBC( 0, 0, 2, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets( 0, 5, 0, 5), 0, 0));
        }
        
        getContentPane().add(panel, Layout.getGBC( 0, 1, 1, 1, 80.0, 80.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));
        
        if (!readonly) {
            getContentPane().add(panelActions,  Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                     new Insets(0, 0, 0, 0), 0, 0));
            getContentPane().add(statusPanel,  Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(0, 0, 0, 0), 0, 0));
        }
        
    }
    
    public void initProgressBar(int maxValue) {
        panel.initProgressBar(maxValue);
    }
    
    public void updateProgressBar(int value) {
        panel.updateProgressBar(value);
    }
    
    public void deleteUnused() {
        if (isTaskRunning() || !DcSwingUtilities.displayQuestion(
                DcResources.getText("msgDeleteQuestionUnusedItems", DcModules.get(module).getObjectNamePlural()))) 
            return;
        
        Collection<DcObject> objects = list.getItems();
        if (objects.size() > 0) {
            task = new DeletingThread(objects);
            task.setIgnoreWarnings(true);
            task.start();
        }
    }
    
    public void delete() {
        if (isTaskRunning() || !DcSwingUtilities.displayQuestion("msgDeleteQuestion")) 
            return;
        
        Collection<DcObject> objects = list.getSelectedItems();
        if (objects.size() > 0) {
            task = new DeletingThread(objects);
            task.start();
        } else {
            DcSwingUtilities.displayWarningMessage("msgSelectItemToDel");
        }
    }  
    
    @Override
    public void keyPressed(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}    
    
    @Override
    public void keyReleased(KeyEvent e) {
        DcShortTextField txtFilter = (DcShortTextField) e.getSource();
        String filter = txtFilter.getText();
        
        if (filter.trim().length() == 0) {
            list.clear();
            list.addElements(all);
        } else {
            List<DcListElement> filtered = new ArrayList<DcListElement>();
            for (DcListElement el : all) {
                DcObjectListElement element = (DcObjectListElement) el;
                if (element.getDcObject().toString().toLowerCase().startsWith(filter.toLowerCase()))
                    filtered.add(el);
            }
        
            list.clear();
            
            list.addElements(filtered);
        }
    }    
    
    protected class DeletingThread extends DataTask {

        boolean ignoreWarning = false;
        
        public DeletingThread(Collection<DcObject> objects) {
            super(null, objects);
        }
        
        public void setIgnoreWarnings(boolean b) {
            ignoreWarning = b;
        }
        
        @Override
        public void run() {
            try {
                updateProgressBar(0);
                initProgressBar(items.size());   
                list.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                
                startTask();
                denyActions();
                int counter = 1;
                Requests requests;
                for (DcObject dco : items) {
                    updateProgressBar(counter);
                    
                    dco.markAsUnchanged();
                    
                    if (counter == items.size()) {
                        requests = getAfterDeleteRequests();
                        for (int j = 0; j < requests.get().length; j++) {
                            dco.addRequest(requests.get()[j]);
                        }
                    } 

                    try {
                        dco.delete(true);
                    } catch (ValidationException e) {
                        if (!ignoreWarning) DcSwingUtilities.displayWarningMessage(e.getMessage());
                    }
                    
                    try {
                        sleep(300);
                    } catch (Exception ignore) {}
                    
                    counter++;
                }
                
                list.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } finally {
                list.setSelectedIndex(0);
                endTask();
                allowActions();
            }
        }

        @Override
        public void startTask() {
            denyActions();
            super.startTask();
        }

        @Override
        public void endTask() {
            allowActions();
            super.endTask();
        }
    }     
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (!readonly) {
            if (SwingUtilities.isRightMouseButton(e)) {
                if (list.getSelectedIndex() == -1) {
                    int index = list.locationToIndex(e.getPoint());
                    list.setSelectedIndex(index);
                }
                
                if (list.getSelectedIndex() > -1) {
                    JPopupMenu menu = getPopupMenu();                
                    menu.setInvoker(list);
                    menu.show(list, e.getX(), e.getY());
                }
            }

            if (e.getClickCount() == 2 && list.getSelectedIndex() > -1) 
                open();
        }
    }
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("createNew"))
            createNew();
        else if (e.getActionCommand().equals("close"))
            close();
        else if (e.getActionCommand().equals("createMultiple"))
            createMultiple();
        else if (e.getActionCommand().equals("delete"))
            delete();
        else if (e.getActionCommand().equals("open_readonly"))
            open(false);        
        else if (e.getActionCommand().equals("open_edit"))
            open(true);
        else if (e.getActionCommand().equals("delete_unused"))
            deleteUnused();
    }
    
    private class DcMinimalisticItemViewMenu extends DcMenuBar {
        
        private DcModule module;
        
        public DcMinimalisticItemViewMenu(DcModule module, DcMinimalisticItemView parent) {
            this.module = module;
            build(parent);
        }
        
        private void build(DcMinimalisticItemView parent) {
            DcMenu menuEdit = ComponentFactory.getMenu(DcResources.getText("lblEdit"));
            
            DcMenuItem miOpen = new DcMenuItem(DcResources.getText("lblOpen"));
            DcMenuItem miEdit = new DcMenuItem(DcResources.getText("lblEdit"));
            DcMenuItem miAdd = new DcMenuItem(DcResources.getText("lblNewItem", ""));
            DcMenuItem miDelete = new DcMenuItem(DcResources.getText("lblDelete"));
            DcMenuItem miDeleteUnused = new DcMenuItem(DcResources.getText("lblDeleteUnassigned", module.getObjectNamePlural()));
            
            miOpen.addActionListener(parent);
            miEdit.addActionListener(parent);
            miAdd.addActionListener(parent);
            miDelete.addActionListener(parent);
            miDeleteUnused.addActionListener(parent);
            
            miOpen.setActionCommand("open_readonly");
            miEdit.setActionCommand("open_edit");
            miAdd.setActionCommand("createNew");
            miDelete.setActionCommand("delete");
            miDeleteUnused.setActionCommand("delete_unused");
            
            miOpen.setIcon(IconLibrary._icoOpen);
            miEdit.setIcon(IconLibrary._icoOpen);
            miAdd.setIcon(IconLibrary._icoAdd);
            miDelete.setIcon(IconLibrary._icoDelete);
            miDeleteUnused.setIcon(IconLibrary._icoDelete);
            
            menuEdit.add(miOpen);
            menuEdit.add(miEdit);
            menuEdit.add(miAdd);
            menuEdit.add(miDelete);
            menuEdit.addSeparator();
            menuEdit.add(miDeleteUnused);
            
            add(menuEdit);
            
            DcMenu menuSettings = ComponentFactory.getMenu(DcResources.getText("lblSettings"));
            PluginHelper.add(menuSettings, "ItemFormSettings", module.getIndex());
            PluginHelper.add(menuSettings, "FieldSettings", module.getIndex());
            add(menuSettings);
        }
    }
}