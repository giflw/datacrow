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

package net.datacrow.console.components;

import java.awt.Graphics;
import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.menu.DcEditorMouseListener;
import net.datacrow.util.DcSwingUtilities;

public class DcLoginNameField extends JTextField implements IComponent {

    public DcLoginNameField() {
        super();
        addMouseListener(new DcEditorMouseListener());
        ComponentFactory.setBorder(this);
    }

    @Override
    protected Document createDefaultModel() {
        return new LoginNameDocument();
    }

    public Object getValue() {
        return getText();
    }

    public void clear() {} 
    
    public void setValue(Object o) {
        setText(o != null ? (String) o : "");
    }
    
    private static class LoginNameDocument extends PlainDocument {
        private final static Pattern CHARS = Pattern.compile("[^a-zA-Z]");

        @Override
        public void insertString(int offs, String s, AttributeSet a) throws BadLocationException {
            if (s != null && s.trim().length() > 0 && !CHARS.matcher(s).matches())
                super.insertString(offs, s, a);
        }
    } 
    
    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(DcSwingUtilities.setRenderingHint(g));
    }    
}
