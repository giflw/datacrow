package net.datacrow.core.wf.requests;

import net.datacrow.core.data.DataManager;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.helpers.User;

public class DeleteUserRequest implements IRequest {

    private static final long serialVersionUID = 8435667290693840987L;
    private User user;

    public DeleteUserRequest(User user) {
        this.user = (User) DataManager.getItem(DcModules._USER, user.getID());
    }

    @Override
    public void execute() {
        DatabaseManager.deleteUser(user);
    }
    
    @Override
    public void end() {
        // was explicitly retrieved for this request
        if (user != null) user.destroy();
        user = null;
    }
    
    @Override
    public boolean getExecuteOnFail() {
        return false;
    }

    @Override
    public void setExecuteOnFail(boolean b) {}
}
