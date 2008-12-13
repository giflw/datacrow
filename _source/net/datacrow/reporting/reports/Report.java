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

package net.datacrow.reporting.reports;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;

import net.datacrow.console.windows.reporting.ReportingDialog;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;

import org.apache.log4j.Logger;

/**
 * Representation of a report. Is capable of generating itself.
 * @author Robert Jan van der Waals
 */
public abstract class Report {

    private static Logger logger = Logger.getLogger(Report.class.getName());
    
    protected Collection<DcObject> objects;
    protected File target;
    protected ReportingDialog dialog;
    protected BufferedOutputStream bos;

    protected ReportThread rt;
    
    protected boolean keepOnRunning = true;
    
    protected boolean successfull = true;

    /**
     * Creates a new instance.
     */
    public Report() {}
    
    /**
     * Compiles the report.
     * @param dialog The dialog to inform about the status of the compilation.
     * @param objects The items to add to the report.
     * @param target The target file.
     */
    public void compile(ReportingDialog dialog, Collection<DcObject>  objects, File target) {
        successfull = true;
        keepOnRunning = true;
        this.dialog = dialog;
        this.objects = objects;
        this.target = target;
        
        rt = new ReportThread();
        rt.start();
    }
    
    /**
     * Indicates if the report creation process was successful.
     */
    public boolean isSuccessfull() {
        return successfull;
    }

    /**
     * Joins the threads.
     */
    public void join() {
        try {
            rt.join();
        } catch (InterruptedException ignore) {}
    }
    
    /**
     * Cancel the report generation.
     */
    public void cancel() {
        keepOnRunning = false;
    }
    
    protected void initialize() throws Exception {
        bos = new BufferedOutputStream(new FileOutputStream(target));
        dialog.initProgressBar(objects.size());
        dialog.addMessage(DcResources.getText("msgCreatingReportForXObjects", String.valueOf(objects.size())));
    }    
    
    private class ReportThread extends Thread {
        @Override
        public void run() {
            try {
                create();
            } catch (Exception exp) {
                successfull = false;
                logger.error(DcResources.getText("msgErrorWhileCreatingReport", exp.toString()), exp);
                dialog.addMessage(DcResources.getText("msgErrorWhileCreatingReport", exp.toString()));
            } finally {
                objects = null;
                target = null;
                bos = null;
                dialog.allowActions(true);
                dialog = null;
            }
        }
    }

    /**
     * Creates a new report.
     * @throws Exception
     */
    public abstract void create() throws Exception;
    
    /**
     * The file type.
     * @return File extension.
     */
    public abstract String getFileType();
}
