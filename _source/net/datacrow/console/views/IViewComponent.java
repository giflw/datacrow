package net.datacrow.console.views;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.event.ListSelectionListener;

import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcObject;

public interface IViewComponent {
    
    void clear();
    
    View getView();
    void setView(View view);
    
    DcModule getModule();
    
    int getItemCount();
    
    int getOptimalItemAdditionBatchSize();
    
    boolean remove(String[] ids);
    void remove(int[] indices);
    
    void add(DcObject item);
    void add(DcObject[] items); 
    void add(Collection<? extends DcObject> items);
    
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
    
    void updateItem(String ID, DcObject dco, boolean overwrite, boolean allowDeletes, boolean mark);
    void updateItemAt(int index, DcObject dco, boolean overwrite, boolean allowDeletes, boolean mark);

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
    void updateUI(String ID);
    void afterUpdate();

    // LISTENERS
    void addSelectionListener(ListSelectionListener lsl);
    void removeSelectionListener(ListSelectionListener lsl);
    
    void addKeyListener(KeyListener kl);
    void addMouseListener(MouseListener ml);
    void removeMouseListener(MouseListener ml);
    MouseListener[] getMouseListeners();
}
