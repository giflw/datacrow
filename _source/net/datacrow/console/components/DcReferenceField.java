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

package net.datacrow.console.components;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.util.DcSwingUtilities;

public class DcReferenceField extends JComponent implements IComponent, ActionListener {

    private JComboBox cb;
    private JButton btCreate = ComponentFactory.getIconButton(IconLibrary._icoOpenNew);
    
    private final int referenceModIdx;
    
    public DcReferenceField(int referenceModIdx) {
        super();
        
        setFont(ComponentFactory.getStandardFont());
        
        this.referenceModIdx = referenceModIdx;
        
        cb = ComponentFactory.getObjectCombo(referenceModIdx);
        
        setLayout(Layout.getGBL());
        add(cb, Layout.getGBC( 0, 0, 1, 1, 100.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));        
        
        if (SecurityCentre.getInstance().getUser().isEditingAllowed(DcModules.get(referenceModIdx)))
            add(btCreate, Layout.getGBC( 1, 0, 1, 1, 0.0, 0.0
                    ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                     new Insets( 0, 0, 0, 0), 0, 0));        
        
        btCreate.addActionListener(this);
        btCreate.setActionCommand("create");
    }

    public void setEditable(boolean b) {
        btCreate.setEnabled(b);
    }
    
    public Object getValue() {
        return cb.getSelectedItem();
    }
    
    public void setValue(Object o) {
        cb.setSelectedItem(o);
    }
    
    public void clear() {
        btCreate = null;
        cb = null;
        
        removeAll();
    }
    
    private void create() {
        DcObject dco = DcModules.get(referenceModIdx).getDcObject();
        ItemForm itemForm = new ItemForm(false, false, dco, true);
        itemForm.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("create"))
            create();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(DcSwingUtilities.setRenderingHint(g));
    }  
}