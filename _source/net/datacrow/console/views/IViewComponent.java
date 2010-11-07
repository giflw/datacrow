package net.datacrow.console.views;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;

import javax.swing.event.ListSelectionListener;

import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcObject;

public interface IViewComponent extends ISortableComponent {
    
    void clear();
    
    View getView();
    void setView(View view);
    
    DcModule getModule();
    
    int getItemCount();
    
    boolean remove(String[] keys);
    void remove(int[] indices);
    
    void clear(int index);
    
    void setIgnorePaintRequests(boolean b);
    boolean isIgnoringPaintRequests();
    
    int getFirstVisibleIndex();
    int getLastVisibleIndex();
    int getViewportBufferSize();
    
    int add(String key);
    int add(DcObject item);
    void add(List<? extends DcObject> items);
    void add(Map<String, Integer> keys);
    
    List<String> getItemKeys();
    List<DcObject> getItems();
    DcObject getItemAt(int idx);
    DcObject getItem(String ID);
    String getItemKey(int idx);
    int getModule(int idx);
    
    void setSelected(int index);
    
    void ignoreEdit(boolean b);

    List<? extends DcObject> getSelectedItems();    
    List<String> getSelectedItemKeys();
    int[] getSelectedIndices();
    int getSelectedIndex();
    DcObject getSelectedItem();

    void deselect();
    
    int update(String ID);
    int update(String ID, DcObject dco);
    void afterUpdate();

    int[] getChangedIndices();
    void undoChanges();
    boolean isChangesSaved();
    
    void setSelectionMode(int mode);
    
    void cancelEdit();
    int locationToIndex(Point point);    
    void setCursor(Cursor cursor);
    
    boolean allowsHorizontalTraversel();
    boolean allowsVerticalTraversel();
    
    void applySettings();
    void saveSettings();

    // LISTENERS
    void addSelectionListener(ListSelectionListener lsl);
    void removeSelectionListener(ListSelectionListener lsl);
    
    void addKeyListener(KeyListener kl);
    void addMouseListener(MouseListener ml);
    void removeMouseListener(MouseListener ml);
    MouseListener[] getMouseListeners();
    
    void activate();
    
    void paintRegionChanged();
    void repaint();
    void revalidate();
}
