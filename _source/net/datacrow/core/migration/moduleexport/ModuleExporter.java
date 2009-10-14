package net.datacrow.core.migration.moduleexport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.DataCrow;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.migration.IModuleWizardClient;
import net.datacrow.core.migration.itemexport.IItemExporterClient;
import net.datacrow.core.migration.itemexport.ItemExporterSettings;
import net.datacrow.core.migration.itemexport.XmlExporter;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.ExternalReferenceModule;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.Utilities;
import net.datacrow.util.zip.ZipFile;

/**
 * This exporter is capable of exporting a module and the related information.
 * The related custom modules and the data can be exported (based on the user settings).
 * The exported module zip file can be imported using the ModuleImporter class.
 * 
 * @author Robert Jan van der Waals
 */
public class ModuleExporter {

	private int module;
	
	private boolean exportRelatedMods = false;
	private boolean exportData = false;
	private boolean exportDataRelatedMods = false;
	
	private File path;
	
	private Exporter exporter;
	
	/**
	 * Creates a new instance for the specific module. 
	 * The module must be a main module ({@link DcModule#isTopModule()}).
	 * @param module Main module
	 * @param path The export path
	 */
	public ModuleExporter(int module, File path) {
		this.module = module;
		this.path = path;
	}

	/**
	 * Indicates whether the related custom modules should also be exported.
	 * @param exportRelatedMods
	 */
	public void setExportRelatedMods(boolean exportRelatedMods) {
		this.exportRelatedMods = exportRelatedMods;
	}

    /**
     * Indicates whether the data (items) of the main module should be exported.
     * @param exportRelatedMods
     */	
	public void setExportData(boolean exportData) {
		this.exportData = exportData;
	}

    /**
     * Indicates whether the data (items) of the related modules should be exported.
     * @param exportRelatedMods
     */ 	
	public void setExportDataRelatedMods(boolean exportDataRelatedMods) {
		this.exportDataRelatedMods = exportDataRelatedMods;
	}
	
	/**
	 * The main module index.
	 */
	private int getModule() {
		return module;
	}

	/**
	 * The export path.
	 */
	private File getPath() {
		return path;
	}

	private boolean isExportRelatedMods() {
		return exportRelatedMods;
	}

	private boolean isExportData() {
		return exportData;
	}

	private boolean isExportDataRelatedMods() {
		return exportDataRelatedMods;
	}

	public void start(IModuleWizardClient client) {
		Exporter exporter = new Exporter(client, this);
		exporter.start();
	}
	
	public void cancel() {
		if (exporter != null)
			exporter.cancel();
	}
	
	private class Exporter extends Thread implements IItemExporterClient {
		
		private IModuleWizardClient client;
		private ModuleExporter parent;
		
		private boolean canceled = false;
		
		protected Exporter(IModuleWizardClient client, ModuleExporter parent) {
			this.client = client;
			this.parent = parent;
		}
		
		public void cancel() {
			canceled = true;
		}
		
		@Override
		public void run() {
		    
		    client.notifyNewTask();
		    
			Collection<DcModule> modules = new ArrayList<DcModule>();
			modules.add(DcModules.get(module));
			
			if (parent.isExportRelatedMods()) {
				for (DcModule reference : DcModules.getReferencedModules(module))
				    if (!reference.isAbstract() && 
				         reference.getIndex() != DcModules._CONTACTPERSON)
				        modules.add(reference);
			}

			client.notifyStarted(modules.size());
			
			try {
				ZipFile zf = new ZipFile(new File(parent.getPath(), DcModules.get(module).getName().toLowerCase() + "_export.zip"));
			
				for (DcModule module : modules) {
					
					if (canceled) break;
					
					String modName = module.getName().toLowerCase();
					
					// only export custom modules
					// adds the module jar file to the distribution
					if (module.isCustomModule() && module.getXmlModule() != null) {
    					XmlModule xmlModule = module.getXmlModule();
						String jarName = xmlModule.getJarFilename();
						byte[] content = Utilities.readFile(new File(DataCrow.moduleDir, jarName));
						zf.addEntry(jarName, content);
					}

					// settings export
					File file = module.getSettings().getSettings().getSettingsFile();
					module.getSettings().save();
					if (file.exists()) {
					    byte[] content = Utilities.readFile(file);
					    zf.addEntry(file.getName(), content);
					}
					
					// item export
					if (((module.getIndex() == parent.getModule() || (module instanceof ExternalReferenceModule)) && parent.isExportData()) ||
					   (!(module instanceof ExternalReferenceModule) && module.getIndex() != parent.getModule() && parent.isExportDataRelatedMods())) {
					
					    try {
    					    exportData(module.getIndex());
    					    
    					    // get the XML
    					    file = new File(parent.getPath(), modName + ".xml");
    					    if (file.exists() && !canceled) {
    					        byte[] data = Utilities.readFile(file);
    					        zf.addEntry(modName + ".xml", data);
    					        
    					        // get the images
    					        File imgPath = new File(parent.getPath(), modName + "_images");
    					        if (imgPath.exists()) {
    					            for (String image : imgPath.list()) {
    					                
    					                if (canceled) break;
    					                
    					                // add the image
    					                byte[] img = Utilities.readFile(new File(imgPath.toString(), image));
    					                zf.addEntry(modName + "_" + image, img);
    					            }
    					        }
    					    }
					    } catch (Exception e) {
					        client.notifyError(e);
					    }
					}
					client.notifyMessage(DcResources.getText("msgExportedModule", module.getLabel()));
					client.notifyProcessed();
				}
				
				zf.close();
			} catch (Exception e) {
				client.notifyError(e);
			}
			
			client.notifyFinished();
			client = null;
			parent = null;
		}

		public void notifyMessage(String message) {
		    client.notifyMessage(message);
		}

		public void notifyProcessed() {
		    client.notifySubProcessed();
		}

		public void notifyStarted(int count) {
		    client.notifyStartedSubProcess(count);
		}

		public void notifyStopped() {}
		
		private void exportData(int module) throws Exception {
		    
            Collection<DcObject> items = new ArrayList<DcObject>();
            for (DcObject item : DataManager.get(module, null)) 
                items.add(item);
            
            if (items.size() == 0)
                return;
		    
			String modName = DcModules.get(module).getName().toLowerCase();
			
			XmlExporter itemExporter = new XmlExporter(parent.getModule(), XmlExporter._MODE_NON_THREADED);
			
			ItemExporterSettings settings = new ItemExporterSettings();
			settings.set(ItemExporterSettings._COPY_IMAGES, Boolean.TRUE);
			settings.set(ItemExporterSettings._ALLOWRELATIVEIMAGEPATHS, Boolean.TRUE);
			settings.set(ItemExporterSettings._SCALE_IMAGES, Boolean.FALSE);
			itemExporter.setSettings(settings);
			
			itemExporter.setFile(new File(parent.getPath(), modName + ".xml"));

			
			itemExporter.setItems(items);
			itemExporter.setClient(this);
			itemExporter.start();
		}
	}
}
