package net.datacrow.core.wf.requests;

import java.util.Collection;

import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.User;

/**
 * Creates a new user including all her permissions.
 * 
 * @author Robert Jan van der Waals
 */
public class CreateUserRequest implements IRequest {

    private static final long serialVersionUID = -300657035562085171L;
    private User user;

    public CreateUserRequest(User user) {
        this.user = user;
    }

    public void execute(Collection<DcObject> objects) {
        DatabaseManager.createUser(user, "");
    }
    
    public void end() {
        user = null;
    }
    
    public boolean getExecuteOnFail() {
        return false;
    }

    public void setExecuteOnFail(boolean b) {}
}
