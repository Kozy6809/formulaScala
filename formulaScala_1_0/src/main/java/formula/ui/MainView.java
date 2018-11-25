package formula.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import formula.*;
import myui.*;

import java.util.prefs.Preferences;

/**
 * メイン画面。以下のコンポーネントで構成される。<br>
 * <ul>検索モード設定ラジオボタン</ul>
 * <ul>品種選択テキストボックス</ul>
 * <ul>検索コード入力テキストフィールド。数字しか入力できない</ul>
 * <ul>検索文字列入力テキストフィールド。ワイルドカード対応</ul>
 * <ul>検索結果リスト。ここから複数選択して処方ブラウザを表示できる</ul>
 */
public class MainView extends JFrame {
    private MainC mvc;
    private JList<String> seriesList = new JList<>();
    private JList<String> resultList = new JList<>();
    private NumberField codeField = new NumberField();
    private JTextField nameField = new JTextField();
    private int searchMode = 0;
    private JLabel selectedNames = new JLabel("全品種");
    private boolean allSeriesSelected = true;
    private final Color[] obsColor = {Color.black, Color.magenta, Color.red};

    private Preferences prefs;

    /**
     * MainView コンストラクター・コメント。
     */
    public MainView(MainC mvc) {
        super();
        this.mvc = mvc;
        prefs = Preferences.userNodeForPackage(MainView.class);
        init();
        int w = prefs.getInt("width", -1);
        int h = prefs.getInt("height", -1);
        if (w < 0 || h < 0) pack();
        else setSize(w, h);
    }

    /**
     * クローズ時にウィンドウのステータスを保存する。現在はウィンドウサイズのみ
     */
    public void saveStatus() {
        Dimension d = getSize();
        prefs.putInt("width", d.width);
        prefs.putInt("height", d.height);
    }

    /**
     * @return javax.swing.JTextField
     */
    public JTextField getCodeField() {
        return codeField;
    }

    /**
     * @return javax.swing.JTextField
     */
    public JTextField getNameField() {
        return nameField;
    }

    /**
     * @return javax.swing.JList
     */
    public JList<String> getResultList() {
        return resultList;
    }

    /**
     * 指定された検索コードを返す。テキストが空文字列だった場合は-1を返す
     *
     * @return int
     */
    public int getSearchCode() {
        String s = codeField.getText();
        return (s.length() == 0) ? -1 : Integer.parseInt(s);
    }

    /**
     * @return int
     */
    public int getSearchMode() {
        return searchMode;
    }

    /**
     * 指定された検索名を返す
     *
     * @return java.lang.String
     */
    public String getSearchName() {
        return nameField.getText();
    }

    /**
     * @return javax.swing.JList
     */
    public JList<String> getSeriesList() {
        return seriesList;
    }

    /**
     */
    private void init() {
        setTitle("処方データベース");
        Container cp = getContentPane();

        // create menus
        JMenuBar mb = new JMenuBar();
        JMenu m;
        JMenuItem mi;
        setJMenuBar(mb);
        m = new JMenu("ファイル(F)");
        m.setMnemonic('F');
        mb.add(m);
        mi = new JMenuItem("クリップボードへコピー(C)");
        mi.setMnemonic('C');
        mi.addActionListener(e -> mvc.copyToClip());
        m.add(mi);
        mi = new JMenuItem("終了(Q)");
        mi.setMnemonic('Q');
        mi.addActionListener(e -> mvc.exit());
        m.add(mi);
        m = new JMenu("一括処理(G)");
        m.setMnemonic('G');
        mb.add(m);
        mi = new JMenuItem("資材の一括変更(M)");
        mi.setMnemonic('M');
        mi.addActionListener(e -> mvc.showMGU());
        m.add(mi);
        m = new JMenu("ウィンドウ(W)");
        m.setMnemonic('W');
        mb.add(m);
        mi = new JMenuItem("新原料ウィンドウ(M)");
        mi.setMnemonic('M');
        mi.addActionListener(e -> mvc.showNMC());
        m.add(mi);
        mi = new JMenuItem("新製品ウィンドウ(P)");
        mi.setMnemonic('P');
        mi.addActionListener(e -> mvc.showNPC());
        m.add(mi);
        mi = new JMenuItem("処方リンクウィンドウ(L)");
        mi.setMnemonic('L');
        mi.addActionListener(e -> mvc.showFLC());
        m.add(mi);
        mi = new JMenuItem("登録確認ウィンドウ(R)");
        mi.setMnemonic('R');
        mi.addActionListener(e -> mvc.showCRC());
        m.add(mi);

        Border emp = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        Border etc = BorderFactory.createEtchedBorder();
        Border paneBorder = BorderFactory.createCompoundBorder(
                emp,
                BorderFactory.createCompoundBorder(etc, emp)
        );
        JPanel p0, p1, p2, p3;

        // create panels
        p0 = new JPanel(new BorderLayout());
        cp.add(p0, BorderLayout.WEST);
        p1 = new JPanel(new GridLayout(3, 1));
        p1.setBorder(paneBorder);
        p0.add(p1, BorderLayout.NORTH);
        ButtonGroup bg = new ButtonGroup();
        JRadioButton rb;
        rb = new JRadioButton("製品で検索(P)");
        rb.setMnemonic('P');
        rb.setSelected(true);
        bg.add(rb);
        rb.addActionListener(e -> searchMode = 0);
        p1.add(rb);
        rb = new JRadioButton("原料で検索(M)");
        rb.setMnemonic('M');
        bg.add(rb);
        rb.addActionListener(e -> searchMode = 1);
        p1.add(rb);
        rb = new JRadioButton("原料で分解処方を検索(D)");
        rb.setMnemonic('D');
        bg.add(rb);
        rb.addActionListener(e -> searchMode = 2);
        p1.add(rb);

        p1 = new JPanel(new BorderLayout());
        p1.setBorder(paneBorder);
        p0.add(p1, BorderLayout.CENTER);
        JLabel l = new JLabel("検索する品種");
        p1.add(l, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(seriesList);
        p1.add(sp, BorderLayout.CENTER);
        p2 = new JPanel(new GridLayout(3, 1));
        p1.add(p2, BorderLayout.SOUTH);
        p3 = new JPanel(new FlowLayout());
        p2.add(p3);
        JButton b;
        b = new JButton("全品種を選択(A)");
        b.setMnemonic('A');
        b.addActionListener(e -> {
            seriesList.clearSelection();
            selectedNames.setText("全品種");
        });
        p3.add(b);
        l = new JLabel("選択されている品種：");
        p2.add(l);
        p2.add(selectedNames);

        p0 = new JPanel(new BorderLayout());
        p0.setBorder(paneBorder);
        cp.add(p0, BorderLayout.CENTER);
        p1 = new JPanel(new BorderLayout());
        p0.add(p1, BorderLayout.NORTH);
        p2 = new JPanel(new GridLayout(2, 1));
        p1.add(p2, BorderLayout.WEST);
        l = new JLabel("検索するコード(C)");
        l.setDisplayedMnemonic('C');
        l.setLabelFor(codeField);
        p2.add(l);
        l = new JLabel("検索する名前(N)");
        l.setDisplayedMnemonic('N');
        l.setLabelFor(nameField);
        p2.add(l);
        p2 = new JPanel(new GridLayout(2, 1));
        p1.add(p2, BorderLayout.CENTER);
        codeField.addActionListener(e -> {
            nameField.setText("");
            getResultList().clearSelection();
            mvc.searchByCode(seriesList.getSelectedValuesList(), getSearchCode(), searchMode);
        });
        p2.add(codeField);
        nameField.addActionListener(e -> {
            codeField.setText("");
            getResultList().clearSelection();
            mvc.searchByName(seriesList.getSelectedValuesList(), getSearchName(), searchMode);
        });
        p2.add(nameField);
        p1 = new JPanel(new BorderLayout());
        p0.add(p1, BorderLayout.CENTER);
        l = new JLabel("検索結果", SwingConstants.CENTER);
        l.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        p1.add(l, BorderLayout.NORTH);
        sp = new JScrollPane(resultList);
        p1.add(sp, BorderLayout.CENTER);
        p2 = new JPanel(new FlowLayout());
        p1.add(p2, BorderLayout.SOUTH);
        b = new JButton("選択した製品の処方を表示(S)");
        b.setMnemonic('S');
        b.addActionListener(e -> mvc.showFBrowser(resultList.getSelectedIndices()));
        p2.add(b);

        // シリーズの選択状態をselectedNamesに反映させる
        seriesList.addListSelectionListener(e -> {
            java.util.List<String> s = seriesList.getSelectedValuesList();
            if (s.size() == 0) {
                allSeriesSelected = true;
                selectedNames.setText("全品種");
            } else {
                allSeriesSelected = false;
                selectedNames.setText(String.join(" ", s));
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                mvc.exit();
            }
        });
    }

    /**
     * codeFieldとnameFieldの表示色を廃番状態に応じて設定する
     * @param status 0-現行 1-廃番予定 2-廃番
     */
    public void setObsoleteStatus(int status) {
        codeField.setForeground(obsColor[status]);
        nameField.setForeground(obsColor[status]);
    }
}
/*
$Id: MainView.java,v 1.1 2008/10/17 01:15:38 wakui Exp $
*/
