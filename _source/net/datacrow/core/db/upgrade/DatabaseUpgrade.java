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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.windows.LogForm;
import net.datacrow.console.windows.UpgradeDialog;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.windows.messageboxes.QuestionBox;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.Version;
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
import net.datacrow.core.objects.helpers.ContactPerson;
import net.datacrow.core.objects.helpers.ExternalReference;
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
    
    private static final String _SETTING_SEPARATOR_CHAR_LAN = "separator_char_lan";
    private static final String _SETTING_SEPARATOR_CHAR_CTR = "separator_char_ctr";

    private static final String _SETTING_SAFEGUARD_COLUMNS = "safeguard_columns";
    private static final String _SETTING_REMOVE_SRT = "remove_srt";
    private static final String _SETTING_CLEANUP_VALUES = "cleanup_values";

    public void start() {
        try {
            
            boolean upgraded = false;
            if (DatabaseManager.getVersion().isOlder(new Version(3, 5, 0, 0))) {
                upgraded |= convertMappingModules();
                upgraded |= convertMovieCountriesAndLanguages();
            }
            
            if (DatabaseManager.getVersion().isOlder(new Version(3, 6, 0, 0))) {
                upgraded |= convertExternalReferences();
            }
            
            if (upgraded) {
                MessageBox mb = new MessageBox("The upgrade was successful. Data Crow will now continue.", MessageBox._INFORMATION);
                mb.setVisible(true);
                LogForm.getInstance().setVisible(false);
                DataCrow.showSplashScreen(true);
            }
            
        } catch (Exception e) {
            String msg = e.getMessage() + ". Data conversion failed. " +
                "Please restore your latest Backup and retry. Contact the developer " +
                "if the error persists";
            new MessageBox(msg, MessageBox._ERROR);
            logger.error(msg, e);
        }            
    }
    
    private boolean convertExternalReferences() throws Exception {
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        
        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            stmt.executeQuery("SELECT TOP 1 ASIN FROM MOVIE");
            
            stmt.close();
            conn.close();
        } catch (Exception e) {
            // new database or ASIN field has already been removed.
            return false;
        }
        
        QuestionBox qb = new QuestionBox("A new functionality has become available; external references. The items currently present in yoru system " +
        		" will be examined and, if possible, the external ID will be extracted and stored separately. Furthermore will the ASIN fields be removed " +
        		" from the Software, Movie, Audio CD and Music Album modules and its values will be stored in the external references field. Continue?");
        
        qb.setVisible(true);
        
        if (!qb.isAffirmative())
            System.exit(0);
        
        LogForm.getInstance().setVisible(true);
        LogForm.getInstance().toFront();

        createTable(DcModules.get(DcModules._EXTERNALREFERENCE + DcModules._MOVIE));
        createTable(DcModules.get(DcModules._EXTERNALREFERENCE + DcModules._AUDIOCD));
        createTable(DcModules.get(DcModules._EXTERNALREFERENCE + DcModules._SOFTWARE));
        createTable(DcModules.get(DcModules._EXTERNALREFERENCE + DcModules._MUSICALBUM));
        
        Collection<MappingModule> modules = new ArrayList<MappingModule>();
        modules.add(new MappingModule(DcModules.get(DcModules._MOVIE), DcModules.get(DcModules._EXTERNALREFERENCE + DcModules._MOVIE), DcObject._SYS_EXTERNAL_REFERENCES));
        modules.add(new MappingModule(DcModules.get(DcModules._AUDIOCD), DcModules.get(DcModules._EXTERNALREFERENCE + DcModules._AUDIOCD), DcObject._SYS_EXTERNAL_REFERENCES));
        modules.add(new MappingModule(DcModules.get(DcModules._SOFTWARE), DcModules.get(DcModules._EXTERNALREFERENCE + DcModules._SOFTWARE), DcObject._SYS_EXTERNAL_REFERENCES));
        modules.add(new MappingModule(DcModules.get(DcModules._MUSICALBUM), DcModules.get(DcModules._EXTERNALREFERENCE + DcModules._MUSICALBUM), DcObject._SYS_EXTERNAL_REFERENCES));
    
        for (DcModule module : modules)
            createTable(module);

        for (MappingModule module : modules)
            migrateASIN(module);
        
        migrateDiscID(new MappingModule(DcModules.get(DcModules._MUSICALBUM), DcModules.get(DcModules._EXTERNALREFERENCE + DcModules._MUSICALBUM), DcObject._SYS_EXTERNAL_REFERENCES));
        migrateDiscID(new MappingModule(DcModules.get(DcModules._AUDIOCD), DcModules.get(DcModules._EXTERNALREFERENCE + DcModules._AUDIOCD), DcObject._SYS_EXTERNAL_REFERENCES));
        
        migrateServiceURL(DcModules.get(DcModules._MOVIE));
        migrateServiceURL(DcModules.get(DcModules._ACTOR));
        migrateServiceURL(DcModules.get(DcModules._DIRECTOR));
        migrateServiceURL(DcModules.get(DcModules._SOFTWARE));
        migrateServiceURL(DcModules.get(DcModules._DEVELOPER));
        migrateServiceURL(DcModules.get(DcModules._SOFTWAREPUBLISHER));
        migrateServiceURL(DcModules.get(DcModules._BOOK));
        migrateServiceURL(DcModules.get(DcModules._AUDIOCD));
        migrateServiceURL(DcModules.get(DcModules._MUSICALBUM));
        migrateServiceURL(DcModules.get(DcModules._MUSICARTIST));
        
        return true;
    }
    
    private void migrateServiceURL(DcModule module) throws Exception {
        
        logger.info("Extracting external IDs of type for module " + module.getLabel());
        
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        
        ResultSet rs = stmt.executeQuery("SELECT ID, " + module.getField(Movie._SYS_SERVICEURL).getDatabaseFieldName() + 
                " FROM " + module.getTableName() + " WHERE " + module.getField(Movie._SYS_SERVICEURL).getDatabaseFieldName() + " IS NOT NULL");
        
        String id;
        String url;
        
        String externalID = null;
        
        DcObject ref;
        DcObject x;
        
        createTable(DcModules.get(DcModules._EXTERNALREFERENCE + module.getIndex()));
        
        DcModule mm = new MappingModule(module, DcModules.get(DcModules._EXTERNALREFERENCE + module.getIndex()), DcObject._SYS_EXTERNAL_REFERENCES);
        
        createTable(mm);
        
        String type;
        while (rs.next()) {
            id = rs.getString(1);
            url = rs.getString(2);

            if (Utilities.isEmpty(url)) continue;
            
            String base = url.toLowerCase();
            type = base.indexOf("amazon") > -1 ? DcRepository.ExternalReferences._ASIN :
                   base.indexOf("imdb") > -1 ? DcRepository.ExternalReferences._IMDB :
                   base.indexOf("mobygames") > -1 ? DcRepository.ExternalReferences._MOBYGAMES :
                   base.indexOf("bol.com") > -1 ? DcRepository.ExternalReferences._BOL :
                   base.indexOf("barnesandnoble.com") > -1 ? DcRepository.ExternalReferences._BARNES_NOBLE :         
                   base.indexOf("www.mcu.es") > -1 ? DcRepository.ExternalReferences._MCU :
                   base.indexOf("musicbrainz") > -1 ? DcRepository.ExternalReferences._MUSICBRAINZ :
                   base.indexOf("musicbrainz") > -1 ? DcRepository.ExternalReferences._MUSICBRAINZ :    
                   base.indexOf("discogs.com") > -1 ? DcRepository.ExternalReferences._DISCOGS :  
                   base.indexOf("ofdb.de") > -1 ? DcRepository.ExternalReferences._OFDB :
                   "";

            if (type.equals(DcRepository.ExternalReferences._IMDB)) {
                externalID = url.substring(url.lastIndexOf("/") + 1);
                if (externalID.startsWith("tt") || externalID.startsWith("nm"))
                    externalID = externalID.endsWith("/") ? externalID.substring(0, externalID.length() - 1) : externalID;
                else 
                    continue;
            } else if (type.equals(DcRepository.ExternalReferences._ASIN)) {
                int idx = url.toLowerCase().indexOf("&itemid="); 
                
                if (idx == -1) continue;
                    
                externalID = url.substring(idx + 8);
                idx = externalID.indexOf("&");
                externalID = idx > -1 ? externalID.substring(0, idx) : externalID;
                
                if (externalID.length() > 12) continue;
            } else if (type.equals(DcRepository.ExternalReferences._OFDB) ||
                      (type.equals(DcRepository.ExternalReferences._MOBYGAMES) && module.getIndex() != DcModules._SOFTWARE)) {
                externalID = url.substring(url.lastIndexOf("/") + 1);
                externalID = externalID.endsWith("/") ? externalID.substring(0, externalID.length() - 1) : externalID;
            } else if (type.equals(DcRepository.ExternalReferences._MOBYGAMES) && module.getIndex() == DcModules._SOFTWARE) {
                externalID = url.substring(url.toLowerCase().lastIndexOf("mobygames.com/") + 14);
            } else if (type.equals(DcRepository.ExternalReferences._BOL)) {
                externalID = url.substring(0, url.lastIndexOf("/"));
                externalID = externalID.substring(externalID.lastIndexOf("/") + 1);
            } else if (type.equals(DcRepository.ExternalReferences._MCU)) {
                externalID = url.substring(url.toUpperCase().indexOf("&DOCN=") + 6);
            } else if (type.equals(DcRepository.ExternalReferences._BARNES_NOBLE)) {
                externalID = url.substring(url.toUpperCase().indexOf("&EAN=") + 5);
                externalID = externalID.indexOf("&") > 0 ? externalID.substring(0, externalID.indexOf("&")) : externalID;
            } else if (type.equals(DcRepository.ExternalReferences._DISCOGS)) {
                externalID = url.substring(url.toLowerCase().indexOf("release/") + 8);
            } else if (type.equals(DcRepository.ExternalReferences._MUSICBRAINZ) && module.getIndex() != DcModules._MUSICARTIST) {
                int idx = url.toLowerCase().indexOf("/release/") + 9;
                externalID = url.substring(idx);
                externalID = externalID.indexOf("?") > 0 ? externalID.substring(0, externalID.indexOf("?")) : externalID;
            } else if (type.equals(DcRepository.ExternalReferences._MUSICBRAINZ) && module.getIndex() == DcModules._MUSICARTIST) {
                int idx = url.toLowerCase().indexOf("/artist/") + 8;
                externalID = url.substring(idx);
                externalID = externalID.indexOf("?") > 0 ? externalID.substring(0, externalID.indexOf("?")) : externalID;
            }
            
            if (externalID != null) {
                
                PreparedStatement ps2 = conn.prepareStatement("SELECT * FROM " + DcModules.get(DcModules._EXTERNALREFERENCE + module.getIndex()).getTableName() + " WHERE EXTERNALID = ? AND EXTERNALIDTYPE = ?");
                ps2.setString(1, externalID);
                ps2.setString(2, type);
                
                ResultSet rs2 = ps2.executeQuery();
                boolean exists = false;
                while (rs2.next()) {
                    exists = true;
                }
                
                if (exists) {
                    rs2.close();
                    ps2.close();
                    continue;
                }
                
                ref = new ExternalReference(module.getIndex() + DcModules._EXTERNALREFERENCE);
                ref.setIDs();
                ref.setValue(ExternalReference._EXTERNAL_ID, externalID);
                ref.setValue(ExternalReference._EXTERNAL_ID_TYPE, type);
                PreparedStatement ps = new Query(Query._INSERT, ref, null, null).getQuery();
                ps.execute();
                ps.close();
                
                x = mm.getItem();
                x.setValue(DcMapping._A_PARENT_ID, id);
                x.setValue(DcMapping._B_REFERENCED_ID, ref.getID());
                ps = new Query(Query._INSERT, x, null, null).getQuery();
                ps.execute();
                ps.close();
            }
        }
        
        rs.close();
        conn.close();
        stmt.close();
    }
    
    
    private void migrateDiscID(MappingModule mapping) throws Exception {
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        
        DcModule module =  DcModules.get(mapping.getParentModIdx());
        String sql = "SELECT ID, DISCID FROM " + module.getTableName() + " WHERE DISCID IS NOT NULL";
        ResultSet rs = stmt.executeQuery(sql);
        
        PreparedStatement ps;
        while (rs.next()) {
            String ID = rs.getString(1);
            String ASIN = rs.getString(2).trim();
            if (!Utilities.isEmpty(ASIN)) {

                DcObject ref = new ExternalReference(module.getIndex() + DcModules._EXTERNALREFERENCE);
                ref.setIDs();
                ref.setValue(ExternalReference._EXTERNAL_ID, ASIN);
                ref.setValue(ExternalReference._EXTERNAL_ID_TYPE, DcRepository.ExternalReferences._DISCID);
                ps = new Query(Query._INSERT, ref, null, null).getQuery();
                ps.execute();
                ps.close();
                
                DcObject x = mapping.getItem();
                x.setValue(DcMapping._A_PARENT_ID, ID);
                x.setValue(DcMapping._B_REFERENCED_ID, ref.getID());
                ps = new Query(Query._INSERT, x, null, null).getQuery();
                ps.execute();
                ps.close();
                        
                logger.info("Created mapping for item with ID " + ID + " for module [" + module.getName() + "]");
            }   
        }
        
        sql = "ALTER TABLE " + module.getTableName() + " DROP COLUMN DISCID"; 
        stmt.execute(sql);
        
        rs.close();
        conn.close();
        stmt.close();
    }
    
    private void migrateASIN(MappingModule mapping) throws Exception {
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        
        DcModule module =  DcModules.get(mapping.getParentModIdx());
        String sql = "SELECT ID, ASIN FROM " + module.getTableName() + " WHERE ASIN IS NOT NULL";
        ResultSet rs = stmt.executeQuery(sql);
        
        PreparedStatement ps;
        while (rs.next()) {
            String ID = rs.getString(1);
            String ASIN = rs.getString(2).trim();
            if (!Utilities.isEmpty(ASIN)) {

                DcObject ref = new ExternalReference(module.getIndex() + DcModules._EXTERNALREFERENCE);
                ref.setIDs();
                ref.setValue(ExternalReference._EXTERNAL_ID, ASIN);
                ref.setValue(ExternalReference._EXTERNAL_ID_TYPE, DcRepository.ExternalReferences._ASIN);
                ps = new Query(Query._INSERT, ref, null, null).getQuery();
                ps.execute();
                ps.close();
                
                DcObject x = mapping.getItem();
                x.setValue(DcMapping._A_PARENT_ID, ID);
                x.setValue(DcMapping._B_REFERENCED_ID, ref.getID());
                ps = new Query(Query._INSERT, x, null, null).getQuery();
                ps.execute();
                ps.close();
                        
                logger.info("Created mapping for item with ID " + ID + " for module [" + module.getName() + "]");
            }   
        }
        
        sql = "ALTER TABLE " + module.getTableName() + " DROP COLUMN ASIN"; 
        stmt.execute(sql);
        
        rs.close();
        conn.close();
        stmt.close();
    }
    
    /**
     * Converts the mapping modules to new structure.
     * @throws Exception
     */
    private boolean convertMappingModules() throws Exception {
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        
        for (DcModule module : DcModules.getAllModules()) {
            for (DcField field : module.getFields()) {
                DcModule mod = module instanceof TemplateModule ? ((TemplateModule) module).getTemplatedModule() : module; 
       
                if (mod.isAbstract())
                    continue;
                
                int sourceIdx = field.getSourceModuleIdx();
                int derivedIdx = sourceIdx;

                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    try {
                        MappingModule mappingMod = (MappingModule) DcModules.get(DcModules.getMappingModIdx(module.getIndex(), derivedIdx, field.getIndex()));
                        mappingMod = mappingMod == null ? (MappingModule) DcModules.get(DcModules.getMappingModIdx(module.getIndex(), field.getReferenceIdx(), field.getIndex())) : mappingMod;
                        int referenceIdx = mappingMod.getReferencedModIdx();

                        String sql = "ALTER TABLE X_" + mod.getTableName() + "_" + DcModules.get(referenceIdx).getTableName() + " RENAME TO " + mappingMod.getTableName();
                        stmt.execute(sql);
                        
                    } catch (SQLException e) {
                        if (e.getErrorCode() != -22)
                            throw new Exception(e);
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean convertMovieCountriesAndLanguages() throws Exception {
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        
        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.createStatement();
            stmt.executeQuery("SELECT TOP 1 COUNTRY FROM MOVIE");
        } catch (Exception e) {
            // no conversion is needed
            stmt.close();
            conn.close();
            return false;
        }

        SettingsGroup group = new SettingsGroup("1", "");
        group.add(new Setting(DcRepository.ValueTypes._STRING, _SETTING_SEPARATOR_CHAR_LAN, "", 
                ComponentFactory._SHORTTEXTFIELD, "", "Separator Character for Languages", true, true));
        group.add(new Setting(DcRepository.ValueTypes._STRING, _SETTING_SEPARATOR_CHAR_CTR, "", 
                ComponentFactory._SHORTTEXTFIELD, "", "Separator Character for Countries", true, true));
        group.add(new Setting(DcRepository.ValueTypes._BOOLEAN, _SETTING_SAFEGUARD_COLUMNS, Boolean.FALSE, 
                ComponentFactory._CHECKBOX, "", "Safeguard the columns (rename them to _KEEP_ + orginal name)", false, true));
        group.add(new Setting(DcRepository.ValueTypes._BOOLEAN, _SETTING_REMOVE_SRT, Boolean.TRUE, 
                ComponentFactory._CHECKBOX, "", "Remove the .SRT (and the likes) value from the languages", false, true));
        group.add(new Setting(DcRepository.ValueTypes._BOOLEAN, _SETTING_CLEANUP_VALUES, Boolean.TRUE, 
                ComponentFactory._CHECKBOX, "", "Make the values a bit nicer (make lower case and start with upper case)", false, true));

        final UpgradeDialog dlg = new UpgradeDialog("The following text fields will be converted to list (aka. reference) fields:\n" +
                "Movie module: Language, Country, Subtitle Language, Audio Language.\n" +
                "Contact person module: Country.\n" + "If your fields only hold single value you do not have to set any of the settings below. If not, make" +
                "sure to specify the correct separator character. The separator character will be used to split your values;\n" +
                "For example if you have filled the fields like so \"value1, value2\" you should enter the comma character as the separtor character." +
                "Additionally you can choose to keep a copy of the database field just in case the upgrade does not accomodate your usage of one of these fields. The fields will be rename to \"KEEP_\" appended " +
                "by the original database field name", group);
        
        dlg.setVisible(true);
        
        if (!dlg.isAffirmative()) {
            System.exit(0);
        }        
        
        stmt.close();
        conn.close();
        
        // create the cross tables..
        DcModule ctr = DcModules.get(DcModules._COUNTRY);
        DcModule lan = DcModules.get(DcModules._LANGUAGE);
        
        MappingModule movAudLan = new MappingModule(DcModules.get(DcModules._MOVIE), lan, Movie._1_AUDIOLANGUAGE);
        MappingModule movSubLan = new MappingModule(DcModules.get(DcModules._MOVIE), lan, Movie._2_SUBTITLELANGUAGE);
        MappingModule movLan = new MappingModule(DcModules.get(DcModules._MOVIE), lan, Movie._D_LANGUAGE);
        MappingModule movCtr = new MappingModule(DcModules.get(DcModules._MOVIE), ctr, Movie._F_COUNTRY);        

        LogForm.getInstance().setVisible(true);
        
        prepareTable(DcModules.get(DcModules._CONTACTPERSON), "COUNTRY", ContactPerson._K_COUNTRY);
        createTable(ctr);
        createTable(lan);
        createTable(movCtr);
        createTable(movLan);
        createTable(movAudLan);
        createTable(movSubLan);
        
        String sepLan = group.getSettings().get(_SETTING_SEPARATOR_CHAR_LAN).getValueAsString();
        String sepCtr= group.getSettings().get(_SETTING_SEPARATOR_CHAR_CTR).getValueAsString();
        
        fill(DcModules.get(DcModules._MOVIE), ctr, movCtr, Movie._F_COUNTRY, "COUNTRY", group.getSettings(), sepCtr);
        
        fill(DcModules.get(DcModules._CONTACTPERSON), ctr, null, ContactPerson._K_COUNTRY, "_KEEP_COUNTRY", group.getSettings(), null);
        
        fill(DcModules.get(DcModules._MOVIE), lan, movLan, Movie._D_LANGUAGE, "LANGUAGE", group.getSettings(), sepLan);
        fill(DcModules.get(DcModules._MOVIE), lan, movAudLan, Movie._1_AUDIOLANGUAGE,  "AUDIOLANGUAGE", group.getSettings(), sepLan);
        fill(DcModules.get(DcModules._MOVIE), lan, movSubLan, Movie._2_SUBTITLELANGUAGE, "SUBTITLELANGUAGE", group.getSettings(), sepLan);
        
        return true;
    }
    
    /**
     * Creates (a) reference(s) based on text field values.
     * 
     * @param mod The main module
     * @param refMod The referenced module (a property)
     * @param mapMod The mapping module. Set this to null when creating single reference values.
     * @param fieldIdx The field index (of the main module). Is used when dealing with single references/
     * @param oldName The old column name.
     * @param settings The settings which will be used to base decisions on.
     * 
     * @throws Exception A generic exception. 
     */
    private void fill(DcModule mod, DcModule refMod, DcModule mapMod, int fieldIdx, String oldName, Map<String, Setting> settings, String sep) throws Exception {
        
        boolean multiRef = mapMod != null;
        
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        
        String sql = "SELECT ID, " + oldName + " FROM " + mod.getTableName() + " WHERE " + oldName + " IS NOT NULL";
        ResultSet rs = stmt.executeQuery(sql);
        
        boolean cleanup = Boolean.valueOf(settings.get(_SETTING_CLEANUP_VALUES).getValueAsString());
        boolean removeSrt = Boolean.valueOf(settings.get(_SETTING_REMOVE_SRT).getValueAsString());
        boolean safeguard = Boolean.valueOf(settings.get(_SETTING_SAFEGUARD_COLUMNS).getValueAsString());
        
        while (rs.next()) {
            String ID = rs.getString(1);
            String s = rs.getString(2);
            if (!Utilities.isEmpty(s)) {
                s = s.trim();

                Collection<String> values = new ArrayList<String>();
                if (multiRef && (sep != null && sep.length() > 0) && s.indexOf(sep) != -1) {
                    StringTokenizer st = new StringTokenizer(s, sep);
                    while (st.hasMoreElements())
                        values.add((String) st.nextElement());
                } else {
                    values.add(s);
                }
                
                for (String value : values) {
                    
                    if (removeSrt && value.toUpperCase().contains(".SRT"))
                        continue;
                    
                    value = value.trim();
                    
                    if (value.length() == 0)
                        continue;
                    
                    // cleanup (length check to safeguard abbreviations like US and USA)
                    if (cleanup && value.length() > 3) {
                        value = value.toLowerCase();
                        value = StringUtils.capitalize(value);
                    }
    
                    String refID = getReferenceID(value, refMod);
                    
                    if (multiRef) {
                        // Create a mapping
                        DcObject x = mapMod.getItem();
                        x.setValue(DcMapping._A_PARENT_ID, ID);
                        x.setValue(DcMapping._B_REFERENCED_ID, refID);
                        
                        PreparedStatement psInsertMapping = new Query(Query._INSERT, x, null, null).getQuery();
                        psInsertMapping.execute();
                        psInsertMapping.close();
                        
                        logger.info("Created mapping for item with ID " + ID + " for module [" + mod.getName() + "]");
                        
                    } else {
                        sql = "UPDATE " + mod.getTableName() + " SET " + mod.getField(fieldIdx).getDatabaseFieldName() + " = ? WHERE ID = ?";
                        PreparedStatement psUpdate = conn.prepareStatement(sql);
                        psUpdate.setString(1, refID);
                        psUpdate.setString(2, ID);
                        psUpdate.execute();
                        psUpdate.close();
                        
                        logger.info("Updated item with ID " + ID + " for module [" + mod.getName() + "]");
                    }
                }
            }   
        }
        
        if (safeguard && multiRef) {
            // Renames the column. This one will never be removed
            sql = "ALTER TABLE " + mod.getTableName() + " ALTER COLUMN " + oldName + " RENAME TO _KEEP_" + oldName; 
            stmt.execute(sql);
        } else if (!safeguard) {
            // In case of a single reference the column was already renamed. No safeguard? drop column.
            // For multi reference its obvious what has to been done:
            sql = "ALTER TABLE " + mod.getTableName() + " DROP COLUMN " + oldName; 
            stmt.execute(sql);
        }
        
        rs.close();
        conn.close();
        stmt.close();
    }
    
    private String getReferenceID(String name, DcModule refMod) throws Exception {
        // check if it already exists
        String sql = "SELECT ID FROM " + refMod.getTableName() + " WHERE UPPER(" + refMod.getField(DcProperty._A_NAME) + ") = UPPER(?)";
        PreparedStatement psRefID = DatabaseManager.getAdminConnection().prepareStatement(sql);
        psRefID.setString(1, name);
        ResultSet rs = psRefID.executeQuery();

        String refID = null;
        while (rs.next())
            refID = rs.getString(1);
        
        // if not.. create the value
        if (refID == null) {
            DcObject ref = refMod.getItem();
            ref.setIDs();
            ref.setValue(DcProperty._A_NAME, name);
            PreparedStatement psCreateRef = new Query(Query._INSERT, ref, null, null).getQuery();
            psCreateRef.execute();
            psCreateRef.close();
            refID = ref.getID();
            logger.info("Created [" + name + "] for [" +  refMod.getName() + "]");
        }
        return refID;
    }
    
    /**
     * Prepares the module for the single reference update
     * @param module
     * @throws Exception
     */
    private void prepareTable(DcModule module, String oldColumn, int fieldIdx ) throws Exception {
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        
        String sql = "ALTER TABLE " + module.getTableName() + " ALTER COLUMN " + oldColumn + " RENAME TO " + " _KEEP_" + oldColumn;
        stmt.execute(sql);
        
        sql = "ALTER TABLE " + module.getTableName() + " ADD COLUMN " + module.getField(fieldIdx).getDatabaseFieldName() + 
              " " + module.getField(fieldIdx).getDataBaseFieldType();
        stmt.execute(sql);
    }
    
    /**
     * Creates the table. Only creates the table when it does not exist.
     * @param module
     * @throws Exception Any other exception then the 'table already exists exception'
     */
    private void createTable(DcModule module) throws Exception {
        try {
            PreparedStatement ps = new Query(Query._CREATE, module.getItem(), null, null).getQuery();
            ps.execute();
            ps.close();
            
            Collection<DcObject> items = module.getDefaultData();
            if (items != null) {
                for (DcObject dco : items) {
                    PreparedStatement psInsert = new Query(Query._INSERT, dco, null, null).getQuery();
                    psInsert.execute();
                    psInsert.close();
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() != -21)
                throw new Exception(e);
        }
    }
}
