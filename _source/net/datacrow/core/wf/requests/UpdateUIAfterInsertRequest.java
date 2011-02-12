package net.datacrow.core.wf.requests;

import net.datacrow.console.views.MasterView;
import net.datacrow.core.DataCrow;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;

public class UpdateUIAfterInsertRequest implements IUpdateUIRequest {

    private boolean updateRelatedModules = false;
    private DcObject dco;
    
    public UpdateUIAfterInsertRequest(DcObject dco, boolean updateRelatedModules) {
        this.updateRelatedModules = updateRelatedModules;
        this.dco = dco;
    }
    
    @Override
    public boolean getExecuteOnFail() {
        return false;
    }

    @Override
    public void setExecuteOnFail(boolean b) {
    }

    @Override
    public void end() {
        dco = null;
    }
    
    @Override
    public void execute() {
        
        if (!DataCrow.isInitialized()) return;
        
        // Note that in the item form (close(boolean b)) the potential parent module's
        // quick view is already updated. No need to do that here.
        String ID = dco.getID();

        if (dco.getModule().isTopModule()) {
            if (dco.getModule().hasSearchView()) {
                dco.getModule().getSearchView().add(dco, dco.isLastInLine());
                if (dco.getModule().getInsertView() != null)
                    dco.getModule().getInsertView().remove(ID);
            }
        
            for (DcModule module : DcModules.getAbstractModules(dco.getModule()))
                if (module.isSearchViewInitialized() && module.getSearchView().isLoaded() && !module.isChildModule()) {
                    module.getSearchView().add(dco);
                    
                    if (updateRelatedModules && module.getSearchView().getGroupingPane() != null && module.getSearchView().getGroupingPane().isEnabled())
                        module.getSearchView().getGroupingPane().getCurrent().setSelected(dco);
            }
        }
        
        if (dco.getModule().isChildModule() && dco.isLastInLine()) {
            String parentID = dco.getParentID();
            MasterView parentVw = dco.getModule().getParent().getSearchView();
            if (parentVw.getCurrent().getSelectedItemKeys().contains(parentID))
                parentVw.getCurrent().refreshQuickView();
        }   
    }
}
