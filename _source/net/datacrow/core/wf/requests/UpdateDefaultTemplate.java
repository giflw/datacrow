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

package net.datacrow.core.wf.requests;

import java.util.Collection;
import java.util.List;

import net.datacrow.core.data.DataManager;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcTemplate;

import org.apache.log4j.Logger;

public class UpdateDefaultTemplate implements IRequest {

    private static final long serialVersionUID = 294614205717327174L;

    private static Logger logger = Logger.getLogger(UpdateDefaultTemplate.class.getName());
    
    private String templateName;
    private final int module;

    public UpdateDefaultTemplate(String templateName, int module) {
        this.templateName = templateName;
        this.module = module;
    }

    @Override
    public void execute(Collection<DcObject> objects) {
        List<DcObject> templates = DataManager.get(module, null);
        for (DcObject dco : templates) {
            if (!dco.getValue(DcTemplate._SYS_TEMPLATENAME).equals(templateName)) {
                try {
                    dco.setValue(DcTemplate._SYS_DEFAULT, false);
                    dco.saveUpdate(false);
                } catch (Exception e) {
                    logger.error("Could not update template with ID " + dco.getID(), e);
                }
            }
        }
        
        end();
    }

    @Override
    public void end() {
        templateName = null;
    }
    
    @Override
    public boolean getExecuteOnFail() {
        return false;
    }

    @Override
    public void setExecuteOnFail(boolean b) {}
}