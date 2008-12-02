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

package net.datacrow.core.modules.xml;

import java.lang.reflect.InvocationTargetException;

import net.datacrow.core.modules.InvalidValueException;
import net.datacrow.util.XMLParser;

import org.w3c.dom.Element;

/**
 * Representation of a XML structure.
 * Delivers additional parsing methods. 
 * 
 * @see XmlModule
 * 
 * @author Robert Jan van der Waals
 */
public abstract class XmlObject {
    
    @SuppressWarnings("unchecked")
    public Class getClass(Element element, String tag, boolean instantiationTest) throws InvalidValueException {
        String s = XMLParser.getString(element, tag);
        try {
            Class cl = s != null && s.trim().length() > 0 ? Class.forName(s) : null;
            if (cl != null && !instantiationTest)
                cl.getConstructors()[0].newInstance(new Object[] {});
            return cl;
        } catch (ClassNotFoundException e) {
            throw new InvalidValueException(tag, s);
        } catch (InstantiationException e) {
            throw new InvalidValueException("Could not instantiate [" + s + "] for " + tag);
        } catch (IllegalAccessException e) {
            throw new InvalidValueException("Could not instantiate [" + s + "] for " + tag);
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("Could not instantiate [" + s + "] for " + tag);        
        } catch (SecurityException e) {
            throw new InvalidValueException("Could not instantiate [" + s + "] for " + tag);
        } catch (InvocationTargetException e) {
            throw new InvalidValueException("Could not instantiate [" + s + "] for " + tag);
        }
    }
}
