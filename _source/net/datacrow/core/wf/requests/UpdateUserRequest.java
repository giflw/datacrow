package net.datacrow.core.wf.requests;

import java.util.Collection;

import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.User;

public class UpdateUserRequest implements IRequest {

    private static final long serialVersionUID = -404560611962354183L;
    private User user;

    public UpdateUserRequest(User user) {
        this.user = user;
    }

    public void execute(Collection<DcObject> objects) {
        DatabaseManager.setPriviliges(user);
    }
    
    public void end() {
        user = null;
    }
    
    public boolean getExecuteOnFail() {
        return false;
    }

    public void setExecuteOnFail(boolean b) {}
}