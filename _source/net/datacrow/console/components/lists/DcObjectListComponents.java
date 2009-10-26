package net.datacrow.console.components.lists;

import java.util.ArrayList;
import java.util.List;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcPictureField;
import net.datacrow.console.components.DcTextPane;

public abstract class DcObjectListComponents {

    private static final int _MAX_ITEMS = 50;
    
    private static List<DcTextPane> textPanes = new ArrayList<DcTextPane>();
    private static List<DcPictureField> pictureFields = new ArrayList<DcPictureField>();
    
    public static DcTextPane getTextPane() {
        return textPanes.size() > 0 ? textPanes.remove(0) : ComponentFactory.getTextPane();
    }

    public static DcPictureField getPictureField() {
        return pictureFields.size() > 0 ? pictureFields.remove(0) : ComponentFactory.getPictureField(false, false, false);
    }
    
    public static void release(DcPictureField picField) {
        if (picField != null) {
            picField.clear();
            
            if (pictureFields.size() < _MAX_ITEMS)
                pictureFields.add(picField);
        }
    }

    public static void release(DcTextPane textPane) {
        if (textPane != null) {
            textPane.setText("");
            
            if (textPanes.size() < _MAX_ITEMS)
                textPanes.add(textPane);
        }
    }
}