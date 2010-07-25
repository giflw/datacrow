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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.db.Query;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.DcPropertyModule;
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
    private Map<Long, SecuredUser> users = new HashMap<Long, SecuredUser>();
    
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
           
        try {
            connection.close();
        } catch (SQLException se) {}
        
        try {
            connection = DatabaseManager.getAdminConnection();
            String sql = "select * from user where lower(loginname) = '" + username.toLowerCase() + "'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<DcObject> users = new WorkFlow().convert(rs);

            User user;
            if (users.size() == 1) {
                user = (User) users.get(0);
                sql = "select * from permission where user = " + user.getID();
                rs = stmt.executeQuery(sql);

                List<DcObject> permissions = new WorkFlow().convert(rs);
                for (DcObject permission : permissions)
                    user.addChild(permission);
                
                stmt.close();
                connection.close();
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
            e.printStackTrace();
            logger.error(e, e);
            throw new SecurityException(DcResources.getText("msgUserOrPasswordIncorrect"));
        }
    }   
    
    /**
     * Creates the default user. This user reflects the default SA account of the
     * HSQL database. No additional privileges need to be set.
     */
    private void createDefaultUser() {
        // default system administrator
        Connection connection = DatabaseManager.getConnection("dc_admin", "UK*soccer*96");
        if (connection == null) {
            User user = new User();
            user.setValue(User._A_LOGINNAME, "dc_admin");
            user.setValue(User._B_ENABLED, Boolean.TRUE);
            user.setValue(User._L_ADMIN, Boolean.TRUE);
            DatabaseManager.createUser(user, "UK*soccer*96");
        }
        
        User user = new User();
        user.setIDs();
        user.setValue(User._A_LOGINNAME, "sa");
        user.setValue(User._B_ENABLED, Boolean.TRUE);
        user.setValue(User._L_ADMIN, Boolean.TRUE);
        user.setValue(User._C_NAME, "Administrator");
        user.setValue(User._D_DESCRIPTION, "The default users. Has all rights.");
        
        for (Permission permission : getDefaultPermissions())
            user.addChild(permission);
        
        try {
            user.setIDs();
            user.saveNew(false);
            for (DcObject permission : user.getChildren())
                permission.saveNew(false);
            
        } catch (Exception e) {
            logger.error(e, e);
        }
    }  
    
    private Collection<Permission> getDefaultPermissions() {
        Collection<Permission> permissions = new ArrayList<Permission>();
        
        for (DcModule module : getManagedModules()) {
            for (DcField field : module.getFields()) {
                Permission permission = (Permission) DcModules.get(DcModules._PERMISSION).getItem();
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
                Permission permission = (Permission) DcModules.get(DcModules._PERMISSION).getItem();
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
            if (  !(module instanceof DcPropertyModule) &&
                   (module.isTopModule() || 
                    module.isChildModule()) &&
                    module.getIndex() != DcModules._CONTAINER &&
                    module.getIndex() != DcModules._USER &&
                    module.getIndex() != DcModules._PERMISSION)
                modules.add(module);
        }
        return modules;
    }
    
    private int getUserCount() {
        Connection connection = null;
        Statement stmt = null;
        int users = 0;
        try {
            connection = DatabaseManager.getConnection("dc_admin", "UK*soccer*96");
            connection = connection == null ? DatabaseManager.getConnection("sa", "") : connection;
            
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
        Connection connection = DatabaseManager.getConnection("sa", "");
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
            Query query = new Query(Query._CREATE, DcModules.get(DcModules._USER).getItem(), null, null);
            for (PreparedStatement ps : query.getQueries()) 
                ps.execute();
        } catch (Exception se) {}

        try {
            Query query = new Query(Query._CREATE, DcModules.get(DcModules._PICTURE).getItem(), null, null);
            for (PreparedStatement ps : query.getQueries()) 
                ps.execute();
        } catch (SQLException se) {}

        try {
            Query query = new Query(Query._CREATE, DcModules.get(DcModules._PERMISSION).getItem(), null, null);
            for (PreparedStatement ps : query.getQueries()) 
                ps.execute();
        } catch (SQLException se) {}
    }
}
