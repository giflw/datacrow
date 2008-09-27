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

package net.datacrow.console.components.tables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import net.datacrow.console.ComponentFactory;
import net.datacrow.console.components.DcLoginNameField;
import net.datacrow.console.components.DcMultiLineToolTip;
import net.datacrow.console.components.DcNumberField;
import net.datacrow.console.components.DcRatingComboBox;
import net.datacrow.console.components.DcShortTextField;
import net.datacrow.console.components.renderers.AvailabilityCheckBoxTableCellRenderer;
import net.datacrow.console.components.renderers.CheckBoxTableCellRenderer;
import net.datacrow.console.components.renderers.ComboBoxTableCellRenderer;
import net.datacrow.console.components.renderers.ContactPersonTableCellRenderer;
import net.datacrow.console.components.renderers.DcTableCellRenderer;
import net.datacrow.console.components.renderers.DcTableHeaderRenderer;
import net.datacrow.console.components.renderers.DcTableHeaderRendererRequired;
import net.datacrow.console.components.renderers.FileSizeTableCellRenderer;
import net.datacrow.console.components.renderers.ModuleTableCellRenderer;
import net.datacrow.console.components.renderers.NumberTableCellRenderer;
import net.datacrow.console.components.renderers.PictureTableCellRenderer;
import net.datacrow.console.components.renderers.RatingTableCellRenderer;
import net.datacrow.console.components.renderers.ReferencesTableCellRenderer;
import net.datacrow.console.components.renderers.TimeFieldTableCellRenderer;
import net.datacrow.console.views.IViewComponent;
import net.datacrow.console.views.View;
import net.datacrow.console.windows.messageboxes.MessageBox;
import net.datacrow.core.DcRepository;
import net.datacrow.core.data.DataManager;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.objects.Loan;
import net.datacrow.core.objects.Picture;
import net.datacrow.core.objects.helpers.Media;
import net.datacrow.core.resources.DcResources;
import net.datacrow.settings.definitions.DcFieldDefinition;
import net.datacrow.settings.definitions.DcFieldDefinitions;
import net.datacrow.util.DcSwingUtilities;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;

public class DcTable extends JTable implements IViewComponent {

    private static Logger logger = Logger.getLogger(DcTable.class.getName());

    private final DcModule module;
    private final Hashtable<String, DcObject> cache = new Hashtable<String, DcObject>();
    private final TableValueChangedAction tableChangeListener = new TableValueChangedAction();

    private final boolean caching;
    private final boolean readonly;

    private boolean ignoreSettings = false;

    private View view;

    private boolean ignoreEdit = false;

    private ArrayList<TableColumn> columnsHidden = new ArrayList<TableColumn>();
    private Map<Object, TableColumn> columns = new HashMap<Object, TableColumn>();

    public DcTable(boolean readonly, boolean caching) {
        super(new DcTableModel());

        this.readonly = readonly;

        module = null;

        setProperties();

        this.caching = caching;
    }

    public DcTable(DcModule module, boolean readonly, boolean caching) {
        super(new DcTableModel());

        this.caching = caching;
        this.module = module;
        this.readonly = readonly;

        buildTable();

        setProperties();
        applySettings();

        setListeningForChanges(true);
    }

    public int getOptimalItemAdditionBatchSize() {
        return 25;
    }

    public boolean allowsHorizontalTraversel() {
        return false;
    }

    public boolean allowsVerticalTraversel() {
        return true;
    }

    public View getView() {
        return view;
    }

    public void setIgnoreSettings(boolean b) {
        ignoreSettings = b;
    }

    public void setView(View view) {
        this.view = view;
    }

    public boolean isChangesSaved() {
        return cache.size() == 0;
    }

    public DcTableModel getDcModel() {
        if (!(getModel() instanceof DcTableModel))
            setModel(new DcTableModel());

        DcTableModel model = (DcTableModel) getModel();
        return model;
    }

    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }

    private int getFieldForColumnIndex(int columnIndex) {
        int field = -1;
        for (TableColumn column : columns.values()) {
            if (column.getModelIndex() == columnIndex) {
                Integer identifier = (Integer) column.getIdentifier();

                if (identifier != null)
                    field = identifier.intValue();
            }
        }
        return field;
    }

    public int getColumnIndexForField(int field) {
        int columnIndex = -1;
        for (TableColumn column : columns.values()) {
            Integer identifier = (Integer) column.getIdentifier();
            if (identifier != null && identifier.intValue() == field)
                columnIndex = column.getModelIndex();
        }
        return columnIndex;
    }

    public Collection<DcObject> getSelectedItems() {
        Collection<DcObject> c = new ArrayList<DcObject>();
        int[] rows = getSelectedRows();
        for (int i = 0; i < rows.length; i++)
            c.add(getItemAt(rows[i]));

        return c;
    }

    private int addRow() {
        Object[] row = new Object[module.getFieldCount()];

        DcObject dco = module.getDcObject();
        int[] fields = dco.getFieldIndices();

        for (int i = 0; i < fields.length; i++) {
            int field = fields[i];
            int col = getColumnIndexForField(field);
            row[col] = dco.getValue(field);
        }
        getDcModel().addRow(row);

        return (getRowCount() - 1);
    }

    public void add(DcObject dco) {
        add(dco, false);
    }

    public void add(DcObject dco, boolean setSelected) {
        setListeningForChanges(false);
        int[] fields = dco.getFieldIndices();

        int row = addRow();
        for (int i = 0; i < fields.length; i++) {
            int field = fields[i];
            int col = getColumnIndexForField(field);
            Object value = dco.getValue(fields[i]);
            getDcModel().setValueAt(value, row, col);
        }

        if (module.isAbstract()) {
            int col = getColumnIndexForField(Media._SYS_MODULE);
            Object value = dco.getModule();
            getDcModel().setValueAt(value, row, col);
        }

        if (setSelected)
            setSelected(getRowCount() - 1);

        setListeningForChanges(true);
    }

    public void add(Collection<? extends DcObject> objects) {
        for (DcObject dco : objects)
            add(dco, false);

        setSelected(getRowCount() - 1);
    }

    public void add(DcObject[] objects) {
        for (DcObject dco : objects)
            add(dco, false);

        setSelected(getRowCount() - 1);
    }

    public boolean isReadOnly() {
        return readonly;
    }

    public void ignoreEdit(boolean b) {
        ignoreEdit = b;
    }

    public void applyHeaders() {
        DcTableHeaderRenderer.getInstance().applySettings();

        for (Enumeration<TableColumn> e = getColumnModel().getColumns(); e
                .hasMoreElements();) {
            TableColumn column = e.nextElement();
            column.setHeaderRenderer(DcTableHeaderRenderer.getInstance());
            columns.put(column.getIdentifier(), column);
        }
    }

    public void moveRowToTop() {
        int row = getSelectedRow();

        if (row > -1) {
            int destination = 0;
            getDcModel().moveRow(row, row, destination);
            setSelected(destination);
        } else {
            new MessageBox(DcResources.getText("msgNoRowSelectedToMove"),
                    MessageBox._WARNING);
        }
    }

    public void moveRowToBottom() {
        int row = getSelectedRow();

        if (row > -1) {
            int total = getRowCount();
            if (row < total - 1) {
                int destination = total - 1;
                getDcModel().moveRow(row, row, destination);
                setSelected(destination);
            }
        } else {
            new MessageBox(DcResources.getText("msgNoRowSelectedToMove"),
                    MessageBox._WARNING);
        }
    }

    public void moveRowDown() {
        int row = getSelectedRow();

        if (row > -1) {
            int total = getRowCount();
            if (row < total - 1) {
                int destination = row + 1;
                getDcModel().moveRow(row, row, destination);
                setSelected(destination);
            }
        } else {
            new MessageBox(DcResources.getText("msgNoRowSelectedToMove"),
                    MessageBox._WARNING);
        }
    }

    public void moveRowUp() {
        int row = getSelectedRow();

        if (row > -1) {
            if (row != 0) {
                int destination = row - 1;
                getDcModel().moveRow(row, row, destination);
                setSelected(destination);
            }
        } else {
            new MessageBox(DcResources.getText("msgNoRowSelectedToMove"),
                    MessageBox._WARNING);
        }
    }

    public void removeFields(int[] fields) {
        cancelEdit();

        for (int i = 0; i < fields.length; i++) {
            int columnIndex = getColumnIndexForField(fields[i]);

            int tableIndex = convertColumnIndexToView(columnIndex);
            try {
                TableColumn column = getColumnModel().getColumn(tableIndex);
                columnsHidden.add(column);
                removeColumn(column);
            } catch (Exception ignore) {
            }
        }
    }

    public void undoChanges() {
        cache.clear();
    }

    public void removeFromCache(String sID) {
        cache.remove(sID);
    }

    public Collection<DcObject> getItems() {
        Collection<DcObject> objects = new ArrayList<DcObject>();
        for (int row = 0; row < getRowCount(); row++)
            objects.add(getItemAt(row));
        return objects;
    }

    public DcObject getItemAt(int row) {
        if (row == -1)
            return null;

        int col = getColumnIndexForField(DcObject._ID);

        Object o = getValueAt(row, col, true);
        String id = o != null ? o.toString() : null;

        if (isCached(id)) {
            return cache.get(id);
        } else {
            cancelEdit();
            DcObject dco = getModuleForRow(row).getDcObject();
            for (TableColumn column : columns.values()) {
                col = column.getModelIndex();
                int field = getFieldForColumnIndex(col);
                Object value = getValueAt(row, col, true);
                dco.setValue(field, value);
            }

            if (view != null && dco != null && view.getType() != View._TYPE_INSERT)
                dco.markAsUnchanged();

            return dco;
        }
    }

    private boolean isCached(String id) {
        return id != null && cache.containsKey(id);
    }

    public DcModule getModuleForRow(int row) {
        DcModule result = module;

        if (module.isAbstract()) {
            int col = getColumnIndexForField(Media._SYS_MODULE);
            Object value = getValueAt(row, col, true);
            if (value instanceof DcModule) {
                result = (DcModule) value;
            } else {
                String s = value.toString();
                for (DcModule mod : DcModules.getModules()) {
                    if (mod.getName().equals(s))
                        result = mod;
                }
            }
        }
        return result;
    }

    public String getObjectID(int row) {
        int col = getColumnIndexForField(DcObject._ID);
        Object o = getValueAt(row, col, true);
        return o == null ? null : o.toString();
    }

    public Collection<DcObject> getChangedObjects() {
        Collection<DcObject> objects = new ArrayList<DcObject>();
        for (String id : cache.keySet()) {
            objects.add(cache.get(id));
        }
        return objects;
    }

    public int[] getChangedIndices() {
        cancelEdit();
        int[] rows = new int[cache.size()];
        int counter = 0;
        for (String id : cache.keySet()) {
            int row = getRowNumberWithID(id);
            rows[counter++] = row;
        }
        return rows;
    }

    public Object getValueAt(int row, int col, boolean hidden) {
        Object value = null;
        try {
            if (row > -1 && col > -1)
                value = hidden ? getDcModel().getValueAt(row, col) : super
                        .getValueAt(row, col);
        } catch (Exception ignore) {
        }

        return value;
    }

    public void clear() {
        cache.clear();
        cancelEdit();

        setListeningForChanges(false);
        getDcModel().setRowCount(0);
        setListeningForChanges(true);
    }

    public int getRowNumberWithID(String ID) {
        cancelEdit();
        for (int i = 0; i < getDcModel().getRowCount(); i++) {
            if (ID.equals(getObjectID(i)))
                return i;
        }
        return -1;
    }

    public void setColumnCount(int count) {
        getDcModel().setColumnCount(count);
    }

    public void remove(int[] rows) {
        cancelEdit();
        for (int i = rows.length - 1; i > -1; i--) {
            if (caching) {
                int row = rows[i];
                int col = getColumnIndexForField(DcObject._ID);
                removeFromCache((String) getValueAt(row, col, true));
            }
            getDcModel().removeRow(rows[i]);
        }
    }

    public void removeRow(int row) {
        getDcModel().removeRow(row);
    }

    public void setRowCount(int count) {
        getDcModel().setRowCount(count);
    }

    public void deselect() {
        getSelectionModel().clearSelection();
    }

    // not implemented; this is not used for tables
    public void setSelected(Collection<? extends DcObject> items) {
    }

    // not implemented; updating the UI of a single element is not needed for
    // tables
    public void updateUI(String ID) {
    }

    public void setSelected(int row) {
        try {
            if (getSelectedRow() > -1) {
                removeColumnSelectionInterval(0, getColumnCount() - 1);
                removeRowSelectionInterval(getSelectedRow(), getSelectedRow());
            }

            getSelectionModel().setValueIsAdjusting(true);

            addRowSelectionInterval(row, row);
            addColumnSelectionInterval(0, getColumnCount() - 1);

            if (row <= getRowCount()) {
                Rectangle rect = getCellRect(row, 0, true);
                scrollRectToVisible(rect);
            }

        } catch (Exception e) {
            logger.debug(
                    "Error while trying to set the selected row in the table to "
                            + row, e);
        }
    }

    public void updateItem(String ID, DcObject dco, boolean overwrite,
            boolean allowDeletes, boolean mark) {
        int index = getIndex(ID);
        if (index > -1)
            updateItemAt(index, dco, overwrite, allowDeletes, mark);
    }

    public void updateItemAt(int row, DcObject dco, boolean overwrite,
            boolean allowDeletes, boolean mark) {
        cancelEdit();

        if (!mark) {
            setListeningForChanges(false);
            removeFromCache(dco.getID());
        }

        int[] indices = dco.getFieldIndices();

        try {
            setSelected(row);
        } catch (Exception e) {
            logger.error(
                    "Error while trying to set the selected row in the table to "
                            + row, e);
        }

        for (int i = 0; i < indices.length; i++) {
            try {

                // media module does not have all columns available for
                // specialized objects. Skip if the
                // column is not available.
                if (module != null && module.isAbstract()
                        && !columns.containsKey(indices[i]))
                    continue;

                TableColumn column = columns.get(indices[i]);
                int col = column.getModelIndex();

                Object oNew = dco.getValue(indices[i]);
                Object oOld = getDcModel().getValueAt(row, col);
                oNew = oNew instanceof Picture && ((Picture) oNew).isDeleted() ? null
                        : oNew;

                String sNewValue = Utilities.getComparableString(oNew);
                String sOldValue = Utilities.getComparableString(oOld);
                boolean isNewEmpty = sNewValue.equals("");
                boolean isOldEmpty = sOldValue.equals("");

                if (!sNewValue.equals(sOldValue)
                        && (isOldEmpty || (overwrite && (!isNewEmpty || allowDeletes))))
                    getDcModel().setValueAt(oNew, row, col);

            } catch (Exception e) {
                Integer key = indices[i];
                TableColumn column = columns.containsKey(key) ? columns
                        .get(key) : null;
                logger.error("Error while setting value for column " + column
                        + " module: " + module, e);
            }
        }

        if (module.isAbstract()) {
            int col = getColumnIndexForField(Media._SYS_MODULE);
            Object value = dco.getModule();
            getDcModel().setValueAt(value, row, col);
        }

        if (!mark)
            setListeningForChanges(true);
    }

    public void addRow(Object[] row) {
        getDcModel().addRow(row);
    }

    public void addRowToCache(int row, int column) {
        setListeningForChanges(false);

        try {
            if (row != -1 && column != -1) {
                int col = getColumnIndexForField(DcObject._ID);

                Object oID = getValueAt(row, col, true);
                String id = oID == null ? null : oID.toString();

                if (id != null && !id.equals("")) {
                    DcObject dco;
                    if (!cache.containsKey(id)) {
                        dco = getItemAt(row);

                        if (view.getType() != View._TYPE_INSERT)
                            dco.markAsUnchanged();

                        DcObject o = DataManager.getObject(module.getIndex(),
                                id);
                        if (o != null) {
                            int field = getFieldForColumnIndex(column);
                            Object valueOld = o.getValue(field);
                            Object valueNew = getDcModel().getValueAt(row,
                                    column);

                            valueOld = valueOld == null ? "" : valueOld;
                            valueNew = valueNew == null ? "" : valueNew;

                            if (valueOld.equals(valueNew))
                                return;
                        }
                    } else {
                        dco = cache.get(id);

                        int field = getFieldForColumnIndex(column);
                        Object valueOld = dco.getValue(field) == null ? ""
                                : dco.getValue(field);
                        Object valueNew = getDcModel().getValueAt(row, column);

                        if (valueOld.equals(valueNew))
                            return;
                    }

                    dco.setValue(getFieldForColumnIndex(column), getDcModel()
                            .getValueAt(row, column));
                    cache.put(id, dco);
                }
            }
        } finally {
            setListeningForChanges(true);
        }
    }

    public void cancelEdit() {
        int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            for (int i = 0; i < getColumnCount(); i++)
                try {
                    getCellEditor(selectedRow, i).stopCellEditing();
                } catch (Exception e) {
                }
        }
    }

    // *************************************************************************
    // Private methods and classes
    // *************************************************************************
    private void buildTable() {
        DcObject dco = module.getDcObject();
        getDcModel().setColumnCount(dco.getFields().size());

        int counter = 0;
        for (DcField field : dco.getFields()) {
            TableColumn columnNew = getColumnModel().getColumn(counter);
            columnNew.setIdentifier(field.getIndex());
            columnNew.setHeaderValue(field.getLabel());

            JComponent comp = ComponentFactory.getComponent(module.getIndex(),
                    field.getReferenceIdx(), field.getFieldType(), field
                            .getLabel(), field.getMaximumLength());
            comp.setAutoscrolls(false);
            comp.setBorder(null);
            comp.setIgnoreRepaint(true);
            comp.setVerifyInputWhenFocusTarget(false);

            if (field.getFieldType() == ComponentFactory._LONGTEXTFIELD
                    || field.getFieldType() == ComponentFactory._URLFIELD) {

                comp = ComponentFactory.getComponent(module.getIndex(), field
                        .getReferenceIdx(), ComponentFactory._SHORTTEXTFIELD,
                        field.getLabel(), field.getMaximumLength());
            }

            if (field.getIndex() == DcObject._ID
                    || field.getIndex() == DcObject._SYS_LENDBY
                    || field.getIndex() == DcObject._SYS_LOANDURATION
                    || field.getIndex() == DcObject._SYS_CREATED
                    || field.getIndex() == DcObject._SYS_MODIFIED) {

                DcShortTextField text = ComponentFactory.getTextFieldDisabled();
                columnNew.setCellEditor(new DefaultCellEditor(text));
                DcTableCellRenderer renderer = DcTableCellRenderer
                        .getInstance();
                renderer.setFont(ComponentFactory.getSystemFont());
                columnNew.setCellRenderer(renderer);
            } else if (field.getFieldType() == ComponentFactory._REFERENCESFIELD) {
                columnNew.setCellEditor(new DefaultCellEditor(ComponentFactory
                        .getTextFieldDisabled()));
                columnNew.setMaxWidth(100);
                columnNew.setCellRenderer(ReferencesTableCellRenderer
                        .getInstance());
            } else if (field.getIndex() == DcObject._SYS_MODULE) {
                DcShortTextField text = ComponentFactory.getTextFieldDisabled();
                columnNew.setCellEditor(new DefaultCellEditor(text));
                columnNew
                        .setCellRenderer(ModuleTableCellRenderer.getInstance());
            } else if (dco instanceof Loan
                    && field.getIndex() == Loan._C_CONTACTPERSONID) {
                DcShortTextField text = ComponentFactory.getTextFieldDisabled();
                columnNew.setCellEditor(new DefaultCellEditor(text));
                columnNew.setCellRenderer(ContactPersonTableCellRenderer
                        .getInstance());
            } else {
                switch (field.getFieldType()) {
                case ComponentFactory._AVAILABILITYCOMBO:
                    columnNew.setCellEditor(new DefaultCellEditor(
                            ComponentFactory.getTextFieldDisabled()));
                    columnNew
                            .setCellRenderer(AvailabilityCheckBoxTableCellRenderer
                                    .getInstance());
                    break;
                case ComponentFactory._CHECKBOX:
                    columnNew.setCellEditor(new DefaultCellEditor(
                            ComponentFactory.getTextFieldDisabled()));
                    columnNew.setCellRenderer(CheckBoxTableCellRenderer
                            .getInstance());
                    break;
                case ComponentFactory._FILESIZEFIELD:
                    columnNew.setCellEditor(new DefaultCellEditor(
                            (JTextField) comp));
                    columnNew.setMaxWidth(100);
                    columnNew.setCellRenderer(FileSizeTableCellRenderer
                            .getInstance());
                    break;
                case ComponentFactory._NUMBERFIELD:
                case ComponentFactory._DECIMALFIELD:
                    columnNew.setCellEditor(new DefaultCellEditor(
                            (JTextField) comp));
                    columnNew.setMaxWidth(100);
                    columnNew.setCellRenderer(NumberTableCellRenderer
                            .getInstance());
                    break;
                case ComponentFactory._LONGTEXTFIELD:
                case ComponentFactory._SHORTTEXTFIELD:
                    columnNew.setCellEditor(new DefaultCellEditor(
                            (JTextField) comp));
                    break;
                case ComponentFactory._TIMEFIELD:
                    DcNumberField numberField = ComponentFactory
                            .getNumberField();
                    columnNew.setCellEditor(new DefaultCellEditor(numberField));
                    columnNew.setCellRenderer(TimeFieldTableCellRenderer
                            .getInstance());
                    break;
                case ComponentFactory._URLFIELD:
                    columnNew.setCellEditor(new DefaultCellEditor(
                            (JTextField) comp));
                    DcTableCellRenderer renderer = DcTableCellRenderer
                            .getInstance();
                    renderer.setForeground(new Color(0, 0, 255));
                    columnNew.setCellRenderer(renderer);
                    break;
                case ComponentFactory._PICTUREFIELD:
                    DcShortTextField text = ComponentFactory
                            .getTextFieldDisabled();
                    text.setEditable(false);
                    text.setFont(ComponentFactory.getUnreadableFont());
                    columnNew.setCellEditor(new DefaultCellEditor(text));
                    columnNew.setCellRenderer(PictureTableCellRenderer
                            .getInstance());
                    break;
                case ComponentFactory._REFERENCEFIELD:
                    columnNew.setCellRenderer(ComboBoxTableCellRenderer
                            .getInstance());
                    columnNew.setCellEditor(new DefaultCellEditor(
                            (JComboBox) comp));
                    break;
                case ComponentFactory._RATINGCOMBOBOX:
                    columnNew.setMinWidth(70);
                    columnNew.setCellRenderer(RatingTableCellRenderer
                            .getInstance());
                    columnNew.setCellEditor(new DefaultCellEditor(
                            (DcRatingComboBox) comp));
                    break;
                case ComponentFactory._YESNOCOMBO:
                    columnNew.setCellEditor(new DefaultCellEditor(
                            (JComboBox) comp));
                    break;
                case ComponentFactory._LOGINNAMEFIELD:
                    columnNew.setCellEditor(new DefaultCellEditor(
                            (DcLoginNameField) comp));
                    break;
                }

                comp.setEnabled(!field.isReadOnly() && !readonly);
            }

            counter++;
        }
    }

    public void applySettings() {
        if (!ignoreSettings) {
            int[] fields = module.getSettings().getIntArray(
                    DcRepository.ModuleSettings.stTableColumnOrder);
            setVisibleColumns(fields);
        }
    }

    public void setVisibleColumns(int[] fields) {
        removeColumns();
        
        DcFieldDefinitions definitions = (DcFieldDefinitions) module
                .getSetting(DcRepository.ModuleSettings.stFieldDefinitions);

        for (int field : fields) {

            DcFieldDefinition definition = definitions.get(field);

            if (!module.canBeLended()
                    && (field == DcObject._SYS_AVAILABLE
                            || field == DcObject._SYS_LOANDURATION
                            || field == DcObject._SYS_LENDBY
                            || field == DcObject._SYS_LOANDAYSTILLOVERDUE || field == DcObject._SYS_LOANDUEDATE))
                continue;

            try {
                TableColumn column = columns.get(Integer.valueOf(field));

                if (column == null)
                    continue;

                if (definition.isRequired())
                    column.setHeaderRenderer(DcTableHeaderRendererRequired
                            .getInstance());
                else
                    column.setHeaderRenderer(DcTableHeaderRenderer
                            .getInstance());

                String label = definition.getLabel();

                if (label != null && label.length() > 0) {
                    column.setHeaderValue(label);
                } else {
                    column.setHeaderValue(module
                            .getField(definition.getIndex()).getSystemName());
                }

                addColumn(column);

            } catch (Exception e) {
                Integer key = definition.getIndex();
                TableColumn column = columns.containsKey(key) ? columns
                        .get(key) : null;
                logger.debug("Error while applying settings to column "
                        + column + " for field definition "
                        + definition.getLabel());
            }
        }

        applyHeaders();
    }

    private void removeColumns() {
        for (TableColumn column : columns.values())
            removeColumn(column);
    }

    public void resetTable() {
        removeColumns();

        for (DcFieldDefinition definition : module.getFieldDefinitions()
                .getDefinitions()) {
            TableColumn column = columns.get(Integer.valueOf(definition
                    .getIndex()));
            addColumn(column);
        }
    }

    private void setProperties() {
        setAutoscrolls(true);

        setDefaultRenderer(Object.class, DcTableCellRenderer.getInstance());
        setFont(ComponentFactory.getStandardFont());

        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(false);
        setRequestFocusEnabled(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        setAlignmentY(JTable.TOP_ALIGNMENT);
        getTableHeader().setReorderingAllowed(false);

        setBackground(new Color(255, 255, 255));
        setGridColor(new Color(220, 220, 200));

        setShowHorizontalLines(false);
        setShowVerticalLines(false);
        setIntercellSpacing(new Dimension());

        applyHeaders();

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    public boolean isListeningForChanges() {
        TableModelListener[] listeners = getDcModel().getTableModelListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof TableValueChangedAction)
                return true;
        }
        return false;
    }

    public void setListeningForChanges(boolean b) {
        boolean enable = b && caching;

        TableModelListener[] listeners = getDcModel().getTableModelListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof TableValueChangedAction)
                getDcModel().removeTableModelListener(listeners[i]);
        }

        if (enable && getListeners(tableChangeListener.getClass()).length == 0)
            getDcModel().addTableModelListener(tableChangeListener);
    }

    private class TableValueChangedAction implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            if (!ignoreEdit
                    && (e.getType() == TableModelEvent.INSERT || e.getType() == TableModelEvent.UPDATE)) {

                int row = getSelectedRow();

                Component component = null;

                try {
                    component = getEditorComponent();
                } catch (Exception exp) {
                }

                if (component == null) {
                    addRowToCache(row, e.getColumn());
                } else {
                    int field = getFieldForColumnIndex(e.getColumn());
                    try {
                        if (component.isEnabled()
                                && !module.getField(field).isUiOnly())
                            addRowToCache(row, e.getColumn());
                    } catch (Exception whatever) {
                        addRowToCache(row, e.getColumn());
                    }
                }
            }
        }
    }

    public void afterUpdate() {
    }

    public DcObject getItem(String ID) {
        int index = getIndex(ID);
        return index >= 0 ? getItemAt(index) : null;
    }

    private int getIndex(String ID) {
        for (int i = 0; i < getItemCount(); i++) {
            String objectID = getObjectID(i);
            if (ID.equals(objectID))
                return i;
        }
        return -1;
    }

    public int getItemCount() {
        return super.getRowCount();
    }

    public DcModule getModule() {
        return module;
    }

    public int getSelectedIndex() {
        return getSelectedRow();
    }

    public int[] getSelectedIndices() {
        return super.getSelectedRows();
    }

    public DcObject getSelectedItem() {
        return getItemAt(getSelectedIndex());
    }

    public int locationToIndex(Point point) {
        return super.rowAtPoint(point);
    }

    public boolean remove(String[] ids) {
        boolean removed = false;
        for (String ID : ids) {
            int idx = getIndex(ID);
            if (idx > -1) {
                removeRow(idx);
                removed = true;
            }
        }
        return removed;
    }

    public void addSelectionListener(ListSelectionListener lsl) {
        removeSelectionListener(lsl);
        getSelectionModel().addListSelectionListener(lsl);
    }

    public void removeSelectionListener(ListSelectionListener lsl) {
        getSelectionModel().removeListSelectionListener(lsl);
    }

    @Override
    protected void paintComponent(Graphics g) {
    	try {
    		super.paintComponent(DcSwingUtilities.setRenderingHint(g));
    	} catch(Exception e) {
    		super.paintComponent(g);
    	}
    }
}
