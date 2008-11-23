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

package net.datacrow.core.modules;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.Settings;

public class LoanModule extends DcModule {

    private static final long serialVersionUID = -1777037389578494831L;

    public LoanModule() {
        super(DcModules._LOAN, 
              false, 
              DcResources.getText("sysLoan"), 
              "", 
              DcResources.getText("sysLoan"),
              DcResources.getText("sysLoan"),
              "loans", 
              "lo");
    }
    
    @Override
    public boolean isEnabled() {
        return DcModules.get(DcModules._CONTACTPERSON).isEnabled();
    }

    @Override
    public Settings getSettings() {
        setSetting(DcRepository.ModuleSettings.stTableColumnOrder, new int[] {Loan._C_CONTACTPERSONID, Loan._A_STARTDATE, Loan._B_ENDDATE});
        return super.getSettings();
    }

    @Override
    public DcObject getDcObject() {
        return new Loan();
    }

    @Override
    protected void initializeFields() {
        super.initializeFields();

        addField(new DcField(Loan._A_STARTDATE, getIndex(), "Start date",
                false, true, false, false, false,
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._DATE,
                "StartDate"));
        addField(new DcField(Loan._B_ENDDATE, getIndex(), "End date",
                false, true, false, false, false,
                255, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._DATE,
                "EndDate"));
        addField(new DcField(Loan._C_CONTACTPERSONID, getIndex(), "Contact Person",
                false, true, false, false, false,
                50, ComponentFactory._NUMBERFIELD, getIndex(), DcRepository.ValueTypes._BIGINTEGER,
                "PersonID"));
        addField(new DcField(Loan._D_OBJECTID, getIndex(), "Object",
                false, true, false, false, false,
                50, ComponentFactory._NUMBERFIELD, getIndex(), DcRepository.ValueTypes._BIGINTEGER,
                "ObjectID"));
        addField(new DcField(Loan._E_DUEDATE, getIndex(), "Due Date",
                false, true, false, false, false,
                50, ComponentFactory._DATEFIELD, getIndex(), DcRepository.ValueTypes._DATE,
                "DueDate"));        
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof LoanModule ? ((LoanModule) o).getIndex() == getIndex() : false);
    }      
}
