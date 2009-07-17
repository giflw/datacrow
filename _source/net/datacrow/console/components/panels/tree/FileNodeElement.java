package net.datacrow.console.components.panels.tree;

import java.io.File;

import javax.swing.ImageIcon;

import net.datacrow.core.IconLibrary;
import net.datacrow.core.objects.DcObject;
import net.datacrow.util.Utilities;

public class FileNodeElement extends NodeElement {
    
    private File file;
    
    public FileNodeElement(int module, String key) {
    	super(module, key, null);
    }

    @Override
    public ImageIcon getIcon() {
    	if (file == null) {
    		return IconLibrary._icoOpen;
    	} else {
    		boolean isFile = file.isFile();
    		boolean isDirectory = file.isDirectory();
    		boolean exists = file.exists();
    		
    		return exists && isFile ? IconLibrary._icoFileSystemExists :
    			   exists && isDirectory ?  IconLibrary._icoFileSystemExists :
				  IconLibrary._icoFileSystemNotExists;
    	}
    }
    
    @Override
	public void addValue(DcObject dco) {
		super.addValue(dco);
		String filename = dco.getFilename();
		file = file == null && !Utilities.isEmpty(filename)? new File(filename) : file;
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
    public void clear() {
		super.clear();
		file = null;
    }
	
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof FileNodeElement))
            return false;
        else 
            return getKey().equals(((FileNodeElement) o).getKey());
    }
}
