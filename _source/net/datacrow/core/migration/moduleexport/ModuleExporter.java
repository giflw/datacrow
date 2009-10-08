package net.datacrow.core.migration.moduleexport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import net.datacrow.core.DataCrow;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.migration.itemexport.IItemExporterClient;
import net.datacrow.core.migration.itemexport.ItemExporterSettings;
import net.datacrow.core.migration.itemexport.XmlExporter;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.xml.XmlModule;
import net.datacrow.core.objects.DcObject;
import net.datacrow.util.Utilities;
import net.datacrow.util.zip.ZipFile;

public class ModuleExporter {

	private int module;
	
	private boolean exportRelatedMods = false;
	private boolean exportData = false;
	private boolean exportDataRelatedMods = false;
	
	private File path;
	
	private Exporter exporter;
	
	public ModuleExporter(int module, File path) {
		this.module = module;
		this.path = path;
	}

	public void setExportRelatedMods(boolean exportRelatedMods) {
		this.exportRelatedMods = exportRelatedMods;
	}

	public void setExportData(boolean exportData) {
		this.exportData = exportData;
	}

	public void setExportDataRelatedMods(boolean exportDataRelatedMods) {
		this.exportDataRelatedMods = exportDataRelatedMods;
	}
	
	private int getModule() {
		return module;
	}

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

	public void start(IModuleExporterClient client) {
		Exporter exporter = new Exporter(client, this);
		exporter.start();
	}
	
	public void cancel() {
		if (exporter != null)
			exporter.cancel();
	}
	
	private class Exporter extends Thread implements IItemExporterClient {
		
		private IModuleExporterClient client;
		private ModuleExporter parent;
		
		private boolean canceled = false;
		
		protected Exporter(IModuleExporterClient client, ModuleExporter parent) {
			this.client = client;
			this.parent = parent;
		}
		
		public void cancel() {
			canceled = true;
		}
		
		public void run() {
			Collection<DcModule> modules = new ArrayList<DcModule>();
			modules.add(DcModules.get(module));
			
			if (parent.isExportRelatedMods()) {
				for (DcModule reference : DcModules.getReferencedModules(module)) {
					if (reference.isCustomModule())
						modules.add(reference);
				}
			}

			try {
				ZipFile zf = new ZipFile(new File(parent.getPath(), DcModules.get(module).getName().toLowerCase() + "_export.zip"));
			
				for (DcModule module : modules) {
					
					if (canceled) break;
					
					XmlModule xmlModule = module.getXmlModule();
					if (xmlModule != null) {
					    String modName = xmlModule.getName().toLowerCase();
						String jarName = xmlModule.getJarFilename();
						byte[] content = Utilities.readFile(new File(DataCrow.moduleDir, jarName));
						zf.addEntry(jarName, content);
						
						if ((module.getIndex() == parent.getModule() && parent.isExportData()) ||
						    (module.getIndex() != parent.getModule() && parent.isExportDataRelatedMods())) {
						
						    exportData(module.getIndex());
						    File file = new File(parent.getPath(), modName + ".xml");
						    if (file.exists() && !canceled) {
						        byte[] data = Utilities.readFile(file);
						        zf.addEntry(modName + ".xml", data);
						        
						        File imgPath = new File(file.getPath(), modName + "_images");
						        if (imgPath.exists()) {
						            for (String image : imgPath.list()) {
						                
						                if (canceled) break;
						                
						                byte[] img = Utilities.readFile(new File(imgPath.toString(), image));
						                zf.addEntry(modName + "_" + image, img);
						            }
						        }
						    }
						}
					}
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
		}

		public void notifyProcessed() {
		}

		public void notifyStarted(int count) {
			
		}

		public void notifyStopped() {
		}
		
		private void exportData(int module) throws Exception {
			XmlModule xmlModule = DcModules.get(module).getXmlModule();
			String modName = xmlModule.getName().toLowerCase();
			
			XmlExporter itemExporter = new XmlExporter(parent.getModule(), XmlExporter._MODE_NON_THREADED);
			
			ItemExporterSettings settings = new ItemExporterSettings();
			settings.set(ItemExporterSettings._COPY_IMAGES, Boolean.TRUE);
			settings.set(ItemExporterSettings._ALLOWRELATIVEIMAGEPATHS, Boolean.TRUE);
			settings.set(ItemExporterSettings._SCALE_IMAGES, Boolean.FALSE);
			itemExporter.setSettings(settings);
			
			itemExporter.setFile(new File(parent.getPath(), modName + ".xml"));
			Collection<DcObject> items = new ArrayList<DcObject>();
			for (DcObject item : DataManager.get(module, null)) 
				items.add(item);
			
			itemExporter.setItems(items);
			itemExporter.setClient(this);
			itemExporter.start();
		}
	}
}
