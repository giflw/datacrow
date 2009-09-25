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

package net.datacrow.core.objects;

import java.util.StringTokenizer;

import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.Utilities;

/**
 * Represents companies and persons.  
 * 
 * @author Robert Jan van der Waals
 */
public class DcAssociate extends DcObject {

    private static final long serialVersionUID = 6420907381739662714L;
    
    public static final int _A_NAME = 1;
    public static final int _B_DESCRIPTION = 2;
    public static final int _C_WEBPAGE = 3;
    public static final int _D_PHOTO = 4;
    public static final int _E_FIRSTNAME = 5;
    public static final int _F_LASTTNAME = 6;
    
    /**
     * Creates a new instance.
     * @param module
     */
    public DcAssociate(int module) {
        super(module);
    }

    /**
     * Executed before inserting or updating the item.
     */
    @Override
    protected void beforeSave() throws ValidationException {
        setName();
    }
    
    /**
     * Sets the various name parts. The last and first name are calculated if only the
     * full name is available. If only the last and first name are available the full name
     * is created out of these parts.
     */
    public void setName()  throws ValidationException {
        String name = (String) getValue(DcAssociate._A_NAME);
        String firstname = (String) getValue(DcAssociate._E_FIRSTNAME);
        String lastname = (String) getValue(DcAssociate._F_LASTTNAME);

        boolean isNameSet = !Utilities.isEmpty(name);
        boolean isFirstNameSet = !Utilities.isEmpty(firstname);
        boolean isLastNameSet = !Utilities.isEmpty(lastname);
        if (!isNameSet && (isLastNameSet || isFirstNameSet)) {
            firstname = firstname == null ? "" : firstname.trim();
            lastname = lastname == null ? "" : lastname.trim();
            setValue(DcAssociate._A_NAME, firstname + " " + lastname);
        } else if (isNameSet && (!isLastNameSet || !isFirstNameSet)) {
            name = name.indexOf("(") > 0 ? name.substring(0, name.indexOf("(")).trim() : name;
            StringTokenizer st = new StringTokenizer(name);
            int i = 0;
            while (st.hasMoreElements()) {
                if ((i + 1) != st.countTokens()) {
                    firstname = (String) getValue(DcAssociate._E_FIRSTNAME);
                    String s = (String) st.nextElement();
                    if (!isFirstNameSet) {
                        firstname = firstname == null ? "" : firstname;
                        firstname += firstname.length() > 0 ? " " : "";
                        firstname += s;
                        setValue(DcAssociate._E_FIRSTNAME, firstname);
                    }
                } else {
                    lastname = (String) getValue(DcAssociate._F_LASTTNAME);
                    String s = (String) st.nextElement();
                    if (!isLastNameSet) {
                        lastname = lastname == null ? "" : lastname;
                        lastname += lastname.length() > 0 ? " " : "";
                        lastname += s;
                        setValue(DcAssociate._F_LASTTNAME, lastname);
                    }
                }
            }
        }
        super.beforeSave();
    }

    @Override
    public int getSystemDisplayFieldIdx() {
        return DcAssociate._A_NAME;
    }
    
    /**
     * Retrieves the field on which associates are sorted. 
     * This depends on the {@link DcRepository.Settings#stPersonOrder} setting.
     */
    @Override
    public int getDefaultSortFieldIdx() {
    	String order = DcSettings.getString(DcRepository.Settings.stPersonOrder);
    	if (order.equals(DcResources.getText("lblPersonOrginalOrder")))
    		return DcAssociate._ID;
    	else if (order.equals(DcResources.getText("lblPersonOrderByLastname")))
    		return DcAssociate._F_LASTTNAME;
    	else if (order.equals(DcResources.getText("lblPersonOrderByFirstname")))
    		return DcAssociate._E_FIRSTNAME;
    	else 
    		return DcAssociate._A_NAME;
    }    
    
    @Override
    public String toString() {
    	String format = DcSettings.getString(DcRepository.Settings.stPersonDisplayFormat);
    	
    	if (format.equals(DcResources.getText("lblPersonLastNameFirstname"))) {
        	String firstname = (String) getValue(DcAssociate._E_FIRSTNAME);
        	String lastname = (String) getValue(DcAssociate._F_LASTTNAME);
        	
        	if (Utilities.isEmpty(firstname) || Utilities.isEmpty(lastname))
        		return getValue(DcAssociate._A_NAME) != null  ? (String) getValue(DcAssociate._A_NAME) : "";
    		
    		return  lastname + ", " + firstname;
    	} else { 
    		return getValue(DcAssociate._A_NAME) != null  ? (String) getValue(DcAssociate._A_NAME) : "";
    	}
    }  

    @Override
    public String getName() {
        return toString();
    }    
}
