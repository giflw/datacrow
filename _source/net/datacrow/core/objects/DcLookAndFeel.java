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

public class DcLookAndFeel {
    
    public static final int _SKINLF = 0;
    public static final int _LAF = 1;
    public static final int _NONE = 3;
    public static final int _JTATTOO = 4;
    
    private final String name;
    private final String filename;
    private final String className;
    
    private final int type;
    
    public DcLookAndFeel(String name, String className, String filename, int type) {
        this.name = name;
        this.className = className;
        this.filename = filename;
        this.type = type;
    }
    
    public static DcLookAndFeel getDisabled() {
        return new DcLookAndFeel("None", null, null, _NONE);
    }
    
    public String getName() {
        return name;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getFileName() {
        return filename;
    }
    
    public int getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : super.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o instanceof DcLookAndFeel) 
            equal = ((DcLookAndFeel) o).getName().equals(getName());
        return equal;
    }
}
