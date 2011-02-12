package net.datacrow.core.wf.requests;

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
            if (dco.isLastInLine() && dco.getModule().getSearchView().getGroupingPane() != null)
                dco.getModule().getSearchView().getGroupingPane().getCurrent().setSelected(dco);
        }
        
        if (updateRelatedModules && dco.isLastInLine()) {
            for (DcModule module : DcModules.getReferencingModules(dco.getModule().getIndex())) {
                if (module.isSearchViewInitialized() && module.getSearchView().isLoaded()) {
                    module.getSearchView().refreshQuickView();
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
