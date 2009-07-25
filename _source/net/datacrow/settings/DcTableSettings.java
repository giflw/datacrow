package net.datacrow.settings;

import java.util.HashMap;
import java.util.Map;

import net.datacrow.util.StringUtils;

public class DcTableSettings {
    
    private int module; 
    private Map<Integer, Integer> columnWidths = new HashMap<Integer, Integer>();

    /**
     * Creates a new, empty, definition
     * @param module
     */
    public DcTableSettings(int module) {
        this.module = module;
    }
    
    public DcTableSettings(String value) {
        setValue(value);
    }
    
    public int getModuleIdx() {
        return module;
    }

    public void setColumnWidth(int fieldIdx, int width) {
        columnWidths.put(Integer.valueOf(fieldIdx), Integer.valueOf(width));
    }
    
    public int getWidth(int fieldIdx) {
        // 75 is defined as the default width by Swing
        return columnWidths.containsKey(fieldIdx) ? columnWidths.get(Integer.valueOf(fieldIdx)) : 75;
    }
    
    public String getValue() {
        return toString();
    }

    private void setValue(String definition) {
        String s = StringUtils.getValueBetween("[", "]", definition);
        module = Integer.parseInt(s);
        
        for (String value : StringUtils.getValuesBetween("{", "}", definition)) {
            int field = Integer.parseInt(value.substring(0, value.indexOf(",")));
            int width = Integer.parseInt(value.substring(value.indexOf(",") + 1));
            columnWidths.put(Integer.valueOf(field), Integer.valueOf(width));
        }
    }

    @Override
    public String toString() {
        String s = "[" + module + "]";
        for (Integer fieldIdx : columnWidths.keySet()) {
            s += "{" + String.valueOf(fieldIdx.intValue()) + "," + 
                 String.valueOf(columnWidths.get(fieldIdx).intValue()) + "}";
        }
        return s;
    } 
}