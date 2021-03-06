/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
 *                                                                            *
 *                       This file is part of Data Crow.                      *
 *       Data Crow is free software; you can redistribute it and/or           *
 *        modify it under the terms of the GNU General Public                 *
 *       License as published by the Free Software Foundation; either         *
 *              version 3 of the License, or any later version.               *
 *                                                                            *
 *        Data Crow is distributed in the hope that it will be useful,        *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *           MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.             *
 *           See the GNU General Public License for more details.             *
 *                                                                            *
 *        You should have received a copy of the GNU General Public           *
 *  License along with this program. If not, see http://www.gnu.org/licenses  *
 *                                                                            *
 ******************************************************************************/

package net.datacrow.fileimporters;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.datacrow.console.windows.fileimport.FileImportDialog;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMediaObject;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.settings.Settings;
import net.datacrow.synchronizers.ISynchronizerClient;
import net.datacrow.synchronizers.Synchronizer;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Directory;
import net.datacrow.util.StringUtils;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

/**
 * Base for all file importers. A file importer is capable of scanning a specific
 * location for specific file types. These files are parsed and their information
 * is stored in a {@link DcObject}.
 * 
 * @author Robert Jan van der Waals
 */
public abstract class FileImporter implements ISynchronizerClient {

    private static Logger logger = Logger.getLogger(FileImporter.class.getName());
    
    private final int module;
    private IFileImportClient client;
    
    private Region region;
    private SearchMode sm;
    private IServer server;
    private DcObject container;
    
    public void setClient(IFileImportClient client) {
        this.client = client;
        this.region = client.getRegion();
        this.sm = client.getSearchMode();
        this.server = client.getServer();
        this.container = client.getDcContainer();
    }
    
    public IFileImportClient getClient() {
        return client;
    }
    
    /**
     * Creates a new instance.
     * @param module The module to which this importer belongs.
     */
    public FileImporter(int module) {
        this.module = module;
    }
    
    /**
     * The module to which this importer belongs.
     */
    public int getModule() {
        return module;
    }
    
    /**
     * Opens the importer dialog.
     * @see FileImportDialog
     */
    public void showUI() {
        FileImportDialog dlg = new FileImportDialog(this);
        dlg.setVisible(true);
    }
    
    /**
     * Parses a file and extracts its information.
     * @param filename The file to check.
     * @param directoryUsage A free interpretation of the directory usage. 
     * Depends on a specific implementation.
     * @throws ParseException
     */
    public abstract DcObject parse(String filename, int directoryUsage);
    
    public abstract String[] getDefaultSupportedFileTypes();

    public String[] getSupportedFileTypes() {
        return DcModules.get(module).getSettings().getStringArray(DcRepository.ModuleSettings.stFileImportFileTypes);
    }
    
    /**
     * Indicates if a directory can be used instead of a file.
     * @return false
     */
    public boolean allowDirectoryRegistration() {
        return false;
    }
    
    /**
     * Indicates if files can be parsed again. This useful when you know that the information
     * of the file can be changed (such as the ID tag content of MP3 files).
     * @return false
     */
    public boolean allowReparsing() {
        return false;
    }
    
    /**
     * Indicates if local art can be used.
     * @return false
     */
    public boolean canImportArt() {
        return false;
    }
    
    /**
     * To be executed before a file is parsed.
     */
    public void beforeParse() {}
    
    /**
     * Starts the parsing task.
     * @param sources The files to check.
     * @throws Exception
     */
    public void parse(final Collection<String> sources) throws Exception {
        
        beforeParse();
        
        if (sources == null || sources.size() == 0)
            throw new Exception(DcResources.getText("msgSelectFiles"));
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getClient().addMessage(DcResources.getText("msgImportStarts"));
                getClient().addMessage(DcResources.getText("msgParsingXFiles", String.valueOf(sources.size())));
                
                getClient().initProgressBar(sources.size());
                
                try {
                    int counter = 1;
                    for (String filename : sources) {
                        if  (getClient().cancelled()) break;
                        
                        try {
                            parse(filename, counter == sources.size());
                        } catch (Throwable e) {
                            getClient().addError(e);
                            logger.error("An unhandled error occured during the import of " + filename, e);
                        }
                        getClient().updateProgressBar(counter++);
                    }
                    getClient().addMessage(DcResources.getText("msgImportStops"));
                } finally {
                    afterImport();
                    getClient().finish();
                }
            }
        });
        
        thread.start();
    }
    
    /**
     * Parses a single file. The process skips already imported files.
     * @param listener
     * @param filename
     */
    protected void parse(String filename, boolean last) {
        int module = getClient().getModule().getIndex();
        
        module = module == DcModules._MUSICALBUM ? DcModules._MUSICTRACK : module;
        
        DataFilter df = new DataFilter(getClient().getModule().getIndex());
        df.addEntry(new DataFilterEntry(module, DcObject._SYS_FILENAME, Operator.EQUAL_TO, filename));
        List<String> items = DataManager.getKeyList(df);
        
        if (items.size() > 0) {
            getClient().addMessage(DcResources.getText("msgSkippingAlreadyImportedFile", 
                                new String[] {filename, items.get(0).toString()}));
            
        } else {
            getClient().addMessage(DcResources.getText("msgProcessingFileX", filename));
            DcObject dco = parse(filename, getClient().getDirectoryUsage());
                
            if (getClient().getStorageMedium() != null) { 
                for (DcField  field : dco.getFields()) {
                    if (field.getSourceModuleIdx() == DcModules._STORAGEMEDIA)
                        DataManager.createReference(dco, field.getIndex(), getClient().getStorageMedium());
                }
            }
    
            if (getContainer() != null && dco.getModule().isContainerManaged())
                DataManager.createReference(dco, DcObject._SYS_CONTAINER, getClient().getDcContainer());
            
            dco.setLastInLine(last);
            dco.applyTemplate();
            dco.setIDs();
            afterParse(dco);
        }
    }
    
    /**
     * Called after finishing the whole parsing process.
     */
    protected void afterImport() {}
    
    /**
     * Called after parsing a single file.
     * @param listener
     * @param dco
     */
    protected void afterParse(DcObject dco) {
        if (getClient().useOnlineServices()) {
            try {
                getClient().addMessage(DcResources.getText("msgSearchingOnlineFor", StringUtils.normalize(dco.toString())));
                
                String originalTitle = (String) dco.getValue(DcMediaObject._A_TITLE);
                dco.setValue(DcMediaObject._A_TITLE, StringUtils.normalize(originalTitle));
                
                getClient().getModule().getSynchronizer().onlineUpdate(this, dco);
                
                dco.setValue(DcMediaObject._A_TITLE, originalTitle);
            } catch (Exception e) {
                logger.error(e, e);
            }
        }

        dco.setIDs();
        
        try {
            dco.setUpdateGUI(false);
            dco.saveNew(false);
        } catch (Exception e) {
            getClient().addError(e);
        }
    }

    /**
     * Tries to create a name from the specified file.
     * @param file
     * @param directoryUsage Either 1 (to use directory information) or 0.
     * @return The name.
     */
    protected String getName(String file, int directoryUsage) {
        String name = "";
        if (directoryUsage == 1) {
            File f = new File(file);
            String path = f.isDirectory() ? f.toString() : f.getParent();
            name = path.substring(path.lastIndexOf(File.separator) + 1);
        } else {
            name = new File(file).getName();
            int index = name.lastIndexOf(".");
            if (index > 0 && index > name.length() - 5)
                name = name.substring(0, index);
            
            String regex = DcModules.get(getModule()).getSettings().getString(DcRepository.ModuleSettings.stTitleCleanupRegex);
            if (!Utilities.isEmpty(regex)) {
                Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(name);
                while (matcher.find())
                    name = matcher.replaceAll("");
            }
            
            name = name.replaceAll("\\.", " ");
            
            String remove = 
                DcModules.get(getModule()).getSettings().getString(DcRepository.ModuleSettings.stTitleCleanup);
            
            if (!Utilities.isEmpty(remove)) {
                StringTokenizer st = new StringTokenizer(remove, ",");
                while (st.hasMoreElements()) {
                    String s = (String) st.nextElement();
                    int idx = name.toLowerCase().indexOf(s.toLowerCase());
                    if (idx > -1)
                        name = name.substring(0, idx) + name.substring(idx + s.length());
                }
            }
        }
        
        return StringUtils.capitalize(name.trim());
    }    
    
    private boolean match(String settingKey, String filename) {
        Settings settings = DcModules.get(module).getSettings();
        String keywords = settings.getString(settingKey);
        StringTokenizer st = new StringTokenizer(keywords, ",");
        while (st.hasMoreElements()) {
            if (filename.toLowerCase().indexOf((String) st.nextElement()) != -1)
                return true;
        }
        return false;
    }
    
    /**
     * Retrieve and use local art.
     * 
     * @see DcRepository.ModuleSettings#stImportLocalArt
     * @see DcRepository.ModuleSettings#stImportLocalArtRecurse
     * @see DcRepository.ModuleSettings#stImportLocalArtFrontKeywords
     * @see DcRepository.ModuleSettings#stImportLocalArtBackKeywords
     * @see DcRepository.ModuleSettings#stImportLocalArtMediaKeywords
     * 
     * @param filename The file location for which art will be retrieved.
     * @param dco
     * @param front The front image field index.
     * @param back The back image field index.
     * @param cd The media image field index.
     */
    protected void setImages(String filename, DcObject dco, int front, int back, int cd) {
        Settings settings = DcModules.get(module).getSettings();
        if (!settings.getBoolean(DcRepository.ModuleSettings.stImportLocalArt)) 
            return;
        
        String directory = new File(filename).getParent();
        boolean recurse = settings.getBoolean(DcRepository.ModuleSettings.stImportLocalArtRecurse);
        
        Directory dir = new Directory(directory, recurse, new String[] {"jpg", "jpeg", "png", "gif"});
        Collection<String> files = dir.read();
        boolean frontSet = false;
        boolean backSet = false;
        boolean cdSet = false;
        
        for (String file : files) {
            try {
                
                if (!frontSet) {
                    String name1 = new File(filename).getName();
                    String name2 = new File(file).getName();
                    name1 = name1.substring(0, name1.lastIndexOf(".") > 0 ? name1.lastIndexOf(".") : name1.length());
                    name2 = name2.substring(0, name2.lastIndexOf(".") > 0 ? name2.lastIndexOf(".") : name2.length());
                    
                    if (StringUtils.equals(name1, name2)) {
                        dco.setValue(front, new DcImageIcon(file));
                        frontSet = true;
                    }
                }
                
                if (!frontSet && (files.size() == 1 || 
                    match(DcRepository.ModuleSettings.stImportLocalArtFrontKeywords, file))) {
                    dco.setValue(front, new DcImageIcon(file));
                    frontSet = true;
                } else if (!backSet && match(DcRepository.ModuleSettings.stImportLocalArtBackKeywords, file)) {
                    dco.setValue(back, new DcImageIcon(file));
                    backSet = true;
                } else if (!cdSet && match(DcRepository.ModuleSettings.stImportLocalArtMediaKeywords, file)) {
                    dco.setValue(cd, new DcImageIcon(file));
                    cdSet = true;
                }
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }

    @Override
    public void addMessage(String message) {
        logger.info(message);
    }

    @Override
    public void enableActions(boolean b) {}

    @Override
    public int getItemPickMode() {
        return Synchronizer._ALL;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public SearchMode getSearchMode() {
        return sm;
    }
    
    public DcObject getContainer() {
        return container;
    }

    @Override
    public IServer getServer() {
        return server;
    }

    @Override
    public void initialize() {}

    @Override
    public void initProgressBar(int max) {}

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isReparseFiles() {
        return false;
    }

    @Override
    public void updateProgressBar() {}

    @Override
    public boolean useOnlineService() {
        return client.useOnlineServices();
    }
}