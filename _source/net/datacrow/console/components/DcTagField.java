package net.datacrow.console.components;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import net.datacrow.console.components.painter.RectanglePainter;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcTag;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class DcTagField extends JTextArea implements IComponent, KeyListener, MouseListener {
    
    private static Logger logger = Logger.getLogger(DcTagField.class.getName());
    private static final RectanglePainter red = new RectanglePainter(new Color(153, 204, 255));
    
    private Collection<DcObject> references = new ArrayList<DcObject>();
    
    private int mappingModIdx;
    
    public DcTagField(int mappingModIdx) {
        super();
        
        this.mappingModIdx = mappingModIdx;
        this.addKeyListener(this);
        
        List<String> items = new ArrayList<String>();
        for (DcObject dco : DataManager.getTags()) 
            items.add(dco.toString());
    }
    
    @Override
    public void clear() {}
    
    private void removeWord() {
        if (getTextLength() > 0) {
            if (Utilities.isEmpty(getSelectedText())) {
                setSelectionStart(getLastTagStart());
                setSelectionEnd(getText().length());
            }
            cut();
        }
    }
    
    @Override
    public Object getValue() {
        Collection<DcObject> mappings = new ArrayList<DcObject>();
        
        DcObject tag = null;
        DcMapping mapping;
        
        boolean found;
        for (String name : getTags()) {
            found = false;
            for (DcObject existingRef : references) {
                if (name.equals(existingRef.toString())) {
                    mappings.add(existingRef);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                try {
                    for (DcObject existingTag : DataManager.getTags()) {
                        if (name.equals(existingTag.toString())) {
                            tag = existingTag;
                            break;
                        }
                    }
                    
                    if (tag == null) {
                        tag = new DcTag();
                        tag.setValue(DcTag._A_NAME, name);
                        tag.setIDs();
                    }
                    
                    mapping = (DcMapping) DcModules.get(mappingModIdx).getItem();
                    mapping.setReference(tag);
                    mapping.setValue(DcMapping._B_REFERENCED_ID, tag.getID());
                    mappings.add(mapping);
                } catch (Exception e) {
                    logger.error("Error while saving Tag " + tag, e);
                }
            }
        }
        
        return mappings;
    }

    @Override
    public void setValue(Object o) {
        
        references = (Collection<DcObject>) o;
        
        StringBuffer sb = new StringBuffer("");
        for (DcObject tag : references) {
            sb.append(tag);
            sb.append(" ");   
        }
        
        setText(sb.toString());
        highlightTags();
    }
    
    private Collection<String> getTags() {
        String s = getText();
        StringTokenizer st = new StringTokenizer(s, " ");
        
        Collection<String> tags = new ArrayList<String>();
        String tag;
        while (st.hasMoreElements()) {
            tag = (String) st.nextElement();
            tag = tag.trim();
            
            if (!tags.contains(tag))
                tags.add(tag);
        }
        
        return tags;
    }

    private int getLastTagStart() {
        String s = getText();
        while (s.endsWith(" ")) 
            s = s.substring(0, s.length() - 1);
        
        int idx = s.lastIndexOf(" ");
        return idx == -1 ? 0 : idx;
    }
    
    private Collection<Point> getLocations() {
        String s = getText();
        StringTokenizer st = new StringTokenizer(s, " ");
        
        Collection<Point> locs = new ArrayList<Point>();
        
        int length = 0;
        String word;
        while (st.hasMoreElements()) {
            word = (String) st.nextElement();
            locs.add(new Point(length, length + word.length()));
            length += word.length() + 1;
        }
        
        return locs;
    }
    
    private void highlightTags() {
        for (Point point : getLocations()) {
            try {
                this.getHighlighter().addHighlight(point.x, point.y, red);
            } catch (BadLocationException ble) {
                logger.debug(ble, ble);
            }
        }
    }
    
    private char getLastChar() {
        String s = getText();
        return s== null || s.length() == 0 ? ' ' : s.charAt(s.length() - 1);
    }
    
    private int getTextLength() {
        String s = getText();
        return s == null ? 0 : s.length();
    }
    
    @Override
    public void keyTyped(KeyEvent ke) {}

    @Override
    public void keyPressed(KeyEvent ke) {} 
    
    @Override
    public void keyReleased(KeyEvent ke) {
       
        if (    ke.getKeyCode() == KeyEvent.VK_SPACE ||
                ke.getKeyCode() == KeyEvent.VK_TAB ||
                ke.getKeyCode() == KeyEvent.VK_ENTER) {
            highlightTags();
        } else if (ke.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
                ke.getKeyCode() == KeyEvent.VK_DELETE) {
            removeWord();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void refresh() {}
    
    
    @Override
    protected Document createDefaultModel() {
        return new TagDocument(this);
    }
    
    protected class TagDocument extends PlainDocument {
        
        private DcTagField fld;
        
        protected TagDocument(DcTagField fld) {
            this.fld = fld;
        }
        
        @Override
        public void insertString(int i, String s, AttributeSet attributeset) throws BadLocationException {
            if ((fld.getText().endsWith(" ") && (s.equals(" ") || s.equals("\t") || s.equals("\r") || s.equals("\n")))) {
                return;
            } else if (s.equals("\t") || s.equals("\n") || s.equals("\r")) {
                super.insertString(i, " ", attributeset);
            } else {
                super.insertString(i, s, attributeset);
            }
        }
    }
}
