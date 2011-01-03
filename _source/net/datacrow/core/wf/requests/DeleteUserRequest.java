package net.datacrow.core.wf.requests;

import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.objects.helpers.User;

public class DeleteUserRequest implements IRequest {

    private static final long serialVersionUID = 8435667290693840987L;
    private User user;

    public DeleteUserRequest(User user) {
        this.user = user;
    }

    @Override
    public void execute() {
        DatabaseManager.deleteUser(user);
    }
    
    @Override
    public void end() {
        user = null;
    }
    
    @Override
    public boolean getExecuteOnFail() {
        return false;
    }

    @Override
    public void setExecuteOnFail(boolean b) {}
}
