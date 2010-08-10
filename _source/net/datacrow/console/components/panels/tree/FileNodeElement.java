package net.datacrow.console.components.panels.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import net.datacrow.core.IconLibrary;

public class FileNodeElement extends NodeElement {
    
    private File file;
    
    public FileNodeElement(int module, String key) {
    	super(module, key, null, null);
    }
    
    @Override
    public List<String> getItems(List<NodeElement> parents) {
        return new ArrayList<String>();
    }

    @Override
    public String getWhereClause() {
        return null;
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

    @Override
    public int getCount() {
        return 0;
    }
}
