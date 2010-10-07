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

package net.datacrow.console.windows.fileimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcLongTextField;
import net.datacrow.console.windows.DcDialog;
import net.datacrow.core.DcRepository;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.Settings;

public class SelectFileTypesDialog extends DcDialog implements ActionListener {
    
    protected JTextArea textFileTypes = ComponentFactory.getTextArea();
    private boolean changed = false;
    private Settings settings;
    private String orig = "";
    
    public SelectFileTypesDialog(int module) {
        super();
        
        setTitle(DcResources.getText("lblFileTypes"));
        
        this.settings = DcModules.get(module).getSettings();
        
        build();
        setCenteredLocation();
        setModal(true);
        setSize(400, 300);
    }
    
    public boolean isChanged() {
        return changed;
    }
    
    private void apply() {
        String fileTypes = textFileTypes.getText();
        changed = !fileTypes.equals(orig);
        
        if (changed) {
            StringTokenizer st = new StringTokenizer(fileTypes, ",");
            String[] extensions = new String[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens())
                extensions[i++] = (String) st.nextElement();
            
            settings.set(DcRepository.ModuleSettings.stFileImportFileTypes, extensions);
        }
        close();
    }
    
    private void build() {
        //**********************************************************
        //Help panel
        //**********************************************************
        DcLongTextField explanation = ComponentFactory.getLongTextField();
        explanation.setText(DcResources.getText("msgImportSettingsExplanation"));
        ComponentFactory.setUneditable(explanation);
        
        //**********************************************************
        //Configuration panel
        //**********************************************************
        JPanel panelConfig = new JPanel();
        panelConfig.setLayout(Layout.getGBL());
        
        JScrollPane scroller = new JScrollPane(textFileTypes);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        String types = "";
        for (String type : settings.getStringArray(DcRepository.ModuleSettings.stFileImportFileTypes)) {
            types += types.length() > 0 ? "," : "";
            types += type;
        }
        
        orig = types;
        
        textFileTypes.setText(types);
        panelConfig.add(scroller,      Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(0, 5, 5, 5), 0, 0));
        
        //**********************************************************
        //Action panel
        //**********************************************************
        JPanel panelAction = new JPanel();
        JButton buttonCancel = ComponentFactory.getButton(DcResources.getText("lblCancel"));
        JButton buttonApply = ComponentFactory.getButton(DcResources.getText("lblOK"));
        
        buttonCancel.setActionCommand("cancel");
        buttonApply.setActionCommand("apply");
        
        buttonCancel.addActionListener(this);
        buttonApply.addActionListener(this);
        
        panelAction.add(buttonCancel);
        panelAction.add(buttonApply);
        
        //**********************************************************
        //Main panel
        //**********************************************************
        getContentPane().setLayout(Layout.getGBL());
        
        getContentPane().add(explanation, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 5, 5, 5), 0, 0));
        getContentPane().add(panelConfig, Layout.getGBC( 0, 1, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelAction, Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("apply")) {
            apply();
        } else if (ae.getActionCommand().equals("cancel")) {
            changed = false;
            close();
        }
    }
    
    @Override
    public void close() {
        settings = null;
        textFileTypes = null;
        orig = null;
        super.close();
    }
}
