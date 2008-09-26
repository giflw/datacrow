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

package net.datacrow.console.components.renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcObject;
import net.datacrow.settings.DcSettings;

public class DcTableCellRenderer extends DefaultTableCellRenderer {

    private static final DcTableCellRenderer instance = new DcTableCellRenderer();
    private boolean disabled = false;

    protected DcTableCellRenderer() {}
    
    public static DcTableCellRenderer getInstance() {
        return instance;
    }
    
    protected boolean allowTooltips() {
    	return true;
    }
    
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (DcSettings.getBoolean(DcRepository.Settings.stShowTableTooltip) && allowTooltips()) {
        	if (disabled) {
        		ToolTipManager.sharedInstance().registerComponent(table);
                ToolTipManager.sharedInstance().registerComponent(table.getTableHeader());
                disabled = false;
        	} 
            
            ToolTipManager.sharedInstance().setDismissDelay(2000);
            setToolTipText(getTipText());

        } else {
            if (!disabled) { 
                disabled = true;
            	ToolTipManager.sharedInstance().unregisterComponent(table);
            	ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());
            }
        }
        
        Color colorOddRow = DcSettings.getColor(DcRepository.Settings.stOddRowColor);
        Color colorEvenRow = DcSettings.getColor(DcRepository.Settings.stEvenRowColor);
        Color colorRowSelection = DcSettings.getColor(DcRepository.Settings.stSelectionColor);
        Font font = DcSettings.getFont(DcRepository.Settings.stSystemFontNormal);
        setFont(font);
        
        setForeground(ComponentFactory.getCurrentForegroundColor());

        boolean overdue = false;
        
    	int idx = ((DcTable) table).getColumnIndexForField(DcObject._SYS_LOANDAYSTILLOVERDUE);
    	if (idx != -1) {
    		Long days = (Long) ((DcTable) table).getValueAt(row, idx, true);
    		if (days != null && days.longValue() < 0)
    			overdue = true;
    	}
        
    	if (overdue)
    		setForeground(Color.RED);
    	
    	if (!isSelected) {
            if ((row % 2) == 0) {
        		setBackground(colorEvenRow);
            } else {
                setBackground(colorOddRow);
            }
        } else {
        	if ((row % 2) == 0) {
        		if (hasFocus) {
        			setBackground(colorEvenRow);
        		} else {
        			setBackground(colorRowSelection);
        		}
        	} else {
        		if (hasFocus) {
        			setBackground(colorOddRow);
        		} else {
        			setBackground(colorRowSelection);
        		}
        	}
        }
        
    	if (hasFocus) {
    		setBorder(getBorder());
    	} else {
    		setBorder(noFocusBorder);
    	}
    
    	return c;

    }
    
    protected String getTipText() {
        return super.getText();
    }
}