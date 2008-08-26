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

import java.util.Calendar;
import java.util.Date;

import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.helpers.ContactPerson;

public class Loan extends DcObject {

    private static final long serialVersionUID = 2452738490117020045L;

    public static final int _A_STARTDATE = 1;
    public static final int _B_ENDDATE = 2;
    public static final int _C_CONTACTPERSONID = 3;
    public static final int _D_OBJECTID = 4;

    public Loan() {
        super(DcModules._LOAN);
    }

    public boolean isAvailable(String objectID) {
        Loan loan;
        if ((getID() != null && getID().length() > 0) || getValue(_A_STARTDATE) != null)
            loan = this;
        else 
            loan = DataManager.getCurrentLoan(objectID);
        
        boolean available = true;
        if (loan != null) {
            Date start = (Date) loan.getValue(Loan._A_STARTDATE);
            Date end = (Date) loan.getValue(Loan._B_ENDDATE);
            Date current = new Date();
            if (end != null)
                available = true;
            else if (start != null && current.compareTo(start) >= 0)
                available = false;
        }
        
        return available;
    }
    
    public String getPersonDescription() {
        String personID = (String) getValue(Loan._C_CONTACTPERSONID);
        ContactPerson person = !isAvailable((String) getValue(Loan._D_OBJECTID)) ? (ContactPerson) DataManager.getObject(DcModules._CONTACTPERSON, personID) : null;
        return person == null ? "" : person.toString();
    }
    
    
    public Long getDaysLoaned() {
        Date startDate = (Date) getValue(Loan._A_STARTDATE);
        Long days = null;
        if (startDate != null && !isAvailable((String) getValue(Loan._D_OBJECTID))) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new java.util.Date());
            int currentDay = cal.get(Calendar.DAY_OF_YEAR);
            cal.setTime(startDate);
            int startDay = cal.get(Calendar.DAY_OF_YEAR);
            days = Long.valueOf(currentDay - startDay);
        }
        return days;
    }
}
