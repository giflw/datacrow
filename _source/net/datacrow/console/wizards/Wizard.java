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

package net.datacrow.console.wizards;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcFrame;
import net.datacrow.console.components.DcLongTextField;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.wizards.item.InternetWizardPanel;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.resources.DcResources;

/**
 * Base for all GUI wizards of Data Crow. The wizard is used to navigate through
 * a list of wizard panels (GUI components implementing the IWizardPanel interface).
 * This wizard base implementation contains methods and checks for navigation.
 * 
 * @author Robert Jan van der Waals
 */
public abstract class Wizard extends DcFrame implements ActionListener {

    protected int current = 0;

    private DcLongTextField textHelp;
    
    private List<IWizardPanel> wizardPanels;
    
    private JButton buttonNext;
    private JButton buttonBack;
    private JButton buttonRestart;
    private JButton buttonFinish;
    private JButton buttonClose;
    
    private boolean cancelled = false;
    private boolean restarted = false;
    
    protected boolean closed = false;

    public Wizard() {
        super("", IconLibrary._icoWizard);
        
        initialize();
        
        if (closed) 
            return;
        
        wizardPanels = getWizardPanels();
        
        build();
        
        addKeyListener(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), new NextOnEnterAction(), "next");
    }
    
    protected abstract String getWizardName();
    protected abstract void saveSettings();
    public abstract void finish() throws WizardException;
    public abstract void next() throws WizardException;
    protected abstract void restart();
    protected abstract void initialize();
    protected abstract List<IWizardPanel> getWizardPanels();    

    @Override
    public void close() {
        
        if (wizardPanels != null) {
            for (IWizardPanel panel : wizardPanels)
                panel.destroy();
            
            wizardPanels.clear();
        }
        
        wizardPanels = null;
        textHelp = null;
        buttonNext = null;
        buttonBack = null;
        buttonRestart = null;
        buttonFinish = null;
        buttonClose = null;
        
        super.close();
    }
    
    protected void applyPanel() {
        buttonBack.setEnabled(current != 0);
        buttonNext.setVisible(current != wizardPanels.size() - 1);
        buttonFinish.setVisible(current == wizardPanels.size() - 1);
        buttonRestart.setVisible(current == wizardPanels.size() - 1);
        textHelp.setText(getCurrent().getHelpText());
        
        if (buttonFinish.isVisible())
            buttonFinish.requestFocus();
        else
            buttonNext.requestFocus();

        setTitle(getWizardName());
        
        repaint();
        
        if (getCurrent() != null) {
            getCurrent().revalidate();
            getCurrent().repaint();
        }
    }

    protected boolean isRestarted() {
        return restarted;
    }
    
    protected boolean isCancelled() {
        return cancelled;
    }
    
    protected IWizardPanel getCurrent() {
        return wizardPanels.get(current);
    }

    protected int getStepCount() {
        return wizardPanels.size();
    }
    
    protected IWizardPanel getWizardPanel(int step) {
        return wizardPanels.get(step);
    }
    
    protected void back() {
        current -= 1;
        int counter = 0;

        if (current >= 0) {
            for (IWizardPanel panel : wizardPanels) {
                panel.setVisible(counter == current);
                counter++;
            }
        } else {
            current += 1;
        }

        applyPanel();
    }
    
    private void build() {
        getContentPane().setLayout(Layout.getGBL());
        getContentPane().add(getInformationPanel(), Layout.getGBC(1, 0, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                             new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(getActionPanel(), Layout.getGBC(1, 3, 1, 2, 1.0, 1.0
                            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                             new Insets(5, 5, 5, 15), 0, 0));

        // the actual content panels
        int counter = 0;
        for (IWizardPanel wp : wizardPanels) {
            JPanel panel = (JPanel) wp;
            panel.setMinimumSize(new Dimension(350,350));
            panel.setPreferredSize(new Dimension(350,350));
            panel.setVisible(counter == 0);
            getContentPane().add(panel,   Layout.getGBC(1, 1, 1, 1, 20.0, 20.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                             new Insets(5, 5, 5, 5), 0, 0));
        }

        setResizable(true);
        applyPanel();
        
        for (IWizardPanel panel : wizardPanels)
            panel.setVisible(false);
        
        getWizardPanel(0).setVisible(true);
        
        pack();
    }
    
    private JPanel getInformationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());
        
        textHelp = ComponentFactory.getHelpTextField();

        panel.add(textHelp, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                           ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                            new Insets(5, 5, 5, 5), 0, 0));
        return panel;
    }
    
    private JPanel getActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        buttonNext = ComponentFactory.getButton(DcResources.getText("lblNext"));
        buttonBack = ComponentFactory.getButton(DcResources.getText("lblBack"));
        buttonRestart = ComponentFactory.getButton(DcResources.getText("lblFinishRestart"));
        buttonFinish = ComponentFactory.getButton(DcResources.getText("lblFinish"));
        buttonClose = ComponentFactory.getButton(DcResources.getText("lblCancel"));
        
        buttonNext.addActionListener(this);
        buttonNext.setActionCommand("next");

        buttonBack.addActionListener(this);
        buttonBack.setActionCommand("back");
        
        buttonRestart.addActionListener(this);
        buttonRestart.setActionCommand("restart");
        
        buttonFinish.addActionListener(this);
        buttonFinish.setActionCommand("finish");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        buttonNext.setMnemonic('N');
        buttonBack.setMnemonic('B');
        buttonFinish.setMnemonic('F');
        buttonRestart.setMnemonic('R');
        buttonClose.setMnemonic('C');
        
        buttonBack.setEnabled(false);
        buttonFinish.setVisible(false);
        buttonRestart.setVisible(false);
        
        panel.add(buttonBack);
        panel.add(buttonNext);
        panel.add(buttonRestart);
        panel.add(buttonFinish);
        panel.add(buttonClose);
        
        return panel;
    }    
    
    public boolean isAtTheEnd() {
        return current == wizardPanels.size() - 1;
    }
    
    private class NextOnEnterAction extends AbstractAction {
        public void actionPerformed(ActionEvent arg0) {
            try {
                if (getCurrent() instanceof InternetWizardPanel)
                    return;
                
                if (!isAtTheEnd())
                    next();
                else
                    finish();
            } catch (WizardException we) {
                new MessageBox(we.getMessage(), MessageBox._ERROR);
            }
        }
    }


    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("restart")) {
            restarted = true;
            restart();
        } else if (e.getActionCommand().equals("next")) {
            try {
                next();
            } catch (Exception exp) {
                new MessageBox(exp.getMessage(), MessageBox._WARNING);
            }
        } else if (e.getActionCommand().equals("close")) {
            saveSettings();
            cancelled = true;
            close();
        } else if (e.getActionCommand().equals("back")) {
            back();
        } else if (e.getActionCommand().equals("finish")) {
            try {
                finish();
            } catch (Exception exp) {
                new MessageBox(exp.getMessage(), MessageBox._WARNING);
            }
        }
    }
}
