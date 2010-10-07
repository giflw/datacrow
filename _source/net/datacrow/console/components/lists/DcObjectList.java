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

    @Override
    public void setIgnorePaintRequests(boolean b) {
        ignorePaintRequests = b;
    }

    @Override
    public boolean isIgnoringPaintRequests() {
        return ignorePaintRequests; 
    }

    public boolean isVisibleIndex(int index) {
        return index >= getFirstVisibleIndex() && index <= getLastVisibleIndex();
    }
    
    @Override
    public String getItemKey(int idx) {
        return getElement(idx).getKey();
    }

    @Override
    public int getModule(int idx) {
        return getItemAt(idx).getModule().getIndex();
    }

    @Override
    public void activate() {}

    @Override
    public void paintRegionChanged() {
        if (vu != null) vu.cancel();
        vu = new ViewUpdater(this);
        vu.start();
    }

    @Override
    public void clear(int idx) {
        getElement(idx).clear();
    }

    @Override
    public int getViewportBufferSize() {
        return 10;
    }

    @Override
    public void saveSettings() {
    }

    public int getOptimalItemAdditionBatchSize() {
        return 1;
    }
    
    @Override
    public void ignoreEdit(boolean b) {}
    
    @Override
    public void undoChanges() {}

    @Override
    public boolean isChangesSaved() {
        return true;
    }    
    
    @Override
    public void setView(View view) {
        this.view = view;
    }
    
    @Override
    public boolean allowsHorizontalTraversel() {
        return true;
    }
    
    @Override
    public boolean allowsVerticalTraversel() {
        return true;
    }    
    
    @Override
    public void cancelEdit() {}
    
    @Override
    public DcModule getModule() {
        return module;
    }
    
    @Override
    public View getView() {
        return view;
    }    
    
    @Override
    public DcObject getItemAt(int idx) {
        return getElement(idx).getDcObject();
    }

    @Override
    public int getItemCount() {
        return getDcModel().getSize();
    }
    
    @Override
    public List<DcObject> getItems() {
    	List<DcObject> objects = new ArrayList<DcObject>();
        DcObjectListElement element;
        for (int i = 0 ; i < getDcModel().getSize(); i++) {
            element = (DcObjectListElement) getDcModel().getElementAt(i);
            if (element.getDcObject() != null) 
                objects.add(element.getDcObject());
        }
        return objects;
    }

	@Override
    public List<String> getItemKeys() {
		List<String> keys = new ArrayList<String>();
        DcObjectListElement element;
        for (int i = 0 ; i < getDcModel().getSize(); i++) {
            element = (DcObjectListElement) getDcModel().getElementAt(i);
            keys.add(element.getKey());
        }
        return keys;
	}

    @Override
    public List<DcObject> getSelectedItems() {
        int[] indices = getSelectedIndices();
        List<DcObject> objects = new ArrayList<DcObject>();

        DcObjectListElement element;
        DcObject dco;
        for (int i = 0; i < indices.length; i++) {
            element = (DcObjectListElement) getDcModel().getElementAt(indices[i]);
            dco = element.getDcObject();
            objects.add(dco);
        }

        return objects;
    } 
    
    @Override
    public List<String> getSelectedItemKeys() {
        int[] indices = getSelectedIndices();
        List<String> items = new ArrayList<String>();

        DcObjectListElement element;
        for (int i = 0; i < indices.length; i++) {
            element = (DcObjectListElement) getDcModel().getElementAt(indices[i]);
            items.add(element.getKey());
        }

        return items;
    }  
    
    @Override
    public DcObject getItem(String ID) {
        DcObjectListElement element = getElement(ID);
        return element != null ? element.getDcObject() : null;
    }    

    private DcObjectListElement getElement(String ID) {
        DcObjectListElement element;
        for (int i = 0 ; i < getDcModel().getSize(); i++) {
            element = (DcObjectListElement) getDcModel().getElementAt(i);
            if (element.getKey() != null && element.getKey().equals(ID)) 
                return element;
        }
        return null;
    }      

    private DcObjectListElement getElement(int idx) {
        return (DcObjectListElement) getDcModel().getElementAt(idx);
    }      
    
    @Override
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

    @Override
    public void deselect() {
        try {
            super.clearSelection();
        } catch (Exception e) {}
    }

    public void fireIntervalAdded(int from, int to) {
        getDcModel().fireIntervalAdded(getModel(), from, to);
    }

    @Override
    public void setSelected(int index) {
        super.setSelectedIndex(index);
        ensureIndexIsVisible(index);
    }

    @Override
    public void applySettings() {}
    
    @Override
    public void update(String ID) {
        DcObjectListElement element = getElement(ID);
        if (element != null) {
            element.update();
        }
    }       

    @Override
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
    
//    public void setSelected(DcObject dco, int field) {
//        int size = getDcModel().getSize();
//        for (int row = 0 ; row < size; row++) {
//            DcObjectListElement element = (DcObjectListElement) getDcModel().getElementAt(row);
//            if (element.getDcObject() != null) {
//                if (dco.getDisplayString(field).equals(element.getDcObject().getDisplayString(field))) {
//                    setSelectedIndex(row);
//                    break;
//                }
//            }
//        }
//    }
    
//    public void setSelected(Collection<? extends DcObject> dcos) {
//        int[] indices = new int[dcos.size()];
//        int counter = 0;
//        DcObjectListElement element;
//        for (DcObject o : dcos) {
//            for (int row = 0 ; row < getDcModel().getSize(); row++) {
//                element = (DcObjectListElement) getDcModel().getElementAt(row);
//                if (element.getDcObject() != null) {
//                    if (element.getDcObject().equals(o)) {
//                        indices[counter] = row;
//                        break;
//                    }
//                }
//            }
//        }
//        setSelectedIndices(indices);
//    }
    
    @Override
    public int[] getChangedIndices() {
        return new int[0];
    }
    
    @Override
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

    @Override
    public DcObject getSelectedItem() {
        DcObjectListElement element = (DcObjectListElement) getSelectedValue();
        
        DcObject dco = null;
        if (element != null) 
            dco = element.getDcObject(); 
        
        return dco;
    }
    
    @Override
    public void add(String key) {
        DcObjectListElement element = getDisplayElement(getModule().getIndex());
        element.setKey(key);
        getDcModel().addElement(element);
    }

    @Override
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
    
    @Override
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
    
    @Override
    public void add(List<? extends DcObject> objects) {
        clear();
        
        DcListModel model = new DcListModel();
        DcObjectListElement element;
        for (DcObject dco : objects) {
            element = getDisplayElement(dco.getModule().getIndex());
            element.setDcObject(dco);
            element.setKey(dco.getID());
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

    @Override
    public void addSelectionListener(ListSelectionListener lsl) {
        removeSelectionListener(lsl);
        super.addListSelectionListener(lsl);
    }
    
    @Override
    public void removeSelectionListener(ListSelectionListener lsl) {
        removeListSelectionListener(lsl);
    }
}
