package net.datacrow.console.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import net.datacrow.util.StringUtils;

public class DcFilePatternTextField extends DcShortTextField {

    public DcFilePatternTextField() {
        super(500);
    }
    
    @Override
    protected Document createDefaultModel() {
        return new ShortStringDocument();
    }
    
    protected class ShortStringDocument extends PlainDocument {
        
        @Override
        public void insertString(int i, String s, AttributeSet attributeset) throws BadLocationException {
            if (i + 1 < MAX_TEXT_LENGTH)
                super.insertString(i, StringUtils.normalize(s), attributeset);
        }
    }
}
