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

package net.datacrow.settings;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcMediaModule;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMediaObject;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.Container;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.settings.Setting;
import net.datacrow.core.settings.SettingsGroup;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.DcFieldDefinitions;
import net.datacrow.settings.definitions.QuickViewFieldDefinition;
import net.datacrow.settings.definitions.QuickViewFieldDefinitions;
import net.datacrow.settings.definitions.WebFieldDefinition;
import net.datacrow.settings.definitions.WebFieldDefinitions;

public class DcModuleSettings extends net.datacrow.settings.Settings {
    
    private String _General = DcResources.getText("lblGroupGeneral");
    
    public DcModuleSettings(DcModule module) {
        super();
        
        createSettings(module);
        createDefinitions(module);
        
        net.datacrow.core.settings.Settings settings = getSettings();

        // load the default settings (if available)
        String filename = module.getName().toLowerCase() + ".properties";
        File file = new File(DataCrow.moduleDir + filename);
        if (file.exists()) {
            settings.setSettingsFile(file);
            load();
        }
        
        settings.setSettingsFile(new File(DataCrow.baseDir + "/data/" + filename));
        load();
        
        correctSettings(module);
    }
    
    private void correctSettings(DcModule module) {
        int[] fields = getIntArray(DcRepository.ModuleSettings.stOnlineSearchFieldOverwriteSettings);
        Collection<Integer> correctedFields = new ArrayList<Integer>();
        for (int field : fields) {
            DcField fld = module.getField(field);
            if (fld != null)
                correctedFields.add(Integer.valueOf(field));
        }
        
        int i = 0;
        fields = new int[correctedFields.size()];
        for (Integer field : correctedFields)
            fields[i++] = field.intValue();
        
        set(DcRepository.ModuleSettings.stOnlineSearchFieldOverwriteSettings, fields);
    }

    @Override
    protected void createGroups() {
        _General = DcResources.getText("lblGroupGeneral");
        
        SettingsGroup generalGroup = new SettingsGroup(_General, "dc.Settings.GeneralSettings");
        getSettings().addGroup(_General, generalGroup);
    }
    
    private void createDefinitions(DcModule module) {
        
        DcFieldDefinitions fldDefinitions = new DcFieldDefinitions();
        WebFieldDefinitions webDefinitions = new WebFieldDefinitions();
        
        QuickViewFieldDefinitions qvDefinitions = new QuickViewFieldDefinitions();
        
        for (DcField field : module.getFields()) {
            fldDefinitions.add(new DcFieldDefinition(field.getIndex(), null, true, false, false));
            boolean enabled = field.getValueType() == DcRepository.ValueTypes._STRING ||
                              field.getValueType() == DcRepository.ValueTypes._LONG;
            
            qvDefinitions.add(new QuickViewFieldDefinition(field.getIndex(), enabled, DcResources.getText("lblHorizontal"), 0));
            webDefinitions.add(new WebFieldDefinition(field.getIndex(), 100, 0, enabled, field.getIndex() == DcMediaObject._A_TITLE, field.getIndex() == DcMediaObject._A_TITLE));
        }
        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DEFINITIONGROUP,
                            DcRepository.ModuleSettings.stWebFieldDefinitions,
                            webDefinitions,
                            -1,
                            "",
                            "",
                            false,
                            false));  

        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DEFINITIONGROUP,
                            DcRepository.ModuleSettings.stQuickViewFieldDefinitions,
                            qvDefinitions,
                            -1,
                            "",
                            "",
                            false,
                            false));           
        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DEFINITIONGROUP,
                            DcRepository.ModuleSettings.stFieldDefinitions,
                            fldDefinitions,
                            -1,
                            "",
                            "",
                            false,
                            false));        
    }
    
    protected void createSettings(DcModule module) {
        
        if (module.isFileBacked()) {
            getSettings().addSetting(_General,
                    new Setting(DcRepository.ValueTypes._STRING,
                                DcRepository.ModuleSettings.stFileRenamerPattern,
                                "",
                                -1,
                                "",
                                "",
                                false,
                                false));
        }
        
        if (module.getIndex() == DcModules._CONTAINER) {
            getSettings().addSetting(_General,
                    new Setting(DcRepository.ValueTypes._LONG,
                                DcRepository.ModuleSettings.stTreePanelShownItems,
                                DcModules._ITEM,
                                -1,
                                "",
                                "",
                                false,
                                false));
        }
        
        int[] order;
        if (module.getIndex() == DcModules._CONTAINER)
            order = new int[] {Container._A_NAME};
        else if (module.getIndex() == DcModules._ITEM)
            order = new int[] {DcObject._SYS_MODULE, DcObject._SYS_DISPLAYVALUE};
        else if (module instanceof DcMediaModule)
            order = new int[] {DcMediaObject._A_TITLE, DcMediaObject._C_YEAR, DcMediaObject._E_RATING};
        else
            order = new int[] {DcObject._SYS_DISPLAYVALUE};
        
        Collection<DcField> pics = new ArrayList<DcField>();
        for (DcField field : module.getFields()) {
        	if (field.getValueType() == DcRepository.ValueTypes._PICTURE)
        		pics.add(field);
        }
        
        int[] picFieldOrder = new int[pics.size()];
        int i = 0;
        for (DcField field : pics)
        	picFieldOrder[i++] = field.getIndex();

        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stCardViewPictureOrder,
                            picFieldOrder,
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stTableColumnOrder,
                            order,
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stCardViewItemDescription,
                            new int[] {DcObject._SYS_DISPLAYVALUE},
                            -1,
                            "",
                            "",
                            false,
                            false));        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stTitleCleanup,
                            "axxo,dvdrip,cdrip,dvd-rip,cd-rip",
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stImportLocalArt,
                            module.getIndex() == DcModules._SOFTWARE ? Boolean.FALSE : Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stImportLocalArtRecurse,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stImportLocalArtFrontKeywords,
                            "front,cover,case",
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stImportLocalArtBackKeywords,
                            "back",
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stImportLocalArtMediaKeywords,
                            "cd,dvd,media",
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stOnlineSearchFormSize,
                            new Dimension(700, 600),
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRINGARRAY,
                            DcRepository.ModuleSettings.stSearchOrder,
                            new String[] {},
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stItemFormSize,
                            new Dimension(700, 600),
                            -1,
                            "",
                            "",
                            false,
                            false));        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stFilterDialogSize,
                            new Dimension(700, 400),
                            -1,
                            "",
                            "",
                            false,
                            false));   
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stFieldSettingsDialogSize,
                            new Dimension(500, 300),
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stCardViewSettingsDialogSize,
                            new Dimension(500, 400),
                            -1,
                            "",
                            "",
                            false,
                            false)); 
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stTableViewSettingsDialogSize,
                            new Dimension(500, 400),
                            -1,
                            "",
                            "",
                            false,
                            false));        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stWebFieldSettingsDialogSize,
                            new Dimension(500, 300),
                            -1,
                            "",
                            "",
                            false,
                            false));        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stGroupedBy,
                            new int[] {},
                            -1,
                            "",
                            "",
                            false,
                            false));        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stQuickViewSettingsDialogSize,
                            new Dimension(500, 300),
                            -1,
                            "",
                            "",
                            false,
                            false));        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stLoanFormSize,
                            new Dimension(500, 500),
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
               new Setting(DcRepository.ValueTypes._DIMENSION,
                           DcRepository.ModuleSettings.stImportCDDialogSize,
                           new Dimension(600, 900),
                           -1,
                           "",
                           "",
                           false,
                           false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stFileImportDialogSize,
                            new Dimension(550, 600),
                            -1,
                            "",
                            "",
                            false,
                            false));        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stSynchronizerDialogSize,
                            new Dimension(550, 600),
                            -1,
                            "",
                            "",
                            false,
                            false));  
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stFieldSettingsDialogSize,
                            new Dimension(500, 300),
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stOnlineSearchFormSize,
                            new Dimension(700, 600),
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stSimpleItemViewSize,
                            new Dimension(450, 550),
                            -1,
                            "",
                            "",
                            false,
                            false));        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stSimpleItemFormSize,
                            new Dimension(450, 550),
                            -1,
                            "",
                            "",
                            false,
                            false));  
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stOnlineSearchFieldSettingsDialogSize,
                            new Dimension(600, 450),
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.ModuleSettings.stQuickFilterDefaultField,
                            Long.valueOf(DcObject._ID),
                            -1,
                            "",
                            "",
                            false,
                            false));           
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stAutoCreateSubItems,
                            true,
                            ComponentFactory._CHECKBOX,
                            DcResources.getText("tpAutoCreateMissingItems"),
                            DcResources.getText("lblAutoCreateMissingItems"),
                            false,
                            false));     
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stOnlineSearchSubItems,
                            false,
                            ComponentFactory._CHECKBOX,
                            DcResources.getText("tpOnlineSearchSubItems"),
                            DcResources.getText("lblOnlineSearchSubItems"),
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stOnlineSearchDefaultServer,
                            null,
                            ComponentFactory._SHORTTEXTFIELD,
                            "",
                            "",
                            false,
                            false));        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stOnlineSearchDefaultRegion,
                            null,
                            ComponentFactory._SHORTTEXTFIELD,
                            "",
                            "",
                            false,
                            false));        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stOnlineSearchDefaultMode,
                            null,
                            ComponentFactory._SHORTTEXTFIELD,
                            "",
                            "",
                            false,
                            false));        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stAutoAddPerfectMatch,
                            false,
                            ComponentFactory._CHECKBOX,
                            DcResources.getText("tpAutoAddPerfectMatch"),
                            DcResources.getText("lblAutoAddPerfectMatch"),
                            false,
                            false)); 
        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stEnabled,
                            true,
                            -1,
                            "",
                            "",
                            false,
                            false)); 
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stOnlineSearchFieldSettingsDialogSize,
                            new Dimension(600, 450),
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stOnlineSearchOverwrite,
                            false,
                            -1,
                            "",
                            "",
                            false,
                            false));        
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stOnlineSearchFieldOverwriteSettings,
                            new int[] {},
                            -1,
                            "",
                            "",
                            false,
                            false));
        getSettings().addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stOnlineSearchRetrievedFields,
                            module.getFieldIndices(),
                            -1,
                            "",
                            "",
                            false,
                            false));
    }
}
