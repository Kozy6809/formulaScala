package formula.ui;

/**
 * この クラス は SmartGuide によって生成されました。
 * 
 */
import java.awt.*;

public class BorderedPanel extends java.awt.Panel {
  /**
   * BorderedPanel コンストラクター・コメント
   */
  public BorderedPanel() {
    super();
  }
  /**
 * BorderedPanel コンストラクター・コメント
 * @param layout java.awt.LayoutManager
 */
  public BorderedPanel(java.awt.LayoutManager layout) {
    super(layout);
  }
  /**
 * このメソッドは SmartGuide によって作成されました。
 * @param g Graphics
 */
  public void paint(Graphics g) {
    //	super.paint(g);
    Dimension d = getSize();
    g.setColor(Color.black);
    g.drawRoundRect(0, 0, d.width-1, d.height-1, 5, 5);
  }
}
