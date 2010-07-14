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

package net.datacrow.core.web.beans;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;
import javax.servlet.http.HttpServletResponse;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.web.model.DcWebObject;
import net.datacrow.core.web.model.DcWebObjects;

import org.apache.myfaces.custom.navmenu.NavigationMenuItem;

public class DownloadFileBean extends DcBean {

    public String open() {
        
        if (!isLoggedIn())
            return redirect();
        
        FacesContext fc = FacesContext.getCurrentInstance();
        VariableResolver vr = fc.getApplication().getVariableResolver();
        DcWebObjects objects = (DcWebObjects) vr.resolveVariable(fc, "webObjects");
        List<?> data = (List<?>) objects.getData().getRowData();
        
        DcWebObject wod = (DcWebObject) vr.resolveVariable(fc, "webObject");
        int moduleIdx = objects.getModule();

        if (!getUser().isAuthorized(DcModules.get(moduleIdx)))
            return redirect();

        wod.initialize(moduleIdx);
        wod.setRowIdx(objects.getData().getRowIndex());
        wod.setID((Long) data.get(data.size() - 1));
        wod.setName(wod.getDcObject().toString());
        wod.load();
        
        DcObject dco = wod.getDcObject();
        String filename = dco.getFilename();
        File file = new File(filename);
        HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

        int read = 0;
        byte[] bytes = new byte[1024];
        response.setContentType("application/data");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + file.getName() + "\""); 
        
        try {
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = response.getOutputStream();
            while((read = fis.read(bytes)) != -1){
                os.write(bytes,0,read);
            }
            os.flush();
            os.close();
            FacesContext.getCurrentInstance().responseComplete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return current();
    }
    
    @Override
    public String back() {
        return "search";
    }

    @Override
    public String current() {
        return "download";
    }

    @Override
    public String getActionListener() {
        return "#{download.actionListener}";
    }

    @Override
    public List<NavigationMenuItem> getMenuItems() {
        return new ArrayList<NavigationMenuItem>();
    }

}
