package formula.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import formula.*;
/**
 * 業務部での処方登録確認ビュー
 */
public class ConfRegV extends JFrame {
  private ConfReg crc;
  private AbstractTableModel tm;
  private JScrollPane sp = null;
  private JTable table = null;
  /**
   * ConfRegV コンストラクター・コメント。
   */
  public ConfRegV(ConfReg crc, AbstractTableModel tm) {
    super("管理部への登録");
    this.crc = crc;
    this.tm = tm;
    init();
  }

    public ConfRegV(ConfRegC confRegC, AbstractTableModel crtm) {
    }

    /**
   */
  private void init() {
    Container cp = getContentPane();
    JPanel p0, p1, p2, p3;
    JMenuBar mb = new JMenuBar();
    setJMenuBar(mb);
    JMenu m = new JMenu("ファイル(F)");
    m.setMnemonic('F');
    mb.add(m);
    JMenuItem mi = new JMenuItem("クリップボードへコピー(C)");
    mi.setMnemonic('C');
    mi.addActionListener(e -> crc.copyToClip());
    m.add(mi);
    m = new JMenu("表示(V)");
    m.setMnemonic('V');
    mb.add(m);
    mi = new JMenuItem("最新表示に更新(U)");
    mi.setMnemonic('U');
    mi.addActionListener(e -> {
    });
    m.add(mi);

    p0 = new JPanel(new BorderLayout());
    cp.add(p0, BorderLayout.CENTER);
    initTable();
    sp = new JScrollPane(table);
    p0.add(sp, BorderLayout.CENTER);
    p1 = new JPanel(new FlowLayout());
    p0.add(p1, BorderLayout.SOUTH);
    JButton b = new JButton("更新(U)");
    b.setMnemonic('U');
    b.addActionListener(e -> crc.update());
    p1.add(b);
    b = new JButton("キャンセル(C)");
    b.setMnemonic('C');
    b.addActionListener(e -> crc.close());
    p1.add(b);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	crc.close();
      }
    });	
  }
  /**
   * このメソッドは VisualAge で作成されました。
   */
  private void initTable() {
    TableColumnModel tcm = new DefaultTableColumnModel();
    TableColumn tc = new TableColumn(0, 32);
    tc.setHeaderValue("登録");
    tcm.addColumn(tc);
    tc = new TableColumn(1, 48);
    tc.setHeaderValue("中間品");
    tcm.addColumn(tc);
    tc = new TableColumn(2, 80);
    tc.setHeaderValue("登録日");
    tcm.addColumn(tc);
    tc = new TableColumn(3, 256);
    tc.setHeaderValue("品名");
    tcm.addColumn(tc);
    tc = new TableColumn(4, 64);
    tc.setHeaderValue("登録者");
    tcm.addColumn(tc);
	
    table = new JTable(tm, tcm);
    table.setCellSelectionEnabled(true);
    table.setRowSelectionAllowed(false);
    table.setColumnSelectionAllowed(false);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setPreferredScrollableViewportSize(new Dimension(512, 64));
  }
}
