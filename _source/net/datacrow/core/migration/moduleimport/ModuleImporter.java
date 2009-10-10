package net.datacrow.core.migration.moduleimport;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.datacrow.core.DataCrow;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.migration.IModuleWizardClient;
import net.datacrow.core.migration.itemimport.ItemImporterHelper;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.ModuleJar;
import net.datacrow.core.modules.xml.XmlField;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

/**
 * This importer is capable of importing a module and its related information.
 * The importer takes care of clashes with the module indices. The actual data import
 * is managed by the startup process ({@link DcModule#getDefaultData()}).
 * 
 * @author Robert Jan van der Waals
 */
public class ModuleImporter {

	private File file;
	
	private Importer importer;
	
	public ModuleImporter(File file) {
		this.file = file;
	}
	
	public void start(IModuleWizardClient client) {
	    Importer importer = new Importer(client, file);
		importer.start();
	}
	
	public void cancel() {
		if (importer != null)
		    importer.cancel();
	}
	
	private class Importer extends Thread {
		
		private boolean canceled = false;
		
		private File file;
		private IModuleWizardClient client;
		
		protected Importer(IModuleWizardClient client, File file) {
			this.client = client;
			this.file = file;
		}
		
		public void cancel() {
			canceled = true;
		}
		
		@Override
		public void run() {
		    try {
		        
		        client.notifyNewTask();
		        
    		    ZipFile zf = new ZipFile(file);
    
                Collection<ModuleJar> modules = new ArrayList<ModuleJar>();
                Map<String, Collection<DcImageIcon>> icons = new HashMap<String, Collection<DcImageIcon>>();
                Map<String, File> data = new HashMap<String, File>();
                
                Enumeration<? extends ZipEntry> list = zf.entries();
                
                client.notifyStarted(zf.size());
                while (list.hasMoreElements() && !canceled) {
                    ZipEntry ze = list.nextElement();
    
                    BufferedInputStream bis = new BufferedInputStream(zf.getInputStream(ze));
                    String name = ze.getName();
                    
                    client.notifyMessage(DcResources.getText("msgProcessingFileX", name));
                    
                    if (name.toLowerCase().endsWith(".jpg")) {
                        String moduleName = name.substring(0, name.indexOf("_"));
                        Collection<DcImageIcon> c = icons.get(moduleName);
                        c = c == null ? new ArrayList<DcImageIcon>() : c;
                        
                        int size = (int) ze.getSize();
                        byte[] bytes = new byte[size];
                        bis.read(bytes);
                        
                        DcImageIcon icon = new DcImageIcon(bytes);
                        icon.setFilename(name.substring(name.indexOf("_") + 1));
                        
                        c.add(icon);
                        icons.put(moduleName, c);
                    } else if (name.toLowerCase().endsWith(".xml")) {
                        File file =  new File(System.getProperty("java.io.tmpdir"), name);
                        writeToFile(bis, file);
                        file.deleteOnExit();
                        data.put(name.substring(0, name.toLowerCase().indexOf(".xml")), file);
                    } else if (name.toLowerCase().endsWith(".jar")) {
                        // check if the custom module does not already exist
                        if (DcModules.get(name.substring(0, name.indexOf(".jar"))) == null) {
                            writeToFile(bis, new File(DataCrow.moduleDir, name));
                            ModuleJar moduleJar = new ModuleJar(name);
                            moduleJar.load();
                            modules.add(moduleJar);
                        }
                    }
                    client.notifyProcessed();
                    bis.close();
                }
                
                processData(icons, data);
                reindexModules(modules);

		    } catch (Exception e) {
		        client.notifyError(e);
		    }
		    
		    client.notifyFinished();
		}
		
		private void reindexModules(Collection<ModuleJar> modules) {
		    for (ModuleJar mj : modules) {
		        XmlModule xm = mj.getModule();
		        
		        // module does not exist
		        if (DcModules.get(xm.getIndex()) == null) continue;

	            int oldIdx = xm.getIndex();
	            int newIdx = DcModules.getAvailableIdx(xm);
	            
	            xm.setIndex(newIdx);

	            // module does already exist and needs to be renumbered
                // all references must also be updated..
	            for (ModuleJar other : modules) {
	                
	                if (other == mj) continue;
	                
	                for (XmlField field : other.getModule().getFields()) {
	                    if (field.getModuleReference() == oldIdx)
	                        field.setModuleReference(newIdx);
	                }
	            }
		    }
		}
		
		private void processData(Map<String, Collection<DcImageIcon>> icons, 
		                         Map<String, File> data) {
		    
		    client.notifyStarted(data.size());
		    
		    client.notifyMessage(DcResources.getText("msgProcessingModuleItems"));
		    
		    for (String key : data.keySet()) {
		        DcModule module = DcModules.get(key);
		        
		        client.notifyMessage(DcResources.getText("msgProcessingItemsForX", key));
		        
                // for existing module data has to be loaded manually.
                // new modules can use the demo data / default data functionality.
		        if (module != null) {
		            
		            client.notifyMessage(DcResources.getText("msgModuleExistsMergingItems"));
		            
		            // place the images in the correct folder
		            storeImages(key, icons.get(key), true);
		            // start the import process
		            loadItems(data.get(key), module.getIndex());
		            
		            
		        } else { 
		            try {
		                client.notifyMessage(DcResources.getText("msgModuleIsNewCreatingItems"));
		                storeImages(key, icons.get(key), false);
                        Utilities.rename(file, new File(DataCrow.moduleDir + "data", key + ".xml"));
                    } catch (IOException e) {
                        client.notifyError(e);
                    }
		        }
		        client.notifyProcessed();
		    }
		}
		
		private void storeImages(String moduleName, Collection<DcImageIcon> icons, boolean temp) {

		    if (icons == null || icons.size() == 0) return;
		    
		    File dir;
		    if (temp) {
	            String tempDir = System.getProperty("java.io.tmpdir");
	            dir = new File(tempDir, moduleName.toLowerCase() + "_images");
		    } else {
		        dir = new File(DataCrow.moduleDir + "data", moduleName.toLowerCase() + "_images");
		    }
		    dir.mkdirs();
		    
		    client.notifyStartedSubProcess(icons.size());
		    client.notifyMessage(DcResources.getText("msgSavingImagesTo", dir.toString()));
		    
		    for (DcImageIcon icon : icons) {
		        try {
		            Utilities.writeToFile(icon.getBytes(), new File(dir, icon.getFilename()));
		            client.notifyMessage(DcResources.getText("msgSavingImage", icon.getFilename()));
		            client.notifySubProcessed();
		        } catch (Exception e) {
		            client.notifyError(e);
		        }
		    }
		    client.notifyFinishedSubProcess();
		}
		
		private void loadItems(File file, int moduleIdx) {
		    try {
    	        
		        client.notifyMessage("Loading items");
		        
		        ItemImporterHelper reader = new ItemImporterHelper("XML", moduleIdx, file);
                reader.start();
                Collection<DcObject> items = reader.getItems();
                reader.clear();
                
                client.notifyStartedSubProcess(items.size());
                
                for (DcObject item : items) {
                    DcObject other = DataManager.getObjectForDisplayValue(item.getModule().getIndex(), item.toString());
                    // Check if the item exists and if so, update the item with the found values. Else just create a new item.
                    // This is to make sure the order in which XML files are processed (first software, then categories)
                    // is of no importance (!).
                    try {
                        if (other != null) {
                            client.notifyMessage(DcResources.getText("msgItemExistsMerged", other.toString()));
                            other.copy(item, true);
                            other.saveUpdate(true, false);
                        } else {
                            client.notifyMessage(DcResources.getText("msgItemNoExistsCreated", item.toString()));
                            item.setValidate(false);
                            item.saveNew(true);
                        }
                    } catch (ValidationException ignore) {} // cannot occur (for real!)
                    
                    client.notifySubProcessed();
                }
		    } catch (Exception e) {
		        client.notifyError(e);
		    }
		}
		
		private void writeToFile(InputStream is, File file) throws IOException{
            FileOutputStream fos = new FileOutputStream(file);
            
            int b;
            while ((b = is.read())!= -1)
                fos.write(b);
            
            fos.flush();
            fos.close();
		}
	}
}
