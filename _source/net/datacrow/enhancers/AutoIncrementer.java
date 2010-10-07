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

package net.datacrow.enhancers;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

import org.apache.log4j.Logger;

/**
 * Auto numbering functionality. Applies a new number to the indicated number 
 * field. 
 * 
 * @see IValueEnhancer
 * @author Robert Jan van der Waals
 */
public class AutoIncrementer implements IValueEnhancer {

    private static Logger logger = Logger.getLogger(AutoIncrementer.class.getName());
    
    private boolean enabled = false;
    private boolean fillGaps = false;
    private int step = 1;
    private int field;

    /**
     * Creates a new instance. 
     * @param field The field to which enhancements will be made.
     */
    public AutoIncrementer(int field) {
        this.field = field;
    }

    /**
     * Creates a new instance. 
     * @param field The field to which enhancements will be made.
     * @param enabled Indicates if this enhancer is enabled.
     * @param fillGaps Indicates if gaps in the numbering should be filled.
     * @param step The step size (amount to increase per number).
     */
    public AutoIncrementer(int field, boolean enabled, boolean fillGaps, int step) {
        this.field = field;
        this.enabled = enabled;
        this.fillGaps = fillGaps;
        this.step = step;
    }
    
    @Override
    public String toSaveString() {
        return enabled + "/&/" + fillGaps + "/&/" + step;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean isFillGaps() {
        return fillGaps;
    }
    
    public int getStep() {
        return step;
    }
    
    @Override
    public boolean isRunOnUpdating() {
        return false;
    }
    
    @Override
    public boolean isRunOnInsert() {
        return true;
    }
    
    @Override
    public Object apply(DcField field, Object value) {
        Object result = value;
        
        DcModule module = DcModules.get(field.getModule());
        int counter = 0;
        
        try {
            if (!fillGaps) {
                String query = "SELECT MAX(" + field.getDatabaseFieldName() + ") AS COUNTMAXIMUM FROM " +
                               module.getTableName();
                ResultSet rs = DatabaseManager.executeSQL(query);
                int maximum = 0;
                while (rs.next()) {
                    maximum = rs.getInt("COUNTMAXIMUM");
                }
                
                result = Long.valueOf(maximum + getStep());
                rs.close();
            } else {
                counter = counter + getStep();
                
                String qryCurrent = "SELECT " + field.getDatabaseFieldName() + " FROM " + module.getTableName() + 
                " WHERE " + field.getDatabaseFieldName() + " IS NOT NULL AND " +
                field.getDatabaseFieldName() + " > 0 " +
                "ORDER BY 1";            
                
                Collection<Integer> currentValues = new ArrayList<Integer>();
                ResultSet rs = DatabaseManager.executeSQL(qryCurrent);
                while (rs.next())
                    currentValues.add(rs.getInt(field.getDatabaseFieldName()));
                
                if (currentValues.contains(counter)) {
                    boolean currentfound = false;
                    for (int x : currentValues) {
                        while (!currentfound && x == counter)
                            counter += getStep();
                    }
                }            
                result = Long.valueOf(counter);
            }
        } catch (Exception e) {
            String msg = DcResources.getText("msgAutoNumberError",  new String[] {field.getLabel(), e.getMessage()});
            logger.error(msg, e);
            DcSwingUtilities.displayErrorMessage(msg);
        }
            
        return result;
    }
    
    public int getField() {
        return field;
    }

    @Override
    public void parse(String s) {
        String tmp = s;
        String s1 = tmp.substring(0, tmp.indexOf("/&/"));
        tmp = tmp.substring(tmp.indexOf("/&/") + 3, tmp.length());
        
        String s2 = tmp.substring(0, tmp.indexOf("/&/"));
        tmp = tmp.substring(tmp.indexOf("/&/") + 3, tmp.length());
        
        String s3 = tmp;
        
        enabled = Boolean.valueOf(s1);
        fillGaps = Boolean.valueOf(s2);
        step = Integer.valueOf(s3);
    }

    @Override
    public int getIndex() {
        return ValueEnhancers._AUTOINCREMENT;
    }
}
