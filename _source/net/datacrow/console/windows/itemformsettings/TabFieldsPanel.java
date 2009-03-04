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

package net.datacrow.console.windows.itemformsettings;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import net.datacrow.console.Layout;
import net.datacrow.console.components.panels.FieldSelectionPanel;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;

public class TabFieldsPanel extends JPanel implements ActionListener {

    private FieldSelectionPanel fld = new FieldSelectionPanel(DcModules.getCurrent(), false); 
    private DcObject tab;
    
    public TabFieldsPanel(DcObject tab) {
        this.tab = tab;
        build();
    }
    
    protected void remove(Collection<DcField> fields) {
        fld.remove(fields);
    }
    
    protected DcObject getTab() {
        return tab;
    }
    
    protected Collection<DcField> getFields() {
        Collection<DcField> fields = new ArrayList<DcField>();
        
        for (DcField field : fld.getSelectedFields())
            fields.add(field);
        
        return fields;
    }
    
    private void build() {
        
        //**********************************************************
        //Main Panel
        //**********************************************************
        add(fld,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
    }
    
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
    }
}
