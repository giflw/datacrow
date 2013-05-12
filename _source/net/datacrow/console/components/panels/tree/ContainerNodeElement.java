package net.datacrow.console.components.panels.tree;

import java.util.List;
import java.util.Map;

import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.helpers.Item;
import net.datacrow.util.DcImageIcon;

public class ContainerNodeElement extends NodeElement {

	public ContainerNodeElement(String key, String displayValue, DcImageIcon icon) {
		super(key, displayValue, icon);
		addItem(key, DcModules._CONTAINER);
	}

	@Override
	public Map<String, Integer> getItems() {
		if (	DcModules.get(DcModules._CONTAINER).getSettings().getInt(
				DcRepository.ModuleSettings.stTreePanelShownItems) == DcModules._ITEM) {
			
			DataFilter df = new DataFilter(DcModules._ITEM);
			df.addEntry(new DataFilterEntry(DcModules._ITEM, Item._SYS_CONTAINER, Operator.EQUAL_TO, getKey()));
			return DataManager.getKeys(df);
		} else {
			return super.getItems();
		}
	}
	
	@Override
    public Map<String, Integer> getItemsSorted(List<String> allOrderedItems) {
    	return getItems();
    }
	
    @Override
    public String toString() {
        return getDisplayValue();
    }
}
