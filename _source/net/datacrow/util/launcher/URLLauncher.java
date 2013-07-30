package net.datacrow.util.launcher;

import java.awt.Desktop;
import java.net.URL;

import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.DcSwingUtilities;
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
				runCmd(new String[] {browserPath, url.toString()});
				launched = true;
			} catch (Exception e) {
			    logger.debug("Failed to launch "  + browserPath + " " + url.toString(), e);
			}
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
	                    DcSwingUtilities.displayWarningMessage(DcResources.getText("msgCannotLaunchURLNoBrowserPath", url.toString()));
	            	else 
                        DcSwingUtilities.displayWarningMessage(DcResources.getText("msgCannotLaunchURL", url.toString()));
	            }
	        }
		}
	}
}
