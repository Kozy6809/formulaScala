package formula.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import formula.*;
/**
 * 新製品登録画面から新シリーズ名を登録する際に呼び出されるダイアログ
 */
public class NewSeriesDialog extends JDialog {
  private NewProdView npv;
  private JTextField newSeries = new JTextField(16);
  /**
   * NewSeriesDialog コンストラクター・コメント。
   * @param arg1 java.awt.Frame
   */
  public NewSeriesDialog(NewProdView npv) {
    super(npv, "", true);
    this.npv = npv;
    init();
  }
  /**
   * NewSeriesDialog コンストラクター・コメント。
   * @param arg1 java.awt.Frame
   */
  public NewSeriesDialog(java.awt.Frame arg1) {
    super(arg1, "", true);
    init();
  }
  /**
   * @return java.lang.String
   */
  public String getNewSeries() {
    return newSeries.getText();
  }
  /**
   */
  private void init() {
    setLocationRelativeTo(npv);
    setTitle("新シリーズ名登録");
    Container cp = getContentPane();
    cp.setLayout(new GridLayout(3, 1, 4, 4));
    cp.add(new JLabel("新しいシリーズ名を入力して下さい　"));
    cp.add(newSeries);
    JButton b = new JButton("決定(G)");
    b.setMnemonic('G');
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	setVisible(false);
      }
    });
    cp.add(b);
  }
}
