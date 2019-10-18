package formula.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import formula.*;
import myui.*;

/**
 * この型は VisualAge で作成されました。
 */
public class FBrowseView extends JFrame {
    private FBrowseC fbc;
    private JLabel lDate = new JLabel("date");
    private JLabel lPerson = new JLabel("person");
    private NumberField tSG = new NumberField("0");
    private JLabel lPrice = new JLabel("0");
    private JTextArea aComment = new JTextArea(2, 8);
    private JTextArea aReason = new JTextArea(2, 8);
    private JTable fTable = null;
    private JLabel total = new JLabel("0.000");
    private JRadioButtonMenuItem miShowNorm = null;
    private JLabel poison = new JLabel("");
    private int rowOnPopup = 0; // テーブルでポップアップメニューが表示された時の行位置

    public FBrowseView(FBrowseC fbvc, String title) {
        super(title);
        this.fbc = fbvc;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        init();
    }

    public FBrowseView(FBrowseViewC fBrowseViewC, String title) {
    }

    /**
     * @return javax.swing.JTextArea
     */
    public JTextArea getComment() {
        return aComment;
    }

    /**
     * @return javax.swing.JLabel
     */
    public JLabel getDate() {
        return lDate;
    }

    /**
     * 現在表示されているのが通常処方ならtrueを返す
     *
     * @return boolean
     */
    public boolean getDispMode() {
        return miShowNorm.isSelected();
    }

    /**
     * @return javax.swing.JLabel
     */
    public JLabel getPerson() {
        return lPerson;
    }

    /**
     * @return javax.swing.JLabel
     */
    public JLabel getPrice() {
        return lPrice;
    }

    /**
     * @return javax.swing.JTextArea
     */
    public JTextArea getReason() {
        return aReason;
    }

    /**
     * @return myui.NumberField
     */
    public NumberField getSG() {
        return tSG;
    }

    /**
     * @return javax.swing.JTable
     */
    public JTable getTable() {
        return fTable;
    }

    /**
     * @return javax.swing.JLabel
     */
    public JLabel getTotal() {
        return total;
    }

    /**
     */
    private void init() {
        Container cp = getContentPane();
        JPanel p0, p1, p2, p3;
        JLabel l;
        Border bd = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        JMenuBar mb = new JMenuBar();
        setJMenuBar(mb);
        JMenu m = new JMenu("ファイル(F)");
        m.setMnemonic('F');
        mb.add(m);
        JMenuItem mi = new JMenuItem("クリップボードへコピー(C)");
        mi.setMnemonic('C');
        mi.addActionListener(e -> fbc.copyToClip(miShowNorm.isSelected()));
        m.add(mi);
        mi = new JMenuItem("更新権限の設定(A)");
        mi.setMnemonic('A');
        mi.addActionListener(e -> fbc.authorize());
        m.add(mi);
        mi = new JMenuItem("処方の更新(U)");
        mi.setMnemonic('U');
        mi.addActionListener(e -> fbc.update());
        m.add(mi);
        mi = new JMenuItem("クローズ(X)");
        mi.setMnemonic('X');
        m.add(mi);
        mi.addActionListener(e -> fbc.requestClose());
        m = new JMenu("表示(S)");
        m.setMnemonic('S');
        mb.add(m);
        mi = new JMenuItem("履歴リスト(H)");
        mi.setMnemonic('H');
        mi.addActionListener(e -> fbc.showHistory());
        m.add(mi);
        mi = new JMenuItem("処方リンク状況(L)");
        mi.setMnemonic('L');
        mi.addActionListener(e -> fbc.showLinkList());
        m.add(mi);
        mi = new JMenuItem("毒性レポート(P)");
        mi.setMnemonic('P');
        mi.addActionListener(e -> fbc.showPoisonList());
        m.add(mi);
        m.add(new JSeparator());
        ButtonGroup bg = new ButtonGroup();
        miShowNorm = new JRadioButtonMenuItem("通常処方(N)");
        mi = miShowNorm;
        mi.setMnemonic('N');
        mi.setSelected(true);
        mi.addActionListener(e -> fbc.showNorm());
        bg.add(mi);
        m.add(mi);
        mi = new JRadioButtonMenuItem("分解処方(D)");
        mi.setMnemonic('D');
        bg.add(mi);
        mi.addActionListener(e -> fbc.showDecomp());
        m.add(mi);
        m.add(new JSeparator());
        mi = new JMenuItem("毒性の再計算(R)");
        mi.setMnemonic('R');
        bg.add(mi);
        mi.addActionListener(e -> fbc.recalcPoison());
        m.add(mi);
        m = new JMenu("印刷(P)");
        m.setMnemonic('P');
        mb.add(m);
        mi = new JMenuItem("印刷(P)");
        mi.setMnemonic('P');
        mi.addActionListener(e -> fbc.print(miShowNorm.isSelected()));
        m.add(mi);

        p0 = new JPanel(new BorderLayout());
        cp.add(p0, BorderLayout.NORTH);
        p1 = new JPanel(new BorderLayout());
        p1.setBorder(bd);
        p0.add(p1, BorderLayout.WEST);
        p2 = new JPanel(new GridLayout(4, 1));
        p1.add(p2, BorderLayout.WEST);
        l = new JLabel("登録日");
        l.setBorder(bd);
        p2.add(l);
        l = new JLabel("登録者");
        l.setBorder(bd);
        p2.add(l);
        l = new JLabel("比重");
        l.setBorder(bd);
        p2.add(l);
        l = new JLabel("g単価");
        l.setBorder(bd);
        p2.add(l);
        p2 = new JPanel(new GridLayout(4, 1));
        p1.add(p2, BorderLayout.CENTER);
        lDate.setBorder(bd);
        lPerson.setBorder(bd);
        //	tSG.setBorder(bd);
        tSG.setAllowDouble(true);
        lPrice.setBorder(bd);
        p2.add(lDate);
        p2.add(lPerson);
        p2.add(tSG);
        p2.add(lPrice);
        p1 = new JPanel(new BorderLayout());
        p1.setBorder(bd);
        p0.add(p1, BorderLayout.CENTER);
        p2 = new JPanel(new GridLayout(2, 1));
        p1.add(p2, BorderLayout.WEST);
        l = new JLabel("特記事項");
        l.setBorder(bd);
        p2.add(l);
        l = new JLabel("更新理由");
        l.setBorder(bd);
        p2.add(l);
        p2 = new JPanel(new GridLayout(2, 1));
        p1.add(p2, BorderLayout.CENTER);
        Border lbd = BorderFactory.createCompoundBorder(bd, BorderFactory.createLineBorder(Color.black));
        aComment.setText("a quick brown forx jumped over the lazy dog's back.\nand back\nand back and back");
        JScrollPane sp = new JScrollPane(aComment);
        sp.setBorder(lbd);
        p2.add(sp);
        sp = new JScrollPane(aReason);
        sp.setBorder(lbd);
        p2.add(sp);

        p0 = new JPanel(new BorderLayout());
        cp.add(p0, BorderLayout.CENTER);
        initTable();
        p0.add(new JScrollPane(fTable), BorderLayout.CENTER);
        p1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p1.setBorder(bd);
        p0.add(p1, BorderLayout.SOUTH);
        poison.setForeground(Color.red);
        p1.add(poison);
        p1.add(new JLabel("合計："));
        p1.add(total);

        setEditable(false);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                fbc.requestClose();
            }
        });
    }

    /**
     * tableのポップアップメニューを作る
     *
     * @return javax.swing.JPopupMenu
     */
    private JPopupMenu initPopup() {
        JPopupMenu pm = new JPopupMenu();
        JMenuItem mi = new JMenuItem("1行挿入");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fbc.insertRow(rowOnPopup);
            }
        });
        pm.add(mi);
        mi = new JMenuItem("1行削除");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fbc.deleteRow(rowOnPopup);
            }
        });
        pm.add(mi);
        mi = new JMenuItem("下行と交換");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fbc.exchangeRow(rowOnPopup);
            }
        });
        pm.add(mi);
        return pm;
    }

    /**
     * このメソッドは VisualAge で作成されました。
     */
    private void initTable() {
        TableColumnModel tcm = new DefaultTableColumnModel();
        FormulaCellRenderer fcr = new FormulaCellRenderer(fbc, this, SwingConstants.CENTER);
        final NumberField nf = new NumberField();
        EmptyTextCellEditor etce = new EmptyTextCellEditor(nf) {
            public Object getCellEditorValue() {
                return new Integer(nf.getText());
            }
        };
        TableColumn tc = new TableColumn(0, 64, fcr, etce);
        tc.setHeaderValue("コード");
        tcm.addColumn(tc);
        fcr = new FormulaCellRenderer(fbc, this, SwingConstants.LEFT);
        etce = new EmptyTextCellEditor(new JTextField());
        tc = new TableColumn(1, 128, fcr, etce);
        tc.setHeaderValue("資材記号");
        tcm.addColumn(tc);
        fcr = new FormulaCellRenderer(fbc, this, SwingConstants.RIGHT);
        fcr.setFractionDigits(3);
        final NumberField nf2 = new NumberField();
        nf2.setAllowDouble(true);
        etce = new EmptyTextCellEditor(nf2) {
            public Object getCellEditorValue() {
                return new Double(nf2.getText());
            }
        };
        tc = new TableColumn(2, 64, fcr, etce);
        tc.setHeaderValue("比率");
        tcm.addColumn(tc);

        fTable = new JTable(null, tcm);
        fTable.setCellSelectionEnabled(true);
        fTable.setRowSelectionAllowed(false);
        fTable.setColumnSelectionAllowed(false);
        fTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        fTable.setPreferredScrollableViewportSize(new Dimension(64, 192));

        final JPopupMenu pm = initPopup();
        fTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (!fbc.isEditing()) return;
                if (e.isPopupTrigger()) {
                    rowOnPopup = fTable.rowAtPoint(new Point(e.getX(), e.getY()));
                    pm.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * このメソッドは VisualAge で作成されました。
     */
    public void selectShowNormMenu() {
        miShowNorm.setSelected(true);
    }

    /**
     * 各コンポーネントの編集可能性を設定する
     *
     * @param b boolean
     */
    public void setEditable(boolean b) {
        tSG.setEditable(b);
        aComment.setEditable(b);
        aReason.setEditable(b);
    }

    /**
     * 毒性の有無を表示する
     *
     * @param b boolean 毒性があればtrue
     */
    public void setPoison(boolean b) {
        if (b) poison.setText("毒性あり　");
        else poison.setText("");
    }

    /**
     * このメソッドは VisualAge で作成されました。
     */
    public void showNorm() {
        miShowNorm.setSelected(true);
    }
}
