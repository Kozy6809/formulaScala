package myui;

import java.awt.*;
import javax.swing.*;
/**
 * editorComponentがJTextFieldの場合、編集の開始を空文字列から始めるようにする
 */
public class EmptyTextCellEditor extends DefaultCellEditor {
  private boolean editText = false;
  /**
   * EmptyTextCellEditor コンストラクター・コメント。
   * @param x com.sun.java.swing.JCheckBox
   */
  public EmptyTextCellEditor(JCheckBox x) {
    super(x);
  }
  /**
   * EmptyTextCellEditor コンストラクター・コメント。
   * @param x com.sun.java.swing.JComboBox
   */
  public EmptyTextCellEditor(JComboBox x) {
    super(x);
  }
  /**
   * EmptyTextCellEditor コンストラクター・コメント。
   * @param x com.sun.java.swing.JTextField
   */
  public EmptyTextCellEditor(JTextField x) {
    super(x);
    editText = true;
  }
  /**
   * 初期値として空文字列を設定するように変更したメソッド
   * @return Component
   * @param table com.sun.java.swing.JTable
   * @param value java.lang.Object
   * @param isSelected boolean
   * @param row int
   * @param column int
   */
  public Component getTableCellEditorComponent
    (JTable table, Object value, boolean isSelected, int row, int column) {
      Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
      if (!editText) return c;
      JTextField tf = (JTextField)c;
      tf.setText("");
      tf.getCaret().setVisible(true);
      return tf;
  }

  /**
   * 初期値として空文字列を設定するように変更したメソッド
   * @return java.awt.Component
   * @param tree com.sun.java.swing.JTree
   * @param value java.lang.Object
   * @param isSelected boolean
   * @param expanded boolean
   * @param leaf boolean
   * @param row int
   */
  public Component getTreeCellEditorComponent
    (JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
    Object o = (editText) ? "" : value;
    return super.getTreeCellEditorComponent(tree, o, isSelected, expanded, leaf, row);
  }
}
