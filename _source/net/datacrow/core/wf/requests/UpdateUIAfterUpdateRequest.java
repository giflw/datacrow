package net.datacrow.core.wf.requests;

import java.util.Collection;

import net.datacrow.core.DataCrow;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

public class UpdateUIAfterUpdateRequest implements IUpdateUIRequest {

    private boolean updateRelatedModules = false;
    private DcObject dco;
    
    public UpdateUIAfterUpdateRequest(DcObject dco, boolean updateRelatedModules) {
        this.updateRelatedModules = updateRelatedModules;
        this.dco = dco;
    }
    
    @Override
    public boolean getExecuteOnFail() {
        return false;
    }

    @Override
    public void setExecuteOnFail(boolean b) {}

    @Override
    public void end() {
        dco = null;
    }
    
    @Override
    public void execute() {
        
        if (!DataCrow.isInitialized()) return;
        
        if (dco.getModule().hasSearchView()) {
            dco.getModule().getSearchView().update(dco);
            if (dco.isLastInLine() && dco.getModule().getSearchView().getGroupingPane() != null) {
                dco.getModule().getSearchView().getGroupingPane().getCurrent().setSelected(dco);
            }
        }
        
        if (updateRelatedModules && dco.isLastInLine()) {
            Collection<DcModule> modules = DcModules.getReferencingModules(dco.getModule().getIndex());
            for (DcModule module : modules) {
                if (module.isSearchViewInitialized() && module.getSearchView().isLoaded()) {
                    module.getSearchView().refreshQuickView();
                    
                    // update the tree of this module to reflect name changes, etc.
                    if (module.getSearchView().getGroupingPane() != null) {
                        module.getSearchView().getGroupingPane().updateTreeNodes(dco);
                    }
                }
            }
        }

        if (updateRelatedModules) {
            for (DcModule module : DcModules.getAbstractModules(dco.getModule())) {
                if (module.isSearchViewInitialized() && module.getSearchView().isLoaded()) {
                    
                    module.getSearchView().update(dco);
                    
                    if (module.getSearchView().getGroupingPane() != null && 
                        module.getSearchView().getGroupingPane().isEnabled())
                        module.getSearchView().getGroupingPane().getCurrent().setSelected(dco);
                }
            }
        }
        
        DataManager.updateIcon(dco.getID());
        
        dco = null;
    }
}
