package net.datacrow.console.components;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;

import net.datacrow.console.components.painter.RectanglePainter;
import net.datacrow.console.menu.DcEditorPopupMenu;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcTag;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class DcTagField extends DcLongTextField implements KeyListener, MouseListener {
    
    private static Logger logger = Logger.getLogger(DcTagField.class.getName());
    
    private JPopupMenu jpm = new DcEditorPopupMenu(this);
    
    private RectanglePainter red = new RectanglePainter( Color.RED );
    private RectanglePainter cyan = new RectanglePainter( Color.CYAN );
    
    private Collection<DcObject> tags = new ArrayList<DcObject>();
    
    public DcTagField() {
        super();
        
        jpm.add(new JMenuItem("BLAAAHHH"));
        this.addKeyListener(this);
    }
    
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
    public Collection<DcObject> getValue() {
        return tags;
    }

    @Override
    public void setValue(Object o) {
        tags = (Collection<DcObject>) o;
        
        StringBuffer sb = new StringBuffer("");
        for (DcObject tag : tags) {
            sb.append(tag.getValue(DcTag._A_NAME));
            sb.append(" ");   
        }
        
        setText(sb.toString());
        highlightTags();
    }
    
    private Collection<String> getTags() {
        String s = getText();
        StringTokenizer st = new StringTokenizer(s, " ");
        
        Collection<String> tags = new ArrayList<String>();
        while (st.hasMoreElements()) {
            tags.add((String) st.nextElement());
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
    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_TAB) {
            insert(" ", getTextLength());
            ke.consume();
            highlightTags();
        } else if (ke.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
                   ke.getKeyCode() == KeyEvent.VK_DELETE) {
            removeWord();   
        }
    } 
    
    @Override
    public void keyReleased(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
            jpm.setVisible(false);
            highlightTags();
        } else if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
            jpm.setVisible(false);
        } else {
//            int loc = getCaretPosition();
//            String text = getText();
//            Point p = getCaret().getMagicCaretPosition();
//            SwingUtilities.convertPointToScreen(p, this);
//            jpm.setLocation(p);
//            jpm.setVisible(true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
    }
}
