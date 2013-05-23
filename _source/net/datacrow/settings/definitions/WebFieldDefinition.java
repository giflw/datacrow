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

package net.datacrow.settings.definitions;

import net.datacrow.core.modules.DcModule;

public class WebFieldDefinition extends Definition {

    private int field;
    private int width = 0;
    private int maxTextLength = 0;
    private boolean visible;
    private boolean link;
    private boolean quickSearch;
    
    public WebFieldDefinition(int field, int width, int maxTextLength, boolean visible, boolean link, boolean quickSearch) {
        super();
        this.field = field;
        this.width = width;
        this.visible = visible;
        this.link = link;
        this.quickSearch = quickSearch;
        this.maxTextLength = maxTextLength;
    }
    
    public int getMaxTextLength() {
        return maxTextLength;
    }

    public boolean isLink() {
        return link;
    }
    
    public int getField() {
        return field;
    }

    public int getWidth() {
        return width;
    }

    public boolean isOverview() {
        return visible;
    }
    
    public boolean isQuickSearch() {
        return quickSearch;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof WebFieldDefinition) {
            WebFieldDefinition def = (WebFieldDefinition) o;
            return def.getField() == getField();
        }
        return false;
    }    

    @Override
    public String toSettingValue() {
        return field + "/&/" + width + "/&/" + maxTextLength + "/&/" + visible + "/&/" + link + "/&/" + quickSearch;
    }    
    
    public Object[] getDisplayValues(DcModule module) {
        return new Object[] {module.getField(field).getLabel(),
                             width, 
                             maxTextLength,
                             Boolean.valueOf(visible), 
                             Boolean.valueOf(link),
                             Boolean.valueOf(quickSearch),
                             module.getField(field)};
    }
}
