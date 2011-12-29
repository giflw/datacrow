package net.datacrow.console.views;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;

import net.datacrow.console.windows.security.UserForm;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

public class UserView extends View {

    public UserView(MasterView mv, int type, IViewComponent vc, String title, ImageIcon icon, int index) {
        super(mv, type, vc, title, icon, index);
    }

    @Override
    public void open(boolean readonly) {
        DcObject dco = getSelectedItem();
        
        if (dco != null) {
            UserForm form = new UserForm(readonly, dco, getType() == View._TYPE_SEARCH, getType() != View._TYPE_SEARCH);
            form.setVisible(true);
        } else {
            DcSwingUtilities.displayWarningMessage(DcResources.getText("msgSelectRowToOpen"));
        }
    }
    
    @Override
    protected Collection<Component> getAdditionalActions() {
        return new ArrayList<Component>();
    } 
}
