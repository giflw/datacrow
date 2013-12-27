package net.datacrow.core.objects;

import net.datacrow.core.modules.DcModules;

import org.apache.log4j.Logger;

public class DcObjectPart extends DcObject {

    private static Logger logger = Logger.getLogger(DcObjectPart.class.getName());
    
    public static final int _B_FILESIZE = 2;
    public static final int _A_FILENAME = 1;
    public static final int _C_FILEHASH = 3;
    public static final int _D_FILEHASHTYPE = 4;
    
    /**
     * Creates a new instance
     */
    public DcObjectPart() {
        super(DcModules._OBJECTPART);
    }

    @Override
    public boolean hasPrimaryKey() {
        return true;
    }
   
    
    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        
        if (o instanceof DcObjectPart) {
            DcObjectPart file = (DcObjectPart) o;
            
            String filename1 = (String) file.getValue(DcObjectPart._A_FILENAME);
            String filename2 = (String) getValue(DcObjectPart._A_FILENAME);
            
            equals = o == this || filename1 == filename2 || (filename1 != null && filename1.equals(filename2));
        } else {
            equals = super.equals(o);
        }
        
        return equals;
   }
}
