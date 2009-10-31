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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.windows.DcReferencesDialog;
import net.datacrow.console.windows.itemforms.IItemFormListener;
import net.datacrow.console.windows.itemforms.ItemForm;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.MappingModule;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.Utilities;
import net.datacrow.util.comparators.DcObjectComparator;

public class DcReferencesField extends JComponent implements IComponent, ActionListener, IItemFormListener {

    private DcHtmlEditorPane fld = ComponentFactory.getHtmlEditorPane();

    private List<DcObject> references = new ArrayList<DcObject>();
    private JButton btOpen = ComponentFactory.getIconButton(IconLibrary._icoOpen);
    private JButton btCreate = ComponentFactory.getIconButton(IconLibrary._icoOpenNew);
    
    private final int mappingModIdx;
    
    public DcReferencesField(int mappingModIdx) {
        super();
        
        this.mappingModIdx = mappingModIdx;
        
        setLayout(Layout.getGBL());
        
        JScrollPane scrollIn = new JScrollPane(fld);
        scrollIn.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollIn.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollIn.setPreferredSize(new Dimension(350,50));
        ComponentFactory.setBorder(scrollIn);
        
        add(scrollIn, Layout.getGBC( 0, 0, 1, 2, 80.0, 80.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 0, 0, 0), 0, 0));        
        add(btCreate, Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));        
        add(btOpen,   Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));        
        
        btOpen.addActionListener(this);
        btOpen.setActionCommand("openDialog");

        btCreate.addActionListener(this);
        btCreate.setActionCommand("create");
    }
    
    public void setEditable(boolean b) {
        btOpen.setEnabled(b);
    }
    
    public Object getValue() {
        return references;
    }
    
    public void setValue(Collection<DcMapping> c) {
        references.clear();
        references.addAll(c);
        setDescription();
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object o) {
        setValue((Collection<DcMapping>) o);
    }
    
    public void clear() {
        fld = null;
        references.clear();
        references = null;
        btOpen = null;
        btCreate = null;
        removeAll();
    }
    
    private void create() {
        MappingModule mm = (MappingModule) DcModules.get(mappingModIdx);
        DcObject dco = DcModules.get(mm.getReferencedModIdx()).getItem();
        ItemForm itemForm = new ItemForm(false, false, dco, true);
        itemForm.setListener(this);
        itemForm.setVisible(true);
    }
        
    private void openDialog() {
        MappingModule mappingModule = (MappingModule) DcModules.get(mappingModIdx);
        DcReferencesDialog dlg = new DcReferencesDialog(references, mappingModule);
        
        dlg.setVisible(true);
        
        if (dlg.isSaved()) {
            references.clear();
            references.addAll(dlg.getDcObjects());
            setDescription();
        }
        dlg.dispose();        
    }    
    
    private void setDescription() {
        fld.setText("");
        if (references == null)
            return;
        
        List<DcObject> items = new ArrayList<DcObject>();
        for (DcObject dco : references) {
            if (dco.getModule().getType() == DcModule._TYPE_MAPPING_MODULE) {
                DcObject ref = ((DcMapping) dco).getReferencedObject();
                if (ref != null) items.add(ref);
            } else {
                if (dco != null) items.add(dco);
            }
        }
        
        if (items.size() == 0) return;
        
        int index = items.size() > 0 ? items.get(0).getDefaultSortFieldIdx() : 0;
        Collections.sort(items, new DcObjectComparator(index));

        StringBuffer desc = new StringBuffer("<html><body><div " + Utilities.getHtmlStyle() + ">");
        desc.append(fld.createLinks(items));
        desc.append("</div></body></html>");
        
        fld.setHtml(desc.toString());
        fld.setCaretPosition(0);
    }
    
    public void notifyItemSaved(DcObject dco) {
        DcObject mapping = DcModules.get(mappingModIdx).getItem();
        mapping.setValue(DcMapping._B_REFERENCED_ID, dco.getID());
        references.add(mapping);
        setDescription();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(DcSwingUtilities.setRenderingHint(g));
    }  

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("openDialog"))
            openDialog();
        else if (e.getActionCommand().equals("create"))
            create();        
    }
    
    public void refresh() {
        setDescription();
    }    
}