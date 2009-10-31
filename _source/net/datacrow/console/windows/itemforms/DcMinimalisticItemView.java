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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.MainFrame;
import net.datacrow.console.components.DcPanel;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.menu.DcPropertyViewPopupMenu;
import net.datacrow.console.views.ISimpleItemView;
import net.datacrow.console.windows.CreateMultipleItemsDialog;
import net.datacrow.console.windows.DcFrame;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.requests.IRequest;
import net.datacrow.core.wf.requests.RefreshSimpleViewRequest;
import net.datacrow.core.wf.requests.Requests;
import net.datacrow.core.wf.requests.StatusUpdateRequest;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.Settings;
import net.datacrow.util.DataTask;
import net.datacrow.util.DcSwingUtilities;

public class DcMinimalisticItemView extends DcFrame implements ActionListener, MouseListener, ISimpleItemView {

    protected DataTask task;
    
    private final boolean readonly;
    
    private JScrollPane scroller;
    
    private JPanel panelActions = new JPanel();
    private JPanel statusPanel;
    
    private JButton buttonCreateMultiple = ComponentFactory.getButton(DcResources.getText("lblAddMultiple"));
    private JButton buttonNew = ComponentFactory.getButton(DcResources.getText("lblNew"));
    private JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
    
    protected DcObjectList list;
    protected int module;

    private DcPanel panel = new DcPanel();
    
    public DcMinimalisticItemView(int module, boolean readonly) {
        super(DcModules.get(module).getObjectNamePlural(), DcModules.get(module).getIcon32());
        
        this.list = new DcObjectList(DcObjectList._LISTING, false, true);
        this.readonly = readonly;
        
        DcSwingUtilities.setRootFrame(this);
        this.module = module;
        
        buildPanel();

        pack();
        
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
        DcObject dco = list.getSelectedItem();
        if (dco != null) {
            dco.markAsUnchanged();
            DcMinimalisticItemForm itemForm = new DcMinimalisticItemForm(false, true, dco, this);
            itemForm.setVisible(true);
        }
    }
    
    public void createMultiple() {
    	CreateMultipleItemsDialog dlg = new CreateMultipleItemsDialog(getModuleIdx());
    	dlg.setVisible(true);
    	loadItems();
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
    
    public void setObjects(Collection<DcObject> objects) {
        list.clear();
        list.add(objects);
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
            loadItems();
            setCenteredLocation();
        }
    }    
    
    public void clear() {
        if (task != null)
            task.cancel();
        
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
    
    public void loadItems() {
        list.clear();
        DcObject dco = DcModules.get(module).getItem();
        DataFilter filter = new DataFilter(dco);
        filter.setOrder(new DcField[] {dco.getField(DcModules.get(module).getDefaultSortFieldIdx())});
        list.add(DataManager.get(module, filter));
        dco.release();
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
        panelActions.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        buttonCreateMultiple.addActionListener(this);
        buttonCreateMultiple.setActionCommand("createMultiple");
        
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
        
        getContentPane().add(panel,         Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));
        
        if (!readonly) {
            getContentPane().add(panelActions,  Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                     new Insets(0, 0, 0, 0), 0, 0));
            getContentPane().add(statusPanel,  Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
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
    
    public void setStatus(String text) {
        panel.setStatus(text);
    }    
    
    public void delete() {
        if (!isTaskRunning()) {
            Collection<DcObject> objects = list.getSelectedItems();
            if (objects.size() > 0) {
                task = new DeletingThread(objects);
                task.start();
            } else {
                DcSwingUtilities.displayWarningMessage("msgSelectItemToDel");
            }
        }
    }     
    
    protected class DeletingThread extends DataTask {

        public DeletingThread(Collection<DcObject> objects) {
            super(objects);
        }
        
        @Override
        public void run() {
            try {
                updateProgressBar(0);
                initProgressBar(objects.length);   
                list.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                
                setStatus("Deleting " + objects.length + " items from to the database");
                startRunning();
                denyActions();
                
                if (!DcSwingUtilities.displayQuestion("msgDeleteQuestion")) {
                    stopRunning();
                } else {
                    int counter = 1;
                    for (DcObject dco : objects) {
                        updateProgressBar(counter);
                        
                        dco.markAsUnchanged();
                        dco.setSilent(true);
                        
                        if (counter == objects.length) {
                            Requests requests = getAfterDeleteRequests();
                            for (int j = 0; j < requests.get().length; j++) {
                                dco.addRequest(requests.get()[j]);
                            }
                            
                            dco.setEndOfBatch(true);
                            IRequest request = 
                                new StatusUpdateRequest(dco.getModule().getIndex(), MainFrame._SEARCHTAB, "msgDeleteSuccessfull");
                            request.setExecuteOnFail(true);
                            dco.addRequest(request);
                        } else {
                            dco.setEndOfBatch(false);
                        }

                        dco.delete(); 
                        
                        try {
                            sleep(300);
                        } catch (Exception ignore) {}
                        
                        counter++;
                    }
                    
                    list.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            } finally {
                list.setSelectedIndex(0);
                stopRunning();
                allowActions();
            }
        }
    }     
    
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
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("createNew"))
            createNew();
        else if (e.getActionCommand().equals("close"))
            close();
        else if (e.getActionCommand().equals("createMultiple"))
            createMultiple();
    }
}