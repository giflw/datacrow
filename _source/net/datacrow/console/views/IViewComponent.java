package net.datacrow.console.views;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.List;

import javax.swing.event.ListSelectionListener;

import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcObject;

public interface IViewComponent {
    
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
    
    void add(String key);
    void add(DcObject item);
    void add(List<? extends DcObject> items);
    void add(Collection<String> keys);
    
    Collection<DcObject> getItems();
    DcObject getItemAt(int idx);
    DcObject getItem(String ID);
    
    void setSelected(Collection<? extends DcObject> dcos);
    void setSelected(int index);
    
    void ignoreEdit(boolean b);

    Collection<? extends DcObject> getSelectedItems();    
    int[] getSelectedIndices();
    int getSelectedIndex();
    DcObject getSelectedItem();

    void deselect();
    
    void update(String ID);
    void update(String ID, DcObject dco);
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
