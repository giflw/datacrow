package net.datacrow.core.web;


import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.web.model.DcWebField;
import net.datacrow.settings.definitions.WebFieldDefinition;
import net.datacrow.util.StringUtils;

/**
 * @author rwaals
 */
public abstract class WebUtilities {
    
    public static String getValue(DcObject dco, DcWebField wf, Object value) {
        return getValue(dco, wf.getIndex(), wf.getMaxTextLength(), value);
    }

    public static String getValue(DcObject dco, WebFieldDefinition def, Object value) {
        return getValue(dco, def.getField(), def.getMaxTextLength(), value);
    }

    private static String getValue(DcObject dco, int fieldIdx, int maxTextLength, Object value) {
        DcField field = dco.getField(fieldIdx);
        String s = "";
        s = value != null && field.getValueType() == DcRepository.ValueTypes._PICTURE ? 
                    "/mediaimages/" + ((Picture) value).getScaledFilename() : 
                    dco.getDisplayString(field.getIndex());

        if (maxTextLength != 0 && field.getValueType() != DcRepository.ValueTypes._PICTURE)
            s = StringUtils.concatUserFriendly(s, maxTextLength);
        
        return s;
    }
}
