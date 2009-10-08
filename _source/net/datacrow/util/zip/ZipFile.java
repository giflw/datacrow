package net.datacrow.util.zip;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public class ZipFile {

	private static Logger logger = Logger.getLogger(ZipFile.class.getName());
	
	private ZipOutputStream zout;
	private FileOutputStream fos;
	private BufferedOutputStream bos;
	
	public ZipFile(String path, String filename) throws FileNotFoundException {
		this(new File(path + filename));
	}

	public ZipFile(File file) throws FileNotFoundException {
		fos = new FileOutputStream(file);
        bos = new BufferedOutputStream(fos);
        zout = new ZipOutputStream(bos);
	}
	
    public void addEntry(String name, byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        
        ZipEntry ze = new ZipEntry(name);
        zout.putNextEntry(ze);
        
        byte b[] = new byte[512];
        int len = 0;
        while ((len = bais.read(b)) != -1) {
            zout.write(b, 0, len);
        }
        
        zout.closeEntry();
        bais.close();
    }	
    
    public void close() {
    	try {
    		zout.close();
    		bos.close();
    		fos.close();
    	} catch (Exception e) {
    		logger.debug("Could not close zip file streams", e);
    	}
    }
}
