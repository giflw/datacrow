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

package net.datacrow.console.wizards.module;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcCheckBox;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.DcNumberField;
import net.datacrow.console.components.DcShortTextField;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.xml.XmlField;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.StringUtils;

public class DefineFieldDialog extends DcDialog implements ActionListener {
    
    private boolean canHaveReferences;
    
    private DcShortTextField textName;
    private DcCheckBox checkSearchable;
    private DcCheckBox checkTechinfo;
    private DcNumberField numberMaxLength;
    
    private JComboBox comboReference;
    private JComboBox comboFieldType;
    
    private Collection<String> excludedNames;
    private XmlField field;

    boolean existingField = true;
    
    public DefineFieldDialog(Wizard parent,
                             XmlField oldField,
                             Collection<String> excludedNames, 
                             boolean canHaveReferences) {
        
        super(parent);
        
        this.canHaveReferences = canHaveReferences;
        this.setModal(true);
        this.excludedNames = excludedNames;
        this.setTitle(DcResources.getText("lblDefineField"));
        this.field = oldField;
        this.existingField = parent instanceof AlterModuleWizard;
        
        build();
        applyField();
        
        setResizable(false);
        pack();
        setSize(new Dimension(400,300));
        setCenteredLocation();
    }

    private void applyField() {
        
        if (field == null) return;

        textName.setText(field.getName());
        
        checkSearchable.setSelected(field.isSearchable());
        checkTechinfo.setSelected(field.isTechinfo());
        numberMaxLength.setValue(field.getMaximumLength());
        
        for (int i = 0; i < comboFieldType.getItemCount(); i++) {
            FieldType ft = (FieldType) comboFieldType.getItemAt(i);
            if (ft.getIndex() == field.getFieldType()) {
                comboFieldType.setSelectedItem(ft);
                break;
            }
        }

        if (field.getFieldType() == ComponentFactory._REFERENCESFIELD ||
            field.getFieldType() == ComponentFactory._REFERENCEFIELD) {
            
            for (int i = 0; i < comboReference.getItemCount(); i++) {
                DcModule m = (DcModule) comboReference.getItemAt(i);
                if (m.getIndex() == field.getModuleReference())
                    comboReference.setSelectedIndex(i);
            }
        }
        
        if (existingField)
            textName.setEditable(false);
    }
    
    public XmlField getField() {
        return field;
    }
    
    private void createField() {
        field =  new XmlField();
        
        String name = textName.getText();
        
        try {
            checkValue(name, DcResources.getText("lblName"));
            
            if (numberMaxLength.isEnabled())
                checkValue(name, DcResources.getText("lblMaxTextLength"));
            
            String column = StringUtils.normalize(name).replaceAll(" ", "").replaceAll("[\\-]", "");
            
            if (!existingField) {
                for (String excludedName : excludedNames) {
                    if (excludedName != null && 
                        excludedName.toLowerCase().equals(column.toLowerCase()))
                        throw new WizardException(DcResources.getText("msgFieldWithSameNameExists"));
                }
            }
            
            field.setColumn(column);
            field.setName(name);
            field.setSearchable(checkSearchable.isSelected());
            field.setTechinfo(checkTechinfo.isSelected());
            
            field.setMaximumLength(numberMaxLength.getValue() == null ? 255 : 
            						((Long) numberMaxLength.getValue()).intValue());
            
            FieldType ft = (FieldType) comboFieldType.getSelectedItem();
            if (ft.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                ft.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                
                DcModule m = (DcModule) comboReference.getSelectedItem();
                field.setModuleReference(m.getIndex());
                
                if (m.getXmlModule() != null)
                    m.getXmlModule().setHasDependingModules(true);
            }

            field.setUiOnly(ft.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                            ft.getValueType() == DcRepository.ValueTypes._PICTURE);
            
            field.setValueType(ft.getValueType());
            field.setFieldType(ft.getIndex());
            field.setOverwritable(true);
            
            close();
            
        } catch (WizardException we) {
        	field = null;
            new MessageBox(we.getMessage(), MessageBox._WARNING);
        }
    }
    
    private void checkValue(String s, String desc) throws WizardException { 
        if (s == null || s.trim().length() == 0)
            throw new WizardException(DcResources.getText("msgXNotEntered", desc));
    }
    
    @Override
    public void close() {
        textName = null;
        checkSearchable = null;
        checkTechinfo = null;
        numberMaxLength = null;
        comboReference = null;
        comboFieldType = null;
        super.close();
    }
    
    public void clear() {
        field = null;
    }
    
    private void build() {
        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());
        
        textName = ComponentFactory.getShortTextField(255);
        checkSearchable = ComponentFactory.getCheckBox("");
        checkTechinfo = ComponentFactory.getCheckBox("");
        numberMaxLength = ComponentFactory.getNumberField();
        
        comboReference = ComponentFactory.getComboBox();
        comboFieldType = ComponentFactory.getComboBox();
        initializeTypes();
        
        panel.add(ComponentFactory.getLabel(DcResources.getText("lblName")),
                Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        panel.add(textName,
                Layout.getGBC(1, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        panel.add(ComponentFactory.getLabel(DcResources.getText("lblSearchable")),
                Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        panel.add(checkSearchable,
                Layout.getGBC(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        panel.add(ComponentFactory.getLabel(DcResources.getText("lblHoldsTechnicalInfo")),
                Layout.getGBC(0, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        panel.add(checkTechinfo,
                Layout.getGBC(1, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        panel.add(ComponentFactory.getLabel(DcResources.getText("lblMaxTextLength")),
                Layout.getGBC(0, 3, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        panel.add(numberMaxLength,
                Layout.getGBC(1, 3, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        panel.add(ComponentFactory.getLabel(DcResources.getText("lblFieldType")),
                Layout.getGBC(0, 4, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        panel.add(comboFieldType,
                Layout.getGBC(1, 4, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        
        if (canHaveReferences) {
            panel.add(ComponentFactory.getLabel(DcResources.getText("lblReferencedModule")),
                    Layout.getGBC(0, 5, 1, 1, 1.0, 1.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 0), 0, 0));
            panel.add(comboReference,
                    Layout.getGBC(1, 5, 1, 1, 1.0, 1.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 0, 0), 0, 0));
            
            List<DcModule> modules = new ArrayList<DcModule>();
            modules.addAll(DcModules.getPropertyBaseModules());
            for (DcModule module : DcModules.getModules()) {
                if (  module.isTopModule() && !module.hasDependingModules() &&
                     !module.isParentModule() && !module.isChildModule() &&
                     !DcModules.isUsedInMapping(module.getIndex()) && 
                      DcModules.getReferencingModules(module.getIndex()).size() == 0)
                    modules.add(module);
            }

            Collections.sort(modules, new ModuleComparator());
            for (DcModule module : modules)
                comboReference.addItem(module);
        }

        comboFieldType.addActionListener(this);
        comboFieldType.setActionCommand("fieldTypeSelected");
        comboFieldType.setSelectedIndex(0);
        
        JPanel panelActions = new JPanel();
        panelActions.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        JButton buttonOk = ComponentFactory.getButton(DcResources.getText("lblOK"));
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        panelActions.add(buttonOk);
        panelActions.add(buttonClose);
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        buttonOk.addActionListener(this);
        buttonOk.setActionCommand("createField");
        
        getContentPane().setLayout(Layout.getGBL());
        add(panel, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        add(panelActions, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
    }
    
    private void initializeTypes() {
        List<FieldType> fieldTypes = new ArrayList<FieldType>();
        fieldTypes.add(new FieldType(ComponentFactory._CHECKBOX, 
                                     DcRepository.ValueTypes._BOOLEAN, 
                                     DcResources.getText("lblCheckbox")));
        fieldTypes.add(new FieldType(ComponentFactory._DATEFIELD, 
                                     DcRepository.ValueTypes._DATE, 
                                     DcResources.getText("lblDateField")));
        fieldTypes.add(new FieldType(ComponentFactory._FILEFIELD, 
                                     DcRepository.ValueTypes._STRING, 
                                     DcResources.getText("lblFileField")));
        fieldTypes.add(new FieldType(ComponentFactory._FILELAUNCHFIELD, 
                                     DcRepository.ValueTypes._STRING, 
                                     DcResources.getText("lblFileLauncherField")));
        fieldTypes.add(new FieldType(ComponentFactory._LONGTEXTFIELD, 
                                     DcRepository.ValueTypes._STRING, 
                                     DcResources.getText("lblLongTextField")));
        fieldTypes.add(new FieldType(ComponentFactory._NUMBERFIELD, 
                                     DcRepository.ValueTypes._LONG, 
                                     DcResources.getText("lblNumberField")));
        fieldTypes.add(new FieldType(ComponentFactory._PICTUREFIELD, 
                                     DcRepository.ValueTypes._PICTURE, 
                                     DcResources.getText("lblPictureField")));
        fieldTypes.add(new FieldType(ComponentFactory._RATINGCOMBOBOX, 
                                     DcRepository.ValueTypes._LONG, 
                                     DcResources.getText("lblRatingField")));
        fieldTypes.add(new FieldType(ComponentFactory._SHORTTEXTFIELD, 
                                     DcRepository.ValueTypes._STRING, 
                                     DcResources.getText("lblShortTextField")));
        fieldTypes.add(new FieldType(ComponentFactory._URLFIELD, 
                                     DcRepository.ValueTypes._STRING, 
                                     DcResources.getText("lblUrlField")));
        fieldTypes.add(new FieldType(ComponentFactory._DECIMALFIELD, 
                                     DcRepository.ValueTypes._DOUBLE, 
                                     DcResources.getText("lblDecimalField")));
        
        if (canHaveReferences) {
            fieldTypes.add(new FieldType(ComponentFactory._REFERENCESFIELD, 
                                         DcRepository.ValueTypes._DCOBJECTCOLLECTION, 
                                         DcResources.getText("lblMultiReferenceField")));
            fieldTypes.add(new FieldType(ComponentFactory._REFERENCEFIELD, 
                                         DcRepository.ValueTypes._DCOBJECTREFERENCE, 
                                         DcResources.getText("lblSingleReferenceField")));
        }
        
        Collections.sort(fieldTypes, new FieldTypeComparator());
        
        for (FieldType ft : fieldTypes) 
            comboFieldType.addItem(ft);
    }    
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("fieldTypeSelected")) {
            FieldType type = (FieldType) comboFieldType.getSelectedItem();
            
            if (type.getIndex() != ComponentFactory._SHORTTEXTFIELD &&
                type.getIndex() != ComponentFactory._NUMBERFIELD && 
                type.getIndex() != ComponentFactory._LONGTEXTFIELD) {
                numberMaxLength.setEnabled(false);
            } else {
                numberMaxLength.setEnabled(true);
            }
            
            if ((type.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                 type.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) &&
                !existingField) {
                
                comboReference.setEnabled(true);
            } else {
                comboReference.setEnabled(false);
            }
        } else if (e.getActionCommand().equals("close")) {
            close();
        } else if (e.getActionCommand().equals("createField")) {
            createField();
        }
    }
    
    private static class FieldType {
        private int index;
        private int valueType;
        private String name;
        
        public FieldType(int index, int valueType, String name) {
            this.index = index;
            this.name = name;
            this.valueType = valueType;
        }

        @Override
        public String toString() {
            return getName();
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public int getValueType() {
            return valueType;
        }
    }

    private static class FieldTypeComparator implements Comparator<FieldType>, Serializable {
        public int compare(FieldType ft1, FieldType ft2) {
            return ft1.getName().compareTo(ft2.getName());
        }
    }
    
    private static class ModuleComparator implements Comparator<DcModule>, Serializable {
        public int compare(DcModule m1, DcModule m2) {
            return m1.getLabel().compareTo(m2.getLabel());
        }
    }
}