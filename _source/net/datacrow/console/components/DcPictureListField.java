package net.datacrow.console.components;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.datacrow.console.Layout;
import net.datacrow.console.components.lists.DcObjectList;
import net.datacrow.console.views.ViewMouseListener;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.Picture;

public class DcPictureListField extends JPanel {
    
    private DcObjectList list = new DcObjectList(DcModules.get(DcModules._PICTURE), DcObjectList._CARDS, true, true);
    
    public DcPictureListField() {
        build();
        
        list.addMouseListener(new ViewMouseListener());
    }
    
    public void add(Picture picture) {
        list.add(picture);
    }
    
    public void clear() {
        list.clear();
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        JScrollPane sp = new JScrollPane(list);
        add(sp, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
    }
}
