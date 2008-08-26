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

package net.datacrow.console.windows;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcButton;
import net.datacrow.console.components.DcCheckBox;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.DcTextPane;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class TipOfTheDayDialog extends DcDialog implements ActionListener {

    private static Logger logger = Logger.getLogger(TipOfTheDayDialog.class.getName());
    
    private DcTextPane tipPane = ComponentFactory.getTextPane();
    private DcCheckBox checkShowTips = ComponentFactory.getCheckBox(DcResources.getText("lblShowTipsOnStartup"));
    
    private HTMLEditorKit kit = new HTMLEditorKit();
    private HTMLDocument document = new HTMLDocument();

    private List<String> tips = new LinkedList<String>();
    
    private int currentTip = 0;
    
    public TipOfTheDayDialog() {
        super(DataCrow.mainFrame);
        setTitle(DcResources.getText("lblTipOfTheDay"));
        
        build();
        setSize(400, 300);
        setCenteredLocation();
    }
    
    private void showNextTip() {
        currentTip = currentTip < tips.size() -1 ? currentTip + 1 : 0; 
        showNextTip(currentTip);
    }    

    private void showNextTip(int idx) {
        try {
            String tip = tips.get(idx);
            String html = "<html><body " + Utilities.getHtmlStyle() + ">" + tip + "</body></html>";
            tipPane.setText("");
            kit.insertHTML(document, 0, html, 0, 0, null);
        } catch (Exception e) {
            logger.error("Could not set the html", e);
        }
    }    
    
    private void build() {
        tipPane.setEditorKit(kit);
        tipPane.setDocument(document);
        tipPane.setEditable(false);
        tipPane.setBounds(1,1,1,1);
        
        getContentPane().setLayout(Layout.getGBL());
        
        DcButton buttonNext = ComponentFactory.getButton(DcResources.getText("lblNext"));
        DcButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        buttonNext.addActionListener(this);
        buttonNext.setActionCommand("next");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        checkShowTips.addActionListener(this);
        checkShowTips.setActionCommand("toggleActive");
        checkShowTips.setSelected(DcSettings.getBoolean(DcRepository.Settings.stShowTipsOnStartup));
        
        JScrollPane scroller = new JScrollPane(tipPane);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JPanel panelActions = new JPanel();
        panelActions.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panelActions.add(buttonNext);
        panelActions.add(buttonClose);
        
        buttonNext.setMnemonic('N');
        buttonClose.setMnemonic('C');
        
        getContentPane().add(scroller,      Layout.getGBC( 0, 0, 4, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        getContentPane().add(checkShowTips, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions,  Layout.getGBC( 3, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 0), 0, 0));
        
        setResizable(false);
        pack();

        initializeTips();
        currentTip = new Random().nextInt(tips.size());
        showNextTip(currentTip);
    }
    
    
    private void initializeTips() {
        Properties properties = DcResources.getResources();
        
        for (Object o : properties.keySet()) {
            String key = (String) o;
            if (key.startsWith("tip")) 
                tips.add((String) properties.get(key));
        }
    }
    
    @Override
    public void close() {
        kit = null;
        document = null;
        tipPane = null;
        checkShowTips = null;
        
        tips.clear();
        tips = null;
        super.close();
    }    
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("toggleActive"))
            DcSettings.set(DcRepository.Settings.stShowTipsOnStartup, checkShowTips.isSelected());
        else if (ae.getActionCommand().equals("next"))
            showNextTip();
        else if (ae.getActionCommand().equals("close"))
            close();
    }    
}
