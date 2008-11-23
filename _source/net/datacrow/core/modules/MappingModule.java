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
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;

public class MappingModule extends DcModule {

    private static final long serialVersionUID = 7229196343761371630L;
    private final DcModule parentMod;
    private final DcModule referencedMod;
    
    public MappingModule(DcModule parentMod, DcModule referencedMod) {
        super(referencedMod.getIndex() + DcModules._MAPPING + parentMod.getIndex(), 
              "", "", "", "", 
              "X_" + parentMod.getTableName() + "_"  + referencedMod.getTableName(), 
              "X" + parentMod.getTableShortName() + referencedMod.getTableShortName(), 
              false);
        
        this.parentMod = parentMod;
        this.referencedMod = referencedMod;
        
        initializeSystemFields();
        initializeFields();
        initializeSettings();
    } 
    
    @Override
    public DcObject getDcObject() {
        DcMapping mapping = new DcMapping(getIndex());
        return mapping;
    }    

    public int getReferencedModIdx() {
        return getField(DcMapping._B_REFERENCED_ID).getSourceModuleIdx();
    } 

    public int getParentModIdx() {
        return getField(DcMapping._A_PARENT_ID).getReferenceIdx();
    } 
    
    @Override
    public boolean hasInsertView() {
        return false;
    }

    @Override
    public boolean hasSearchView() {
        return false;
    }    
    
    @Override
    protected void initializeFields() {
        addField(new DcField(DcMapping._A_PARENT_ID, getIndex(), "Object ID",
                             false, true, false, false, false,
                             50, ComponentFactory._NUMBERFIELD, parentMod.getIndex(), DcRepository.ValueTypes._BIGINTEGER,
                             "ObjectID"));
        addField(new DcField(DcMapping._B_REFERENCED_ID, getIndex(), "Referenced ID",
                             false, true, false, false, false,
                             50, ComponentFactory._NUMBERFIELD, referencedMod.getIndex(), DcRepository.ValueTypes._BIGINTEGER,
                             "ReferencedId"));
        addField(new DcField(DcMapping._C_DISPLAY_STRING, getIndex(), "Display String",
                             true, true, true, false, false,
                             50, ComponentFactory._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                             "DisplayString"));
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof MappingModule ? ((MappingModule) o).getIndex() == getIndex() : false);
    }     
    
    @Override
    public DcObject[] getDefaultData() throws Exception  {
        return null;
    }
}