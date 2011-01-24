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

package net.datacrow.console.windows;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.views.View;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcSwingUtilities;

public class FindReplaceDialog extends DcFrame implements ActionListener {

    private JButton buttonApply;
    private JButton buttonClose;

    private DcModule module;
    private View view;

    private JTextField txtFind = ComponentFactory.getShortTextField(255);
    private JTextField txtReplace = ComponentFactory.getShortTextField(255);
    private JComboBox cbFields = ComponentFactory.getComboBox();

    public FindReplaceDialog(View view) {

        super(DcResources.getText("lblFindReplace"), IconLibrary._icoUpdateAll);
        
        this.view = view;
        this.module = view.getModule();

        setHelpIndex("dc.tools.findreplace");

        buildDialog(module);

        setSize(DcSettings.getDimension(DcRepository.Settings.stFindReplaceDialogSize));
        setCenteredLocation();
    }

    private void replace() {
        final DcField field = (DcField) cbFields.getSelectedItem();
        DataFilter df = new DataFilter(module.getIndex());
        df.addEntry(new DataFilterEntry(module.getIndex(), field.getIndex(), Operator.CONTAINS, txtFind.getText()));
        
        Collection<Integer> include = new ArrayList<Integer>();
        include.add(field.getIndex());
        
        if (txtFind.getText().length() == 0) {
            DcSwingUtilities.displayWarningMessage(DcResources.getText("msgEnterValueForFind"));
            return;
        }
        
        int[] fields =  module.getMinimalFields(include);
        FindReplaceTaskDialog dlg = new FindReplaceTaskDialog(
                this, view, DataManager.get(df, fields), fields, field.getIndex(), 
                txtFind.getText(), txtReplace.getText());
        dlg.setVisible(true);
    }

    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stFindReplaceDialogSize, getSize());
        
        buttonApply = null;
        buttonClose = null;
        view = null;
        module = null;
        if (cbFields != null) cbFields.removeAllItems();
        cbFields = null;
        txtFind = null;
        view = null;
        
        super.close();
    }

    private void buildDialog(DcModule module) {
        //**********************************************************
        //Input panel
        //**********************************************************
        JPanel panelInput = new JPanel();
        panelInput.setLayout(Layout.getGBL());
        
        for (DcField field : module.getFields()) {
            if (    field.getValueType() == DcRepository.ValueTypes._STRING && 
                    field.isEnabled() && 
                   !field.isReadOnly() &&
                   (field.getFieldType() == ComponentFactory._SHORTTEXTFIELD ||
                    field.getFieldType() == ComponentFactory._LONGTEXTFIELD))
                cbFields.addItem(field);
        }

        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblField")), Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        panelInput.add(cbFields, Layout.getGBC(1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblFind")), Layout.getGBC(0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        panelInput.add(txtFind, Layout.getGBC(1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblReplacement")), Layout.getGBC(0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        panelInput.add(txtReplace, Layout.getGBC(1, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        
        //**********************************************************
        //Action panel
        //**********************************************************
        JPanel panelActions = new JPanel();

        buttonApply = ComponentFactory.getButton(DcResources.getText("lblApply"));
        buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));

        buttonApply.addActionListener(this);
        buttonApply.setActionCommand("updateAll");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        panelActions.add(buttonApply);
        panelActions.add(buttonClose);
        
        //**********************************************************
        //Main panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());
        this.getContentPane().add(panelInput  ,Layout.getGBC(0, 0, 1, 1, 10.0, 10.0
                                              ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                               new Insets(5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelActions,Layout.getGBC(0, 2, 1, 1, 1.0, 1.0
                                              ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));

        pack();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("updateAll"))
            replace();
    }  
}
