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

package net.datacrow.console.views;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcPanel;
import net.datacrow.console.components.DcViewDivider;
import net.datacrow.console.components.panels.QuickViewPanel;
import net.datacrow.console.components.panels.tree.GroupingPane;
import net.datacrow.console.views.tasks.DeleteTask;
import net.datacrow.console.views.tasks.FillerTask;
import net.datacrow.console.views.tasks.SaveTask;
import net.datacrow.console.windows.UpdateAllDialog;
import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.windows.messageboxes.QuestionBox;
import net.datacrow.console.windows.reporting.ReportingDialog;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.plugin.InvalidPluginException;
import net.datacrow.core.plugin.Plugins;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DataTask;

import org.apache.log4j.Logger;

/**
 * The Swing presentation. A view uses a view component to render items.
 * Any component implementing the IViewComponent interface can be used as a view
 * component. 
 */
public class View extends DcPanel implements ListSelectionListener {

    private static Logger logger = Logger.getLogger(View.class.getName());
    
    public static final int _TYPE_SEARCH = 0;
    public static final int _TYPE_INSERT = 1;
    
    protected IViewComponent vc;
    protected DataTask task;
    
    protected DcViewDivider vdQuickPane;
    protected DcViewDivider vdGroupingPane;
    protected ViewActionPanel actionPanel;
    
    private GroupingPane groupingPane;
    
    protected QuickViewPanel quickView;
    protected boolean updateQuickView = true;
    private boolean checkForChanges = true;

    protected JPanel panelResult = new JPanel();
    protected JPanel panelStatus = getStatusPanel();
    
    private JScrollPane spChildView;
    private View childView;
    private View parentView;

    private boolean actionsAllowed = true;
    
    private String parentID;
    
    private final int type;
    private final int index;
    
    private final ViewMouseListener vml = new ViewMouseListener();
    
    public View(MasterView mv, int type, IViewComponent vc, String title, ImageIcon icon, int index) {
        super(title, icon);  
        
        this.groupingPane = mv.getGroupingPane();
        this.type = type;
        this.vc = vc;
        this.index = index;
        
        DcModule cm = vc.getModule() != null ? vc.getModule().getChild() : null;
        this.childView = cm != null ?
                getType() == _TYPE_SEARCH ? cm.getSearchView().get(index) :
                cm.getInsertView().get(index) : null;
        
        if (childView != null)
            childView.setParentView(this);
                
        vc.addMouseListener(vml);
        vc.addSelectionListener(this);
        vc.addKeyListener(new ViewKeyListener(this));

        this.actionPanel = new ViewActionPanel(this);
        
        build();
        
        setFocusable(true);

        if (    SecurityCentre.getInstance().getUser() == null ||
                SecurityCentre.getInstance().getUser().isAdmin()) {
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE");
            getActionMap().put("DELETE", new AbstractAction() {
                public void actionPerformed(ActionEvent ignored) {
                    delete();
                }
            });
        }
    }
    
    @Override
    public String getHelpIndex() {
    	if (getType() == _TYPE_INSERT)
    		return "dc.items.inserting";	
    	else 
    		return "dc.items.views";
    }
    
    public void setListSelectionListenersEnabled(boolean b) {
        if (b)
            vc.addSelectionListener(this);
        else
            vc.removeSelectionListener(this);
    }

    public int getOptimalItemAdditionBatchSize() {
        return vc.getOptimalItemAdditionBatchSize();
    }
    
    protected boolean allowsHorizontalTraversel() {
        return vc.allowsHorizontalTraversel();
    }
    
    protected boolean allowsVerticalTraversel() {
        return vc.allowsVerticalTraversel();
    }
    
    public boolean isParent() {
        return childView != null && childView.isVisible();
    }
    
    public boolean isChild() {
        return parentView != null;
    }
    
    public View getParentView() {
        return parentView;
    }

    public View getChildView() {
        return childView;
    }
    
    public void setParentView(View parentView) {
        this.parentView = parentView;
    }      
    
    public void deselect() {
        vc.deselect();
    }
    
    public IViewComponent getViewComponent() {
        return vc;
    }
    
    public int getIndex() {
        return index;
    }
    
    public int getType() {
        return type;
    }
    
    protected boolean isTaskRunning() {
        boolean isTaskRunning = task != null && task.isRunning() && task.isAlive();
        if (isTaskRunning)
            new MessageBox(DcResources.getText("msgJobRunning"), MessageBox._WARNING); 
        
        return isTaskRunning; 
    }
    
    public void applyViewDividerLocation() {
        if (vdQuickPane != null)
            vdQuickPane.setDividerLocation(DcSettings.getInt(DcRepository.Settings.stQuickViewDividerLocation));
        
        if (vdGroupingPane != null)
            vdGroupingPane.setDividerLocation(DcSettings.getInt(DcRepository.Settings.stTreeDividerLocation));
    }
    
    public DataTask getCurrentTask() {
        return task;
    }
    
    public void undoChanges() {
        vc.undoChanges();
    }    
    
    public void afterUpdate() {
        vc.afterUpdate();
    }
    
    public void setDefaultSelection() {
        try {
            int total = getItemCount();
            if (total > 0) {
                vc.setSelected(0);
                afterSelect(0);
            }
        } catch (Exception e) {
            logger.error(e, e);
        }
    }
    
    public void sort() {
        int rowCount = getItemCount();
        ArrayList<DcObject> objects = new ArrayList<DcObject>();
        for (int i = 0; i < rowCount; i++)
            objects.add(getItemAt(i));
        
        DataFilter df = new DataFilter(vc.getModule().getIndex());
        df.setOrder(vc.getModule().getSettings().getStringArray(DcRepository.ModuleSettings.stSearchOrder));
        df.sort(objects);

        add(objects);
    }
    
    public void openUpdateAllDialog() {
        UpdateAllDialog dialog = new UpdateAllDialog(this);
        dialog.setVisible(true);
    }     

    public DcModule getModule() {
        return vc.getModule();
    }
    
    public void add(DcObject dco) {
        add(dco, true);
    }    

    public void add(final DcObject dco, final boolean select) {
        vc.add(dco);
        
        if (getType() == View._TYPE_INSERT && isParent())  {
            // add the children to the cached child view..
            childView.add(dco.getChildren());
            // and load them
            childView.setParentID(dco.getID(), false);
        }

        if (select)
            setSelected();
    }

    public void cancelCurrentTask() {
        if (task != null && task.isAlive() && task instanceof FillerTask)
            task.stopRunning();
    }
    
    public void add(Collection<DcObject> c) {
        add(c.toArray(new DcObject[0]));
    }
    
    public void add(DcObject[] objects) {
        cancelCurrentTask();
        task = new FillerTask(groupingPane != null ? groupingPane.getActiveTree() : null, this, objects);
        task.start();
    }    
    
    protected void setSelected() {
        int idx = getItemCount() - 1;
        if (idx >= 0) {
            vc.setSelected(idx);
            afterSelect(idx);
        }
    }    
    
    @Override
    public void setVisible(boolean b) {
    	if (b && groupingPane != null && vdGroupingPane != null) {
			vdGroupingPane.setLeftComponent(groupingPane);
			if (vc.getItemCount() == 0)
			    groupingPane.updateView();
    	}
    	super.setVisible(b);
    }
    
    public void checkForChanges(boolean b) {
        checkForChanges = b;
    }
    
    public void clear(boolean saveChanges) {
        if (checkForChanges && saveChanges) {
            boolean saved = isChangesSaved();
            if (!saved && getCurrentTask() != null) {
                QuestionBox qb = new QuestionBox(DcResources.getText("msgNotSaved"));
                if (qb.isAffirmative())
                    save();
                else
                    vc.undoChanges();

                try {
                    getCurrentTask().join(5000);
                } catch (Exception e) {
                    logger.error("Error while trying to join the new saving thread with the current one", e);
                }
            }
        } else {
            vc.undoChanges();
        }

        vc.ignoreEdit(true);
        vc.clear();
        setStatus(DcResources.getText("msgViewCleared"));
        updateProgressBar(0);      
        
        if (isParent()) {
            childView.clear();
            childView.setParentID(null, false);
        }
        
        if (quickView != null)
            quickView.clear();
        
        vc.ignoreEdit(false);
    }

    public void cancelTask() {
        if (task != null) task.cancel();
        setActionsAllowed(true);
        updateProgressBar(0);
    }
    
    public void groupBy() {
        if (groupingPane != null)
            groupingPane.groupBy();        
    }

    public void delete() {
        if (getType() == _TYPE_INSERT) {
            remove(vc.getSelectedIndices());
        } else {
            if (isTaskRunning())
                return;
                
            Collection<? extends DcObject> objects = vc.getSelectedItems();
            if (objects.size() > 0) {
                task = new DeleteTask(this, objects);
                task.start();
            } else {
                new MessageBox(DcResources.getText("msgSelectItemToDel"), MessageBox._WARNING);    
            }
        }
    }

    public boolean isChangesSaved() {
        if (!checkForChanges)
            return true;
        
        cancelEdit();
        boolean saved = vc.isChangesSaved();
        if (childView != null)
            saved &= childView.isChangesSaved();
        
        return saved;
    }

    public void open() {
        DcObject dco = getSelectedItem();
        
        if (dco != null) {
            // reload the item when in an abstract view (or when we are opening an item from 
            // the container's child view. 
            if (    DcModules.getCurrent().isAbstract() || 
                   (DcModules.getCurrent().getIndex() == DcModules._CONTAINER && 
                    dco.getModule().getIndex() != DcModules._CONTAINER))
                dco.reload();
            
            ItemForm form = new ItemForm(false, getType() == View._TYPE_SEARCH, 
                                         dco, getType() != View._TYPE_SEARCH);
            form.setVisible(true);
        } else {
            new MessageBox(DcResources.getText("msgSelectRowToOpen"), MessageBox._WARNING);
        }
    }
    
    public void reload(String id) {
        vc.updateUI(id);

        if (quickView != null)
            quickView.createImageTabs();
        
        repaint();
    }  
    
    public void repaintQuickViewImage() {
        if (quickView != null)
            quickView.reloadImage();
    }

    public void save() {
        if (!isTaskRunning()) {
        	
        	Collection<DcObject> objects = new ArrayList<DcObject>();
        	if (getType() == _TYPE_SEARCH) {
        		int[] indices = vc.getChangedIndices();
        		for (int index : indices) 
        			objects.add(getItemAt(index));
                
                if (getChildView() != null) { 
                    for (DcObject child : getChildView().getChangedItems()) 
                        objects.add(child);
                }
        	} else {
        		objects.addAll(getItems());
        	}
        	
        	if (objects.size() > 0) {
        		task = new SaveTask(this, objects);
        		task.startRunning();
        		task.start();   
            } else {
                new MessageBox(DcResources.getText("msgNoChangesToSave"), MessageBox._WARNING);    
            }
        }        
    }

    public void saveSelected() {
        if (!isTaskRunning()) {
            Collection<DcObject> objects = new ArrayList<DcObject>();
            
            if (getType() == _TYPE_SEARCH) {
                objects.addAll(vc.getSelectedItems());
                if (getChildView() != null)
                    objects.addAll(getChildView().getChangedItems());
            } else {
                objects.addAll( vc.getSelectedItems());
            } 
            
            task = new SaveTask(this, objects);
            task.startRunning();
            task.start();
        }        
    }

    @Override
    public void clear() {
        clear(true);
    }
    
    public void saveSettings() {
        vc.saveSettings();
    }
    
    public void applySettings() {
        setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontNormal));
        
        if (quickView != null) {
            quickView.setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontBold));
            quickView.setVisible(DcSettings.getBoolean(DcRepository.Settings.stShowQuickView));
        }
        
        if (groupingPane != null) {
            boolean treeVisibible = groupingPane.isVisible();
            boolean treeVisibibleSett = DcSettings.getBoolean(DcRepository.Settings.stShowGroupingPanel);
            
            groupingPane.setVisible(treeVisibibleSett);
            if (!treeVisibible && treeVisibibleSett)
                groupingPane.groupBy();
            else if (treeVisibible && !treeVisibibleSett)
                groupingPane.reset();
            
            groupingPane.setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontBold));
        }
        
        vc.applySettings();
        panelStatus.setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontBold));
        
        if (getModule().getIndex() == DcModules._CONTAINER) {
            if (groupingPane != null)
                groupingPane.updateView();
        }
    }

    public int getItemCount() {
        return vc.getItemCount();
    }

    public boolean isActionsAllowed() {
        return actionsAllowed;
    }
    
    public void setActionsAllowed(boolean b) {
        this.actionsAllowed = b;
        
        Cursor cursor = b ? new Cursor(Cursor.DEFAULT_CURSOR) : new Cursor(Cursor.WAIT_CURSOR);
        vc.setCursor(cursor);
        
        if (b) {
            for (MouseListener ml : vc.getMouseListeners()) {
                if (ml == vml)
                    vc.removeMouseListener(ml);
            }
            
            vc.addMouseListener(vml);
        } else { 
            vc.removeMouseListener(vml);
        }

        actionPanel.setEnabled(b);
    }
    
    public Collection<DcObject> getItems() {
        Collection<DcObject> items = new ArrayList<DcObject>();
        for (int i = 0; i < getItemCount(); i++)
            items.add(getItemAt(i));
        
        return items;
    }    
    
    public DcObject getItemAt(int idx) {
        vc.cancelEdit();
        DcObject dco = vc.getItemAt(idx);
         
        if (dco != null) {
            if (isParent() && getType() == View._TYPE_INSERT) {
                dco.removeChildren();
                dco.setChildren(((CachedChildView) childView).getChildren(dco.getID()));
            }
        }        
        
        return dco;
    }

    public DcObject getItem(String ID) {
        return vc.getItem(ID);
    }
    
    public void setSelected(int index) {
        vc.setSelected(index);
        afterSelect(index);
    }
    
    public Collection<? extends DcObject> getSelectedItems() {
        return vc.getSelectedItems();
    }

    public DcObject getSelectedItem() {
        if (vc.getSelectedIndex() > -1)
            return getItemAt(vc.getSelectedIndex());
        else
            return null;
    }

    public void createReport() {
        List<DcObject> objects = new ArrayList<DcObject>();
        if (groupingPane != null && groupingPane.isActive())
            objects.addAll(groupingPane.getItems());
        else
            objects.addAll(vc.getItems());
        
        if (objects.size() > 0) {
            ReportingDialog dialog = new ReportingDialog(objects);
            dialog.setVisible(true);
        } else {
            new MessageBox(DcResources.getText("msgReportNoItems"), MessageBox._WARNING);
        }
    }

    public void remove(String[] IDs) {
        // remove it from the view
        if (vc.remove(IDs)) {
            // at this point actions should be enabled to allow the quick view to be populated
            setActionsAllowed(true);                
            setDefaultSelection();
            
            if (getItemCount() == 0) {
                if (quickView != null) quickView.clear();
                if (isParent()) childView.clear(false);
            }
        }
    }

    public void remove(int[] indices) {
        String[] ids = new String[indices.length];
        int i = 0;
        for (int index : indices) {
            DcObject dco = getItemAt(index);
            ids[i++] = dco.getID();
        }
        remove(ids);
    }

    public void showQuickView(boolean b) {
        quickView.setVisible(b);
    }

    public int[] getSelectedRows() {
        return vc.getSelectedIndices();
    }
    
    public void updateItemAt(int index, DcObject dco, boolean overwrite, boolean allowDeletes, boolean mark) {
        DcObject o = getItemAt(index);
        if (o != null) vc.updateItem(o.getID(), dco, overwrite, allowDeletes, mark);
    }

    public void updateItem(String ID, DcObject dco, boolean overwrite, boolean allowDeletes, boolean mark) {
        vc.updateItem(ID, dco, overwrite, allowDeletes, mark);
        DcObject item = getItem(ID);
        
        if (quickView != null && index > -1)
            quickView.setObject(item, true);
    }
    
    protected Collection<DcObject> getChangedItems() {
        Collection<DcObject> objects = new ArrayList<DcObject>();
        for (int idx : vc.getChangedIndices()) 
            objects.add(getItemAt(idx));
        
        return objects;
    }

    public void removeFromCache(String id) {
        DcObject dco = vc.getItem(id);
        if (dco != null) dco.markAsUnchanged();
    }

    public DcObject getDcObject(String ID) {
        return vc.getItem(ID);
    }
    
    public void cancelEdit() {
        vc.cancelEdit();
    }
    
    public void loadChildren() {
        if (isParent())
            childView.loadChildren();
        else if (isChild() && parentID != null && parentID.length() > 0) {
            add(DataManager.getObject(getModule().getParent().getIndex(), parentID).getChildren());
        }
    }
    
    /**
     * Note that the items only have to be shown after a select. 
     */
    public void setParentID(String id, boolean show) {
        this.parentID = id;
    }
    
    public String getParentID() {
        return parentID;
    }
    
    public void afterSelect(int idx) {
        if (isParent() && actionsAllowed) {
            DcObject dco = getSelectedItem();
            childView.setParentID(dco != null ? dco.getID() : null, true);
            if (!(childView instanceof CachedChildView))
                loadChildren();
        }

        if (getItemCount() > 0 && !isChild() && quickView != null && quickView.isVisible() && isActionsAllowed()) {
            try {
                DcObject dco = getItemAt(idx);
                quickView.setObject(dco, false);
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }
    
    protected Collection<Component> getAdditionalActions() {
        ArrayList<Component> components = new ArrayList<Component>();
        if (isParent()) {
            try {
                JButton btAddChild = ComponentFactory.getButton(DcResources.getText("lblAddChild", getModule().getChild().getObjectName()));
                btAddChild.addActionListener(Plugins.getInstance().get("AddChild", null, null, getIndex(), getModule().getIndex()));
                btAddChild.setMnemonic('T');
                components.add(btAddChild);
            } catch (InvalidPluginException ipe) {
                logger.error(ipe, ipe);
            }
        }
        return components;
    }    

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() && actionsAllowed)
            afterSelect(vc.getSelectedIndex());
    }
    
    private void addChildView() {
        if (childView != null) {
            childView.setVisible(true);
            panelResult.add(spChildView,  Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                           ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                            new Insets(5, 5, 5, 5), 0, 0));
            revalidate();
            repaint();
        }
    }
    
//    private void removeChildView() {
//        if (childView != null) {
//            childView.setVisible(false);
//            panelResult.remove(spChildView);
//            revalidate();
//            repaint();
//        }
//    }
    
    private void build() {
        //**********************************************************
        //Search result panel
        //**********************************************************
        panelResult.setLayout(Layout.getGBL());

        JScrollPane scroller1 = new JScrollPane((Component) vc);
        scroller1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelResult.add(scroller1,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                       ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                        new Insets(5, 5, 5, 5), 0, 0));
        
        if (isParent()) {
            spChildView = new JScrollPane((Component) childView.getViewComponent());
            spChildView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            addChildView();
        }
        
        // only the search view uses view dividers
        if (getType() == _TYPE_SEARCH) {
            vdGroupingPane = new DcViewDivider(groupingPane, panelResult, DcRepository.Settings.stTreeDividerLocation);
        
            quickView = getModule().getQuickView();
            vdQuickPane = new DcViewDivider(vdGroupingPane, quickView, DcRepository.Settings.stQuickViewDividerLocation);

            quickView.setVisible(DcSettings.getBoolean(DcRepository.Settings.stShowQuickView));
        }
        
        //**********************************************************
        //Main panel
        //**********************************************************
        setLayout(Layout.getGBL());
        
        Component c = vdQuickPane != null ? vdQuickPane : panelResult;
        add(    c, Layout.getGBC( 0, 1, 3, 1, 100.0, 100.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0)); 
        add(    actionPanel,    Layout.getGBC( 0, 2, 3, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
                new Insets(0, 5, 0, 5), 0, 0));
        add(    panelStatus,    Layout.getGBC( 0, 3, 3, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
                new Insets(0, 5, 0, 5), 0, 0));

        ToolTipManager.sharedInstance().registerComponent((JComponent) vc);
    }
}
