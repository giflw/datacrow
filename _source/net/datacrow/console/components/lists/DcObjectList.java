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

package net.datacrow.console.components.lists;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JList;
import javax.swing.JViewport;
import javax.swing.event.ListSelectionListener;

import net.datacrow.console.components.lists.elements.DcAudioCDListHwElement;
import net.datacrow.console.components.lists.elements.DcAudioTrackListElement;
import net.datacrow.console.components.lists.elements.DcBookListHwElement;
import net.datacrow.console.components.lists.elements.DcCardObjectListElement;
import net.datacrow.console.components.lists.elements.DcMovieListHwElement;
import net.datacrow.console.components.lists.elements.DcMusicAlbumListHwElement;
import net.datacrow.console.components.lists.elements.DcMusicTrackListElement;
import net.datacrow.console.components.lists.elements.DcObjectListElement;
import net.datacrow.console.components.lists.elements.DcPropertyListElement;
import net.datacrow.console.components.lists.elements.DcShortObjectListElement;
import net.datacrow.console.components.lists.elements.DcSoftwareListHwElement;
import net.datacrow.console.components.lists.elements.DcTemplateListElement;
import net.datacrow.console.components.renderers.DcObjectListRenderer;
import net.datacrow.console.views.IViewComponent;
import net.datacrow.console.views.View;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.util.ViewUpdater;

import org.apache.log4j.Logger;

public class DcObjectList extends DcList implements IViewComponent {
    
    private static Logger logger = Logger.getLogger(DcObjectList.class.getName());

    private int style = _CARDS;

    private View view;
    private DcModule module;
    
    public static final int _ELABORATE = 0;
    public static final int _CARDS = 1;
    public static final int _LISTING = 2;
    
    private DcObjectListRenderer renderer = new DcObjectListRenderer();
    
    private ViewUpdater vu;
    
    private boolean ignorePaintRequests = false;

    public DcObjectList(int style, boolean wrap, boolean evenOddColors) {
        this(null, style, wrap, evenOddColors);
    }
    
    public DcObjectList(DcModule module, 
                        int style, 
                        boolean wrap, 
                        boolean evenOddColors) {
        
        super(new DcListModel());
        
        this.module = module;
        this.style = style;
        
        addComponentListener(new ListComponentListener());
        setCellRenderer(renderer);
        renderer.setEventOddColors(evenOddColors);
        
        if (wrap)
            setLayoutOrientation(JList.HORIZONTAL_WRAP);
        else 
            setLayoutOrientation(JList.VERTICAL_WRAP);
    }    

    public void setIgnorePaintRequests(boolean b) {
        ignorePaintRequests = b;
    }

    public boolean isIgnoringPaintRequests() {
        return ignorePaintRequests; 
    }

    public boolean isVisibleIndex(int index) {
        return index >= getFirstVisibleIndex() && index <= getLastVisibleIndex();
    }
    
    public void activate() {}

    public void paintRegionChanged() {
        if (vu != null) vu.cancel();
        vu = new ViewUpdater(this);
        vu.start();
    }

    public void clear(int idx) {
        getElement(idx).clear();
    }

    public int getViewportBufferSize() {
        return 10;
    }

    public void saveSettings() {
    }

    public int getOptimalItemAdditionBatchSize() {
        return 1;
    }
    
    public void ignoreEdit(boolean b) {}
    
    public void undoChanges() {}

    public boolean isChangesSaved() {
        return true;
    }    
    
    public void setView(View view) {
        this.view = view;
    }
    
    public boolean allowsHorizontalTraversel() {
        return true;
    }
    
    public boolean allowsVerticalTraversel() {
        return true;
    }    
    
    public void cancelEdit() {}
    
    public DcModule getModule() {
        return module;
    }
    
    public View getView() {
        return view;
    }    
    
    public DcObject getItemAt(int idx) {
        return getElement(idx).getDcObject();
    }

    public int getItemCount() {
        return getDcModel().getSize();
    }
    
    public Collection<DcObject> getItems() {
        Collection<DcObject> objects = new ArrayList<DcObject>();
        DcObjectListElement element;
        for (int i = 0 ; i < getDcModel().getSize(); i++) {
            element = (DcObjectListElement) getDcModel().getElementAt(i);
            if (element.getDcObject() != null) 
                objects.add(element.getDcObject());
        }
        return objects;
    }

    public Collection<DcObject> getSelectedItems() {
        int[] indices = getSelectedIndices();
        Collection<DcObject> objects = new ArrayList<DcObject>();

        DcObjectListElement element;
        DcObject dco;
        for (int i = 0; i < indices.length; i++) {
            element = (DcObjectListElement) getDcModel().getElementAt(indices[i]);
            dco = element.getDcObject();
            objects.add(dco);
        }

        return objects;
    }    
    
    public DcObject getItem(String ID) {
        DcObjectListElement element = getElement(ID);
        return element != null ? element.getDcObject() : null;
    }    

    private DcObjectListElement getElement(String ID) {
        DcObjectListElement element;
        for (int i = 0 ; i < getDcModel().getSize(); i++) {
            element = (DcObjectListElement) getDcModel().getElementAt(i);
            if (element.getDcObject() != null && element.getDcObject().getID().equals(ID)) 
                return element;
        }
        return null;
    }      

    private DcObjectListElement getElement(int idx) {
        return (DcObjectListElement) getDcModel().getElementAt(idx);
    }      
    
    public void afterUpdate() {
        if (getModule().getType() == DcModule._TYPE_PROPERTY_MODULE) return;
            
        if (getDcModel().size() > 0) {
            DcObjectListElement elem = (DcObjectListElement) getDcModel().getElementAt(0);
            Dimension elemSize = elem.getPreferredSize();
            
            setFixedCellHeight(elemSize.height);
            setFixedCellWidth(elemSize.width);
            
            int width = ((JViewport) getParent()).getWidth();
            setColumnsPerRow((int) Math.floor(width / elemSize.width));
        }
    }

    public void deselect() {
        try {
            super.clearSelection();
        } catch (Exception e) {}
    }

    public void fireIntervalAdded(int from, int to) {
        getDcModel().fireIntervalAdded(getModel(), from, to);
    }

    public void setSelected(int index) {
        super.setSelectedIndex(index);
        ensureIndexIsVisible(index);
    }

    public void applySettings() {}
    
    public void update(String ID) {
        DcObjectListElement element = getElement(ID);
        if (element != null) {
            element.update();
            revalidate();
            repaint();
        }
    }       

    public void update(String ID, DcObject dco) {
        updateElement(getElement(ID), dco);
    }    

    private void updateElement(DcObjectListElement element, DcObject dco) {
        if (element != null) {
            element.update(dco);
            setSelectedValue(element, true);
        } else {
            logger.debug("Could not update " + dco + ", element could not be found in the view");   
        }
    }
    
    public void setSelected(DcObject dco, int field) {
        int size = getDcModel().getSize();
        for (int row = 0 ; row < size; row++) {
            DcObjectListElement element = (DcObjectListElement) getDcModel().getElementAt(row);
            if (element.getDcObject() != null) {
                if (dco.getDisplayString(field).equals(element.getDcObject().getDisplayString(field))) {
                    setSelectedIndex(row);
                    break;
                }
            }
        }
    }
    
    public void setSelected(Collection<? extends DcObject> dcos) {
        int[] indices = new int[dcos.size()];
        int counter = 0;
        DcObjectListElement element;
        for (DcObject o : dcos) {
            for (int row = 0 ; row < getDcModel().getSize(); row++) {
                element = (DcObjectListElement) getDcModel().getElementAt(row);
                if (element.getDcObject() != null) {
                    if (element.getDcObject().equals(o)) {
                        indices[counter] = row;
                        break;
                    }
                }
            }
        }
        setSelectedIndices(indices);
    }
    
    public int[] getChangedIndices() {
        Collection<Integer> rows = new ArrayList<Integer>();
        DcObjectListElement element;
        for (int i = 0 ; i < getDcModel().getSize(); i++) {
            element = (DcObjectListElement) getDcModel().getElementAt(i);
            if (element.getDcObject() != null && element.getDcObject().isChanged()) 
                rows.add(i);
        }
        
        int[] indices = new int[rows.size()];
        int counter = 0;
        for (Integer idx : rows)
            indices[counter++] = idx.intValue();

        return indices;        
    }
    
    public boolean remove(String[] keys) {
        boolean removed = false;
        DcObjectListElement element;
        for (String key : keys) {
            element = getElement(key);
            if (element != null) {
            	getDcModel().removeElement(element);
            	element.destroy();
                removed = true;
            }
        }
        return removed;
    }

    public DcObject getSelectedItem() {
        DcObjectListElement element = (DcObjectListElement) getSelectedValue();
        
        DcObject dco = null;
        if (element != null) 
            dco = element.getDcObject(); 
        
        return dco;
    }
    
    public void add(String key) {
        DcObjectListElement element = getDisplayElement(getModule().getIndex());
        element.setKey(key);
        getDcModel().addElement(element);
    }

    public void add(Collection<String> keys) {
        clear();
        
        DcListModel model = new DcListModel();
        
        renderer.stop();
        
        DcObjectListElement element;
        for (String key : keys) {
            element = getDisplayElement(module.getIndex());
            element.setKey(key);
            model.addElement(element);
        }
        
        renderer.start();
        
        setModel(model);
        revalidate();
    }
    
    public void add(DcObject dco) {
        if (dco.getID() == null || dco.getID().equals("")) 
            dco.setIDs();        
        
        if (getView() != null && getView().getType() == View._TYPE_SEARCH)
            dco.markAsUnchanged();
        
        DcObjectListElement element = getDisplayElement(dco.getModule().getIndex());
        element.setDcObject(dco);
        getDcModel().addElement(element);
        ensureIndexIsVisible(getModel().getSize());
    }
    
    public void add(List<? extends DcObject> objects) {
        clear();
        
        DcListModel model = new DcListModel();
        DcObjectListElement element;
        for (DcObject dco : objects) {
            element = getDisplayElement(dco.getModule().getIndex());
            element.setDcObject(dco);
            model.addElement(element);
        }
        
        setModel(model);
        revalidate();
    }
    
    public DcObjectListElement getDisplayElement(int module) {
        
        DcObjectListElement element = null;
        
        if (style == _ELABORATE) {
            if (module == DcModules._AUDIOCD) 
                element = new DcAudioCDListHwElement(module);
            else if (module == DcModules._MUSICALBUM) 
                element = new DcMusicAlbumListHwElement(module);
            else if (module == DcModules._SOFTWARE)
                element = new DcSoftwareListHwElement(module);
            else if (module == DcModules._MOVIE)
                element = new DcMovieListHwElement(module);
            else if (module == DcModules._BOOK)
                element = new DcBookListHwElement(module);
            else 
                element = new DcCardObjectListElement(module);
        } else if (style == _CARDS) {
            if (module == DcModules._AUDIOTRACK)
                element = new DcAudioTrackListElement(module);
            else if (module == DcModule._TYPE_TEMPLATE_MODULE)
                element = new DcTemplateListElement(module);
            else if (module == DcModules._MUSICTRACK)
                element = new DcMusicTrackListElement(module);
            else if (module == DcModule._TYPE_PROPERTY_MODULE)
                element = new DcPropertyListElement(module);
            else if (DcModules.get(module).isChildModule())
                element = new DcShortObjectListElement(module);
            else 
                element = new DcCardObjectListElement(module);       
        } else if (style == _LISTING) {
            if (DcModules.get(module).getType() == DcModule._TYPE_PROPERTY_MODULE)
                element = new DcPropertyListElement(module);
            else if (DcModules.get(module).getType() == DcModule._TYPE_TEMPLATE_MODULE)
            	element = new DcTemplateListElement(module);
            else if (module == DcModules._MUSICTRACK)
                element = new DcMusicTrackListElement(module);
            else if (module == DcModules._AUDIOTRACK)
                element = new DcAudioTrackListElement(module);
            else
                element = new DcShortObjectListElement(module);
        }
        return element;
    }

    public void addSelectionListener(ListSelectionListener lsl) {
        removeSelectionListener(lsl);
        super.addListSelectionListener(lsl);
    }
    
    public void removeSelectionListener(ListSelectionListener lsl) {
        removeListSelectionListener(lsl);
    }
}
