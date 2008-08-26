package net.datacrow.console.components.lists.elements;

import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.helpers.User;

public class DcUserListElement extends DcObjectListElement {
    
//    private static final Color bg = new Color(255, 255, 230);
//    private static final Dimension dimTxt = new Dimension(145, 45);
//    private static final Dimension dimPicLbl = new Dimension(146, 140);
    
    public DcUserListElement(DcObject dco) {
        super(dco);
    }

    @Override
    protected void build() {
//        addPicture(getPictures());
//        
//        DcTextPane title = ComponentFactory.getTextPane();
//        JScrollPane scroller = new JScrollPane(title);
//        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
//        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        title.setText(gdco.getName());
//        title.setBackground(bg);
//        
//        title.setPreferredSize(dimTxt);
//        title.setMinimumSize(dimTxt);
//        title.setMaximumSize(dimTxt);
//        add(title);
    }

    @Override
    public Collection<Picture> getPictures() {
        Collection<Picture> pictures = new ArrayList<Picture>();
        pictures.add((Picture) dco.getValue(User._E_PHOTO));
        return pictures;
    }
}
