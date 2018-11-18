package formula.ui;

import formula.*;
import myutil.*;
import java.text.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
/**
 * 毒性レポートの原料寄与表(ctable)用レンダラ
 */
public class ContrbCellRenderer extends DefaultTableCellRenderer {
  private PoisonListM plm;
  /**
   * PoisonCellRenderer コンストラクター・コメント。
   */
  public ContrbCellRenderer(PoisonListM plm, int horizontalAlignment) {
    super();
    this.plm = plm;
    setHorizontalAlignment(horizontalAlignment);
  }
  /**
   * 毒性が閾値を越えている行を赤で表示するようにする
   * @return Component
   * @param table javax.swing.JTable
   * @param value java.lang.Object
   * @param isSelected boolean
   * @param hasFocus boolean
   * @param row int
   * @param column int
   */
  public Component getTableCellRendererComponent
    (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    boolean b = plm.isCover(row);
    if (b) c.setForeground(Color.red);
    else c.setForeground(Color.black);
    return c;
  }
  /**
   * 
   */
  public void setValue(Object value) {
    if (value instanceof Double) {
      setText(ExpFormat.format(((Double)value).doubleValue(), 3));
    } else setText(value.toString());
  }
}
