package formula.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import formula.*;
import myui.*;
/**
 * 処方リンクに追加する新規の処方を指定するためのダイアログ
 */
public class NewLinkDlg extends JDialog {
  private FormulaLinkC flc;
  private NumberField nf = new NumberField(6);
  private int pcode = -1;
  private String series = null;
  private String name = null;
  private JLabel nameLabel = new JLabel("不正なコードです　");
  private boolean invalidName = true;
  private String person = null;
  private JLabel personLabel = new JLabel("登録者：");
  private boolean thisIsMaster = false; // 追加する処方に既存処方を合わせていくならtrue
  private boolean canceled = false;
  /**
   * NewLinkDlg コンストラクター・コメント。
   * @param arg1 java.awt.Frame
   */
  public NewLinkDlg(Frame f) {
    super(f, "処方リンクに追加", true);
    //	this.flm = flm;
    init();
  }
  /**
   * NewLinkDlg コンストラクター・コメント。
   * @param arg1 java.awt.Frame
   */
  public NewLinkDlg(Frame f, FormulaLinkC flc) {
    super(f, "処方リンクに追加", true);
    this.flc = flc;
    init();
  }
  /**
   * 入力された製造コードから品名を取得して表示する
   */
  private void code2name() {
    String s = nf.getText();
    if (s.length() == 0) return;
    pcode = new Integer(s).intValue();
    flc.code2name(pcode);
  }
  /**
   * 製品名を取り出す
   * @return java.lang.String
   */
  public String getName() {
    return name;
  }
  /**
   * @return int
   */
  public int getPcode() {
    return pcode;
  }
  /**
   * シリーズ名を取り出す
   * @return java.lang.String
   */
  public String getSeries() {
    return series;
  }
  private void init() {
    Border emp = BorderFactory.createEmptyBorder(1, 1, 1, 1);
    Border etc = BorderFactory.createEtchedBorder();
    Border paneBorder = BorderFactory.createCompoundBorder(
							   emp,
							   BorderFactory.createCompoundBorder(etc, emp)
							   );

    JPanel p0, p1, p2;
    Container cp = getContentPane();
    JLabel l = new JLabel("追加する処方の製造コードを指定して下さい　");
    cp.add(l, BorderLayout.NORTH);
    p0 = new JPanel(new BorderLayout());
    cp.add(p0, BorderLayout.CENTER);
    p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p0.add(p1, BorderLayout.NORTH);
    p1.add(personLabel);

    nf.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	code2name();
      }
    });
    nf.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
	code2name();
      }
    });
    p1.add(nf);
    p1.add(nameLabel);
    p2 = new JPanel(new BorderLayout());
    p0.add(p2, BorderLayout.CENTER);
    p0 = new JPanel(new GridLayout(2, 1));
    p2.add(p0, BorderLayout.NORTH);
    p0.setBorder(paneBorder);

    ButtonGroup bg = new ButtonGroup();
    JRadioButton rb;
    rb = new JRadioButton("この処方を変更して、既登録のリンク処方に一致させる　");
    rb.setSelected(true);
    bg.add(rb);
    rb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	thisIsMaster = false;
      }
    });
    p0.add(rb);
    rb = new JRadioButton("既登録のリンク処方を変更して、この処方に一致させる　");
    bg.add(rb);
    rb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	thisIsMaster = true;
      }
    });
    p0.add(rb);

    p1 = new JPanel(new BorderLayout());
    p2.add(p1, BorderLayout.CENTER);
    p0 = new JPanel(new FlowLayout());
    p1.add(p0, BorderLayout.NORTH);
    JButton b;
    b = new JButton("決定(G)");
    b.setMnemonic('G');
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	if (invalidName) return;
	canceled = false;
	setVisible(false);
      }
    });
    p0.add(b);
    b = new JButton("キャンセル(C)");
    b.setMnemonic('C');
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	canceled = true;
	setVisible(false);
      }
    });
    p0.add(b);
  }
  /**
   * @return boolean
   */
  public boolean isCanceled() {
    return canceled;
  }
  /**
   * このメソッドは VisualAge で作成されました。
   * @return boolean
   */
  public boolean isMaster() {
    return thisIsMaster;
  }
  /**
   * 製品名をセットする
   * @param name java.lang.String
   */
  public void setNames(String series, String name) {
    this.series = series;
    this.name = name;
    if (name == null) {
      invalidName = true;
      this.nameLabel.setText("不正なコードです");
    } else {
      invalidName = false;
      this.nameLabel.setText(series + " " + name);
    }
  }
  /**
   * 更新者の名前をセットする
   * @param person java.lang.String
   */
  public void setPerson(String person) {
    this.person = person;
    personLabel.setText("登録者：" + person);
  }
}
