package formula.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import formula.*;
/**
 * 毒性レポートを表示する
 */
public class PoisonListV extends javax.swing.JFrame {
  private PoisonListC plc;
  private PoisonListM plm;
  private JTable ptable = null;
  private JTable ctable = null;
  /**
   * PoisonListV コンストラクター・コメント。
   */
  public PoisonListV
    (PoisonListC plc, PoisonListM plm, String title) {
    super(title + "の毒性");
    this.plc = plc;
    this.plm = plm;
    init();
  }
  /**
   */
  private void init() {
    JMenuBar mb = new JMenuBar();
    setJMenuBar(mb);
    JMenu m = new JMenu("ファイル(F)");
    m.setMnemonic('F');
    mb.add(m);
    JMenuItem mi = new JMenuItem("クリップボードへコピー(C)");
    mi.setMnemonic('C');
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	plc.copyToClip();
      }
    });
    m.add(mi);

    Container cp = getContentPane();
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    cp.setLayout(gridbag);
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.WEST;
    JPanel p0 = new JPanel();
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.weighty = 0.3;
    gridbag.setConstraints(p0, c);
    cp.add(p0);
    JPanel p1 = new JPanel();
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 1.0;
    c.weighty = 0.9;
    gridbag.setConstraints(p1, c);
    cp.add(p1);


    p0.setLayout(new BorderLayout());
    JLabel l = new JLabel("毒性リスト");
    p0.add(l, BorderLayout.NORTH);
    TableColumnModel tcm = new DefaultTableColumnModel();
    PoisonCellRenderer pcr = new PoisonCellRenderer(plm, SwingConstants.CENTER);
    TableColumn tc = new TableColumn(0, 64, pcr, null);
    tc.setHeaderValue("No");
    tcm.addColumn(tc);
    pcr = new PoisonCellRenderer(plm, SwingConstants.LEFT);
    tc = new TableColumn(1, 256, pcr, null);
    tc.setHeaderValue("摘要");
    tcm.addColumn(tc);
    pcr = new PoisonCellRenderer(plm, SwingConstants.RIGHT);
    tc = new TableColumn(2, 64, pcr, null);
    tc.setHeaderValue("しきい値");
    tcm.addColumn(tc);
    pcr = new PoisonCellRenderer(plm, SwingConstants.RIGHT);
    tc = new TableColumn(3, 64, pcr, null);
    tc.setHeaderValue("含有量");
    tcm.addColumn(tc);
    ptable = new JTable(null, tcm);
    ptable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    ptable.setPreferredScrollableViewportSize(new Dimension(448, 64));
    p0.add(new JScrollPane(ptable), BorderLayout.CENTER);

    p1.setLayout(new BorderLayout());
    l = new JLabel("各原料の寄与");
    p1.add(l, BorderLayout.NORTH);
    tcm = new DefaultTableColumnModel();
    ContrbCellRenderer ccr = new ContrbCellRenderer(plm, SwingConstants.CENTER);
    tc = new TableColumn(0, 64, ccr, null);
    tc.setHeaderValue("No");
    tcm.addColumn(tc);
    ccr = new ContrbCellRenderer(plm, SwingConstants.LEFT);
    tc = new TableColumn(1, 256, ccr, null);
    tc.setHeaderValue("摘要");
    tcm.addColumn(tc);
    ccr = new ContrbCellRenderer(plm, SwingConstants.LEFT);
    tc = new TableColumn(2, 64, ccr, null);
    tc.setHeaderValue("資材記号");
    tcm.addColumn(tc);
    ccr = new ContrbCellRenderer(plm, SwingConstants.RIGHT);
    tc = new TableColumn(3, 64, ccr, null);
    tc.setHeaderValue("含有量");
    tcm.addColumn(tc);
    ctable = new JTable(null, tcm);
    ctable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    ctable.setPreferredScrollableViewportSize(new Dimension(448, 64));
    p1.add(new JScrollPane(ctable), BorderLayout.CENTER);
	
  }
  /**
   * このメソッドは VisualAge で作成されました。
   * @param ptm javax.swing.table.TableModel
   * @param ctm javax.swing.table.TableModel
   */
  public void setModel(TableModel ptm, TableModel ctm) {
    ptable.setModel(ptm);
    ctable.setModel(ctm);
  }
}
