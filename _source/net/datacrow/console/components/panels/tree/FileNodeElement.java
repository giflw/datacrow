package net.datacrow.console.components.panels.tree;

import java.io.File;

import javax.swing.ImageIcon;

import net.datacrow.core.IconLibrary;
import net.datacrow.core.objects.DcObject;
import net.datacrow.util.Utilities;

public class FileNodeElement extends NodeElement {
    
    private String path;
    
    public FileNodeElement(int module, String key) {
    	super(module, key, null);
    }

    @Override
    public ImageIcon getIcon() {
    	if (path == null) {
    		return IconLibrary._icoOpen;
    	} else {
    		File file = new File(path);
    		boolean isFile = file.isFile();
    		boolean isDirectory = file.isDirectory();
    		boolean exists = file.exists();
    		
    		return exists && isFile ? IconLibrary._icoAnchor :
    			   exists && isDirectory ?  IconLibrary._icoAccept :
				   IconLibrary._icoError;
    	}
    }
    
    @Override
	public void addValue(DcObject dco) {
		super.addValue(dco);
		path = Utilities.isEmpty(path) ? dco.getFilename() : path;
	}

	@Override
    public String toString() {
    	int count = getValues() == null ? 0 : getValues().size();
        if (count <= 1) 
            return getKey();
        else 
            return getKey() + " (" + String.valueOf(count) + ")";    
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof FileNodeElement))
            return false;
        else 
            return getKey().equals(((FileNodeElement) o).getKey());
    }
}
