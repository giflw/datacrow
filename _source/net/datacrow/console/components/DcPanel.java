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

package net.datacrow.console.components;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;

public class DcPanel extends JPanel {

	private String helpIndex = "";

    private TitledBorder border = ComponentFactory.getTitleBorder(DcResources.getText("lblStatus"));
    private JLabel labelStatus = ComponentFactory.getLabel("");
    private DcProgressBar progress = new DcProgressBar();
    private String title;
    private ImageIcon icon;

    private final JPanel panelStatus = new JPanel();

    public DcPanel() {
        createStatusPanel();
    }
    
    public DcPanel(String title, ImageIcon icon) {
        this.title = title;
        this.icon = icon;
        createStatusPanel();
    }

    public void setTitle(String s) {
        this.title = s;
    }
    
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
    
    public String getTitle() {
    	return title;
    }

    public ImageIcon getIcon() {
    	return icon;
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);

        if (labelStatus != null) {
            Font system = DcSettings.getFont(DcRepository.Settings.stSystemFontNormal);
            labelStatus.setFont(system);
            border.setTitleFont(system);
        }
    }

    public void setHelpIndex(String helpIndex) {
        this.helpIndex = helpIndex;
    }

    public String getHelpIndex() {
    	return helpIndex;
    }

    public void setStatus(final String text) {
        if (text != null) {
            if (SwingUtilities.isEventDispatchThread()) {
                labelStatus.setText(text);
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        labelStatus.setText(text);
                    }
                });
            }
        }
    }
    
    
    public void setMaxForProgressBar(final int maxValue) {
        if (SwingUtilities.isEventDispatchThread()) {
            progress.setMaximum(maxValue);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setMaximum(maxValue);
                }
            });
        }
    }

    public void initProgressBar(final int maxValue) {
        if (SwingUtilities.isEventDispatchThread()) {
            progress.setValue(0);
            progress.setMaximum(maxValue);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setValue(0);
                    progress.setMaximum(maxValue);
                }
            });
        }
    }

    public void updateProgressBar() {
        if (SwingUtilities.isEventDispatchThread()) {
            progress.setValue(progress.getValue() + 1);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setValue(progress.getValue() + 1);                }
            });
        }
    }
    
    public void updateProgressBar(final int value) {
        if (SwingUtilities.isEventDispatchThread()) {
            progress.setValue(value);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setValue(value);
                }
            });
        }
    }

    public JPanel getStatusPanel() {
    	return panelStatus;
    }
    
    public void clear() {
        helpIndex = null;
        border = null;
        labelStatus = null;
        progress = null;
        title = null;
        icon = null;
    }

    private void createStatusPanel() {
        panelStatus.setLayout(Layout.getGBL());

        labelStatus.setBorder(border);
        labelStatus.setText(DataCrow.getVersion().toString());
        JPanel panelProgress = new JPanel();
        panelProgress.setLayout(Layout.getGBL());
        panelProgress.add(progress, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                        GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));

        panelStatus.add(labelStatus, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                        GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        panelStatus.add(panelProgress, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                        GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
    }
}
