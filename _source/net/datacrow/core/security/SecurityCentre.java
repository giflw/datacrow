/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
 *                                                                            *
 *                       This file is part of Data Crow.                      *
 *       Data Crow is free software; you can redistribute it and/or           *
 *        modify it under the terms of the GNU General Public                 *
 *       License as published by the Free Software Foundation; either         *
 *              version 3 of the License, or any later version.               *
 *                                                                            *
 *        Data Crow is distributed in the hope that it will be useful,        *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *           MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.             *
 *           See the GNU General Public License for more details.             *
 *                                                                            *
 *        You should have received a copy of the GNU General Public           *
 *  License along with this program. If not, see http://www.gnu.org/licenses  *
 *                                                                            *
 ******************************************************************************/

package net.datacrow.core.security;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.datacrow.core.db.CreateQuery;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.Permission;
import net.datacrow.core.objects.helpers.User;
import net.datacrow.core.plugin.Plugins;
import net.datacrow.core.plugin.RegisteredPlugin;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.wf.WorkFlow;

import org.apache.log4j.Logger;

/**
 * The security center is the access point for all security related information.
 * Note that the users logged on to the web application are not represented by this class. 
 * 
 * @author Robert Jan van der Waals
 */
public class SecurityCentre {
    
    private static Logger logger = Logger.getLogger(SecurityCentre.class.getName());
    private static SecurityCentre instance = new SecurityCentre();
    
    // all logged on users
    private Map<String, SecuredUser> users = new HashMap<String, SecuredUser>();
    
    // the current using, running on this instance
    private User user;
    
    /**
     * Retrieves the sole instance of this class
     */
    public static synchronized SecurityCentre getInstance() {
        return instance;
    }

    /**
     * Creates a new instance
     */
    private SecurityCentre() {}
    
    /**
     * Retrieves the currently logged on user.
     */
    public SecuredUser getUser() {
        return user != null ? users.get(user.getID()) : null;
    }
    
    /**
     * Changes the password for the specified user.
     * @param user
     * @param password The new password
     */
    public void changePassword(User user, String password) {
        DatabaseManager.changePassword(user, password);
        if (users.containsKey(user.getID()))
            users.get(user.getID()).setPassword(password);    
    }
    
    /**
     * Try to log in with the default user credentials (sa, empty password).
     */
    public boolean unsecureLogin() {
        try {
            return login("sa", "", false) != null && getUserCount() == 1;
        } catch (SecurityException se) {
            return false;
        }
    }
    
    public boolean isLoggedIn(SecuredUser su) {
        return users.containsValue(su);
    }
    
    public void logoff(User user) {
        users.remove(user.getID());
    }
    
    public SecuredUser login(String username, String password, boolean web) throws SecurityException {
        Connection connection = DatabaseManager.getConnection(username, password);
        
        if (connection == null) 
            throw new SecurityException(DcResources.getText("msgUserOrPasswordIncorrect"));
           
        ResultSet rs = null;
        Statement stmt = null;
        try {
            connection = DatabaseManager.getAdminConnection();
            String sql = "select * from user where lower(loginname) = '" + username.toLowerCase() + "'";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            
            List<DcObject> users = WorkFlow.getInstance().convert(rs, new int[] {User._ID, User._A_LOGINNAME, User._B_ENABLED, User._C_NAME, User._L_ADMIN});

            User user;
            if (users.size() == 1) {
                user = (User) users.get(0);
                sql = "select * from permission where user = '" + user.getID() + "'";
                rs = stmt.executeQuery(sql);

                List<DcObject> permissions = WorkFlow.getInstance().convert(rs, new int[] {Permission._ID, 
                                                                                           Permission._A_PLUGIN, 
                                                                                           Permission._B_FIELD, 
                                                                                           Permission._C_MODULE, 
                                                                                           Permission._D_VIEW,
                                                                                           Permission._E_EDIT,
                                                                                           Permission._F_USER});
                for (DcObject permission : permissions)
                    user.addChild(permission);
                
                stmt.close();
            } else {
                stmt.close();
                connection.close();
                throw new SecurityException(DcResources.getText("msgUserOrPasswordIncorrect"));
            }
            
            if (!(Boolean) user.getValue(User._B_ENABLED))
                throw new SecurityException(DcResources.getText("msgLoginNotAllowed"));
            
            SecuredUser su = new SecuredUser(user, password);
            this.users.put(user.getID(), su);
            
            // web users hold their own reference of the logged in user
            if (!web)
                this.user = (User) users.get(0);
            
            return su;
            
        } catch (Exception e) {
            logger.info(e, e);
            throw new SecurityException(DcResources.getText("msgUserOrPasswordIncorrect"));
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (Exception e) {
                logger.info(e, e);
            }
        }
    }   
    
    /**
     * Creates the default user. This user reflects the default SA account of the
     * HSQL database. No additional privileges need to be set.
     */
    private void createDefaultUser() {
        // default system administrator
        Connection connection = DatabaseManager.getConnection("DC_ADMIN", "UK*soccer*96");
        if (connection == null) {
            User user = new User();
            user.setValue(User._A_LOGINNAME, "DC_ADMIN");
            user.setValue(User._B_ENABLED, Boolean.TRUE);
            user.setValue(User._L_ADMIN, Boolean.TRUE);
            DatabaseManager.createUser(user, "UK*soccer*96");
        } else {
            try {
                connection.close();
            } catch (SQLException se) {
                logger.error(se, se);
            }
        }
        
        User user = new User();
        user.setIDs();
        user.setValue(User._A_LOGINNAME, "SA");
        user.setValue(User._B_ENABLED, Boolean.TRUE);
        user.setValue(User._L_ADMIN, Boolean.TRUE);
        user.setValue(User._C_NAME, "Administrator");
        user.setValue(User._D_DESCRIPTION, "The default users. Has all rights.");
        
        for (Permission permission : getDefaultPermissions())
            user.addChild(permission);
        
        try {
            user.setIDs();
            user.saveNew(false);
        } catch (Exception e) {
            logger.error(e, e);
        }
    }  
    
    private Collection<Permission> getDefaultPermissions() {
        Collection<Permission> permissions = new ArrayList<Permission>();
        
        Permission permission;
        for (DcModule module : getManagedModules()) {
            for (DcField field : module.getFields()) {
                permission = (Permission) DcModules.get(DcModules._PERMISSION).getItem();
                permission.setIDs();
                permission.setValue(Permission._B_FIELD, Long.valueOf(field.getIndex()));
                permission.setValue(Permission._C_MODULE, Long.valueOf(field.getModule()));
                permission.setValue(Permission._D_VIEW, Boolean.TRUE);
                permission.setValue(Permission._E_EDIT, Boolean.TRUE);
                permissions.add(permission);
            }
        }
        
        for (RegisteredPlugin plugin : Plugins.getInstance().getRegistered()) {
            if (plugin.isAuthorizable()) {
                permission = (Permission) DcModules.get(DcModules._PERMISSION).getItem();
                permission.setIDs();
                permission.setValue(Permission._A_PLUGIN, plugin.getKey());
                permission.setValue(Permission._D_VIEW, Boolean.TRUE);
                permissions.add(permission);
            }
        }
        
        return permissions;
    }
    
    public Collection<DcModule> getManagedModules() {
        Collection<DcModule> modules = new ArrayList<DcModule>();
        for (DcModule module : DcModules.getAllModules()) {
            if (    module.getIndex() != DcModules._CONTAINER &&
                    module.getIndex() != DcModules._USER &&
                    module.getIndex() != DcModules._PERMISSION &&
                    module.getType()  != DcModule._TYPE_EXTERNALREFERENCE_MODULE &&
                    module.getType()  != DcModule._TYPE_MAPPING_MODULE &&
                    module.getType()  != DcModule._TYPE_TEMPLATE_MODULE)
                modules.add(module);
        }
        return modules;
    }
    
    private int getUserCount() {
        Connection connection = null;
        Statement stmt = null;
        int users = 0;
        try {
            connection = DatabaseManager.getConnection("DC_ADMIN", "UK*soccer*96");
            connection = connection == null ? DatabaseManager.getConnection("SA", "") : connection;
            
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ID FROM user");
            while (rs.next())
                users++;
            
            rs.close();
            
        } catch (SQLException se) {
            logger.error(se, se);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (Exception e) {}
        }
        return users;
    }
    
    public void initialize() {
        Connection connection = DatabaseManager.getConnection("SA", "");
        if (connection != null) { // default user present.
            try {
                createTables();
                if (getUserCount() == 0) // no user. create default.
                    createDefaultUser();

                connection.close();
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }
    
    private void createTables() {
        try {
            CreateQuery query = new CreateQuery(DcModules._USER);
            query.setLog(false);
            query.run();
        } catch (Exception se) {}

        try {
            CreateQuery query = new CreateQuery(DcModules._PICTURE);
            query.setLog(false);
            query.run();
        } catch (SQLException se) {}

        try {
            CreateQuery query = new CreateQuery(DcModules._PERMISSION);
            query.setLog(false);
            query.run();
        } catch (SQLException se) {}
    }
}
