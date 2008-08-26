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

package net.datacrow.console.windows.settings;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.components.DcImageLabel;
import net.datacrow.console.components.DcTree;
import net.datacrow.core.DataCrow;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.settings.Settings;
import net.datacrow.core.settings.SettingsFile;
import net.datacrow.core.settings.SettingsGroup;

/**
 * Tree view with panels. Loads and saves settings
 *
 * @author Robert Jan van der Waals
 * @since 1.4
 * @version 1.9
 */
public class SettingsView extends DcDialog implements ActionListener {

    protected DcTree tree;
    
    private Settings settings;

    private InformationPanel panelInfo;
    private JPanel panelActions;
    private JPanel panelBogus = new JPanel();

    private JButton buttonSave;
    private JButton buttonClose;

    public SettingsView(String title, Settings settings) {
        super(DataCrow.mainFrame);

        setTitle(title);
        
        this.settings = settings;
        buildView();
        this.setTitle(title);
    }

    @Override
    public void close() {
        SettingsFile.save(settings);
        
        settings = null;
        tree = null;
        panelInfo = null;
        panelActions = null;
        panelBogus = null;
        buttonSave = null;
        buttonClose = null;
        
        super.close();
    }

    /**
     * Creates the action panel (save buttons and such)
     */
    private JPanel getActionPanel() {
        panelActions = new JPanel();

        buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));

        panelActions.add(buttonSave);
        panelActions.add(buttonClose);
        panelActions.setVisible(false);

        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        buttonSave.setMnemonic('S');
        buttonClose.setMnemonic('C');

        return panelActions;
    }

    @Override
    public void setFont(Font font) {

        if (tree != null && buttonClose != null) {
            buttonClose.setFont(ComponentFactory.getSystemFont());
            buttonSave.setFont(ComponentFactory.getSystemFont());
            tree.setFont(ComponentFactory.getSystemFont());

            for (SettingsPanel panel : getPanels()) {
                Component[] components = panel.getComponents();
                for (int i = 0; i < components.length; i++) {
                    if (components[i] instanceof JLabel || components[i] instanceof JCheckBox)
                        components[i].setFont(ComponentFactory.getSystemFont());
                    else
                        components[i].setFont(ComponentFactory.getStandardFont());
                }
            }
        }
    }

    public void setDisclaimer(ImageIcon icon) {
        panelInfo.setImage(icon);
    }

    /**
     * Puts the current values of the settings in the panel
     */
    private void initializeSettings() {
        for (SettingsPanel panel : getPanels())
            panel.initializeSettings();
    }
    
    /**
     * Saves all settings as they are defined in the panels
     */
    private void save() {
        for (SettingsPanel panel : getPanels())
            panel.saveSettings();

        DataCrow.mainFrame.applySettings();
        toFront();
    }

    @SuppressWarnings("unchecked")
    private Collection<SettingsPanel> getPanels() {
        Collection<SettingsPanel> panels = new ArrayList<SettingsPanel>();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        for (Enumeration<DefaultMutableTreeNode> enumerator = root.children(); enumerator.hasMoreElements(); ) {
            DefaultMutableTreeNode oCurrent = enumerator.nextElement();
            SettingsPanel nodePanel = (SettingsPanel) oCurrent.getUserObject();
            panels.add(nodePanel);

            for (Enumeration<DefaultMutableTreeNode> enumChilds = oCurrent.children(); enumChilds.hasMoreElements(); ) {
                DefaultMutableTreeNode oChild = enumChilds.nextElement();
                SettingsPanel nodePanelChild = (SettingsPanel) oChild.getUserObject();
                panels.add(nodePanelChild);
            }
        }
        return panels;
    }

    protected void setVisible(JPanel panel) {
        for (SettingsPanel settingsPanel : getPanels()) {
            if (settingsPanel.equals(panel))
                settingsPanel.setVisible(true);
            else
                settingsPanel.setVisible(false);
        }
    }

    protected void setPanelsVisible(boolean visible) {
        for (SettingsPanel panel : getPanels())
            panel.setVisible(visible);
    }

    private void buildView() {
        setResizable(true);

        panelInfo = new InformationPanel();
        panelInfo.setMinimumSize(new Dimension(700, 380));
        panelInfo.setPreferredSize(new Dimension(700, 380));
        panelInfo.setMaximumSize(new Dimension(700, 380));

        getContentPane().setLayout(Layout.getGBL());
        tree = new DcTree(buildTreeModel());
        tree.setFont(ComponentFactory.getSystemFont());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new NodeSelectedAction());
        tree.setBorder(new EtchedBorder());

        tree.setPreferredSize(new Dimension(300, 420));
        tree.setMinimumSize(new Dimension(300, 420));
        tree.setMaximumSize(new Dimension(300, 420));

        panelBogus.add(panelInfo,  Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                      ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                       new Insets(0, 0, 0, 0), 0, 0));

        getContentPane().add(tree,  Layout.getGBC( 0, 0, 1, 2, 2.0, 2.0
                            ,GridBagConstraints.NORTH, GridBagConstraints.NONE,
                             new Insets(5, 5, 0, 5), 0, 0));
        getContentPane().add(getActionPanel(),  Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                             new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelBogus,  Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                             new Insets(5, 5, 5, 5), 0, 0));
        initializeSettings();
        this.pack();
        setCenteredLocation();
    }    
    
    /**
     * Builds the tree model by reading the settings groups.
     * Each settingsgroup can contain several childeren, which are
     * added as leafs of the parent in the tree.
     * The settings panels are instantiated here as well.
     * Each leaf contains a specific panel with the settings components.
     */
    private DefaultMutableTreeNode buildTreeModel() {
        DefaultMutableTreeNode topTreeNode = new DefaultMutableTreeNode(DcResources.getText("lblSettings"));
        topTreeNode.setUserObject(panelInfo);

        
        for (SettingsGroup group : settings.getSettingsGroups().values()) { 
            JPanel panel = new SettingsPanel(group);
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(panel);

            topTreeNode.add(treeNode);

            panelBogus.setPreferredSize(new Dimension(700, 380));

            panel.setMinimumSize(new Dimension(700, 380));
            panel.setPreferredSize(new Dimension(700, 380));
            panel.setMaximumSize(new Dimension(700, 380));

            getContentPane().add(panel, Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                 new Insets(5, 5, 5, 5), 0, 0));

            for (SettingsGroup childGroup : group.getChildren().values()) {
                JPanel childPanel = new SettingsPanel(childGroup);
                DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(childPanel);
                treeNode.add(childTreeNode);

                childPanel.setMinimumSize(new Dimension(700, 380));
                childPanel.setPreferredSize(new Dimension(700, 380));
                childPanel.setMaximumSize(new Dimension(700, 380));

                getContentPane().add(childPanel, Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                     new Insets(5, 5, 5, 5), 0, 0));
            }
        }
        return topTreeNode;
    }

    /**
     * Information panel, holder of the disclaimer information
     */
    private class InformationPanel extends JPanel {

        public InformationPanel() {
            // Build the panel
            JLabel labelProduct = ComponentFactory.getLabel(DataCrow.getVersion().getFullString());
            JLabel urlField = ComponentFactory.getLabel("www.datacrow.net");
            JLabel labelEmail = ComponentFactory.getLabel("info@datacrow.net");

            this.setLayout(Layout.getGBL());

            this.add(labelProduct,    Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                      new Insets( 0, 30, 0, 5), 0, 0));
            this.add(urlField,        Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                      new Insets( 0, 30, 0, 5), 0, 0));
            this.add(labelEmail,      Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                      new Insets( 0, 30, 0, 5), 0, 0));
        }

        public void setImage(ImageIcon icon) {
            DcImageLabel imageLabel = new DcImageLabel(icon);
            
            
            this.add(imageLabel,        Layout.getGBC( 0, 0, 1, 1, 20.0, 20.0
                    , GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                      new Insets( 5, 5, 5, 5), 0, 0));            
            
            pack();
            revalidate();

        }

        @Override
        public String toString() {
            return DcResources.getText("lblSettings");
        }
    }    
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("save"))
            save();
        else if (ae.getActionCommand().equals("close"))
            close();
    }    

    private class NodeSelectedAction implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (node == null)
                return;

            Object nodeInfo = node.getUserObject();

            if (nodeInfo.equals(panelInfo)) {
                JPanel panel = (JPanel) nodeInfo;
                panel.setVisible(true);
                setPanelsVisible(false);
                panelActions.setVisible(false);
            } else {
                SettingsPanel panel = (SettingsPanel) nodeInfo;
                setHelpIndex(panel.getHelpIndex());
                setVisible(panel);
                panelInfo.setVisible(false);
                panelActions.setVisible(true);
            }
        }
    }
}
