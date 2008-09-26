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

package net.datacrow.core.objects.helpers;

import net.datacrow.console.windows.messageboxes.QuestionBox;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.db.DatabaseManager;
import net.datacrow.core.db.Query;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.resources.DcResources;

public class ContactPerson extends DcObject {

    private static final long serialVersionUID = 3504231999443415852L;
    
    public static final int _A_NAME = 1;
    public static final int _B_DESCRIPTION = 2;
    public static final int _C_PHOTO = 3;
    public static final int _D_CATEGORY = 4;
    public static final int _E_EMAIL = 5;
    public static final int _F_ADDRESS = 6;
    public static final int _G_PHONE_HOME = 7;
    public static final int _H_PHONE_WORK = 8;
    public static final int _J_CITY = 9;
    public static final int _K_COUNTRY = 10;
    
    public ContactPerson() {
        super(DcModules._CONTACTPERSON);
    }
    
    @Override
    public void delete() {
        Loan loan = new Loan();
        loan.setValue(Loan._C_CONTACTPERSONID, getID());
        
        DataFilter filter = new DataFilter(loan);
        DcObject[] loans = DataManager.get(DcModules._LOAN, filter);
        
        if (loans == null || loans.length == 0) {
            super.delete();
        } else {
            QuestionBox qb = new QuestionBox(DcResources.getText("msgDeletePersonLendItems"));
            if (qb.isAffirmative()) {
                DatabaseManager.executeQuery("DELETE FROM " + loan.getModule().getTableName() + " WHERE " + 
                                             loan.getField(Loan._C_CONTACTPERSONID) + " = " + getID(), 
                                             Query._DELETE, false);
                for (DcObject dco : loans)
                    DataManager.remove(dco, loan.getModule().getIndex());

                super.delete();
            } else {
                getRequests().clear();
            }
        }
    }    
}

