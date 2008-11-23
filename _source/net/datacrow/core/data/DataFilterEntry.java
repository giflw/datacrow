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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.Utilities;

/**
 * A data filter entry belongs to a data filter.
 * 
 * @author Robert Jan van der Waals
 */
public class DataFilterEntry {

    public static final String _AND = DcResources.getText("lblAnd");
    public static final String _OR = DcResources.getText("lblOr");
    
    private int module;
    private int field;

    private Operator operator;
    private Object value; 
    
    private String andOr;

    /**
     * Creates an empty filter entry.
     */
    public DataFilterEntry() {}
    
    /**
     * Creates a filter entry.
     * @param andOr {@link DataFilterEntry#_AND} or {@link DataFilterEntry#_OR} 
     * @param module The module to which the specified field belongs.
     * @param field The field to be checked on.
     * @param operator The operator.
     * @param value The value used to test against the input.
     */
    public DataFilterEntry(String andOr, int module, int field, Operator operator, Object value) {
        this.module = module;
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.andOr = andOr;
    }
    
    /**
     * The ID of the filter entry.
     */
    public int getID() {
        return (module * 1000) + field;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public void setField(int field) {
        this.field = field;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    /**
     * Specifies if the entry should be treated as an and or an or condition.
     * @see #_AND
     * @see #_OR
     * @param andOr
     */
    public void setAndOr(String andOr) {
        this.andOr = andOr;
    }

    /**
     * Indicates if the filter entry should be treated as an and or a or condition.
     * @see #_AND
     * @see #_OR
     */
    public String getAndOr() {
        return andOr;
    }

    /**
     * Indicates if the filter entry should be treated as an or condition.
     */
    public boolean isOr() {
        return andOr.equals(_OR);
    }

    /**
     * Indicates if the filter entry should be treated as an and condition.
     */
    public boolean isAnd() {
        return andOr.equals(_AND);
    }

    public int getField() {
        return field;
    }

    public int getModule() {
        return module;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Checks if the supplies item lives up to the filter entry.
     * @see Operator
     * @param dco
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean applies(DcObject dco) {

        Object o = dco.getValue(field);
        boolean isInputEmpty = Utilities.isEmpty(o);
        boolean isCheckEmpty = Utilities.isEmpty(value);
        
        String input = isInputEmpty ? "" : o instanceof DcObject ? ((DcObject) o).getID() : o.toString().toLowerCase();
        String check = isCheckEmpty ? "" : value instanceof DcObject ? ((DcObject) value).getID() : value.toString().toLowerCase();
        
        if (dco.getField(field) == null)
        	return false;
        
        if (operator.getIndex() == Operator.CONTAINS.getIndex()) {
            if (dco.getField(field).getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                Collection<DcObject> c1 = (Collection<DcObject>) o;
                Collection<DcObject> c2 = (Collection<DcObject>) value;
                return contains(c1, c2);
            } else { 
                return input.indexOf(check) > -1 || input.equals(check);
            }
        } else if (operator.getIndex() == Operator.DOES_NOT_CONTAIN.getIndex()) {
            if (dco.getField(field).getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                Collection<DcObject> c1 = (Collection<DcObject>) o;
                Collection<DcObject> c2 = (Collection<DcObject>) value;
                return !contains(c1, c2);
            } else { 
                return input.indexOf(check) == -1;
            }
        } else if (operator.getIndex() == Operator.ENDS_WITH.getIndex()) {
            return input.endsWith(check);
        } else if (operator.getIndex() == Operator.EQUAL_TO.getIndex()) {
        	if (dco.getField(field).getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                Collection<DcObject> c1 = (Collection<DcObject>) o;
                
                Collection<DcObject> c2;
                if (value instanceof Collection) {
                    c2 = (Collection<DcObject>) value;
                } else {
                    c2 = new ArrayList<DcObject>();
                    c2.add((DcObject) value);
                }

                return contains(c1, c2);
            } else { 
                return input.equals(check);
            }
        } else if (operator.getIndex() == Operator.BEFORE.getIndex()) {
            return isInputEmpty ? false : ((Date) o).before((Date) value);
        } else if (operator.getIndex() == Operator.AFTER.getIndex()) {
            return isInputEmpty ? false : ((Date) o).after((Date) value);
        } else if (operator.getIndex() == Operator.GREATER_THEN.getIndex()) {
            return isInputEmpty ? false : 
                Integer.valueOf(input).intValue() > Integer.valueOf(check).intValue();
        } else if (operator.getIndex() == Operator.IS_EMPTY.getIndex()) {
            return isInputEmpty;
        } else if (operator.getIndex() == Operator.IS_FILLED.getIndex()) {
            return !isInputEmpty;
        } else if (operator.getIndex() == Operator.LESS_THEN.getIndex()) {
            if (dco.getField(field).getValueType() == DcRepository.ValueTypes._DATE) {
                return isInputEmpty ? false : ((Date) o).before((Date) value);
            } else {
                return isInputEmpty ? false : 
                    Integer.valueOf(input).intValue() < Integer.valueOf(check).intValue();
            }
        } else if (operator.getIndex() == Operator.NOT_EQUAL_TO.getIndex()) {
            if (dco.getField(field).getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                Collection c1 = (Collection) o;
                Collection c2 = (Collection) value;
                return !contains(c1, c2);
            } else { 
                return !input.equals(check);
            }            
        } else if (operator.getIndex() == Operator.STARTS_WITH.getIndex()) {
            return input.startsWith(check);
        }
        
        return true;
    }

    private boolean contains(Collection<DcObject> c1, Collection<DcObject> c2) {
        boolean contains = false;
        
        if (c2 == null) {
            contains = true;
        } else if (c1 != null) {
            contains = true;
            for (DcObject dco2 : c2 ) {
                boolean b = false;
                for (DcObject dco1 : c1)
                    b = dco1.toString().equals(dco2.toString()) ? true : b;

                contains &= b; 
            }
        }

        return contains;
    }
    
    @Override
    public String toString() {
        return getAndOr() + " " + DcModules.get(module).getField(field).getLabel() + " " +
               getOperator().getName() + " " + getValue();
    }
}
