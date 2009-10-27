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

import javax.swing.JList;
import javax.swing.JViewport;
import javax.swing.ListModel;
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
import net.datacrow.core.modules.IChildModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcProperty;
import net.datacrow.core.objects.DcTemplate;
import net.datacrow.core.objects.helpers.AudioTrack;
import net.datacrow.core.objects.helpers.MusicTrack;

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
    
    public void saveSettings() {
    }

    public int getOptimalItemAdditionBatchSize() {
        return 1;
    }
    
    public void ignoreEdit(boolean b) {}
    
    public void undoChanges() {
        for (DcObject dco : getItems())
            dco.markAsUnchanged();
    }

    public boolean isChangesSaved() {
        for (DcObject dco : getItems())
            if (dco.isChanged()) return false;
        
        return true;
    }    
    
    public void setView(View view) {
        this.view = view;
        
        if (    view.getType() == View._TYPE_SEARCH && 
                module != null && 
                module.isSelectableInUI()) {
            new ViewRecycler("View recycler " + module).start();
        }
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
        for (int i = 0 ; i < getDcModel().getSize(); i++) {
            DcObjectListElement element = (DcObjectListElement) getDcModel().getElementAt(i);
            if (element.getDcObject() != null) 
                objects.add(element.getDcObject());
        }
        return objects;
    }

    public Collection<DcObject> getSelectedItems() {
        int[] indices = getSelectedIndices();
        Collection<DcObject> objects = new ArrayList<DcObject>();

        for (int i = 0; i < indices.length; i++) {
            DcObjectListElement element = (DcObjectListElement) getDcModel().getElementAt(indices[i]);
            DcObject dco = element.getDcObject();
            objects.add(dco);
        }

        return objects;
    }    
    
    public DcObject getItem(String ID) {
        DcObjectListElement element = getElement(ID);
        return element != null ? element.getDcObject() : null;
    }    

    private DcObjectListElement getElement(String ID) {
        for (int i = 0 ; i < getDcModel().getSize(); i++) {
            DcObjectListElement element = (DcObjectListElement) getDcModel().getElementAt(i);
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
    
    public void updateUI(String ID) {
        DcObjectListElement element = getElement(ID);
        if (element != null) {
            element.update();
            revalidate();
            repaint();
        }
    }       
    
    public void updateItemAt(int index, DcObject dco, boolean overwrite, boolean allowDeletes, boolean mark) {
        DcObjectListElement element = (DcObjectListElement) getDcModel().getElementAt(index);
        element.update(dco, overwrite, allowDeletes, mark);
        
        try {
            setSelectedValue(getDcModel().getElementAt(index), true);
        } catch (Exception e) {
            logger.warn("Could set [" + index + "] selected", e);
        }
    }
    
    public void updateItem(String ID, DcObject dco, boolean overwrite, boolean allowDeletes, boolean mark) {
        DcObjectListElement element = getElement(ID);
        if (element != null)
            element.update(dco, overwrite, allowDeletes, mark);
        
        try {
            setSelectedValue(getElement(ID), true);
        } catch (Exception e) {
            logger.warn("Could set [" + ID + "] selected", e);
        }
    }    
    
    public void setSelected(DcObject dco, int field) {
        for (int row = 0 ; row < getDcModel().getSize(); row++) {
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
        for (DcObject o : dcos) {
            for (int row = 0 ; row < getDcModel().getSize(); row++) {
                DcObjectListElement element = (DcObjectListElement) getDcModel().getElementAt(row);
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
        for (int i = 0 ; i < getDcModel().getSize(); i++) {
            DcObjectListElement element = (DcObjectListElement) getDcModel().getElementAt(i);
            if (element.getDcObject() != null && element.getDcObject().isChanged()) 
                rows.add(i);
        }
        
        int[] indices = new int[rows.size()];
        int counter = 0;
        for (Integer idx : rows)
            indices[counter++] = idx.intValue();

        return indices;        
    }
    
    public boolean remove(String[] ids) {
        boolean removed = false;
        for (int i = 0; i < ids.length; i++) {
            DcObjectListElement element = getElement(ids[i]);
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
    
    public void add(DcObject dco) {
        if (dco.getID() == null || dco.getID().equals("")) 
            dco.setIDs();        
        
        if (getView() != null && getView().getType() == View._TYPE_SEARCH)
            dco.markAsUnchanged();
        
        getDcModel().addElement(getDisplayElement(dco));
        ensureIndexIsVisible(getModel().getSize());
    }
    
    public void add(DcObject[] objects) {
        DcListModel model = new DcListModel();
        
        for (DcObject dco : objects)
            model.addElement(getDisplayElement(dco));
        
        setModel(model);
        revalidate();
        repaint();
    }    
    
    public void add(Collection<? extends DcObject> objects) {
        DcListModel model = new DcListModel();
        
        for (DcObject dco : objects)
            model.addElement(getDisplayElement(dco));
        
        setModel(model);
        revalidate();
        repaint();
    }
    
    public DcObjectListElement getDisplayElement(DcObject dco) {
        
        // TODO: move to the module class itself!
        
        DcObjectListElement element = null;
        DcModule module = dco.getModule();
        
        if (style == _ELABORATE) {
            if (module.getIndex() == DcModules._AUDIOCD) 
                element = new DcAudioCDListHwElement(dco);
            else if (module.getIndex() == DcModules._MUSICALBUM) 
                element = new DcMusicAlbumListHwElement(dco);
            else if (module.getIndex() == DcModules._SOFTWARE)
                element = new DcSoftwareListHwElement(dco);
            else if (module.getIndex() == DcModules._MOVIE)
                element = new DcMovieListHwElement(dco);
            else if (module.getIndex() == DcModules._BOOK)
                element = new DcBookListHwElement(dco);
            else 
                element = new DcCardObjectListElement(dco);
        } else if (style == _CARDS) {
            if (dco instanceof AudioTrack)
                element = new DcAudioTrackListElement(dco);
            else if (dco instanceof DcTemplate)
                element = new DcTemplateListElement(dco);
            else if (dco instanceof MusicTrack)
                element = new DcMusicTrackListElement(dco);
            else if (dco instanceof DcProperty)
                element = new DcPropertyListElement(dco);
            else if (module instanceof IChildModule)
                element = new DcShortObjectListElement(dco);
            else 
                element = new DcCardObjectListElement(dco);       
        } else if (style == _LISTING) {
            if (dco instanceof DcProperty)
                element = new DcPropertyListElement(dco);
            else if (dco instanceof DcTemplate)
            	element = new DcTemplateListElement(dco);
            else if (dco instanceof MusicTrack)
                element = new DcMusicTrackListElement(dco);
            else if (dco instanceof AudioTrack)
                element = new DcAudioTrackListElement(dco);
            else
                element = new DcShortObjectListElement(dco);
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
    
    private class ViewRecycler extends Thread {
        
        public ViewRecycler(String name) {
            super(name);
            setPriority(Thread.MIN_PRIORITY);
        }
        
        @Override
        public void run() {
            
            ListModel model = getModel();
            
            while (true) {
                int first = getFirstVisibleIndex() - 10;
                int last = first + 20;
                int size = model.getSize();
                
                first = first < 0 ? 0 : first;
                last = last > size ? size : last;
                
                try {
                    for (int i = 0; i < first; i++)
                        getElement(i).clear();
                    
                    for (int i = last; i < size; i++)
                        getElement(i).clear();

                } catch (Exception e) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e1) {
                        logger.error(e, e);
                    }
                }
                
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    logger.error(e, e);
                }
            }
        }
    }
}
