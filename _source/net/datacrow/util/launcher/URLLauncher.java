package net.datacrow.util.launcher;

import java.awt.Desktop;
import java.net.URL;

import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class URLLauncher extends Launcher {

	private static Logger logger = Logger.getLogger(URLLauncher.class.getName());
	
	private URL url;
	
	public URLLauncher(URL url) {
		super();
		this.url = url;
	}

	@Override
	public void launch() {

		boolean launched = false;
		
		String browserPath = DcSettings.getString(DcRepository.Settings.stBrowserPath);
		
		if (!Utilities.isEmpty(browserPath)) {
			try {
				runCmd(browserPath + " " + url);
				launched = true;
			} catch (Exception e) {}
		}
		
		if (!launched) {
			Desktop desktop = getDesktop();
	        if (desktop != null) {
	            try {
	            	desktop.browse(url.toURI());
	            	launched = true;
	            } catch (Exception exp) {
	            	logger.debug("Could not launch URL using the Dekstop class [" + url + "]", exp);
	            	
	            	if (!Utilities.isEmpty(browserPath))
	            		new MessageBox(DcResources.getText("msgCannotLaunchURLNoBrowserPath", url.toString()), MessageBox._WARNING);
	            	else 
	            		new MessageBox(DcResources.getText("msgCannotLaunchURL", url.toString()), MessageBox._WARNING);
	            }
	        }
		}
	}
}
