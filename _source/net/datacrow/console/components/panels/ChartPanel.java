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

package net.datacrow.console.components.panels;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcPanel;
import net.datacrow.core.DcRepository;
import net.datacrow.core.DcThread;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataFilter;
import net.datacrow.core.data.DataFilterEntry;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.data.Operator;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DcSwingUtilities;

import org.apache.log4j.Logger;

import com.approximatrix.charting.CoordSystem;
import com.approximatrix.charting.charting.model.ObjectChartDataModel;
import com.approximatrix.charting.charting.render.BarChartRenderer;
import com.approximatrix.charting.charting.render.PieChartRenderer;


public class ChartPanel extends DcPanel implements ActionListener {
    
    private static Logger logger = Logger.getLogger(ChartPanel.class.getName());
    
    private JComboBox comboFields;
    private JComboBox comboTypes;
    private JButton button = ComponentFactory.getIconButton(IconLibrary._icoAccept);
    
    private ThreadGroup tg = new ThreadGroup("chart-builders");
    private com.approximatrix.charting.charting.swing.ChartPanel chart;
    
    private final int module;
    
    public ChartPanel(int module) {
        super(DcResources.getText("lblCharts"), IconLibrary._icoChart);
        setHelpIndex("dc.charts");
        this.module = module;
        build();
    }
    
    @Override
    public void clear() {
        super.clear();
        comboFields = null;
        comboTypes = null;
        chart = null;
        button = null;
        tg = null;
    }
    
    @Override
    public void setEnabled(boolean b) {
        comboFields.setEnabled(b);
        comboTypes.setEnabled(b);
        button.setEnabled(b);
    }
    
    private void buildChart() {
        DcField field = (DcField) comboFields.getSelectedItem();
        
        if (field == null) return;
        
        if (comboTypes.getSelectedIndex() == 1)
            buildBar(field);
        else
            buildPie(field);
    }
    
    private void deinstall() {
        if (chart != null) {
            chart.removeAll();
            remove(chart);
            chart = null;
            repaint();
        }
    }
    
    private void install() {
        chart.setBorder(ComponentFactory.getTitleBorder(""));
        add(chart, Layout.getGBC( 0, 1, 2, 1, 40.0, 40.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets(5, 5, 5, 5), 0, 0));
     
        setEnabled(true);
        revalidate();
    }
    
    private String[] getSortedLabels(Map<String, Integer> dataMap) {
        List<String> c = new ArrayList<String>();
        c.addAll(dataMap.keySet());
        Collections.sort(c, new Comparator<String>() {
            public int compare(String s1, String s2) {
                return (s1.compareTo(s2));
            }
        });
        
        return c.toArray(new String[0]);        
    }
    
    private void buildBar(DcField field) {
        deinstall();
        setEnabled(false);

        new BarChartBuilder(field.getIndex()).start();
    }
    
    private void buildPie(DcField field) {
        deinstall();
        setEnabled(false);

        new PieChartBuilder(field.getIndex()).start();
    }
    
    private Collection<String> getUniqueValues(int module, int field) {
        List<DcObject> objects = DataManager.get(module, null);
        
        Collection<String> values = new ArrayList<String>();
        String s;
        for (DcObject dco : objects) {
            s = dco.getDisplayString(field);    
            if (!values.contains(s) && s.length() > 0)
                values.add(s);
        }
        return values;
    }   
    
    public static int getCount(int module, DcField field, Object value) {
        List<DcObject> objects = DataManager.get(module, null);
        int count = 0;
        String s1;
        String s2;
        for (DcObject dco : objects) {
            s1 = dco.getDisplayString(field.getIndex()); 
            s2 = (String) value;
            
            if ((field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION && (s1.equals(s2) || s1.indexOf(s2) >= 0)) ||
                (s1.equals(s2)))
                count++;
        }
        return count;
    }    
    
    private Map<String, Integer> getDataMap(DcField field) {
        Collection<String> categories = new ArrayList<String>();
        int module = field.getModule();
        
        List<DcObject> o;
        if (module != field.getReferenceIdx()) {
            o = DataManager.get(field.getReferenceIdx(), null);
            for (DcObject dco : o) 
                categories.add(dco.toString());
        } else {
            categories = getUniqueValues(module, field.getIndex());
        }
        
        if (categories.size() == 0) {
            DcSwingUtilities.displayMessage("msgCouldNotCreateChart");
            return null;
        }
        
        // check for empty values and include these
        DataFilter df = new DataFilter(module);
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, module, field.getIndex(), Operator.IS_EMPTY, null));
        
        int empty = DataManager.get(field.getModule(), df).size();
        if (empty > 0) categories.add(DcResources.getText("lblEmpty"));
        
        // create the data map. exclude zero counts
        Map<String, Integer> dataMap = new HashMap<String, Integer>();
        int count;
        for (String key : categories) {
            
            try {
                Thread.sleep(5);
            } catch (Exception e) {
                logger.error(e, e);
            }
            
            count = getCount(module, field, key);
            if (key.equals(DcResources.getText("lblEmpty"))) {
                dataMap.put(key + " (" + empty + ")", empty);
            } else if (count > 0) { 
                dataMap.put(key + " (" + count + ")", count);
            }
        }
        
        return dataMap.size() > 0 ? dataMap : null;
    }    
    
    public boolean isSupported() {
        return getFields().size() > 0;
    }
    
    private Collection<DcField> getFields() {
        Collection<DcField> fields = new ArrayList<DcField>();
        for (DcField field : DcModules.get(module).getFields()) {
            if (((((field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE ||
                   (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) &&
                   DcModules.get(field.getReferenceIdx()).getType() == DcModule._TYPE_PROPERTY_MODULE) ||
                   field.getValueType() == DcRepository.ValueTypes._LONG ||
                   field.getValueType() == DcRepository.ValueTypes._BOOLEAN))) && 
                   field.isEnabled() && !field.isTechnicalInfo()) {
                
                fields.add(field);
            }
        }
        return fields;
    }

    private void build() {
        setLayout(Layout.getGBL());
        
        JPanel panel = new JPanel();
        
        comboFields = ComponentFactory.getComboBox();
        comboTypes = ComponentFactory.getComboBox();
        
        panel.add(comboFields);
        panel.add(comboTypes);
        panel.add(button);
        
        for (DcField field : getFields()) 
            comboFields.addItem(field);
        
        comboTypes.addItem(DcResources.getText("lblPie"));
        comboTypes.addItem(DcResources.getText("lblBar"));
        
        button.addActionListener(this);
        button.setActionCommand("buildChart");
        add(   panel, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
              ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
               new Insets(5, 5, 5, 5), 0, 0));
    }   
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (comboFields != null) {
            comboFields.setFont(font);
            comboTypes.setFont(font);
            
            if (chart != null)
                chart.setFont(font);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("buildChart"))
            buildChart();
    }
    
    
    private class PieChartBuilder extends DcThread {
        
        private final int fieldIdx;
        
        public PieChartBuilder(int field) {
            super(tg, "");
            this.fieldIdx = field;
        }
        
        @Override
        public void run() {
            
            DcField field = DcModules.get(module).getField(fieldIdx);
            
            cancelOthers();
            
            Map<String, Integer> dataMap = getDataMap(field);
            
            if (dataMap == null) {
            	setEnabled(true);
            	return;
            }
            
            double[][] data = new double[dataMap.keySet().size()][1];
            String[] labels = getSortedLabels(dataMap);
            String key;
            for (int i = 0; i < labels.length; i++) {
                
                if (isCanceled()) break;
                
                key = labels[i];
                data[i][0] = dataMap.get(key).intValue();
            }
            
            
            if (!isCanceled()) {
                // create the model
                ObjectChartDataModel model = new ObjectChartDataModel(
                        data, new String[] {field.getLabel()}, labels, 
                        ComponentFactory.getSystemFont(), true);
                
                // create the chart
                CoordSystem coord = new CoordSystem(model);
                coord.setPaintAxes(false);
                
                chart = new com.approximatrix.charting.charting.swing.ChartPanel(model, field.getLabel());
                chart.setCoordSystem(coord);
                chart.addChartRenderer(new PieChartRenderer(model), 0);
                
                dataMap.clear();
                
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            install();
                        };
                    });
                } catch (Exception e) {
                    logger.error(e, e);
                }                    
            }
        }
    }
    
    private class BarChartBuilder extends DcThread {
        
        private final int fieldIdx;
        
        public BarChartBuilder(int field) {
            super(tg, "");
            this.fieldIdx = field;
        }
        
        @Override
        public void run() {
            
            cancelOthers();
            
            Map<String, Integer> dataMap = getDataMap(DcModules.get(module).getField(fieldIdx));
            
            if (dataMap == null) return;
            
            double[][] data = new double[1][dataMap.keySet().size()];
            int maximum = 0;
            String[] labels = getSortedLabels(dataMap);
            String key;
            int value;
            for (int i = 0; i < labels.length; i++) {
                
                if (isCanceled()) break;
                
                key = labels[i];
                value = dataMap.get(key).intValue();
                data[0][i] = value;
                maximum = maximum < value ? value : maximum;
            }
            
            if (!isCanceled()) {
                // create the model
                ObjectChartDataModel model = 
                    new ObjectChartDataModel(data, labels, 
                            new String[] {DcModules.get(module).getField(fieldIdx).getLabel()}, 
                            ComponentFactory.getSystemFont(), true);  
                
                // create the chart
                CoordSystem coord = new CoordSystem(model);
        
                model.setManualScale(true);
                model.setMinimumValue(new Double(0.0));
                model.setMaximumValue(new Double(maximum * 1.5));
                
                chart = new com.approximatrix.charting.charting.swing.ChartPanel(model, " ");
                chart.setCoordSystem(coord);
                chart.addChartRenderer(new BarChartRenderer(coord, model), 0);
                
                dataMap.clear();
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        install();
                    };
                });
            }
        }
    }
}
