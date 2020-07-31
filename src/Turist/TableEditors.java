
package Turist;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author mozevil
 */
public class TableEditors {
    
}


class EachRowEditor implements TableCellEditor {
  protected Hashtable editors;

  protected TableCellEditor editor, defaultEditor;

  JTable table;

  /**
   * Constructs a EachRowEditor. create default editor
   * 
   * @see TableCellEditor
   * @see DefaultCellEditor
   */
  public EachRowEditor(JTable table) {
    this.table = table;
    editors = new Hashtable();
    defaultEditor = new DefaultCellEditor(new JTextField());
  }

  /**
   * @param row
   *            table row
   * @param editor
   *            table cell editor
   */
  public void setEditorAt(int row, TableCellEditor editor) {
    editors.put(new Integer(row), editor);
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected, int row, int column) {
    //editor = (TableCellEditor)editors.get(new Integer(row));
    //if (editor == null) {
    //  editor = defaultEditor;
    //}
    return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
  }

  public Object getCellEditorValue() {
    return editor.getCellEditorValue();
  }

  public boolean stopCellEditing() {
    return editor.stopCellEditing();
  }

  public void cancelCellEditing() {
    editor.cancelCellEditing();
  }

  public boolean isCellEditable(EventObject anEvent) {
    selectEditor((MouseEvent) anEvent);
    return editor.isCellEditable(anEvent);
  }

  public void addCellEditorListener(CellEditorListener l) {
    editor.addCellEditorListener(l);
  }

  public void removeCellEditorListener(CellEditorListener l) {
    editor.removeCellEditorListener(l);
  }

  public boolean shouldSelectCell(EventObject anEvent) {
    selectEditor((MouseEvent) anEvent);
    return editor.shouldSelectCell(anEvent);
  }

  protected void selectEditor(MouseEvent e) {
    int row;
    if (e == null) {
      row = table.getSelectionModel().getAnchorSelectionIndex();
    } else {
      row = table.rowAtPoint(e.getPoint());
    }
    editor = (TableCellEditor) editors.get(new Integer(row));
    if (editor == null) {
      editor = defaultEditor;
    }
  }
}


class EachRowRenderer implements TableCellRenderer {  
  protected Hashtable renderers;  
  protected TableCellRenderer renderer, defaultRenderer;  
  
  public EachRowRenderer() {  
    renderers = new Hashtable();  
    defaultRenderer = new DefaultTableCellRenderer();  
  }  
    
  public void add(int row, TableCellRenderer renderer) {  
    renderers.put(new Integer(row),renderer);  
  }  
    
  public Component getTableCellRendererComponent(JTable table,  
      Object value, boolean isSelected, boolean hasFocus,  
                                      int row, int column) {  
    renderer = (TableCellRenderer)renderers.get(new Integer(row));  
    if (renderer == null) {  
      renderer = defaultRenderer;  
    }  
    return renderer.getTableCellRendererComponent(table,  
             value, isSelected, hasFocus, row, column);  
  }  
}  


class CheckBoxCellEditor extends AbstractCellEditor implements TableCellEditor {

        private static final long serialVersionUID = 1L;
        protected JCheckBox checkBox;

        public CheckBoxCellEditor() {
            checkBox = new JCheckBox();
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            //checkBox.setBackground(Color.WHITE);
        }

        public Component getTableCellEditorComponent(
            JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {
            checkBox.setSelected(((Boolean) value).booleanValue());
            return checkBox;

        }

        public Object getCellEditorValue() {
            return Boolean.valueOf(checkBox.isSelected());
        }
    }


class CWCheckBoxRenderer extends JCheckBox implements TableCellRenderer {

        private static final long serialVersionUID = 1L;
        Border border = new EmptyBorder(1, 2, 1, 2);

        public CWCheckBoxRenderer() {
            super();
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
            if (value instanceof Boolean) {
                setSelected(((Boolean) value).booleanValue());
                //setEnabled(table.isCellEditable(row, column));
                setForeground(table.getForeground());
                setBackground(table.getBackground());

            }
            return this;
        }
    }
