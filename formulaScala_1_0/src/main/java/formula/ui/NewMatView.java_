package formula.ui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import formula.*;
import myui.*;
/**
 * 新原料の登録画面
 */
public class NewMatView extends JFrame {
  private NewMatC nmc;
  private NumberField mcode = new NumberField(6);
  private JTextField mname = new JTextField(16);;
  private NumberField price = new NumberField(6);
  /**
   * NewMatView コンストラクター・コメント。
   */
  public NewMatView(NewMatC nmc) {
    super();
    this.nmc = nmc;
    init();
  }
  /**
   * @return int
   */
  public int getMcode() {
    String s = mcode.getText();
    try {
      return new Integer(s).intValue();
    } catch (NumberFormatException e) {
      return 0;
    }
  }
  /**
   * @return java.lang.String
   */
  public String getMname() {
    return mname.getText();
  }
  /**
   * @return double
   */
  public double getPrice() {
    String s = price.getText();
    try {
      return new Double(s).doubleValue();
    } catch (NumberFormatException e) {
      return 0.0;
    }
  }
	
  /**
   */
  private void init() {
    price.setAllowDouble(true);
    setTitle("新原料登録");
    Container cp = getContentPane();
    JPanel p0, p1, p2;
    JLabel l;
    JButton b;

    p0 = new JPanel(new BorderLayout());
    cp.add(p0, BorderLayout.WEST);
    p1 = new JPanel(new BorderLayout());
    p0.add(p1, BorderLayout.NORTH);
    p2 = new JPanel(new GridLayout(3, 1));
    p1.add(p2, BorderLayout.WEST);
    l = new JLabel("資材コード(C)");
    l.setDisplayedMnemonic('C');
    l.setLabelFor(mcode);
    p2.add(l);
    l = new JLabel("資材記号(S)");
    l.setDisplayedMnemonic('S');
    l.setLabelFor(mname);
    p2.add(l);
    l = new JLabel("グラム単価(P)");
    l.setDisplayedMnemonic('P');
    l.setLabelFor(price);
    p2.add(l);
    p2 = new JPanel(new GridLayout(3, 1));
    p1.add(p2, BorderLayout.EAST);
    p2.add(mcode);
    p2.add(mname);
    p2.add(price);

    p0 = new JPanel(new BorderLayout());
    cp.add(p0, BorderLayout.EAST);
    p1 = new JPanel(new GridLayout(5, 1));
    p0.add(p1, BorderLayout.NORTH);
    l = new JLabel("　空欄に記入の上　");
    p1.add(l);
    l = new JLabel("　登録ボタンを押して下さい　");
    p1.add(l);
    l = new JLabel("");
    p1.add(l);
    l = new JLabel("　単価はキログラム単価ではないので　");
    p1.add(l);
    l = new JLabel("　注意して下さい　");
    p1.add(l);
	
    p0 = new JPanel(new FlowLayout());
    cp.add(p0, BorderLayout.SOUTH);
    b = new JButton("登録(R)");
    b.setMnemonic('R');
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	nmc.update();
      }
    });
    p0.add(b);
    b = new JButton("戻る(Q)");
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	nmc.close();
      }
    });
    b.setMnemonic('Q');
    p0.add(b);
  }
}
