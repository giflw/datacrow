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

import java.util.StringTokenizer;

public class Version {

    private int minor = 0;
    private int major = 0;
    private int build = 0;
    private int patch = 0;

    public Version(int major, int minor, int build, int patch) {
        this.minor = minor;
        this.major = major;
        this.build = build;
        this.patch = patch;
    }
    
    public Version(String version) {
        String v = version.toLowerCase().startsWith("data crow") ? version.substring(10) : version;
        StringTokenizer st = new StringTokenizer(v, ".");
        if (st.hasMoreElements())
            major = Integer.valueOf((String) st.nextElement());
        if (st.hasMoreElements())
            minor = Integer.valueOf((String) st.nextElement());
        if (st.hasMoreElements())
            build = Integer.valueOf((String) st.nextElement());
        if (st.hasMoreElements())
            patch = Integer.valueOf((String) st.nextElement());
    }
    
    public int getMinor() {
        return minor;
    }

    public int getMajor() {
        return major;
    }

    public int getBuild() {
        return build;
    }

    public int getPatch() {
        return patch;
    }
    
    public boolean isUndetermined() {
        return hashCode() == 0;
    }

    public boolean isNewer(Version v) {
        return v.hashCode() > hashCode();
    }

    public boolean isOlder(Version v) {
        return hashCode() < v.hashCode();
    }
    
    public String getFullString() {
        return "Data Crow " + toString() + " beta";
    }
    
    @Override
    public String toString() {
        return major + "." + minor + "." + build + (patch > 0 ? String.valueOf(patch) : "");
    }
    
    @Override
    public int hashCode() {
        return (major * 10000) + (minor * 1000) + (build * 100) + (patch * 10);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Version)
            return ((Version) o).hashCode() == hashCode();
            
        return false;
    }
}
