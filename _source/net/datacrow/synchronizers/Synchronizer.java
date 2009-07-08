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

package net.datacrow.synchronizers;

import java.util.Collection;
import java.util.Iterator;

import net.datacrow.console.windows.ItemSynchronizerDialog;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.services.OnlineSearchHelper;
import net.datacrow.core.services.Region;
import net.datacrow.core.services.SearchMode;
import net.datacrow.core.services.plugin.IServer;
import net.datacrow.util.Utilities;

public abstract class Synchronizer {
    
    protected ItemSynchronizerDialog dlg;

    public static final int _ALL = 0;
    public static final int _SELECTED = 1;

    private String title;
    protected final int module;
    
    public Synchronizer(String title, int module) {
        this.title = title;
        this.module = module;
    }
    
    public abstract Thread getTask();
    public abstract String getHelpText();
    
    public abstract boolean canParseFiles();
    public abstract boolean canUseOnlineServices();
    
    public  String getTitle() {
        return title;
    }

    public String getHelpIndex() {
        return "dc.tools.massupdate";
    }
    
    public abstract boolean onlineUpdate(DcObject dco, IServer server, Region region, SearchMode mode);
    
    /**
     * Executed before the online update.
     * @param dco
     */
    protected boolean parseFiles(DcObject dco) {
        return false;
    }

    /**
     * Merges the data of the source and the target with regard of the settings.
     */
    protected void merge(DcObject target, DcObject source) {
        merge(target, source, null);
    }

    /**
     * Merges the data of the source and the target with regard of the settings.
     * The online search helper is used to query additional data when needed.
     */
    protected void merge(DcObject target, DcObject source, OnlineSearchHelper osh) {
        if (source == null) return;
        
        DcObject queried = osh != null ? osh.query(source) : source;
        for (int field : queried.getFieldIndices())
            setValue(target, field, queried.getValue(field));
    }
    
    public void synchronize(ItemSynchronizerDialog dlg) {
        this.dlg = dlg;
        
        Thread thread = getTask();
        thread.start();
    }
    
    public void setListener(ItemSynchronizerDialog dlg) {
        this.dlg = dlg;
    }
    
    public void addMessage(String message) {
        if (dlg != null)
            dlg.addMessage(message);
    }
    
    protected boolean isReparseFiles() {
        return dlg.isReparseFiles();
    }

    protected boolean useOnlineService() {
        return dlg.useOnlineService();
    }
    
    
    protected boolean isCancelled() {
        return dlg.isCancelled();
    }
    
    protected void initProgressBar(int max) {
        dlg.initProgressBar(max);
    }

    protected void updateProgressBar() {
        dlg.updateProgressBar();
    }
    
    protected void enableAction(boolean b) {
        dlg.enableActions(b);
    }

    protected void initialize() {
        dlg.initialize();
    }
    
    protected IServer getServer() {
        return dlg.getServer();
    }

    protected Region getRegion() {
        return dlg.getRegion();
    }

    protected SearchMode getSearchMode() {
        return dlg.getSearchMode();
    }
    
    @SuppressWarnings("unchecked")
    protected void setValue(DcObject dco, int field, Object value) {
        
        // empty value, no need to update
        if (Utilities.isEmpty(value))
            return;
        
        boolean overwrite = dco.getModule().getSettings().getBoolean(DcRepository.ModuleSettings.stOnlineSearchOverwrite);
        int[] fields = overwrite ?
                       dco.getModule().getSettings().getIntArray(DcRepository.ModuleSettings.stOnlineSearchFieldOverwriteSettings) :
                       dco.getModule().getFieldIndices();
            
       // if all fails, just update all..
       if (fields == null || fields.length == 0)
           fields = dco.getModule().getFieldIndices();
                       
        boolean allowed = false;
        for (int i = 0;i < fields.length; i++)
            allowed |= fields[i] == field;
        
        if (allowed) {
            if ((dco.isFilled(field) && overwrite) || !dco.isFilled(field)) {
                if (value instanceof Collection) {
                    for (Iterator iter = ((Collection) value).iterator(); iter.hasNext(); ) {
                        DcObject o = (DcObject) iter.next();
                        if (o instanceof DcMapping)
                            o.setValue(DcMapping._A_PARENT_ID, dco.getID());
                        else
                            DataManager.createReference(dco, field, o);
                    }
                } else if (value instanceof Picture) {
                    dco.setValue(field, ((Picture) value).getBytes());
                } else {
                    dco.setValue(field, value);    
                }                
            }
        }
    }
}