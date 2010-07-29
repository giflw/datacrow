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
import net.datacrow.console.views.tasks.SaveTask;
import net.datacrow.console.windows.UpdateAllDialog;
import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilters;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.plugin.InvalidPluginException;
import net.datacrow.core.plugin.Plugins;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DataTask;
import net.datacrow.util.DcSwingUtilities;

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
    
    private JScrollPane spChildView;
    private View childView;
    private View parentView;

    private boolean actionsAllowed = true;
    
    private Long parentID;
    
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

        if (type == _TYPE_INSERT)
            actionPanel = new ViewActionPanel(this);
        
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
    
    public void refreshQuickView() {
        if (quickView != null)
            quickView.refresh();
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
            DcSwingUtilities.displayWarningMessage(DcResources.getText("msgJobRunning"));
        
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
        DataFilter df = DataFilters.getCurrent(vc.getModule().getIndex());
        add(DataManager.getKeys(df));
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
    
    public void add(Long key) {
        add(key, true);
    }    
    
    public void add(final Long key, final boolean select) {
        vc.add(key);
        
        if (select)
            setSelected();
    }
    
    public void add(final DcObject dco, final boolean select) {
        dco.markAsUnchanged();
        
        if (getType() == View._TYPE_INSERT)
            dco.setIDs();
        
        vc.add(dco);

        if (select)
            setSelected();
    }    

    public void cancelCurrentTask() {
        if (task != null && task.isAlive())
            task.stopRunning();
    }
    
    /**
     * Adds the items to the view. 
     * Note: children for the insert view are added by the view component.
     * @see DcTable#add(DcObject).
     * @param items
     */
    public void add(Collection<Long> keys) {
        setActionsAllowed(false);

        vc.deselect();
        vc.add(keys);
        
        setActionsAllowed(true);
        
        revalidate();
        afterUpdate();
        setSelected();
    }      
    
    /**
     * Adds the items to the view. 
     * Note: children for the insert view are added by the view component.
     * @see DcTable#add(DcObject).
     * @param items
     */
    public void add(List<DcObject> items) {
        setActionsAllowed(false);

        for (DcObject item : items) {
            if (getType() == View._TYPE_INSERT)
                item.setIDs();
        }
        
        vc.deselect();
        vc.add(items);
        
        setActionsAllowed(true);
        
        revalidate();
        afterUpdate();
        setDefaultSelection();
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
    	}
    	super.setVisible(b);
    }
    
    public void checkForChanges(boolean b) {
        checkForChanges = b;
    }
    
    public void clear(boolean saveChanges) {
        if (checkForChanges && saveChanges) {
            boolean saved = isChangesSaved();
            if (!saved) {
                if (DcSwingUtilities.displayQuestion("msgNotSaved"))
                    save(false);
                else
                    vc.undoChanges();
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
                DcSwingUtilities.displayWarningMessage(DcResources.getText("msgSelectItemToDel"));
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
            
            // create a fresh instance
            ItemForm form = new ItemForm(false, getType() == View._TYPE_SEARCH, 
                                         DataManager.getItem(dco.getModule().getIndex(), dco.getID()), 
                                         getType() != View._TYPE_SEARCH);
            form.setVisible(true);
        } else {
            DcSwingUtilities.displayWarningMessage(DcResources.getText("msgSelectRowToOpen"));
        }
    }
    
    public void reload(Long id) {
        vc.updateUI(id);

        if (quickView != null)
            quickView.createImageTabs();
        
        repaint();
    }  
    
    public void repaintQuickViewImage() {
        if (quickView != null)
            quickView.reloadImage();
    }

    public void save(boolean threaded) {
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
        		
        		if (threaded)
        		    task.start();
        		else 
        		    task.run();
            } else {
                DcSwingUtilities.displayWarningMessage(DcResources.getText("msgNoChangesToSave"));
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
                groupingPane.load();
            
            groupingPane.setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontBold));
        }
        
        if (type == _TYPE_SEARCH) {
            add(getItems());
        }
        
        vc.applySettings();
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

        if (actionPanel != null)
            actionPanel.setEnabled(b);
    }
    
    public List<DcObject> getItems() {
        List<DcObject> items = new ArrayList<DcObject>();
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
                
                // RJW: Do not set IDs, creates mayhem for the cached child view.
                // dco.setIDs();
            }
        }        
        
        return dco;
    }

    public DcObject getItem(Long ID) {
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
//        List<DcObject> objects = new ArrayList<DcObject>();
//        if (groupingPane != null && groupingPane.isActive())
//            objects.addAll(groupingPane.getValues());
//        else
//            objects.addAll(vc.getItems());
//        
//        if (objects.size() > 0) {
//            ReportingDialog dialog = new ReportingDialog(objects);
//            dialog.setVisible(true);
//        } else {
//            DcSwingUtilities.displayWarningMessage(DcResources.getText("msgReportNoItems"));
//        }
    }

    public void remove(Long[] IDs) {
        // remove it from the view
        if (vc.remove(IDs)) {
            // at this point actions should be enabled to allow the quick view to be populated
            setActionsAllowed(true);                
            setDefaultSelection();
            
            if (isParent() && childView instanceof CachedChildView) {
                for (Long ID : IDs)
                    ((CachedChildView) childView).removeChildren(ID);
            }
            
            if (getItemCount() == 0) {
                if (quickView != null) quickView.clear();
                if (isParent()) childView.clear(false);
            }
        }
    }

    public void remove(int[] indices) {
        Long[] IDs = new Long[indices.length];
        int i = 0;
        for (int index : indices) {
            DcObject dco = getItemAt(index);
            IDs[i++] = dco.getID();
        }
        remove(IDs);
    }

    public void showQuickView(boolean b) {
        quickView.setVisible(b);
    }

    public int[] getSelectedRows() {
        return vc.getSelectedIndices();
    }
    
    public void updateItemAt(int index, DcObject dco) {
        DcObject o = getItemAt(index);

        if (o != null)
            updateItem(o.getID(), dco);
        else 
            logger.warn("No element found at index " + index);
    }

    public void updateItem(Long ID, DcObject dco) {
        vc.updateItem(ID, dco);
        
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

    public void removeFromCache(Long id) {
        DcObject dco = vc.getItem(id);
        if (dco != null) dco.markAsUnchanged();
    }

    public DcObject getDcObject(Long ID) {
        return vc.getItem(ID);
    }
    
    public void cancelEdit() {
        vc.cancelEdit();
    }
    
    public void loadChildren() {
        if (isParent())
            childView.loadChildren();
        else if (isChild() && parentID != null) {
            add(DataManager.getItem(getModule().getParent().getIndex(), parentID).getChildren());
        }
    }
    
    /**
     * Note that the items only have to be shown after a select. 
     */
    public void setParentID(Long ID, boolean show) {
        this.parentID = ID;
    }
    
    public Long getParentID() {
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
    
    private void build() {
        //**********************************************************
        //Search result panel
        //**********************************************************
        panelResult.setLayout(Layout.getGBL());

        ViewScrollPane scroller1 = new ViewScrollPane(this);
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
        if (getType() == _TYPE_SEARCH && !getModule().isChildModule()) {
            vdGroupingPane = new DcViewDivider(groupingPane, panelResult, DcRepository.Settings.stTreeDividerLocation);
        
            quickView = getModule().getQuickView();
            vdQuickPane = new DcViewDivider(vdGroupingPane, quickView, DcRepository.Settings.stQuickViewDividerLocation);

            quickView.setVisible(DcSettings.getBoolean(DcRepository.Settings.stShowQuickView));
            quickView.setBorder(null);
        }
        
        //**********************************************************
        //Main panel
        //**********************************************************
        setLayout(Layout.getGBL());
        
        Component c = vdQuickPane != null ? vdQuickPane : panelResult;
        add(    c, Layout.getGBC( 0, 1, 3, 1, 100.0, 100.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0)); 
        
        if (actionPanel != null)
            add(    actionPanel,    Layout.getGBC( 0, 2, 3, 1, 1.0, 1.0
                    ,GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 5, 0, 5), 0, 0));

        ToolTipManager.sharedInstance().registerComponent((JComponent) vc);
    }
}
