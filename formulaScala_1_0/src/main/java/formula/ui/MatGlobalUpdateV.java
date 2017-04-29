package formula.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import formula.*;

/**
 * 原料の一括更新のビュー。
 */
public class MatGlobalUpdateV extends JFrame {
  private MatGlobalUpdateC mguc;
  private JTextField orgCode = new JTextField(8);
  private JTextField orgName = new JTextField(24);
  private JTextField altCode = new JTextField(8);
  private JTextField altName = new JTextField(24);
  /**
   * このメソッドは VisualAge で作成されました。
   */
  public MatGlobalUpdateV() {
    super();
    init();
  }
  /**
   * MatGlobalUpdateV コンストラクター・コメント。
   */
  public MatGlobalUpdateV(MatGlobalUpdateC mguc) {
    super("原料の一括更新");
    this.mguc = mguc;
    init();
  }
  /**
   */
  private void init() {
    Container cp = getContentPane();
    JPanel p0, p1, p2;
    p0 = new JPanel(new BorderLayout());
    cp.add(p0, BorderLayout.CENTER);
    p1 = new JPanel(new BorderLayout());
    p0.add(p1, BorderLayout.WEST);
    Border eb = BorderFactory.createEtchedBorder();
    TitledBorder tb = BorderFactory.createTitledBorder(eb, "更新される原料");
    p1.setBorder(tb);
    p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p1.add(p2, BorderLayout.NORTH);
    JLabel l = new JLabel("資材コード(C)");
    l.setDisplayedMnemonic('C');
    l.setLabelFor(orgCode);
    p2.add(l);
    p2.add(orgCode);
    p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p1.add(p2, BorderLayout.SOUTH);
    l = new JLabel("資材記号(N)");
    l.setDisplayedMnemonic('N');
    l.setLabelFor(orgName);
    p2.add(l);
    p2.add(orgName);
	
    p1 = new JPanel(new BorderLayout());
    p0.add(p1, BorderLayout.EAST);
    tb = BorderFactory.createTitledBorder(eb, "新しい原料");
    p1.setBorder(tb);
    p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p1.add(p2, BorderLayout.NORTH);
    l = new JLabel("資材コード(D)");
    l.setDisplayedMnemonic('D');
    l.setLabelFor(altCode);
    p2.add(l);
    p2.add(altCode);
    p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p1.add(p2, BorderLayout.SOUTH);
    l = new JLabel("資材記号(M)");
    l.setDisplayedMnemonic('M');
    l.setLabelFor(altName);
    p2.add(l);
    p2.add(altName);

    p0 = new JPanel(new FlowLayout());
    cp.add(p0, BorderLayout.SOUTH);
    JButton b = new JButton("更新(U)");
    b.setMnemonic('U');
    p0.add(b);
    b = new JButton("キャンセル(A)");
    b.setMnemonic('A');
    p0.add(b);
	
  }
}
