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

package net.datacrow.core.settings;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A settings group contains specific settings. A group can have
 * on child, creating a two level settings hierarchy. Deeper 
 * hierarchies are not supported as yet.
 * 
 * @author Robert Jan van der Waals
 * @since 1.4
 * @version 1.9
 */
public class SettingsGroup {
    
    private String helpIndex = "";
    private String sKey = "";
    private LinkedHashMap<String, Setting> htSettings = new LinkedHashMap<String, Setting> ();
    private Hashtable<String, SettingsGroup> htChildren = new Hashtable<String, SettingsGroup>();
    
    /**
     * Creates a new settings group
     * 
     * @param sKey a unique identifier of this group
     */
    public SettingsGroup(String sKey, String helpIndex) {
        this.sKey = sKey;
        this.helpIndex = helpIndex;
    }

    /**
     * Adds a child to the settings group
     */
    public void addChild(SettingsGroup child) {
        htChildren.put(child.getKey(), child);
    }
    
    public String getKey() {
        return sKey;   
    }
    
    public String getHelpIndex() {
    	return helpIndex;
    }
    
    public Hashtable<String, SettingsGroup> getChildren() {
        return htChildren;
    }
    
    public SettingsGroup getChild(String sGroupKey) {
        return htChildren.get(sGroupKey);
    }
    
    public Map<String, Setting> getSettings() {
        return htSettings;
    }
    
    protected Setting getSetting(String sKey) {
        Setting setting = htSettings.get(sKey);
        
        if (setting != null) return setting; 
        
        for (SettingsGroup group : htChildren.values()) {
            if (group != null) {
                setting = group.getSetting(sKey);
                
                if (setting != null) break;   
            }
        }
        return setting;
    }
    
    protected void add(Setting setting) {
        htSettings.put(setting.getKey(), setting);
    }
    
    @Override
    public String toString() {
        return sKey; 
    }
}
