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

package net.datacrow.drivemanager;

import net.datacrow.util.Utilities;

public class FileInfo {

    private String hash;
    private String filename;
    private Long size;
    
    public FileInfo(String hash, String filename, Long size) {
        super();
        this.hash = hash;
        this.filename = filename;
        this.size = size;
    }

    public boolean valid(int precision) {
        return  hash != null && size != null ||
                (precision == DriveManager._PRECISION_LOWEST) ||
                (precision == DriveManager._PRECISION_HIGHEST && (hash != null && size != null)) ||
                (precision == DriveManager._PRECISION_MEDIUM && (size != null));
    }
    
    public String getFilename() {
        return filename;
    }

    public String getHash() {
        return hash;
    }

    public Long getSize() {
        return size;
    }
    
    public void clear() {
        size = null;
        hash = null;
        filename = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FileInfo) {
            FileInfo fi = ((FileInfo) o); 
            return fi.getFilename().equals(filename) &&
                   Utilities.getComparableString(getHash()).equals(Utilities.getComparableString(fi.getHash())) &&
                   ((getSize() == null && fi.getSize() == null) ||  
                    (getSize() != null && fi.getSize() != null && fi.getSize().equals(getSize())));
        } else {
            return false;
        }
    }
}
