package formula.ui;

import formula.*;
import java.text.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
/**
 * 処方ブラウザのテーブル用レンダラ
 * 資材のステータスによって色を変えて表示し、数値表示の書式をサポートし、
 * setValue()メソッドを適切にオーバーライドする
 */
public class FormulaCellRenderer extends DefaultTableCellRenderer {
  private IFormulaModel fm;
  private FBrowseView fbv;
  private NumberFormat nf = NumberFormat.getInstance();
  /**
   * FormulaCellRenderer コンストラクター・コメント。
   */
  public FormulaCellRenderer(IFormulaModel fm, FBrowseView fbv, int horizontalAlignment) {
    super();
    this.fm = fm;
    this.fbv = fbv;
    setHorizontalAlignment(horizontalAlignment);
  }
  /**
   * 資材ステータスを読みだし、それによってテキストの表示色を変える
   * @return Component
   * @param table javax.swing.JTable
   * @param value java.lang.Object
   * @param isSelected boolean
   * @param hasFocus boolean
   * @param row int
   * @param column int
   */
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    int status = (fbv.getDispMode()) ? fm.getNormMatStatus(row) : fm.getDecompMatStatus(row);
    switch (status) {
    case 0:
      c.setForeground(Color.black);
      break;
    case 1:
      c.setForeground(Color.magenta);
      break;
    case 2:
      c.setForeground(Color.red);
      break;
    default:
    }
    return c;
  }
  /**
   * @param d int
   */
  public void setFractionDigits(int fd) {
    nf.setMinimumFractionDigits(fd);
    nf.setMaximumFractionDigits(fd);
  }
  /**
   * 
   */
  public void setValue(Object value) {
    if (value instanceof Integer) {
      setText(((Integer)value).toString());
    } else if (value instanceof Double) {
      setText(nf.format(((Double)value).doubleValue()));
    } else setText(value.toString());
  }
}
