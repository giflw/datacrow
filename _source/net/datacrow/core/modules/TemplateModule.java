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

package net.datacrow.core.modules;

import javax.swing.ImageIcon;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.windows.itemforms.DcMinimalisticItemView;
import net.datacrow.console.windows.itemforms.TemplateForm;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcTemplate;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.DcFieldDefinitions;

/**
 * The template module represents templates. Templates can be applied when creating new
 * items. The template module is based on the module which it is serving.
 *  
 * @author Robert Jan van der Waals
 */
public class TemplateModule extends DcModule {
    
    private static final long serialVersionUID = 5366008689695714729L;
    
    private TemplateForm form;
    private DcModule parent;
    
    /**
     * Creates a new instance based on the specified module. The fields of the provided module
     * are added to this module.
     * @param parent
     */
    public TemplateModule(DcModule parent) {
        super(parent.getIndex() + DcModules._TEMPLATE, 
              false, 
              DcResources.getText("sysTemplate"), 
              "",
              DcResources.getText("sysTemplate"),
              DcResources.getText("sysTemplatePlural"),
              parent.getTableName() + "_template", 
              parent.getTableShortName() + "temp");
        
        this.parent = parent;
        
        for (DcField field : parent.getFields()) {
            addField(new DcField(field.getIndex(), getIndex(), field.getLabel(), field.isUiOnly(),
                                 field.isEnabled(), field.isReadOnly(), field.isSearchable(), field.isTechnicalInfo(),
                                 field.getMaximumLength(), field.getFieldType(), field.getSourceModuleIdx(), field.getValueType(),
                                 field.getDatabaseFieldName()));
        }
    }
    
    /**
     * Retrieves the module this template module has been created for.
     */
    public DcModule getTemplatedModule() {
        return parent;
    }

    @Override
    public ImageIcon getIcon16() {
        return IconLibrary._icoTemplate;
    }

    @Override
    public ImageIcon getIcon32() {
        return IconLibrary._icoTemplate;
    }        
    
    /**
     * Creates the simple item  view.
     * @return
     */
    public DcMinimalisticItemView getForm() {
        initializeUI();
        return form;    
    }  
    
    /**
     * Creates a new template item.
     * @see DcTemplate
     */
    @Override
    public DcObject getDcObject() {
        return new DcTemplate(getIndex(), parent.getIndex());
    }    
    
    /**
     * The name of the objects.
     */
    @Override
    public String getObjectName() {
        return DcResources.getText("sysTemplate");
    }

    /**
     * The plural name of the objects.
     */
    @Override
    public String getObjectNamePlural() {
        return DcResources.getText("sysTemplatePlural");
    }
    
    /**
     * The field settings/definitions.
     * @see DcModule#getFieldDefinitions()
     */    
    @Override
    public DcFieldDefinitions getFieldDefinitions() {
        DcFieldDefinitions fds = parent.getFieldDefinitions();
        DcFieldDefinitions definitions = new DcFieldDefinitions();
        
        definitions.add(new DcFieldDefinition(DcTemplate._SYS_TEMPLATENAME, null, true, true, true));
        definitions.add(new DcFieldDefinition(DcTemplate._SYS_DEFAULT, null, true, true, true));
        
        for (DcFieldDefinition fd : fds.getDefinitions())
            definitions.add(new DcFieldDefinition(fd.getIndex(), fd.getLabel(), fd.isEnabled(), false, false));
        
        return definitions;
    }      

    /**
     * Initializes the standard fields.
     */
    @Override
    protected void initializeFields() {
        super.initializeFields();
        addField(new DcField(DcTemplate._SYS_TEMPLATENAME, getIndex(), "Template Name",
                             false, true, false, true, false,
                             255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                             "TemplateName"));
        addField(new DcField(DcTemplate._SYS_DEFAULT, getIndex(), "Default",
                             false, true, false, true, false,
                             255, ComponentFactory._CHECKBOX, getIndex(), DcRepository.ValueTypes._BOOLEAN,
                             "DefaultTemplate"));        
    }

    /**
     * Creates (of need be) the template form.
     * @see TemplateForm
     */
    @Override
    protected void initializeUI() {
        if (form == null)
            form = new TemplateForm(getIndex(), false);
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof TemplateModule ? ((TemplateModule) o).getIndex() == getIndex() : false);
    }     
}