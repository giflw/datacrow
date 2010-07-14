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

package net.datacrow.console.views;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

public class CachedChildView extends View implements ActionListener {

    private ArrayList<DcObject> children = new ArrayList<DcObject>();
    
    public CachedChildView(MasterView mv, int type, IViewComponent vc, String title, ImageIcon icon, int index) {
        super(mv, type, vc, title, icon, index);
    }
    
    @Override
    public void remove(Long[] ids) {
        super.remove(ids);
        
        DcObject dco = null;
        
        for (DcObject child : children) {
            for (int i = 0; i < ids.length; i++) {
                if (child.getID().equals(ids[i]))
                    dco = child;
            }
        }
        children.remove(dco);
    }

    @Override
    public void open() {}    
    
    @Override
    public void clear() {
        super.clear(false);
        children.clear();
    }
    
    public void clearView() {
        super.clear(false);
    }
    
    @Override
    public void add(List<DcObject> c) {
        for (DcObject dco : c) {
            if (!children.contains(dco)) {
                //dco.setIDs();
                children.add(dco);
            }
        }
    }
    
    @Override
    public void setParentID(Long ID, boolean show) {
        if (ID != null && (vc.getItemCount() == 0 || !ID.equals(getParentID()))) {
            syncCache();
            super.setParentID(ID, show);
            if (show) 
                loadChildren();
        }
    }     
    
    protected void syncCache() {
        DcObject[] c = children.toArray(new DcObject[0]);
        for (DcObject dco : vc.getItems()) {
            boolean exists = false;
            for (int j = 0; j < c.length; j++) {
                DcObject dcoCached = c[j];
                if (dcoCached.getID().equals(dco.getID())) {
                    dcoCached.copy(dco, true, true);
                    exists = true;
                    break;
                }
            }

            if (!exists)
                children.add(dco);
        }
    }
    
    public void removeChildren(Long parentID) {
        Collection<DcObject> c = getChildren(parentID);
        children.removeAll(c);
    }

    @Override
    public void loadChildren() {
        clearView();
        List<DcObject> c = getChildren(getParentID());
        vc.add(c);
    }
    
    public List<DcObject> getChildren(Long parentID) {
        syncCache();
        List<DcObject> c = new ArrayList<DcObject>();
        for (int i = 0; i < children.size(); i++) {
            DcObject dco = children.get(i);
            if (dco.getParentID().equals(parentID))
                c.add(dco);
        }
        return c;
    }

    @Override
    public Collection<Component> getAdditionalActions() {
        Collection<Component> components = new ArrayList<Component>();
        
        JButton buttonAddChild = ComponentFactory.getButton(DcResources.getText("lblAddChild", getModule().getObjectName()));
        buttonAddChild.addActionListener(this);
        buttonAddChild.setActionCommand("addChild");
        buttonAddChild.setMnemonic('T');
        components.add(buttonAddChild);
        
        return components;
    }

    private void addChild() {
        if (getParentID() != null && !getParentID().equals("")) {
            DcObject dco = getModule().getItem();
            dco.setValue(dco.getParentReferenceFieldIndex(), getParentID());
            add(dco);
            children.add(dco);
        } else {
            DcSwingUtilities.displayErrorMessage(DcResources.getText("msgAddSelectParent"));
        }  
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("addChild"))
            addChild();
    }
}