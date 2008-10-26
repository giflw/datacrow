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

package net.datacrow.console.windows.onlinesearch;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcDialog;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.services.SearchTask;
import net.datacrow.util.DcSwingUtilities;

import org.apache.log4j.Logger;

public class RetrieveItemDetailsDialog extends DcDialog implements ActionListener, KeyListener {
    
    private static Logger logger = Logger.getLogger(RetrieveItemDetailsDialog.class.getName());
    
    private JProgressBar bar = new JProgressBar();
    private SearchTask task;
    private DcObject dco;
    
    public RetrieveItemDetailsDialog(SearchTask task, DcObject dco) {
        super(DcSwingUtilities.getRootFrame());
        
        this.task = task;
        this.dco = dco;
        
        JButton btStart = ComponentFactory.getIconButton(IconLibrary._icoStart);
        btStart.setActionCommand("start");
        btStart.addActionListener(this);
        btStart.addKeyListener(this);

        getContentPane().setLayout(Layout.getGBL());
        getContentPane().add(bar,  Layout.getGBC( 0, 0, 1, 1, 50.0, 50.0
                ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(btStart,  Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));
        
        pack();
        
        setSize(new Dimension(500, 100));
        setCenteredLocation();
        setResizable(false);

        setTitle("Retrieving information for " + dco.getName());
        
        btStart.requestFocusInWindow();
        setModal(true);
        setVisible(true);
    }
    
    private void start() {
        FillerTask filler = new FillerTask(task, dco);
        filler.start();
        
        PollerTask poller = new PollerTask(filler);
        poller.start();
    }
    
    @Override
    public void close() {
        bar = null;
        task = null;
        dco = null;
        super.close();
    }



    private class PollerTask extends Thread {
        
        private FillerTask filler;
        
        public PollerTask(FillerTask filler) {
            this.filler = filler;
        }
        
        @Override
        public void run() {
            bar.setMinimum(0);
            bar.setMaximum(100);
            
            while (filler.isAlive()) {
                int value = bar.getValue();
                value = value < bar.getMaximum() ? value + 1 : 0; 
                bar.setValue(value);
                try {
                    sleep(10);
                } catch (Exception ignore) {}
            }
            
            SwingUtilities.invokeLater(
                new Thread(new Runnable() { 
                    public void run() {
                        close();
                    }
                }));
        }
    }
    
    private class FillerTask extends Thread {
        
        private SearchTask task;
        private DcObject dco;
        
        public FillerTask(SearchTask task, DcObject dco) {
            this.dco = dco;
            this.task = task;
            setPriority(Thread.NORM_PRIORITY);
        }
        
        @Override
        public void run() {
            try {
                DcObject dcoNew = task.query(dco);
                dco.copy(dcoNew, false);
                dco.setIDs();
            } catch (Exception e) {
                new MessageBox(e.getMessage(), MessageBox._ERROR);
                logger.error(e, e);
            }
        }
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("start"))
            start();
    }

    public void keyPressed(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            start();
    }
}
