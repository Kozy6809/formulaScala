package formula.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import formula.*;
import myui.*;

/**
 * 新製品の登録ウィンドウ
 */
public class NewProdView extends JFrame {
  private NewProdC npc;
  private JList series = new JList();
  private NumberField pcode = new NumberField(6);
  private JTextField name = new JTextField(16);
  private JLabel selectedSeries = new JLabel("");
  /**
   * NewProdView コンストラクター・コメント。
   */
  public NewProdView(NewProdC npc) {
    super();
    this.npc = npc;
    init();
  }
  /**
   * @return java.lang.String
   */
  public String getName() {
    return name.getText();
  }
  /**
   * @return int
   */
  public int getPcode() {
    try {
      return new Integer(pcode.getText()).intValue();
    } catch (NumberFormatException e) {
      return 0;
    }
  }
  /**
   * @return java.lang.String
   */
  public String getSelectedSeries() {
    return selectedSeries.getText();
  }
  /**
   * @return javax.swing.JList
   */
  public JList getSeries() {
    return series;
  }
  /**
   */
  private void init() {
    setTitle("新製品登録");
    Container cp = getContentPane();
    cp.setLayout(new BorderLayout(8, 8));
    JPanel p0, p1, p2, p3;
    JButton b;
    JLabel l;

    p0 = new JPanel(new BorderLayout(4, 4));
    cp.add(p0, BorderLayout.WEST);
    JScrollPane sp = new JScrollPane(series);
    p0.add(sp, BorderLayout.CENTER);
    b = new JButton("新シリーズ名を登録(S)");
    b.setMnemonic('S');
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	npc.showNewSeriesDlg();
      }
    });
    p0.add(b, BorderLayout.SOUTH);

    p0 = new JPanel(new BorderLayout());
    cp.add(p0, BorderLayout.EAST);
    p1 = new JPanel(new BorderLayout());
    p0.add(p1, BorderLayout.NORTH);
    p2 = new JPanel(new BorderLayout());
    p1.add(p2, BorderLayout.NORTH);
    p3 = new JPanel(new GridLayout(3, 1));
    p2.add(p3, BorderLayout.WEST);
    l = new JLabel("シリーズ名");
    p3.add(l);
    l = new JLabel("製造コード(P)");
    l.setDisplayedMnemonic('P');
    l.setLabelFor(pcode);
    p3.add(l);
    l = new JLabel("品名(N)");
    l.setDisplayedMnemonic('N');
    l.setLabelFor(name);
    p3.add(l);
    p3 = new JPanel(new GridLayout(3, 1));
    p2.add(p3, BorderLayout.CENTER);
    p3.add(selectedSeries);
    p3.add(pcode);
    p3.add(name);

    p2 = new JPanel(new GridLayout(3, 1));
    p1.add(p2, BorderLayout.SOUTH);
    l = new JLabel("");
    p2.add(l);
    l = new JLabel("まずシリーズ名を選択して下さい　");
    p2.add(l);
    l = new JLabel("品名にはシリーズ名を含めません　");
    p2.add(l);
    p1 = new JPanel(new FlowLayout());
    p0.add(p1, BorderLayout.SOUTH);
    b = new JButton("登録(R)");
    b.setMnemonic('R');
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	npc.update();
      }
    });
    p1.add(b);
    b = new JButton("戻る(Q)");
    b.setMnemonic('Q');
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	npc.close();
      }
    });
    p1.add(b);

    series.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    series.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
	String s = (String)series.getSelectedValue();
	selectedSeries.setText((s == null) ? "" : s);
      }
    });
	
		
  }
  /**
   * @param s java.lang.String
   */
  public void setSelectedSeries(String s) {
    selectedSeries.setText(s);
  }
}
