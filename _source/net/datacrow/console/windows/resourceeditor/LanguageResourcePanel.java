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

package net.datacrow.console.windows.resourceeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcList;
import net.datacrow.console.components.tables.DcTable;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcLanguageResource;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class LanguageResourcePanel extends JPanel implements ListSelectionListener {

    private int selected = 0;

    private DcList topicList = new DcList();

    private JPanel panelInput = new JPanel();

    private List<JScrollPane> scrollers = new ArrayList<JScrollPane>();
    private DcTable tableTips = ComponentFactory.getDCTable(false, false);
    private DcTable tableSystemLabels = ComponentFactory.getDCTable(false, false);
    private DcTable tableLabels = ComponentFactory.getDCTable(false, false);
    private DcTable tableMessages = ComponentFactory.getDCTable(false, false);
    private DcTable tableTooltips = ComponentFactory.getDCTable(false, false);

    private String language;
    
    public LanguageResourcePanel(String language) {
        this.language = language;
        
        build();
        load();
    }
    
    private void load() {
        DcLanguageResource resources = DcResources.getLanguageResource(language);
        
        Set<String> keys = resources.getResourcesMap().keySet();
        ArrayList<String> list = new ArrayList<String>(keys);
        Collections.sort(list);
        
        for (String key : list) {
            String value = resources.get(key);

            if (key.startsWith("lbl")) tableLabels.addRow(new Object[] {key, value});
            if (key.startsWith("msg")) tableMessages.addRow(new Object[] {key, value});
            if (key.startsWith("tp")) tableTooltips.addRow(new Object[] {key, value});
            if (key.startsWith("sys")) tableSystemLabels.addRow(new Object[] {key, value});
            if (key.startsWith("tip")) tableTips.addRow(new Object[] {key, value});            
        }
    }
    
    public void save() {
        DcLanguageResource resources = DcResources.getLanguageResource(language);
        
        for (int i = 0; i < tableLabels.getRowCount(); i++) {
            String key = (String) tableLabels.getValueAt(i, 0, true);
            String value = (String) tableLabels.getValueAt(i, 1, true);
            resources.put(key, value);
        }
        
        for (int i = 0; i < tableMessages.getRowCount(); i++) {
            String key = (String) tableMessages.getValueAt(i, 0, true);
            String value = (String) tableMessages.getValueAt(i, 1, true);
            resources.put(key, value);
        }
        
        for (int i = 0; i < tableTooltips.getRowCount(); i++) {
            String key = (String) tableTooltips.getValueAt(i, 0, true);
            String value = (String) tableTooltips.getValueAt(i, 1, true);
            resources.put(key, value);
        }  
        
        for (int i = 0; i < tableSystemLabels.getRowCount(); i++) {
            String key = (String) tableSystemLabels.getValueAt(i, 0, true);
            String value = (String) tableSystemLabels.getValueAt(i, 1, true);
            resources.put(key, value);
        }   

        for (int i = 0; i < tableTips.getRowCount(); i++) {
            String key = (String) tableTips.getValueAt(i, 0, true);
            String value = (String) tableTips.getValueAt(i, 1, true);
            resources.put(key, value);
        }        
        
        resources.save();
    }
    
    private void setActiveTopic() {
        Dimension size = panelInput.getSize();
        selected = topicList.getSelectedIndex();
        int counter = 0;
        for (JScrollPane scroller : scrollers) {
            scroller.setVisible(counter == selected);
            counter++;
        }
        
        if (size.height != 0 && size.width != 0)
            panelInput.setPreferredSize(size);
    }
    
    private void setTopics() {
        Vector<JPanel> vector = new Vector<JPanel>();

        JPanel panelLabels = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelLabels.add(ComponentFactory.getLabel(IconLibrary._icoLabels));
        panelLabels.add(ComponentFactory.getLabel(DcResources.getText("lblLabels")));

        JPanel panelSystemLabels = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSystemLabels.add(ComponentFactory.getLabel(IconLibrary._icoLabels));
        panelSystemLabels.add(ComponentFactory.getLabel(DcResources.getText("lblSystemLabels")));      
        
        JPanel panelTooltips = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelTooltips.add(ComponentFactory.getLabel(IconLibrary._icoTooltips));
        panelTooltips.add(ComponentFactory.getLabel(DcResources.getText("lblTooltips")));          
        
        JPanel panelMessages = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelMessages.add(ComponentFactory.getLabel(IconLibrary._icoMessages));
        panelMessages.add(ComponentFactory.getLabel(DcResources.getText("lblMessages")));        

        JPanel panelTips = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelTips.add(ComponentFactory.getLabel(IconLibrary._icoTips));
        panelTips.add(ComponentFactory.getLabel(DcResources.getText("lblTipsOfTheDay")));        
        
        vector.addElement(panelLabels);
        vector.addElement(panelSystemLabels);
        vector.addElement(panelTooltips);
        vector.addElement(panelMessages);
        vector.addElement(panelTips);
        
        // hack to make the module indices correspond to the model indices
        // (due to the not added Audio Track module)
        JLabel label = new JLabel();
        label.setMaximumSize(new Dimension(50,0));
        label.setPreferredSize(new Dimension(50,0));
        
        topicList.setListData(vector);
    }    
    
    public void clear() {
        topicList = null;
        panelInput = null;
        scrollers.clear();
        scrollers = null;
        
        tableTips = null;
        tableSystemLabels = null;
        tableLabels = null;
        tableMessages = null;
        tableTooltips = null;        
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        //**********************************************************
        //Labels
        //**********************************************************           
        JScrollPane scrollerLabels = new JScrollPane(tableLabels);
        tableLabels.setColumnCount(2);

        TableColumn columnKey = tableLabels.getColumnModel().getColumn(0);
        columnKey.setMaxWidth(300);
        columnKey.setPreferredWidth(150);
        columnKey.setCellEditor(new DefaultCellEditor(ComponentFactory.getTextFieldDisabled()));       
        columnKey.setHeaderValue(DcResources.getText("lblKey"));
        
        TableColumn columnValue = tableLabels.getColumnModel().getColumn(1);
        columnValue.setCellEditor(new DefaultCellEditor(ComponentFactory.getShortTextField(1000)));
        columnValue.setHeaderValue(DcResources.getText("lblValue"));
        
        scrollerLabels.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollerLabels.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);        
        
        tableLabels.applyHeaders();
        ComponentFactory.setBorder(scrollerLabels);
        
        //**********************************************************
        //Tips
        //**********************************************************           
        JScrollPane scrollerTips = new JScrollPane(tableTips);
        tableTips.setColumnCount(2);

        columnKey = tableTips.getColumnModel().getColumn(0);
        columnKey.setMaxWidth(300);
        columnKey.setPreferredWidth(150);
        columnKey.setCellEditor(new DefaultCellEditor(ComponentFactory.getTextFieldDisabled()));       
        columnKey.setHeaderValue(DcResources.getText("lblKey"));
        
        columnValue = tableTips.getColumnModel().getColumn(1);
        columnValue.setCellEditor(new DefaultCellEditor(ComponentFactory.getShortTextField(1000)));
        columnValue.setHeaderValue(DcResources.getText("lblValue"));
        
        scrollerTips.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollerTips.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);        
        
        tableTips.applyHeaders();       
        ComponentFactory.setBorder(scrollerTips);
        
        //**********************************************************
        //Messages
        //**********************************************************           
        JScrollPane scrollerMessages = new JScrollPane(tableMessages);
        tableMessages.setColumnCount(2);

        columnKey = tableMessages.getColumnModel().getColumn(0);
        columnKey.setMaxWidth(300);
        columnKey.setPreferredWidth(150);
        columnKey.setCellEditor(new DefaultCellEditor(ComponentFactory.getTextFieldDisabled()));       
        columnKey.setHeaderValue(DcResources.getText("lblKey"));
        
        columnValue = tableMessages.getColumnModel().getColumn(1);
        columnValue.setCellEditor(new DefaultCellEditor(ComponentFactory.getShortTextField(1000)));
        columnValue.setHeaderValue(DcResources.getText("lblValue"));
        
        scrollerMessages.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollerMessages.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);        
        
        tableMessages.applyHeaders();       
        ComponentFactory.setBorder(scrollerMessages);
                
        //**********************************************************
        //Tooltips
        //**********************************************************           
        JScrollPane scrollerTooltips = new JScrollPane(tableTooltips);
        tableTooltips.setColumnCount(2);

        columnKey = tableTooltips.getColumnModel().getColumn(0);
        columnKey.setMaxWidth(300);
        columnKey.setPreferredWidth(150);
        columnKey.setCellEditor(new DefaultCellEditor(ComponentFactory.getTextFieldDisabled()));       
        columnKey.setHeaderValue(DcResources.getText("lblKey"));
        
        columnValue = tableTooltips.getColumnModel().getColumn(1);
        columnValue.setCellEditor(new DefaultCellEditor(ComponentFactory.getShortTextField(1000)));
        columnValue.setHeaderValue(DcResources.getText("lblValue"));
        
        scrollerTooltips.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollerTooltips.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);        
        
        tableTooltips.applyHeaders();
        ComponentFactory.setBorder(scrollerTooltips);
        
        //**********************************************************
        //System Labels
        //**********************************************************           
        JScrollPane scrollerSystemLabels = new JScrollPane(tableSystemLabels);
        tableSystemLabels.setColumnCount(2);

        columnKey = tableSystemLabels.getColumnModel().getColumn(0);
        columnKey.setMaxWidth(300);
        columnKey.setPreferredWidth(150);
        columnKey.setCellEditor(new DefaultCellEditor(ComponentFactory.getTextFieldDisabled()));       
        columnKey.setHeaderValue(DcResources.getText("lblKey"));
        
        columnValue = tableSystemLabels.getColumnModel().getColumn(1);
        columnValue.setCellEditor(new DefaultCellEditor(ComponentFactory.getShortTextField(1000)));
        columnValue.setHeaderValue(DcResources.getText("lblValue"));
        
        scrollerSystemLabels.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollerSystemLabels.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);        
        
        tableSystemLabels.applyHeaders();
        ComponentFactory.setBorder(scrollerSystemLabels);
        
        //**********************************************************
        //Topic List Panel
        //**********************************************************
        scrollers.add(scrollerLabels);
        scrollers.add(scrollerSystemLabels);
        scrollers.add(scrollerTooltips);
        scrollers.add(scrollerMessages);
        scrollers.add(scrollerTips);
        
        JPanel panelTopics = new JPanel();
        
        topicList.addListSelectionListener(this);
        panelTopics.setLayout(new BorderLayout());

        JLabel label = new JLabel(DcResources.getText("lblTopics"));
        label.setFont(ComponentFactory.getSystemFont());
        label.setHorizontalAlignment(JLabel.CENTER);
        
        JPanel panelHeader = new JPanel(Layout.getGBL());
        panelHeader.setLayout(Layout.getGBL());
        panelHeader.setBorder(BorderFactory.createEtchedBorder());
        panelHeader.add(label, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        
        panelTopics.add(panelHeader, BorderLayout.NORTH);
        panelTopics.add(topicList, BorderLayout.CENTER);

        //**********************************************************
        //Panel Input
        //**********************************************************
        panelInput.setLayout(Layout.getGBL());
        
        panelInput.add(scrollerLabels,       Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        panelInput.add(scrollerSystemLabels, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));        
        panelInput.add(scrollerMessages,     Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        panelInput.add(scrollerTooltips,     Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        panelInput.add(scrollerTips,         Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        
        //**********************************************************
        //Main panel
        //**********************************************************
        add(panelTopics,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL,
                 new Insets( 5, 5, 5, 5), 0, 0));
        add(panelInput,   Layout.getGBC( 1, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));

        setTopics();
        
        panelInput.setPreferredSize(DcSettings.getDimension(DcRepository.Settings.stResourcesEditorViewSize));
        topicList.setSelectedIndex(0);        
    }
    
    public void valueChanged(ListSelectionEvent arg0) {
        setActiveTopic();
    }    
}
