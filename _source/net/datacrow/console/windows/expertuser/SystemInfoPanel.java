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

package net.datacrow.console.windows.expertuser;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.renderers.NumberTableCellRenderer;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class SystemInfoPanel extends JPanel {
    
    public SystemInfoPanel() {
        build();
    }
    
    private void build() {
        setLayout(Layout.getGBL());

        JLabel databaseNameLabel = ComponentFactory.getLabel(DcResources.getText("lblDatabase"));
        JLabel databaseNameTextLabel = ComponentFactory.getLabel(DcSettings.getString(DcRepository.Settings.stConnectionString));
        JLabel databaseDriverLabel = ComponentFactory.getLabel(DcResources.getText("lblDatabaseDriver"));
        JLabel databaseDriverTextLabel = ComponentFactory.getLabel(DcSettings.getString(DcRepository.Settings.stDatabaseDriver));
        
        DcTable table = ComponentFactory.getDCTable(true, false);
        table.setEnabled(false);
        table.setColumnCount(2);
        TableColumn columnName = table.getColumnModel().getColumn(0);
        TableColumn columnCount = table.getColumnModel().getColumn(1);
        columnName.setHeaderValue(DcResources.getText("lblName"));
        columnCount.setHeaderValue(DcResources.getText("lblItems"));
        table.applyHeaders();
        
        columnCount.setCellRenderer(NumberTableCellRenderer.getInstance());
        
        int total = 0;
        for (DcModule module : DcModules.getModules()) {
            if (!module.isAbstract() && module.isTopModule()) {
                int count = getRecordCount(module);
                total += count;
                table.addRow(new Object[] {module.getLabel(), count});
            }
        }

        table.addRow(new Object[] {DcResources.getText("lblTotal"), total});

        JScrollPane sp = new JScrollPane(table);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(databaseNameLabel,       Layout.getGBC( 0, 0, 1, 1, 1.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 5, 0, 0), 0, 0));
        add(databaseNameTextLabel,   Layout.getGBC( 1, 0, 1, 1, 1.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 5, 0, 0), 0, 0));
        add(databaseDriverLabel,     Layout.getGBC( 0, 1, 1, 1, 1.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 5, 0, 0), 0, 0));
        add(databaseDriverTextLabel, Layout.getGBC( 1, 1, 1, 1, 1.0, 0.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 5, 0, 0), 0, 0));        
        add(sp,                      Layout.getGBC( 0, 2, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 10, 5, 5, 5), 0, 0));
    }
    
    private int getRecordCount(DcModule module) {
    	return DataManager.getCount(module.getIndex(), -1, null);
    }    
}
