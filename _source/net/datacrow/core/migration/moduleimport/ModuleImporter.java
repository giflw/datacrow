package net.datacrow.core.migration.moduleimport;

import java.io.File;

import net.datacrow.core.modules.DcModule;

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
	
	public void start(IModuleImporterClient client) {
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
		private IModuleImporterClient client;
		
		protected Importer(IModuleImporterClient client, File file) {
			this.client = client;
			this.file = file;
		}
		
		public void cancel() {
			canceled = true;
		}
		
		@Override
		public void run() {
			
		}
	}
}
