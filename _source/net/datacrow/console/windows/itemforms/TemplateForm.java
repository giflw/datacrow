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

package net.datacrow.console.windows.itemforms;

import net.datacrow.core.DataCrow;
import net.datacrow.core.DcRepository;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.template.Templates;
import net.datacrow.core.wf.requests.RefreshPropertyItemViewRequest;
import net.datacrow.core.wf.requests.Requests;

public class TemplateForm extends DcMinimalisticItemView {

    public TemplateForm(int module, boolean readonly) {
        super(module, readonly);
        setHelpIndex("dc.items.templates");
    }

    @Override
    public void addObjects() {
        Templates.refresh();
        list.clear();
        list.add(Templates.getTemplates(getModuleIdx()));
    }

    @Override
    public void close() {
        getModule().setSetting(DcRepository.ModuleSettings.stSimpleItemViewSize, getSize());
        DataCrow.mainFrame.updateMenuBar();
        super.close();      
    }

    @Override
    public void createNew() {
        TemplateItemForm form = new TemplateItemForm(false, getModule().getDcObject(), this);
        form.setVisible(true);
    }

    @Override
    public Requests getAfterDeleteRequests() {
        Requests requests = new Requests();
        requests.add(new RefreshPropertyItemViewRequest(this));
        return requests;
    }

    @Override
    public void open() {
        DcObject dco = list.getSelectedItem();
        if (dco != null) {
            dco.markAsUnchanged();
            TemplateItemForm form = new TemplateItemForm(true, dco, this);
            form.setVisible(true);
        }
    }
}
