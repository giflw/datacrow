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

import net.datacrow.util.Utilities;

public class DcAssociate extends DcObject {

    private static final long serialVersionUID = 6420907381739662714L;
    
    public static final int _A_NAME = 1;
    public static final int _B_DESCRIPTION = 2;
    public static final int _C_WEBPAGE = 3;
    public static final int _D_PHOTO = 4;
    public static final int _E_FIRSTNAME = 5;
    public static final int _F_LASTTNAME = 6;
    
    public DcAssociate(int module) {
        super(module);
    }
    
    @Override
    protected void beforeSave() {
        setName();
    }
    
    public void setName() {
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
            StringTokenizer st = new StringTokenizer(name);
            int i = 0;
            while (st.hasMoreElements()) {
                if ((i + 1) != st.countTokens()) {
                    firstname = (String) getValue(DcAssociate._E_FIRSTNAME);
                    firstname = firstname == null ? "" : firstname;
                    firstname += firstname.length() > 0 ? " " : "";
                    firstname += st.nextElement();
                    setValue(DcAssociate._E_FIRSTNAME, firstname);
                } else {
                    lastname = (String) getValue(DcAssociate._F_LASTTNAME);
                    lastname = lastname == null ? "" : lastname;
                    lastname += lastname.length() > 0 ? " " : "";
                    lastname += st.nextElement();
                    setValue(DcAssociate._F_LASTTNAME, lastname);
                }
            }
        }
        super.beforeSave();
    }

    @Override
    public int getDefaultSortFieldIdx() {
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
