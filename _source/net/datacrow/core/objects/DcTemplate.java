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

public class DcTemplate extends DcObject {

    private static final long serialVersionUID = -6626046268821765360L;

    public static final int _SYS_TEMPLATENAME = 50;
    public static final int _SYS_DEFAULT = 51;
    
    private final int parent;
 
    public DcTemplate(int module, int parent) {
        super(module);
        this.parent = parent;
     }

     public String getTemplateName() {
         return (String) getValue(_SYS_TEMPLATENAME);
     }     
     
     public boolean isDefault() {
         Object o = getValue(_SYS_DEFAULT);
         
         if (o == null)
             return false;
         
         if (o instanceof String)
             return Boolean.valueOf((String) o);
         
         return ((Boolean) getValue(_SYS_DEFAULT)).booleanValue();
     }
     
     @Override
     public void freeResources() {
         // prevent images error
     }     
     
     public int getParentModule() {
         return parent;
     }     
     
     @Override
     public String toString() {
         return getValue(DcTemplate._SYS_TEMPLATENAME) != null ? 
                getValue(DcTemplate._SYS_TEMPLATENAME).toString() : "";
     } 

     @Override
     public void checkIntegrity(boolean update) throws ValidationException {
         validateRequiredFields(update);
         isUnique(this, update);
     }  
}
