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

/**
 * A version definition.
 * 
 * @author Robert Jan van der Waals
 */
public class Version {

    private int minor = 0;
    private int major = 0;
    private int build = 0;
    private int patch = 0;

    /**
     * Creates a new version
     * @param major
     * @param minor
     * @param build
     * @param patch
     */
    public Version(int major, int minor, int build, int patch) {
        this.minor = minor;
        this.major = major;
        this.build = build;
        this.patch = patch;
    }
    
    /**
     * Creates a version based on a string representation.
     * @param version
     */
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
    
    /**
     * The minor version number
     */
    public int getMinor() {
        return minor;
    }

    /**
     * The major version number
     */
    public int getMajor() {
        return major;
    }

    /**
     * The build version number
     */
    public int getBuild() {
        return build;
    }

    /**
     * The patch version number
     */
    public int getPatch() {
        return patch;
    }
    
    /**
     * Checks whether the version is valid.
     */
    public boolean isUndetermined() {
        return hashCode() == 0;
    }

    /**
     * Checks if this version is newer than the supplied version.
     * @param v
     */
    public boolean isNewer(Version v) {
        return hashCode() > v.hashCode();
    }

    /**
     * Checks if this version is older than the supplied version.
     * @param v
     */
    public boolean isOlder(Version v) {
        return hashCode() < v.hashCode();
    }
    
    /**
     * Full string representation of the current version. 
     */
    public String getFullString() {
        return "Data Crow " + toString();
    }
    
    @Override
    public String toString() {
        return major + "." + minor + "." + build + (patch > 0 ? "." + String.valueOf(patch) : "");
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
