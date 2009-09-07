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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolTip;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;

public class DcFieldSelectorField extends JComponent implements IComponent {

    private Map<DcField, JCheckBox> componentMap = new HashMap<DcField, JCheckBox>();
    private final int module;

    public DcFieldSelectorField(int module) {
        this.module = module;
        buildComponent();
    }

    public Object getValue() {
        return getSelectedFields();
    }

    public void setValue(Object o) {
        if (o instanceof int[]) {
            setSelectedFields((int[]) o);
        }
    }
    
    public void setEditable(boolean b) {
        for (JCheckBox cb : componentMap.values()) 
            cb.setEnabled(b);
    }
    
    public void clear() {
        componentMap.clear();
        componentMap = null;
    }    

    public void invertSelection() {
        for (DcField field : componentMap.keySet()) {
            JCheckBox checkBox = componentMap.get(field);
            checkBox.setSelected(!checkBox.isSelected());
        }
    }
    
    public void selectAll() {
        for (DcField field : componentMap.keySet())
            componentMap.get(field).setSelected(true);
    }

    public void unselectAll() {
        for (DcField field : componentMap.keySet())
            componentMap.get(field).setSelected(false);
    }

    public int[] getSelectedFieldIndices() {
        Collection<DcField> fields = getSelectedFields();
        int[] indices = new int[fields.size()];
        int counter = 0;
        for (DcField field : fields)
            indices[counter++] = field.getIndex();
        
        return indices;
    }    

    public Collection<DcField> getSelectedFields() {
        Collection<DcField> fields = new ArrayList<DcField>();
        for (DcField field : componentMap.keySet()) {
            JCheckBox checkBox = componentMap.get(field);
            if (checkBox.isSelected())
                fields.add(field);
        }
        return fields;
    }

    public void setSelectedFields(int[] fields) {
        for (DcField field : componentMap.keySet()) {
            JCheckBox checkBox = componentMap.get(field);
            if (fields.length == 0) {
                checkBox.setSelected(true);
            } else {
                for (int j = 0; j < fields.length; j++) {
                    if (field.getIndex() == fields[j])
                        checkBox.setSelected(true);
                }
            }
        }
    }

    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
    
    private void buildComponent() {
        setLayout(Layout.getGBL());

        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());

        DcObject dco = DcModules.get(module).getDcObject();

        int x = 0;
        int y = 0;

        for (DcField field : dco.getFields()) {
            if (field.getIndex() != DcObject._ID) {
                JCheckBox checkBox = ComponentFactory.getCheckBox(field.getLabel());
                componentMap.put(field, checkBox);
    
                panel.add(checkBox, Layout.getGBC(x, y++, 1, 1, 1.0, 1.0
                         ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                          new Insets( 0, 10, 0, 0), 0, 0));
    
                if (y == 12) {
                    x++;
                    y = 0;
                }
            }
        }

        add(panel, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                  ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                   new Insets( 0, 0, 0, 0), 0, 0));
    }
    
    public void refresh() {}
}
