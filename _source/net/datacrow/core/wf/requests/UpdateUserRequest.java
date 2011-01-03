package net.datacrow.core.wf.requests;

import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.objects.helpers.User;

public class UpdateUserRequest implements IRequest {

    private static final long serialVersionUID = -404560611962354183L;
    private User user;

    public UpdateUserRequest(User user) {
        this.user = user;
    }

    @Override
    public void execute() {
        DatabaseManager.setPriviliges(user);
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