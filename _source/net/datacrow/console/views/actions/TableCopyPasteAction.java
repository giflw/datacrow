package net.datacrow.console.views.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import net.datacrow.console.views.IViewComponent;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;

import org.apache.log4j.Logger;

public class TableCopyPasteAction implements ActionListener {
    
    private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    private static Logger logger = Logger.getLogger(TableCopyPasteAction.class.getName());
    
    public TableCopyPasteAction() {
    }
    
    public void actionPerformed(ActionEvent e) {
        IViewComponent vc = (IViewComponent) e.getSource();
        if (e.getActionCommand().equals("copy"))
            copy(vc);
        if (e.getActionCommand().equals("paste"))
            paste(vc);
    }
    
    private void paste(IViewComponent vc) {
        DcModule module = vc.getModule();
        
        int[] fields = (int[]) vc.getModule().getSetting(DcRepository.ModuleSettings.stTableColumnOrder);
        int updateIdx = vc.getSelectedIndex();
        
        try {
            String text = (String) (clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor));
            StringTokenizer rowTokenizer = new StringTokenizer(text, "\n");
            for (int i = 0; rowTokenizer.hasMoreTokens(); i++) {
                String values = rowTokenizer.nextToken();
                DcObject dco = module.getItem();

                int fldCounter = 0;
                int index = values.indexOf("\t");
                while (index > -1) {
                    String value = values.substring(0, index);
                    if (fldCounter <= fields.length) {
                        DcField field = dco.getField(fields[fldCounter]);
                        if (field.getValueType() == DcRepository.ValueTypes._LONG) {
                            try { 
                                dco.setValue(fields[fldCounter], Long.valueOf(value.trim()));
                            } catch (Exception e) {
                                logger.warn("Could not set " + value + " for field " + fields[fldCounter], e);
                            }
                        } else {
                            dco.setValue(fields[fldCounter], value);
                        }
                    }

                    fldCounter++;
                    values = values.substring(index + 1, values.length());
                    index = values.indexOf("\t");
                }
                dco.setValue(fields[fldCounter], values);
                vc.updateItemAt(updateIdx++, dco, true, true, true);
                dco.release();
            }
        }
        catch (Exception exp) {
            logger.error("An error occurred while pasting data into the table", exp);
        }
    }
    
    private void copy(IViewComponent vc) {
        StringBuffer sb = new StringBuffer();
        
        int[] fields = (int[]) vc.getModule().getSetting(DcRepository.ModuleSettings.stTableColumnOrder);
        for (int index : vc.getSelectedIndices()) {
            DcObject dco = vc.getItemAt(index);
            int fieldCount = 0;
            
            for (int field : fields) {
                if (fieldCount > 0)
                    sb.append("\t");

                Object o = dco.getValue(field);
                String value = o == null ||
                    dco.getField(field).getValueType() == DcRepository.ValueTypes._PICTURE ? "" : o.toString();
                sb.append(value);
                fieldCount++;
            }
            sb.append("\n");
        }
        StringSelection stSelection = new StringSelection(sb.toString());
        clipboard.setContents(stSelection, stSelection);        
    }
}
