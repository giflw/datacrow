package net.datacrow.core.objects;

import net.datacrow.util.DcImageIcon;

public class DcSimpleValue {
    
    private String name;
    private String ID;
    private DcImageIcon icon;
    
    public DcSimpleValue(String ID, String name) {
        this(ID, name, null);
    }
    
    public DcSimpleValue(String ID, String name, DcImageIcon icon) {
        this.name = name;
        this.ID = ID;
        this.icon = icon;
    }
    
    public void setIcon(DcImageIcon icon) {
        this.icon = icon;
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return name;
    }
    
    public DcImageIcon getIcon() {
        return icon;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof DcSimpleValue && ((DcSimpleValue) o).getID().equals(getID());
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    protected void finalize() throws Throwable {
        ID = null;
        name = null;
        
        if (icon != null) {
            icon.flush();
            icon = null;
        }
        
        super.finalize();
    }
}