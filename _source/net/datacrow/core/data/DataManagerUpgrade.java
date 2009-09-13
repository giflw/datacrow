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

package net.datacrow.core.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.helpers.ContactPerson;
import net.datacrow.core.objects.helpers.Movie;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class DataManagerUpgrade {

    private static Logger logger = Logger.getLogger(DataManagerUpgrade.class.getName());
    
    public void start() {
        try {
            convertMovieCountriesAndLanguages();
        } catch (Exception e) {
            String msg = e.getMessage() + ". Data conversion failed. " +
                "Please restore your latest Backup and retry. Contact the developer " +
                "if the error persists";
            new MessageBox(msg, MessageBox._ERROR);
            logger.error(msg, e);
        }            
    }
    
    private void convertMovieCountriesAndLanguages() throws Exception {
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        
        ResultSet rs;

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
        
        rs = stmt.executeQuery("SELECT ID, COUNTRY FROM MOVIE WHERE COUNTRY IS NOT NULL");
        while (rs.next()) {
            String id = rs.getString(1);
            String country = rs.getString(2);
            if (!Utilities.isEmpty(country)) {
                DcObject movie = DataManager.getObject(DcModules._MOVIE, id);
                DataManager.createReference(movie, Movie._F_COUNTRY, country);
                movie.saveUpdate(false, false);
            }
        }
        
        rs = stmt.executeQuery("SELECT ID, LANGUAGE FROM MOVIE WHERE LANGUAGE IS NOT NULL");
        while (rs.next()) {
            String id = rs.getString(1);
            String language = rs.getString(2);
            if (!Utilities.isEmpty(language)) {
                DcObject movie = DataManager.getObject(DcModules._MOVIE, id);
                DataManager.createReference(movie, Movie._D_LANGUAGE, language);
                movie.saveUpdate(false, false);
            }                
        }
        
        rs.close();
        rs = stmt.executeQuery("SELECT ID, AUDIOLANGUAGE FROM MOVIE WHERE AUDIOLANGUAGE IS NOT NULL");
        while (rs.next()) {
            String id = rs.getString(1);
            String language = rs.getString(2);
            if (!Utilities.isEmpty(language)) {
                DcObject movie = DataManager.getObject(DcModules._MOVIE, id);
                DataManager.createReference(movie, Movie._1_AUDIOLANGUAGE, language);
                movie.saveUpdate(false, false);
            }                  
        }

        rs.close();
        rs = stmt.executeQuery("SELECT ID, SUBTITLELANGUAGE FROM MOVIE WHERE SUBTITLELANGUAGE IS NOT NULL");
        while (rs.next()) {
            String id = rs.getString(1);
            String language = rs.getString(2);
            if (!Utilities.isEmpty(language)) {
                DcObject movie = DataManager.getObject(DcModules._MOVIE, id);
                DataManager.createReference(movie, Movie._2_SUBTITLELANGUAGE, language);
                movie.saveUpdate(false, false);
            }   
        }
        
        rs.close();
        rs = stmt.executeQuery("SELECT ID, COUNTRY FROM PERSON WHERE COUNTRY IS NOT NULL");
        while (rs.next()) {
            String id = rs.getString(1);
            String language = rs.getString(2);
            if (!Utilities.isEmpty(language)) {
                DcObject person = DataManager.getObject(DcModules._CONTACTPERSON, id);
                DataManager.createReference(person, ContactPerson._K_COUNTRY, language);
                person.saveUpdate(false, false);
            }   
        }        
        
        try {
            stmt.close();
            conn.close();
            rs.close();
        } catch (Exception e) {
            logger.error(e, e);
        }
    }
}
