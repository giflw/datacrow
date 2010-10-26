package net.datacrow.console.components.panels.tree;

import java.io.File;

import javax.swing.ImageIcon;

import net.datacrow.core.IconLibrary;

public class FileNodeElement extends NodeElement {
    
    private File file;
    
    public FileNodeElement(String key, File file) {
    	super(key, key, null);
    	this.file = file;
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
    public String toString() {
    	int count = getCount();
        if (count <= 1) 
            return getDisplayValue();
        else 
            return getDisplayValue() + " (" + String.valueOf(count) + ")";    
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
