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

package net.datacrow.core.settings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JComponent;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcColorSelector;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcLookAndFeel;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.definitions.DcFieldDefinitions;
import net.datacrow.settings.definitions.Definition;
import net.datacrow.settings.definitions.IDefinitions;
import net.datacrow.settings.definitions.ProgramDefinitions;
import net.datacrow.settings.definitions.QuickViewFieldDefinitions;
import net.datacrow.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * A Settings definition. The setting is always added to a file
 * (the settings file) and if specified, the setting is also editable in the SettingsView.
 * 
 * @author Robert Jan van der Waals
 */
public class Setting {
    
    private static Logger logger = Logger.getLogger(Setting.class.getName());

    private int dataType;
    private String key;
    private Object value;

    private int settingsGroup = 0; 
    private int parentGroup = -1; // no parent as default    
    private int componentType = ComponentFactory._SHORTTEXTFIELD;
    private String helpText = "";
    private boolean displayLabel = true; 
    private boolean showToUser = true;
    private String labelText;
    
    private boolean temporary = false;
    
    /**
     * Creates a setting
     * 
     * @param iDataType the data type of the value
     * @param sKey a unique identifier, also used for the settings file
     * @param oValue the default value for thsi setting
     * @param componentType the UI component
     * @param helptext helptext for this setting, used as tooltip
     * @param labeltext the display text for the label
     * @param displaylabel show the label on screen ?
     * @param showToUser is the setting meant to be display in the panel ?
     */
    public Setting(int dataType,
                   String key,
                   Object value,
                   int componentType,
                   String helpText,
                   String labelText,
                   boolean displayLabel,
                   boolean showToUser) {

        this.dataType = dataType;
        this.key = key;
        this.value = value;
        this.componentType = componentType;
        this.displayLabel = displayLabel;
        this.showToUser = showToUser;
        this.labelText = labelText;
        this.helpText = helpText;
    }
    
    public void isTemporary(boolean b) {
        this.temporary = b;
    }
    
    public boolean isTemporary() {
        return temporary;
    }
    
    public String getLabelText() {
        String s = DcResources.getText(labelText);
        return s == null ? labelText : s; 
    }
    
    public boolean displayLabel() {
        return displayLabel;   
    }
    
    public boolean showToUser() {
        return showToUser;   
    }
    
    public int getSettingsGroup() {
        return settingsGroup;   
    }

    public int getSettingsGroupParent() {
        return parentGroup;   
    }
    
    public JComponent getUIComponent() {
        if (componentType == ComponentFactory._COLORSELECTOR)
            return new DcColorSelector(key);
        
        return ComponentFactory.getComponent(-1, -1, componentType, getLabelText(), 400);
    }

    /**
     * The value type.
     * @return {@link DcRepository.ValueTypes}
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * The setting key ({@link DcRepository.Settings}, {@link DcRepository.ModuleSettings}).
     */
    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
    
    public String getHelpText() {
        String s = DcResources.getText(helpText);
        return s == null ? helpText : s;
    }

    /**
     * Creates a string representation of the value which can be used to store the value.
     * @return String representation of the value.
     */
    public String getValueAsString() {
        String toString = "";
        
        if (value != null) {
            switch (dataType) {
    	        case DcRepository.ValueTypes._DEFINITIONGROUP:
    	        	IDefinitions definitions = (IDefinitions) value;
    	        	for (Definition definition : definitions.getDefinitions()) {
                        toString += "{" + definition.toSettingValue() + "}";
    	        	}
    	            break;
                case DcRepository.ValueTypes._DIMENSION:
                    int x = (int) ((Dimension) value).getWidth();
                    int y = (int) ((Dimension) value).getHeight();
    
                    toString = "" + x + '/' + y;
                    break;
                case DcRepository.ValueTypes._FONT:
                    toString =  toString +
                                ((Font) value).getName() + '/' +
                                ((Font) value).getSize() + '/' +
                                ((Font) value).getStyle();
                    break;
                case DcRepository.ValueTypes._LOOKANDFEEL:
                    toString =  toString +
                                ((DcLookAndFeel) value).getClassName() + "%%" +
                                ((DcLookAndFeel) value).getFileName() + "%%" +
                                ((DcLookAndFeel) value).getName() + "%%" +
                                ((DcLookAndFeel) value).getType();
                    break;                
                case DcRepository.ValueTypes._COLOR:
                    toString =  toString +
                                ((Color) value).getRed() + '/' +
                                ((Color) value).getGreen() + '/' +
                                ((Color) value).getBlue();
                    break;
                case DcRepository.ValueTypes._LONG:
                    toString =  toString + value;
                    break;
                case DcRepository.ValueTypes._STRINGARRAY:
                    String[] sArray = (String[]) value;
                    for (int i = 0; i < sArray.length; i++) {
                        toString = toString + sArray[i] + '#';
                    }
                    break;
                case DcRepository.ValueTypes._INTEGERARRAY:
                    int[] iArray = (int[]) value;
                    if (iArray != null) {
                        for (int i = 0; i < iArray.length; i++) {
                            toString = toString + iArray[i] + '#';
                        }
                    }
                    break;                
                default:
                    toString =  toString + (value != null ? value.toString() : "");
            }
        }
        return toString;
    }

    public void setValue(Object o) {
        this.value = o;
    }

    /**
     * Allows a string representation to be set as a value. The string
     * will be parsed. The result will be set as the actual value for this setting.
     * @param s String representation of the value.
     */
    public void setStringAsValue(String s) {
        String sValue = s;
        try {
            switch (dataType) {

            case DcRepository.ValueTypes._DEFINITIONGROUP:
                    int group = sValue.indexOf("}");
                    IDefinitions definitions = (IDefinitions) getValue();
                    
                    if (definitions == null) {
                        if (key.equals(DcRepository.Settings.stProgramDefinitions))
                            definitions = new ProgramDefinitions();
                        else if (key.equals(DcRepository.ModuleSettings.stQuickViewFieldDefinitions))
                            definitions = new QuickViewFieldDefinitions();
                        else if (key.equals(DcRepository.ModuleSettings.stWebFieldDefinitions))
                            definitions = new QuickViewFieldDefinitions();
                        else 
                            definitions = new DcFieldDefinitions();
                    }
                    
                    while (group > -1) {
                        String value = sValue.substring(1, group);
                        definitions.add(value);
                        if (sValue.length() > group + 1) {
                            sValue = sValue.substring(group + 1, sValue.length());
                            group = sValue.indexOf("}");
                        } else {
                            group = -1;
                        }
                    }
                    
                    if (definitions.getSize() > 0)
                        value = definitions;

                    break;
                case DcRepository.ValueTypes._DIMENSION:
                    int index1 = sValue.indexOf("/");
                    String testX = sValue.substring(0, index1);
                    String testY = sValue.substring(index1 + 1, sValue.length());

                    int x = Integer.valueOf(testX).intValue();
                    int y = Integer.valueOf(testY).intValue();

                    value = new Dimension(x, y);
                    break;
                case DcRepository.ValueTypes._COLOR:
                    String[] colorValue = StringUtils.getListElements(sValue, "/");
                    int red = Integer.valueOf(colorValue[0]).intValue();
                    int green = Integer.valueOf(colorValue[1]).intValue();
                    int blue = Integer.valueOf(colorValue[2]).intValue();
                    value = new Color(red, green, blue);
                    break;
                case DcRepository.ValueTypes._FONT:
                    String[] fontValues = StringUtils.getListElements(sValue, "/");
                    String fontName = fontValues[0];
                    int size = Integer.valueOf(fontValues[1]).intValue();
                    int style = Integer.valueOf(fontValues[2]).intValue();
                	value = new Font(fontName, style, size);
                    break;
                case DcRepository.ValueTypes._LOOKANDFEEL:  
                    String[] lafValues =  StringUtils.getListElements(sValue, "%%");
                    String className = lafValues[0];
                    String fileName = lafValues[1];
                    String name = lafValues[2];
                    int lafType = Integer.valueOf(lafValues[3]).intValue();
                    value = new DcLookAndFeel(name, className, fileName, lafType);
                    break;                    
                case DcRepository.ValueTypes._BIGINTEGER:
                case DcRepository.ValueTypes._LONG:
                    value = Long.valueOf(sValue);
                    break;
                case DcRepository.ValueTypes._BOOLEAN:
                    value = Boolean.valueOf(sValue);
                    break;
                case DcRepository.ValueTypes._INTEGERARRAY:
                    Collection<Integer> cInt = new ArrayList<Integer>(0);
                    int index = sValue.indexOf("#");
                    while (index > -1) {
                        cInt.add(Integer.valueOf(sValue.substring(0, index)));
                        sValue = sValue.substring(index + 1, sValue.length());
                        index = sValue.indexOf("#");
                    }
                    Integer[] iFields = cInt.toArray(new Integer[0]);
                    int[] intArray = new int[iFields.length];
                    for (int i = 0; i < iFields.length; i++) {
                        intArray[i] = iFields[i].intValue();
                    }
                    value = intArray;
                    break;                    
                case DcRepository.ValueTypes._STRINGARRAY:
                    Vector<String> fields = new Vector<String>(0);
                    index = sValue.indexOf("#");
                    while (index > -1) {
                        fields.add(sValue.substring(0, index));
                        sValue = sValue.substring(index + 1, sValue.length());
                        index = sValue.indexOf("#");
                    }

                    String[] sFields = new String[fields.size()];
                    value = fields.toArray(sFields);
                    break;
                default:
                    value = sValue;
            }
        } catch (Exception e) {
            logger.error("An error occurred while converting [" + s + "] to a valid " +
                         "settings value", e);
        }
    }
}
