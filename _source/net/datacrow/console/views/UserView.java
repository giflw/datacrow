package net.datacrow.console.views;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;

import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.windows.security.UserForm;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;

public class UserView extends View {

    public UserView(MasterView mv, int type, IViewComponent vc, String title, ImageIcon icon, int index) {
        super(mv, type, vc, title, icon, index);
    }

    @Override
    public void open() {
        DcObject dco = getSelectedItem();
        
        if (dco != null) {
            UserForm form = new UserForm(dco, getType() == View._TYPE_SEARCH, getType() != View._TYPE_SEARCH);
            form.setVisible(true);
        } else {
            new MessageBox(DcResources.getText("msgSelectRowToOpen"), MessageBox._WARNING);
        }
    }
    
    @Override
    protected Collection<Component> getAdditionalActions() {
        return new ArrayList<Component>();
    } 
}
