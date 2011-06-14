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

import net.datacrow.core.modules.DcModules;
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
    public static final int _G_IS_COMPANY = 7;
    
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
        if (!isFilled(DcAssociate._G_IS_COMPANY)) {
            if (getModule().getIndex() == DcModules._SOFTWAREPUBLISHER ||
                getModule().getIndex() == DcModules._DEVELOPER ||
                getModule().getIndex() == DcModules._BOOKPUBLISHER ||
                getModule().getIndex() == DcModules._AUTHOR) {
                
                setValue(DcAssociate._G_IS_COMPANY, Boolean.TRUE);
            } else {
                setValue(DcAssociate._G_IS_COMPANY, Boolean.FALSE);
            }
        }
        
        setName();
        
        super.beforeSave();
    }

	@Override
	public int getDefaultSortFieldIdx() {
		return DcAssociate._A_NAME;
	}

	/**
     * Sets the various name parts. The last and first name are calculated if only the
     * full name is available. If only the last and first name are available the full name
     * is created out of these parts.
     */
    public void setName() {
        String name = (String) getValue(DcAssociate._A_NAME);
        String firstname = (String) getValue(DcAssociate._E_FIRSTNAME);
        String lastname = (String) getValue(DcAssociate._F_LASTTNAME);
        
        if (!isFilled(DcAssociate._G_IS_COMPANY))
        	setValue(DcAssociate._G_IS_COMPANY, Boolean.FALSE);
        
        boolean company = (Boolean) getValue(DcAssociate._G_IS_COMPANY);
        
        boolean isNameSet = !Utilities.isEmpty(name);
        boolean isFirstNameSet = !Utilities.isEmpty(firstname);
        boolean isLastNameSet = !Utilities.isEmpty(lastname);
        
        if (!isNameSet && (isLastNameSet || isFirstNameSet)) {
        	name = Utilities.getName(firstname, lastname, company);
            setValue(DcAssociate._A_NAME, name);

        } else if (isNameSet && !company && (!isLastNameSet || !isFirstNameSet)) {
        	firstname = Utilities.getFirstName(name);
        	lastname = Utilities.getLastName(name);
        	name = Utilities.getName(firstname, lastname, company);

        	setValueLowLevel(DcAssociate._A_NAME, name);
        	setValueLowLevel(DcAssociate._E_FIRSTNAME, firstname);
        	setValueLowLevel(DcAssociate._F_LASTTNAME, lastname);
        }
        
        
    }

    @Override
    public int getSystemDisplayFieldIdx() {
        return DcAssociate._A_NAME;
    }
    
    @Override
    public String toString() {
		return getValue(DcAssociate._A_NAME) != null  ? (String) getValue(DcAssociate._A_NAME) : "";
    }  

    @Override
    public String getName() {
        return toString();
    }    
}
