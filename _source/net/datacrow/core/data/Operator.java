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

import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.resources.DcResources;

public enum Operator {

    EQUAL_TO(1, DcResources.getText("lblEqualTo"), true),
    NOT_EQUAL_TO(2, DcResources.getText("lblNotEqualTo"), true),
    CONTAINS(3, DcResources.getText("lblContains"), true),
    DOES_NOT_CONTAIN(4, DcResources.getText("lblDoesNotContain"), true),
    IS_EMPTY(4, DcResources.getText("lblIsEmpty"), false),
    IS_FILLED(5, DcResources.getText("lblIsFilled"), false),
    STARTS_WITH(6, DcResources.getText("lblStartsWith"), true),
    ENDS_WITH(7, DcResources.getText("lblEndsWith"), true),
    LESS_THEN(8, DcResources.getText("lblLessThen"), true),
    GREATER_THEN(9, DcResources.getText("lblGreaterThen"), true),
    BEFORE(10, DcResources.getText("lblBefore"), true),
    AFTER(11, DcResources.getText("lblAfter"), true);
    
    private final int index;  
    private final String name; 
    private final boolean needsValue;
    
    Operator(int index, String name, boolean needsValue) {
        this.index = index;
        this.name = name;
        this.needsValue = needsValue;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public boolean needsValue() {
        return needsValue;
    }
    
    public static Collection<Operator> get(DcField field) {
        ArrayList<Operator> operators = new ArrayList<Operator>();

        operators.add(EQUAL_TO);
        operators.add(NOT_EQUAL_TO);
        
        if (field.getValueType() == DcRepository.ValueTypes._DATE) {
        	operators.add(BEFORE);
            operators.add(AFTER);
    	} else if (field.getValueType() == DcRepository.ValueTypes._LONG) {
            operators.add(LESS_THEN);
            operators.add(GREATER_THEN);
        } else if (field.getValueType() != DcRepository.ValueTypes._BOOLEAN) {
            operators.add(IS_EMPTY);
            operators.add(IS_FILLED);
            
            if (field.getValueType() != DcRepository.ValueTypes._LONG &&
            	field.getValueType() != DcRepository.ValueTypes._DATE) {

            	operators.add(0, CONTAINS);
	            operators.add(DOES_NOT_CONTAIN);
	            operators.add(STARTS_WITH);
	            operators.add(ENDS_WITH);
            }
        }
        
        return operators;
    }

    @Override
    public String toString() {
        return getName();
    }
}
