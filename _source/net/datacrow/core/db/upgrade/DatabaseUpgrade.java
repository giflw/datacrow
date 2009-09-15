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

package net.datacrow.core.db.upgrade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.windows.UpgradeDialog;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DcRepository;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.db.Query;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.MappingModule;
import net.datacrow.core.modules.TemplateModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcProperty;
import net.datacrow.core.objects.helpers.Movie;
import net.datacrow.core.settings.Setting;
import net.datacrow.core.settings.SettingsGroup;
import net.datacrow.util.StringUtils;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * If possible, perform upgrades / changes in the DataManagerConversion class!
 * 
 * Converts the current database before the actual module tables are created / updated.
 * This means that the code here defies workflow logic and is strictly to be used for
 * table conversions and migration out of the scope of the normal module upgrade code.
 * 
 * The automatic database correction script runs after this manual upgrade.
 * 
 * @author Robert Jan van der Waals
 */
public class DatabaseUpgrade {
    
private static Logger logger = Logger.getLogger(DatabaseUpgrade.class.getName());
    
    private static final String _SETTING_SEPARATOR_CHAR = "separator_char";
    private static final String _SETTING_SAFEGUARD_COLUMNS = "safeguard_columns";


    public void start() {
//        try {
//            convertMappingModules();
//            convertMovieCountriesAndLanguages();
//        } catch (Exception e) {
//            String msg = e.getMessage() + ". Data conversion failed. " +
//                "Please restore your latest Backup and retry. Contact the developer " +
//                "if the error persists";
//            new MessageBox(msg, MessageBox._ERROR);
//            logger.error(msg, e);
//        }            
    }
    
    private void convertMappingModules() throws Exception {
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        
        for (DcModule module : DcModules.getAllModules()) {
            for (DcField field : module.getFields()) {
                DcModule mod = module instanceof TemplateModule ? ((TemplateModule) module).getTemplatedModule() : module; 
       
                int sourceIdx = field.getSourceModuleIdx();
                int derivedIdx = sourceIdx;

                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    try {
                        DcModule mappingMod = DcModules.get(DcModules.getMappingModIdx(module.getIndex(), derivedIdx));
                        String sql = "ALTER TABLE X_" + mod.getTableName() + "_" + DcModules.get(sourceIdx).getTableName() + " RENAME TO " + mappingMod.getTableName();
                        stmt.execute(sql);
                    } catch (SQLException e) {
                        if (e.getErrorCode() != -22)
                            throw new Exception(e);
                    }
                }
            }
        }
    }
    
    private void convertMovieCountriesAndLanguages() throws Exception {
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        
        SettingsGroup group = new SettingsGroup("1", "");
        group.add(new Setting(DcRepository.ValueTypes._STRING, _SETTING_SEPARATOR_CHAR, "", 
                ComponentFactory._SHORTTEXTFIELD, "", "Separator Character", true, true));
        group.add(new Setting(DcRepository.ValueTypes._BOOLEAN, _SETTING_SAFEGUARD_COLUMNS, "", 
                ComponentFactory._CHECKBOX, "", "Separator Character", false, true));
        
        UpgradeDialog dlg = new UpgradeDialog("The following text fields will be converted to list (aka. reference) fields:\n" +
                "Movie module: Language, Country, Subtitle Language, Audio Language.\n" +
                "Contact person module: Country.\n" + "If your fields only hold single value you do not have to set any of the settings below. If not, make" +
        		"sure to specify the correct separator character. The separator character will be used to split your values;\n" +
        		"For example if you have filled the fields like so \"value1, value2\" you should enter the comma character as the separtor character." +
        		"Additionally you can choose to keep a copy of the database field just in case the upgrade does not accomodate your usage of one of these fields. The fields will be rename to \"KEEP_\" appended " +
        		"by the original database field name", group);
        
        if (!dlg.isAffirmative()) {
            System.exit(0);
        }
        
        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            stmt.executeQuery("SELECT TOP 1 COUNTRY FROM MOVIE");
        } catch (Exception e) {
            // no conversion is needed
            stmt.close();
            conn.close();
            return;
        }
        
        stmt.close();
        conn.close();
        
        // create the cross tables.. 
        MappingModule movAudLan = new MappingModule(DcModules.get(DcModules._MOVIE), DcModules.get(DcModules._LANGUAGE), Movie._1_AUDIOLANGUAGE);
        MappingModule movSubLan = new MappingModule(DcModules.get(DcModules._MOVIE), DcModules.get(DcModules._LANGUAGE), Movie._2_SUBTITLELANGUAGE);
        MappingModule movLan = new MappingModule(DcModules.get(DcModules._MOVIE), DcModules.get(DcModules._LANGUAGE), Movie._D_LANGUAGE);
        MappingModule movCtr = new MappingModule(DcModules.get(DcModules._MOVIE), DcModules.get(DcModules._LANGUAGE), Movie._F_COUNTRY);        
        DcModule ctr = DcModules.get(DcModules._COUNTRY);
        DcModule lan = DcModules.get(DcModules._LANGUAGE);

        createTable(ctr);
        createTable(lan);
        createTable(movCtr);
        createTable(movLan);
        createTable(movAudLan);
        createTable(movSubLan);
        
        fill(DcModules.get(DcModules._MOVIE), ctr, movCtr, "COUNTRY", group.getSettings());
        fill(DcModules.get(DcModules._MOVIE), ctr, movLan, "LANGUAGE", group.getSettings());
        fill(DcModules.get(DcModules._MOVIE), ctr, movAudLan, "AUDIOLANGUAGE", group.getSettings());
        fill(DcModules.get(DcModules._MOVIE), ctr, movSubLan, "SUBTITLELANGUAGE", group.getSettings());
        
        
//        rs.close();
//        rs = stmt.executeQuery("SELECT ID, COUNTRY FROM PERSON WHERE COUNTRY IS NOT NULL");
//        while (rs.next()) {
//            String id = rs.getString(1);
//            String language = rs.getString(2);
//            if (!Utilities.isEmpty(language)) {
//                DcObject person = DataManager.getObject(DcModules._CONTACTPERSON, id);
//                DataManager.createReference(person, ContactPerson._K_COUNTRY, language);
//                person.saveUpdate(false, false);
//            }   
//        }        
//        
//        try {
//            stmt.close();
//            conn.close();
//            rs.close();
//        } catch (Exception e) {
//            logger.error(e, e);
//        }
    }
    
    private void fill(DcModule mod, DcModule refMod, DcModule mapMod, String oldName, Map<String, Setting> settings) throws Exception {
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        
        String sql = "SELECT ID, " + oldName + " FROM " + mod.getTableName() + " WHERE " + oldName + " IS NOT NULL";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            String id = rs.getString(1);
            String value = rs.getString(2);
            if (!Utilities.isEmpty(value)) {
                value = value.trim();

                value = value.toLowerCase();
                value = StringUtils.capitalize(value);

                // check if it already exists
                sql = "SELECT ID FROM " + refMod.getTableName() + " WHERE UPPER(" + refMod.getField(DcProperty._A_NAME) + ") = UPPER(?)";
                PreparedStatement ps = DatabaseManager.getAdminConnection().prepareStatement(sql);
                ps.setString(1, value);
                ResultSet rs2 = ps.executeQuery();

                String refID = null;
                while (rs.next())
                    refID = rs2.getString(1);
                
                // if not.. create the value
                if (refID == null) {
                    DcObject ref = refMod.getDcObject();
                    ref.setIDs();
                    ref.setValue(DcProperty._A_NAME, value);
                    PreparedStatement ps2 = new Query(Query._INSERT, ref, null, null).getQuery();
                    ps2.execute();
                    ps2.close();
                    
                    refID = ref.getID();
                }
                
                DcObject x = mapMod.getDcObject();
                x.setValue(DcMapping._A_PARENT_ID, id);
                x.setValue(DcMapping._B_REFERENCED_ID, refID);
                
                PreparedStatement ps3 = new Query(Query._INSERT, x, null, null).getQuery();
                ps3.execute();
                ps3.close();
                
                ps.close();
                rs2.close();
            }   
        }
        
        rs.close();
        conn.close();
        stmt.close();

    }
    
    private void createTable(DcModule module) throws SQLException {
        PreparedStatement ps = new Query(Query._CREATE, module.getDcObject(), null, null).getQuery();
        ps.execute();
        ps.close();
    }
}
