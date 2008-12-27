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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcCheckBox;
import net.datacrow.console.components.DcIconSelectField;
import net.datacrow.console.components.DcLongTextField;
import net.datacrow.console.components.DcShortTextField;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;
import net.datacrow.core.DataCrow;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.DcPropertyModule;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.StringUtils;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class PanelBasicInfo extends ModuleWizardPanel {

    private static Logger logger = Logger.getLogger(PanelBasicInfo.class.getName());
    
    private DcIconSelectField pic32;
    private DcIconSelectField pic16;
    
    private DcLongTextField textDesc;
    private DcShortTextField textName;
    private DcShortTextField textObjectName;
    private DcShortTextField textObjectNamePlural;
    private DcCheckBox checkCanBeLended;
    
    private boolean exists;
    
    public PanelBasicInfo(Wizard wizard, boolean exists) {
        super(wizard);
        this.exists = exists;
        build();
    }
    
    @Override
    public void setModule(XmlModule module) {
        super.setModule(module);
        
        textDesc.setText(module.getDescription());
        textName.setText(module.getLabel());
        textObjectName.setText(module.getObjectName());
        textObjectNamePlural.setText(module.getObjectNamePlural());
        checkCanBeLended.setSelected(module.canBeLend());
        
        if (module.getIcon16() != null)
            pic16.setIcon(new DcImageIcon(module.getIcon16()));
        
        if (module.getIcon32() != null)
            pic32.setIcon(new DcImageIcon(module.getIcon32()));
        
        checkCanBeLended.setVisible(!getModule().getModuleClass().equals(DcPropertyModule.class));
        if (getModule().getModuleClass().equals(DcPropertyModule.class))
            checkCanBeLended.setSelected(false);
    }
    
    private String saveIcon(DcImageIcon icon, String suffix) throws WizardException {
        XmlModule module = getModule();
        
        File file = null;
        
        try {
            file = File.createTempFile("module_" + StringUtils.normalize(module.getName()).replaceAll(" ", "") + suffix, ".png");
            byte[] bytes = icon.getBytes();
            
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            bos.flush();
            bos.close();
       } catch (Exception e) {
            throw new WizardException("Error while saving icon " + (file != null ? file.toString() : ""));
       }
       
       return (file != null ? file.toString() : null);
    }
    
    private String toTablename(String s) {
        String tablename = StringUtils.normalize(s.toLowerCase()).replaceAll(" ", "").toLowerCase();
        return Character.isDigit(tablename.charAt(0)) ? "module" + tablename : tablename ;
    }
    
    public Object apply() throws WizardException {
        XmlModule module = getModule();
        
        String name = textName.getText();
        String objectName = textObjectName.getText();
        checkValue(name, DcResources.getText("lblName"));
        
        checkValue(pic16.getIcon(), DcResources.getText("lblIcon"));
        checkValue(pic32.getIcon(), DcResources.getText("lblIcon"));
        checkValue(textObjectName.getText(), DcResources.getText("lblItemName"));
        checkValue(textObjectNamePlural.getText(), DcResources.getText("lblItemNamePlural"));

        String nameNormalized = !(getWizard() instanceof CreateModuleWizard) && 
        						 module.getTableName() != null && 
        					    !module.getTableName().equals("") ?
                                 module.getTableName() : toTablename(objectName);
                                
        if (getWizard() instanceof CreateModuleWizard && DcModules.get(nameNormalized) != null)
        	throw new WizardException(DcResources.getText("msgModuleNameNotUnique"));

        textName.setText(nameNormalized);
        module.setName(nameNormalized);
        module.setDescription(textDesc.getText());
        module.setEnabled(true);
        module.setObjectName(objectName);
        module.setObjectNamePlural(textObjectNamePlural.getText());
        module.setTableName(nameNormalized);
        module.setTableNameShort(nameNormalized);
        module.setLabel(name);
        module.setCanBeLend(checkCanBeLended.isSelected());
        module.setHasInsertView(true);
        module.setHasSearchView(true);

        ImageIcon icon16 = pic16.getIcon();
        ImageIcon icon32 = pic32.getIcon();

        try {
            if (pic16.isChanged() || getWizard() instanceof CreateModuleWizard) {
                module.setIcon16(Utilities.getBytes(icon16, DcImageIcon._TYPE_PNG));
                module.setIcon16Filename(saveIcon(new DcImageIcon(module.getIcon16()), "_small"));
            }
            
            if (pic32.isChanged() || getWizard() instanceof CreateModuleWizard) {
                module.setIcon32(Utilities.getBytes(icon32, DcImageIcon._TYPE_PNG));
                module.setIcon32Filename(saveIcon(new DcImageIcon(module.getIcon32()), ""));                
            }
        } catch (Exception e) {
        	logger.error("Error while reading the icons", e);
        	throw new WizardException("Could not store / use the selected icons");
        }

        return module;
    }
    
    private void checkValue(Object o, String desc) throws WizardException { 
        if (o == null || o.toString().trim().length() == 0)
            throw new WizardException(DcResources.getText("msgXNotEntered", desc));
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("msgBasicModuleInfo");
    }
    
    public void destroy() {
        pic32 = null;
        pic16 = null;
        textDesc = null;
        textName = null;
        textObjectName = null;
        textObjectNamePlural = null;
        checkCanBeLended = null;
    }    
    
    private void build() {
        // info panel
        setLayout(Layout.getGBL());
        
        textDesc = ComponentFactory.getLongTextField();
        JScrollPane scollDesc = new JScrollPane(textDesc);
        scollDesc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scollDesc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        textName = ComponentFactory.getShortTextField(25);
        textObjectName = ComponentFactory.getShortTextField(25);
        checkCanBeLended = ComponentFactory.getCheckBox(DcResources.getText("lblCanBeLended"));
        textObjectNamePlural = ComponentFactory.getShortTextField(25);
        
        if (!exists) {
            add(ComponentFactory.getLabel(DcResources.getText("lblName")), 
                    Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets( 5, 5, 5, 5), 0, 0));
            add(textName,         
                    Layout.getGBC(1, 0, 1, 1, 1.0, 1.0
                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets( 5, 5, 5, 5), 0, 0));
            add(ComponentFactory.getLabel(DcResources.getText("lblItemName")), 
                    Layout.getGBC(0, 1, 1, 1, 1.0, 1.0
                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets( 5, 5, 5, 5), 0, 0));  
            add(textObjectName,         
                    Layout.getGBC(1, 1, 1, 1, 1.0, 1.0
                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets( 5, 5, 5, 5), 0, 0));
            add(ComponentFactory.getLabel(DcResources.getText("lblItemNamePlural")), 
                    Layout.getGBC(0, 2, 1, 1, 1.0, 1.0
                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets( 5, 5, 5, 5), 0, 0));  
            add(textObjectNamePlural,         
                    Layout.getGBC(1, 2, 1, 1, 1.0, 1.0
                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets( 5, 5, 5, 5), 0, 0));
        }
        
        add(checkCanBeLended, 
                Layout.getGBC(1, 3, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets( 5, 5, 5, 5), 0, 0));
        
        
        add(ComponentFactory.getLabel(DcResources.getText("lblDescription")), 
                Layout.getGBC(0, 4, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets( 5, 5, 5, 5), 0, 0));  
        add(scollDesc,        
                Layout.getGBC(1, 4, 1, 1, 2.0, 2.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets( 5, 5, 5, 5), 0, 0));  

        
        
        pic16 = ComponentFactory.getIconSelectField(new DcImageIcon(DataCrow.installationDir + "icons_system/icon16.png"));
        pic32 = ComponentFactory.getIconSelectField(new DcImageIcon(DataCrow.installationDir + "icons_system/icon32.png"));
        add(ComponentFactory.getLabel(DcResources.getText("lblIcon16")), 
                     Layout.getGBC(0, 10, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                     new Insets(5, 5, 5, 5), 0, 0)); 
        add(pic16,   Layout.getGBC(1, 10, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0)); 
        add(ComponentFactory.getLabel(DcResources.getText("lblIcon32")), 
                     Layout.getGBC(0, 11, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                     new Insets(5, 5, 5, 5), 0, 0)); 
        add(pic32,   Layout.getGBC(1, 11, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0)); 
    }
}
