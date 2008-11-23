/******************************************************************************
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

package net.datacrow.console.windows.itemforms;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.Layout;
import net.datacrow.console.components.DcCheckBox;
import net.datacrow.console.components.DcFrame;
import net.datacrow.console.components.DcLongTextField;
import net.datacrow.console.components.DcPictureField;
import net.datacrow.console.components.panels.LoanPanel;
import net.datacrow.console.components.panels.RelatedItemsPanel;
import net.datacrow.console.windows.ItemTypeDialog;
import net.datacrow.console.windows.loan.LoanInformationPanel;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.console.windows.messageboxes.QuestionBox;
import net.datacrow.core.DcRepository;
import net.datacrow.core.IconLibrary;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.modules.IChildModule;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcMapping;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.DcTemplate;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.ValidationException;
import net.datacrow.core.plugin.PluginHelper;
import net.datacrow.core.resources.DcResources;
import net.datacrow.core.security.SecurityCentre;
import net.datacrow.core.wf.requests.CloseWindowRequest;
import net.datacrow.fileimporters.FileImporter;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.util.Base64;
import net.datacrow.util.DcImageIcon;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class ItemForm extends DcFrame implements ActionListener {

    private static Logger logger = Logger.getLogger(ItemForm.class.getName());
    
    // the item we will be working on
    protected DcObject dco;
    // the original item (for comparison of entered value; workaround for failed save issue)
    protected DcObject dcoOrig;
    protected final int moduleIndex;

    protected Map<DcField, JLabel> labels = new HashMap<DcField, JLabel>();
    protected Map<DcField, JComponent> fields = new HashMap<DcField, JComponent>();

    protected final boolean update;
    protected final boolean readonly;

    private boolean applyTemplate = true;

    protected JTabbedPane tabbedPane = ComponentFactory.getTabbedPane();
    private DcMinimalisticItemView childView;
    
    private DcTemplate template;
    
    public ItemForm(
            boolean readonly,
            boolean update,
            DcObject o,
            boolean applyTemplate) {
        this(null, readonly, update, o, applyTemplate);
    }
    
    
    public ItemForm(    DcTemplate template,
                        boolean readonly,
                        boolean update,
                        DcObject o,
                        boolean applyTemplate) {

        super("", o.getModule().getIcon16());
        this.applyTemplate =  applyTemplate && !update;
        
        setHelpIndex("dc.items.itemform");
        
        this.template = template;
        this.readonly = readonly;
        this.update = update;
        this.dcoOrig = o;
        
        this.dcoOrig.setPartOfBatch(false);

        if (!update && !readonly && o.getModule().isAbstract()) {
            ItemTypeDialog dialog = new ItemTypeDialog(this);
            dialog.setVisible(true);
            this.moduleIndex = dialog.getSelectedModule();
            
            if (moduleIndex == -1)
                return;
            
            String parentID = dcoOrig.getParentID();
            
            this.dcoOrig = DcModules.get(moduleIndex).getDcObject();
            this.dco = DcModules.get(moduleIndex).getDcObject();
            
            if (DcModules.getCurrent().getIndex() == DcModules._CONTAINER) {
                if (this.template == null) 
                    this.template = (DcTemplate) dco.getModule().getTemplateModule().getDcObject();
                
                this.template.setValue(DcObject._SYS_CONTAINER,
                                       DcModules.getCurrent().getCurrentSearchView().getSelectedItem());
            }
            
            if (!Utilities.isEmpty(parentID))
                dco.setValue(dco.getParentReferenceFieldIndex(), parentID);
            
        } else {
            this.dco = o.clone();
            this.moduleIndex = dco.getModule().getIndex();
        }
        
        setTitle(!update && !readonly ?
                  DcResources.getText("lblNewItem", dco.getModule().getObjectName()) :
                  dco.getName());

        JMenuBar mb = getDcMenuBar();
        if (mb != null) setJMenuBar(mb);
        
        setResizable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.getContentPane().setLayout(Layout.getGBL());

        initializeComponents();
        final DcModule module = DcModules.get(moduleIndex);
        JPanel panelActions = getActionPanel(module, readonly);

        addInputPanel();
        addChildrenPanel();
        addPictureTabs();
        addRelationPanel();
        
        if (	module.canBeLend() && 
                SecurityCentre.getInstance().getUser().isAuthorized("Loan") && 
        		update && 
        		!readonly)
            addLoanTab();

        getContentPane().add(tabbedPane,  Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                             new Insets(0,0,15,0), 0, 0));
        getContentPane().add(panelActions,  Layout.getGBC(0, 1, 1, 1, 0.0, 0.0
                            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                             new Insets(5, 5, 5, 5), 0, 0));

        setRequiredFields();
        setReadonly(readonly);
        setData(dco, true);

        pack();
        
        applySettings();
        setCenteredLocation();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent we) {
                try {
                    for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
                        int index = definition.getIndex();
                        DcField field = dco.getField(index);
                        JComponent component = fields.get(field);
    
                        if (   !field.isTechnicalInfo() && 
                                component instanceof JTextField && 
                                field.isEnabled() && 
                                component.isShowing()) {
                            
                            component.requestFocusInWindow();
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }); 
    }

    private JMenuBar getDcMenuBar() {
        JMenuBar mb = null;
        
        FileImporter importer = DcModules.get(moduleIndex).getImporter();
        if (    importer != null && importer.allowReparsing() && 
                DcModules.get(moduleIndex).getDcObject().getFileField() != null) {

            mb = ComponentFactory.getMenuBar();
            JMenu menuEdit = ComponentFactory.getMenu(DcResources.getText("lblFile"));

            PluginHelper.add(menuEdit, "AttachFileInfo");
            menuEdit.addSeparator();
            PluginHelper.add(menuEdit, "CloseWindow");
            
            mb.add(menuEdit);
        }
        
        return mb;
    }
    
    public void hide(DcField field) {
        labels.get(field).setVisible(false);
        fields.get(field).setVisible(false);

        if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
            
            for (int i = tabbedPane.getTabCount() - 1; i > 0; i--) {
                String title = tabbedPane.getTitleAt(i);
                if (title.equals(field.getLabel()))
                    tabbedPane.removeTabAt(i);
            }

        }
        
        repaint();
    }
    
    protected void applySettings() {
        setSize(DcModules.get(moduleIndex).getSettings().getDimension(DcRepository.ModuleSettings.stItemFormSize));
    }
    
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    @Override
    public void close() {
        close(false);
    }
    
    /**
     * Closes this form by setting the visibility to false
     */
    public void close(boolean aftersave) {
        if (!aftersave && update && dco != null && (!readonly && isChanged())) {
            QuestionBox qb = new QuestionBox(DcResources.getText("msgNotSaved"));
        	if (qb.isAffirmative()) {
        		saveValues();
                return;
            }
        }

    	saveSettings();
    	
    	if (labels != null)
    	    labels.clear();
    	
    	if (fields != null)
    	    fields.clear();
    	
    	if (childView != null)
    	    childView.clear();
    	
        childView = null;
        template = null;  
        
        if (dcoOrig != null && update) {
            dcoOrig.freeResources();
            // repaint the image as an image tab might have been selected
            if (dcoOrig.getModule().getCurrentSearchView() != null)
                dcoOrig.getModule().getCurrentSearchView().repaintQuickViewImage();
        }
        
        if (dco != null)
            dco.freeResources();
        
        ComponentFactory.clean(getJMenuBar());
        setJMenuBar(null);
        
        dco = null;
        dcoOrig = null;
        fields = null;
        labels = null;
        tabbedPane = null;
        
        super.close();
    }
    
    protected void saveSettings() {
        DcModules.get(moduleIndex).setSetting(DcRepository.ModuleSettings.stItemFormSize, getSize());        
    }

    public void setData(DcObject object, boolean overwrite) {
        try {
            dco.applyEnhancers(update);
            
            if (applyTemplate && !(dco instanceof DcTemplate))
                dco.applyTemplate(template);  
            
            if (childView != null) {
                if (update)
                    childView.loadItems();
                else if (object.getChildren() != null)
                    childView.setObjects(object.getChildren());
            }
    
            int[] indices = object.getFieldIndices();
            for (int i = 0; i < indices.length; i++) {
                int index = indices[i];
                
                DcField field = dco.getField(index);
                JComponent component = fields.get(field);
                Object oldValue = ComponentFactory.getValue(component);
                Object newValue = object.getValue(index);
    
                if (newValue instanceof Picture)
                    ((Picture) newValue).loadImage();
    
                boolean empty = Utilities.getComparableString(oldValue).length() == 0;
                if ((empty || overwrite) && !Utilities.isEmpty(newValue))
                     ComponentFactory.setValue(component, newValue);
            }
        } catch (Exception e) {
            logger.error("Error while setting values of [" + dco + "] on the item form", e);
        }
    }

    private void setReadonly(boolean readonly) {
        if (readonly) {
            for (JComponent component : fields.values())
                ComponentFactory.setUneditable(component);
        }
    }

    private void setRequiredFields() {
        int[] fields = dco.getFieldIndices();
        for (int i = 0; i < fields.length; i++) {
            DcField field = dco.getField(fields[i]);
            if (field.isRequired()) {
                JLabel label = labels.get(field);
                label.setForeground(ComponentFactory.getRequiredColor());
            }
        }
    }

    protected void deleteValues() {
        QuestionBox qb = new QuestionBox(DcResources.getText("msgDeleteQuestion"), this);

        if (qb.isAffirmative()) {
            String id = dco.getID();
            dco.clearValues();
            dco.setValue(DcObject._ID, id);
            dco.setSilent(true);

            dco.addRequest(new CloseWindowRequest(this));
            dco.delete();
        }
    }

    @SuppressWarnings("unchecked")
    protected boolean isChanged() {
        boolean changed = dcoOrig.isChanged();

        int[] indices = dcoOrig.getFieldIndices();
        for (int i = 0; i < indices.length && !changed; i++) {
            int index = indices[i];
            if (index == DcObject._ID)
                continue;
            
            DcField field = dcoOrig.getField(index);
            JComponent component = fields.get(field);
            Object o = ComponentFactory.getValue(component);
            
            if (index == DcObject._SYS_CREATED || index == DcObject._SYS_MODIFIED)
            	continue;

            if (field.getValueType() == DcRepository.ValueTypes._ICON) {
                byte[] newValue = o == null ? new byte[0] : ((DcImageIcon) o).getBytes();
                
                Object oOld = dcoOrig.getValue(index);
                byte[] oldValue;
                if (oOld instanceof String)
                    oldValue = Base64.decode(((String) oOld).toCharArray());
                else 
                    oldValue = (byte[]) oOld;

                oldValue = oldValue == null ? new byte[0] : oldValue;
                if (!Utilities.sameImage(newValue, oldValue)) {
                    dcoOrig.setChanged(DcObject._ID, true);
                    logger.debug("Field " + field.getLabel() + " is changed. Old: " + oldValue + ". New: " + newValue);
                    changed = true;
                }
            } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                List<DcMapping> oldList = dcoOrig.getValue(index) instanceof List ? (List<DcMapping>) dcoOrig.getValue(index) : null;
                List<DcMapping> newList = (List<DcMapping>) o;
                
                oldList = oldList == null ? new ArrayList<DcMapping>() : oldList;
                newList = newList == null ? new ArrayList<DcMapping>() : newList;
                
                if (oldList.size() == newList.size()) {
                    for (DcMapping newMapping : newList) {
                        boolean found = false;
                        for (DcMapping oldMapping : oldList) {
                            if (oldMapping.getReferencedId().equals(newMapping.getReferencedId()))
                                found = true;
                        }
                        changed = !found;
                        if (changed) logger.debug("Field " + field.getLabel() + " is changed. Old: " + oldList + ". New: " + newList);
                    }
                } else {
                    changed = true;
                    logger.debug("Field " + field.getLabel() + " is changed. Old: " + oldList + ". New: " + newList);
                }
            } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION || 
                      (!field.isUiOnly() && field.getValueType() != DcRepository.ValueTypes._PICTURE)) {
                
                if (field.getValueType() == DcRepository.ValueTypes._DATE) {
                    Date dateOld = (Date) dcoOrig.getValue(index);
                    Date dateNew = (Date) o;
                    
                    if (   (dateOld == null && dateNew != null) || (dateNew == null && dateOld != null) ||
                           (dateOld != null && dateNew != null && dateOld.compareTo(dateNew) != 0)) {
                        changed = true;
                        logger.debug("Field " + field.getLabel() + " is changed. Old: " + dateOld + ". New: " + dateNew);
                    }
                } else if (field.getFieldType() == ComponentFactory._FILELAUNCHFIELD) {
                    String newValue = Utilities.getComparableString(o);
                    String oldValue = Utilities.getComparableString(dcoOrig.getValue(index));
                    changed = !oldValue.equals(newValue);
                    if (changed) logger.debug("Field " + field.getLabel() + " is changed. Old: " + oldValue + ". New: " + newValue);
                } else {
                    String newValue = Utilities.getComparableString(o);
                    String oldValue = Utilities.getComparableString(dcoOrig.getValue(index));
                    changed = !oldValue.equals(newValue);
                    if (changed) logger.debug("Field " + field.getLabel() + " is changed. Old: " + oldValue + ". New: " + newValue);
                } 
            } else if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                Picture picture = (Picture) dcoOrig.getValue(index);
                changed = (picture != null && (picture.isUpdated() || picture.isNew() || picture.isDeleted())) ||
                		  ((DcPictureField) component).isChanged();
                if (changed) logger.debug("Picture " + field.getLabel() + " is changed.");
            }
            
            if (changed)
                break;
        }

        return changed;
    }
    
    public DcObject getItem() {
        apply();
        return dco;
    }

    public void apply() {
        dco.removeRequests();
        dco.removeChildren();
        if (DcModules.get(moduleIndex).getChild() != null && childView != null) {
            for (DcObject child : childView.getItems())
                dco.addChild(child);
        }

        if (update) dco.markAsUnchanged();
        
        for (DcField field : fields.keySet()) {
            JComponent component = fields.get(field);
            Object value = ComponentFactory.getValue(component);
            value = value == null ? "" : value;

            if (update) {
                Object oldValue = dcoOrig.getValue(field.getIndex()) == null ? "" : dcoOrig.getValue(field.getIndex());
                if (dcoOrig.isChanged(field.getIndex())) {
                    dco.setValue(field.getIndex(), value);
                    if (field.getValueType() == DcRepository.ValueTypes._PICTURE)
                        dco.setChanged(DcObject._ID, true);
                } else if (oldValue == null || !oldValue.equals(value)) {
                    dco.setValue(field.getIndex(), value);
                    if (field.getValueType() == DcRepository.ValueTypes._PICTURE)
                        dco.setChanged(DcObject._ID, true);
                }
            } else if (isChanged()) {
                
                // check if the value has been cleared manually 
                // do not save if the value is empty, however do clear it when it was not empty before 
                // (online search bug, values of added items could not be removed).
                if (!value.equals("") || !Utilities.isEmpty(dco.getValue(field.getIndex())))
                    dco.setValue(field.getIndex(), value);
            }
        }

        if (update && dco.getModule().canBeLend())
            dco.setLoanInformation();
    }

    private void onlineSearch() {
    	apply();
        DcModules.get(moduleIndex).getOnlineServices().getUI(dco, this, true).setVisible(true);
    }
    
    protected void saveValues() {
        apply();
        
        dco.addRequest(new CloseWindowRequest(this));
        dco.setSilent(true);
        dco.setPartOfBatch(false);
        
        try {
            if (!update)
                dco.saveNew(true);
            else if (isChanged())
                dco.saveUpdate(true);
            else
            	new MessageBox(DcResources.getText("msgNoChangesToSave"), MessageBox._WARNING);
        } catch (ValidationException vExp) {
            new MessageBox(vExp.getMessage(), MessageBox._WARNING);
        }
    }

    private void initializeComponents() {
        int[] indices = dco.getFieldIndices();
        for (int i = 0; i < indices.length; i ++) {
            int index = indices[i];
            DcField field = dco.getField(index);

            labels.put(field, ComponentFactory.getLabel(dco.getLabel(index)));
            if (index == DcObject._ID) {
                fields.put(field, ComponentFactory.getIdFieldDisabled());
            } else {
                JComponent c = ComponentFactory.getComponent(field.getModule(),
                                                             field.getReferenceIdx(),
                                                             field.getFieldType(),
                                                             field.getLabel(),
                                                             field.getMaximumLength());
                fields.put(field, c);
            }
        }
    }

    protected void addInputPanel() {
        JPanel panelInfo = new JPanel();
        JPanel panelTech = new JPanel();
        panelInfo.setLayout(Layout.getGBL());
        panelTech.setLayout(Layout.getGBL());

        DcModule module = DcModules.get(moduleIndex);

        int yTech = 0;
        int yInfo = 0;
        boolean technicalInfoPresent = false;
        
        for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
            int index = definition.getIndex();

            DcField field = dco.getField(index);
            JLabel label = labels.get(field);
            JComponent component = fields.get(field);
            
        	if ((!field.isUiOnly() || field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) && 
        	      field.isEnabled() && 
                  field.getValueType() != DcRepository.ValueTypes._PICTURE && // check the field type
                  field.getValueType() != DcRepository.ValueTypes._ICON &&
                 (index != dco.getParentReferenceFieldIndex() || 
                  index == DcObject._SYS_CONTAINER )) { // not a reference field

                int stretch = GridBagConstraints.HORIZONTAL;
                int factor = 1;

                if (field.getFieldType() == ComponentFactory._LONGTEXTFIELD) {
                    stretch = GridBagConstraints.BOTH;
                    factor = 50;

                    DcLongTextField longText = (DcLongTextField) component;
                    longText.setMargin(new Insets(1, 1, 1, 5));

                    if (field.isReadOnly()) 
                        ComponentFactory.setUneditable(longText);
                    
                    JScrollPane pane = new JScrollPane(longText);
                    
                    ComponentFactory.setBorder(pane);
                    pane.setPreferredSize(new Dimension(100,100));
                    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                    pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                    component = pane;
                } else {
                	component.setPreferredSize(new Dimension(300, ComponentFactory.getPreferredFieldHeight()));
                }
                
                if (field.getFieldType() == ComponentFactory._REFERENCESFIELD) {
                    stretch = GridBagConstraints.BOTH;
                    factor = 10;
                }
                
                if (component instanceof DcCheckBox)
                    ((DcCheckBox) component).setText("");

                label.setPreferredSize(new Dimension(100, ComponentFactory.getPreferredFieldHeight()));
                if (!field.isTechnicalInfo()) {
                	int ySpace = yInfo == 0 ? 5 : 0; 
                	
                    panelInfo.add(label, Layout.getGBC(0, yInfo, 1, 1, 1.0, 1.0
                             ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                              new Insets(ySpace, 2, 0, 5), 0, 0));
                    panelInfo.add(component,  Layout.getGBC(1, yInfo, 1, 1, factor, factor
                             ,GridBagConstraints.NORTHWEST, stretch,
                              new Insets(ySpace, 0, 0, 2), 0, 0));
                    yInfo++;
                } else {
                	int ySpace = yTech == 0 ? 5 : 0; 
                	
                    technicalInfoPresent = true;
                    panelTech.add(label, Layout.getGBC(0, yTech, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                             new Insets(ySpace, 2, 0, 5), 0, 0));
                    panelTech.add(component,  Layout.getGBC(1, yTech, 1, 1, 50, 50
                            ,GridBagConstraints.NORTHWEST, stretch,
                             new Insets(ySpace, 0, 0, 2), 0, 0));
                    yTech++;
                }
                
                if (field.isReadOnly())
                    ComponentFactory.setUneditable(component);
            }
        }
        
        tabbedPane.addTab(DcResources.getText("lblDescription"), IconLibrary._icoInformation, panelInfo);
        if (technicalInfoPresent) {
            JPanel dummy = new JPanel();
            dummy.setLayout(Layout.getGBL());
            dummy.add(panelTech,  Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                      ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                      new Insets(2, 0, 0, 0), 0, 0));
            tabbedPane.addTab(DcResources.getText("lblTechnicalInfo"), IconLibrary._icoInformationTechnical, dummy);
        }
    }

    protected void addChildrenPanel() {
        DcModule module = DcModules.get(moduleIndex);
        DcModule childModule = module.getChild();
        
        if (childModule != null) {
            IChildModule m = (IChildModule) childModule;
            childView = m.getItemView(dco, childModule.getIndex(), !update);
            childView.hideDialogActions(true);
            tabbedPane.addTab(childModule.getObjectNamePlural(), childModule.getIcon16(), childView.getContentPane());
        }
    }
    
    protected void addRelationPanel() {
    	if (update) {
	        if (DcModules.getActualReferencingModules(moduleIndex).size() > 0 &&
	            moduleIndex != DcModules._CONTACTPERSON &&
	            moduleIndex != DcModules._CONTAINER) {
	        	
	            RelatedItemsPanel rip = new RelatedItemsPanel(dco);
	            tabbedPane.addTab(rip.getTitle(), rip.getIcon(),  rip);
	        }
	        
	        if (moduleIndex == DcModules._CONTACTPERSON) {
	        	LoanInformationPanel panel = new LoanInformationPanel(dco);
	            tabbedPane.addTab(panel.getTitle(), panel.getIcon(),  panel);
	        }
    	}
    }

    protected void addLoanTab() {
        tabbedPane.addTab(DcResources.getText("lblLoan"), IconLibrary._icoLoan, new LoanPanel(dco, null));
    }

    protected void addPictureTabs() {
        DcModule module = DcModules.get(moduleIndex);

        for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
            int index = definition.getIndex();
            DcField field = dco.getField(index);
            JComponent component = fields.get(field);

            if (field.isEnabled() &&
               (field.getValueType() == DcRepository.ValueTypes._PICTURE || 
                field.getValueType() == DcRepository.ValueTypes._ICON)) {

                JPanel panel = new JPanel();
                panel.setLayout(Layout.getGBL());

                component.setPreferredSize(component.getMinimumSize());
                panel.add(component, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                         ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                          new Insets(5, 5, 5, 5), 0, 0));

                if (field.isReadOnly())
                    ComponentFactory.setUneditable(component);
                
                tabbedPane.addTab(field.getLabel(), IconLibrary._icoPicture, panel);
            }
        }
    }

    public JPanel getActionPanel(DcModule module, boolean readonly) {
        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());

        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        JButton buttonDelete = ComponentFactory.getButton(DcResources.getText("lblDelete"));
        JButton buttonInternet = null;

        JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        buttonSave.setMnemonic(KeyEvent.VK_S);
        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");

        buttonClose.setMnemonic(KeyEvent.VK_C);
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        if (   !readonly && (module.deliversOnlineService()) && 
                SecurityCentre.getInstance().getUser().isAuthorized("OnlineSearch") &&
                SecurityCentre.getInstance().getUser().isEditingAllowed(module)) {
        	
            buttonInternet = ComponentFactory.getButton(DcResources.getText("lblOnlineUpdate"));
            buttonInternet.setIcon(IconLibrary._icoSearchOnline);
            buttonInternet.setMnemonic(KeyEvent.VK_U);
            buttonInternet.addActionListener(this);
            buttonInternet.setActionCommand("onlineSearch");
            panel.add(buttonInternet, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                     ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                      new Insets(5, 5, 5, 5), 0, 0));
        }
        
        if (!readonly && SecurityCentre.getInstance().getUser().isEditingAllowed(module)) {
            panel.add(buttonSave, Layout.getGBC(1, 0, 1, 1, 1.0, 1.0
                 ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                  new Insets(5, 5, 5, 5), 0, 0));
        }

        if (SecurityCentre.getInstance().getUser().isAdmin() && update && !readonly) {
            buttonDelete.setMnemonic(KeyEvent.VK_D);
            buttonDelete.addActionListener(this);
            buttonDelete.setActionCommand("delete");
            panel.add(buttonDelete, Layout.getGBC(2, 0, 1, 1, 1.0, 1.0
                     ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                      new Insets(5, 5, 5, 5), 0, 0));
        }

        panel.add(buttonClose , Layout.getGBC(3, 0, 1, 1, 1.0, 1.0
                 ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                  new Insets(5, 5, 5, 5), 0, 0));

        return panel;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("save"))
            saveValues();
        else if (e.getActionCommand().equals("delete"))
            deleteValues();
        else if (e.getActionCommand().equals("close"))
            close(false);
        else if (e.getActionCommand().equals("onlineSearch"))
            onlineSearch();
    }
}
