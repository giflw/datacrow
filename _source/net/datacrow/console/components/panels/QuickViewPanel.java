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
import java.util.ArrayList;
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
import net.datacrow.console.menu.DcEditorPopupMenu;
import net.datacrow.core.DataCrow;
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
    
	private DcObject dco;
    
    private String key;
    private int module;
    
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
    
    public void reloadImage() {
        if (tabbedPane.getSelectedIndex() > 0)
            loadImage();
    }
    
    private void loadImage() {
        int index = tabbedPane.getSelectedIndex() - 1;
        Picture picture = pictures.get(index);
        picture.loadImage(false);
        
        JPanel panel = imagePanels.get(index);
        Component[] components =  panel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof DcPictureField)
                ((DcPictureField) components[i]).setValue(picture.getValue(Picture._D_IMAGE));
        }
    }
    
    public void createImageTabs(DcObject dco) {
        try {
            clearImages();
    
            Picture picture;
            DcPictureField picField;
            JPanel panel;

//            QuickViewFieldDefinitions definitions = (QuickViewFieldDefinitions) 
//            dco.getModule().getSettings().getDefinitions(DcRepository.ModuleSettings.stQuickViewFieldDefinitions);
        
            // TODO: rethink this
            Collection<Integer> pictureDescFields = new ArrayList<Integer>();
//            for (QuickViewFieldDefinition def : definitions.getDefinitions())
//            	if (dco.getField(def.getField()).getValueType() == DcRepository.ValueTypes._PICTURE && def.isEnabled())
//            		pictureDescFields.add(def.getField());
            
            for (DcFieldDefinition definition : dco.getModule().getFieldDefinitions().getDefinitions()) {
                if (    dco.isEnabled(definition.getIndex()) && 
                        dco.getField(definition.getIndex()).getValueType() == DcRepository.ValueTypes._PICTURE &&
                        !pictureDescFields.contains(definition.getIndex())) {
                    
                    picture = (Picture) dco.getValue(definition.getIndex());

                    if (picture == null) continue;
                    
                    if (picture.hasImage()) {
                    	pictures.add(picture);    

                    	picField = ComponentFactory.getPictureField(true, false);
                    	ComponentFactory.setBorder(this);
                        
                        panel = new JPanel();
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
    
    public void refresh() {
        int caret = descriptionPane.getCaretPosition();
        dco = null;
        setObject(key, module);
        try {
            descriptionPane.setCaretPosition(caret);
        } catch (Exception e) {
            logger.debug("Error while setting the quick view caret position", e);
        }
    }
    
    public void setObject(String key, int module) {
    	
    	if (dco != null && dco.getID().equals(key))
    		return;
    	
        if (key != null) {
            Collection<Integer> fields = new ArrayList<Integer>();
            QuickViewFieldDefinitions definitions = 
                (QuickViewFieldDefinitions) DcModules.get(module).getSettings().getDefinitions(DcRepository.ModuleSettings.stQuickViewFieldDefinitions);
            
            for (QuickViewFieldDefinition def : definitions.getDefinitions())
                fields.add(def.getField());
            
            setObject(DataManager.getItem(module, key, DcModules.get(module).getMinimalFields(fields)));
        }
    }   
    
    protected void setObject(DcObject dco) {
        try {
            int tab = tabbedPane.getSelectedIndex();
            module = dco.getModule().getIndex();
            clear();
            
            this.dco = dco;
            this.key = dco.getID();
            
            if (DcModules.getCurrent().isAbstract())
            	this.dco.reload(dco.getModule().getMinimalFields(null));
            
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

            createImageTabs(dco);
            
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
        dco = null;
        tabbedPane.setSelectedIndex(0);
        descriptionPane.setHtml("<html><body " + Utilities.getHtmlStyle("", DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor)) + ">\n</body> </html>");
    	clearImages();
    }
    
    private void removeTabs() {
        tabbedPane.removeChangeListener(this);
        tabbedPane.removeAll();
        tabbedPane.addChangeListener(this);
    }
    
    private void clearImages() {
        removeTabs();
        tabbedPane.addTab(DcResources.getText("lblDescription"), IconLibrary._icoInformation, scroller);
        
        Component[] components;
        for (JPanel panel : imagePanels) {
            components = panel.getComponents();
            for (int i = 0; i < components.length; i++)
                ComponentFactory.clean(components[i]);
        }

        pictures.clear();
        imagePanels.clear();
    }
    
    private String getDescriptionTable(DcObject dco) {
        String table = "<h3>" + dco.toString() + "</h3>";
        
        table += "<table " + Utilities.getHtmlStyle(DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor)) + ">\n";
        
        QuickViewFieldDefinitions definitions = (QuickViewFieldDefinitions) 
            dco.getModule().getSettings().getDefinitions(DcRepository.ModuleSettings.stQuickViewFieldDefinitions);
        
        for (QuickViewFieldDefinition def : definitions.getDefinitions()) {
            if (dco.getField(def.getField()).isEnabled() && def.isEnabled()) 
                table = addTableRow(dco, table, def.getField(), def.getDirectrion(), def.getMaxLength());    
        }
        
        table += "</table>";    
        
        if (dco.getModule().getChild() != null && dco.getModule().getIndex() != DcModules._USER) {
            table += "\n\n";
            table += getChildTable(dco);
        }
        
        return table;
    }
    
    private String getChildTable(DcObject dco) {
        DcModule module = dco.getModule().getChild();
        Collection<DcObject> children = dco.getChildren();
        
        if (children == null || children.size() == 0)
            return "";
        
        String table = "<br><h3>" + module.getObjectNamePlural() + "</h3>";
        
        table += "<table " + Utilities.getHtmlStyle(DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor)) + ">\n";

        QuickViewFieldDefinitions definitions = 
            (QuickViewFieldDefinitions) module.getSettings().getDefinitions(DcRepository.ModuleSettings.stQuickViewFieldDefinitions);
        
        boolean first;
        StringBuffer description;
        String value;
        
        for (DcObject child : children) {
        	
        	if (dco.getModule().getIndex() == DcModules._CONTAINER)
        		child.load(child.getModule().getMinimalFields(null));
            
            table += "<tr><td>";

            first = true;
            description = new StringBuffer();
            for (QuickViewFieldDefinition definition : definitions.getDefinitions()) {
                value = child.getDisplayString(definition.getField());
                
                if (definition.isEnabled() && value.trim().length() > 0) {
                    
                    if (first) description.append("<b>");
                    
                    if (!first) description.append(" ");
                    
                    description.append(value);
                    
                    if (first) description.append("</b>");
                    
                    first = false;
                }
            }
            
            table += descriptionPane.createLink(child, description.toString());
            table += "</td></tr>";
        }
        table += "</table>";
        return table;
    }
    
    @SuppressWarnings("unchecked")
    protected String addTableRow(DcObject dco, String htmlTable, int index, String direction, int maxLength) {
        
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
                } else if (dco.getField(index).getFieldType() == ComponentFactory._PICTUREFIELD) {
                	Picture p = (Picture) dco.getValue(index);
                	value = "<p><img src=\"file:///" + DataCrow.imageDir + "/" + p.getScaledFilename() + "\"></p><br>";
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
                        DcObject reference = o instanceof DcObject ? (DcObject) o : DataManager.getItem(dco.getField(index).getReferenceIdx(), (String) o);
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
    	tabbedPane.setFont(DcSettings.getFont(DcRepository.Settings.stSystemFontBold));
    	
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
        
        if (descriptionPane.isShowing())
            popupMenu.show(descriptionPane, x, y);
        else 
            popupMenu.show(this, x, y);
    }    
    
    @Override
    public void stateChanged(ChangeEvent evt) {
        JTabbedPane pane = (JTabbedPane) evt.getSource();
        
        if (pane.getSelectedIndex() > 0)
            loadImage();
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e))
            showPopupMenu(e.getX(), e.getY());

        if (e.getClickCount() == 2 && dco != null)
            DcModules.get(module).getCurrentSearchView().open();
    }

    @Override
	public void setFont(Font font) {
		super.setFont(font);
		
		if (tabbedPane != null)
			tabbedPane.setFont(font);
	}

	@Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    
    private class PopupMenu extends DcEditorPopupMenu {
        public PopupMenu() {
            super(descriptionPane);
            
            addSeparator();

            if (!descriptionPane.isShowing())
                removeAll();
            
            PluginHelper.add(this, "ToggleQuickView");
            PluginHelper.add(this, "QuickViewSettings");
        }
    }   
}
