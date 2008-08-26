package net.datacrow.core.wf.requests;

import java.util.Collection;

import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.User;

public class DeleteUserRequest implements IRequest {

    private static final long serialVersionUID = 8435667290693840987L;
    private User user;

    public DeleteUserRequest(User user) {
        this.user = user;
    }

    public void execute(Collection<DcObject> objects) {
        DatabaseManager.deleteUser(user);
    }
    
    public void end() {
        user = null;
    }
    
    public boolean getExecuteOnFail() {
        return false;
    }

    public void setExecuteOnFail(boolean b) {}
}
