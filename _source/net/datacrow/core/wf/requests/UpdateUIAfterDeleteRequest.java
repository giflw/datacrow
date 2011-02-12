package net.datacrow.core.wf.requests;

import net.datacrow.core.DataCrow;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

public class UpdateUIAfterDeleteRequest implements IUpdateUIRequest {

    private boolean updateRelatedModules = false;
    private DcObject dco;
    
    public UpdateUIAfterDeleteRequest(DcObject dco, boolean updateRelatedModules) {
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
        
        String ID = dco.getID();
        
        if (dco.getModule().hasSearchView())
            dco.getModule().getSearchView().remove(ID);
        
        if (updateRelatedModules) {
            for (DcModule module : DcModules.getReferencingModules(dco.getModule().getIndex())) {
                if (module.hasSearchView() && dco.isLastInLine()) {
                    module.getSearchView().refreshQuickView();
                }
            }

            if (dco.getModule().isChildModule()) {
                if (dco.getModule().getParent().hasSearchView())
                    dco.getModule().getParent().getSearchView().refreshQuickView();
            }
            
            for (DcModule module : DcModules.getAbstractModules(dco.getModule())) {
                if (    module.hasSearchView() && 
                        module.isSearchViewInitialized() && 
                        module.getSearchView().isLoaded())
                    
                    module.getSearchView().remove(ID);
            }
        }  

        DataManager.removeIcon(ID);
        dco = null;
    }
}
