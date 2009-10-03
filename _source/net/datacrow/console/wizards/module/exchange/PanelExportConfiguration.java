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

package net.datacrow.console.wizards.module.exchange;

import net.datacrow.console.Layout;
import net.datacrow.console.wizards.Wizard;
import net.datacrow.console.wizards.WizardException;

public class PanelExportConfiguration extends ModuleExportWizardPanel {

    public PanelExportConfiguration(Wizard wizard, boolean exists) {
        build();
    }
    
    public Object apply() throws WizardException {
        
        return null;
    }

    @Override
    public String getHelpText() {
        return "";//DcResources.getText("msgBasicModuleInfo");
    }
    
    public void destroy() {
    }    
    
    private void build() {
        setLayout(Layout.getGBL());
        
//        add(checkCanBeLended, 
//                Layout.getGBC(1, 4, 1, 1, 1.0, 1.0
//               ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
//                new Insets( 5, 5, 5, 5), 0, 0));
//        add(ComponentFactory.getLabel(DcResources.getText("lblDescription")), 
//                Layout.getGBC(0, 5, 1, 1, 1.0, 1.0
//               ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
//                new Insets( 5, 5, 5, 5), 0, 0));  
    }
}
