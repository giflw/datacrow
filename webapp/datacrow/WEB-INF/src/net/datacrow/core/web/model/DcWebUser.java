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

package net.datacrow.core.web.model;

import java.util.List;

import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.User;
import net.datacrow.core.security.SecuredUser;

public class DcWebUser {

    private String password = "";
    private String username = "";
    
    private SecuredUser securedUser;
    
    public SecuredUser getSecuredUser() {
        return securedUser;
    }

    public void setSecuredUser(SecuredUser securedUser) {
        this.securedUser = securedUser;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getUsername() {
        if (username == null || username.length() == 0) {
            List<DcObject> users = DataManager.get(DcModules._USER, null);
            if (users.size() == 1)
                username = (String) users.get(0).getValue(User._A_LOGINNAME);
        }
        
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
