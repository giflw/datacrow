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

package net.datacrow.core.plugin;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import net.datacrow.console.views.View;
import net.datacrow.core.DataCrow;
import net.datacrow.core.UserMode;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcTemplate;

public abstract class Plugin extends AbstractAction {

    protected static final int _SEARCHTAB = 0;
    protected static final int _INSERTTAB = 1;
    protected static final int _NOTETAB = 2;
    
    private String label = null;
    private final int viewIdx;
    private final int moduleIdx;
    private DcObject dco;
    private DcTemplate template;

    protected Plugin(DcObject dco, 
                     DcTemplate template, 
                     int viewIdx, 
                     int moduleIdx) {
        
        this.moduleIdx = moduleIdx;
        this.viewIdx = viewIdx;
        this.template = template;
        this.dco = dco;
    }
    
    public int getXpLevel() {
        return UserMode._XP_BEGINNER;
    }
    
    public boolean isAdminOnly() {
        return false;
    }
    
    public int getViewIdx() {
        return viewIdx;
    }

    public View getView() {
        if (getCurrentTab() == _SEARCHTAB)
            return getModule().getSearchView().get(viewIdx);
        else 
            return getModule().getInsertView().get(viewIdx);
    }    

    public DcObject getItem() {
        return dco;
    }

    public DcTemplate getTemplate() {
        return template;
    }
    
    public String getLabelShort() {
        return getLabel();
    }
    
    public final int getCurrentTab() {
        return DataCrow.mainFrame.getSelectedTab();
    }
    
    public final int getModuleIdx() {
		return moduleIdx;
	}

	public final DcModule getModule() {
        return moduleIdx != -1 ? DcModules.get(moduleIdx) : DcModules.getCurrent();
    }
    
    public String getHelpText() {
        return null;
    }

    public KeyStroke getKeyStroke() {
        return null;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void clear() {
        if (dco != null) {
            // only clear when the action is item specific 
            dco = null;
            template = null;
        }
    }
    
    public final String getKey() {
        return getClass().getSimpleName();
    }
    
    public boolean isSystemPlugin() {
        return false;
    }

    public boolean isShowOnToolbar() {
        return true;
    }
    
    public boolean isShowInPopupMenu() {
        return true;
    }

    public boolean isShowInMenu() {
        return true;
    }
    
    public boolean isAuthorizable() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    public abstract ImageIcon getIcon();
    public abstract void actionPerformed(ActionEvent ae);

    @Override
    public final boolean equals(Object o) {
        if (o instanceof Plugin) {
            Plugin p = (Plugin) o;
            return p.getItem() == null && 
                   getItem() == null &&
                   p.getModuleIdx() == getModuleIdx() &&
                   p.getTemplate() == getTemplate() &&
                   p.getViewIdx() == getViewIdx();
        }
        return false;
    }

    @Override
    public final int hashCode() {
        int hash = getItem() != null ? getItem().hashCode() : 0;
        hash += getTemplate() != null ? getTemplate().hashCode() : 0;
        hash += viewIdx;
        hash += getModule().getIndex();
        return hash;
    }
}