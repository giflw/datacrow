package net.datacrow.console.components.panels;

import net.datacrow.core.objects.DcField;

public interface IFieldSelectionListener {
    
    void fieldSelected(DcField field);
    
    void fieldDeselected(DcField field);
}
