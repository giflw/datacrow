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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.swing.ImageIcon;

import net.datacrow.console.ComponentFactory;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.util.Base64;
import net.datacrow.util.Converter;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Rating;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * The value class represents a field value.
 * It knows when it has been changed.
 * 
 * @author Robert Jan van der Waals
 */
public class DcValue implements Serializable {

    private static final long serialVersionUID = 3222707222963657152L;

    private static Logger logger = Logger.getLogger(DcValue.class.getName());
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    
    private boolean changed = false;
    
    private String displayString = "";
    private Object value = null;

    /**
     * Indicates if the value has been changed.
     * @return
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * Marks the value as changed.
     * @param b
     */
    public void setChanged(boolean b) {
        changed = b;
        
        if (!b && value instanceof Picture && value != null)
            ((Picture) value).markAsUnchanged();
    }

    /**
     * Bypasses all checks and sets the value directly.
     * @param newValue The new value to be used.
     * @param field The field for which the value is set.
     */
    public void setValueLowLevel(Object newValue, DcField field) {
        if (!field.isUiOnly())
            setChanged(true);
        
        setValueNative(newValue, field);
    }
    
    /**
     * Sets the new value for this object.
     * @param o The new value.
     * @param field The field for which the value is set.
     */
    @SuppressWarnings("unchecked")
    public void setValue(Object o, DcField field) {
        if (!field.isUiOnly()) 
            setChanged(true);
        else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION)
            setChanged(true);

        if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {

            if (o instanceof Picture) {
                setValueNative(o, field);    
            } else {
                Picture picture = value == null ? new Picture() : (Picture) value;
                value = picture; 

                ImageIcon currentImage = (ImageIcon) picture.getValue(Picture._D_IMAGE);
                ImageIcon newImage = o instanceof ImageIcon ? (ImageIcon) o : 
                	                 o instanceof byte[] ? new DcImageIcon((byte[]) o) : null;

                if (currentImage != newImage) {
                	// prevent empty and incorrect images to be saved
		        	if (	newImage != null && 
		        			newImage.getIconHeight() != 0 && 
		        			newImage.getIconWidth() != 0) {
		        		
		                picture.setValue(Picture._D_IMAGE, o);
		                if (currentImage != null) 
		                	picture.isUpdated(true);
		                else
		                	picture.isNew(true);
		                
                        setValueNative(picture, field);
		            } else if (currentImage != null) {
		                ((Picture) value).isDeleted(true);
                        setValueNative(picture, field);
                    }
	            }
            }
       } else if (field.getValueType() == DcRepository.ValueTypes._ICON) {
    	   if (o instanceof DcImageIcon) {
    		   byte[] bytes = ((DcImageIcon) o).getBytes();
    		   setValueNative(new String(Base64.encode(bytes)), field);
    	   } else if (o != null && o instanceof byte[] && ((byte[]) o).length > 0)
               setValueNative(new String(Base64.encode((byte[]) o)), field);
           else 
               setValueNative(o, field);
        } else {
            if (o == null) {
                setValueNative(null, field);
            } else {
                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    if (o instanceof Collection) // always create a new arraylist
                        setValueNative(new ArrayList<DcMapping>((Collection) o), field);
                } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                    if (o instanceof DcObject)
                        setValueNative(o, field);
                    else
                        setValueNative(DataManager.getObject(field.getReferenceIdx(), o.toString()), field);

                    if (getValue() == null && !o.equals(""))
                        setValueNative(o, field); // allow string reference to be set

                } else if ( (field.getValueType() == DcRepository.ValueTypes._LONG ||
                             field.getValueType() == DcRepository.ValueTypes._DOUBLE ) 
                             && !Utilities.isEmpty(o)) {
                    try {
                        if (field.getFieldType() == ComponentFactory._FILESIZEFIELD) {
                            if (o instanceof Long) {
                                setValueNative(o, field);
                            } else if (o instanceof Number) {
                                setValueNative(Long.valueOf(((Number) o).intValue()), field);
                            } else if (o instanceof String && ((String) o).trim().length() > 0) {
                                String num = "";
                                for (char c : ((String) o).toCharArray()) {
                                    if (Character.isDigit(c))
                                        num += c; 
                                }
                                setValueNative(Long.valueOf(num), field);
                            } else {
                                throw new NumberFormatException();
                            }
                        }
                        
                        if (field.getValueType() == DcRepository.ValueTypes._LONG) {
                            if (o instanceof Long)
                                setValueNative(o, field);
                            else if (o instanceof Number)
                                setValueNative(Long.valueOf(((Number) o).intValue()), field);
                            else if (o instanceof String && ((String) o).trim().length() > 0)
                                setValueNative(Long.valueOf(((String) o).trim()), field);
                            else
                                throw new NumberFormatException();
                        }
                        
                        if (field.getValueType() == DcRepository.ValueTypes._DOUBLE) {
                            if (o instanceof Double) {
                                setValueNative(o, field);
                            } else if (o instanceof Number) {
                                setValueNative(new Double(((Number) o).doubleValue()), field);
                            } else if (o instanceof String && ((String) o).trim().length() > 0) {
                                setValueNative(Double.valueOf(((String) o).trim()), field);
                            } else {
                                throw new NumberFormatException();
                            }
                        }
                        
                    } catch (Exception e) {
                        logger.error("Could not set " + o + " for " + field + ". Not a number and invalid String.", e);
                        setValueNative(null, field); 
                    }
                } else if (field.getValueType() == DcRepository.ValueTypes._STRING) {
                    String s = Converter.databaseValueConverter((o instanceof String ? (String) o : o.toString()));
                    s = field.getMaximumLength() > 0 && s.length() > field.getMaximumLength() ?
                        s.substring(0, field.getMaximumLength()) : s;

                    setValueNative(s, field);         
                } else if (field.getValueType() == DcRepository.ValueTypes._DATE) {
                    if (o instanceof Date) {
                        setValueNative(o, field);
                    } else if (o instanceof String) {
                        try {
                            Date date = formatter.parse((String) o);
                            setValueNative(date, field);
                        } catch (java.text.ParseException e) {
                            logger.error("Could not parse date for field " + field.getLabel(), e);
                        }
                    }
                } else {
                    // for all other cases: just set the value
                    setValueNative(o, field); 
                }
            }
        }
    }
    
    private void setValueNative(Object value, DcField field) {
        this.value = value;
        this.displayString = createDisplayString(field);
        this.changed = true;
    }

    /**
     * Returns the stored display string.
     */
    public String getDisplayString() {
        return displayString;
    }
    
    /**
     * Clears the value and sets it to null.
     */
    public void clear() {
        if (value != null && value instanceof Picture)
            ((Picture) value).unload();

        displayString = null;
        value = null;
    }
    
    public Object getValue() {
        return value;
    }

    /**
     * Creates a string representation.
     */
    public String getValueAsString() {
        return value != null ? value.toString() : "";
    }
    
    @SuppressWarnings("unchecked")
    private String createDisplayString(DcField field) {
        Object o = getValue();
        String text = "";

        try {
            if (!Utilities.isEmpty(o)) {
                if (field.getFieldType() == ComponentFactory._REFERENCESFIELD) {
                    Collection<DcMapping> mappings = (Collection<DcMapping>) o;
                    if (mappings != null) {
                        boolean first = true;
                        
                        for (DcMapping mapping : mappings) {
                            if (!first) text += ", ";
                            
                            text += mapping;
                            first = false;
                        }
                    }
                } else if (field.getFieldType() == ComponentFactory._RATINGCOMBOBOX) {
                    int value = o != null ? ((Long) o).intValue() : -1; 
                    text = Rating.getLabel(value);
    
                } else if (field.getFieldType() == ComponentFactory._TIMEFIELD) {
                    Calendar cal = Calendar.getInstance();
                    cal.clear();
    
                    int value = 0;
                        
                    if (o instanceof String)
                        value = Integer.parseInt((String) o);
                    
                    if (o instanceof Long)
                        value = ((Long) o).intValue();
    
                    int minutes = 0;
                    int seconds = 0;
                    int hours = 0;
    
                    if (value != 0) {
                        cal.set(Calendar.SECOND, value);
                        minutes = cal.get(Calendar.MINUTE);
                        seconds = cal.get(Calendar.SECOND);
                        hours = cal.get(Calendar.HOUR);
                    }
    
                    String sSeconds = getDoubleDigitString(seconds);
                    String sMinutes = getDoubleDigitString(minutes);
                    text = "" + hours + ":" + sMinutes + ":" + sSeconds;
                } else if (field.getValueType() == DcRepository.ValueTypes._DOUBLE) {
                    text = Utilities.toString((Double) o);
                } else if (field.getFieldType() == ComponentFactory._FILESIZEFIELD) {
                    text = Utilities.toFileSizeString((Long) o);
                } else {
                	text = o == null ? "" : o instanceof String ? (String) o : o.toString();
                }
            }
        } catch (Exception e) {
            logger.error("Error while creating the display string for field " + field + ", value " + o, e);
        }

        return text;
    }

    private String getDoubleDigitString(int value) {
        StringBuffer sb = new StringBuffer();
        if (value == 0) {
            sb.append("00");
        } else if (value < 10) {
            sb.append("0");
            sb.append(value);
        } else {
            sb.append(value);
        }
        return sb.toString();
    }
}
