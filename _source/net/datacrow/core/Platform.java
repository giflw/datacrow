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

public final class Platform {
	
	private boolean isWin;
	private boolean isMac; 
	private boolean isLinux; 
	private boolean isVista;
	
	private boolean isJava2;
	private boolean isJava14;
	private boolean isJava15;
	private boolean isJava16; 
	
	protected Platform() {
		String os = System.getProperty("os.name");
		isWin = os.startsWith("Windows");
		isMac = !isWin && os.startsWith("Mac");
		isLinux = os.startsWith("Linux");
		isVista = isWin && os.indexOf("Vista")!=-1;
		String version = System.getProperty("java.version").substring(0,3);
		
		isJava2  = version.compareTo("1.1") > 0;
		isJava14 = version.compareTo("1.3") > 0;
		isJava15 = version.compareTo("1.4") > 0;
		isJava16 = version.compareTo("1.5") > 0;
	}

	public final boolean isWin() {
		return isWin;
	}

	public final boolean isMac() {
		return isMac;
	}

	public final boolean isLinux() {
		return isLinux;
	}

	public final boolean isVista() {
		return isVista;
	}

	public final boolean isJava2() {
		return isJava2;
	}

	public final boolean isJava14() {
		return isJava14;
	}

	public final boolean isJava15() {
		return isJava15;
	}

	public final boolean isJava16() {
		return isJava16;
	}
}
