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

package net.datacrow.core.settings.upgrade;

import net.datacrow.core.modules.DcModule;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.Definition;

/**
 * Used for conversion purposes only.
 * 
 * @deprecated
 * @author Robert Jan van der Waals
 */
@SuppressWarnings("dep-ann")
public class LegacyFieldDefinition extends Definition {

    private int index;
    private boolean required;
    private boolean visible;
    private boolean enabled;
    private boolean descriptive;
    private String label;

    public LegacyFieldDefinition(int index, String label, boolean enabled,
                                 boolean required, boolean descriptive, boolean visible) {
        this.index = index;
        this.required = required;
        this.visible = visible;
        this.enabled = enabled;
        this.descriptive = descriptive;
        this.label = label;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void isEnabled(boolean b) {
        this.enabled = b;
    }

    public boolean isVisible() {
        return visible;
    }

    public void isVisible(boolean b) {
        this.visible = b;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isDescriptive() {
        return descriptive;
    }

    public void isDescriptive(boolean b) {
        this.descriptive = b;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object[] getDisplayValues(DcModule module) {
        return new Object[] { module.getField(index).getSystemName(),
                label, enabled, required, descriptive, visible,
                module.getField(index) };
    }

    @Override
    public String toSettingValue() {
        return index + "/&/"
                + (label == null || label.length() == 0 ? "null" : label)
                + "/&/" + enabled + "/&/" + required + "/&/" + visible
                + "/&/" + descriptive;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DcFieldDefinition) {
            DcFieldDefinition def = (DcFieldDefinition) o;
            return def.getIndex() == getIndex();
        }
        return false;
    }
}
