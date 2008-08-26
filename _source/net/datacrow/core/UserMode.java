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

package net.datacrow.core;

import net.datacrow.settings.DcSettings;

public abstract class UserMode {
    
    public static final int _XP_BEGINNER = 0;
    public static final int _XP_EXPERT = 1;
    
    private static int current = _XP_EXPERT;
    
    public static void setUserMode(int mode) {
        if (current != mode) {
            current = mode;
            DataCrow.mainFrame.updateMenuBar();
            DcSettings.set(DcRepository.Settings.stXpMode, mode);
        }
    }
    
    public static int getUserMode() {
        return current;
    }
    
    public static boolean isCorrectXpLevel(int level) {
        if (getUserMode() == _XP_EXPERT)
            return true;
        else if (getUserMode() == _XP_BEGINNER && level == _XP_EXPERT)
            return false;
        else 
            return true;    
    }
}
