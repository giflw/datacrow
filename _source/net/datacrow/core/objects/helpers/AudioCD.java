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

package net.datacrow.core.objects.helpers;

import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcMediaObject;

public class AudioCD extends DcMediaObject {

    private static final long serialVersionUID = -9181349541953148790L;
    
    public static final int _F_ARTIST = 1;
    public static final int _G_GENRES = 2;
    public static final int _H_PICTUREFRONT = 3;
    public static final int _I_PICTUREBACK = 4;
    public static final int _J_PICTURECD = 5;
    public static final int _K_DISCID = 6;
    public static final int _L_STATE = 7;
    public static final int _M_WEBPAGE = 8;
    public static final int _N_EAN = 9;
    public static final int _O_ASIN = 10;
    
    public AudioCD() {
        super(DcModules._AUDIOCD);
    }
}
