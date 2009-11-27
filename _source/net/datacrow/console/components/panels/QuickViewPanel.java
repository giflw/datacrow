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

package net.datacrow.console.components.panels;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcHtmlEditorPane;
import net.datacrow.console.components.DcPictureField;
import net.datacrow.console.components.DcPopupMenu;
import net.datacrow.console.views.MasterView;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.plugin.PluginHelper;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.DcSettings;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.QuickViewFieldDefinition;
import net.datacrow.settings.definitions.QuickViewFieldDefinitions;
import net.datacrow.util.StringUtils;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class QuickViewPanel extends JPanel implements ChangeListener, MouseListener {
    
    private static Logger logger = Logger.getLogger(QuickViewPanel.class.getName());
    
    public static final String _DIRECTION_HORIZONTAL = DcResources.getText("lblHorizontal");
    public static final String _DIRECTION_VERTICAL = DcResources.getText("lblVertical");  
    
    protected final boolean showImages;
    
    protected DcObject dco;
    private LinkedList<Picture> pictures = new LinkedList<Picture>();
    private LinkedList<JPanel> imagePanels = new LinkedList<JPanel>();

    private DcHtmlEditorPane descriptionPane;
    
    private JScrollPane scroller;
    private final JTabbedPane tabbedPane = ComponentFactory.getTabbedPane();
    
    public QuickViewPanel(boolean showImages) {
        this.showImages = showImages;
        setLayout(Layout.getGBL());
        tabbedPane.addChangeListener(this);
        buildPanel();
    }    
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);

        if (tabbedPane != null)
            tabbedPane.setFont(ComponentFactory.getSystemFont());
        
        if (dco != null) {
            setObject(dco, true);            
        }
    }
    
    public void reloadImage() {
        if (tabbedPane.getSelectedIndex() > 0)
            loadImage();
    }
    
    private void loadImage() {
        int index = tabbedPane.getSelectedIndex() - 1;
        Picture picture = pictures.get(index);
        picture.loadImage();
        
        JPanel panel = imagePanels.get(index);
        Component[] components =  panel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof DcPictureField)
                ((DcPictureField) components[i]).setValue(picture.getValue(Picture._D_IMAGE));
        }
    }
    
    public void createImageTabs() {
        try {
            clearImages();
    
            if (dco == null)
                return;
            
            for (DcFieldDefinition definition : dco.getModule().getFieldDefinitions().getDefinitions()) {
                if (    dco.isEnabled(definition.getIndex()) && 
                        dco.getField(definition.getIndex()).getValueType() == DcRepository.ValueTypes._PICTURE) {
                    
                    Picture picture = (Picture) dco.getValue(definition.getIndex());

                    if (picture == null) continue;
                    
                    if (picture.hasImage()) {
                    	pictures.add(picture);    

                    	DcPictureField picField = ComponentFactory.getPictureField(true, false, false);
                    	ComponentFactory.setBorder(this);
                        
                        JPanel panel = new JPanel();
                        panel.setLayout(Layout.getGBL());
                        panel.addMouseListener(this);
                        panel.add(picField, Layout.getGBC(0, 0, 1, 1, 10.0, 10.0,
                                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                new Insets(2, 2, 2, 2), 0, 0));
                            
                        tabbedPane.addTab(dco.getLabel(definition.getIndex()), IconLibrary._icoPicture, panel);
                        imagePanels.add(panel);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred while setting the images", e);
        }
    }
    
    public boolean hasObject() {
        return dco != null;
    }
    
    public void refresh() {
        int caret = descriptionPane.getCaretPosition();
        setObject(dco, true);
        
        try {
            descriptionPane.setCaretPosition(caret);
        } catch (Exception e) {
            logger.debug("Error while setting the quick view caret position", e);
        }
    }
    
    public void setObject(final DcObject dco, boolean allowSame) {
        if (dco == null || (!allowSame && dco.equals(this.dco)))
            return;

        setObject(dco);
    }   
    
    protected void setObject(DcObject dco) {
        try {
            if (DcModules.getCurrent().isAbstract() &&
                DcModules.getCurrent().getCurrentSearchView().getIndex() == MasterView._TABLE_VIEW) {
                dco.reload();
                dco.loadChildren();
            }
            
            int tab = tabbedPane.getSelectedIndex();
            clear();
            this.dco = dco;
            
            String html = "<html><body " + 
                           Utilities.getHtmlStyle("", DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor)) + 
                           ">\n" +
                            getDescriptionTable(dco) +
                          "</body> </html>";
            
            descriptionPane.setHtml(html);
            descriptionPane.setBackground(DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor));
            
            try {
                descriptionPane.setCaretPosition(0);
            } catch (Exception exp) {}
            
            createImageTabs();
            
            boolean error = true;
            tab += 1;
            
            // prevent endless loop
            int counter = 0;
            while (error && counter < 6) {
                counter ++;
                try {
                    tab -= 1;
                    tabbedPane.setSelectedIndex(tab);
                    error = false;
                } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            logger.error("An error occurred while setting the information of " + dco, e);
        }            
    }
    
    public void clear() {
        if (dco != null) {
            dco = null;

            try {
                tabbedPane.setSelectedIndex(0);
            } catch (Exception e) {}
            
            descriptionPane.setHtml("<html><body " + Utilities.getHtmlStyle("", DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor)) + ">\n</body> </html>");
        	clearImages();
        }
    }
    
    private void removeTabs() {
        tabbedPane.removeChangeListener(this);
        tabbedPane.removeAll();
        tabbedPane.addChangeListener(this);
    }
    
    private void clearImages() {
        removeTabs();
        tabbedPane.addTab(DcResources.getText("lblDescription"), IconLibrary._icoInformation, scroller);
        
        for (JPanel panel : imagePanels) {
            Component[] components = panel.getComponents();
            for (int i = 0; i < components.length; i++)
                ComponentFactory.clean(components[i]);
        }

        pictures.clear();
        imagePanels.clear();
    }
    
    private String getDescriptionTable(DcObject dco) {
        String table = "<table " + Utilities.getHtmlStyle(DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor)) + ">\n";
        
        QuickViewFieldDefinitions definitions = (QuickViewFieldDefinitions) 
            dco.getModule().getSettings().getDefinitions(DcRepository.ModuleSettings.stQuickViewFieldDefinitions);
        
        for (QuickViewFieldDefinition def : definitions.getDefinitions()) {
            if (dco.getField(def.getField()).isEnabled() && def.isEnabled()) 
                table = addTableRow(table, def.getField(), def.getDirectrion(), def.getMaxLength());    
        }
        
        table += "</table>";    
        
        if (dco.getModule().getChild() != null && dco.getModule().getIndex() != DcModules._USER) {
            table += "\n\n";
            table += getChildTable(dco.getModule().getChild());
        }
        
        return table;
    }
    
    private String getChildTable(DcModule module) {
        StringBuffer body = new StringBuffer();
        QuickViewFieldDefinitions definitions = 
            (QuickViewFieldDefinitions) module.getSettings().getDefinitions(DcRepository.ModuleSettings.stQuickViewFieldDefinitions);
        
        dco.removeChildren();
        dco.loadChildren();
        
        int counter = 0;
        
        Collection<DcObject> children = dco.getChildren();
        for (DcObject child : children) {
            
            if (counter > 0)
                body.append("<b> / </b>");

            boolean first = true;
            StringBuffer description = new StringBuffer();
            for (QuickViewFieldDefinition definition : definitions.getDefinitions()) {
                String value = child.getDisplayString(definition.getField());
                
                if (definition.isEnabled() && value.trim().length() > 0) {
                    
                    if (first) description.append("<b>");
                    
                    if (!first) description.append(" ");
                    
                    description.append(value);
                    
                    if (first) description.append("</b>");
                    
                    first = false;
                }
            }
            
            body.append(descriptionPane.createLink(child, description.toString()));
            
            counter++;
        }
        
        String table = "<table " + Utilities.getHtmlStyle(DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor)) + "><tr><td>\n";
        table += body;
        table += "</tr></td></table>";
        
        return table;
    }
    
    @SuppressWarnings("unchecked")
    protected String addTableRow(String htmlTable, int index, String direction, int maxLength) {
        String table = htmlTable;
        if (dco.isEnabled(index)) {
            Font font = DcSettings.getFont(DcRepository.Settings.stSystemFontNormal);
            boolean horizontal = direction.equals(_DIRECTION_HORIZONTAL);

            if (!Utilities.isEmpty(dco.getValue(index))) {
                table += "<tr><td>\n";
                table += "<b>" + dco.getLabel(index) + "</b>";
                
                if (!horizontal) {
                    table += "</td></tr>";
                    table += "<tr><td>\n";
                } else {
                    table += " ";
                }

                String value = "";
                
                // Create links
                if (dco.getField(index).getFieldType() == ComponentFactory._FILEFIELD ||
                    dco.getField(index).getFieldType() == ComponentFactory._FILELAUNCHFIELD) { 
                
                    String filename = dco.getDisplayString(index);
                    filename = filename.replaceAll(" ", "%20");
                    value = "<a " + Utilities.getHtmlStyle() + " href=\"file:///" + filename + "\">" + new File(dco.getDisplayString(index) ).getName() + "</a>";
                } else if (dco.getField(index).getFieldType() == ComponentFactory._URLFIELD) {
                	value = "<a " + Utilities.getHtmlStyle() + "  href=\"" +  dco.getValue(index) + "\">" + DcResources.getText("lblLink") + "</a>";
                } else if (dco.getField(index).getReferenceIdx() > 0 && 
                    dco.getField(index).getReferenceIdx() != dco.getModule().getIndex()) {
                    
                    if (dco.getField(index).getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                        int i = 0;
                        for (DcObject reference : (Collection<DcObject>) dco.getValue(index)) {
                            if (i > 0)
                                value += "&nbsp;/&nbsp;";
                            
                            if (reference instanceof DcMapping) 
                                reference = ((DcMapping) reference).getReferencedObject();
                            
                            if (reference == null)
                                continue;
                            
                            value += descriptionPane.createLink(reference, reference.toString());
                            i++;
                        }
                    } else {
                        Object o = dco.getValue(index);
                        DcObject reference = o instanceof DcObject ? (DcObject) o : DataManager.getObject(dco.getField(index).getReferenceIdx(), (String) o);
                        reference = reference == null && o instanceof String ? DataManager.getObjectForString(dco.getField(index).getReferenceIdx(), (String) o) : reference;
                        value += descriptionPane.createLink(reference, reference.toString());
                    }
                } else { // Add simple value
                    value = dco.getDisplayString(index);
                    
                    if (dco.getField(index).getValueType() == DcRepository.ValueTypes._STRING) {
                        value = value.replaceAll("[\r\n]", "<br>");
                        value = value.replaceAll("[\t]", "    ");
                    }
                  
                    if (maxLength > 0)
                        value = StringUtils.concatUserFriendly(value, maxLength);
                    
                    if (font.getStyle() == Font.BOLD) 
                        value = "<b>" + value + "</b>";
                    
                }
                    
                table += value;                
                table += "</td></tr>";
            } else if (dco.getField(index).getValueType() == DcRepository.ValueTypes._PICTURE) {
                Picture picture = (Picture) dco.getValue(index);
                pictures.add(picture);
            }
        }
        
        return table;
    }    
    
    private void buildPanel() {
        // description panel
        descriptionPane = ComponentFactory.getHtmlEditorPane();
        descriptionPane.addMouseListener(this);

        scroller = new JScrollPane(descriptionPane);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // tabbed pane
        tabbedPane.addTab(DcResources.getText("lblDescription"), IconLibrary._icoInformation ,scroller);
        add(tabbedPane, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets(4, 0, 3, 0), 0, 0));
        
        String html = "<html><body " + Utilities.getHtmlStyle(DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor)) + "></body> </html>";
        descriptionPane.setHtml(html);
    }    
    
    private void showPopupMenu(int x, int y) {
        PopupMenu popupMenu = new PopupMenu();
        popupMenu.show(this, x, y);
    }    
    
    public void stateChanged(ChangeEvent evt) {
        JTabbedPane pane = (JTabbedPane) evt.getSource();
        
        if (pane.getSelectedIndex() > 0)
            loadImage();
    }
    
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            showPopupMenu(e.getX(), e.getY());
        } 

        if (e.getClickCount() == 2 && dco != null) {
            dco.getModule().getCurrentSearchView().open();
        }
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    
    private static class PopupMenu extends DcPopupMenu {
        public PopupMenu() {
            PluginHelper.add(this, "ToggleQuickView");
            PluginHelper.add(this, "QuickViewSettings");
        }
    }   
}
