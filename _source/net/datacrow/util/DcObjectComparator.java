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

package net.datacrow.util;

import java.util.Comparator;
import java.util.Date;

import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;

public class DcObjectComparator implements Comparator<DcObject> {

    private final int field;
    
    public DcObjectComparator(int field) {
        this.field = field;
    }
    
    public int compare(DcObject dco1, DcObject dco2) {
        Object o1 = dco1.getValue(field);
        Object o2 = dco2.getValue(field);

        if (o1 == null && o2 == null)
            return 0;
        else if (o1 == null || o2 == null)
            return -1;
        
        DcField fld = dco1.getField(field);
        if (o1 instanceof Number && o2 instanceof Number &&
            fld.getValueType() == DcRepository.ValueTypes._BIGINTEGER ||
            fld.getValueType() == DcRepository.ValueTypes._DOUBLE ||
            fld.getValueType() == DcRepository.ValueTypes._LONG) {
        
            Number n1 = (Number) o1;
            Number n2 = (Number) o2;
            
            return (int) (n1.longValue() - n2.longValue());

        } else if (fld.getValueType() == DcRepository.ValueTypes._DATE) {
            Date d1 = (Date) o1;
            Date d2 = (Date) o2;
        
            return d1.compareTo(d2);

        } else {
            return dco1.getDisplayString(field).toLowerCase().compareTo(
                   dco2.getDisplayString(field).toLowerCase());
        }
    }
}
