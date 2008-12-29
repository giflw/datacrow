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

import javax.swing.ImageIcon;

/**
 * Holder of all icons. This class makes sure that icons are only loaded once.
 * Images from this class should never be unloaded.
 * 
 * @author Robert Jan van der Waals
 */
public abstract class IconLibrary {

    public static final String picPath = DataCrow.installationDir + "icons_system/";
    
    public static final ImageIcon _icoRotateLeft = new ImageIcon(picPath + "rotate_left.png");
    public static final ImageIcon _icoRotateRight = new ImageIcon(picPath + "rotate_right.png");
    public static final ImageIcon _icoMain = new ImageIcon(picPath + "datacrow.png");
    public static final ImageIcon _icoIcon16 = new ImageIcon(picPath + "icon16.png");
    public static final ImageIcon _icoIcon32 = new ImageIcon(picPath + "icon32.png");
    public static final ImageIcon _icoChart = new ImageIcon(picPath + "chart.png");
    public static final ImageIcon _icoPersons = new ImageIcon(picPath + "persons.png");
    public static final ImageIcon _icoModuleTypeProperty = new ImageIcon(picPath + "moduletype_property.png");
    public static final ImageIcon _icoModuleTypePlain = new ImageIcon(picPath + "moduletype_other.png");
    public static final ImageIcon _icoModuleTypeMedia = new ImageIcon(picPath + "moduletype_media.png");
    public static final ImageIcon _icoTreeLeaf = new ImageIcon(picPath + "tree_leaf.png");
    public static final ImageIcon _icoTreeOpen = new ImageIcon(picPath + "tree_open.png");
    public static final ImageIcon _icoTreeClosed = new ImageIcon(picPath + "tree_closed.png");
    public static final ImageIcon _icoCalendar = new ImageIcon(picPath + "calendar.png");
    public static final ImageIcon _icoTemplate = new ImageIcon(picPath + "template.png");
    public static final ImageIcon _icoRenumber = new ImageIcon(picPath + "renumber.png");
    public static final ImageIcon _icoTitleRewriter = new ImageIcon(picPath + "title_rewriter.png");
    public static final ImageIcon _icoMassUpdate = new ImageIcon(picPath + "massupdate.png");
    public static final ImageIcon _icoWebServer = new ImageIcon(picPath + "webserver.png");
    public static final ImageIcon _icoAccept = new ImageIcon(picPath + "accept.png");
    public static final ImageIcon _icoLAF = new ImageIcon(picPath + "laf.png");
    public static final ImageIcon _icoFilter = new ImageIcon(picPath + "filter.png");
    public static final ImageIcon _icoCut = new ImageIcon(picPath + "cut.png");
    public static final ImageIcon _icoCopy = new ImageIcon(picPath + "copy.png");
    public static final ImageIcon _icoPaste = new ImageIcon(picPath + "paste.png");
    public static final ImageIcon _icoUpdateAll = new ImageIcon(picPath + "updateall.png");
    public static final ImageIcon _icoSort = new ImageIcon(picPath + "sort.png");
    public static final ImageIcon _icoExpert = new ImageIcon(picPath + "expert.png");
    public static final ImageIcon _icoBeginner = new ImageIcon(picPath + "beginner.png");
    public static final ImageIcon _icoContainer16 = new ImageIcon(picPath + "container16.png");
    public static final ImageIcon _icoContainer32 = new ImageIcon(picPath + "container32.png");
    public static final ImageIcon _icoUser16 = new ImageIcon(picPath + "user16.png");
    public static final ImageIcon _icoUser32 = new ImageIcon(picPath + "user32.png");
    public static final ImageIcon _icoPermission16 = new ImageIcon(picPath + "permission16.png");
    public static final ImageIcon _icoPermission32 = new ImageIcon(picPath + "permission32.png");
    public static final ImageIcon _icoPicture = new ImageIcon(picPath + "picture.png");
    public static final ImageIcon _icoAnchor = new ImageIcon(picPath + "anchor.png");
    public static final ImageIcon _icoMessages = new ImageIcon(picPath + "messages.gif");
	public static final ImageIcon _icoLabels = new ImageIcon(picPath + "labels.gif");
    public static final ImageIcon _icoTooltips = new ImageIcon(picPath + "tooltips.gif");
    public static final ImageIcon _icoSQLTool = new ImageIcon(picPath + "sqltool.png");
	public static final ImageIcon _icoFileImport = new ImageIcon(picPath + "importfile.png");
	public static final ImageIcon _icoImport = new ImageIcon(picPath + "import.png");
    public static final ImageIcon _icoReport = new ImageIcon(picPath + "report.png");
    public static final ImageIcon _icoNote = new ImageIcon(picPath + "note.png");
    public static final ImageIcon _icoLoan = new ImageIcon(picPath + "loan.png");
    public static final ImageIcon _icoBackup = new ImageIcon(picPath + "backup.png");
    public static final ImageIcon _icoClose = new ImageIcon(picPath + "close.png");
    public static final ImageIcon _icoRemove = new ImageIcon(picPath + "remove.png");
    public static final ImageIcon _icoStart = new ImageIcon(picPath + "start.png");
    public static final ImageIcon _icoStop = new ImageIcon(picPath + "stop.png");
    public static final ImageIcon _icoOpenApplication = new ImageIcon(picPath + "openapplication.png");
    public static final ImageIcon _icoFileRenamer = new ImageIcon(picPath + "filerenamer.png");
    public static final ImageIcon _icoDriveManager = new ImageIcon(picPath + "drivemanager.png");
    public static final ImageIcon _icoDriveScanner = new ImageIcon(picPath + "drivescanner.png");
    public static final ImageIcon _icoDrivePoller = new ImageIcon(picPath + "drivepoller.png");
    public static final ImageIcon _icoFileSynchronizer = new ImageIcon(picPath + "filesynchronizer.png");
    public static final ImageIcon _icoEventLog = new ImageIcon(picPath + "eventlog.png");
    public static final ImageIcon _icoItemsNew = new ImageIcon(picPath + "itemsnew.png");
    public static final ImageIcon _icoInformation = new ImageIcon(picPath + "information.png");
    public static final ImageIcon _icoInformationTechnical = new ImageIcon(picPath + "informationtechnical.png");
    public static final ImageIcon _icoExit = new ImageIcon(picPath + "exit.png");
    public static final ImageIcon _icoSearch = new ImageIcon(picPath + "search.gif");
    public static final ImageIcon _icoSearchAgain = new ImageIcon(picPath + "searchagain.gif");
    public static final ImageIcon _icoQuestion = new ImageIcon(picPath + "help.png");
    public static final ImageIcon _icoError = new ImageIcon(picPath + "error.png");
    public static final ImageIcon _icoHelp = new ImageIcon(picPath + "help.png");
    public static final ImageIcon _icoSearchOnline = new ImageIcon(picPath + "searchonline.png");
    public static final ImageIcon _icoWarning = new ImageIcon(picPath + "warning.png");
    public static final ImageIcon _icoSettings = new ImageIcon(picPath + "settings.png");
    public static final ImageIcon _icoAbout = new ImageIcon(picPath + "about.png");
    public static final ImageIcon _icoSave = new ImageIcon(picPath + "save.png");
    public static final ImageIcon _icoSaveAll = new ImageIcon(picPath + "saveall.png");
    public static final ImageIcon _icoAdd = new ImageIcon(picPath + "add.png");
    public static final ImageIcon _icoOpen = new ImageIcon(picPath + "open.png");
    public static final ImageIcon _icoOpenNew = new ImageIcon(picPath + "open_new.png");
    public static final ImageIcon _icoDelete = new ImageIcon(picPath + "delete.png");
    public static final ImageIcon _icoWizard = new ImageIcon(picPath + "wizard.png");
    public static final ImageIcon _icoTips = new ImageIcon(picPath + "tips.gif");
    public static final ImageIcon _icoCardView = new ImageIcon(picPath + "viewcard.gif");
    public static final ImageIcon _icoTableView = new ImageIcon(picPath + "viewtable.gif");
    public static final ImageIcon _icoUnchecked = new ImageIcon(picPath + "unchecked.gif");
    public static final ImageIcon _icoChecked = new ImageIcon(picPath + "checked.gif");
    public static final ImageIcon _icoArrowTop = new ImageIcon(picPath + "arrow_top.png");
    public static final ImageIcon _icoArrowBottom = new ImageIcon(picPath + "arrow_bottom.png");
    public static final ImageIcon _icoArrowUp = new ImageIcon(picPath + "arrow_up.png");
    public static final ImageIcon _icoArrowDown = new ImageIcon(picPath + "arrow_down.png");
}
