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

package net.datacrow.core.services;

public class Region {
    
    private final String url;
    private final String code;
    private final String displayName;

    public Region(String code, String displayName, String url) {
        this.code = code;
        this.url = url;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getUrl() {
        return url;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    @Override
    public int hashCode() {
        return code.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        return o == null || !(o instanceof Region) ? false : ((Region) o).getCode().equals(code);
    }    
}
